/*
 *  The TapLeaf class represents a leaf made of a distorted spline mesh, and later on
 *  thickened using triangular mesh conversion.
 */
/*
 *  (C) 2004 by Francois Guillet
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
import artofillusion.texture.*;
import artofillusion.material.*;
import java.io.*;


/**
 *  Description of the Class
 *
 *@author     pims
 *@created    20 juin 2004
 */
public class TapLeaf
         extends TriangleMesh
         implements TapObject
{
    float vertSmoothness;
    float edgeSmoothness;
    float boundVertSmoothness;
    float boundEdgeSmoothness;
    short orientation;
    TapFunction shape;
    SplineMesh originalSplineMesh;
    SplineMesh subdividedSplineMesh;
    double tolerance;
    double thickness;
    double[] meshThickness;
    int[] meshPositionRef;
    Vec3[] meshPositionTranslation;
    Vec3[] meshOriginalNorm;


    /**
     *  Constructor for the TapLeaf object
     *
     *@param  s          Description of the Parameter
     *@param  vs         Description of the Parameter
     *@param  es         Description of the Parameter
     *@param  bvs        Description of the Parameter
     *@param  bes        Description of the Parameter
     *@param  shape      Description of the Parameter
     *@param  tol        Description of the Parameter
     *@param  thickness  Description of the Parameter
     */
    public TapLeaf( SplineMesh s, float vs, float es, float bvs, float bes, TapFunction shape, double tol, double thickness )
    {
        super( new Vertex[0], new int[0][0] );
        tolerance = tol;
        originalSplineMesh = (SplineMesh) s.duplicate();
        subdividedSplineMesh = (SplineMesh) s.duplicate();

        MeshVertex[] v = subdividedSplineMesh.getVertices();
        Vec3[] vr = new Vec3[v.length];
        double miny = 1e8;

        for ( int i = 0; i < v.length; ++i )
            if ( v[i].r.y < miny )
                miny = v[i].r.y;

        for ( int i = 0; i < v.length; ++i )
        {
            vr[i] = new Vec3( v[i].r );
            vr[i].y -= miny;
        }

        subdividedSplineMesh.setVertexPositions( vr );
        super.copyObject( subdividedSplineMesh.convertToTriangleMesh( tol ) );
        subdividedSplineMesh = SplineMesh.subdivideMesh( subdividedSplineMesh, tol );
        vertSmoothness = vs;
        edgeSmoothness = es;
        boundVertSmoothness = bvs;
        boundEdgeSmoothness = bes;
        this.thickness = thickness;
        this.shape = shape.duplicate();
        setupMesh();
        setTexture( s.getTexture(), s.getTexture().getDefaultMapping(this) );
    }


    /**
     *  Constructor for the TapLeaf object
     */
    private TapLeaf()
    {
        super( new Vertex[0], new int[0][0] );
    }


    /**
     *  Description of the Method
     *
     *@param  v  Description of the Parameter
     *@return    Description of the Return Value
     */
    private short findOrientation( Vertex[] v )
    {
        //find the relevant 2D plane, calculations are carried out 2D
        int i;
        double minx;
        double maxx;
        double miny;
        double maxy;
        double minz;
        double maxz;
        minx = miny = minz = 1e8;
        maxx = maxy = maxz = -1e8;

        for ( i = 0; i < v.length; ++i )
        {
            if ( v[i].r.x < minx )
                minx = v[i].r.x;

            if ( v[i].r.y < miny )
                miny = v[i].r.y;

            if ( v[i].r.z < minz )
                minz = v[i].r.z;

            if ( v[i].r.x > maxx )
                maxx = v[i].r.x;

            if ( v[i].r.y > maxy )
                maxy = v[i].r.y;

            if ( v[i].r.z > maxz )
                maxz = v[i].r.z;
        }

        double dx = ( maxx - minx );
        double dy = ( maxy - miny );
        double dz = ( maxz - minz );

        if ( dx < dy && dx < dz )
            return 0;
        else if ( dy < dx && dy < dz )
            return 1;
        else if ( dz < dx && dz < dy )
            return 2;

        return 0;
    }


    /**
     *  Gets the barycentreCoefficients attribute of the TapLeaf object
     *
     *@param  v            Description of the Parameter
     *@param  v1           Description of the Parameter
     *@param  v2           Description of the Parameter
     *@param  v3           Description of the Parameter
     *@param  orientation  Description of the Parameter
     *@return              The barycentreCoefficients value
     */
    private double[] getBarycentreCoefficients( Vec3 v, Vec3 v1, Vec3 v2, Vec3 v3, short orientation )
    {
        double x1;
        double y1;
        double x2;
        double y2;
        double x3;
        double y3;
        double xv;
        double yv;
        boolean clockwise = false;

        //use the relevant 2D plane, calculations are carried out 2D
        switch ( orientation )
        {
            default:
            case 0:
                x1 = v1.y;
                x2 = v2.y;
                x3 = v3.y;
                y1 = v1.z;
                y2 = v2.z;
                y3 = v3.z;
                xv = v.y;
                yv = v.z;

                break;
            case 1:
                x1 = v1.x;
                x2 = v2.x;
                x3 = v3.x;
                y1 = v1.z;
                y2 = v2.z;
                y3 = v3.z;
                xv = v.x;
                yv = v.z;

                break;
            case 2:
                x1 = v1.x;
                x2 = v2.x;
                x3 = v3.x;
                y1 = v1.y;
                y2 = v2.y;
                y3 = v3.y;
                xv = v.x;
                yv = v.y;

                break;
        }

        //find if the triangle is clockwise ar anticlockwise
        double det = ( x2 - x1 ) * ( y3 - y1 ) - ( x3 - x1 ) * ( y2 - y1 );

        if ( det < 0 )
        {
            //clockwise, swap vertices 2 and 3

            clockwise = true;

            double x;
            double y;
            x = x2;
            y = y2;
            x2 = x3;
            y2 = y3;
            x3 = x;
            y3 = y;
        }

        //Setup vector normals, Geometric Tools for Computer Graphics, pp 695-696
        Vec2 e1 = new Vec2( x2, y2 );
        e1.subtract( new Vec2( x1, y1 ) );

        Vec2 e2 = new Vec2( x3, y3 );
        e2.subtract( new Vec2( x2, y2 ) );

        Vec2 e3 = new Vec2( x1, y1 );
        e3.subtract( new Vec2( x3, y3 ) );

        Vec2 n1 = new Vec2( e1.y, -e1.x );
        Vec2 n2 = new Vec2( e2.y, -e2.x );
        Vec2 n3 = new Vec2( e3.y, -e3.x );

        //calulate the coefficients
        Vec2 P = new Vec2( xv, yv );
        P.subtract( new Vec2( x2, y2 ) );

        double[] coef = new double[3];
        coef[0] = -( P.dot( n2 ) ) / ( n2.dot( e1 ) );
        P = new Vec2( xv, yv );
        P.subtract( new Vec2( x3, y3 ) );
        coef[1] = -( P.dot( n3 ) ) / ( n3.dot( e2 ) );
        P = new Vec2( xv, yv );
        P.subtract( new Vec2( x1, y1 ) );
        coef[2] = -( P.dot( n1 ) ) / ( n1.dot( e3 ) );

        if ( clockwise )
        {
            //put coefficients back in order

            double x = coef[1];
            coef[1] = coef[2];
            coef[2] = x;
        }

        return coef;
    }


    /**
     *  Description of the Method
     */
    private void setupMesh()
    {
        //adapted from the thicken script by Peter Eastman

        int i;
        int j;
        int k;
        double t;
        double p;
        double[] coef;

        Vertex[] vert = (Vertex[]) getVertices();
        orientation = findOrientation( vert );
        Vec3[] norm = getNormals();

        // Let's get the boundary guys. During the process, the leaf center is calculated.
        Edge[] edge = getEdges();
        Vec3 center = new Vec3();
        int numBoundary = 0;
        Vec3 dumVec;
        double distance = 0;

        for ( i = 0; i < edge.length; i++ )
            if ( edge[i].f2 == -1 )
            {
                numBoundary++;
                dumVec = new Vec3( vert[edge[i].v1].r );
                dumVec.add( vert[edge[i].v2].r );
                dumVec.scale( vert[edge[i].v1].r.distance( vert[edge[i].v2].r ) / 2.0 );
                center.add( dumVec );
                distance += vert[edge[i].v1].r.distance( vert[edge[i].v2].r );
            }

        center.scale( 1.0 / distance );

        //meshThickness records the thickness at at a particular vertex
        //meshPositionRef records which vertex of subdividedSplineMesh is closest
        //meshPositionTranslation records the vector between the spline mesh reference
        //vertex and the triangular mesh vertex. (0,0,0) at the moment (vertices of
        //spline mesh and triangular spline mesh are identical)
        //may change in the future, hence the general algorithm
        Vertex[] newVert = new Vertex[vert.length * 2 - numBoundary];
        meshThickness = new double[vert.length * 2 - numBoundary];
        meshPositionRef = new int[vert.length * 2 - numBoundary];
        meshPositionTranslation = new Vec3[vert.length * 2 - numBoundary];

        //verticesRef marks the boundary vertices as negative
        //newVertRef establishes correspondance between new and old vertices.
        int[] verticesRef = new int[vert.length * 2 - numBoundary];
        int[] newVertRef = new int[vert.length];

        //list of edges on the boundary
        int[] boundaryEdges = new int[numBoundary];

        for ( i = 0; i < verticesRef.length; i++ )
            verticesRef[i] = i;

        j = 0;

        for ( i = 0; i < edge.length; i++ )
            if ( edge[i].f2 == -1 )
            {
                verticesRef[edge[i].v1] = -edge[i].v1;
                verticesRef[edge[i].v2] = -edge[i].v2;
                boundaryEdges[j] = i;
                ++j;
            }

        MeshVertex[] splineVertices = subdividedSplineMesh.getVertices();
        meshOriginalNorm = subdividedSplineMesh.getNormals();

        j = vert.length;

        for ( i = 0; i < vert.length; i++ )
        {
            newVert[i] = new Vertex( vert[i] );

            //calulate the correspondance between spline mesh and triangular mesh
            double dist = 1e8;
            double dum;

            for ( k = 0; k < splineVertices.length; ++k )
            {
                dum = splineVertices[k].r.distance( vert[i].r );

                if ( dum < dist )
                {
                    meshPositionRef[i] = k;
                    dist = dum;
                }
            }

            meshPositionTranslation[i] = new Vec3( vert[i].r );
            meshPositionTranslation[i].subtract( splineVertices[meshPositionRef[i]].r );

            if ( verticesRef[i] > 0 )
            {
                //inner vertices
                newVert[j] = new Vertex( vert[i] );
                newVertRef[i] = j;
                newVert[i].smoothness = vertSmoothness;
                newVert[j].smoothness = vertSmoothness;
                meshPositionRef[j] = meshPositionRef[i];
                meshPositionTranslation[j] = new Vec3( meshPositionTranslation[i] );
                verticesRef[j] = i;
                t = 1;

                for ( k = 0; k < numBoundary; ++k )
                {
                    coef = getBarycentreCoefficients( vert[i].r, vert[edge[boundaryEdges[k]].v1].r, vert[edge[boundaryEdges[k]].v2].r, center, orientation );

                    if ( ( coef[0] >= -0.001 && coef[0] <= 1.001 ) && ( coef[1] >= -0.001 && coef[1] <= 1.001 ) && ( coef[2] >= -0.001 && coef[2] <= 1.001 ) )
                        t = 1 - coef[2];
                }

                p = shape.calcValue( t ) * thickness;
                meshThickness[i] = t;
                meshThickness[j] = t;
                newVert[i].r.add( norm[i].times( p ) );
                newVert[j].r.add( norm[i].times( -p ) );
                ++j;
            }
            else
            {
                //boundary vertices

                newVertRef[i] = i;
                newVert[i].smoothness = boundVertSmoothness;
                meshThickness[i] = 1;
            }
        }

        // Duplicate the faces.
        Face[] face = getFaces();
        int[][] newFace = new int[face.length * 2][3];

        for ( i = 0; i < face.length; i++ )
        {
            newFace[i][0] = face[i].v1;
            newFace[i][1] = face[i].v2;
            newFace[i][2] = face[i].v3;
            newFace[face.length + i][0] = newVertRef[face[i].v1];
            newFace[face.length + i][1] = newVertRef[face[i].v3];
            newFace[face.length + i][2] = newVertRef[face[i].v2];
        }

        // Set the new list of vertices and faces for the mesh.
        super.setShape( newVert, newFace );

        // Setup smoothness
        Face[] newf = getFaces();
        Edge[] newe = getEdges();

        for ( i = 0; i < newf.length; i++ )
        {
            newe[newf[i].e1].smoothness = edgeSmoothness;
            newe[newf[i].e2].smoothness = edgeSmoothness;
            newe[newf[i].e3].smoothness = edgeSmoothness;
        }

        // Set the edges smoothness around the former boundary
        for ( i = 0; i < newe.length; i++ )
        {
            if ( verticesRef[newe[i].v1] < 0 && verticesRef[newe[i].v2] < 0 )
                newe[i].smoothness = boundEdgeSmoothness;
        }
    }


    /**
     *  Sets the shape attribute of the TapLeaf object
     *
     *@param  shape      The new shape value
     *@param  thickness  The new shape value
     */
    public void setShape( TapFunction shape, double thickness )
    {
        this.shape = shape.duplicate();
        this.thickness = thickness;
        updateMesh();
    }


    /**
     *  Gets the shape attribute of the TapLeaf object
     *
     *@return    The shape value
     */
    public TapFunction getShape()
    {
        return shape;
    }


    /**
     *  Gets the thickness attribute of the TapLeaf object
     *
     *@return    The thickness value
     */
    public double getThickness()
    {
        return thickness;
    }


    /**
     *  Sets the smoothness attribute of the TapLeaf object
     *
     *@param  vs   The new smoothness value
     *@param  es   The new smoothness value
     *@param  bvs  The new smoothness value
     *@param  bes  The new smoothness value
     */
    public void setSmoothness( float vs, float es, float bvs, float bes )
    {
        vertSmoothness = vs;
        edgeSmoothness = es;
        boundVertSmoothness = bvs;
        boundEdgeSmoothness = bes;
        updateMesh();
    }


    /**
     *  Gets the vertSmoothness attribute of the TapLeaf object
     *
     *@return    The vertSmoothness value
     */
    public float getVertSmoothness()
    {
        return vertSmoothness;
    }


    /**
     *  Gets the edgeSmoothness attribute of the TapLeaf object
     *
     *@return    The edgeSmoothness value
     */
    public float getEdgeSmoothness()
    {
        return edgeSmoothness;
    }


    /**
     *  Gets the boundVertSmoothness attribute of the TapLeaf object
     *
     *@return    The boundVertSmoothness value
     */
    public float getBoundVertSmoothness()
    {
        return boundVertSmoothness;
    }


    /**
     *  Gets the boundEdgeSmoothness attribute of the TapLeaf object
     *
     *@return    The boundEdgeSmoothness value
     */
    public float getBoundEdgeSmoothness()
    {
        return boundEdgeSmoothness;
    }


    /**
     *  Sets the tolerance attribute of the TapLeaf object
     *
     *@param  tol  The new tolerance value
     */
    public void setTolerance( double tol )
    {
        tolerance = tol;
        updateMesh();
    }


    /**
     *  Gets the tolerance attribute of the TapLeaf object
     *
     *@return    The tolerance value
     */
    public double getTolerance()
    {
        return tolerance;
    }


    /**
     *  Sets the shape attribute of the TapLeaf object
     *
     *@param  parms  The new shape value
     */
    @Override
    public void setShape( TapDistortParameters parms )
    {
        if ( parms != null )
            TapUtils.distortSplineMesh( subdividedSplineMesh, parms );

        updateMesh();
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public Object3D duplicate()
    {
        TapLeaf leaf = new TapLeaf();
        leaf.copyObject( this );

        return (Object3D) leaf;
    }


    /**
     *  Description of the Method
     *
     *@param  obj  Description of the Parameter
     */
    @Override
    public void copyObject( Object3D obj )
    {
        super.copyObject( obj );

        TapLeaf leaf = (TapLeaf) obj;
        originalSplineMesh = (SplineMesh) leaf.originalSplineMesh.duplicate();
        subdividedSplineMesh = (SplineMesh) leaf.subdividedSplineMesh.duplicate();
        orientation = leaf.orientation;
        vertSmoothness = leaf.vertSmoothness;
        edgeSmoothness = leaf.edgeSmoothness;
        boundVertSmoothness = leaf.boundVertSmoothness;
        boundEdgeSmoothness = leaf.boundEdgeSmoothness;
        tolerance = leaf.tolerance;
        thickness = leaf.thickness;
        meshThickness = new double[leaf.meshThickness.length];
        meshPositionRef = new int[leaf.meshPositionRef.length];
        meshPositionTranslation = new Vec3[leaf.meshPositionTranslation.length];
        meshOriginalNorm = new Vec3[leaf.meshOriginalNorm.length];

        for ( int i = 0; i < meshThickness.length; ++i )
        {
            meshThickness[i] = leaf.meshThickness[i];
            meshPositionRef[i] = leaf.meshPositionRef[i];
            meshPositionTranslation[i] = new Vec3( leaf.meshPositionTranslation[i] );
        }

        for ( int i = 0; i < meshOriginalNorm.length; ++i )
            meshOriginalNorm[i] = new Vec3( leaf.meshOriginalNorm[i] );

        shape = leaf.shape.duplicate();
    }


    /**
     *  Sets the texture attribute of the TapLeaf object
     *
     *@param  tex      The new texture value
     *@param  mapping  The new texture value
     */
    @Override
    public void setTexture( Texture tex, TextureMapping mapping )
    {
        super.setTexture( tex, mapping );
        if ( originalSplineMesh != null )
            originalSplineMesh.setTexture( tex, mapping );
        if ( subdividedSplineMesh != null )
            subdividedSplineMesh.setTexture( tex, mapping );
    }


    /**
     *  Get this object's Texture.
     *
     *@return    The texture value
     */

    @Override
    public Texture getTexture()
    {
        return super.getTexture();
    }


    /**
     *  Get this object's TextureMapping.
     *
     *@return    The textureMapping value
     */

    @Override
    public TextureMapping getTextureMapping()
    {
        return super.getTextureMapping();
    }


    /**
     *  Set the Material and MaterialMapping for this object. Pass null for both
     *  arguments to specify that the object does not have a Material.
     *
     *@param  mat      The new material value
     *@param  mapping  The new material value
     */

    @Override
    public void setMaterial( Material mat, MaterialMapping mapping )
    {
        super.setMaterial( mat, mapping );
    }


    /**
     *  Get this object's Material.
     *
     *@return    The material value
     */

    @Override
    public Material getMaterial()
    {
        return theMaterial;
    }


    /**
     *  Get this object's MaterialMapping.
     *
     *@return    The materialMapping value
     */

    @Override
    public MaterialMapping getMaterialMapping()
    {
        return matMapping;
    }


    /**
     *  Description of the Method
     */
    private void updateMesh()
    {
        Vertex[] vert = (Vertex[]) getVertices();
        Vec3[] newNormals = subdividedSplineMesh.getNormals();
        MeshVertex[] splineVert = subdividedSplineMesh.getVertices();
        int i;
        int l;

        Vec3 position;
        Vec3 rotAxis;
        Mat4 m;
        double angle;

        for ( i = 0; i < vert.length; ++i )
        {
            rotAxis = new Vec3( meshOriginalNorm[meshPositionRef[i]] );
            rotAxis = rotAxis.cross( newNormals[meshPositionRef[i]] );
            angle = Math.acos( meshOriginalNorm[meshPositionRef[i]].dot( newNormals[meshPositionRef[i]] ) );
            m = Mat4.axisRotation( rotAxis, angle );
            position = new Vec3( splineVert[meshPositionRef[i]].r );
            position.add( m.timesDirection( meshPositionTranslation[i] ) );
            vert[i].r = position;
        }

        //now the planar mesh is distorted, add some thickness
        Vec3[] norm = getNormals();
        Vec3[] v = new Vec3[vert.length];

        for ( i = 0; i < vert.length; ++i )
        {
            vert[i].r.add( norm[i].times( shape.calcValue( meshThickness[i] ) * thickness ) );
            v[i] = vert[i].r;
        }

        setVertexPositions( v );
    }


    /**
     *  Description of the Method
     */
    @Override
    public void regenerateMesh()
    {
        updateMesh();
    }


    /**
     *  Gets the originalSplineMesh attribute of the TapLeaf object
     *
     *@return    The originalSplineMesh value
     */
    public SplineMesh getOriginalSplineMesh()
    {
        return (SplineMesh) originalSplineMesh.duplicate();
    }


    /**
     *  Gets the position attribute of the TapLeaf object
     *
     *@param  yPos       Description of the Parameter
     *@param  angle      Description of the Parameter
     *@param  rDisplace  Description of the Parameter
     *@return            The position value
     */
    @Override
    public Mat4 getPosition( double yPos, double angle, boolean rDisplace )
    {
        int i;
        int j;
        int k;
        int i1;
        int i2;

        i1 = 0;
        i2 = 1;

        int usize = subdividedSplineMesh.getUSize();
        int vsize = subdividedSplineMesh.getVSize();
        double[] yPositions = new double[vsize];
        int ucenter = (int) ( usize / 2 );
        MeshVertex[] vert = subdividedSplineMesh.getVertices();
        Vec3[] yVert = new Vec3[vsize];

        for ( i = 0; i < vsize; ++i )
        {
            yVert[i] = new Vec3( vert[ucenter + usize * i].r );
            if ( orientation == 2 )
            {
                //take into account the 90� rotation
                yVert[i].x = -vert[ucenter + usize * i].r.z;
                yVert[i].z = vert[ucenter + usize * i].r.x;
            }
        }
        double maxY = 0;
        yPositions[0] = 0;

        for ( i = 1; i < vsize; ++i )
        {
            maxY += yVert[i].distance( yVert[i - 1] );
            yPositions[i] = maxY;
        }

        for ( i = 0; i < vsize; ++i )
            yPositions[i] /= maxY;

        int vLength = yPositions.length;
        i = (int) Math.round( yPos * vLength );

        if ( i < 0 )
            i = 0;

        if ( i > vLength - 1 )
            i = vLength - 1;

        if ( yPos < 0.0001 )
        {
            i1 = 0;
            i2 = 1;
        }
        else if ( yPos > 0.99999 )
        {
            i1 = vLength - 2;
            i2 = vLength - 1;
        }
        else if ( yPositions[i] > yPos )
        {
            while ( yPositions[i] > yPos && i >= 1 )
                i--;

            if ( i == 1 )
                if ( yPositions[i] > yPos )
                    --i;

            i1 = i;
            i2 = i + 1;
        }
        else if ( yPositions[i] <= yPos )
        {
            while ( yPositions[i] < yPos && i < vLength - 1 )
                i++;

            if ( i == vLength - 2 )
                if ( yPositions[i] < yPos )
                    ++i;

            i1 = i - 1;
            i2 = i;
        }

        Vec3 position = null;

        if ( yPos > 0.0001 && yPos < 0.99999 )
        {
            double coef = ( yPos - yPositions[i1] ) / ( yPositions[i2] - yPositions[i1] );
            position = new Vec3( yVert[i1].x * ( 1 - coef ) + yVert[i2].x * coef, yVert[i1].y * ( 1 - coef ) + yVert[i2].y * coef, yVert[i1].z * ( 1 - coef ) + yVert[i2].z * coef );
        }
        else if ( yPos <= 0.0001 )
            position = new Vec3( yVert[0] );
        else
            position = new Vec3( yVert[vLength - 1] );

        Vec3 deriv = yVert[i2].minus( yVert[i1] );
        deriv.normalize();

        Vec3 yVec = new Vec3( 0, 1, 0 );
        Vec3 axis = yVec.cross( deriv );
        double matAngle = Math.acos( yVec.dot( deriv ) );
        Mat4 m = Mat4.axisRotation( axis, matAngle );
        /*
         *  if (rDisplace)
         *  {    Vec3 r = new Vec3(0.5*Math.cos(angle)*0.5*xscale*sizeR,0,-0.5*Math.sin(angle)*0.5*zscale*sizeR);
         *  m.transform(r);
         *  position=position.minus(r);
         *  }
         */
        Mat4 result = Mat4.translation( position.x, position.y, position.z );
        result = result.times( m );

        return result;
    }


    /**
     *  Constructor for the TapLeaf object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public TapLeaf( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        int i;
        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        originalSplineMesh = new SplineMesh( in, theScene );
        subdividedSplineMesh = new SplineMesh( in, theScene );
        orientation = in.readShort();
        tolerance = in.readDouble();
        thickness = in.readDouble();
        vertSmoothness = in.readFloat();
        edgeSmoothness = in.readFloat();
        boundVertSmoothness = in.readFloat();
        boundEdgeSmoothness = in.readFloat();
        shape = new TapFunction( in );

        Vertex[] vert = (Vertex[]) getVertices();
        meshThickness = new double[vert.length];
        meshPositionRef = new int[vert.length];
        meshPositionTranslation = new Vec3[vert.length];

        Vec3[] norms = subdividedSplineMesh.getNormals();
        meshOriginalNorm = new Vec3[norms.length];

        for ( i = 0; i < vert.length; ++i )
        {
            meshThickness[i] = in.readDouble();
            meshPositionRef[i] = in.readInt();
            meshPositionTranslation[i] = new Vec3( in );
        }

        for ( i = 0; i < norms.length; ++i )
            meshOriginalNorm[i] = new Vec3( in );
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

        int i;

        out.writeShort( 0 );
        originalSplineMesh.writeToFile( out, theScene );
        subdividedSplineMesh.writeToFile( out, theScene );
        out.writeShort( orientation );
        out.writeDouble( tolerance );
        out.writeDouble( thickness );
        out.writeFloat( vertSmoothness );
        out.writeFloat( edgeSmoothness );
        out.writeFloat( boundVertSmoothness );
        out.writeFloat( boundEdgeSmoothness );
        shape.writeToFile( out );

        for ( i = 0; i < getVertices().length; ++i )
        {
            out.writeDouble( meshThickness[i] );
            out.writeInt( meshPositionRef[i] );
            meshPositionTranslation[i].writeToFile( out );
        }

        for ( i = 0; i < subdividedSplineMesh.getNormals().length; ++i )
            meshOriginalNorm[i].writeToFile( out );
    }


    /**
     *  Sets the size attribute of the TapLeaf object
     *
     *@param  xsize  The new size value
     *@param  ysize  The new size value
     *@param  zsize  The new size value
     */
    @Override
    public void setSize( double xsize, double ysize, double zsize )
    {
        setSize( xsize, ysize, zsize, null );
    }


    /**
     *  Description of the Method
     *
     *@param  size   Description of the Parameter
     *@param  sizeR  Description of the Parameter
     *@param  sizeY  Description of the Parameter
     *@param  parms  Description of the Parameter
     */
    @Override
    public void resizeAndDistort( Vec3 size, double sizeR, double sizeY, TapDistortParameters parms )
    {
        setSize( size.x * sizeR, size.y * sizeY, size.z * sizeR, parms );
    }


    /**
     *  Sets the size attribute of the TapLeaf object
     *
     *@param  xsize  The new size value
     *@param  ysize  The new size value
     *@param  zsize  The new size value
     *@param  parms  The new size value
     */
    public void setSize( double xsize, double ysize, double zsize, TapDistortParameters parms )
    {
        subdividedSplineMesh.getBounds();
        subdividedSplineMesh.setSize( xsize, ysize, zsize );

        if ( parms != null )
            TapUtils.distortSplineMesh( subdividedSplineMesh, parms );

        updateMesh();
    }


    /**
     *  Sets the counterAction attribute of the TapLeaf object
     *
     *@param  yPos   The new counterAction value
     *@param  angle  The new counterAction value
     *@param  parms  The new counterAction value
     *@return        Description of the Return Value
     */
    @Override
    public Mat4 setCounterAction( double yPos, double angle, TapDistortParameters parms )
    {
        return getPosition( yPos, angle, false );
    }


    /*
     *  these functions are for compatibility with the Object3D class
     */
    /**
     *  Gets the plainAoIObject attribute of the TapLeaf object
     *
     *@return    The plainAoIObject value
     */
    @Override
    public Object3D getPlainAoIObject()
    {
        TriangleMesh mesh = new TriangleMesh( new Vertex[0], new int[0][0] );
        mesh.copyObject( this );

        return mesh;
    }
}

