/*
 *  This class represents an AoI object Module
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
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import artofillusion.tapDesigner.TapModule.*;


/**
 *  This class represents an AoI object module
 *
 *@author     Francois Guillet
 *@created    19 avril 2004
 */
public class AoIObjectModule extends ObjectModule
{
    private static TapModule.ModuleTypeInfo typeInfo;


    /**
     *  Constructor for the AoIObjectModule object
     *
     *@param  procedure  The procedure to which the module belongs to
     *@param  position   The graphical location of the module
     */
    public AoIObjectModule( TapProcedure procedure, Point position )
    {
        super(procedure, TapBTranslate.text( "object" ), position );
        setCurrentObject( procedure.getScene().getObject( 0 ).duplicate() );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "aoiObjectName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/aoi_tree.png" ) ) );
    }


    /**
     *  Constructor for the AoIObjectModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public AoIObjectModule( DataInputStream in, Scene theScene )
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
        AoIObjectModule module = new AoIObjectModule( this.procedure, this.location );

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
     *  the AoI object module offers the two methods.
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
        super.edit( parentFrame );

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
     *  Gets the moduleTypeInfo attribute of the AoIObjectModule object
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
        return new AoIObjectEditWidget( cb, standalone, this );
    }


    /**
     *  AoI object editor window
     *
     *@author     Francois Guillet
     *@created    19 avril 2004
     */
    private class AoIObjectEditWidget
             extends EditWidgetBase
    {
        private BComboBox objectChoice;
        private ObjectInfo dialogCurrentObject;
        private Vector object3DVector;
        private BCheckBox deliverCB;
        private BCheckBox hiddenCB;
        private BButton textureButton;
        private BButton materialButton;
        private BButton editButton;
        private BSplitPane sp;
        private ObjectInfo stackObject;


        /**
         *  Constructor for the AoIObjectEditWidget object
         *
         *@param  cb          Runnable to call when updating.
         *@param  standalone  In a standalone window of in a properties split
         *      pane.
         *@param  module      The module that owns the edit widget.
         */
        public AoIObjectEditWidget( Runnable cb, boolean standalone, TapModule module )
        {
            super( cb, standalone, "objectModuleTitle", module );

            backupObject = currentObject.duplicate( currentObject.object.duplicate() );
            dialogCurrentObject = currentObject.duplicate( currentObject.object.duplicate() );

            textureButton = TapBTranslate.bButton( "setTexture", this, "doTexture" );
            materialButton = TapBTranslate.bButton( "setMaterial", this, "doMaterial" );
            editButton = TapBTranslate.bButton( "edit", this, "doEdit" );

            int numObjects = procedure.getNumObjects();
            object3DVector = new Vector();
            for ( int i = 0; i < numObjects; ++i )
                object3DVector.add( procedure.getScene().getObject( i ) );

            String[] comboStrings = new String[object3DVector.size()];

            for ( int i = 0; i < object3DVector.size(); ++i )
                comboStrings[i] = ( (ObjectInfo) object3DVector.elementAt( i ) ).name;

            objectChoice = new BComboBox( comboStrings );

            for ( int i = 0; i < object3DVector.size(); ++i )
                if ( ( (ObjectInfo) object3DVector.elementAt( i ) ).name.equals( currentObject.name ) )
                    objectChoice.setSelectedIndex( i );

            deliverCB = TapBTranslate.bCheckBox( "deliverDuplicates", deliverDuplicates );
            hiddenCB = TapBTranslate.bCheckBox( "hidden", !currentObject.visible );

            BorderContainer content = new BorderContainer();
            ColumnContainer cc = new ColumnContainer();
            LayoutInfo layout = new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 3, 3, 3, 3 ), new Dimension( 0, 0 ) );
            LayoutInfo buttonLayout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets( 3, 3, 3, 3 ), new Dimension( 0, 0 ) );
            cc.setDefaultLayout( layout );
            cc.add( TapBTranslate.bLabel( "chooseAnObject" ) );
            LayoutInfo comboLayout = new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 3, 20, 3, 3 ), new Dimension( 0, 0 ) );
            cc.add( objectChoice, comboLayout );
            cc.add( textureButton, buttonLayout );
            cc.add( materialButton, buttonLayout );
            cc.add( editButton, buttonLayout );
            cc.add( deliverCB );
            cc.add( hiddenCB );

            sp = new BSplitPane();
            sp.add( cc, 0 );
            sp.setResizeWeight( 0.0 );
            sp.setOneTouchExpandable( true );
            add( sp, BorderContainer.CENTER );

            objectChoice.addEventLink( ValueChangedEvent.class, this, "doObjectChoice" );
            deliverCB.addEventLink( ValueChangedEvent.class, this, "doModified" );
            hiddenCB.addEventLink( ValueChangedEvent.class, this, "doModified" );

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
        private void doObjectChoice()
        {
            for ( int i = 0; i < object3DVector.size(); ++i )
                if ( ( (ObjectInfo) object3DVector.elementAt( i ) ).name.equals( (String) objectChoice.getSelectedValue() ) )
                {
                    dialogCurrentObject = ( (ObjectInfo) object3DVector.elementAt( i ) ).duplicate( ( (ObjectInfo) object3DVector.elementAt( i ) ).object.duplicate() );
                    updateObject();
                    doModified();
                }
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
                objectChoice.addEventLink( ValueChangedEvent.class, this, "doObjectChoice" );
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
            sp.add( opc = new ObjectPreviewCanvas( dialogCurrentObject ), 1 );
            if ( dialogCurrentObject.object instanceof Light )
                opc.setRenderMode( ViewerCanvas.RENDER_WIREFRAME );
            opc.setPreferredSize( new Dimension( 250, 250 ) );
            textureButton.setEnabled( dialogCurrentObject.object.canSetTexture() );
            materialButton.setEnabled( dialogCurrentObject.object.canSetMaterial() );
            editButton.setEnabled( dialogCurrentObject.object.isEditable() );
            deliverCB.setState( deliverDuplicates );
            hiddenCB.setState( !dialogCurrentObject.visible );
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
            setCurrentObject( dialogCurrentObject );
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

