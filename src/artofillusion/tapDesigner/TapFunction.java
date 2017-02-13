/*
 *  Function Module class. Most of its code is taken from FunctionModule.java, written by Peter
 *  Eastman
 */
/*
 *  Copyright (C) 2000,2002 by Peter Eastman, 2003 by Francois Guillet
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.tapDesigner;

import artofillusion.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import artofillusion.procedural.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.reflect.*;
import buoy.widget.*;
import buoy.event.*;


/**
 *  Description of the Class
 *
 *@author     pims
 *@created    19 avril 2004
 */
public class TapFunction
{
    private TapFunction function;
    boolean repeat;
    double x[], y[];
    double a0[], a1[], a2[], a3[], b[];
    short shape;
    FunctionDialog editDialog;
    boolean isEditDialogOn;
    private static double xclip[], yclip[];
    private static short clipShape;
    private static boolean clipRepeat;

    /**
     *  Description of the Field
     */
    public final static short LINEAR = 0;
    /**
     *  Description of the Field
     */
    public final static short INTERPOLATING = 1;


    /**
     *  Constructor for the TapFunction object
     */
    public TapFunction()
    {
        x = new double[]{0.0, 1.0};
        y = new double[]{1.0, 0.0};
        shape = LINEAR;
        calcCoefficients();
        function = this;
    }


    /**
     *  Constructor for the TapFunction object
     *
     *@param  y1  Description of the Parameter
     *@param  y2  Description of the Parameter
     */
    public TapFunction( double y1, double y2 )
    {
        x = new double[]{0.0, 1.0};
        y = new double[]{y1, y2};
        shape = LINEAR;
        calcCoefficients();
        function = this;
    }


