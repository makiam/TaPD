/*
 *  This class represents an abstract object Module, from which object modules such as AoI object module,
 *  spline mesh module, tube module are derived
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
import java.awt.*;
import java.awt.event.*;
import java.io.*;


/**
 *  Description of the Class
 *
 *@author     pims
 *@created    30 mai 2004
 */
public abstract class ObjectModule extends TapModule
{
    /**
     *  Description of the Field
     */
    protected ObjectInfo currentObject;
    /**
     *  Description of the Field
     */
    protected ObjectInfo currentSizedObject;
    /**
     *  Description of the Field
     */
    protected double currentSizeR;
    /**
     *  Description of the Field
     */
    protected double currentSizeY;
    /**
     *  Description of the Field
     */
    protected boolean deliverDuplicates;
    /**
     *  Description of the Field
     */
    protected ObjectModule module;
    /**
     *  Description of the Field
     */
    protected int sceneObjectIndex;
    /**
     *  Description of the Field
     */
    protected ObjectInfo backupObject;
    /**
     *  Description of the Field
     */
    protected boolean backDeliver;
    /**
     *  Description of the Field
     */
    protected boolean backHidden;


    /**
     *  Constructor for the ObjectModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  name       Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public ObjectModule( TapProcedure procedure, String name, Point position )
    {
        super( procedure, name, position );
        deliverDuplicates = false;
        setNumInput( 2 );
        setNumOutput( 1 );
        setup();
    }


    /**
     *  Description of the Method
     */
    protected void setup()
    {
        outputNature[0] = OBJECT_PORT;
        outputTooltips = new String[1];
        outputTooltips[0] = TapBTranslate.text( "objectOutput" );
        inputNature[0] = VALUE_PORT;
        inputNature[1] = VALUE_PORT;
        inputTooltips = new String[2];
        inputTooltips[0] = TapBTranslate.text( "sizeRInput", "1" );
        inputTooltips[1] = TapBTranslate.text( "sizeYInput", "1" );
        setBackgroundColor( Color.blue.darker() );
        currentSizeY = 1.0;
        currentSizeR = 1.0;
        currentSizedObject = null;
        module = this;
    }


    /**
     *  Constructor for the ObjectModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public ObjectModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        deliverDuplicates = in.readBoolean();
        currentObject = theScene.getObject( in.readInt() );
        currentSizedObject = null;
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
        out.writeBoolean( deliverDuplicates );
        out.writeInt( procedure.getScene().indexOf( currentObject ) );
        //System.out.println(name+": index "+procedure.getScene().indexOf(currentObject));
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public abstract TapModule duplicate();


    /**
     *  Description of the Method
     */
    public void unregisterFromScene()
    {
        Scene theScene = procedure.getScene();
        int index = theScene.indexOf( currentObject );

        if ( index >= 0 )
            theScene.removeObject( index, null );
    }


    /**
     *  Description of the Method
     *
     *@param  module  Description of the Parameter
     *@return         Description of the Return Value
     */
    public TapModule duplicate( ObjectModule module )
    {
        module.copyModule( this );
        module.unregisterFromScene();
        module.currentObject = this.currentObject.duplicate();
        module.currentObject.object = this.currentObject.object.duplicate();
        module.setCurrentObject( module.currentObject );
        module.currentSizedObject = null;

        return (TapModule) module;
    }


    /**
     *  Description of the Method
     *
     *@param  toScene  Description of the Parameter
     *@return          Description of the Return Value
     */
    public TapModule duplicate( Scene toScene )
    {
        return duplicate( toScene, null );
    }


    /**
     *  Description of the Method
     *
     *@param  toScene      Description of the Parameter
     *@param  translation  Description of the Parameter
     *@return              Description of the Return Value
     */
    public TapModule duplicate( Scene toScene, int[] translation )
    {
        Scene theScene = procedure.getScene();
        int index = 0;
        if ( translation != null )
            index = translation[theScene.indexOf( currentObject )];
        else
            index = theScene.indexOf( currentObject );
        ObjectModule module = (ObjectModule) this.duplicate();
        int removeIndex = theScene.indexOf( module.currentObject );
        module.registerInScene( toScene, index );
        procedure.getScene().removeObject( removeIndex, null );

        return (TapModule) module;
    }


    /**
     *  Description of the Method
     *
     *@param  toScene  Description of the Parameter
     *@param  index    Description of the Parameter
     */
    private void registerInScene( Scene toScene, int index )
    {
        if ( currentObject == null )
            return;

        currentObject = toScene.getObject( index );
        currentSizedObject = null;
    }


    /**
     *  Sets the currentObject attribute of the ObjectModule object
     *
     *@param  newObject  The new currentObject value
     */
    protected void setCurrentObject( ObjectInfo newObject )
    {
        Scene theScene = procedure.getScene();
        int index = theScene.indexOf( currentObject );

        if ( index >= 0 )
        {
            theScene.removeObject( index, null );
            theScene.addObject( newObject, index, null );
        }
        else
            theScene.addObject( newObject, null );

        currentObject = newObject;
    }


    /*
     *  do some cleanup before deletion
     */
    /**
     *  Description of the Method
     */
    @Override
    public void prepareToBeDeleted()
    {
        Scene theScene = procedure.getScene();
        int index = theScene.indexOf( currentObject );
        if ( index >= 0 )
        {
            procedure.deleteObject( index );
        }
        super.prepareToBeDeleted();
    }


