/*
 *  A top module arranges a decorator on top of the decorated object.
 */
/*
 *  Copyright (C) 2003 by Francois Guillet
 *  Changes copyright (C) 2019 by Maksim Khramov
 *
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

import buoy.widget.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import artofillusion.tapDesigner.TapModule.*;

/**
 *  Description of the Class
 *
 *@author     pims
 *@created    19 avril 2004
 */
public class TopModule extends TapModule implements Cloneable
{
    private TopModule module;
    private static TapModule.ModuleTypeInfo typeInfo;

    double probability;
    boolean sizeChildren;
    double rSizeFactor;
    double ySizeFactor;
    double randomYRotation;
    double inward;
    boolean center;


    /**
     *  Constructor for the TopModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public TopModule( TapProcedure procedure, Point position )
    {
        super(procedure, TapBTranslate.text( "top" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "topName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/top_tree.png" ) ) );

        setNumInput( 2 );
        setNumOutput( 1 );
        probability = 0.5;
        sizeChildren = false;
        rSizeFactor = 1.0;
        ySizeFactor = 1.0;
        center = false;
        inward = 0.0;
        randomYRotation = 0.0;
        setup();
    }


    /**
     *  Description of the Method
     */
    private void setup()
    {
        inputNature[0] = OBJECT_PORT;
        inputNature[1] = OBJECT_PORT;
        outputNature[0] = OBJECT_PORT;
        inputTooltips = new String[2];
        inputTooltips[0] = TapBTranslate.text( "objectDecorate" );
        inputTooltips[1] = TapBTranslate.text( "objectDecorated" );
        outputTooltips = new String[1];
        outputTooltips[0] = TapBTranslate.text( "objectOutput" );
        setBackgroundColor( Color.orange.darker() );
        module = this;
    }


