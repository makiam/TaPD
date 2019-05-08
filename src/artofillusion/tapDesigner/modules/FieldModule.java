/*
 *  This module decorates the faces of a triangle mesh with objects
 *  This process can be used to populate a field with grass and plants.
 */
/*
 *  Copyright (C) 2004 by Francois Guillet
 *  Changes copyright (C) 2019 by Maksim Khramov
 *
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.tapDesigner;

import artofillusion.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.object.TriangleMesh.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import artofillusion.tapDesigner.TapModule.*;


/**
 *  This module decorates the faces of a triangle mesh with objects
 *
 *@author     Francois Guillet
 *@created    16 july 2004
 */
public class FieldModule extends TapModule
{
    private FieldModule module;
    private static TapModule.ModuleTypeInfo typeInfo;

    private double occupancy;
    private double jitter;
    private double rotMean, rotDev;
    private short rotDistType;
    private boolean randomRotation;
    private double inward;
    private int estimate;
    private short coverType;
    private boolean followMeshOrientation;
    private boolean sizeChildren;
    private double sizeDist;
    private short sizeDistType;
    private TapDistortParameters parms;
    private final static short UNIFORM = 0;
    private final static short VERTICES = 1;
    private final static short FACE_CENTERS = 2;
    private final static short QUAD_CENTERS = 3;



    /**
     *  Constructor for the FieldModule object
     *
     *@param  procedure  The tapDesigner procedure this module is attached to
     *@param  position   The location of the module
     */
    public FieldModule( TapProcedure procedure, Point position )
    {
        super(procedure, TapBTranslate.text( "field" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "fieldName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/field_tree.png" ) ) );

