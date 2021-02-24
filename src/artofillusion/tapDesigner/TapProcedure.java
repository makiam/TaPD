/*
 *  This class stores the whole procedure for creating a plant object
 *  It mainly stores modules and objects imported from AoI.
 */
/*
 *  Copyright (C) 2003 by Francois Guillet
 *  Changes copyright (C) 2019-2021 by Maksim Khramov
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
import artofillusion.image.*;
import artofillusion.material.*;
import artofillusion.object.*;
import artofillusion.texture.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import javax.swing.*;



/**
 *  Description of the Class
 *
 *@author     pims
 *@created    4 avril 2004
 */
public class TapProcedure
{
    //{{{ variables
    private Vector modules;
    //the full module list
    private Scene theScene;
    //private scene holding textures, objects imported from the TaPD
    //and the objects int the modules
    private long seed;
    private LayoutWindow window;
    //the layout window which holds the procedure
    private BackModuleLink backLinks;
    //This instance of BackModuleLink allow modules to trace back links
    //It is initialized by the procedure prior to calculating a new object
    TapDesignerObjectCollection currentObject;
    ProcPanelLayout[] procPanelLayouts;
    boolean modified;
    private int renderingLevel;
    private int viewLevel;
    private int undoRecordSize;
    private TapUndoRecord undoRecord;
    private TapProcPanel procPanel;
    private Rectangle bounds;
    /**
     *  Description of the Field
     */
    public final static int MAX_UNDO = 11;


    //}}}

    //{{{ Constructor
    /**
     *  Constructor for the TapProcedure object
     *
     *@param  objVec  Description of the Parameter
     */
    public TapProcedure( Vector objVec )
    {
        theScene = new Scene();
        modules = new Vector();

        if ( objVec != null )
        {
            //add objects to the default scene

            ObjectInfo[] obj = new ObjectInfo[objVec.size()];

            for ( int i = 0; i < objVec.size(); ++i )
                obj[i] = (ObjectInfo) objVec.elementAt( i );

            addObjectToScene( obj );
        }

        seed = 123456789;
        currentObject = null;
        modified = false;
        renderingLevel = -1;
        viewLevel = -1;
        undoRecordSize = MAX_UNDO;
    }


    //}}}