    /**
     *  Constructor for the TopModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public TopModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        probability = in.readDouble();
        sizeChildren = in.readBoolean();
        inward = in.readDouble();
        center = in.readBoolean();
        rSizeFactor = in.readDouble();
        ySizeFactor = in.readDouble();
        randomYRotation = in.readDouble();
        setup();
    }


    /**
     *  Description of the Method
     *
     *@param  out              Description of the Parameter
     *@param  theScene         Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    @Override
    public void writeToFile( DataOutputStream out, Scene theScene )
        throws IOException
    {
        super.writeToFile( out, theScene );
        out.writeShort( 0 );
        out.writeDouble( probability );
        out.writeBoolean( sizeChildren );
        out.writeDouble( inward );
        out.writeBoolean( center );
        out.writeDouble( rSizeFactor );
        out.writeDouble( ySizeFactor );
        out.writeDouble( randomYRotation );
    }


    /**
     *  Gets the moduleTypeInfo attribute of the TopModule object
     *
     *@return    The moduleTypeInfo value
     */
    @Override
    public ModuleTypeInfo getModuleTypeInfo()
    {
        return typeInfo;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public TapModule duplicate()
    {
        TopModule module = new TopModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
        module.probability = this.probability;
        module.sizeChildren = this.sizeChildren;
        module.inward = this.inward;
        module.center = this.center;
        module.rSizeFactor = this.rSizeFactor;
        module.ySizeFactor = this.ySizeFactor;
        module.randomYRotation = this.randomYRotation;

        return (TapModule) module;
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    @Override
    public void edit( BFrame parentFrame )
    {
        super.edit( parentFrame );

        if ( isEditDialogOn )
            editDialog.toFront();
        else
        {
            editDialog = new TopModuleDialog( (JFrame) parentFrame.getComponent() );
            isEditDialogOn = true;
        }
    }


    //upstream process
    /**
     *  Gets the object attribute of the TopModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The object value
     */
    @Override
    public TapDesignerObjectCollection getObject( int outputPort, long seed )
    {
        TapDesignerObjectCollection col;
        TapRandomGenerator gen = new TapRandomGenerator( seed );
        BackModuleLink.BackLink bl;
        int j;

        if ( outputPort == -1 )
        {
            //preview wanted !

            stopHere = true;
            col = procedure.getTempObject();
            stopHere = false;

            return col;
        }
        else if ( outputPort == -2 )
        {
            //preview up to

            BackModuleLink backLinks = procedure.getBackLink();
            bl = backLinks.findModule( this, 0 );
            col = null;

            if ( bl != null )
                col = bl.fromModule.getObject( bl.outputPort, gen.getSeed() );

            if ( col == null )
                return null;

            TapDesignerObjectCollection tmpCollection = new TapDesignerObjectCollection( procedure );

            if ( inputPortLink[0] != null )
            {
                //modules linked to output will decorate object

                for ( j = 0; j < inputPortLink[0].length; ++j )
                {
                    TapModule mod = modules.get( linkToIndex[0][j] );
                    TapDesignerObjectCollection modCol = mod.getObject( col, inputPortLink[0][j], gen.getSeed() );

                    if ( modCol != null )
                        col.mergeCollection( modCol, 1 );
                }
            }

            return col;
        }
        else
            return null;
    }


    /**
     *  Gets the object attribute of the TopModule object
     *
     *@param  collection  Description of the Parameter
     *@param  inputPort   Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The object value
     */
    @Override
    public TapDesignerObjectCollection getObject( TapDesignerObjectCollection collection, int inputPort, long seed )
    {
        double Ysize;
        Vec3 size;
        ObjectInfo anInfo;
        ObjectInfo mainObject;
        int j;
        int level;
        int count;
        double sizeR;
        double sizeY;
        double dum;
        double Ycenter;
        CoordinateSystem coords = null;
        TapDesignerObjectCollection col;
        TapRandomGenerator gen = new TapRandomGenerator( seed );
        BackModuleLink.BackLink bl;
        BoundingBox bounds;
        boolean duplicate;
        Mat4 msm;
        Mat4 m;
        String objName;

        level = collection.elementAt( 0 ).getDecorationLevel();

        if ( inputPort == 1 )
        {
            boolean nullObject = false;
            TapDesignerObjectCollection newCollection = new TapDesignerObjectCollection( procedure );
            BackModuleLink backLinks = procedure.getBackLink();
            mainObject = collection.elementAt( 0 ).objectInfo;
            size = mainObject.object.getBounds().getSize();
            Ysize = size.y;
            Ycenter = Ysize / 2;
            bl = backLinks.findModule( this, 0 );
            col = null;

            if ( bl != null )
                col = bl.fromModule.getObject( bl.outputPort, gen.getSeed() );

            duplicate = false;

            if ( col == null )
            {
                Object3D obj = (Object3D) new NullObject();
                obj.setSize( 0, 0, 0 );
                anInfo = new ObjectInfo( obj, new CoordinateSystem(), "dummy" );
                coords = anInfo.coords;
                bounds = anInfo.getBounds();
                dum = bounds.getSize().y;
                coords.setOrigin( new Vec3( 0, dum / 2, 0 ) );

                if ( collection.size() == 0 )
                    return null;

                col = new TapDesignerObjectCollection( procedure );
                col.addObject( anInfo, 0, collection.elementAt( 0 ).sizeR, collection.elementAt( 0 ).sizeY, name );
                nullObject = true;
                Ycenter = 0;
                objName = name;
            }
            else
            {
                if ( bl.fromModule instanceof ObjectModule )
                    duplicate = ( (ObjectModule) bl.fromModule ).isDuplicate();
                else
                    duplicate = false;

                objName = bl.fromModule.getName();
            }

            if ( gen.uniformDeviate() < probability )
            {
                anInfo = col.elementAt( 0 ).objectInfo;
                sizeR = 1.0;
                sizeY = 1.0;

                if ( ( sizeChildren ) && ( !nullObject ) && ( !duplicate ) )
                {
                    size = anInfo.object.getBounds().getSize();
                    sizeR *= collection.elementAt( 0 ).sizeR * rSizeFactor + 1 - rSizeFactor;
                    sizeY *= collection.elementAt( 0 ).sizeY * ySizeFactor + 1 - ySizeFactor;

                    if ( !( anInfo.object instanceof TapObject ) )
                    {
                        anInfo.object.setSize( size.x * sizeR, size.y * sizeY, size.z * sizeR );
                        TapUtils.setObjectAtMinY( anInfo );
                    }
                    else
                        ( (TapObject) anInfo.object ).resizeAndDistort( size, sizeR, sizeY, null );

                    coords = anInfo.coords;
                    bounds = anInfo.getBounds();
                    dum = bounds.getSize().y;
                    Ycenter = dum / 2;

                    if ( !( anInfo.object instanceof TapObject ) )
                        TapUtils.setObjectAtMinY( anInfo );
                }

                TapDesignerObjectCollection tmpCollection = new TapDesignerObjectCollection( procedure );
                tmpCollection.addObject( anInfo, level + 1, sizeR, sizeY, objName );

                if ( ( inputPortLink[0] != null ) && ( !stopHere ) )
                {
                    //modules linked to output will decorate object

                    for ( j = 0; j < inputPortLink[0].length; ++j )
                    {
                        TapModule mod = modules.get( linkToIndex[0][j] );
                        TapDesignerObjectCollection modCol = mod.getObject( tmpCollection, inputPortLink[0][j], gen.getSeed() );

                        if ( modCol != null )
                            tmpCollection.mergeCollection( modCol, 0 );
                    }
                }

                if ( ( inputPortLink[0] != null ) && ( stopHere ) )
                    //preserve random sequence
                    gen.getSeed();
                else if ( nullObject )
                    return null;

                count = tmpCollection.size();

                int start = 0;

                if ( nullObject )
                    start = 1;

                Vec3 tr = new Vec3( 0, Ysize * ( 1 - inward ), 0 );
                msm = null;

                if ( mainObject.object instanceof TapObject )
                {
                    //we know better how to place the object

                    TapObject tapObj = (TapObject) mainObject.object;

                    if ( !center )
                        msm = tapObj.getPosition( 1 - inward, 0, false );
                    else
                        msm = tapObj.getPosition( 0.5, 0, false );
                }

                if ( start < count )
                {
                    for ( j = start; j < count; ++j )
                    {
                        m = Mat4.yrotation( gen.getDistribution( 0, randomYRotation * Math.PI / 180, TapRandomGenerator.UNIFORM ) );
                        anInfo = tmpCollection.elementAt( j ).objectInfo;
                        coords = anInfo.coords;

                        if ( randomYRotation > 0 )
                            coords.transformCoordinates( m );

                        if ( !center )
                        {
                            if ( msm != null )
                                coords.transformCoordinates( msm );
                            else
                                coords.setOrigin( coords.getOrigin().plus( tr ) );
                        }
                        else
                        {
                            bounds = anInfo.getBounds();
                            dum = bounds.getSize().y;

                            if ( msm != null )
                            {
                                coords.setOrigin( new Vec3( 0, -( bounds.maxy + bounds.miny ) / 2, 0 ) );
                                coords.transformCoordinates( msm );
                            }
                            else
                                coords.setOrigin( new Vec3( 0, Ycenter - ( bounds.maxy + bounds.miny ) / 2, 0 ) );
                        }
                    }

                    newCollection.mergeCollection( tmpCollection, 0 );
                }
                else
                    newCollection = null;
            }

            return newCollection;
        }
        else
            return null;
    }


    /**
     *  Gets the value attribute of the TopModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  var         Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The value value
     */
    @Override
    public double getValue( int outputPort, double[] var, long seed )
    {
        return (double) 0.0;
    }


    /**
     *  Description of the Method
     *
     *@param  modifiers  Description of the Parameter
     */
    @Override
    public void showPreviewFrame( int modifiers )
    {
        super.showPreviewFrame( modifiers );

        TapDesignerObjectCollection collection = null;
        procedure.initProcedure();

        if ( ( modifiers & ActionEvent.CTRL_MASK ) != 0 )
            collection = getObject( -2, procedure.getSeed() );
        else
            collection = getObject( -1, procedure.getSeed() );

        if ( collection != null )
            if ( collection.size() > 0 )
            {
                ObjectInfo newObjectInfo = new ObjectInfo( collection, new CoordinateSystem(), getName() );
                newObjectInfo.object.setTexture( procedure.getScene().getDefaultTexture(), procedure.getScene().getDefaultTexture().getDefaultMapping(newObjectInfo.object) );
                setupPreviewFrame( newObjectInfo );
            }
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public boolean acceptsMainEntry()
    {
        return false;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public boolean acceptsPreview()
    {
        return true;
    }


    /**
     *  Description of the Class
     *
     *@author     pims
     *@created    19 avril 2004
     */
    private class TopModuleDialog
             extends JFrame
             implements ActionListener,
            DocumentListener,
            ChangeListener
    {
        private JButton okButton;
        private JButton applyButton;
        private JButton cancelButton;
        private JCheckBox sizeChildrenCB;
        private JRadioButton inwardRB;
        private JRadioButton centerRB;
        ButtonGroup inwardBG;
        private double backProbability;
        private double backInward;
        private boolean modified;
        private boolean backSizeChildren;
        private boolean backCenter;
        private JTextField textField;
        private JTextField inwardTF;
        private JTextField randomYRotTF;
        JFrame parentFrame;
        NumberFormat format;
        private JSlider rSizeFactorSL;
        private JSlider ySizeFactorSL;
        private double backRSizeFactor;
        private double backYSizeFactor;
        private double backRandomYRotation;
        boolean setup;


        /**
         *  Constructor for the TopModuleDialog object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public TopModuleDialog( JFrame parentFrame )
        {
            backSizeChildren = sizeChildren;
            backRSizeFactor = rSizeFactor;
            backYSizeFactor = ySizeFactor;
            backProbability = probability;
            backCenter = center;
            backInward = inward;
            backRandomYRotation = randomYRotation;

            this.parentFrame = parentFrame;

            Container contentPane = this.getContentPane();
            contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS ) );
            setup = true;

            JPanel p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.LEFT ) );

            JLabel tmpLabel = TapDesignerTranslate.jlabel( "enterProbability" );
            p.add( tmpLabel );
            textField = new JTextField( String.valueOf( backProbability ) );
            textField.setColumns( 8 );
            p.add( textField );
            contentPane.add( p );
            textField.getDocument().addDocumentListener( this );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.LEFT ) );
            tmpLabel = TapDesignerTranslate.jlabel( "enterInward" );
            p.add( tmpLabel );
            inwardTF = new JTextField( String.valueOf( backInward ) );
            inwardTF.setColumns( 8 );
            p.add( inwardTF );
            inwardTF.getDocument().addDocumentListener( this );
            centerRB = TapDesignerTranslate.jRadioButton( "center", this );
            inwardRB = TapDesignerTranslate.jRadioButton( "inward", this );

            JPanel pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( inwardRB );
            pRB.add( centerRB );
            p.add( pRB );
            contentPane.add( p );

            inwardBG = new ButtonGroup();
            inwardBG.add( inwardRB );
            inwardBG.add( centerRB );

            if ( center )
            {
                centerRB.setSelected( true );
                inwardTF.setEnabled( false );
            }
            else
                inwardRB.setSelected( true );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.LEFT ) );
            p.add( TapDesignerTranslate.jlabel( "randomYRotation" ) );
            randomYRotTF = new JTextField( String.valueOf( randomYRotation ) );
            randomYRotTF.setColumns( 8 );
            p.add( randomYRotTF );
            randomYRotTF.getDocument().addDocumentListener( this );
            contentPane.add( p );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.LEFT ) );
            sizeChildrenCB = TapDesignerTranslate.jCheckBox( "sizeChildren", this );
            sizeChildrenCB.setSelected( sizeChildren );
            p.add( sizeChildrenCB );
            contentPane.add( p );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.LEFT ) );

            JPanel pg = new JPanel();
            pg.setLayout( new GridLayout( 2, 2 ) );
            pg.add( TapDesignerTranslate.jlabel( "sizeR" ) );
            pg.add( TapDesignerTranslate.jlabel( "sizeY" ) );
            pg.add( rSizeFactorSL = new JSlider( JSlider.HORIZONTAL, 0, 100, (int) ( rSizeFactor * 100 ) ) );
            rSizeFactorSL.setMajorTickSpacing( 100 );
            rSizeFactorSL.setMinorTickSpacing( 10 );
            rSizeFactorSL.setPaintTicks( true );

            Hashtable labelTable = new Hashtable();

            //Dimension dim = rSizeFactorSL.getPreferredSize();
            labelTable.put( new Integer( 0 ), tmpLabel = TapDesignerTranslate.jlabel( "none" ) );

            //dim.width += tmpLabel.getPreferredSize().width/2;
            labelTable.put( new Integer( 100 ), tmpLabel = TapDesignerTranslate.jlabel( "full" ) );

            //dim.width += tmpLabel.getPreferredSize().width/2;
            rSizeFactorSL.setLabelTable( labelTable );
            rSizeFactorSL.setPaintLabels( true );

            //dim.height = rSizeFactorSL.getPreferredSize().height;
            //rSizeFactorSL.setPreferredSize(dim);
            pg.add( ySizeFactorSL = new JSlider( JSlider.HORIZONTAL, 0, 100, (int) ( ySizeFactor * 100 ) ) );
            ySizeFactorSL.setMajorTickSpacing( 100 );
            ySizeFactorSL.setMinorTickSpacing( 10 );
            ySizeFactorSL.setPaintTicks( true );
            ySizeFactorSL.setLabelTable( labelTable );
            ySizeFactorSL.setPaintLabels( true );

            if ( !sizeChildrenCB.isSelected() )
            {
                rSizeFactorSL.setEnabled( false );
                ySizeFactorSL.setEnabled( false );
            }

            p.add( pg );

            //p.setAlignmentX(Component.RIGHT_ALIGNMENT);
            contentPane.add( p );
            okButton = TapDesignerTranslate.jButton( "ok", this );
            applyButton = TapDesignerTranslate.jButton( "apply", this );
            cancelButton = TapDesignerTranslate.jButton( "cancel", this );
            p = new JPanel();
            p.add( okButton );
            p.add( applyButton );
            p.add( cancelButton );

            contentPane.add( p );

            this.setTitle(TapBTranslate.text( "topModuleTitle", module.getName() ) );
            addWindowListener(
                new java.awt.event.WindowAdapter()
                {
                    @Override
                    public void windowClosing( java.awt.event.WindowEvent evt )
                    {
                        exitForm( evt );
                    }
                } );
            setResizable( false );
            setLocationRelativeTo( parentFrame );
            pack();
            setVisible( true );
            modified = false;
            format = NumberFormat.getInstance();
            format.setMaximumFractionDigits( 3 );
            setup = false;
            rSizeFactorSL.addChangeListener( this );
            ySizeFactorSL.addChangeListener( this );
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


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
        public void stateChanged( ChangeEvent e )
        {
            if ( setup )
                return;

            modified = true;

            if ( !centerRB.isSelected() )
                inwardTF.setEnabled( true );
            else
                inwardTF.setEnabled( false );

            if ( sizeChildrenCB.isSelected() )
            {
                rSizeFactorSL.setEnabled( true );
                ySizeFactorSL.setEnabled( true );
            }
            else
            {
                rSizeFactorSL.setEnabled( false );
                ySizeFactorSL.setEnabled( false );
            }
        }


        /**
         *  Description of the Method
         *
         *@param  evt  Description of the Parameter
         */
        @Override
        public void actionPerformed( java.awt.event.ActionEvent evt )
        {
            String command = evt.getActionCommand();

            if ( command.equals( cancelButton.getActionCommand() ) )
                doCancel();
            else if ( command.equals( applyButton.getActionCommand() ) )
            {
                getValues();
                doApply();
            }
            else if ( command.equals( okButton.getActionCommand() ) )
            {
                if ( modified )
                {
                    //cancel changes for undoRecord
                    getBackValues();
                    procedure.addUndoRecord();

                    //redo the changes
                    getValues();
                }

                doApply();
                editDialogClosed();
            }
        }


        /**
         *  Description of the Method
         */
        public void doLiveCheck()
        {
            modified = true;

            try
            {
                double dum = Double.parseDouble( textField.getText().trim().replace( ',', '.' ) );
                textField.setForeground( Color.black );
            }
            catch ( NumberFormatException e )
            {
                textField.setForeground( Color.red );
            }

            try
            {
                double dum = Double.parseDouble( inwardTF.getText().trim().replace( ',', '.' ) );
                textField.setForeground( Color.black );
            }
            catch ( NumberFormatException e )
            {
                textField.setForeground( Color.red );
            }
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
        public void changedUpdate( DocumentEvent e )
        {
            doLiveCheck();
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
        public void insertUpdate( DocumentEvent e )
        {
            doLiveCheck();
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
        public void removeUpdate( DocumentEvent e )
        {
            doLiveCheck();
        }


        /**
         *  Gets the valueString attribute of the TopModuleDialog object
         *
         *@return    The valueString value
         */
        private String getValueString()
        {
            return format.format( probability );
        }


        /**
         *  Gets the values attribute of the TopModuleDialog object
         */
        private void getValues()
        {
            try
            {
                probability = Double.parseDouble( textField.getText().trim().replace( ',', '.' ) );
                inward = Double.parseDouble( inwardTF.getText().trim().replace( ',', '.' ) );
                randomYRotation = Double.parseDouble( randomYRotTF.getText().trim().replace( ',', '.' ) );
                sizeChildren = sizeChildrenCB.isSelected();
                rSizeFactor = (double) rSizeFactorSL.getValue() / 100.0;
                ySizeFactor = (double) ySizeFactorSL.getValue() / 100.0;
                center = centerRB.isSelected();
            }
            catch ( NumberFormatException e )
            {
                getBackValues();
                randomYRotation = backRandomYRotation;
                JOptionPane.showMessageDialog(null, TapBTranslate.text( "nonValueMessage" ), TapBTranslate.text( "error" ), JOptionPane.ERROR_MESSAGE );
            }
        }


        /**
         *  Gets the backValues attribute of the TopModuleDialog object
         */
        private void getBackValues()
        {
            probability = backProbability;
            sizeChildren = backSizeChildren;
            rSizeFactor = backRSizeFactor;
            ySizeFactor = backYSizeFactor;
            center = backCenter;
            inward = backInward;
            randomYRotation = backRandomYRotation;
        }


        /**
         *  Description of the Method
         */
        private void doCancel()
        {
            if ( modified )
            {
                int r = JOptionPane.showConfirmDialog(this, TapBTranslate.text( "parametersModified" ), TapBTranslate.text( "warning" ), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION );

                if ( r == JOptionPane.YES_OPTION )
                    modified = false;

            }

            if ( !modified )
            {
                getBackValues();
                doApply();
                editDialogClosed();
            }
        }
    }
}

