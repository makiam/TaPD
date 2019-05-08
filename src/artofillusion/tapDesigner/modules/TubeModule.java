/*
 *  This class represents a tube module
 */
/*
 *  Copyright (C) 2003 by Francois Guillet
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
import buoy.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import artofillusion.tapDesigner.TapModule.*;

/**
 *  Description of the Class
 *
 *@author     Fran�ois Guillet
 *@created    19 avril 2004
 */
public class TubeModule
         extends ObjectModule
{
    private static TapModule.ModuleTypeInfo typeInfo;


    /**
     *  Constructor for the TubeModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public TubeModule( TapProcedure procedure, Point position )
    {
        super( procedure, TapDesignerTranslate.text( "tube" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "tubeName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/tube_tree.png" ) ) );

        int numYPoints = 3;
        int i;
        int j;
        Vec3[] v = new Vec3[numYPoints];
        double[] yThickness = new double[numYPoints];
        float[] ySmoothness = new float[numYPoints];

        for ( i = 0; i < numYPoints; ++i )
        {
            yThickness[i] = 1.0f;
            ySmoothness[i] = 1.0f;
            v[i] = new Vec3( 0, i * 1.0 / ( numYPoints - 1 ), 0 );
        }

        Curve yCurve = new Curve( v, ySmoothness, Mesh.APPROXIMATING, false );
        TapFunction rShape = new TapFunction();
        TapTube tube = new TapTube( yCurve, rShape, yThickness );
        tube.setTexture( procedure.getScene().getDefaultTexture(), procedure.getScene().getDefaultTexture().getDefaultMapping(tube) );
        setCurrentObject( new ObjectInfo( tube, new CoordinateSystem(), getName() ) );
    }


    /**
     *  Constructor for the TubeModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public TubeModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        setup();
    }


    /**
     *  Description of the Method
     *
     *@param  out              Description of the Parameter
     *@param  theScene         Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void writeToFile( DataOutputStream out, Scene theScene )
        throws IOException
    {
        super.writeToFile( out, theScene );
        out.writeShort( 0 );
    }


    /**
     *  Gets the moduleTypeInfo attribute of the TubeModule object
     *
     *@return    The moduleTypeInfo value
     */
    public ModuleTypeInfo getModuleTypeInfo()
    {
        return typeInfo;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public TapModule duplicate()
    {
        TubeModule module = new TubeModule( this.procedure, this.location );

        return duplicate( module );
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void edit( BFrame parentFrame )
    {
        super.edit( parentFrame );

        if ( isEditDialogOn )
            editBDialog.toFront();
        else
        {
            editBDialog = new EditWidgetDialogBase( parentFrame, this );
            isEditDialogOn = true;
        }
    }


    /*
     *  see TapTube for meaning
     */
    /**
     *  Description of the Method
     *
     *@param  size   Description of the Parameter
     *@param  sizeR  Description of the Parameter
     *@param  sizeY  Description of the Parameter
     *@param  info   Description of the Parameter
     */
    protected void resizeObject( Vec3 size, double sizeR, double sizeY, ObjectInfo info )
    {
        ( (TapTube) info.object ).setSize( size.x, size.y * sizeY, size.z, -sizeR, null );
    }


    /**
     *  Description of the Method
     *
     *@param  size   Description of the Parameter
     *@param  sizeR  Description of the Parameter
     *@param  sizeY  Description of the Parameter
     *@param  info   Description of the Parameter
     */
    public void sizeObject( Vec3 size, double sizeR, double sizeY, ObjectInfo info )
    {
        ( (TapTube) info.object ).setSize( size.x, size.y * sizeY, size.z, sizeR, null );
    }


    //{{{ edit frame methods and class
    /**
     *  Gets the number of edit frames used by the unary operation module
     *
     *@return    The number of edit frames to take into account
     */
    public int getNumEditWidgets()
    {
        return 1;
    }


    /**
     *  Gets the edit frame referenced by index
     *
     *@param  index       The reference to the edit frame
     *@param  cb          The Runnable called when validating modifications
     *@param  standalone  Whether the widget is in standalone frame or embedded
     *@return             The edit frame widget
     */
    public Widget getEditWidget( int index, Runnable cb, boolean standalone )
    {
        return new TubeEditWidget( cb, standalone, this );
    }


    /**
     *  Tube editor window
     *
     *@author     Francois Guillet
     *@created    06 june 2004
     */
    private class TubeEditWidget
             extends EditWidgetBase
    {
        private BCheckBox deliverCB;
        private BCheckBox hiddenCB;
        private BButton rShapeButton;
        private BButton yCurveButton;
        private BButton textureButton;
        private BButton materialButton;
        private BSplitPane sp;
        private ObjectInfo stackObject;


        /**
         *  Constructor for the TubeEditWidget object
         *
         *@param  cb          Runnable to call when updating.
         *@param  standalone  In a standalone window of in a properties split
         *      pane.
         *@param  module      The module that owns the edit widget.
         */
        public TubeEditWidget( Runnable cb, boolean standalone, TapModule module )
        {
            super( cb, standalone, "objectModuleTitle", module );

            backupObject = currentObject.duplicate( currentObject.object.duplicate() );

            textureButton = TapBTranslate.bButton( "setTexture", this, "doTexture" );
            materialButton = TapBTranslate.bButton( "setMaterial", this, "doMaterial" );
            rShapeButton = TapBTranslate.bButton( "rShape", this, "doRShape" );
            yCurveButton = TapBTranslate.bButton( "tubeShape", this, "doYCurve" );

            deliverCB = TapBTranslate.bCheckBox( "deliverDuplicates", deliverDuplicates );
            deliverCB.addEventLink( ValueChangedEvent.class, this, "doModified" );
            hiddenCB = TapBTranslate.bCheckBox( "hidden", !currentObject.visible );
            hiddenCB.addEventLink( ValueChangedEvent.class, this, "doModified" );

            BorderContainer content = new BorderContainer();
            ColumnContainer cc = new ColumnContainer();
            LayoutInfo layout = new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 3, 3, 3, 3 ), new Dimension( 0, 0 ) );
            LayoutInfo buttonLayout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets( 3, 3, 3, 3 ), new Dimension( 0, 0 ) );
            cc.setDefaultLayout( layout );
            cc.add( rShapeButton, buttonLayout );
            cc.add( yCurveButton, buttonLayout );
            cc.add( textureButton, buttonLayout );
            cc.add( materialButton, buttonLayout );
            cc.add( deliverCB );
            cc.add( hiddenCB );

            sp = new BSplitPane();
            sp.add( cc, 0 );
            sp.setResizeWeight( 0.0 );
            sp.setOneTouchExpandable( true );
            add( sp, BorderContainer.CENTER );

            updateObject();
        }


        /**
         *  Sets the texture of the object
         */
        private void doTexture()
        {
            ObjectInfo[] obj = new ObjectInfo[1];
            obj[0] = currentObject;
            new TaPDObjectTextureDialog( TapUtils.getParentBFrame( this ), procedure.getScene(), obj );
            doModified();
        }


        /**
         *  Sets the object material
         */
        private void doMaterial()
        {
            ObjectInfo[] obj = new ObjectInfo[1];
            obj[0] = currentObject;
            new TaPDObjectMaterialDialog( TapUtils.getParentBFrame( this ), procedure.getScene(), obj );
            doModified();
        }



        /**
         *  Description of the Method
         */
        protected void doModified()
        {
            super.doModified();
            currentSizedObject = null;
        }


        /**
         *  Description of the Method
         */
        private void doRShape()
        {
            ( (TapTube) currentObject.object ).getRShape().edit( (JFrame) TapUtils.getParentBFrame( this ).getComponent(), TapBTranslate.text( "rShape" ),
                new Runnable()
                {
                    public void run()
                    {
                        doRunnableUpdate();
                    }
                } );
        }


        /**
         *  Edits the object
         */
        private void doYCurve()
        {
            ObjectInfo yc = currentObject.duplicate();
            yc.name = module.getName() + ": " + TapDesignerTranslate.text( "tube" );
            ( (Tube) currentObject.object ).edit( procedure.getWindow(), yc,
                new Runnable()
                {
                    public void run()
                    {
                        doRunnableUpdate();
                    }
                } );
        }


        /**
         *  Description of the Method
         *
         *@param  force  Description of the Parameter
         */
        public void showValues( boolean force )
        {
            if ( force || changed )
            {
                updateObject();
                super.showValues( force );
            }
        }


        /**
         *  Description of the Method
         */
        private void updateObject()
        {

            ObjectPreviewCanvas opc;
            sp.add( opc = new ObjectPreviewCanvas( currentObject ), 1 );
            opc.setPreferredSize( new Dimension( 250, 250 ) );

            textureButton.setEnabled( currentObject.object.canSetTexture() );
            materialButton.setEnabled( currentObject.object.canSetMaterial() );
            deliverCB.setState( deliverDuplicates );
            hiddenCB.setState( !currentObject.visible );
        }


        /**
         *  Gets the undoValues attribute of the AoIObjectEditWidget object
         */
        protected void getUndoValues()
        {
            setCurrentObject( backupObject );
            deliverDuplicates = backDeliver;
            currentObject.visible = !backHidden;
            currentSizedObject = null;
        }


        /**
         *  Gets the backValues attribute of the AoIObjectEditWidget object
         */
        protected void getValues()
        {
            setCurrentObject( currentObject );
            deliverDuplicates = deliverCB.getState();
            currentObject.visible = !hiddenCB.getState();
            currentSizedObject = null;
        }


        /**
         *  Initializes backup values
         */
        protected void initBackValues()
        {
            backupObject = currentObject.duplicate( currentObject.object.duplicate() );
            backDeliver = deliverDuplicates;
            backHidden = !currentObject.visible;
        }


        /**
         *  Description of the Method
         */
        private void doRunnableUpdate()
        {
            doModified();
            ( (TapTube) currentObject.object ).setShape( null );
            ObjectInfo info = currentObject.duplicate();
            info.object = currentObject.object.duplicate();

            Vec3 size = info.object.getBounds().getSize();
            info.coords.setOrigin( new Vec3( 0, size.y / 2, 0 ) );

            ObjectPreviewCanvas opc;
            sp.add( opc = new ObjectPreviewCanvas( info ), 1 );
            opc.setPreferredSize( new Dimension( 250, 250 ) );

            repaint();
        }


        /**
         *  Description of the Method
         */
        public void pushValues()
        {
            stackObject = currentObject;
        }


        /**
         *  Description of the Method
         */
        public void popValues()
        {
            currentObject = stackObject;
        }

    }


    //}}}

    /**
     *  Description of the Class
     *
     *@author     pims
     *@created    19 avril 2004
     */
    private class OldTubeModuleDialog
             extends JFrame
             implements ActionListener,
            DocumentListener,
            ChangeListener
    {
        private JButton okButton;
        private JButton applyButton;
        private JButton cancelButton;
        private JButton rShapeButton;
        private JButton yCurveButton;
        private JButton textureButton;
        private JButton materialButton;
        private JPanel previewPanel;
        private ObjectInfo backupObject;
        private ObjectPreviewCanvas previewer;
        private JCheckBox deliverCB;
        private JCheckBox hiddenCB;
        private boolean deliverDupBackup;
        private boolean modified;


        /**
         *  Constructor for the TubeModuleDialog object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public OldTubeModuleDialog( JFrame parentFrame )
        {
            backupObject = currentObject.duplicate();
            backupObject.object = currentObject.object.duplicate();

            Container contentPane = this.getContentPane();
            GridBagConstraints c = new GridBagConstraints();
            GridBagLayout gridbag = new GridBagLayout();
            contentPane.setLayout( gridbag );
            rShapeButton = TapDesignerTranslate.jButton( "rShape", this );
            yCurveButton = TapDesignerTranslate.jButton( "tubeShape", this );
            textureButton = TapDesignerTranslate.jButton( "setTexture", this );
            materialButton = TapDesignerTranslate.jButton( "setMaterial", this );

            c.insets = new Insets( 2, 2, 2, 2 );
            c.gridwidth = 3;
            c.gridy = 0;
            c.gridx = 0;
            c.ipadx = 0;
            c.ipady = 0;
            c.fill = c.BOTH;
            c.anchor = GridBagConstraints.NORTH;
            c.weightx = c.weighty = 1.0;

            ObjectInfo info = currentObject.duplicate();
            info.object = info.object.duplicate();

            Vec3 size = info.object.getBounds().getSize();
            Vec3 tr = new Vec3( 0, -info.object.getBounds().miny - size.y / 2, 0 );
            info.coords.setOrigin( tr );
            previewer = new ObjectPreviewCanvas( info );
            //previewer.setPreferredSize( 160, 160 );
            //previewer.setSize( new Dimension( 160, 160 ) );
            previewPanel = new JPanel();
            previewPanel.setSize( new Dimension( 160, 160 ) );
            previewPanel.setPreferredSize( new Dimension( 160, 160 ) );
            previewPanel.setLayout( null );
            //previewPanel.add( previewer );
            JPanel p = new JPanel();
            p.add( previewPanel );
            contentPane.add( p, c );
            p.setLayout( new FlowLayout() );

            c.gridx = 0;
            c.gridwidth = 3;
            c.gridx = 0;
            c.gridy = 3;
            c.ipadx = 0;
            c.ipady = 0;
            c.fill = c.NONE;
            c.insets = new Insets( 2, 2, 2, 2 );

            p = new JPanel();
            contentPane.add( p, c );
            p.setLayout( new FlowLayout() );
            p.add( new JPanel() );
            p.add( rShapeButton );
            p.add( new JPanel() );
            p.add( yCurveButton );
            p.add( new JPanel() );

            /*
             *  c.gridx = 0;
             *  c.gridwidth = 3;
             *  c.gridx = 0;
             *  c.gridy = 4;
             *  c.ipadx = 0;
             *  c.ipady = 0;
             *  c.insets = new Insets(5,2,5,2);
             *  p = new JPanel();
             *  contentPane.add(p,c);
             *  p.setLayout(new FlowLayout());
             *  p.add(TapDesignerTranslate.jlabel("tubeSmoothness"));
             *  p.add(tubeSmoothnessTF = new JTextField(ensureStringLength(String.valueOf(
             *  ((TapTube)currentObject.object).getTubeSmoothness()))));
             *  p.add(new JPanel());
             *  p.add(TapDesignerTranslate.jlabel("curveSmoothness"));
             *  p.add(curveSmoothnessTF = new JTextField(ensureStringLength(String.valueOf(
             *  ((TapTube)currentObject.object).getCurveSmoothness()))));
             */
            c.gridx = 0;
            c.gridwidth = 3;
            c.gridx = 0;
            c.gridy = 5;
            c.ipadx = 0;
            c.ipady = 0;
            c.insets = new Insets( 2, 2, 2, 2 );
            p = new JPanel();
            contentPane.add( p, c );
            p.setLayout( new FlowLayout() );
            p.add( new JPanel() );
            p.add( textureButton );
            p.add( new JPanel() );
            p.add( materialButton );
            p.add( new JPanel() );

            c.anchor = GridBagConstraints.SOUTH;
            c.gridwidth = 1;
            c.gridy = 6;
            c.gridx = 1;
            p = new JPanel();
            contentPane.add( p, c );
            p.setLayout( new FlowLayout() );
            deliverCB = TapDesignerTranslate.jCheckBox( "deliverDuplicates", this );
            deliverCB.setSelected( deliverDuplicates );
            hiddenCB = TapDesignerTranslate.jCheckBox( "hidden", this );
            hiddenCB.setSelected( !currentObject.visible );
            deliverDupBackup = deliverDuplicates;
            p.add( deliverCB );
            p.add( hiddenCB );

            c.gridy = 7;
            c.gridx = 0;
            c.insets = new Insets( 2, 10, 5, 10 );
            okButton = TapDesignerTranslate.jButton( "ok", this );
            contentPane.add( okButton, c );

            c.gridx = 1;
            applyButton = TapDesignerTranslate.jButton( "apply", this );
            contentPane.add( applyButton, c );

            c.gridx = 2;
            cancelButton = TapDesignerTranslate.jButton( "cancel", this );
            contentPane.add( cancelButton, c );

            this.setTitle( TapDesignerTranslate.text( "tubeModuleTitle", module.getName() ) );

            addWindowListener(
                new java.awt.event.WindowAdapter()
                {
                    public void windowClosing( java.awt.event.WindowEvent evt )
                    {
                        exitForm( evt );
                    }
                } );
            pack();
            setLocationRelativeTo( parentFrame );
            setResizable( false );
            setVisible( true );
            modified = false;
        }


        /**
         *  Description of the Method
         *
         *@param  s  Description of the Parameter
         *@return    Description of the Return Value
         */
        private String ensureStringLength( String s )
        {
            if ( s.length() < 5 )
            {
                int j = s.length();

                for ( int i = 5; i > j; --i )
                    s = s + " ";
            }

            return s;
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
         */
        private void doRunnableUpdate()
        {
            /*
             *  float tubeSmoothness = Float.parseFloat(tubeSmoothnessTF.getText().trim().replace(',','.'));
             *  if (tubeSmoothness<0f) tubeSmoothness = 0.0f;
             *  else if (tubeSmoothness>1.0f) tubeSmoothness = 1.0f;
             *  float curveSmoothness = Float.parseFloat(curveSmoothnessTF.getText().trim().replace(',','.'));
             *  if (curveSmoothness<0f) curveSmoothness = 0.0f;
             *  else if (curveSmoothness>1.0f) curveSmoothness = 1.0f;
             *  ((TapTube)currentObject.object).setTubeSmoothness(tubeSmoothness);
             */
            modified = true;
            ( (TapTube) currentObject.object ).setShape( null );

            ObjectInfo info = currentObject.duplicate();
            info.object = currentObject.object.duplicate();

            Vec3 size = info.object.getBounds().getSize();
            info.coords.setOrigin( new Vec3( 0, size.y / 2, 0 ) );
            //previewPanel.remove( previewer );
            previewer = new ObjectPreviewCanvas( info );
            //previewer.setPreferredSize( 160, 160 );
            //previewer.setSize( new Dimension( 160, 160 ) );
            //previewPanel.add( previewer );
            currentSizedObject = null;
            doApply();
            repaint();
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


        /**
         *  Description of the Method
         *
         *@param  evt  Description of the Parameter
         */
        public void actionPerformed( java.awt.event.ActionEvent evt )
        {
            String command = evt.getActionCommand();

            if ( command.equals( textureButton.getActionCommand() ) )
            {
                modified = true;
                ObjectInfo[] obj = new ObjectInfo[1];
                obj[0] = currentObject;
                //new ObjectTextureDialog( this, procedure.getScene(), obj );
                currentSizedObject = null;
            }
            else if ( command.equals( materialButton.getActionCommand() ) )
            {
                modified = true;
                ObjectInfo[] obj = new ObjectInfo[1];
                obj[0] = currentObject;
                //new ObjectMaterialDialog( this, procedure.getScene(), obj );
                currentSizedObject = null;
            }
            else if ( command.equals( cancelButton.getActionCommand() ) )
                doCancel();
            else if ( command.equals( applyButton.getActionCommand() ) )
            {
                doRunnableUpdate();
                deliverDuplicates = deliverCB.isSelected();
                currentObject.visible = !hiddenCB.isSelected();
                currentSizedObject = null;
                doApply();
            }
            else if ( command.equals( okButton.getActionCommand() ) )
            {
                if ( modified )
                {
                    ObjectInfo info = currentObject;
                    setCurrentObject( backupObject );
                    deliverDuplicates = deliverDupBackup;
                    procedure.addUndoRecord();
                    setCurrentObject( info );
                }
                deliverDuplicates = deliverCB.isSelected();
                currentObject.visible = !hiddenCB.isSelected();
                currentSizedObject = null;
                doRunnableUpdate();
                doApply();
                editDialogClosed();
            }
            else if ( command.equals( rShapeButton.getActionCommand() ) )
                ( (TapTube) currentObject.object ).getRShape().edit( this, TapDesignerTranslate.text( "rShape" ),
                    new Runnable()
                    {
                        public void run()
                        {
                            doRunnableUpdate();
                        }
                    } );
            else if ( command.equals( yCurveButton.getActionCommand() ) )
            {
                ObjectInfo yc = currentObject.duplicate();
                yc.name = module.getName() + ": " + TapDesignerTranslate.text( "tube" );
                ( (Tube) currentObject.object ).edit( procedure.getWindow(), yc,
                    new Runnable()
                    {
                        public void run()
                        {
                            doRunnableUpdate();
                        }
                    } );
            }
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void doLiveCheck( DocumentEvent e )
        {
            modified = true;

            Document sourceDoc = e.getDocument();
            JTextField source = null;
            /*
             *  if (sourceDoc == tubeSmoothnessTF.getDocument()) source = tubeSmoothnessTF;
             *  /else if (sourceDoc == curveSmoothnessTF.getDocument()) source = curveSmoothnessTF;
             *  if ((source==tubeSmoothnessTF) || (source==curveSmoothnessTF))
             *  {    try
             *  {    float dum = Float.parseFloat(source.getText().trim().replace(',','.'));
             *  if ((dum<0) || (dum>1))
             *  source.setForeground(Color.red);
             *  else source.setForeground(Color.black);
             *  }
             *  catch (NumberFormatException ex)
             *  {    source.setForeground(Color.red);
             *  }
             *  }
             */
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void changedUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void insertUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void removeUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Description of the Method
         */
        private void doCancel()
        {
            if ( modified )
            {
                int r = JOptionPane.showConfirmDialog( this, TapDesignerTranslate.text( "parametersModified" ), TapDesignerTranslate.text( "warning" ), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION );

                if ( r == JOptionPane.YES_OPTION )
                    modified = false;

            }

            if ( !modified )
            {
                setCurrentObject( backupObject );
                deliverDuplicates = deliverDupBackup;
                currentSizedObject = null;
                editDialogClosed();
            }
        }
    }
}