    /**
     *  Constructor for the TapFunction object
     *
     *@param  in                          Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public TapFunction( DataInputStream in )
        throws IOException, InvalidObjectException
    {
        int count;
        short version = in.readShort();
        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );
        shape = in.readShort();
        repeat = in.readBoolean();
        count = in.readInt();
        x = new double[count];
        y = new double[count];
        for ( int i = 0; i < count; ++i )
        {
            x[i] = in.readDouble();
            y[i] = in.readDouble();
        }
        function = this;
        calcCoefficients();
    }


    /**
     *  Description of the Method
     *
     *@param  out              Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void writeToFile( DataOutputStream out )
        throws IOException
    {
        out.writeShort( 0 );
        out.writeShort( shape );
        out.writeBoolean( repeat );
        out.writeInt( x.length );
        for ( int i = 0; i < x.length; ++i )
        {
            out.writeDouble( x[i] );
            out.writeDouble( y[i] );
        }
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public TapFunction duplicate()
    {
        TapFunction function = new TapFunction();
        function.repeat = this.repeat;
        function.x = new double[x.length];
        function.y = new double[y.length];
        function.shape = shape;
        for ( int i = 0; i < x.length; i++ )
        {
            function.x[i] = x[i];
            function.y[i] = y[i];
        }
        function.calcCoefficients();
        return function;
    }


    /**
     *  Description of the Method
     */
    public void editDialogClosed()
    {
        isEditDialogOn = false;
        editDialog.dispose();
        editDialog = null;
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     *@param  title        Description of the Parameter
     *@param  cb           Description of the Parameter
     *@return              Description of the Return Value
     */
    public FunctionDialog edit( JFrame parentFrame, String title, Runnable cb )
    {
        if ( isEditDialogOn )
            editDialog.toFront();
        else
        {
            editDialog = new FunctionDialog( parentFrame, title, cb );
            isEditDialogOn = true;
        }
        return editDialog;
    }


    /**
     *  Description of the Method
     *
     *@param  value  Description of the Parameter
     *@return        Description of the Return Value
     */
    public double calcValue( double value )
    {
        if ( value <= 0.0 || value >= 1.0 )
        {
            if ( repeat )
                value -= Math.floor( value );
            else if ( value <= 0.0 )
                return y[0];
            else
                return y[y.length - 1];
        }
        int i;
        for ( i = 1; i < x.length && value > x[i]; i++ )
            ;
        i--;
        if ( shape == INTERPOLATING )
        {
            double vl = a0[i] + value * ( 2.0 * a1[i] + value * ( 3.0 * a2[i] + value * 4.0 * a3[i] ) );
            //System.out.println("INterp : "+value+" "+ vl);
            return a0[i] + value * ( 2.0 * a1[i] + value * ( 3.0 * a2[i] + value * 4.0 * a3[i] ) );
        }
        else
        {
            double vl = a0[i] + value * ( 2.0 * a1[i] + value * ( 3.0 * a2[i] + value * 4.0 * a3[i] ) );
            //System.out.println("Approx : "+value+" "+ vl);
            return a0[i] + value * 2.0 * a1[i];
        }
    }


    /*
     *  Calculate the integral of the function at a given point.
     */
    /**
     *  Description of the Method
     *
     *@param  valueIn  Description of the Parameter
     *@return          Description of the Return Value
     */
    public double calcIntegral( double valueIn )
    {
        double vi;
        double vf;
        double result;
        int i;

        if ( repeat )
        {
            vi = Math.floor( valueIn );
            vf = valueIn - vi;
            result = vi * b[b.length - 1];
        }
        else
        {
            if ( valueIn <= 0.0 )
                return valueIn * y[0];
            if ( valueIn >= 1.0 )
                return b[b.length - 1] + ( valueIn - 1.0 ) * y[y.length - 1];
            vf = (float) valueIn;
            result = 0.0;
        }
        if ( vf == 0.0 )
            return result;
        for ( i = 1; i < x.length && vf > x[i]; i++ )
            ;
        i--;
        if ( shape == INTERPOLATING )
            result += b[i] + vf * ( a0[i] + vf * ( a1[i] + vf * ( a2[i] + vf * a3[i] ) ) );
        else
            result += b[i] + vf * ( a0[i] + vf * ( a1[i] ) );
        return result;
    }


    /*
     *  calculate the integral between two x values
     */
    /**
     *  Description of the Method
     *
     *@param  from  Description of the Parameter
     *@param  to    Description of the Parameter
     *@return       Description of the Return Value
     */
    public double calcIntegral( double from, double to )
    {
        return calcIntegral( to ) - calcIntegral( from );
    }


    /**
     *  Gets the oneOverFunction attribute of the TapFunction object
     *
     *@return    The oneOverFunction value
     */
    public TapFunction getOneOverFunction()
    {
        TapFunction function = new TapFunction();
        function.repeat = repeat;
        function.x = new double[x.length];
        function.y = new double[y.length];
        function.shape = shape;
        for ( int i = 0; i < x.length; i++ )
        {
            function.x[i] = x[i];
            if ( y[i] > 0 )
                function.y[i] = 1 / y[i];
            else
                function.y[i] = 10;
        }
        function.calcCoefficients();
        return function;
    }


    /*
     *  Calculate the derivative of the function at a given point.
     */
    /**
     *  Description of the Method
     *
     *@param  value  Description of the Parameter
     *@return        Description of the Return Value
     */
    public double calcDeriv( double value )
    {
        double deriv;
        if ( value <= 0.0 || value > 1.0 )
        {
            if ( !repeat )
            {
                return 0.0;
            }
            value -= Math.floor( value );
        }
        int i;
        for ( i = 1; i < x.length && value > x[i]; i++ )
            ;
        i--;
        if ( shape == INTERPOLATING )
            deriv = 2.0 * a1[i] + value * ( 6.0 * a2[i] + value * 12.0 * a3[i] );
        else
            deriv = 2.0 * a1[i];
        return deriv;
    }


    /**
     *  Description of the Method
     */
    private void calcCoefficients()
    {
        a0 = new double[x.length - 1];
        a1 = new double[x.length - 1];
        a2 = new double[x.length - 1];
        a3 = new double[x.length - 1];
        b = new double[x.length];
        if ( shape == LINEAR )
        {
            for ( int i = 0; i < a0.length; i++ )
            {
                double dx = x[i + 1] - x[i];
                if ( dx == 0.0 )
                    continue;
                a1[i] = ( y[i + 1] - y[i] ) / dx;
                a0[i] = y[i] - a1[i] * x[i];
                a1[i] *= 0.5;
                b[i + 1] = b[i] + x[i + 1] * ( a0[i] + x[i + 1] * a1[i] ) - x[i] * ( a0[i] + x[i] * a1[i] );
            }
            for ( int i = 1; i < b.length - 1; i++ )
                b[i] -= x[i] * ( a0[i] + x[i] * a1[i] );
            return;
        }
        double m[][] = new double[4][4];
        double a[] = new double[4];
        double deriv[] = new double[x.length];
        for ( int i = 1; i < x.length - 1; i++ )
            if ( x[i - 1] != x[i + 1] )
                deriv[i] = ( y[i + 1] - y[i - 1] ) / ( x[i + 1] - x[i] );
        if ( repeat )
            deriv[0] = deriv[x.length - 1] = ( y[1] - y[y.length - 2] ) / ( 1.0 + x[1] - x[x.length - 2] );
        for ( int i = 0; i < a0.length; i++ )
        {
            m[0][0] = 0.0;
            m[0][1] = 1.0;
            m[0][2] = 2.0 * x[i];
            m[0][3] = 3.0 * x[i] * x[i];
            a[0] = deriv[i];
            m[1][0] = 1.0;
            m[1][1] = x[i];
            m[1][2] = x[i] * x[i];
            m[1][3] = x[i] * x[i] * x[i];
            a[1] = y[i];
            m[2][0] = 1.0;
            m[2][1] = x[i + 1];
            m[2][2] = x[i + 1] * x[i + 1];
            m[2][3] = x[i + 1] * x[i + 1] * x[i + 1];
            a[2] = y[i + 1];
            m[3][0] = 0.0;
            m[3][1] = 1.0;
            m[3][2] = 2.0 * x[i + 1];
            m[3][3] = 3.0 * x[i + 1] * x[i + 1];
            a[3] = deriv[i + 1];
            SVD.solve( m, a );
            a0[i] = a[0];
            a1[i] = 0.5 * a[1];
            a2[i] = a[2] / 3.0;
            a3[i] = 0.25 * a[3];
            b[i + 1] = b[i] + x[i + 1] * ( a0[i] + x[i + 1] * ( a1[i] + x[i + 1] * ( a2[i] + x[i + 1] * a3[i] ) ) ) - x[i] * ( a0[i] + x[i] * ( a1[i] + x[i] * ( a2[i] + x[i] * a3[i] ) ) );
        }
        for ( int i = 1; i < b.length - 1; i++ )
            b[i] -= x[i] * ( a0[i] + x[i] * ( a1[i] + x[i] * ( a2[i] + x[i] * a3[i] ) ) );
    }


    /**
     *  Description of the Class
     *
     *@author     pims
     *@created    19 avril 2004
     */
    public class FunctionDialog extends JFrame implements ActionListener, ItemListener,
            KeyListener, MouseListener, MouseMotionListener, DocumentListener, ChangeListener
    {
        Canvas canvas;
        JTextField xField, yField;
        JCheckBox repeatBox, smoothBox;
        JButton okButton, deleteButton, addButton, cancelButton, applyButton;
        BButton copyButton, pasteButton;
        Point clickPoint, handlePos[];
        Rectangle graphBounds;
        FontMetrics fm;
        NumberFormat hFormat, vFormat;
        int selected;
        boolean clickedOk, fixRange;
        double backx[], backy[], miny, maxy, labelstep;
        boolean deactivateTextFields, modified;
        boolean backRepeat;
        short backShape;
        Runnable onClose;
        boolean isClosing;

        final static int HANDLE_SIZE = 5;


        /**
         *  Constructor for the FunctionDialog object
         *
         *@param  parentFrame  Description of the Parameter
         *@param  title        Description of the Parameter
         *@param  cb           Description of the Parameter
         */
        public FunctionDialog( JFrame parentFrame, String title, Runnable cb )
        {
            //super(parent, "Function", true);
            JPanel p = new JPanel();
            GridBagConstraints gc = new GridBagConstraints();
            GridBagConstraints gcp = new GridBagConstraints();
            JButton b;
            Container contentPane;

            this.onClose = cb;
            p.setLayout( new BorderLayout() );
            JLabel text = TapDesignerTranslate.jlabel( "valueFunctionHelpText" );
            contentPane = getContentPane();
            contentPane.setLayout( new GridBagLayout() );
            gcp.insets = new Insets( 5, 5, 5, 5 );
            gcp.anchor = GridBagConstraints.NORTH;
            contentPane.add( text, gcp );

            canvas =
                new Canvas()
                {
                    public void paint( Graphics g )
                    {
                        paintAxes( g );
                        paintCanvas( g, graphBounds );
                    }


                    public Dimension getPreferredSize()
                    {
                        return new Dimension( 400, 300 );
                    }

                    /*
                     *  public Dimension getMinimumSize()
                     *  {
                     *  return new Dimension (300, 300);
                     *  }
                     */
                };
            canvas.addKeyListener( this );
            canvas.addMouseListener( this );
            canvas.addMouseMotionListener( this );
            canvas.setBackground( Color.white );
            graphBounds = new Rectangle();
            hFormat = NumberFormat.getInstance();
            vFormat = NumberFormat.getInstance();
            hFormat.setMaximumFractionDigits( 1 );
            gcp.gridy = 1;
            gcp.weightx = 1.0;
            gcp.weighty = 1.0;
            gcp.anchor = GridBagConstraints.CENTER;
            contentPane.add( canvas, gcp );

            p = new JPanel();
            p.setLayout( new GridBagLayout() );
            gc.gridy = 0;
            gc.insets = new Insets( 5, 0, 0, 5 );
            p.add( TapDesignerTranslate.jlabel( "x" ), gc );
            p.add( xField = new JTextField( "", 5 ), gc );
            xField.addKeyListener( this );
            p.add( TapDesignerTranslate.jlabel( "y" ), gc );
            p.add( yField = new JTextField( "", 5 ), gc );
            yField.addKeyListener( this );
            p.add( addButton = TapDesignerTranslate.jButton( "add", this ), gc );
            p.add( deleteButton = TapDesignerTranslate.jButton( "delete", this ), gc );
            gc.gridy = 1;
            gc.gridwidth = 4;
            p.add( repeatBox = TapDesignerTranslate.jCheckBox( "periodicFunction", this ), gc );
            repeatBox.setSelected( repeat );
            gc.gridwidth = 2;
            p.add( smoothBox = TapDesignerTranslate.jCheckBox( "smooth", this ), gc );
            smoothBox.setSelected( shape == INTERPOLATING );
            repeatBox.setSelected( repeat );
            smoothBox.setSelected( shape == INTERPOLATING );
            repeatBox.addItemListener( this );
            smoothBox.addItemListener( this );
            gcp.gridy = 2;
            gcp.anchor = GridBagConstraints.SOUTH;
            gcp.weightx = 0.0;
            gcp.weighty = 0.0;
            contentPane.add( p, gcp );
            p = new JPanel();
            copyButton = TapBTranslate.bButton( "copyFunction", this, "doCopyButton" );
            p.add( copyButton.getComponent() );
            pasteButton = TapBTranslate.bButton( "pasteFunction", this, "doPasteButton" );
            p.add( pasteButton.getComponent() );
            gcp.gridy = 3;
            contentPane.add( p, gcp );

            p = new JPanel();
            p.add( okButton = TapDesignerTranslate.jButton( "ok", this ) );
            p.add( applyButton = TapDesignerTranslate.jButton( "apply", this ) );
            p.add( cancelButton = TapDesignerTranslate.jButton( "cancel", this ) );
            gcp.gridy = 4;
            contentPane.add( p, gcp );
            x = function.x;
            y = function.y;
            findRange();
            adjustComponents();
            handlePos = new Point[x.length];
            backx = new double[x.length];
            backy = new double[y.length];
            backShape = shape;
            backRepeat = repeat;
            for ( int i = 0; i < x.length; i++ )
            {
                backx[i] = x[i];
                backy[i] = y[i];
                handlePos[i] = new Point( 0, 0 );
            }
            pack();
            fm = canvas.getFontMetrics( canvas.getFont() );
            setResizable( false );
            setLocationRelativeTo( parentFrame );
            addWindowListener(
                new java.awt.event.WindowAdapter()
                {
                    public void windowClosing( java.awt.event.WindowEvent evt )
                    {
                        exitForm( evt );
                    }
                } );
            this.setTitle( title );
            show();
            xField.getDocument().addDocumentListener( this );
            yField.getDocument().addDocumentListener( this );
            canvas.requestFocus();
            modified = false;
        }


        /*
         *  Adjust the various components in the window based on the currently selected point.
         */
        /**
         *  Description of the Method
         */
        private void adjustComponents()
        {
            deactivateTextFields = true;
            if ( selected == 0 || selected == x.length - 1 )
                deleteButton.setEnabled( false );
            else
                deleteButton.setEnabled( true );
            xField.setText( "" + x[selected] );
            yField.setText( "" + y[selected] );
            boolean movable = ( selected > 0 && selected < x.length - 1 );
            xField.setEnabled( movable );
            deleteButton.setSelected( movable );
            deactivateTextFields = false;
        }


        /*
         *  Determine the range of y values and the labels to use on the y axis.
         */
        /**
         *  Description of the Method
         */
        private void findRange()
        {
            if ( fixRange )
                return;
            miny = Double.MAX_VALUE;
            maxy = -Double.MAX_VALUE;
            for ( int i = 0; i < y.length; i++ )
            {
                if ( y[i] < miny )
                    miny = y[i];
                if ( y[i] > maxy )
                    maxy = y[i];
            }
            if ( miny == maxy )
            {
                miny = Math.floor( miny );
                maxy = miny + 1.0;
            }
            int decimals = (int) Math.floor( Math.log( maxy - miny ) / Math.log( 10.0 ) );
            labelstep = Math.pow( 10.0, decimals );
            vFormat.setMaximumFractionDigits( decimals < 0 ? -decimals : 1 );
        }


        /*
         *  Calculate the position of all the handles.
         */
        /**
         *  Description of the Method
         *
         *@param  r  Description of the Parameter
         */
        private void positionHandles( Rectangle r )
        {
            for ( int i = 0; i < x.length; i++ )
            {
                handlePos[i].x = (int) ( r.x + x[i] * r.width );
                handlePos[i].y = (int) ( r.y + ( maxy - y[i] ) * r.height / ( maxy - miny ) );
            }
        }


        /*
         *  Paint the axes on the graph, and calculate the bounds of the graph.
         */
        /**
         *  Description of the Method
         *
         *@param  g  Description of the Parameter
         */
        private void paintAxes( Graphics g )
        {
            int maxWidth = 0;
            int fontHeight = fm.getHeight();
            Dimension d = canvas.getSize();
            double pos = labelstep * Math.ceil( miny / labelstep );
            String label;

            graphBounds.y = HANDLE_SIZE / 2;
            graphBounds.height = d.height - HANDLE_SIZE - fontHeight - 5;
            g.setColor( Color.black );
            while ( pos <= maxy )
            {
                label = vFormat.format( pos );
                int w = fm.stringWidth( label );
                if ( w > maxWidth )
                    maxWidth = w;
                g.drawString( label, 1, graphBounds.y + ( (int) ( ( maxy - pos ) * graphBounds.height / ( maxy - miny ) ) ) + fontHeight / 2 + 5 );
                pos += labelstep;
            }
            graphBounds.x = maxWidth + 5;
            graphBounds.width = d.width - maxWidth - 5 - HANDLE_SIZE / 2;
            pos = labelstep * Math.ceil( miny / labelstep );
            while ( pos <= maxy )
            {
                int v = graphBounds.y + ( (int) ( ( maxy - pos ) * graphBounds.height / ( maxy - miny ) ) );
                g.drawLine( graphBounds.x - 3, v, graphBounds.x, v );
                pos += labelstep;
            }
            for ( int i = 0; i < 10; i++ )
            {
                label = hFormat.format( 0.1 * i );
                int h = graphBounds.x + ( i * graphBounds.width ) / 10;
                g.drawLine( h, graphBounds.y + graphBounds.height, h, graphBounds.y + graphBounds.height + 3 );
                g.drawString( label, h - fm.stringWidth( label ) / 2, d.height - 4 );
            }
            g.drawLine( graphBounds.x, 0, graphBounds.x, graphBounds.y + graphBounds.height );
            g.drawLine( graphBounds.x, graphBounds.y + graphBounds.height, graphBounds.x + graphBounds.width, graphBounds.y + graphBounds.height );
            positionHandles( graphBounds );
        }


        /*
         *  Draw the canvas.
         */
        /**
         *  Description of the Method
         *
         *@param  g  Description of the Parameter
         *@param  r  Description of the Parameter
         */
        private void paintCanvas( Graphics g, Rectangle r )
        {
            g.setColor( Color.black );
            if ( smoothBox.isSelected() )
            {
                int lastx = handlePos[0].x;
                int lasty = handlePos[0].y;
                for ( int i = 0; i < handlePos.length - 1; i++ )
                {
                    double dx = x[i + 1] - x[i];
                    if ( dx == 0.0 )
                    {
                        g.drawLine( lastx, lasty, handlePos[i + 1].x, handlePos[i + 1].y );
                        lastx = handlePos[i + 1].x;
                        lasty = handlePos[i + 1].y;
                        continue;
                    }
                    for ( int j = 1; j < 8; j++ )
                    {
                        double xf = x[i] + j * 0.125 * dx;
                        double yf = function.calcValue( xf );
                        int nextx = (int) ( r.x + xf * r.width );
                        int nexty = (int) ( r.y + ( maxy - yf ) * r.height / ( maxy - miny ) );
                        g.drawLine( lastx, lasty, nextx, nexty );
                        lastx = nextx;
                        lasty = nexty;
                    }
                    g.drawLine( lastx, lasty, handlePos[i + 1].x, handlePos[i + 1].y );
                    lastx = handlePos[i + 1].x;
                    lasty = handlePos[i + 1].y;
                }
            }
            else
                for ( int i = 0; i < handlePos.length - 1; i++ )
                    g.drawLine( handlePos[i].x, handlePos[i].y, handlePos[i + 1].x, handlePos[i + 1].y );
            for ( int i = 0; i < handlePos.length; i++ )
            {
                if ( selected == i )
                    g.setColor( Color.red );
                else
                    g.setColor( Color.black );
                g.fillRect( handlePos[i].x - HANDLE_SIZE / 2, handlePos[i].y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE );
            }
        }


        /*
         *  Add a new handle at the specified position.
         */
        /**
         *  Adds a feature to the Handle attribute of the FunctionDialog object
         *
         *@param  where  The feature to be added to the Handle attribute
         *@param  val    The feature to be added to the Handle attribute
         */
        public void addHandle( double where, double val )
        {
            modified = true;
            double newx[] = new double[x.length + 1];
            double newy[] = new double[y.length + 1];
            int i;

            for ( i = 0; i < x.length && x[i] < where; i++ )
            {
                newx[i] = x[i];
                newy[i] = y[i];
            }
            newx[i] = where;
            newy[i] = val;
            selected = i;
            for ( ; i < x.length; i++ )
            {
                newx[i + 1] = x[i];
                newy[i + 1] = y[i];
            }
            function.x = x = newx;
            function.y = y = newy;
            handlePos = new Point[x.length];
            for ( i = 0; i < handlePos.length; i++ )
                handlePos[i] = new Point( 0, 0 );
            function.calcCoefficients();
            adjustComponents();
            findRange();
            positionHandles( graphBounds );
            canvas.repaint();
        }


        /*
         *  Delete the currently selected handle.
         */
        /**
         *  Description of the Method
         */
        public void deleteSelectedHandle()
        {
            modified = true;
            if ( selected == 0 || selected == x.length - 1 )
                return;
            double newx[] = new double[x.length - 1];
            double newy[] = new double[y.length - 1];
            int i;

            for ( i = 0; i < x.length - 1; i++ )
            {
                if ( i < selected )
                {
                    newx[i] = x[i];
                    newy[i] = y[i];
                }
                else
                {
                    newx[i] = x[i + 1];
                    newy[i] = y[i + 1];
                }
            }
            selected = 0;
            function.x = x = newx;
            function.y = y = newy;
            handlePos = new Point[x.length];
            for ( i = 0; i < handlePos.length; i++ )
                handlePos[i] = new Point( 0, 0 );
            function.calcCoefficients();
            adjustComponents();
            findRange();
            positionHandles( graphBounds );
            canvas.repaint();
        }


        /**
         *  Gets the closing attribute of the FunctionDialog object
         *
         *@return    The closing value
         */
        public boolean isClosing()
        {
            return isClosing;
        }


        /**
         *  Gets the modified attribute of the FunctionDialog object
         *
         *@return    The modified value
         */
        public boolean isModified()
        {
            return modified;
        }


        /**
         *  Description of the Method
         *
         *@param  evt  Description of the Parameter
         */
        private void exitForm( java.awt.event.WindowEvent evt )
        {
            doCancel();
        }


        /*
         *  Respond to the various buttons.
         */
        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void actionPerformed( ActionEvent ev )
        {
            String command = ev.getActionCommand();

            if ( command.equals( addButton.getActionCommand() ) )
            {
                addHandle( 0.5, calcValue( 0.5 ) );
                modified = true;
            }
            if ( command.equals( deleteButton.getActionCommand() ) )
            {
                deleteSelectedHandle();
                modified = true;
            }
            if ( command.equals( okButton.getActionCommand() ) )
            {
                clickedOk = true;
                repeat = repeatBox.isSelected();
                shape = smoothBox.isSelected() ? INTERPOLATING : LINEAR;
                calcCoefficients();
                //doApply();
                if ( onClose != null )
                {
                    isClosing = true;
                    onClose.run();
                }
                editDialogClosed();
            }
            if ( command.equals( applyButton.getActionCommand() ) )
            {
                clickedOk = true;
                repeat = repeatBox.isSelected();
                shape = smoothBox.isSelected() ? INTERPOLATING : LINEAR;
                calcCoefficients();
                if ( onClose != null )
                    onClose.run();
                //doApply();
            }
            if ( command.equals( cancelButton.getActionCommand() ) )
                doCancel();
        }


        /**
         *  Description of the Method
         */
        private void doCopyButton()
        {
            xclip = new double[x.length];
            yclip = new double[y.length];
            for ( int i = 0; i < xclip.length; ++i )
            {
                xclip[i] = x[i];
                yclip[i] = y[i];
            }
            clipShape = smoothBox.isSelected() ? INTERPOLATING : LINEAR;
            System.out.println( "clipShape :" + clipShape + " " + INTERPOLATING );
            clipRepeat = repeatBox.isSelected();
        }


        /**
         *  Description of the Method
         */
        private void doPasteButton()
        {
            if ( xclip == null )
                return;
            modified = true;
            repeat = clipRepeat;
            repeatBox.setSelected( repeat );
            shape = clipShape;
            smoothBox.setSelected( shape == INTERPOLATING );
            x = new double[xclip.length];
            y = new double[yclip.length];
            for ( int i = 0; i < xclip.length; ++i )
            {
                x[i] = xclip[i];
                y[i] = yclip[i];
            }
            handlePos = new Point[x.length];
            for ( int i = 0; i < handlePos.length; i++ )
                handlePos[i] = new Point( 0, 0 );
            function.calcCoefficients();
            adjustComponents();
            findRange();
            positionHandles( graphBounds );
            canvas.repaint();
        }


        /**
         *  Description of the Method
         */
        private void doCancel()
        {
            if ( modified )
            {
                int r = JOptionPane.showConfirmDialog( this,
                        TapDesignerTranslate.text( "parametersModified" ),
                        TapDesignerTranslate.text( "warning" ), JOptionPane.WARNING_MESSAGE,
                        JOptionPane.YES_NO_OPTION );
                if ( r == JOptionPane.YES_OPTION )
                    modified = false;

            }
            if ( !modified )
            {
                x = backx;
                y = backy;
                repeat = backRepeat;
                shape = backShape;
                function.calcCoefficients();
                if ( onClose != null )
                {
                    isClosing = true;
                    onClose.run();
                }
                editDialogClosed();
            }
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void stateChanged( ChangeEvent e )
        {
            modified = true;
        }


        /*
         *  Respond to keypresses.
         */
        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void keyPressed( KeyEvent ev )
        {
            /*
             *  if (ev.getKeyCode() == KeyEvent.VK_ENTER)
             *  {	modified = true;
             *  clickedOk = true;
             *  repeat = repeatBox.isSelected();
             *  shape = smoothBox.isSelected() ? INTERPOLATING : LINEAR;
             *  calcCoefficients();
             *  editDialogClosed();
             *  }
             */
            if ( ev.getSource() != canvas )
                return;
            if ( ev.getKeyCode() == KeyEvent.VK_BACK_SPACE || ev.getKeyCode() == KeyEvent.VK_DELETE )
            {
                modified = true;
                deleteSelectedHandle();
            }
        }


        /*
         *  Deal with mouse clicks on the canvas and the color preview.
         */
        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void mousePressed( MouseEvent ev )
        {
            fixRange = true;
            clickPoint = ev.getPoint();
            canvas.requestFocus();
            if ( ev.isControlDown() )
            {
                modified = true;
                double h = ( clickPoint.x - graphBounds.x ) / ( graphBounds.width - 1.0 );
                double v = ( graphBounds.height - clickPoint.y + graphBounds.y ) / ( graphBounds.height - 1.0 );
                v = v * ( maxy - miny ) + miny;
                addHandle( 0.001 * ( (int) ( 1000.0 * h ) ), 0.001 * ( (int) ( 1000.0 * v ) ) );
                return;
            }
            for ( int i = 0; i < handlePos.length; i++ )
            {
                int xh = handlePos[i].x;
                int yh = handlePos[i].y;
                if ( clickPoint.x >= xh - HANDLE_SIZE / 2 && clickPoint.x <= xh + HANDLE_SIZE / 2 &&
                        clickPoint.y >= yh - HANDLE_SIZE / 2 && clickPoint.y <= yh + HANDLE_SIZE / 2 )
                {
                    selected = i;
                    adjustComponents();
                    canvas.repaint();
                    return;
                }
            }
            clickPoint = null;
        }


        /*
         *  Move the current selected handle.
         */
        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void mouseDragged( MouseEvent ev )
        {
            if ( clickPoint == null )
                return;
            modified = true;
            Point pos = ev.getPoint();

            handlePos[selected].x = pos.x;
            double newx = ( (double) pos.x - graphBounds.x ) / ( graphBounds.width - 1.0 );
            double newy = ( (double) ( graphBounds.height - pos.y + graphBounds.y ) ) / ( graphBounds.height - 1.0 );
            newy = newy * ( maxy - miny ) + miny;
            if ( newx < 0.0 )
                newx = 0.0;
            if ( newx > 1.0 )
                newx = 1.0;
            if ( newy < miny )
                newy = miny;
            if ( newy > maxy )
                newy = maxy;
            y[selected] = 0.001 * ( (int) ( 1000.0 * newy ) );
            if ( selected == 0 || selected == x.length - 1 )
            {
                adjustComponents();
                canvas.repaint();
                return;
            }
            x[selected] = 0.001 * ( (int) ( 1000.0 * newx ) );
            while ( x[selected] < x[selected - 1] )
            {
                double temp = x[selected];
                x[selected] = x[selected - 1];
                x[selected - 1] = temp;
                temp = y[selected];
                y[selected] = y[selected - 1];
                y[selected - 1] = temp;
                Point tempPos = handlePos[selected];
                handlePos[selected] = handlePos[selected - 1];
                handlePos[selected - 1] = tempPos;
                selected--;
            }
            while ( x[selected] > x[selected + 1] )
            {
                double temp = x[selected];
                x[selected] = x[selected + 1];
                x[selected + 1] = temp;
                temp = y[selected];
                y[selected] = y[selected + 1];
                y[selected + 1] = temp;
                Point tempPos = handlePos[selected];
                handlePos[selected] = handlePos[selected + 1];
                handlePos[selected + 1] = tempPos;
                selected++;
            }
            adjustComponents();
            canvas.repaint();
        }


        /*
         *  Reposition the handles when the user finished dragging one.
         */
        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void mouseReleased( MouseEvent ev )
        {
            clickPoint = null;
            function.calcCoefficients();
            fixRange = false;
            findRange();
            positionHandles( graphBounds );
            canvas.repaint();
            modified = true;
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void changedUpdate( DocumentEvent e )
        {
            if ( !deactivateTextFields )
                textValueChanged( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void insertUpdate( DocumentEvent e )
        {
            if ( !deactivateTextFields )
                textValueChanged( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void removeUpdate( DocumentEvent e )
        {
            if ( !deactivateTextFields )
                textValueChanged( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void textValueChanged( DocumentEvent e )
        {
            double newx = Double.NaN;
            double newy = Double.NaN;
            boolean ok = true;
            modified = true;
            try
            {
                newx = Double.valueOf( xField.getText() ).doubleValue();
            }
            catch ( NumberFormatException ex )
            {
            }
            try
            {
                newy = Double.valueOf( yField.getText() ).doubleValue();
            }
            catch ( NumberFormatException ex )
            {
            }
            if ( newx < 0.0 || newx > 1.0 || Double.isNaN( newx ) )
            {
                xField.setForeground( Color.red );
                ok = false;
            }
            else
            {
                x[selected] = newx;
                xField.setForeground( Color.black );
            }
            if ( Double.isNaN( newy ) )
            {
                yField.setForeground( Color.red );
                ok = false;
            }
            else
            {
                y[selected] = newy;
                yField.setForeground( Color.black );
            }
            if ( ok )
            {
                function.calcCoefficients();
                if ( !fixRange )
                {
                    findRange();
                    positionHandles( graphBounds );
                    canvas.repaint();
                }
            }
        }


        /*
         *  Respond to clicks on the checkboxes.
         */
        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void itemStateChanged( ItemEvent ev )
        {
            function.repeat = repeatBox.isSelected();
            function.shape = smoothBox.isSelected() ? INTERPOLATING : LINEAR;
            function.calcCoefficients();
            canvas.repaint();
        }


        /*
         *  Unused event listener methods.
         */
        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void keyReleased( KeyEvent ev )
        {
        }


        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void keyTyped( KeyEvent ev )
        {
        }


        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void mouseClicked( MouseEvent ev )
        {
        }


        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void mouseEntered( MouseEvent ev )
        {
        }


        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void mouseExited( MouseEvent ev )
        {
        }


        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void mouseMoved( MouseEvent ev )
        {
        }
    }
}