    //{{{ Adds an aoi object to the procedure scene
    /**
     *  Adds a feature to the ObjectToScene attribute of the TapProcedure object
     *
     *@param  obj  The feature to be added to the ObjectToScene attribute
     *@return      Description of the Return Value
     */
    public ObjectInfo[] addObjectToScene( ObjectInfo[] obj )
    {
        //to paste objects into the TaPD procedure scene

        Vector textures = new Vector();
        int numObjects = getNumObjects();

        for ( int i = 0; i < obj.length; i++ )
        {
            Texture tex = obj[i].object.getTexture();

            if ( tex instanceof LayeredTexture )
            {
                LayeredMapping map = (LayeredMapping) obj[i].object.getTextureMapping();
                Texture[] layer = map.getLayers();

                for ( int j = 0; j < layer.length; j++ )
                {
                    Texture dup = layer[j].duplicate();
                    dup.setID( layer[j].getID() );

                    TextureMapping dupMap = map.getLayerMapping( j );
                    textures.addElement( dup );
                    map.setLayer( j, dup );
                    map.setLayerMapping( j, map.getLayerMapping( j ).duplicate(obj[i].object, dup ) );
                }
            }
            else if ( tex != null )
            {
                Texture dup = tex.duplicate();
                dup.setID( tex.getID() );
                textures.addElement( dup );
                obj[i].object.setTexture( dup, obj[i].object.getTextureMapping().duplicate(obj[i].object, dup ) );
            }

            setModified( true );
        }

        // Next make a list of all materials used by the objects.
        Vector materials = new Vector();

        for ( int i = 0; i < obj.length; i++ )
        {
            Material mat = obj[i].object.getMaterial();

            if ( mat != null )
            {
                Material dup = mat.duplicate();
                dup.setID( mat.getID() );
                materials.addElement( dup );
                obj[i].object.setMaterial( dup, obj[i].object.getMaterialMapping().duplicate( obj[i].object, dup ) );
            }
        }

        // Now make a list of all ImageMaps used by any of them.
        Vector images = new Vector();

        for ( int i = 0; i < theScene.getNumImages(); i++ )
        {
            ImageMap map = theScene.getImage( i );
            boolean used = false;

            for ( int j = 0; j < textures.size() && !used; j++ )
                used = ( (Texture) textures.elementAt( j ) ).usesImage( map );

            for ( int j = 0; j < materials.size() && !used; j++ )
                used = ( (Material) materials.elementAt( j ) ).usesImage( map );

            if ( used )
                images.addElement( map );
        }

        // Save all of them to the appropriate arrays.
        ObjectInfo[] clipboardObject = obj;
        Texture[] clipboardTexture = new Texture[textures.size()];
        textures.copyInto( clipboardTexture );

        Material[] clipboardMaterial = new Material[materials.size()];
        materials.copyInto( clipboardMaterial );

        ImageMap[] clipboardImage = new ImageMap[images.size()];
        images.copyInto( clipboardImage );

        // First add any new image maps to the scene.
        for ( int i = 0; i < clipboardImage.length; i++ )
        {
            int j;

            for ( j = 0; j < theScene.getNumImages() && clipboardImage[i].getID() != theScene.getImage( j ).getID(); j++ )
                ;

            if ( j == theScene.getNumImages() )
                theScene.addImage( clipboardImage[i] );
        }

        // Now add any new textures.
        for ( int i = 0; i < clipboardTexture.length; i++ )
        {
            Texture newtex;
            int j;

            for ( j = 0; j < theScene.getNumTextures() && clipboardTexture[i].getID() != theScene.getTexture( j ).getID(); j++ )
                ;

            if ( j == theScene.getNumTextures() && !clipboardTexture[i].getName()
                    .equals( theScene.getTexture( 0 ).getName() ) )
            {
                newtex = clipboardTexture[i].duplicate();
                newtex.setID( clipboardTexture[i].getID() );
                theScene.addTexture( newtex );
            }
            else
            {
                if ( j == theScene.getNumTextures() )
                    newtex = theScene.getTexture( 0 );
                else
                    newtex = theScene.getTexture( j );
            }

            for ( j = 0; j < clipboardObject.length; j++ )
            {
                Texture current = clipboardObject[j].object.getTexture();

                if ( current == clipboardTexture[i] )
                    clipboardObject[j].setTexture( newtex, clipboardObject[j].object.getTextureMapping().duplicate(clipboardObject[j].object, newtex ) );
                else if ( current instanceof LayeredTexture )
                {
                    LayeredMapping map = (LayeredMapping) clipboardObject[j].object.getTextureMapping();
                    map = (LayeredMapping) map.duplicate();
                    clipboardObject[j].setTexture( new LayeredTexture( map ), map );

                    Texture[] layer = map.getLayers();

                    for ( int k = 0; k < layer.length; k++ )
                        if ( layer[k] == clipboardTexture[i] )
                        {
                            map.setLayer( k, newtex );
                            map.setLayerMapping( k, map.getLayerMapping( k ).duplicate(clipboardObject[j].object, newtex ) );
                        }
                }
            }
        }

        // Add any new materials.
        for ( int i = 0; i < clipboardMaterial.length; i++ )
        {
            Material newmat;
            int j;

            for ( j = 0; j < theScene.getNumMaterials() && clipboardMaterial[i].getID() != theScene.getMaterial( j ).getID(); j++ )
                ;

            if ( j == theScene.getNumMaterials() )
            {
                newmat = clipboardMaterial[i].duplicate();
                newmat.setID( clipboardMaterial[i].getID() );
                theScene.addMaterial( newmat );
            }
            else
                newmat = theScene.getMaterial( j );

            for ( j = 0; j < clipboardObject.length; j++ )
            {
                Material current = clipboardObject[j].object.getMaterial();

                if ( current == clipboardMaterial[i] )
                	clipboardObject[j].setMaterial(newmat, clipboardObject[j].object.getMaterialMapping().duplicate(clipboardObject[j].object, newmat));
            }
        }

        ObjectInfo[] nobj = ObjectInfo.duplicateAll( clipboardObject );
        for ( int i = 0; i < nobj.length; i++ )
        {
            if ( ( nobj[i].object instanceof TapTube ) || ( nobj[i].object instanceof TapSplineMesh ) )
                theScene.addObject( nobj[i], theScene.getNumObjects(), null );
            else
                theScene.addObject( nobj[i], getNumObjects(), null );
        }
        return nobj;
    }


    //}}}

    //{{{ Adds an aoi object to the procedure
    /**
     *  Adds a feature to the Object attribute of the TapProcedure object
     *
     *@param  info  The feature to be added to the Object attribute
     *@return       Description of the Return Value
     */
    public ObjectInfo addObject( ObjectInfo info )
    {
        boolean found = false;
        String name;

        found = true;
        name = new String( info.name );

        int count = 1;
        int numObjects = getNumObjects();

        while ( found )
        {
            ++count;
            found = false;

            for ( int i = 0; i < numObjects; ++i )
                if ( theScene.getObject( i ).name.equals( name ) )
                    found = true;

            if ( found )
                name = info.name + "_" + count;
        }

        ObjectInfo[] obj = new ObjectInfo[1];
        obj[0] = info.duplicate();
        obj[0].name = name;
        return ( addObjectToScene( obj ) )[0];
    }


    //}}}

    //{{{ Debug printout method
    /*
     *  used to debug
     */
    /**
     *  Description of the Method
     */
    public void printSceneContent()
    {
        System.out.println( "Scene --------------" );

        for ( int i = 0; i < theScene.getNumObjects(); ++i )
            System.out.println( theScene.getObject( i ).name + " " + theScene.getObject( i ) );

        System.out.println( "--------------" );
    }


    //}}}

    //{{{ getters and setters
    /**
     *  Gets the scene attribute of the TapProcedure object
     *
     *@return    The scene value
     */
    public Scene getScene()
    {
        return theScene;
    }


    /**
     *  Gets the modules attribute of the TapProcedure object
     *
     *@return    The modules value
     */
    public Vector getModules()
    {
        return modules;
    }


    /**
     *  Gets the moduleIndex attribute of the TapProcedure object
     *
     *@param  module  Description of the Parameter
     *@return         The moduleIndex value
     */
    public int getModuleIndex( TapModule module )
    {
        for ( int i = 0; i < modules.size(); ++i )
            if ( modules.elementAt( i ) == module )
                return i;
        return -1;
    }


