/*
 *  This class represents a tube module
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
import buoy.event.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;

import artofillusion.tapDesigner.TapModule.*;

/**
 *  Description of the Class
 *
 *@author     Fran�ois Guillet
 *@created    19 avril 2004
 */
public class TubeModule extends ObjectModule
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
        super(procedure, TapBTranslate.text( "tube" ), position );
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
    public TubeModule( DataInputStream in, Scene theScene ) throws IOException, InvalidObjectException
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
    @Override
    public void writeToFile( DataOutputStream out, Scene theScene )throws IOException
    {
        super.writeToFile( out, theScene );
        out.writeShort( 0 );
    }


    /**
     *  Gets the moduleTypeInfo attribute of the TubeModule object
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
        TubeModule module = new TubeModule( this.procedure, this.location );

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
    @Override
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
    @Override
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
        @Override
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
            yc.name = module.getName() + ": " + TapBTranslate.text( "tube" );
            ( (Tube) currentObject.object ).edit( procedure.getWindow(), yc,
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
            materialButton.setEnabled( currentObject.object.canSetMaterial() );
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


}

