/*
 *  This class represents a spline mesh module
 */
/*
 *  Copyright (C) 2003 by François Guillet
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
import artofillusion.ui.*;

import buoy.widget.*;
import buoy.event.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import artofillusion.tapDesigner.TapModule.*;

/**
 *  Description of the Class
 *
 *@author     pims
 *@created    19 avril 2004
 */
public class SplineModule extends ObjectModule
{
    private int numCurvePoints;

    private static TapModule.ModuleTypeInfo typeInfo;


    /**
     *  Constructor for the SplineModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public SplineModule( TapProcedure procedure, Point position )
    {
        super(procedure, TapBTranslate.text( "spline" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "splineName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/spline_tree.png" ) ) );

        int i;

        numCurvePoints = 6;

        int numYPoints = 8;
        Vec3[] v = new Vec3[numYPoints];
        float[] ySmoothness = new float[numYPoints];

        for ( i = 0; i < numYPoints; ++i )
        {
            ySmoothness[i] = 1.0f;
            v[i] = new Vec3( 0, i * 1.0 / ( numYPoints - 1 ), 0 );
        }

        Vector crossSections = calcCrossSections( numCurvePoints, numYPoints, 1.0f );
        Curve yCurve = new Curve( v, ySmoothness, Mesh.APPROXIMATING, false );
        TapFunction rShape = new TapFunction();
        TapSplineMesh mesh = new TapSplineMesh( crossSections, yCurve, rShape, 1.0f, 1.0f );
        mesh.setTexture( procedure.getScene().getDefaultTexture(), procedure.getScene().getDefaultTexture().getDefaultMapping(mesh) );
        setCurrentObject( new ObjectInfo( mesh, new CoordinateSystem(), getName() ) );
    }


    /**
     *  Description of the Method
     *
     *@param  numCurvePoints  Description of the Parameter
     *@param  numYPoints      Description of the Parameter
     *@param  smooth          Description of the Parameter
     *@return                 Description of the Return Value
     */
    private Vector calcCrossSections( int numCurvePoints, int numYPoints, float smooth )
    {
        Vector crossSections = new Vector();
        float[] curveSmoothness = new float[numCurvePoints];
        Vec3[] cv = new Vec3[numCurvePoints];

        for ( int j = 0; j < numCurvePoints; ++j )
            curveSmoothness[j] = smooth;

        for ( int i = 0; i < numYPoints; ++i )
        {
            for ( int j = 0; j < numCurvePoints; ++j )
            {
                double x;
                double z;
                x = 0.5 * Math.cos( ( j + 0.5 ) * 2.0 * Math.PI / numCurvePoints );
                z = -0.5 * Math.sin( ( j + 0.5 ) * 2.0 * Math.PI / numCurvePoints );
                cv[j] = new Vec3( x, 0, z );
            }

            Curve curve = new Curve( cv, curveSmoothness, Mesh.APPROXIMATING, true );
            crossSections.add( curve );
        }

        return crossSections;
    }


    /**
     *  Constructor for the SplineModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public SplineModule( DataInputStream in, Scene theScene ) throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        numCurvePoints = in.readInt();
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
    public void writeToFile( DataOutputStream out, Scene theScene ) throws IOException
    {
        super.writeToFile( out, theScene );
        out.writeShort( 0 );
        out.writeInt( numCurvePoints );
    }


    /**
     *  Gets the moduleTypeInfo attribute of the SplineModule object
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
        SplineModule module = new SplineModule( this.procedure, this.location );

        return duplicate( module );
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    @Override
    public void edit( BFrame parentFrame )
    {

        if ( isEditDialogOn )
            editBDialog.toFront();
        else
        {
            editBDialog = new EditWidgetDialogBase( parentFrame, this );
            isEditDialogOn = true;
        }
    }


    /**
     *  Description of the Method
     *
     *@param  size   Description of the Parameter
     *@param  sizeR  Description of the Parameter
     *@param  sizeY  Description of the Parameter
     *@param  info   Description of the Parameter
     */
    @Override
    protected void resizeObject( Vec3 size, double sizeR, double sizeY, ObjectInfo info )
    {
        ( (TapSplineMesh) info.object ).setSize( size.x * sizeR, size.y * sizeY, size.z * sizeR, null );
    }