    /**
     *  Sets the modules attribute of the TapProcedure object
     *
     *@param  modules  The new modules value
     */
    public void setModules( Vector modules )
    {
        this.modules = modules;

        for ( int i = 0; i < modules.size(); ++i )
            ( (TapModule) modules.elementAt( i ) ).setModules( modules );
    }


    /**
     *  Gets the seed attribute of the TapProcedure object
     *
     *@return    The seed value
     */
    public long getSeed()
    {
        return seed;
    }


    /**
     *  Sets the seed attribute of the TapProcedure object
     *
     *@param  seed  The new seed value
     */
    public void setSeed( long seed )
    {
        this.seed = seed;
        currentObject = null;
        modified = true;
    }


    /**
     *  Gets the window attribute of the TapProcedure object
     *
     *@return    The window value
     */
    public LayoutWindow getWindow()
    {
        return window;
    }


    /**
     *  Sets the window attribute of the TapProcedure object
     *
     *@param  w  The new window value
     */
    public void setWindow( LayoutWindow w )
    {
        window = w;
    }


    /**
     *  Gets the modified attribute of the TapProcedure object
     *
     *@return    The modified value
     */
    public boolean isModified()
    {
        return modified;
    }


    //}}}


    //{{{ Adit levels
    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void editRenderingLevel( BFrame parentFrame )
    {
        EditDecorationLevel fr = new EditDecorationLevel( parentFrame, true );
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void editViewLevel( BFrame parentFrame )
    {
        EditDecorationLevel fr = new EditDecorationLevel( parentFrame, false );
    }


    //}}}

    //{{{ duplicate procedure
    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public TapProcedure duplicate()
    {
        TapProcedure newProcedure = new TapProcedure( null );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try
        {
            theScene.writeToStream( new DataOutputStream( bos ) );

            byte[] bytes = bos.toByteArray();
            Scene newScene = new Scene( new DataInputStream( new ByteArrayInputStream( bytes ) ), true );

            for ( int i = 0; i < modules.size(); ++i )
            {
                TapModule mod = (TapModule) modules.elementAt( i );
                TapModule newMod = null;

                if ( mod instanceof ObjectModule )
                    newMod = ( (ObjectModule) mod ).duplicate( newScene );
                else
                    newMod = mod.duplicate();

                newProcedure.addModule( newMod );

                if ( mod.isMainEntry() )
                    newMod.setMainEntry( true );
            }

            newProcedure.theScene = newScene;
            newProcedure.seed = seed;
            newProcedure.currentObject = null;
            newProcedure.modified = true;
            newProcedure.renderingLevel = renderingLevel;
            newProcedure.viewLevel = viewLevel;
            newProcedure.undoRecord = undoRecord;
            newProcedure.window = window;
            newProcedure.procPanelLayouts = procPanelLayouts;
            newProcedure.procPanel = procPanel;

            if ( bounds != null )
                newProcedure.bounds = new Rectangle( bounds );
            else
                newProcedure.bounds = null;
            return newProcedure;
        }
        catch ( IOException ex )
        {
            System.out.println( "IO exception in procedure scene duplication" );

            return null;
        }
    }


    //}}}

    //{{{ Set procedure links

    /*
     *  set a link from a module to another one
     */
    /**
     *  Sets the link attribute of the TapProcedure object
     *
     *@param  fromModule  The new link value
     *@param  outputPort  The new link value
     *@param  toModule    The new link value
     *@param  inputPort   The new link value
     */
    public void setLink( int fromModule, int outputPort, int toModule, int inputPort )
    {
        if ( modules.isEmpty() )
            System.out.println( "Pocedure.setLink : empty Modules vector" );
        else if ( fromModule >= modules.size() || toModule >= modules.size() )
            System.out.println( "Pocedure.setLink : fromModule or toModule out of bounds " + fromModule + " " + toModule );
        else if ( outputPort >= ( (TapModule) modules.elementAt( fromModule ) ).getNumOutput() || inputPort >= ( (TapModule) modules.elementAt( toModule ) ).getNumInput() )
            System.out.println( "Pocedure.setLink : outputPort or inputPort out of bounds " + fromModule + " " + ( (TapModule) modules.elementAt( fromModule ) ).getNumOutput() + " " + toModule + " " + ( (TapModule) modules.elementAt( toModule ) ).getNumInput() );
        else
            ( (TapModule) modules.elementAt( toModule ) ).setLink( fromModule, inputPort, outputPort );

        modified = true;
    }


    //}}}

    //{{{ adds a module

    /*
     *  adds a module to the procedure
     */
    /**
     *  Adds a feature to the Module attribute of the TapProcedure object
     *
     *@param  additionalModule  The feature to be added to the Module attribute
     */
    public void addModule( TapModule additionalModule )
    {
        modules.setSize( modules.size() + 1 );
        modules.setElementAt( additionalModule, modules.size() - 1 );
        additionalModule.setModules( modules );
        additionalModule.setProcedure( this );
        modified = true;
    }


    //}}}

    //{{{ add modules
    /**
     *  Adds a feature to the Modules attribute of the TapProcedure object
     *
     *@param  externalProc  The feature to be added to the ModulesFromProcedure
     *      attribute
     */
    public void addModulesFromProcedure( TapProcedure externalProc )
    {
        Vector additionalModules = externalProc.getModules();
        Scene externalScene = externalProc.getScene();
        //System.out.println( theScene.getNumObjects() + " " + externalScene.getNumObjects() );
        //for ( int i = 0; i < theScene.getNumObjects(); i++ )
        //    System.out.println( i + ": " + theScene.getObject( i ).object );
        int[] translation = new int[externalScene.getNumObjects()];
        ObjectInfo[] objInfo = new ObjectInfo[externalScene.getNumObjects()];
        for ( int i = 0; i < externalScene.getNumObjects(); i++ )
        {
            ObjectInfo info = externalScene.getObject( i );
            ObjectInfo newInfo = info.duplicate();
            newInfo.object = info.object.duplicate();
            objInfo[i] = addObject( newInfo );
            //System.out.println( i + ": " + newInfo.object );
        }
        for ( int i = 0; i < externalScene.getNumObjects(); i++ )
        {
            translation[i] = theScene.indexOf( objInfo[i] );
        }
        //for ( int i = 0; i < theScene.getNumObjects(); i++ )
        //    System.out.println( i + ": " + theScene.getObject( i ).object );
        int size = modules.size();
        for ( int l = 0; l < additionalModules.size(); ++l )
        {

            TapModule newModule = null;
            if ( additionalModules.elementAt( l ) instanceof ObjectModule )
            {
                newModule = (TapModule) ( (ObjectModule) additionalModules.elementAt( l ) ).duplicate( theScene, translation );

            }
            else
                newModule = ( (TapModule) additionalModules.elementAt( l ) ).duplicate( size );
            if ( newModule != null )
            {
                for ( int i = 0; i < newModule.numOutput; ++i )
                {
                    if ( newModule.linkToIndex[i] != null )
                    {
                        for ( int j = 0; j < newModule.linkToIndex[i].length; ++j )
                        {
                            newModule.linkToIndex[i][j] = newModule.linkToIndex[i][j] + size;
                        }
                    }
                }
                addModule( newModule );
            }

        }
    }


    //{{{ returns the number of AoI objects stored in the scene
    /**
     *  Gets the numObjects attribute of the TapProcedure object
     *
     *@return    The numObjects value
     */
    public int getNumObjects()
    {
        int numObjects = theScene.getNumObjects();

        for ( int i = 0; i < modules.size(); ++i )
            if ( modules.elementAt( i ) instanceof ObjectModule )
                --numObjects;

        return numObjects;
    }


    //}}}

    //{{{ changes the modified state if the TaPD object hold by the procedure
    /**
     *  Sets the modified attribute of the TapProcedure object
     *
     *@param  mod  The new modified value
     */
    public void setModified( boolean mod )
    {
        setModified( true, null );
    }


    /**
     *  Sets the modified attribute of the TapProcedure object
     *
     *@param  mod  The new modified value
     *@param  obj  The new modified value
     */
    public void setModified( boolean mod, TapDesignerObjectCollection obj )
    {
        if ( mod )
        {
            currentObject = null;
            modified = true;
        }
        else
        {
            currentObject = obj;
            modified = false;
        }
    }


    //}}}

    //{{{ a minor change means that a the procedure is modified in a way that doesn't affect the generated object (e.g. renaming a module, changig its location, etc.)
    /**
     *  Description of the Method
     */
    public void notifyMinorChange()
    {
        modified = true;
    }


    //}}}


    //{{{ if the preview of the full TaPD object is asked for, or when the procedure has been duplicated, setting the current object avoids another full calculation of the object
    /**
     *  if the preview of the full TaPD object is asked for, or when the
     *  procedure has been duplicated, setting the current object avoids another
     *  full calculation of the object
     *
     *@param  obj  The new object value
     */
    public void setCurrentObject( TapDesignerObjectCollection obj )
    {
        currentObject = obj;
    }


    //}}}

    //{{{ returns the full TaPD object
    /**
     *  returns the full TaPD object
     *
     *@return    The object value
     */
    public TapDesignerObjectCollection getObject()
    {
        if ( currentObject != null )
        {
            currentObject.setViewLevel( viewLevel );
            currentObject.setRenderingLevel( renderingLevel );

            return currentObject;
        }

        this.initProcedure();

        for ( int i = 0; i < modules.size(); ++i )
            ( (TapModule) modules.elementAt( i ) ).initGenerationProcess();

        for ( int i = 0; i < modules.size(); ++i )
            if ( ( (TapModule) modules.elementAt( i ) ).isMainEntry() )
            {
                currentObject = ( (TapModule) modules.elementAt( i ) ).getObject( -1, seed );
                currentObject.setViewLevel( viewLevel );
                currentObject.setRenderingLevel( renderingLevel );

                return currentObject;
            }

        return null;
    }


    //}}}

    //{{{ returns a partial object asked by a preview up to (clic preview button).
    //This object should not be stored as the the current procedure object
    /**
     *  returns a partial object asked by a preview up to (clic preview button)
     *
     *@return    The temporary object
     */
    public TapDesignerObjectCollection getTempObject()
    {
        this.initProcedure();

        for ( int i = 0; i < modules.size(); ++i )
            ( (TapModule) modules.elementAt( i ) ).initGenerationProcess();

        for ( int i = 0; i < modules.size(); ++i )
            if ( ( (TapModule) modules.elementAt( i ) ).isMainEntry() )
            {
                TapDesignerObjectCollection obj = ( (TapModule) modules.elementAt( i ) ).getObject( -1, seed );
                obj.setViewLevel( -1 );
                obj.setRenderingLevel( -1 );

                return obj;
            }

        return null;
    }


    //}}}

    //{{{ stream read/write
    //reads a procedure from a stream
    /**
     *  Constructor for the TapProcedure object
     *
     *@param  in                          Description of the Parameter
     *@param  sc                          Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     *@exception  ClassNotFoundException  Description of the Exception
     */
    public TapProcedure( DataInputStream in, Scene sc )
        throws IOException, InvalidObjectException, ClassNotFoundException
    {
        int i;
        int count;
        short version;

        version = in.readShort();

        if ( ( version < 0 ) || ( version > 3 ) )
            throw new InvalidObjectException( "" );

        TapBTranslate.setLocale( ArtOfIllusion.getPreferences().getLocale() );
        seed = in.readLong();
        renderingLevel = in.readInt();
        viewLevel = in.readInt();
        theScene = new Scene( in, true );
        count = in.readInt();
        modules = new Vector( count );

        int n;
        for ( i = 0; i < count; ++i )
        {
            String classname = in.readUTF();
            classname = classname.replaceFirst( ".tools.tapDesigner", ".tapDesigner" );
            int len = in.readInt();
            byte[] bytes = new byte[len];
            in.readFully( bytes );

            Class cls = ArtOfIllusion.getClass( classname );

            try
            {
                if ( cls == null )
                    throw new IOException( "Unknown class: " + classname );

                Constructor con = cls.getConstructor( new Class[]
                        {
                        DataInputStream.class, Scene.class
                        } );

                modules.addElement( con.newInstance( new Object[]
                        {
                        new DataInputStream( new ByteArrayInputStream( bytes ) ),
                        theScene
                        } ) );
                /*
                 *  compatibility check : any TapLeaf object is redirected to a LeafModule
                 */
                n = modules.size();
                if ( modules.elementAt( n - 1 ) instanceof AoIObjectModule )
                {
                    AoIObjectModule obj = (AoIObjectModule) modules.elementAt( n - 1 );
                    if ( obj.currentObject.object instanceof TapLeaf )
                    {
                        LeafModule leaf;
                        modules.setElementAt( leaf = new LeafModule( this, obj.getLocation(), obj.currentObject ), n - 1 );
                        leaf.setName( obj.getName() );
                        leaf.portDecoration = obj.portDecoration;
                        leaf.linkToIndex = obj.linkToIndex;
                        leaf.inputPortLink = obj.inputPortLink;
                    }
                }

            }
            catch ( InvocationTargetException ex )
            {
                ex.getTargetException().printStackTrace();
                throw new IOException();
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                throw new IOException();
            }
        }

        for ( i = 0; i < modules.size(); ++i )
        {
            ( (TapModule) modules.elementAt( i ) ).setModules( modules );
            ( (TapModule) modules.elementAt( i ) ).setProcedure( this );
        }

        for ( i = 0; i < modules.size(); ++i )
            ( (TapModule) modules.elementAt( i ) ).remapOutput();

        if ( version >= 1 )
            undoRecordSize = in.readInt();
        else
            undoRecordSize = MAX_UNDO;
        if ( version >= 3 )
        {
            bounds = new Rectangle();
            bounds.x = in.readInt();
            bounds.y = in.readInt();
            bounds.width = in.readInt();
            bounds.height = in.readInt();
            if ( ( bounds.width <= 10 ) || ( bounds.height <= 10 ) )
                bounds = null;
        }
        if ( version >= 2 )
        {
            count = in.readInt();
            if ( count > 0 )
                procPanelLayouts = new ProcPanelLayout[count];
            for ( i = 0; i < count; ++i )
                procPanelLayouts[i] = new ProcPanelLayout( in );
        }
        currentObject = null;
        modified = false;

        //deleteObject(2);
    }


    /*
     *  writes a procedure to stream
     */
    /**
     *  Description of the Method
     *
     *@param  out              Description of the Parameter
     *@param  sc               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void writeToFile( DataOutputStream out, Scene sc )
        throws IOException
    {
        int i;

        out.writeShort( 3 );
        out.writeLong( seed );
        out.writeInt( renderingLevel );
        out.writeInt( viewLevel );
        theScene.writeToStream( out );
        out.writeInt( modules.size() );

        for ( i = 0; i < modules.size(); ++i )
        {
            TapModule mod = (TapModule) modules.elementAt( i );
            out.writeUTF( modules.elementAt( i ).getClass().getName() );

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mod.writeToFile( new DataOutputStream( bos ), theScene );

            byte[] bytes = bos.toByteArray();
            out.writeInt( bytes.length );
            out.write( bytes, 0, bytes.length );
        }

        out.writeInt( undoRecordSize );
        if ( bounds != null )
        {
            out.writeInt( bounds.x );
            out.writeInt( bounds.y );
            out.writeInt( bounds.width );
            out.writeInt( bounds.height );
        }
        else
        {
            for ( i = 0; i < 4; ++i )
                out.writeInt( 0 );
        }

        if ( procPanelLayouts == null )
            out.writeInt( 0 );
        else
        {
            out.writeInt( procPanelLayouts.length );
            for ( i = 0; i < procPanelLayouts.length; ++i )
            {
                procPanelLayouts[i].writeToFile( out );
            }
        }
    }


    //}}}


    //{{{ Edit procedure random seed
    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void editSeed( BFrame parentFrame )
    {
        EditSeedDialog editSeedDialog = new EditSeedDialog( parentFrame );
    }


    //}}}

    //{{{ Back links management

    /*
     *  should be called prior to calculation of full or partial objects.
     *  Initializes the procedure and each module and returns and instance of BackModuleLink
     *  which will make it possible to trace links upstream.
     */
    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public BackModuleLink initProcedure()
    {
        for ( int i = 0; i < modules.size(); ++i )
            ( (TapModule) modules.elementAt( i ) ).initGenerationProcess();

        backLinks = new BackModuleLink( modules, seed );

        return backLinks;
    }


    /**
     *  Gets the backLink attribute of the TapProcedure object
     *
     *@return    The backLink value
     */
    public BackModuleLink getBackLink()
    {
        return backLinks;
    }


    //}}}

    //{{{ Image, texture and material management
    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void importTextures( BFrame parentFrame )
    {
        TapImportFrame importFrame = new TapImportFrame( parentFrame, theScene, window.getScene(), false );
        setModified( true );
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void manageTextures( BFrame parentFrame )
    {
        //theScene.showTexturesDialog( parentFrame );
        //setModified( true );
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void importMaterials( BFrame parentFrame )
    {
        TapImportFrame importFrame = new TapImportFrame( parentFrame, theScene, window.getScene(), true );
        setModified( true );
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void manageMaterials( BFrame parentFrame )
    {
        //theScene.showMaterialsDialog( parentFrame );
        //setModified( true );
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void manageImages( BFrame parentFrame )
    {
        new ImagesDialog( parentFrame, theScene, null );
        setModified( true );
    }


    //}}}

    //{{{ Imported objects management
    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void manageObjects( BFrame parentFrame )
    {
        new TapObjectsFrame( parentFrame, this );
    }


    /**
     *  Description of the Method
     *
     *@param  index  Description of the Parameter
     */
    public void deleteObject( int index )
    {
        if ( ( index >= 0 ) && ( index < theScene.getNumObjects() ) )
            theScene.removeObject( index, null );

        setModified( true );
    }


    //}}}

    //{{{ Live update
    /**
     *  Description of the Method
     */
    public void doLiveUpdate()
    {
        for ( int i = 0; i < modules.size(); ++i )
            ( (TapModule) modules.elementAt( i ) ).updatePreviewFrame();

    }


    //}}}

    //{{{ Undo record buffer management
    /**
     *  Gets the undoRecordSize attribute of the TapProcedure object
     *
     *@return    The undoRecordSize value
     */
    public int getUndoRecordSize()
    {
        return undoRecordSize;
    }


    /**
     *  Sets the undoRecordSize attribute of the TapProcedure object
     *
     *@param  size  The new undoRecordSize value
     */
    public void setUndoRecordSize( int size )
    {
        undoRecordSize = size;
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void editUndoLevel( BFrame parentFrame )
    {
        EditUndoRecordSize fr = new EditUndoRecordSize( parentFrame );
    }


    /**
     *  Description of the Method
     *
     *@param  undoRecord  Description of the Parameter
     */
    public void registerUndoRecord( TapUndoRecord undoRecord )
    {
        this.undoRecord = undoRecord;

    }


    /**
     *  Adds a feature to the UndoRecord attribute of the TapProcedure object
     */
    public void addUndoRecord()
    {
        undoRecord.addRecord( this.duplicate() );
    }


    /**
     *  Adds a feature to the UndoRecord attribute of the TapProcedure object
     *
     *@param  anotherProcedure  The feature to be added to the UndoRecord
     *      attribute
     */
    public void addUndoRecord( TapProcedure anotherProcedure )
    {
        undoRecord.addRecord( anotherProcedure );
    }


    //}}}

    //{{{ Edit seed dialog
    /**
     *  Description of the Class
     *
     *@author     pims
     *@created    4 avril 2004
     */
    private class EditSeedDialog
             extends BDialog
    {
        private BButton okButton;
        private BButton applyButton;
        private BButton cancelButton;
        private long backupSeed;
        TapDesignerObjectCollection backupObject;
        private boolean diagModified;
        private BTextField textField;
        BFrame parentFrame;
        NumberFormat format;


        /**
         *  Constructor for the EditSeedDialog object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public EditSeedDialog( BFrame parentFrame )
        {
            super( parentFrame, TapBTranslate.text( "enterSeedTitle" ), true );
            this.parentFrame = parentFrame;
            backupSeed = seed;
            backupObject = currentObject;

            textField = new BTextField( String.valueOf( backupSeed ), 15 );
            textField.addEventLink( ValueChangedEvent.class, this, "doTextChanged" );
            okButton = TapBTranslate.bButton( "ok", this, "doOK" );
            applyButton = TapBTranslate.bButton( "apply", this, "doApply" );
            cancelButton = TapBTranslate.bButton( "cancel", this, "doCancel" );

            LayoutInfo layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 5, 5, 3, 5 ), new Dimension( 0, 0 ) );
            ColumnContainer cc = new ColumnContainer();
            cc.add( TapBTranslate.bLabel( "enterValueForSeed" ), layout );
            layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 3, 5, 3, 5 ), new Dimension( 0, 0 ) );
            cc.add( textField, layout );
            layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.BOTH, new Insets( 3, 5, 5, 2 ), new Dimension( 0, 0 ) );

            GridContainer gc = new GridContainer( 3, 1 );
            gc.add( okButton, 0, 0, layout );
            gc.add( applyButton, 1, 0, layout );
            gc.add( cancelButton, 2, 0, layout );
            cc.add( gc, layout );
            setContent( cc );
            ( (JDialog) getComponent() ).setLocationRelativeTo( parentFrame.getComponent() );
            pack();
            setVisible( true );

            diagModified = false;
            format = NumberFormat.getInstance();
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
        private void doApply()
        {
            getValues();
            doLiveUpdate();
            currentObject = null;
        }


        /**
         *  Gets the values attribute of the EditSeedDialog object
         */
        private void getValues()
        {
            try
            {
                long dum = ( Long.valueOf( textField.getText() ) ).longValue();
                seed = dum;
            }
            catch ( NumberFormatException e )
            {
                JOptionPane.showMessageDialog( null, TapDesignerTranslate.text( "nonSeedMessage" ), TapDesignerTranslate.text( "error" ), JOptionPane.ERROR_MESSAGE );
            }
        }


        /**
         *  Description of the Method
         */
        private void doOK()
        {
            if ( diagModified )
            {
                getValues();
                addUndoRecord();
            }
            currentObject = null;
            modified = true;
            doLiveUpdate();
            dispose();
        }


        /**
         *  Description of the Method
         */
        public void doTextChanged()
        {
            diagModified = true;

            try
            {
                long dum = ( Long.valueOf( textField.getText() ) ).longValue();

                if ( dum == 0 )
                    ( (JTextField) textField.getComponent() ).setForeground( Color.red );
                else
                    ( (JTextField) textField.getComponent() ).setForeground( Color.black );
            }
            catch ( NumberFormatException e )
            {
                ( (JTextField) textField.getComponent() ).setForeground( Color.red );
            }
        }


        /**
         *  Description of the Method
         */
        private void doCancel()
        {
            if ( diagModified )
            {
                int r = JOptionPane.showConfirmDialog( this.getComponent(), TapBTranslate.text( "parametersModified" ), TapBTranslate.text( "warning" ), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION );

                if ( r == JOptionPane.YES_OPTION )
                    diagModified = false;

            }

            if ( !diagModified )
            {
                seed = backupSeed;
                currentObject = backupObject;
                dispose();
                doLiveUpdate();
            }
        }
    }


    //}}}

    //{{{ Bounds (edit window size
    /**
     *  Gets the bounds attribute of the TapProcedure object
     *
     *@return    The bounds value
     */
    public Rectangle getBounds()
    {
        return bounds;
    }


    /**
     *  Sets the bounds attribute of the TapProcedure object
     *
     *@param  bounds  The new bounds value
     */
    public void setBounds( Rectangle bounds )
    {
        this.bounds = bounds;
    }


    //}}}

//{{{ proc panel and procPanelLayouts

    /**
     *  Sets the procPanelLayouts attribute of the TapProcedure object
     *
     *@param  layouts  The new procPanelLayouts value
     */
    public void setProcPanelLayouts( ProcPanelLayout[] layouts )
    {
        procPanelLayouts = layouts;
    }


    /**
     *  Gets the procPanelLayouts attribute of the TapProcedure object
     *
     *@return    The procPanelLayouts value
     */
    public ProcPanelLayout[] getProcPanelLayouts()
    {
        return procPanelLayouts;
    }


    /**
     *  Sets the procPanel attribute of the TapProcedure object
     *
     *@param  procPanel  The new procPanel value
     */
    public void setProcPanel( TapProcPanel procPanel )
    {
        this.procPanel = procPanel;
    }


    /**
     *  Gets the procPanel attribute of the TapProcedure object
     *
     *@return    The procPanel value
     */
    public TapProcPanel getProcPanel()
    {
        return procPanel;
    }


//}}}

    //{{{ decoration edit dialog
    /**
     *  Description of the Class
     *
     *@author     pims
     *@created    4 avril 2004
     */
    private class EditDecorationLevel
             extends BDialog
    {
        private boolean rendering;
        private BRadioButton displayAll;
        private BRadioButton displayUpTo;
        private RadioButtonGroup group;
        private BButton okButton;
        private BButton cancelButton;
        private int level;
        private BSpinner spinner;
        private BTextField value;


        /**
         *  Constructor for the EditDecorationLevel object
         *
         *@param  parentFrame  Description of the Parameter
         *@param  rendering    Description of the Parameter
         */
        public EditDecorationLevel( BFrame parentFrame, boolean rendering )
        {
            super( parentFrame, rendering ? TapBTranslate.text( "titleRendering" ) : TapBTranslate.text( "titleView" ), true );
            this.rendering = rendering;
            group = new RadioButtonGroup();
            displayAll = TapBTranslate.bRadioButton( "displayAll", true, group, this, "doRadio" );
            displayUpTo = TapBTranslate.bRadioButton( "displayUpTo", false, group );

            if ( rendering )
                level = renderingLevel;
            else
                level = viewLevel;
            //spinner dialog. Sleeping code till things get right in Windows
            /*
             *  if (level>=1) spinner = new BSpinner(level,1,100,1);
             *  else spinner = new BSpinner(1,1,100,1);
             *  if (level>0) group.setSelection(displayUpTo);
             *  else spinner.setEnabled(false);
             */
            if ( level > 0 )
            {
                value = new BTextField( String.valueOf( level ), 5 );
                group.setSelection( displayUpTo );
            }
            else
            {
                value = new BTextField( String.valueOf( 1 ), 5 );
                value.setEnabled( false );
            }

            value.addEventLink( ValueChangedEvent.class, this, "doValueChanged" );
            okButton = TapBTranslate.bButton( "ok", this, "doOK" );
            cancelButton = TapBTranslate.bButton( "cancel", this, "doCancel" );

            GridContainer gc = new GridContainer( 2, 3 );
            LayoutInfo layout = new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 0, 0, 0, 0 ), new Dimension( 0, 0 ) );
            gc.add( displayAll, 0, 0, layout );

            gc.add( displayUpTo, 0, 1, layout );
            gc.add( value, 1, 1 );
            layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 2, 0, 4, 0 ), new Dimension( 0, 0 ) );
            gc.add( okButton, 0, 2, layout );
            gc.add( cancelButton, 1, 2, layout );
            setContent( gc );
            pack();
            ( (JDialog) getComponent() ).setLocationRelativeTo( parentFrame.getComponent() );
            setVisible( true );
            addEventLink( WindowClosingEvent.class, this, "doCancel" );
        }


        /**
         *  Description of the Method
         *
         *@param  evt  Description of the Parameter
         */
        private void doValueChanged( ValueChangedEvent evt )
        {
            BTextField tf = (BTextField) evt.getWidget();
            int dum;

            try
            {
                dum = Integer.parseInt( tf.getText() );

                if ( dum < 1 )
                    ( (JTextField) tf.getComponent() ).setForeground( Color.red );
                else
                    ( (JTextField) tf.getComponent() ).setForeground( Color.black );
            }
            catch ( NumberFormatException ex )
            {
                ( (JTextField) tf.getComponent() ).setForeground( Color.red );
            }
        }


        /**
         *  Description of the Method
         */
        private void doOK()
        {
            //level = ((Integer)spinner.getValue()).intValue();
            try
            {
                level = Integer.parseInt( value.getText() );
            }
            catch ( NumberFormatException ex )
            {
                level = 1;
            }

            if ( level < 1 )
                level = 1;

            modified = true;

            if ( displayUpTo.getState() )
            {
                if ( rendering )
                    renderingLevel = level;
                else
                    viewLevel = level;
            }
            else
            {
                if ( rendering )
                    renderingLevel = -1;
                else
                    viewLevel = -1;
            }

            dispose();
        }


        /**
         *  Description of the Method
         */
        private void doCancel()
        {
            dispose();
        }


        /**
         *  Description of the Method
         */
        private void doRadio()
        {
            if ( displayUpTo.getState() )
                value.setEnabled( true );
            else
                value.setEnabled( false );
        }
    }


    //}}}

    //{{{ Undo record size edit dialog
    /**
     *  Description of the Class
     *
     *@author     pims
     *@created    4 avril 2004
     */
    private class EditUndoRecordSize
             extends BDialog
    {
        private BButton okButton;
        private BButton cancelButton;
        private int level;
        private BSpinner spinner;
        private BTextField value;


        /**
         *  Constructor for the EditUndoRecordSize object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public EditUndoRecordSize( BFrame parentFrame )
        {
            super( parentFrame, TapBTranslate.text( "editUndoMaxLevel" ), true );

            value = new BTextField( String.valueOf( undoRecordSize - 1 ), 5 );
            value.addEventLink( ValueChangedEvent.class, this, "doValueChanged" );

            okButton = TapBTranslate.bButton( "ok", this, "doOK" );
            cancelButton = TapBTranslate.bButton( "cancel", this, "doCancel" );

            ColumnContainer cc = new ColumnContainer();
            RowContainer rc = new RowContainer();
            LayoutInfo layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 7, 4, 0, 4 ), new Dimension( 0, 0 ) );
            rc.add( TapBTranslate.bLabel( "undoLevel" ), layout );
            rc.add( value, layout );
            cc.add( rc, layout );
            rc = new RowContainer();
            layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 0, 4, 7, 4 ), new Dimension( 0, 0 ) );
            rc.add( okButton, layout );
            rc.add( cancelButton, layout );
            cc.add( rc, layout );
            setContent( cc );
            pack();
            ( (JDialog) getComponent() ).setLocationRelativeTo( parentFrame.getComponent() );
            setVisible( true );
            addEventLink( WindowClosingEvent.class, this, "doCancel" );
        }


        /**
         *  Description of the Method
         */
        private void doOK()
        {
            try
            {
                undoRecordSize = Integer.parseInt( value.getText() );

                if ( undoRecordSize < 1 )
                    undoRecordSize = 2;
                else
                    ++undoRecordSize;
            }
            catch ( NumberFormatException ex )
            {
                undoRecordSize = MAX_UNDO;
            }

            dispose();
        }


        /**
         *  Description of the Method
         */
        private void doCancel()
        {
            dispose();
        }


        /**
         *  Description of the Method
         *
         *@param  evt  Description of the Parameter
         */
        private void doValueChanged( ValueChangedEvent evt )
        {
            int dum;

            try
            {
                dum = Integer.parseInt( value.getText() );

                if ( dum < 1 )
                    ( (JTextField) value.getComponent() ).setForeground( Color.red );
                else
                    ( (JTextField) value.getComponent() ).setForeground( Color.black );
            }
            catch ( NumberFormatException ex )
            {
                ( (JTextField) value.getComponent() ).setForeground( Color.red );
            }
        }
    }
    //}}}
}

