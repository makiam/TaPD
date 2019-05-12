/*
 *  This class represents an AoI object Module
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
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import artofillusion.tapDesigner.TapModule.*;


/**
 *  This class represents an leaf object module
 *
 *@author     François Guillet
 *@created    19 avril 2004
 */
public class LeafModule extends ObjectModule
{
    private static TapModule.ModuleTypeInfo typeInfo;


    /**
     *  Constructor for the LeafModule object
     *
     *@param  procedure  The procedure to which the module belongs to
     *@param  position   The graphical location of the module
     */
    public LeafModule( TapProcedure procedure, Point position )
    {
        super(procedure, TapBTranslate.text( "leaf" ), position );
        for ( int i = 0; i < procedure.getNumObjects(); ++i )
        {
            ObjectInfo info = procedure.getScene().getObject( i );
            if ( info.object instanceof SplineMesh )
            {
                setCurrentObject( info.duplicate( info.object.duplicate() ) );
                break;
            }
        }
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "leafName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/leaf_tree.png" ) ) );
    }


    /**
     *  Constructor for the LeafModule object
     *
     *@param  position   The graphical location of the module
     *@param  leaf       Description of the Parameter
     *@param  procedure  Description of the Parameter
     */
    public LeafModule( TapProcedure procedure, Point position, ObjectInfo leaf )
    {
        super(procedure, TapBTranslate.text( "leaf" ), position );
        setCurrentObject( leaf );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "leafName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/aoi_tree.png" ) ) );
    }


    /**
     *  Constructor for the LeafModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public LeafModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( version < 0 || version > 0 )
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
    @Override
    public void writeToFile( DataOutputStream out, Scene theScene ) throws IOException
    {
        super.writeToFile( out, theScene );
        out.writeShort( 0 );

    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public TapModule duplicate()
    {
        LeafModule module = new LeafModule( this.procedure, this.location );

        return duplicate( module );
    }


    /*
     *  resizes the object along R and Y
     */
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
        info.object.setSize( size.x * sizeR, size.y * sizeY, size.z * sizeR );
        TapUtils.setObjectAtMinY( info );
    }


    /*
     *  sizes the object. Rsize and size have different meanings for TaPD objects, and hence
     *  the leaf object module offers the two methods.
     */
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
        resizeObject( size, sizeR, sizeY, info );
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  The parent BFrame
     */
    @Override
    public void edit( BFrame parentFrame )
    {

        int numObjects = procedure.getNumObjects();
        if ( numObjects == 0 )
            return;

        if ( isEditDialogOn )
            editBDialog.toFront();
        else
        {
            editBDialog = new EditWidgetDialogBase( parentFrame, this );
            isEditDialogOn = true;
        }
    }



    /**
     *  Gets the moduleTypeInfo attribute of the LeafModule object
     *
     *@return    The moduleTypeInfo value
     */
    @Override
    public ModuleTypeInfo getModuleTypeInfo()
    {
        return typeInfo;
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
        return new LeafEditWidget( cb, standalone, this );
    }


    /**
     *  AoI object editor window
     *
     *@author     Francois Guillet
     *@created    19 avril 2004
     */
    private class LeafEditWidget extends EditWidgetBase
    {
        private BComboBox objectChoice;
        private ObjectInfo dialogCurrentObject;
        private Vector object3DVector;
        private BCheckBox leafCB;
        private BCheckBox deliverCB;
        private BCheckBox hiddenCB;
        private ValueField thicknessVF;
        private BButton variationButton;
        private ValueField centerEdgesVF, centerVerticesVF;
        private ValueField edgesEdgesVF, edgesVerticesVF;
        private ValueField leafTolVF;
        private BButton textureButton;
        private BButton materialButton;
        private BButton editButton;
        private BSplitPane sp;
        private ObjectInfo stackObject;
        private TapFunction leafFunction;
        private float edgeSmoothness;
        private float vertSmoothness;
        private float bondaryEdgeSmoothness;
        private float bondaryVertSmoothness;
        private boolean leafThickening;
        private double thickness, leafTol;


        /**
         *  Constructor for the LeafEditWidget object
         *
         *@param  cb          Runnable to call when updating.
         *@param  standalone  In a standalone window of in a properties split
         *      pane.
         *@param  module      The module that owns the edit widget.
         */
        public LeafEditWidget( Runnable cb, boolean standalone, TapModule module )
        {
            super( cb, standalone, "leafModuleTitle", module );

            backupObject = currentObject.duplicate( currentObject.object.duplicate() );
            dialogCurrentObject = currentObject.duplicate( currentObject.object.duplicate() );

            if ( currentObject.object instanceof TapLeaf )
            {
                TapLeaf leaf = (TapLeaf) currentObject.object;
                leafThickening = true;
                thickness = leaf.getThickness();
                leafTol = leaf.getTolerance();
                leafFunction = leaf.getShape();
                edgeSmoothness = leaf.getEdgeSmoothness();
                vertSmoothness = leaf.getVertSmoothness();
                bondaryEdgeSmoothness = leaf.getBoundEdgeSmoothness();
                bondaryVertSmoothness = leaf.getBoundVertSmoothness();
            }
            else
            {
                leafThickening = false;
                thickness = 0.1;
                leafTol = 0.1;
                leafFunction = new TapFunction( 1.0, 0.0 );
                edgeSmoothness = 1.0f;
                vertSmoothness = 1.0f;
                bondaryEdgeSmoothness = 0.0f;
                bondaryVertSmoothness = 1.0f;
            }

            textureButton = TapBTranslate.bButton( "setTexture", this, "doTexture" );
            materialButton = TapBTranslate.bButton( "setMaterial", this, "doMaterial" );
            editButton = TapBTranslate.bButton( "edit", this, "doEdit" );

            int numObjects = procedure.getNumObjects();
            object3DVector = new Vector();
            for ( int i = 0; i < numObjects; ++i )
                if ( procedure.getScene().getObject( i ).object instanceof SplineMesh )
                    object3DVector.add( procedure.getScene().getObject( i ) );

            if ( object3DVector.size() > 0 )
            {
                String[] comboStrings = new String[object3DVector.size()];

                for ( int i = 0; i < object3DVector.size(); ++i )
                    comboStrings[i] = ( (ObjectInfo) object3DVector.elementAt( i ) ).name;

                objectChoice = new BComboBox( comboStrings );

            }
            else
            {
                objectChoice = new BComboBox();
                objectChoice.setEnabled( false );
            }

            ColumnContainer cc = new ColumnContainer();
            LayoutInfo layout = new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 3, 3, 3, 3 ), null );
            LayoutInfo buttonLayout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets( 3, 3, 3, 3 ), null );
            cc.setDefaultLayout( layout );
            cc.add( TapBTranslate.bLabel( "chooseAnObject" ) );
            LayoutInfo comboLayout = new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 3, 20, 3, 3 ), null );
            cc.add( objectChoice, comboLayout );

            RowContainer rc = new RowContainer();
            rc.setDefaultLayout( new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 2, 2, 2, 2 ), null ) );
            rc.add( leafCB = TapBTranslate.bCheckBox( "leafThickening", leafThickening ) );
            rc.add( thicknessVF = new ValueField( thickness, ValueField.NONNEGATIVE ) );
            rc.add( variationButton = TapBTranslate.bButton( "variation", this, "doVariation" ) );
            cc.add( rc, new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 2, 2, 2, 2 ), null ) );

            cc.add( TapBTranslate.bLabel( "meshConversionTolerance" ) );
            cc.add( leafTolVF = new ValueField( leafTol, ValueField.NONNEGATIVE ), new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 2, 2, 2, 2 ), null ) );

            GridContainer subgc = new GridContainer( 2, 2 );
            subgc.setDefaultLayout( new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 2, 2, 2, 2 ), null ) );
            subgc.add( TapBTranslate.bLabel( "edges" ), 0, 0 );
            subgc.add( centerEdgesVF = new ValueField( edgeSmoothness, ValueField.NONNEGATIVE ), 0, 1 );
            subgc.add( TapBTranslate.bLabel( "vertices" ), 1, 0 );
            subgc.add( centerVerticesVF = new ValueField( vertSmoothness, ValueField.NONNEGATIVE ), 1, 1 );
            cc.add( new BOutline( subgc, BorderFactory.createTitledBorder( BorderFactory.createRaisedBevelBorder(), TapBTranslate.text( "leafCenter" ) ) ), new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 1, 1, 1, 1 ), null ) );

            subgc = new GridContainer( 2, 2 );
            subgc.setDefaultLayout( new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 2, 2, 2, 2 ), null ) );
            subgc.add( TapBTranslate.bLabel( "edges" ), 0, 0 );
            subgc.add( edgesEdgesVF = new ValueField( bondaryEdgeSmoothness, ValueField.NONNEGATIVE ), 0, 1 );
            subgc.add( TapBTranslate.bLabel( "vertices" ), 1, 0 );
            subgc.add( edgesVerticesVF = new ValueField( bondaryVertSmoothness, ValueField.NONNEGATIVE ), 1, 1 );
            cc.add(new BOutline( subgc, BorderFactory.createTitledBorder( BorderFactory.createRaisedBevelBorder(), TapBTranslate.text( "leafEdges" ) ) ), new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 1, 1, 1, 1 ), null ) );

            cc.add( textureButton, buttonLayout );
            cc.add( materialButton, buttonLayout );
            cc.add( editButton, buttonLayout );

            deliverCB = TapBTranslate.bCheckBox( "deliverDuplicates", deliverDuplicates );
            hiddenCB = TapBTranslate.bCheckBox( "hidden", !currentObject.visible );
            cc.add( deliverCB );
            cc.add( hiddenCB );

            sp = new BSplitPane();
            sp.add( cc, 0 );
            sp.setResizeWeight( 0.0 );
            sp.setOneTouchExpandable( true );
            add( sp, BorderContainer.CENTER );
            leafCB.addEventLink( ValueChangedEvent.class, this, "doLeafCB" );
            objectChoice.addEventLink( ValueChangedEvent.class, this, "doSplineMeshChoice" );

            thicknessVF.addEventLink( ValueChangedEvent.class, this, "doModified" );
            centerEdgesVF.addEventLink( ValueChangedEvent.class, this, "doModified" );
            centerVerticesVF.addEventLink( ValueChangedEvent.class, this, "doModified" );
            edgesEdgesVF.addEventLink( ValueChangedEvent.class, this, "doModified" );
            edgesVerticesVF.addEventLink( ValueChangedEvent.class, this, "doModified" );

            deliverCB.addEventLink( ValueChangedEvent.class, this, "doModified" );
            hiddenCB.addEventLink( ValueChangedEvent.class, this, "doModified" );

            if ( !( dialogCurrentObject.object instanceof TapLeaf ) )
                enableLeafWidgets( false );
            updateObject();
        }


        /**
         *  Sets the texture of the object
         */
        @SuppressWarnings("ResultOfObjectAllocationIgnored")
        private void doTexture()
        {
            ObjectInfo[] obj = new ObjectInfo[1];
            obj[0] = dialogCurrentObject;
            new TaPDObjectTextureDialog( TapUtils.getParentBFrame( this ), procedure.getScene(), obj );
            doModified();
        }


        /**
         *  Sets the object material
         */
        @SuppressWarnings("ResultOfObjectAllocationIgnored")
        private void doMaterial()
        {
            ObjectInfo[] obj = new ObjectInfo[1];
            obj[0] = dialogCurrentObject;
            new TaPDObjectMaterialDialog( TapUtils.getParentBFrame( this ), procedure.getScene(), obj );
            doModified();
        }


        /**
         *  User has selected another object in the combo box
         */
        private void doSplineMeshChoice()
        {
            for ( int i = 0; i < object3DVector.size(); ++i )
                if ( ( (ObjectInfo) object3DVector.elementAt( i ) ).name.equals( (String) objectChoice.getSelectedValue() ) )
                {
                    if ( leafCB.getState() )
                    {
                        getValues();
                        SplineMesh sm = (SplineMesh) ( (ObjectInfo) object3DVector.elementAt( i ) ).object;
                        dialogCurrentObject.object = new TapLeaf( sm, vertSmoothness, edgeSmoothness, bondaryVertSmoothness, bondaryEdgeSmoothness, leafFunction, leafTol, thickness );
                        dialogCurrentObject.clearCachedMeshes();
                    }
                    else
                        dialogCurrentObject = ( (ObjectInfo) object3DVector.elementAt( i ) ).duplicate( ( (ObjectInfo) object3DVector.elementAt( i ) ).object.duplicate() );
                    updateObject();
                    doModified();
                }
        }


        /**
         *  Description of the Method
         */
        private void doVariation()
        {
            editDialog = leafFunction.edit( (JFrame) TapUtils.getParentBFrame( this ).getComponent(), TapBTranslate.text( "leafShape", module.getName() ),
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if ( dialogCurrentObject.object instanceof TapLeaf )
                        {
                            TapLeaf leaf = (TapLeaf) dialogCurrentObject.object;
                            leaf.setShape( leafFunction, leaf.getThickness() );
                            updateObject();
                            doModified();
                        }
                    }
                } );
        }


        /**
         *  Description of the Method
         */
        private void doLeafCB()
        {
            updateLeafObject();
            enableLeafWidgets( leafCB.getState() );
            updateObject();
            doModified();
        }


        /**
         *  Description of the Method
         */
        private void updateLeafObject()
        {
            if ( ( dialogCurrentObject.object instanceof SplineMesh ) && ( leafCB.getState() ) )
            {
                getLeafValues();
                SplineMesh sm = (SplineMesh) dialogCurrentObject.object;
                dialogCurrentObject.object = new TapLeaf( sm, vertSmoothness, edgeSmoothness, bondaryVertSmoothness, bondaryEdgeSmoothness, leafFunction, leafTol, thickness );
                dialogCurrentObject.clearCachedMeshes();
            }
            else if ( ( dialogCurrentObject.object instanceof TapLeaf ) && ( leafCB.getState() ) )
            {
                getLeafValues();
                TapLeaf leaf = (TapLeaf) dialogCurrentObject.object;
                dialogCurrentObject.object = new TapLeaf( leaf.getOriginalSplineMesh(), vertSmoothness, edgeSmoothness, bondaryVertSmoothness, bondaryEdgeSmoothness, leafFunction, leafTol, thickness );
                dialogCurrentObject.clearCachedMeshes();
            }
            else if ( ( dialogCurrentObject.object instanceof TapLeaf ) && ( !leafCB.getState() ) )
            {
                dialogCurrentObject.object = ( (TapLeaf) dialogCurrentObject.object ).getOriginalSplineMesh();
            }
        }


        /**
         *  Description of the Method
         */
        @Override
        public void doTest()
        {
            updateLeafObject();
            updateObject();
            super.doTest();
        }


        /**
         *  Description of the Method
         */
        @Override
        public void doValidate()
        {
            updateLeafObject();
            updateObject();
            super.doValidate();
        }


        /**
         *  Edits the object
         */
        private void doEdit()
        {
            dialogCurrentObject.object.edit( procedure.getWindow(), dialogCurrentObject,
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateObject();
                        doModified();
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
                dialogCurrentObject = currentObject.duplicate( currentObject.object.duplicate() );
                objectChoice.removeEventLink( ValueChangedEvent.class, this );
                for ( int i = 0; i < object3DVector.size(); ++i )
                    if ( ( (ObjectInfo) object3DVector.elementAt( i ) ).name.equals( currentObject.name ) )
                        objectChoice.setSelectedIndex( i );
                objectChoice.addEventLink( ValueChangedEvent.class, this, "doSplineMeshChoice" );
                updateObject();
                if ( currentObject.object instanceof TapLeaf )
                {
                    TapLeaf leaf = (TapLeaf) currentObject.object;
                    leafThickening = true;
                    thickness = leaf.getThickness();
                    leafTol = leaf.getTolerance();
                    leafFunction = leaf.getShape();
                    edgeSmoothness = leaf.getEdgeSmoothness();
                    vertSmoothness = leaf.getVertSmoothness();
                    bondaryEdgeSmoothness = leaf.getBoundEdgeSmoothness();
                    bondaryVertSmoothness = leaf.getBoundVertSmoothness();
                    leafCB.setState( true );
                    thicknessVF.setValue( thickness );
                    centerEdgesVF.setValue( edgeSmoothness );
                    centerVerticesVF.setValue( vertSmoothness );
                    edgesEdgesVF.setValue( bondaryEdgeSmoothness );
                    edgesVerticesVF.setValue( bondaryVertSmoothness );
                    leafTolVF.setValue( leafTol );
                    hiddenCB.setState(currentObject.visible);
                    deliverCB.setState(deliverDuplicates);
                    enableLeafWidgets( true );

                }
                else
                {
                    enableLeafWidgets( false );
                }
                super.showValues( force );
            }
        }


        /**
         *  Description of the Method
         *
         *@param  enable  Description of the Parameter
         */
        private void enableLeafWidgets( boolean enable )
        {
            thicknessVF.setEnabled( enable );
            centerEdgesVF.setEnabled( enable );
            centerVerticesVF.setEnabled( enable );
            edgesEdgesVF.setEnabled( enable );
            edgesVerticesVF.setEnabled( enable );
            leafTolVF.setEnabled( enable );
            variationButton.setEnabled( enable );
        }


        /**
         *  Description of the Method
         */
        private void updateObject()
        {

            ObjectPreviewCanvas opc;
            sp.add( opc = new ObjectPreviewCanvas( dialogCurrentObject ), 1 );
            opc.setPreferredSize( new Dimension( 250, 250 ) );
            textureButton.setEnabled( dialogCurrentObject.object.canSetTexture() );
            materialButton.setEnabled( dialogCurrentObject.object.canSetMaterial() );
            editButton.setEnabled( dialogCurrentObject.object.isEditable() );
            deliverCB.setState( deliverDuplicates );
            hiddenCB.setState( !dialogCurrentObject.visible );
        }


        /**
         *  Gets the undoValues attribute of the LeafEditWidget object
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
         *  Gets the valuesof the LeafEditWidget object
         */
        @Override
        protected void getValues()
        {
            deliverDuplicates = deliverCB.getState();
            dialogCurrentObject.visible = !hiddenCB.getState();
            setCurrentObject( dialogCurrentObject );
            currentSizedObject = null;
        }


        /**
         *  Gets the leafValues attribute of the LeafEditWidget object
         */
        private void getLeafValues()
        {
            leafTol = leafTolVF.getValue();
            thickness = thicknessVF.getValue();
            edgeSmoothness = new Float( centerEdgesVF.getValue() );
            vertSmoothness = new Float( centerVerticesVF.getValue() );
            bondaryEdgeSmoothness = new Float( edgesEdgesVF.getValue() );
            bondaryVertSmoothness = new Float( edgesVerticesVF.getValue() );
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
        @Override
        protected void doModified()
        {
            super.doModified();
            currentSizedObject = null;
        }


        /**
         *  Description of the Method
         */
        @Override
        public void pushValues()
        {
            stackObject = dialogCurrentObject;
        }


        /**
         *  Description of the Method
         */
        @Override
        public void popValues()
        {
            dialogCurrentObject = stackObject;
        }

    }
}