    /**
     *  Description of the Method
     *
     *@param  size   Description of the Parameter
     *@param  sizeR  Description of the Parameter
     *@param  sizeY  Description of the Parameter
     *@param  info   Description of the Parameter
     */
    @Override
    protected void sizeObject( Vec3 size, double sizeR, double sizeY, ObjectInfo info )
    {
        ( (TapSplineMesh) info.object ).setSize( size.x * sizeR, size.y * sizeY, size.z * sizeR, null );
    }


    //{{{ edit frame methods and class
    /**
     *  Gets the number of edit frames used by the unary operation module
     *
     *@return    The number of edit frames to take into account
     */
    @Override
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
    @Override
    public Widget getEditWidget( int index, Runnable cb, boolean standalone )
    {
        return new SplineEditWidget( cb, standalone, this );
    }


    /**
     *  Spline object editor window
     *
     *@author     François Guillet
     *@created    06 june 2004
     */
    private class SplineEditWidget extends EditWidgetBase
    {
        private BCheckBox deliverCB;
        private BCheckBox hiddenCB;
        private BButton rShapeButton;
        private BButton yCurveButton;
        private BButton textureButton;
        private BButton curveEditButton;
        private BButton curveDuplicateButton;
        private BButton curveNumPointsButton;
        private BSpinner numPoints;
        private BSpinner curve;
        private BSplitPane sp;
        private BLabel outOfLabel;
        private ValueField uSmoothnessVF;
        private ValueField vSmoothnessVF;
        private ObjectInfo stackObject;