    /**
     *  Description of the Method
     *
     *@param  size  Description of the Parameter
     *@param  sR    Description of the Parameter
     *@param  sY    Description of the Parameter
     *@param  info  Description of the Parameter
     */
    protected abstract void resizeObject( Vec3 size, double sR, double sY, ObjectInfo info );


    /**
     *  Description of the Method
     *
     *@param  size  Description of the Parameter
     *@param  sR    Description of the Parameter
     *@param  sY    Description of the Parameter
     *@param  info  Description of the Parameter
     */
    protected abstract void sizeObject( Vec3 size, double sR, double sY, ObjectInfo info );


    /**
     *  Gets the duplicate attribute of the ObjectModule object
     *
     *@return    The duplicate value
     */
    public boolean isDuplicate()
    {
        return deliverDuplicates;
    }


    /**
     *  Gets the sizedObject attribute of the ObjectModule object
     *
     *@param  gen  Description of the Parameter
     *@return      The sizedObject value
     */
    public ObjectInfo getSizedObject( TapRandomGenerator gen )
    {
        ObjectInfo info;
        BoundingBox bounds;
        double dum;
        Vec3 size;
        CoordinateSystem coords;

        double sizeR = 1.0;
        double sizeY = 1.0;
        BackModuleLink backLinks = procedure.getBackLink();
        BackModuleLink.BackLink bl = backLinks.findModule( this, 0 );

        if ( bl != null )
        {
            dum = bl.fromModule.getValue( bl.outputPort, null, gen.getSeed() );

            if ( dum > 0 )
                sizeR = dum;
        }

        bl = backLinks.findModule( this, 1 );

        if ( bl != null )
        {
            dum = bl.fromModule.getValue( bl.outputPort, null, gen.getSeed() );

            if ( dum > 0 )
                sizeY = dum;
        }

        if ( ( ( sizeR != currentSizeR ) || ( sizeY != currentSizeY ) ) || ( currentSizedObject == null ) )
        {
            info = currentObject.duplicate();
            info.object = info.object.duplicate();
            currentSizeR = sizeR;
            currentSizeY = sizeY;
            size = info.object.getBounds().getSize();
            resizeObject( size, sizeR, sizeY, info );
            currentSizedObject = info.duplicate();
            currentSizedObject.object = info.object.duplicate();
        }
        else if ( !deliverDuplicates )
        {
            info = currentSizedObject.duplicate();
            info.object = info.object.duplicate();
        }
        else
            info = currentSizedObject.duplicate();

        return info;
    }


    /**
     *  Gets the object attribute of the ObjectModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The object value
     */
    @Override
    public TapDesignerObjectCollection getObject( int outputPort, long seed )
    {
        TapRandomGenerator gen = new TapRandomGenerator( seed );

        if ( outputPort == -1 )
        {
            //the whole collection is asked for

            if ( currentObject == null )
            {
                System.out.println( "Object bidon !! " );

                return null;
            }

            TapDesignerObjectCollection newCollection = new TapDesignerObjectCollection( procedure );
            newCollection.addObject( getSizedObject( gen ), 0, name );

            if ( inputPortLink[0] != null )
            {
                for ( int i = 0; i < inputPortLink[0].length; ++i )
                {
                    TapModule mod = modules.get( linkToIndex[0][i] );
                    TapDesignerObjectCollection collection = mod.getObject( newCollection, inputPortLink[0][i], gen.getSeed() );

                    if ( collection != null )
                        newCollection.mergeCollection( collection, 0 );
                }
            }

            return newCollection;
        }
        else if ( outputPort == 0 || outputPort == -2 )
        {
            //only the object stored in the module is asked for

            TapDesignerObjectCollection newCollection = new TapDesignerObjectCollection( procedure );
            newCollection.addObject( getSizedObject( gen ), 0, name );

            return newCollection;
        }
        else
        {
            System.out.println( "wrong output port for object module : " + outputPort );

            return null;
        }
    }


    /**
     *  Gets the value attribute of the ObjectModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  var         Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The value value
     */
    @Override
    public double getValue( int outputPort, double[] var, long seed )
    {
        System.out.println( "value asked for AoI object module" );

        return (double) 0.0;
        //should never happen !!!
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
        TapDesignerObjectCollection collection;
        procedure.initProcedure();

        if ( isMainEntry() && ( modifiers & ActionEvent.CTRL_MASK ) == 0 )
        {
            collection = getObject( -1, procedure.getSeed() );

            if ( isMainEntry() )
                procedure.setCurrentObject( collection );
        }
        else
            collection = getObject( 0, procedure.getSeed() );

        if ( collection != null )
        {
            ObjectInfo newObjectInfo = new ObjectInfo( collection, new CoordinateSystem(), getName() );
            newObjectInfo.object.setTexture( procedure.getScene().getDefaultTexture(), procedure.getScene().getDefaultTexture().getDefaultMapping(newObjectInfo.object) );
            setupPreviewFrame( newObjectInfo );
        }
    }


    /**
     *  Description of the Method
     */
    @Override
    public void initGenerationProcess()
    {
        //called once before generation

        currentSizedObject = null;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public boolean acceptsMainEntry()
    {
        return true;
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
}