        setNumInput( 2 );
        setNumOutput( 1 );
        occupancy = 1;
        jitter = 0;
        inward = 0;
        coverType = VERTICES;
        followMeshOrientation = false;
        sizeChildren = true;
        sizeDist = 0;
        sizeDistType = TapRandomGenerator.UNIFORM;
        estimate = 1;
        rotMean = 0;
        rotDev = 180;
        randomRotation = false;
        rotDistType = TapRandomGenerator.UNIFORM;
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
     *  Constructor for the FieldModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public FieldModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );
        occupancy = in.readDouble();
        jitter = in.readDouble();
        inward = in.readDouble();
        estimate = in.readInt();
        coverType = in.readShort();
        followMeshOrientation = in.readBoolean();
        sizeChildren = in.readBoolean();
        sizeDist = in.readDouble();
        sizeDistType = in.readShort();
        rotMean = in.readDouble();
        rotDev = in.readDouble();
        randomRotation = in.readBoolean();
        rotDistType = in.readShort();
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
        out.writeDouble( occupancy );
        out.writeDouble( jitter );
        out.writeDouble( inward );
        out.writeInt( estimate );
        out.writeShort( coverType );
        out.writeBoolean( followMeshOrientation );
        out.writeBoolean( sizeChildren );
        out.writeDouble( sizeDist );
        out.writeShort( sizeDistType );
        out.writeDouble( rotMean );
        out.writeDouble( rotDev );
        out.writeBoolean( randomRotation );
        out.writeShort( rotDistType );
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public TapModule duplicate()
    {
        FieldModule module = new FieldModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
//        module.rangeFrom = this.rangeFrom;
//        module.rangeTo = this.rangeTo;
        module.occupancy = this.occupancy;
        module.jitter = this.jitter;
        module.inward = this.inward;
        module.estimate = this.estimate;
        module.coverType = this.coverType;
        module.followMeshOrientation = this.followMeshOrientation;
        module.sizeChildren = this.sizeChildren;
        module.sizeDist = this.sizeDist;
        module.sizeDistType = this.sizeDistType;
        module.rotMean = this.rotMean;
        module.rotDev = this.rotDev;
        module.randomRotation = this.randomRotation;
        module.rotDistType = this.rotDistType;
        return (TapModule) module;
    }


    /**
     *  Description of the Method
     *
     *@param  inputPort  Description of the Parameter
     *@return            Description of the Return Value
     */
    @Override
    public int remap( int inputPort )
    {
        return inputPort;
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


    /**
     *  Gets the object attribute of the GoldenBallModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The object value
     */
    @Override
    public TapDesignerObjectCollection getObject( int outputPort, long seed )
    {
        TapDesignerObjectCollection col = null;
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
        //preview up to
        else if ( outputPort == -2 )
        {
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
                    TapModule mod = (TapModule) modules.elementAt( linkToIndex[0][j] );
                    TapDesignerObjectCollection modCol = mod.getObject( col, inputPortLink[0][j], gen.getSeed() );
                    if ( modCol != null )
                        col.mergeCollection( modCol, 1 );
                }
            }
            return col;
        }
        //A field module is not asked;
        else
            return null;
    }


    /**
     *  Gets a "decorated decorator" object to place on the mesh
     *
     *@param  gen       Description of the Parameter
     *@param  sR        Description of the Parameter
     *@param  sY        Description of the Parameter
     *@param  parms     Description of the Parameter
     *@param  level     Description of the Parameter
     *@param  evaluate  Description of the Parameter
     *@return           The decoratedDecorator value
     */
    private TapDesignerObjectCollection getDecoratedDecorator( TapRandomGenerator gen, double sR, double sY, TapDistortParameters parms, int level, boolean evaluate )
    {
        TapDesignerObjectCollection tmpCollection = null;
        TapDesignerObjectCollection col = null;
        boolean duplicate;
        ObjectInfo anInfo;
        double sizeR;
        double sizeY;
        double dum;
        TapDistortParameters tmpParms;
        Vec3 size;
        String objName;

        BackModuleLink backLinks = procedure.getBackLink();
        BackModuleLink.BackLink bl = backLinks.findModule( this, 0 );
        if ( bl != null )
        {
            col = bl.fromModule.getObject( bl.outputPort, gen.getSeed() );
            anInfo = col.elementAt( 0 ).objectInfo;
            if ( bl.fromModule instanceof ObjectModule )
                duplicate = ( (ObjectModule) bl.fromModule ).isDuplicate();
            else
                duplicate = false;

            objName = bl.fromModule.getName();
            sizeR = 1.0;
            sizeY = 1.0;

            if ( !duplicate )
            {
                if ( sizeChildren )
                {
                    sizeR *= sR;
                    sizeY *= sY;
                }

                if ( sizeDist > 0 )
                {
                    dum = gen.getDistribution( 0, sizeDist, sizeDistType );

                    if ( dum < 0.0001 )
                        dum = 0.0001;

                    sizeR *= ( 1 - dum );
                    sizeY *= ( 1 - dum );
                }

                if ( ( sizeR > 0 ) && ( sizeY > 0 ) )
                {
                    size = anInfo.object.getBounds().getSize();

                    if ( anInfo.object instanceof SplineMesh )
                    {
                        anInfo.object.setSize( size.x * sizeR, size.y * sizeY, size.z * sizeR );
                        TapUtils.setObjectAtMinY( anInfo );
                        //if ( parms != null )
                        //TapUtils.distortSplineMesh( (SplineMesh) anInfo.object, parms );
                    }
                    else if ( !( anInfo.object instanceof TapObject ) )
                    {
                        anInfo.object.setSize( size.x * sizeR, size.y * sizeY, size.z * sizeR );
                        TapUtils.setObjectAtMinY( anInfo );
                    }
                    else
                        ( (TapObject) anInfo.object ).resizeAndDistort( size, sizeR, sizeY, null );
                }
            }

            anInfo = col.elementAt( 0 ).objectInfo;
            tmpCollection = new TapDesignerObjectCollection( procedure );
            tmpCollection.addObject( anInfo, level + 1, sizeR, sizeY, objName );

            if ( ( inputPortLink[0] != null ) && ( evaluate || ( !stopHere ) ) )
            {
                for ( int j = 0; j < inputPortLink[0].length; ++j )
                {
                    TapModule mod = (TapModule) modules.elementAt( linkToIndex[0][j] );
                    TapDesignerObjectCollection modCol = mod.getObject( tmpCollection, inputPortLink[0][j], gen.getSeed() );

                    if ( modCol != null )
                        tmpCollection.mergeCollection( modCol, 0 );
                }
            }
        }

        return tmpCollection;
    }


    /**
     *  Gets the object attribute of the GoldenBallModule object
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
        Mat4 m;
        Mat4 mrot = null;
        ObjectInfo anInfo;
        ObjectInfo mainObject;
        ObjectInfo dummyInfo;
        int level;
        int count;
        double sizeR;
        double sizeY;
        Vec3 size;
        double objectArea = 0.0;
        Vec3 yVec = new Vec3( 0, 1, 0 );

        CoordinateSystem dummyCS = new CoordinateSystem();
        CoordinateSystem coords = null;
        TapDesignerObjectCollection col = null;
        TapDesignerObjectCollection tmpCollection = null;
        level = collection.elementAt( 0 ).getDecorationLevel();
        TapRandomGenerator gen = new TapRandomGenerator( seed );
        BackModuleLink.BackLink bl;
        double dum;
        double dumr;
        Vec3 tr = null;
        Vec3 jitterVec = null;
        Vec3 faceNorm = null;
        Vec3 pos = null;

        //double[] yVal = new double[1];

        if ( inputPort == 1 )
        {
            mainObject = collection.elementAt( 0 ).objectInfo;
            //yVec = mainObject.coords.fromLocal().timesDirection( yVec );
            if ( !( mainObject.object instanceof TriangleMesh ) )
            {
                System.out.println( "Not a triangle mesh !" );
                return null;
            }
            TriangleMesh triMesh = (TriangleMesh) mainObject.object;
            mainObject.coords = new CoordinateSystem();
            TapDesignerObjectCollection newCollection = new TapDesignerObjectCollection( procedure );
            BackModuleLink backLinks = procedure.getBackLink();
            size = mainObject.object.getBounds().getSize();
            sizeR = collection.elementAt( 0 ).sizeR;
            sizeY = collection.elementAt( 0 ).sizeY;
            Ysize = size.y;
            if ( coverType == UNIFORM )
            {
                //find out maximum size
                objectArea = 0;
                count = 0;
                for ( int i = 0; i < estimate; ++i )
                {
                    tmpCollection = getDecoratedDecorator( gen, sizeR, sizeY, null, level, true );
                    if ( tmpCollection != null )
                    {
                        dummyInfo = new ObjectInfo( tmpCollection, dummyCS, "dummy" );
                        tmpCollection.sceneChanged( dummyInfo, procedure.getScene() );
                        //trick the Object Collection for calculating size
                        size = tmpCollection.getBounds().getSize();
                        dum = size.x;
                        if ( dum < 0.005 )
                            dum = 0.005;
                        if ( size.z > dum )
                            dum = size.z;
                        ++count;
                        objectArea += dum;
                    }
                }
                if ( objectArea < 0.00001 )
                    return null;
                objectArea /= count;
            }
            MeshVertex[] v = triMesh.getVertices();
            Vec3[] normals = triMesh.getNormals();
            Face[] faces = triMesh.getFaces();
            Edge[] edges = triMesh.getEdges();
            int numVert = v.length;
            int numFaces = faces.length;
            tr = new Vec3( 0, -inward, 0 );
            switch ( coverType )
            {
                case VERTICES:
                    for ( int i = 0; i < numVert; ++i )
                    {
                        dum = gen.uniformDeviate();
                        if ( dum <= occupancy )
                        {
                            if ( jitter > 0 )
                            {
                                dum = gen.uniformDeviate() * Math.PI * 2;
                                dumr = gen.uniformDeviate() * jitter;
                                jitterVec = new Vec3( dumr * Math.cos( dum ), 0, dumr * Math.sin( dum ) );
                            }
                            if ( randomRotation )
                            {
                                mrot = Mat4.axisRotation( yVec, gen.getDistribution( rotMean, rotDev, rotDistType ) );
                            }
                            m = Mat4.axisRotation( yVec.cross( normals[i] ), Math.acos( yVec.dot( normals[i] ) ) );
                            tmpCollection = getDecoratedDecorator( gen, sizeR, sizeY, null, level, false );
                            count = tmpCollection.size();
                            for ( int j = 0; j < count; ++j )
                            {
                                anInfo = tmpCollection.elementAt( j ).objectInfo;
                                coords = anInfo.coords;
                                coords.setOrigin( coords.getOrigin().plus( tr ) );
                                if ( randomRotation )
                                    coords.transformCoordinates( mrot );
                                if ( jitter > 0 )
                                    coords.setOrigin( coords.getOrigin().plus( jitterVec ) );
                                if ( followMeshOrientation )
                                    coords.transformCoordinates( m );
                                coords.setOrigin( coords.getOrigin().plus( v[i].r ) );
                                //coords.transformOrigin( mainObject.coords.fromLocal() );
                            }
                            newCollection.mergeCollection( tmpCollection, 0 );
                        }
                    }
                    break;
                case FACE_CENTERS:
                    for ( int i = 0; i < numFaces; ++i )
                    {
                        if ( occupancy < 1.0 )
                            dum = gen.uniformDeviate();
                        else
                            dum = 0;
                        if ( dum <= occupancy )
                        {
                            if ( jitter > 0 )
                            {
                                dum = gen.uniformDeviate() * Math.PI * 2;
                                dumr = gen.uniformDeviate() * jitter;
                                jitterVec = new Vec3( dumr * Math.cos( dum ), 0, dumr * Math.sin( dum ) );
                            }
                            if ( randomRotation )
                            {
                                mrot = Mat4.axisRotation( yVec, gen.getDistribution( rotMean, rotDev, rotDistType ) );
                            }
                            faceNorm = v[faces[i].v2].r.minus( v[faces[i].v1].r ).cross( v[faces[i].v3].r.minus( v[faces[i].v1].r ) );
                            faceNorm.normalize();
                            pos = v[faces[i].v1].r.plus( v[faces[i].v2].r.plus( v[faces[i].v3].r ) );
                            pos.scale( 1.0 / 3.0 );
                            m = Mat4.axisRotation( yVec.cross( faceNorm ), Math.acos( yVec.dot( faceNorm ) ) );
                            tmpCollection = getDecoratedDecorator( gen, sizeR, sizeY, null, level, false );
                            count = tmpCollection.size();
                            for ( int j = 0; j < count; ++j )
                            {
                                anInfo = tmpCollection.elementAt( j ).objectInfo;
                                coords = anInfo.coords;
                                coords.setOrigin( coords.getOrigin().plus( tr ) );
                                if ( randomRotation )
                                    coords.transformCoordinates( mrot );
                                if ( jitter > 0 )
                                    coords.setOrigin( coords.getOrigin().plus( jitterVec ) );
                                if ( followMeshOrientation )
                                    coords.transformCoordinates( m );
                                coords.setOrigin( coords.getOrigin().plus( pos ) );
                                coords.transformOrigin( mainObject.coords.fromLocal() );
                            }
                            newCollection.mergeCollection( tmpCollection, 0 );
                        }
                    }
                    break;
                case QUAD_CENTERS:
                    boolean[] done = new boolean[numFaces];
                    int le;
                    int otherFace;
                    for ( int i = 0; i < numFaces; ++i )
                    {
                        //find the longest edge.
                        le = faces[i].e1;
                        if ( TapUtils.edgeLength( edges[le], v ) < TapUtils.edgeLength( edges[faces[i].e2], v ) )
                            le = faces[i].e2;
                        if ( TapUtils.edgeLength( edges[le], v ) < TapUtils.edgeLength( edges[faces[i].e3], v ) )
                            le = faces[i].e3;
                        //find the other face shared by this edge
                        otherFace = edges[le].f2;
                        if ( otherFace == i )
                            otherFace = edges[le].f1;
                        if ( !done[i] )
                        {
                            //done for both faces.
                            done[i] = true;
                            if ( otherFace >= 0 )
                                done[otherFace] = true;
                            if ( occupancy < 1.0 )
                                dum = gen.uniformDeviate();
                            else
                                dum = 0;
                            if ( dum <= occupancy )
                            {
                                pos = v[edges[le].v2].r.plus( v[edges[le].v1].r );
                                pos.scale( 0.5 );
                                faceNorm = normals[edges[le].v1].plus( normals[edges[le].v2] );
                                faceNorm.normalize();
                                if ( jitter > 0 )
                                {
                                    dum = gen.uniformDeviate() * Math.PI * 2;
                                    dumr = gen.uniformDeviate() * jitter;
                                    jitterVec = new Vec3( dumr * Math.cos( dum ), 0, dumr * Math.sin( dum ) );
                                }
                                if ( randomRotation )
                                {
                                    mrot = Mat4.axisRotation( yVec, gen.getDistribution( rotMean, rotDev, rotDistType ) );
                                }
                                m = Mat4.axisRotation( yVec.cross( faceNorm ), Math.acos( yVec.dot( faceNorm ) ) );
                                tmpCollection = getDecoratedDecorator( gen, sizeR, sizeY, null, level, false );
                                count = tmpCollection.size();
                                for ( int j = 0; j < count; ++j )
                                {
                                    anInfo = tmpCollection.elementAt( j ).objectInfo;
                                    coords = anInfo.coords;
                                    coords.setOrigin( coords.getOrigin().plus( tr ) );
                                    if ( randomRotation )
                                        coords.transformCoordinates( mrot );
                                    if ( jitter > 0 )
                                        coords.setOrigin( coords.getOrigin().plus( jitterVec ) );
                                    if ( followMeshOrientation )
                                        coords.transformCoordinates( m );
                                    coords.setOrigin( coords.getOrigin().plus( pos ) );
                                    coords.transformOrigin( mainObject.coords.fromLocal() );
                                }
                                newCollection.mergeCollection( tmpCollection, 0 );
                            }
                            done[i] = true;
                        }
                    }
                    break;
                case UNIFORM:
                    if ( occupancy <= 0 )
                        return newCollection;
                    int v1;
                    int v2;
                    int v3;
                    Vec3 vec1 = null;
                    Vec3 vec2 = null;
                    int maxj;
                    int maxk;
                    double step = objectArea / occupancy;
                    for ( int i = 0; i < numFaces; ++i )
                    {
                        //calculate face normal
                        faceNorm = v[faces[i].v2].r.minus( v[faces[i].v1].r ).cross( v[faces[i].v3].r.minus( v[faces[i].v1].r ) );
                        faceNorm.normalize();
                        m = Mat4.axisRotation( yVec.cross( faceNorm ), Math.acos( yVec.dot( faceNorm ) ) );

                        //find the two smallest edges.
                        int e1 = faces[i].e1;
                        int e2 = faces[i].e2;
                        if ( TapUtils.edgeLength( edges[e2], v ) < TapUtils.edgeLength( edges[e1], v ) )
                        {
                            e1 = faces[i].e2;
                            e2 = faces[i].e1;
                        }
                        if ( TapUtils.edgeLength( edges[faces[i].e3], v ) < TapUtils.edgeLength( edges[e1], v ) )
                        {
                            e2 = e1;
                            e1 = faces[i].e3;
                        }
                        else if ( TapUtils.edgeLength( edges[faces[i].e3], v ) < TapUtils.edgeLength( edges[e2], v ) )
                            e2 = faces[i].e3;

                        //find the common vertex between two edges
                        v1 = edges[e1].v1;
                        v2 = edges[e1].v2;
                        v3 = edges[e2].v2;
                        if ( v1 == edges[e2].v1 )
                        {
                            vec1 = v[edges[e1].v2].r.minus( v[v1].r );
                            vec2 = v[edges[e2].v2].r.minus( v[v1].r );
                            v2 = edges[e1].v2;
                            v3 = edges[e2].v2;
                        }
                        else if ( v1 == edges[e2].v2 )
                        {
                            vec1 = v[edges[e1].v2].r.minus( v[v1].r );
                            vec2 = v[edges[e2].v1].r.minus( v[v1].r );
                            v2 = edges[e1].v2;
                            v3 = edges[e2].v1;
                        }
                        else
                        {
                            v1 = edges[e1].v2;
                            if ( v1 == edges[e2].v1 )
                            {
                                vec1 = v[edges[e1].v1].r.minus( v[v1].r );
                                vec2 = v[edges[e2].v2].r.minus( v[v1].r );
                                v2 = edges[e1].v1;
                                v3 = edges[e2].v2;
                            }
                            else if ( v1 == edges[e2].v2 )
                            {
                                vec1 = v[edges[e1].v1].r.minus( v[v1].r );
                                vec2 = v[edges[e2].v1].r.minus( v[v1].r );
                                v2 = edges[e1].v1;
                                v3 = edges[e2].v1;
                            }
                        }
                        //now we get two vectors to map the map, make a step x step grid
                        maxj = new Long( Math.round( vec1.length() / step ) ).intValue();
                        maxk = new Long( Math.round( vec2.length() / step ) ).intValue();
                        for ( int j = 0; j < maxj; ++j )
                            for ( int k = 0; k < maxk; ++k )
                            {
                                pos = v[v1].r.plus( vec1.times( ( j + 0.5 ) / maxj ) ).plus( vec2.times( ( k + 0.5 ) / maxk ) );
                                //check if grid point still in triangular face
                                if ( TapUtils.ptInTriangle( pos, v[v1].r, v[v2].r, v[v3].r ) )
                                {
                                    if ( jitter > 0 )
                                    {
                                        dum = gen.uniformDeviate() * Math.PI * 2;
                                        dumr = gen.uniformDeviate() * jitter;
                                        jitterVec = new Vec3( dumr * Math.cos( dum ), 0, dumr * Math.sin( dum ) );
                                    }
                                    if ( randomRotation )
                                    {
                                        mrot = Mat4.axisRotation( yVec, gen.getDistribution( rotMean, rotDev, rotDistType ) );
                                    }
                                    tmpCollection = getDecoratedDecorator( gen, sizeR, sizeY, null, level, false );
                                    count = tmpCollection.size();
                                    for ( int l = 0; l < count; ++l )
                                    {
                                        anInfo = tmpCollection.elementAt( l ).objectInfo;
                                        coords = anInfo.coords;
                                        coords.setOrigin( coords.getOrigin().plus( tr ) );
                                        if ( randomRotation )
                                            coords.transformCoordinates( mrot );
                                        if ( jitter > 0 )
                                            coords.setOrigin( coords.getOrigin().plus( jitterVec ) );
                                        if ( followMeshOrientation )
                                            coords.transformCoordinates( m );
                                        coords.setOrigin( coords.getOrigin().plus( pos ) );
                                        coords.transformOrigin( mainObject.coords.fromLocal() );
                                    }
                                    newCollection.mergeCollection( tmpCollection, 0 );
                                }
                            }
                    }
                    break;
            }
            return newCollection;
        }
        else
            return null;
    }


    /**
     *  Gets the value attribute of the GoldenBallModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  var         Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The value value
     */

    @Override
    public double getValue( int outputPort, double[] var, long seed )
    {
        return 0.0;
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
        {
            if ( collection.size() > 0 )
            {
                ObjectInfo newObjectInfo = new ObjectInfo( collection, new CoordinateSystem(), getName() );
                newObjectInfo.object.setTexture( procedure.getScene().getDefaultTexture(), procedure.getScene().getDefaultTexture().getDefaultMapping(newObjectInfo.object) );
                setupPreviewFrame( newObjectInfo );
            }
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
     *  Gets the moduleTypeInfo attribute of the GoldenBallModule object
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


    /*
     *  public String getEditWidgetName( int index )
     *  {
     *  switch ( index )
     *  {
     *  default:
     *  case 0:
     *  return TapBTranslate.text( "fieldMainParameters" );
     *  case 1:
     *  return TapBTranslate.text( "distortFirstParameters" );
     *  }
     *  }
     */
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
        return new FieldModuleEditWidget( cb, standalone, this );
        /*
         *  switch ( index )
         *  {
         *  default:
         *  case 0:
         *  return new FieldModuleEditWidget( cb, standalone, this );
         *  case 1:
         *  return parms.getFirstEditWidget( cb, standalone, "fieldModuleTitle", this );
         *  }
         */
    }


    /**
     *  AoI object editor window
     *
     *@author     Francois Guillet
     *@created    19 avril 2004
     */
    private class FieldModuleEditWidget
             extends EditWidgetBase
    {
        private BComboBox coverChoice;
        private BCheckBox followOrientCB;
        private BSpinner estimateSpinner;
        private BCheckBox sizeChildrenCB, randomRotCB;
        private BRadioButton sizeDistURB, sizeDistGRB, rotURB, rotGRB;
        private RadioButtonGroup sizeDistBG, rotBG;
        private ValueField occupancyVF, sizeDistVF, jitterVF, inwardVF, rotMeanVF, rotDevVF;
        private BLabel sizeDistLabel, rotMeanLabel, rotDevLabel;

        private double backOccupancy;
        private int backEstimate;
        private short backCoverType;
        private boolean backFollowMeshOrientation;
        private boolean backSizeChildren;
        private double backSizeDist;
        private short backSizeDistType;
        private double backInward;
        private double backJitter;
        private double backRotFrom;
        private double backRotTo;
        private boolean backRandomRotation;
        private short backRotDistType;


        /**
         *  Constructor for the FieldModuleEditWidget object
         *
         *@param  cb          Runnable to call when updating.
         *@param  standalone  In a standalone window of in a properties split
         *      pane.
         *@param  module      The module that owns the edit widget.
         */
        public FieldModuleEditWidget( Runnable cb, boolean standalone, TapModule module )
        {
            super( cb, standalone, "fieldModuleTitle", module );

            String[] comboStrings = new String[4];
            comboStrings[0] = TapBTranslate.text( "UNIFORM" );
            comboStrings[1] = TapBTranslate.text( "VERTICES" );
            comboStrings[2] = TapBTranslate.text( "FACE_CENTERS" );
            comboStrings[3] = TapBTranslate.text( "QUAD_CENTERS" );
            coverChoice = new BComboBox( comboStrings );

            estimateSpinner = new BSpinner( estimate, 1, 1000, 1 );
            occupancyVF = new ValueField( occupancy, ValueField.NONNEGATIVE );

            sizeDistVF = new ValueField( sizeDist, ValueField.NONNEGATIVE );
            rotMeanVF = new ValueField( rotMean, ValueField.NONE );
            rotDevVF = new ValueField( rotDev, ValueField.NONE );
            sizeDistBG = new RadioButtonGroup();
            rotBG = new RadioButtonGroup();
            sizeDistURB = TapBTranslate.bRadioButton( "uniform", true, sizeDistBG );
            sizeDistGRB = TapBTranslate.bRadioButton( "gaussian", false, sizeDistBG );
            rotURB = TapBTranslate.bRadioButton( "uniform", true, rotBG );
            rotGRB = TapBTranslate.bRadioButton( "gaussian", false, rotBG );

            followOrientCB = TapBTranslate.bCheckBox( "followOrientation", followMeshOrientation );
            sizeChildrenCB = TapBTranslate.bCheckBox( "fieldSizeChildren", sizeChildren );
            randomRotCB = TapBTranslate.bCheckBox( "randomRotation", randomRotation );
            jitterVF = new ValueField( jitter, ValueField.NONE );
            inwardVF = new ValueField( inward, ValueField.NONE );

            ColumnContainer cc = new ColumnContainer();
            LayoutInfo layout = new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 3, 3, 3, 3 ), null );
            cc.setDefaultLayout( layout );

            BorderContainer bc = new BorderContainer();
            LayoutInfo borderLayout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 0, 3, 0, 3 ), null );
            bc.setDefaultLayout( borderLayout );
            bc.add( TapBTranslate.bLabel( "distributionType" ), BorderContainer.WEST );
            bc.add( coverChoice, BorderContainer.CENTER );
            cc.add( bc );

            bc = new BorderContainer();
            bc.setDefaultLayout( borderLayout );
            bc.add( TapBTranslate.bLabel( "occupancy" ), BorderContainer.WEST );
            bc.add( occupancyVF, BorderContainer.CENTER );
            cc.add( bc );

            cc.add( followOrientCB );

            GridContainer gc = new GridContainer( 4, 1 );
            gc.setDefaultLayout( borderLayout );
            gc.add( TapBTranslate.bLabel( "fieldJitter" ), 0, 0 );
            gc.add( jitterVF, 1, 0 );
            gc.add( TapBTranslate.bLabel( "enterInward" ), 2, 0 );
            gc.add( inwardVF, 3, 0 );
            cc.add( gc );

            bc = new BorderContainer();
            bc.setDefaultLayout( borderLayout );
            bc.add( TapBTranslate.bLabel( "toEstimate" ), BorderContainer.WEST );
            bc.add( estimateSpinner, BorderContainer.CENTER );
            cc.add( bc );

            ColumnContainer acc = new ColumnContainer();
            acc.setDefaultLayout( layout );
            acc.add( randomRotCB );
            FormContainer fc = new FormContainer( 5, 1 );
            fc.setDefaultLayout( borderLayout );
            fc.add( rotMeanLabel = TapBTranslate.bLabel( "rotMean" ), 0, 0 );
            fc.add( rotMeanVF, 1, 0 );
            fc.add( rotDevLabel = TapBTranslate.bLabel( "pm" ), 2, 0 );
            fc.add( rotDevVF, 3, 0 );
            ColumnContainer tcc = new ColumnContainer();
            tcc.setDefaultLayout( new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE ) );
            tcc.add( rotURB );
            tcc.add( rotGRB );
            fc.add( tcc, 4, 0 );
            acc.add( fc );
            cc.add( new BOutline( acc, BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), TapBTranslate.text( "randomRotation" ) ) ), new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 5, 1, 1, 1 ), null ) );

            acc = new ColumnContainer();
            acc.setDefaultLayout( layout );
            bc = new BorderContainer();
            bc.setDefaultLayout( new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 3, 0, 0, 0 ), null ) );
            bc.add( sizeDistLabel = TapBTranslate.bLabel( "sizeDist" ), BorderContainer.WEST );
            bc.add( sizeDistVF, BorderContainer.CENTER );
            tcc = new ColumnContainer();
            tcc.setDefaultLayout( new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE ) );
            tcc.add( sizeDistURB );
            tcc.add( sizeDistGRB );
            bc.add( tcc, BorderContainer.EAST, new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 0, 0, 0, 3 ), null ) );
            acc.add( bc );
            acc.add( sizeChildrenCB );
            cc.add( new BOutline( acc, BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), TapBTranslate.text( "fieldSizeChildren" ) ) ), new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 5, 1, 1, 1 ), null ) );

            add( cc, BorderContainer.CENTER );
            updateValues();
            sizeDistURB.addEventLink( ValueChangedEvent.class, this, "doModified" );
            sizeDistGRB.addEventLink( ValueChangedEvent.class, this, "doModified" );
            rotURB.addEventLink( ValueChangedEvent.class, this, "doModified" );
            rotGRB.addEventLink( ValueChangedEvent.class, this, "doModified" );
            sizeDistVF.addEventLink( ValueChangedEvent.class, this, "doModified" );
            occupancyVF.addEventLink( ValueChangedEvent.class, this, "doModified" );
            jitterVF.addEventLink( ValueChangedEvent.class, this, "doModified" );
            inwardVF.addEventLink( ValueChangedEvent.class, this, "doModified" );
            rotMeanVF.addEventLink( ValueChangedEvent.class, this, "doModified" );
            rotDevVF.addEventLink( ValueChangedEvent.class, this, "doModified" );
            coverChoice.addEventLink( ValueChangedEvent.class, this, "doModified" );
            estimateSpinner.addEventLink( ValueChangedEvent.class, this, "doModified" );
            sizeChildrenCB.addEventLink( ValueChangedEvent.class, this, "doModified" );
            followOrientCB.addEventLink( ValueChangedEvent.class, this, "doModified" );
            randomRotCB.addEventLink( ValueChangedEvent.class, this, "doModified" );
            randomRotCB.addEventLink( ValueChangedEvent.class, this, "doRandomRot" );
            doRandomRot();
            followOrientCB.addEventLink( ToolTipEvent.class, new TapBToolTip( followOrientTooltipComponent() ) );
            //followOrientCB.addEventLink( ToolTipEvent.class, new TapBToolTip( "Test text" ) );
            //sizeChildrenCB.addEventLink( ToolTipEvent.class, new BToolTip( "Do Not Press This Button" ) );
        }



        /**
         *  Description of the Method
         *
         *@return    Description of the Return Value
         */
        private Component followOrientTooltipComponent()
        {
            JPanel panel = new JPanel();
            panel.setBackground( TapUtils.getToolTipBackground() );
            GridBagLayout gbl = new GridBagLayout();
            panel.setLayout( gbl );
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;
            JLabel label = new JLabel( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/tooltips/field/orient_disabled.png" ) ) );
            gbl.setConstraints( label, gbc );
            panel.add( label );
            gbc.gridx = 1;
            gbc.gridy = 0;
            label = new JLabel( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/tooltips/field/orient_enabled.png" ) ) );
            gbl.setConstraints( label, gbc );
            panel.add( label );
            gbc.gridx = 0;
            gbc.gridy = 1;
            label = new JLabel( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/tooltips/disabled.png" ) ) );
            gbl.setConstraints( label, gbc );
            panel.add( label );
            gbc.gridx = 1;
            gbc.gridy = 1;
            label = new JLabel( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/tooltips/enabled.png" ) ) );
            gbl.setConstraints( label, gbc );
            panel.add( label );
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = 2;
            gbc.gridx = 0;
            gbc.gridy = 2;
            JTextArea area = new JTextArea( TapBTranslate.text( "followOrientTooltip" ) );
            gbl.setConstraints( area, gbc );
            panel.add( area );

            panel.setBorder( TapUtils.getToolTipBorder() );
            return panel;
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
                updateValues();
                super.showValues( force );
            }
        }


        /**
         *  Constructor for the doRandomRot object
         */
        private void doRandomRot()
        {
            boolean state = randomRotCB.getState();
            rotMeanVF.setEnabled( state );
            rotDevVF.setEnabled( state );
            rotURB.setEnabled( state );
            rotGRB.setEnabled( state );
            rotMeanLabel.setEnabled( state );
            rotDevLabel.setEnabled( state );
        }


        /**
         *  Description of the Method
         */
        private void updateValues()
        {
            estimateSpinner.setValue( new Integer( estimate ) );
            followOrientCB.setState( followMeshOrientation );
            sizeChildrenCB.setState( sizeChildren );
            TapUtils.setDistRadioButton( sizeDistType, sizeDistURB, sizeDistGRB );
            coverChoice.setSelectedIndex( coverType );
            occupancyVF.setValue( occupancy );
            sizeDistVF.setValue( sizeDist );
            randomRotCB.setState( randomRotation );
            rotMeanVF.setValue( rotMean );
            rotDevVF.setValue( rotDev );
            TapUtils.setDistRadioButton( rotDistType, rotURB, rotGRB );
            doRandomRot();
        }


        /**
         *  Gets the undoValues
         */
        @Override
        protected void getUndoValues()
        {
            occupancy = backOccupancy;
            estimate = backEstimate;
            coverType = backCoverType;
            followMeshOrientation = backFollowMeshOrientation;
            sizeChildren = backSizeChildren;
            sizeDist = backSizeDist;
            sizeDistType = backSizeDistType;
            jitter = backJitter;
            inward = backInward;
            rotMean = backRotFrom;
            rotDev = backRotTo;
            randomRotation = backRandomRotation;
            rotDistType = backRotDistType;
        }


        /**
         *  Gets the backValues
         */
        @Override
        protected void getValues()
        {
            estimate = ( (Integer) estimateSpinner.getValue() ).intValue();
            followMeshOrientation = followOrientCB.getState();
            sizeChildren = sizeChildrenCB.getState();
            coverType = (short) coverChoice.getSelectedIndex();
            sizeDistType = TapUtils.getDistTypeFromRadio( sizeDistURB, sizeDistGRB );
            occupancy = new Float( occupancyVF.getValue() ).floatValue();
            sizeDist = new Float( sizeDistVF.getValue() ).floatValue();
            jitter = new Float( jitterVF.getValue() ).floatValue();
            inward = new Float( inwardVF.getValue() ).floatValue();
            rotMean = new Float( rotMeanVF.getValue() ).floatValue();
            rotDev = new Float( rotDevVF.getValue() ).floatValue();
            if ( rotMean > rotDev )
                rotMean = rotDev = 0;
            randomRotation = randomRotCB.getState();
            rotDistType = TapUtils.getDistTypeFromRadio( rotURB, rotGRB );
        }


        /**
         *  Initializes backup values
         */
        @Override
        protected void initBackValues()
        {
            backOccupancy = occupancy;
            backEstimate = estimate;
            backCoverType = coverType;
            backFollowMeshOrientation = followMeshOrientation;
            backSizeChildren = sizeChildren;
            backSizeDist = sizeDist;
            backSizeDistType = sizeDistType;
            backJitter = jitter;
            backInward = inward;
            backRotFrom = rotMean;
            backRotTo = rotDev;
            backRandomRotation = randomRotation;
            backRotDistType = rotDistType;
        }


        /**
         *  Description of the Method
         */
        @Override
        protected void doModified()
        {
            super.doModified();
        }

    }
    //}}}

}

