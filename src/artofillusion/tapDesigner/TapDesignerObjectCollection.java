/*
 *  This class extend the ObjectCollection AoI class. It allows the tree or plant object to be saved in an AoI file,
 *  displayed in the scene and rendered.
 */
/*
 *  Copyright 2003 François Guillet
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
import artofillusion.animation.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import java.io.*;
import java.util.*;


/**
 *  Description of the Class
 *
 *@author     François Guillet
 *@created    9 mai 2004
 */
public class TapDesignerObjectCollection extends ObjectCollection
{
    private List<ObjectInfo> objectInfoList;
    private List<TapObjectInfo> tapObjectInfoList;
    private List<ObjectInfo> renderInfoVector;
    private TapProcedure procedure;
    private int decorationLevel;
    private int renderingLevel;


    /**
     *  Constructor for the TapDesignerObjectCollection object
     *
     *@param  procedure  Description of the Parameter
     */
    public TapDesignerObjectCollection( TapProcedure procedure )
    {
        super();
        objectInfoList = new ArrayList<>();
        renderInfoVector = new ArrayList<>();
        tapObjectInfoList = new ArrayList();
        this.procedure = procedure;
        decorationLevel = -1;
        //Show everything
        renderingLevel = -1;
    }


    /**
     *  Adds a feature to the Object attribute of the
     *  TapDesignerObjectCollection object
     *
     *@param  anObject  The feature to be added to the Object attribute
     */
    public void addObject( TapObjectInfo anObject )
    {
        tapObjectInfoList.add( anObject );

        if ( ( anObject.getDecorationLevel() < decorationLevel ) || ( decorationLevel <= 0 ) )
            objectInfoList.add( anObject.objectInfo );

        if ( ( anObject.getDecorationLevel() < renderingLevel ) || ( renderingLevel <= 0 ) )
            renderInfoVector.add( anObject.objectInfo );
    }


    /**
     *  Adds a feature to the Object attribute of the
     *  TapDesignerObjectCollection object
     *
     *@param  anObject  The feature to be added to the Object attribute
     *@param  level     The feature to be added to the Object attribute
     *@param  name      The feature to be added to the Object attribute
     */
    public void addObject( ObjectInfo anObject, int level, String name )
    {
        addObject( anObject, level, 1.0, 1.0, name );
    }


    /**
     *  Adds a feature to the Object attribute of the
     *  TapDesignerObjectCollection object
     *
     *@param  anObject  The feature to be added to the Object attribute
     *@param  level     The feature to be added to the Object attribute
     *@param  sizeR     The feature to be added to the Object attribute
     *@param  sizeY     The feature to be added to the Object attribute
     *@param  name      The feature to be added to the Object attribute
     */
    public void addObject( ObjectInfo anObject, int level, double sizeR, double sizeY, String name )
    {
        TapObjectInfo newObject = new TapObjectInfo( anObject );
        newObject.setDecorationLevel( level );
        tapObjectInfoList.add( newObject );

        if ( ( newObject.getDecorationLevel() < decorationLevel ) || ( decorationLevel <= 0 ) )
            objectInfoList.add( newObject.objectInfo );

        if ( ( newObject.getDecorationLevel() < renderingLevel ) || ( renderingLevel <= 0 ) )
            renderInfoVector.add( newObject.objectInfo );

        newObject.sizeR = sizeR;
        newObject.sizeY = sizeY;
        newObject.name = name;
    }


    /**
     *  Sets the viewLevel attribute of the TapDesignerObjectCollection object
     *
     *@param  level  The new viewLevel value
     */
    public void setViewLevel( int level )
    {
        decorationLevel = level;

        objectInfoList.clear();

        for (TapObjectInfo info: tapObjectInfoList)
        {
            if (info.getDecorationLevel() < level  || ( level <= 0 ) )
                objectInfoList.add(info.objectInfo );
        }
    }


    /**
     *  Sets the renderingLevel attribute of the TapDesignerObjectCollection
     *  object
     *
     *@param  level  The new renderingLevel value
     */
    public void setRenderingLevel( int level )
    {
        renderingLevel = level;

        renderInfoVector.clear();

        for (TapObjectInfo info: tapObjectInfoList)
            if ( info.getDecorationLevel() < level || ( level <= 0 ) )
                renderInfoVector.add( info.objectInfo );
    }


    /**
     *  Description of the Method
     *
     *@param  collection  Description of the Parameter
     *@param  levelDiff   Description of the Parameter
     */
    public void mergeCollection( TapDesignerObjectCollection collection, int levelDiff )
    {
        mergeCollection( collection, 0, collection.tapObjectInfoList.size() - 1, levelDiff );
    }