        /**
         *  Constructor for the TubeEditWidget object
         *
         *@param  cb          Runnable to call when updating.
         *@param  standalone  In a standalone window of in a properties split
         *      pane.
         *@param  module      The module that owns the edit widget.
         */
        public SplineEditWidget( Runnable cb, boolean standalone, TapModule module )
        {
            super( cb, standalone, "objectModuleTitle", module );

            backupObject = currentObject.duplicate( currentObject.object.duplicate() );

            textureButton = TapBTranslate.bButton( "setTexture", this, "doTexture" );
            rShapeButton = TapBTranslate.bButton( "rShape", this, "doRShape" );
            yCurveButton = TapBTranslate.bButton( "yCurve", this, "doYCurve" );
            curveEditButton = TapBTranslate.bButton( "edit", this, "doCurveEdit" );
            curveDuplicateButton = TapBTranslate.bButton( "duplicate", this, "doCurveDuplicate" );
            curveNumPointsButton = TapBTranslate.bButton( "curveNumPoints", this, "doCurveNumPoints" );

            deliverCB = TapBTranslate.bCheckBox( "deliverDuplicates", deliverDuplicates );
            deliverCB.addEventLink( ValueChangedEvent.class, this, "doModified" );
            hiddenCB = TapBTranslate.bCheckBox( "hidden", !currentObject.visible );
            hiddenCB.addEventLink( ValueChangedEvent.class, this, "doModified" );

            
            ColumnContainer cc = new ColumnContainer();
            LayoutInfo layout = new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 3, 3, 3, 3 ), new Dimension( 0, 0 ) );
            LayoutInfo buttonLayout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets( 3, 3, 3, 3 ), new Dimension( 0, 0 ) );
            cc.setDefaultLayout( layout );
            ColumnContainer subcc = new ColumnContainer();
            subcc.setDefaultLayout( new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 3, 3, 3, 3 ), new Dimension( 0, 0 ) ) );
            subcc.add( curveEditButton, buttonLayout );
            subcc.add( TapBTranslate.bLabel( "curveNumber" ) );
            RowContainer rc = new RowContainer();
            rc.setDefaultLayout( new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 1, 1, 1, 1 ), new Dimension( 0, 0 ) ) );
            curve = new BSpinner( 1, 1, 1000, 1 );
            rc.add( curve );
            int numCurvePoints = ( (TapSplineMesh) currentObject.object ).getVVSize();
            rc.add( outOfLabel = new BLabel( "/" + numCurvePoints ) );
            subcc.add( rc );
            subcc.add( curveDuplicateButton, buttonLayout );

            LayoutInfo numPointsLayout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 5, 1, 1, 1 ), new Dimension( 0, 0 ) );
            subcc.add( curveNumPointsButton, numPointsLayout );
            numPoints = new BSpinner( ( (TapSplineMesh) currentObject.object ).getUUSize(), 3, 1000, 1 );
            subcc.add( numPoints, numPointsLayout );

            
            cc.add(new BOutline( subcc, BorderFactory.createTitledBorder( BorderFactory.createRaisedBevelBorder(), TapBTranslate.text( "curvesShape" ) ) ), new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 1, 1, 1, 1 ), new Dimension( 0, 0 ) ) );

            GridContainer subgc = new GridContainer( 2, 2 );
            subgc.setDefaultLayout( new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 2, 2, 2, 2 ), new Dimension( 0, 0 ) ) );
            subgc.add( TapBTranslate.bLabel( "section" ), 0, 0 );
            subgc.add( uSmoothnessVF = new ValueField( ( (TapSplineMesh) currentObject.object ).getSplineMeshUSmoothness(), ValueField.NONNEGATIVE ), 0, 1 );
            subgc.add( TapBTranslate.bLabel( "y" ), 1, 0 );
            subgc.add( vSmoothnessVF = new ValueField( ( (TapSplineMesh) currentObject.object ).getSplineMeshVSmoothness(), ValueField.NONNEGATIVE ), 1, 1 );
            cc.add(new BOutline( subgc, BorderFactory.createTitledBorder( BorderFactory.createRaisedBevelBorder(), TapBTranslate.text( "smoothness" ) ) ), new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 1, 1, 1, 1 ), new Dimension( 0, 0 ) ) );

            uSmoothnessVF.addEventLink( ValueChangedEvent.class, this, "doValueFieldChanged" );
            vSmoothnessVF.addEventLink( ValueChangedEvent.class, this, "doValueFieldChanged" );

            cc.add( rShapeButton, buttonLayout );
            cc.add( yCurveButton, buttonLayout );
            cc.add( textureButton, buttonLayout );
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
        @SuppressWarnings("ResultOfObjectAllocationIgnored")
        private void doTexture()
        {
            ObjectInfo[] obj = new ObjectInfo[1];
            obj[0] = currentObject;
            new TaPDObjectTextureDialog( procedure.getWindow(), procedure.getScene(), obj );
            doModified();
        }


        /**
         *  Sets the object material
         */
        @SuppressWarnings("ResultOfObjectAllocationIgnored")
        private void doMaterial()
        {
            ObjectInfo[] obj = new ObjectInfo[1];
            obj[0] = currentObject;
            new TaPDObjectMaterialDialog( procedure.getWindow(), procedure.getScene(), obj );
            doModified();
        }



        /**
         *  Description of the Method
         */
        @Override
        protected void doModified()
        {
            super.doModified();
            currentSizedObject = null;
        }


        /**
         *  Description of the Method
         */
        private void doCurveEdit()
        {
            int dum = ( (Integer) curve.getValue() ).intValue();

            if ( ( dum > 0 ) && ( dum <= ( (Curve) ( (TapSplineMesh) currentObject.object ).getYCurve() ).getVertices().length ) )
            {
                Curve csCurve = (Curve) ( (TapSplineMesh) currentObject.object ).getCrossSections()
                        .elementAt( dum - 1 );
                ObjectInfo csc = currentObject.duplicate();
                csc.object = csCurve;
                csc.name = module.getName() + ": " + TapBTranslate.text( "crossSectionCurve" ) + dum;
                csCurve.edit( procedure.getWindow(), csc,
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            doRunnableUpdate();
                        }
                    } );
            }
        }


        /**
         *  Description of the Method
         */
        private void doCurveDuplicate()
        {
            int dum = ( (Integer) curve.getValue() ).intValue();

            int yCurveLength = ( (Curve) ( (TapSplineMesh) currentObject.object ).getYCurve() ).getVertices().length;
            if ( ( dum > 0 ) && ( dum <= yCurveLength ) )
            {
                Curve csCurve = (Curve) ( (TapSplineMesh) currentObject.object ).getCrossSections()
                        .elementAt( dum - 1 );
                for ( int i = 0; i < yCurveLength; ++i )
                {
                    if ( i != dum - 1 )
                        ( (TapSplineMesh) currentObject.object ).getCrossSections().setElementAt( csCurve.duplicate(), i );

                }
                doRunnableUpdate();
            }
        }


        /**
         *  Description of the Method
         */
        private void doCurveNumPoints()
        {
            int dum = ( (Integer) numPoints.getValue() ).intValue();

            if ( dum > 2 )
            {
                ( (TapSplineMesh) currentObject.object ).setCrossSections( calcCrossSections( dum, ( (TapSplineMesh) currentObject.object ).getVVSize(), ( (TapSplineMesh) currentObject.object ).getSplineMeshUSmoothness() ) );
                doRunnableUpdate();
            }
        }


        /**
         *  Description of the Method
         */
        private void doRShape()
        {
            ( (TapSplineMesh) currentObject.object ).getRShape().edit( (JFrame) TapUtils.getParentBFrame( this ).getComponent(), TapBTranslate.text( "rShape" ),
                new Runnable()
                {
                    @Override
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
            yc.object = ( (TapSplineMesh) currentObject.object ).getYCurve();
            yc.name = module.getName() + ": " + TapBTranslate.text( "yPath" );
            ( (TapSplineMesh) currentObject.object ).getYCurve().edit( procedure.getWindow(), yc,
                new Runnable()
                {
                    @Override
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
        @Override
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
            deliverCB.setState( deliverDuplicates );
            hiddenCB.setState( !currentObject.visible );
        }


        /**
         *  Gets the undoValues attribute of the AoIObjectEditWidget object
         */
        @Override
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
        @Override
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
        @Override
        protected void initBackValues()
        {
            backupObject = currentObject.duplicate( currentObject.object.duplicate() );
            backDeliver = deliverDuplicates;
            backHidden = !currentObject.visible;
        }


        /**
         *  Description of the Method
         */
        private void doValueFieldChanged()
        {
            doRunnableUpdate();
        }


        /**
         *  Description of the Method
         */
        private void doRunnableUpdate()
        {
            doModified();

            Curve YCurve = ( (TapSplineMesh) currentObject.object ).getYCurve();
            int numYCurvePoints = YCurve.getVertices().length;
            Vector crossSections = ( (TapSplineMesh) currentObject.object ).getCrossSections();
            int diff = crossSections.size() - numYCurvePoints;

            if ( diff > 0 )
                crossSections.setSize( numYCurvePoints );
            else if ( diff < 0 )
            {
                int k = crossSections.size() - 1;
                Curve curve = (Curve) crossSections.lastElement();

                for ( int i = 0; i < -diff; ++i )
                    crossSections.add( curve.duplicate() );
            }

            float uSmoothness = new Float( uSmoothnessVF.getValue() ).floatValue();
            float vSmoothness = new Float( vSmoothnessVF.getValue() ).floatValue();

            if ( uSmoothness < 0f )
                uSmoothness = 0.0f;
            else if ( uSmoothness > 1.0f )
                uSmoothness = 1.0f;

            if ( vSmoothness < 0f )
                uSmoothness = 0.0f;
            else if ( vSmoothness > 1.0f )
                uSmoothness = 1.0f;

            for ( int i = 0; i < crossSections.size(); ++i )
            {
                Vec3[] vert = ( (Curve) crossSections.elementAt( i ) ).getVertexPositions();
                float[] smooth = new float[vert.length];

                for ( int j = 0; j < vert.length; ++j )
                    smooth[j] = uSmoothness;

                ( (Curve) crossSections.elementAt( i ) ).setShape( vert, smooth );
            }

            Vec3[] vert = YCurve.getVertexPositions();
            float[] smooth = new float[vert.length];

            for ( int j = 0; j < vert.length; ++j )
                smooth[j] = vSmoothness;

            YCurve.setShape( vert, smooth );
            ( (TapSplineMesh) currentObject.object ).setUVSmoothness( uSmoothness, vSmoothness );
            ( (TapSplineMesh) currentObject.object ).updateMesh( null );

            ObjectInfo info = currentObject.duplicate();
            Vec3 size = info.object.getBounds().getSize();
            info.coords.setOrigin( new Vec3( 0, size.y / 2, 0 ) );

            int numPoints = ( (TapSplineMesh) currentObject.object ).getVVSize();
            outOfLabel.setText( "/" + numPoints );

            ObjectPreviewCanvas opc;
            sp.add( opc = new ObjectPreviewCanvas( info ), 1 );
            opc.setPreferredSize( new Dimension( 250, 250 ) );

            repaint();
        }


        /**
         *  Description of the Method
         */
        @Override
        public void pushValues()
        {
            stackObject = currentObject;
        }


        /**
         *  Description of the Method
         */
        @Override
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
    private class SplineModuleDialog
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
        private JButton curveEditButton;
        private JButton curveDuplicateButton;
        private JButton textureButton;
        private JButton materialButton;
        private JButton curveNumPointsButton;
        private JPanel previewPanel;
        private JLabel outOfLabel;
        private JTextField curveNumTF;
        private JTextField curveNumPointsTF;
        private JTextField uSmoothnessTF;
        private JTextField vSmoothnessTF;
        private ObjectInfo backupObject;
        private ObjectPreviewCanvas previewer;
        private JCheckBox deliverCB;
        private JCheckBox hiddenCB;
        private boolean deliverDupBackup;
        private boolean modified;


        /**
         *  Constructor for the SplineModuleDialog object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public SplineModuleDialog( JFrame parentFrame )
        {
            backupObject = currentObject.duplicate();
            backupObject.object = currentObject.object.duplicate();

            Container contentPane = this.getContentPane();
            GridBagConstraints c = new GridBagConstraints();
            GridBagLayout gridbag = new GridBagLayout();
            contentPane.setLayout( gridbag );
            rShapeButton = TapDesignerTranslate.jButton( "rShape", this );
            yCurveButton = TapDesignerTranslate.jButton( "yCurve", this );
            textureButton = TapDesignerTranslate.jButton( "setTexture", this );
            materialButton = TapDesignerTranslate.jButton( "setMaterial", this );
            materialButton.setEnabled( false );
            curveEditButton = TapDesignerTranslate.jButton( "edit", this );
            curveDuplicateButton = TapDesignerTranslate.jButton( "duplicate", this );
            curveNumPointsButton = TapDesignerTranslate.jButton( "curveNumPoints", this );

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
            Vec3 tr = new Vec3( 0, -size.y / 2, 0 );
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

            c.gridwidth = 3;
            c.gridx = 0;
            c.gridy = 1;
            c.ipadx = 0;
            c.ipady = 0;
            c.fill = c.NONE;
            c.insets = new Insets( 2, 2, 2, 2 );

            p = new JPanel();
            contentPane.add( p, c );
            p.setLayout( new FlowLayout() );
            p.add( new JPanel() );
            p.add( curveEditButton );
            p.add( new JPanel() );
            p.add( TapDesignerTranslate.jlabel( "curveNumber" ) );
            p.add( new JPanel() );

            int numPoints = ( (TapSplineMesh) currentObject.object ).getVVSize();
            p.add( curveNumTF = new JTextField( "1" ) );
            curveNumTF.setColumns( 4 );
            p.add( outOfLabel = new JLabel( "/" + numPoints ) );
            p.add( new JPanel() );
            p.add( curveDuplicateButton );
            p.add( new JPanel() );

            c.gridx = 0;
            c.gridwidth = 3;
            c.gridx = 0;
            c.gridy = 2;
            c.ipadx = 0;
            c.ipady = 0;
            c.fill = c.NONE;
            c.insets = new Insets( 2, 2, 2, 2 );
            p = new JPanel();
            contentPane.add( p, c );
            p.setLayout( new FlowLayout() );
            p.add( new JPanel() );
            p.add( curveNumPointsButton );
            p.add( new JPanel() );
            numPoints = ( (TapSplineMesh) currentObject.object ).getUUSize();
            p.add( curveNumPointsTF = new JTextField( String.valueOf( numPoints ) ) );
            curveNumPointsTF.setColumns( 4 );
            p.add( new JPanel() );

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

            c.gridx = 0;
            c.gridwidth = 3;
            c.gridx = 0;
            c.gridy = 4;
            c.ipadx = 0;
            c.ipady = 0;
            c.insets = new Insets( 2, 2, 2, 2 );
            p = new JPanel();
            contentPane.add( p, c );
            p.setLayout( new FlowLayout() );
            p.add( TapDesignerTranslate.jlabel( "uSmoothness" ) );
            p.add( uSmoothnessTF = new JTextField( String.valueOf( ( (TapSplineMesh) currentObject.object ).getSplineMeshUSmoothness() ) ) );
            uSmoothnessTF.setColumns( 4 );
            p.add( new JPanel() );
            p.add( TapDesignerTranslate.jlabel( "vSmoothness" ) );
            p.add( vSmoothnessTF = new JTextField( String.valueOf( ( (TapSplineMesh) currentObject.object ).getSplineMeshVSmoothness() ) ) );
            vSmoothnessTF.setColumns( 4 );

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
            c.gridwidth = 3;
            c.gridy = 6;
            c.gridx = 0;
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

            c.gridwidth = 1;
            c.gridy = 7;
            c.gridx = 0;
            c.insets = new Insets( 2, 2, 5, 2 );
            okButton = TapDesignerTranslate.jButton( "ok", this );
            contentPane.add( okButton, c );

            c.gridx = 1;
            applyButton = TapDesignerTranslate.jButton( "apply", this );
            contentPane.add( applyButton, c );

            c.gridx = 2;
            cancelButton = TapDesignerTranslate.jButton( "cancel", this );
            contentPane.add( cancelButton, c );

            this.setTitle(TapBTranslate.text( "splineModuleTitle", module.getName() ) );

            addWindowListener(
                new java.awt.event.WindowAdapter()
                {
                    @Override
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
            modified = true;

            Curve YCurve = ( (TapSplineMesh) currentObject.object ).getYCurve();
            int numYCurvePoints = YCurve.getVertices().length;
            Vector crossSections = ( (TapSplineMesh) currentObject.object ).getCrossSections();
            int diff = crossSections.size() - numYCurvePoints;

            if ( diff > 0 )
                crossSections.setSize( numYCurvePoints );
            else if ( diff < 0 )
            {
                int k = crossSections.size() - 1;
                Curve curve = (Curve) crossSections.lastElement();

                for ( int i = 0; i < -diff; ++i )
                    crossSections.add( curve.duplicate() );
            }

            float uSmoothness = Float.parseFloat( uSmoothnessTF.getText().trim().replace( ',', '.' ) );
            float vSmoothness = Float.parseFloat( vSmoothnessTF.getText().trim().replace( ',', '.' ) );

            if ( uSmoothness < 0f )
                uSmoothness = 0.0f;
            else if ( uSmoothness > 1.0f )
                uSmoothness = 1.0f;

            if ( vSmoothness < 0f )
                uSmoothness = 0.0f;
            else if ( vSmoothness > 1.0f )
                uSmoothness = 1.0f;

            for ( int i = 0; i < crossSections.size(); ++i )
            {
                Vec3[] vert = ( (Curve) crossSections.elementAt( i ) ).getVertexPositions();
                float[] smooth = new float[vert.length];

                for ( int j = 0; j < vert.length; ++j )
                    smooth[j] = uSmoothness;

                ( (Curve) crossSections.elementAt( i ) ).setShape( vert, smooth );
            }

            Vec3[] vert = YCurve.getVertexPositions();
            float[] smooth = new float[vert.length];

            for ( int j = 0; j < vert.length; ++j )
                smooth[j] = vSmoothness;

            YCurve.setShape( vert, smooth );
            ( (TapSplineMesh) currentObject.object ).setUVSmoothness( uSmoothness, vSmoothness );
            ( (TapSplineMesh) currentObject.object ).updateMesh( null );

            ObjectInfo info = currentObject.duplicate();
            Vec3 size = info.object.getBounds().getSize();
            info.coords.setOrigin( new Vec3( 0, size.y / 2, 0 ) );
            //previewPanel.remove( previewer );
            //previewer = new ObjectPreviewCanvas( info );
            //previewer.setPreferredSize( 160, 160 );
            //previewer.setSize( new Dimension( 160, 160 ) );
            //previewPanel.add( previewer );

            int numPoints = ( (TapSplineMesh) currentObject.object ).getVVSize();
            outOfLabel.setText( "/" + numPoints );
            currentSizedObject = null;
            doApply();
            repaint();
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
        public void stateChanged( ChangeEvent e )
        {
            modified = true;
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
            else if ( command.equals( curveEditButton.getActionCommand() ) )
            {
                int dum = ( Integer.valueOf( curveNumTF.getText().trim() ) );

                if ( ( dum > 0 ) && ( dum <= ( (Curve) ( (TapSplineMesh) currentObject.object ).getYCurve() ).getVertices().length ) )
                {
                    Curve csCurve = (Curve) ( (TapSplineMesh) currentObject.object ).getCrossSections().get( dum - 1 );
                    ObjectInfo csc = currentObject.duplicate();
                    csc.object = csCurve;
                    csc.name = module.getName() + ": " + TapBTranslate.text( "crossSectionCurve" ) + dum;
                    csCurve.edit(procedure.getWindow(), csc,
                        new Runnable()
                        {
                        @Override
                            public void run()
                            {
                                doRunnableUpdate();
                            }
                        } );
                }
            }
            else if ( command.equals( curveDuplicateButton.getActionCommand() ) )
            {
                int dum = ( Integer.valueOf( curveNumTF.getText().trim() ) );
                int yCurveLength = ( (Curve) ( (TapSplineMesh) currentObject.object ).getYCurve() ).getVertices().length;
                if ( ( dum > 0 ) && ( dum <= yCurveLength ) )
                {
                    Curve csCurve = (Curve) ( (TapSplineMesh) currentObject.object ).getCrossSections().get( dum - 1 );
                    for ( int i = 0; i < yCurveLength; ++i )
                    {
                        if ( i != dum - 1 )
                            ( (TapSplineMesh) currentObject.object ).getCrossSections().set(i, (Curve)csCurve.duplicate() );

                    }
                    doRunnableUpdate();
                }
            }
            else if ( command.equals( curveNumPointsButton.getActionCommand() ) )
            {
                int dum = ( Integer.valueOf( curveNumPointsTF.getText().trim() ) );

                if ( dum > 2 )
                {
                    ( (TapSplineMesh) currentObject.object ).setCrossSections( calcCrossSections( dum, ( (TapSplineMesh) currentObject.object ).getVVSize(), ( (TapSplineMesh) currentObject.object ).getSplineMeshUSmoothness() ) );
                    doRunnableUpdate();
                }
            }
            else if ( command.equals( cancelButton.getActionCommand() ) )
                doCancel();
            else if ( command.equals( applyButton.getActionCommand() ) )
            {
                //currentObject = dialogCurrentObject;
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
                ( (TapSplineMesh) currentObject.object ).updateMesh( null );
                deliverDuplicates = deliverCB.isSelected();
                currentObject.visible = !hiddenCB.isSelected();
                currentSizedObject = null;
                doRunnableUpdate();
                doApply();
                editDialogClosed();
            }
            else if ( command.equals( rShapeButton.getActionCommand() ) )
                ( (TapSplineMesh) currentObject.object ).getRShape().edit(this, TapBTranslate.text( "rShape" ),
                    new Runnable()
                    {
                @Override
                        public void run()
                        {
                            doRunnableUpdate();
                        }
                    } );
            else if ( command.equals( yCurveButton.getActionCommand() ) )
            {
                ObjectInfo yc = currentObject.duplicate();
                yc.object = ( (TapSplineMesh) currentObject.object ).getYCurve();
                yc.name = module.getName() + ": " + TapBTranslate.text( "yPath" );
                ( (TapSplineMesh) currentObject.object ).getYCurve().edit(procedure.getWindow(), yc,
                    new Runnable()
                    {
                    @Override
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

            if ( sourceDoc == curveNumTF.getDocument() )
                source = curveNumTF;
            else if ( sourceDoc == curveNumPointsTF.getDocument() )
                source = curveNumPointsTF;
            else if ( sourceDoc == uSmoothnessTF.getDocument() )
                source = uSmoothnessTF;
            else if ( sourceDoc == vSmoothnessTF.getDocument() )
                source = vSmoothnessTF;

            if ( source == curveNumTF )
            {
                try
                {
                    int dum = ( Integer.valueOf( source.getText().trim() ) ).intValue();

                    if ( ( dum > 0 ) && ( dum < ( (Curve) ( (TapSplineMesh) currentObject.object ).getYCurve() ).getVertices().length ) )
                        source.setForeground( Color.black );
                    else
                        source.setForeground( Color.red );
                }
                catch ( NumberFormatException ex )
                {
                    source.setForeground( Color.red );
                }
            }

            if ( ( source == uSmoothnessTF ) || ( source == vSmoothnessTF ) )
            {
                try
                {
                    float dum = Float.parseFloat( source.getText().trim().replace( ',', '.' ) );

                    if ( ( dum < 0 ) || ( dum > 1 ) )
                        source.setForeground( Color.red );
                    else
                        source.setForeground( Color.black );
                }
                catch ( NumberFormatException ex )
                {
                    source.setForeground( Color.red );
                }
            }
            else if ( source == curveNumPointsTF )
            {
                try
                {
                    int dum = ( Integer.valueOf( source.getText().trim() ) ).intValue();

                    if ( dum > 0 )
                        source.setForeground( Color.black );
                    else
                        source.setForeground( Color.red );
                }
                catch ( NumberFormatException ex )
                {
                    source.setForeground( Color.red );
                }
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
            doLiveCheck( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
        public void insertUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
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
                int r = JOptionPane.showConfirmDialog(this, TapBTranslate.text( "parametersModified" ), TapBTranslate.text( "warning" ), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION );

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