    /**
     *  Description of the Method
     *
     *@param  collection  Description of the Parameter
     *@param  from        Description of the Parameter
     *@param  to          Description of the Parameter
     *@param  levelDiff   Description of the Parameter
     */
    public void mergeCollection( TapDesignerObjectCollection collection, int from, int to, int levelDiff )
    {
        if ( collection.tapObjectInfoList.isEmpty() )
            return;

        if ( to > collection.tapObjectInfoList.size() - 1 )
            to = collection.tapObjectInfoList.size() - 1;

        if ( from < 0 )
            from = 0;

        for ( int i = from; i <= to; ++i )
        {
            TapObjectInfo anObject = collection.tapObjectInfoList.get( i );
            tapObjectInfoList.add( anObject );

            if ( anObject.decorationLevel >= 0 )
                anObject.decorationLevel += levelDiff;
            else
                anObject.decorationLevel -= levelDiff;

            if ( ( anObject.getDecorationLevel() < decorationLevel ) || ( decorationLevel <= 0 ) )
                objectInfoList.add( anObject.objectInfo );

            if ( ( anObject.getDecorationLevel() < renderingLevel ) || ( renderingLevel <= 0 ) )
                renderInfoVector.add( anObject.objectInfo );
        }
    }


    /**
     *  Constructor for the TapDesignerObjectCollection object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     *@exception  ClassNotFoundException  Description of the Exception
     */
    public TapDesignerObjectCollection( DataInputStream in, Scene theScene ) throws IOException, InvalidObjectException, ClassNotFoundException
    {
        super( in, theScene );

        short version;

        TapDesignerTranslate.setLocale( Translate.getLocale() );
        version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        procedure = new TapProcedure( in, theScene );
        decorationLevel = in.readInt();
        renderingLevel = in.readInt();

        TapDesignerObjectCollection obj = procedure.getObject();
        tapObjectInfoList = obj.tapObjectInfoList;
        objectInfoList = obj.objectInfoList;
        renderInfoVector = obj.renderInfoVector;
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
        procedure.writeToFile( out, theScene );
        out.writeInt( decorationLevel );
        out.writeInt( renderingLevel );
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public int size()
    {
        return tapObjectInfoList.size();
    }


    /**
     *  Description of the Method
     *
     *@param  index  Description of the Parameter
     *@return        Description of the Return Value
     */
    public TapObjectInfo elementAt( int index )
    {
        return tapObjectInfoList.get( index );
    }


    /**
     *  Description of the Method
     *
     *@param  info         Description of the Parameter
     *@param  interactive  Description of the Parameter
     *@param  scene        Description of the Parameter
     *@return              Description of the Return Value
     */
    @Override
    protected Enumeration enumerateObjects( ObjectInfo info, boolean interactive, Scene scene )
    {
        if ( interactive )
            return Collections.enumeration(objectInfoList);
        else
            return Collections.enumeration(renderInfoVector);
    }


    /**
     *  Return a Keyframe which describes the current pose of this object.
     *
     *@return    The poseKeyframe value
     */
    @Override
    public Keyframe getPoseKeyframe()
    {
        return null;
    }


    /**
     *  Modify this object based on a pose keyframe.
     *
     *@param  k  Description of the Parameter
     */
    @Override
    public void applyPoseKeyframe( Keyframe k )
    {
    }


    /**
     *  Description of the Method
     *
     *@param  obj  Description of the Parameter
     */
    @Override
    public void copyObject( Object3D obj )
    {
        copyObject( obj, true );

    }


    /**
     *  Description of the Method
     *
     *@param  obj        Description of the Parameter
     *@param  duplicate  Description of the Parameter
     */
    public void copyObject( Object3D obj, boolean duplicate )
    {
        TapDesignerObjectCollection tmpObject = (TapDesignerObjectCollection) obj;

        if ( duplicate )
            tmpObject = (TapDesignerObjectCollection) ( (TapDesignerObjectCollection) obj ).duplicate();

        procedure = tmpObject.procedure;
        decorationLevel = tmpObject.decorationLevel;
        renderingLevel = tmpObject.renderingLevel;
        objectInfoList = tmpObject.objectInfoList;
        renderInfoVector = tmpObject.renderInfoVector;
        tapObjectInfoList = tmpObject.tapObjectInfoList;
        setTexture( obj.getTexture(), obj.getTextureMapping() );
        setMaterial( obj.getMaterial(), obj.getMaterialMapping() );
        cachedBounds = null;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public Object3D duplicate()
    {
        TapDesignerObjectCollection obj = null;

        obj = new TapDesignerObjectCollection( procedure.duplicate() );
        obj.decorationLevel = this.decorationLevel;
        obj.renderingLevel = this.renderingLevel;
        obj.setTexture( this.getTexture(), this.getTextureMapping() );
        obj.setMaterial( this.getMaterial(), this.getMaterialMapping() );

        //TapDesignerObjectCollection dum = obj.procedure.getObject();
        obj.tapObjectInfoList = this.tapObjectInfoList;
        obj.objectInfoList = this.objectInfoList;
        obj.renderInfoVector = this.renderInfoVector;

        return (Object3D) obj;
    }


    /**
     *  Sets the size attribute of the TapDesignerObjectCollection object
     *
     *@param  xsize  The new size value
     *@param  ysize  The new size value
     *@param  zsize  The new size value
     */
    @Override
    public void setSize( double xsize, double ysize, double zsize )
    {
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public boolean canSetMaterial()
    {
        return false;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public boolean canSetTexture()
    {
        return false;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public boolean isEditable()
    {
        return true;
    }


    /**
     *  Gets the bounds attribute of the TapDesignerObjectCollection object
     *
     *@return    The bounds value
     */
    @Override
    public BoundingBox getBounds()
    {
        return cachedBounds;
    }


    /**
     *  Description of the Method
     *
     *@param  parent  Description of the Parameter
     *@param  info    Description of the Parameter
     *@param  cb      Description of the Parameter
     */
    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void edit( final EditingWindow parent, final ObjectInfo info, Runnable cb )
    {
        if ( parent instanceof LayoutWindow ) new TapFrame( (LayoutWindow) parent, info );

    }


    /**
     *  Gets the procedure attribute of the TapDesignerObjectCollection object
     *
     *@return    The procedure value
     */
    public TapProcedure getProcedure()
    {
        return procedure;
    }


    /**
     *  Sets the procedure attribute of the TapDesignerObjectCollection object
     *
     *@param  procedure  The new procedure value
     */
    public void setProcedure( TapProcedure procedure )
    {
        this.procedure = procedure;
    }


    /*
     *  this method returns an array of plain AoI objects to be copied into AoI clipboard
     */
    /**
     *  Gets the aoIObjects attribute of the TapDesignerObjectCollection object
     *
     *@return    The aoIObjects value
     */
    ObjectInfo[] getAoIObjects()
    {
        ObjectInfo[] objects = new ObjectInfo[tapObjectInfoList.size()];
        Vector parents = new Vector();
        int currentLevel = 0;
        int level;
        ObjectInfo currentParent = null;
        int[] levelCount = new int[15];

        for ( int i = 0; i < tapObjectInfoList.size(); ++i )
        {
            TapObjectInfo tInfo = tapObjectInfoList.get( i );
            ObjectInfo info = tInfo.objectInfo.duplicate();

            if ( info.object instanceof TapObject )
                info.object = ( (TapObject) info.object ).getPlainAoIObject();
            else
                info.object = info.object.duplicate();

            info.name = tInfo.name;
            info.addTrack( new PositionTrack( info ), 0 );
            info.addTrack( new RotationTrack( info ), 1 );
            objects[i] = info;

            if ( i == 0 )
            {
                parents.add( objects[0] );
                currentLevel = 0;
                currentParent = objects[0];
            }
            else
            {
                level = tInfo.getDecorationLevel();

                if ( level >= levelCount.length )
                {
                    int[] newLevelCount = new int[levelCount.length + 15];

                    for ( int j = 0; j < levelCount.length; ++j )
                        newLevelCount[j] = levelCount[j];

                    for ( int j = levelCount.length; j < newLevelCount.length; ++j )
                        newLevelCount[j] = 0;

                    levelCount = newLevelCount;
                }

                ++levelCount[level];
                info.name = info.name + "_" + String.valueOf( levelCount[level] );

                if ( level > currentLevel )
                {
                    if ( level >= parents.size() )
                        parents.setSize( level + 1 );

                    parents.setElementAt( objects[i], level );
                    currentParent = (ObjectInfo) parents.elementAt( currentLevel );
                    currentLevel = level;
                }
                else if ( level < currentLevel )
                {
                    currentLevel = level;
                    --level;

                    while ( parents.elementAt( level ) == null )
                        --level;

                    currentParent = (ObjectInfo) parents.elementAt( level );
                    parents.setElementAt( objects[i], currentLevel );
                }

                currentParent.addChild( objects[i], currentParent.children.length );
            }
        }

        return objects;
    }
}

