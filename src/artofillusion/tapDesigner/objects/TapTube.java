/*
 *  TaPD tube object, derived from a tube object
 */
/*
 *  (C) 2003 by Francois Guillet
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


import java.io.*;



/**
 *  Description of the Class
 *
 *@author     pims
 *@created    16 ao�t 2004
 */
public class TapTube
         extends Tube
         implements TapObject
{
    private double[] yPositions;
    private Vec3[] yVert;
    private double maxY;
    private double rSize;
    TapFunction rShape;
    TapDistortParameters sparms;


    /**
     *  Constructor for the TapTube object
     *
     *@param  yCurve     Description of the Parameter
     *@param  rShape     Description of the Parameter
     *@param  thickness  Description of the Parameter
     */
    public TapTube( Curve yCurve, TapFunction rShape, double[] thickness )
    {
        super( yCurve, thickness, Tube.FLAT_ENDS );
        this.rShape = rShape;
        rSize = 1.0;
        setShape( null );
    }


    /**
     *  Constructor for the TapTube object
     *
     *@param  v           Description of the Parameter
     *@param  smoothness  Description of the Parameter
     *@param  thickness   Description of the Parameter
     */
    private TapTube( Vec3[] v, float[] smoothness, double[] thickness )
    {
        super( v, smoothness, thickness, Mesh.APPROXIMATING, Tube.FLAT_ENDS );
    }


    /**
     *  Constructor for the TapTube object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public TapTube( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        rShape = new TapFunction( in );
        rSize = in.readDouble();
        setShape( null );
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
        rShape.writeToFile( out );
        out.writeDouble( rSize );
    }


    /**
     *  Gets the rShape attribute of the TapTube object
     *
     *@return    The rShape value
     */
    public TapFunction getRShape()
    {
        return rShape;
    }


    /**
     *  Sets the rShape attribute of the TapTube object
     *
     *@param  rs  The new rShape value
     */
    public void setRShape( TapFunction rs )
    {
        rShape = rs;
        setShape( null );
    }


    /**
     *  Sets the shape attribute of the TapTube object
     *
     *@param  parms  The new shape value
     */
    public void setShape( TapDistortParameters parms )
    {
        int i;
        int j;
        TapRandomGenerator gen = null;

        if ( parms != null )
            sparms = parms.duplicate();
        else if ( sparms != null )
            parms = sparms;
        if ( parms != null )
            gen = new TapRandomGenerator( parms.seed );

        MeshVertex[] vert = getVertices();
        yVert = new Vec3[vert.length];

        for ( i = 0; i < yVert.length; ++i )
            yVert[i] = vert[i].r;

        int vLength = yVert.length;
        double[] t = new double[vLength];
        yPositions = new double[vLength];
        maxY = 0;

        for ( i = 1; i < yVert.length; ++i )
        {
            maxY += yVert[i].distance( yVert[i - 1] );
            yPositions[i] = maxY;
        }

        yPositions[0] = 0;

        for ( i = 0; i < vLength; ++i )
        {
            yPositions[i] /= maxY;
            t[i] = rShape.calcValue( yPositions[i] ) * rSize;
        }

        Mat4 m = null;

        if ( parms != null )
        {
            double yangle;
            double rangle;
            double ratio;
            double curveAngle;
            double perpCurveAngle;
            double ysign;
            double rsign;
            double bysign = 1;
            double brsign = 1;
            double curveAngleBack = 0;
            double perpCurveAngleBack = 0;
            int yback = yVert.length;
            int rback = yVert.length;
            double segment;
            double RrandomTilt;
            double YrandomTilt;
            double yRatio;
            boolean randomChangeSign;

            if ( Math.abs( parms.curveAngle ) > 0.001 )
                ysign = Math.abs( parms.curveAngle ) / parms.curveAngle;
            else
                ysign = 1;

            if ( Math.abs( parms.perpCurveAngle ) > 0.001 )
                rsign = Math.abs( parms.perpCurveAngle ) / parms.perpCurveAngle;
            else
                rsign = 1;

            curveAngle = ( parms.curveAngle + gen.getDistribution( 0, parms.curveAngleDist, parms.curveAngleDistType ) ) * Math.PI / 180;
            perpCurveAngle = ( parms.perpCurveAngle + gen.getDistribution( 0, parms.perpCurveAngleDist, parms.perpCurveAngleDistType ) ) * Math.PI / 180;

            if ( Math.abs( curveAngle ) > 0.001 )
                ysign = Math.abs( curveAngle ) / curveAngle;
            else
                ysign = 1;

            if ( Math.abs( perpCurveAngle ) > 0.001 )
                rsign = Math.abs( perpCurveAngle ) / perpCurveAngle;
            else
                rsign = 1;

            curveAngle = Math.abs( curveAngle );
            perpCurveAngle = Math.abs( perpCurveAngle );

            if ( Math.abs( parms.curveAngleBack ) > 0.001 )
            {
                yback = yVert.length / 2 - 1;
                curveAngleBack = ( parms.curveAngleBack + gen.getDistribution( 0, parms.curveAngleDist, parms.curveAngleDistType ) ) * Math.PI / 180;

                if ( Math.abs( curveAngleBack ) > 0.001 )
                    bysign = Math.abs( curveAngleBack ) / curveAngleBack;
                else
                    bysign = 1;

                curveAngleBack = Math.abs( curveAngleBack );
                curveAngle *= 2;
            }

            if ( Math.abs( parms.perpCurveAngleBack ) > 0.001 )
            {
                rback = yVert.length / 2 - 1;
                perpCurveAngleBack = ( parms.perpCurveAngleBack + gen.getDistribution( 0, parms.perpCurveAngleDist, parms.perpCurveAngleDistType ) ) * Math.PI / 180;

                if ( Math.abs( perpCurveAngleBack ) > 0.001 )
                    brsign = Math.abs( perpCurveAngleBack ) / perpCurveAngleBack;
                else
                    brsign = 1;

                perpCurveAngleBack = Math.abs( perpCurveAngleBack );
                perpCurveAngle *= 2;
            }

            segment = 0;
            randomChangeSign = false;

            if ( parms.randomTilt > 0 )
            {
                RrandomTilt = gen.getDistribution( 0, parms.randomTilt, TapRandomGenerator.UNIFORM ) * Math.PI / 180;
                YrandomTilt = gen.getDistribution( 0, parms.randomTilt, TapRandomGenerator.UNIFORM ) * Math.PI / 180;
            }
            else
            {
                RrandomTilt = 0;
                YrandomTilt = 0;
            }

            if ( parms.randomTiltDiv > 0 )
                segment = 1.0 / parms.randomTiltDiv;

            for ( i = 1; i < yVert.length; ++i )
            {
                ratio = ( yPositions[i] - yPositions[i - 1] ) / ( 1.0 - yPositions[i - 1] );
                yRatio = ( yPositions[i] - yPositions[i - 1] ) * ( yVert.length - 1 );
                m = Mat4.identity();

                if ( parms.randomTilt > 0 && parms.randomTiltDiv > 0 )
                {
                    if ( yPositions[i] > segment )
                    {
                        RrandomTilt = gen.getDistribution( 0, parms.randomTilt, TapRandomGenerator.UNIFORM ) * Math.PI / 180;
                        YrandomTilt = gen.getDistribution( 0, parms.randomTilt, TapRandomGenerator.UNIFORM ) * Math.PI / 180;
                        segment += 1.0 / parms.randomTiltDiv;
                        randomChangeSign = false;
                    }
                    else if ( ( yPositions[i] > ( segment - 0.5 / parms.randomTiltDiv ) ) && !randomChangeSign )
                    {
                        RrandomTilt = -RrandomTilt;
                        YrandomTilt = -YrandomTilt;
                        randomChangeSign = true;
                    }
                }

                if ( i <= rback )
                {
                    rangle = perpCurveAngle * ratio * parms.perpCurveRate;
                    perpCurveAngle = perpCurveAngle - rangle;

                    if ( perpCurveAngle < 0 )
                        perpCurveAngle = 0;

                    m = m.times( Mat4.xrotation( rsign * rangle + RrandomTilt * yRatio ) );
                }
                else
                {
                    rangle = perpCurveAngleBack * ratio * parms.perpCurveRate;
                    perpCurveAngleBack = perpCurveAngleBack - rangle;

                    if ( perpCurveAngleBack < 0 )
                        perpCurveAngleBack = 0;

                    m = m.times( Mat4.xrotation( brsign * rangle + RrandomTilt * yRatio ) );
                }

                if ( i <= yback )
                {
                    yangle = -curveAngle * ratio * parms.curveRate;
                    curveAngle = curveAngle + yangle;

                    if ( curveAngle < 0 )
                        curveAngle = 0;

                    m = m.times( Mat4.zrotation( ysign * yangle + YrandomTilt * yRatio ) );
                }
                else
                {
                    yangle = -curveAngleBack * ratio * parms.curveRate;
                    curveAngleBack = curveAngleBack + yangle;

                    if ( curveAngleBack < 0 )
                        curveAngleBack = 0;

                    m = m.times( Mat4.zrotation( bysign * yangle + YrandomTilt * yRatio ) );
                }

                for ( j = i; j < yVert.length; ++j )
                {
                    yVert[j] = yVert[j].minus( yVert[i - 1] );
                    m.transform( yVert[j] );
                    yVert[j] = yVert[j].plus( yVert[i - 1] );
                }
            }
        }

        if ( parms != null )
            for ( i = 0; i < yVert.length; ++i )
                vert[i].r = yVert[i];

        super.setShape( vert, super.getSmoothness(), t );
    }


    /**
     *  Sets the thickness attribute of the TapTube object
     *
     *@param  thickness  The new thickness value
     */
    public void setThickness( double[] thickness )
    {
        for ( int i = 0; i < getVertexPositions().length; ++i )
            thickness[i] = rShape.calcValue( yPositions[i] );

        clearCachedMesh();
    }


    /**
     *  Set the position, smoothness, and thickness values for all points.
     *
     *@param  v           The new shape value
     *@param  smoothness  The new shape value
     *@param  thickness   The new shape value
     */
    public void setShape( MeshVertex[] v, float[] smoothness, double[] thickness )
    {
        super.setShape( v, smoothness, thickness );
        setShape( null );

        //for (int i=0;i<getVertexPositions().length;++i) thickness[i] = rShape.calcValue(yPositions[i]);
        clearCachedMesh();
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Object3D duplicate()
    {
        TapTube newTube = new TapTube( yVert, getSmoothness(), getThickness() );
        newTube.copyObject( this );
        newTube.rShape = rShape.duplicate();
        newTube.yPositions = new double[yPositions.length];
        newTube.yVert = newTube.getVertexPositions();
        newTube.maxY = maxY;
        newTube.rSize = rSize;

        for ( int i = 0; i < yPositions.length; ++i )
            newTube.yPositions[i] = yPositions[i];

        return (Object3D) newTube;
    }


    //rDisplace is reserved for (maybe) future use.
    /**
     *  Gets the position attribute of the TapTube object
     *
     *@param  yPos       Description of the Parameter
     *@param  angle      Description of the Parameter
     *@param  rDisplace  Description of the Parameter
     *@return            The position value
     */
    public Mat4 getPosition( double yPos, double angle, boolean rDisplace )
    {
        int i;
        int j;
        int k;
        int i1;
        int i2;

        i1 = 0;
        i2 = 1;

        int vsize = yVert.length;
        i = (int) Math.round( yPos * vsize );

        if ( i < 0 )
            i = 0;

        if ( i > vsize - 1 )
            i = vsize - 1;

        if ( yPos < 0.0001 )
        {
            i1 = 0;
            i2 = 1;
        }
        else if ( yPos > 0.99999 )
        {
            i1 = vsize - 2;
            i2 = vsize - 1;
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
            while ( yPositions[i] < yPos && i < vsize - 1 )
                i++;

            if ( i == vsize - 2 )
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
            position = new Vec3( yVert[vsize - 1] );

        //double sizeR = rShape.calcValue(yPos);
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


    /*
     *  distorts the tube each time a branch is plugged into it (kind of counter reaction)
     */
    /**
     *  Sets the counterAction attribute of the TapTube object
     *
     *@param  yPos   The new counterAction value
     *@param  angle  The new counterAction value
     *@param  parms  The new counterAction value
     *@return        Description of the Return Value
     */
    public Mat4 setCounterAction( double yPos, double angle, TapDistortParameters parms )
    {
        int i;
        int j;
        int k;
        int i1;
        int i2;
        Vec3 axis;
        Mat4 m;

        i1 = 0;
        i2 = 1;

        int vsize = yVert.length;
        i = (int) Math.round( yPos * vsize );

        if ( i < 0 )
            i = 0;

        if ( i > vsize - 1 )
            i = vsize - 1;

        if ( yPos < 0.0001 )
        {
            Vec3 deriv = yVert[1].minus( yVert[0] );
            deriv.normalize();

            Vec3 yVec = new Vec3( 0, 1, 0 );
            axis = yVec.cross( deriv );

            double matAngle = Math.acos( yVec.dot( deriv ) );
            m = Mat4.axisRotation( axis, matAngle );

            return m;
        }
        else if ( yPos > 0.99999 )
        {
            Vec3 deriv = yVert[yVert.length - 1].minus( yVert[yVert.length - 2] );
            deriv.normalize();

            Vec3 yVec = new Vec3( 0, 1, 0 );
            axis = yVec.cross( deriv );

            double matAngle = Math.acos( yVec.dot( deriv ) );
            m = Mat4.axisRotation( axis, matAngle );

            Mat4 result = Mat4.translation( yVert[yVert.length - 1].x, yVert[yVert.length - 1].y, yVert[yVert.length - 1].z );
            result = result.times( m );

            return result;
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
            while ( yPositions[i] < yPos && i < vsize - 1 )
                i++;

            if ( i == vsize - 2 )
                if ( yPositions[i] < yPos )
                    ++i;

            i1 = i - 1;
            i2 = i;
        }

        Vec3 position = null;
        float[] smooth = super.getSmoothness();
        double[] thick = super.getThickness();
        float[] newSmoothness = null;
        double[] newThickness = null;
        Vec3[] newYVert = null;
        MeshVertex[] meshVert = getVertices();
        MeshVertex[] newMeshVert = null;
        double coef = ( yPos - yPositions[i1] ) / ( yPositions[i2] - yPositions[i1] );
        float insSmooth;
        double insThickness;
        double[] newYPositions = null;

        if ( ( coef > 0.005 ) && ( coef < 0.995 ) )
        {
            position = new Vec3( yVert[i1].x * ( 1 - coef ) + yVert[i2].x * coef, yVert[i1].y * ( 1 - coef ) + yVert[i2].y * coef, yVert[i1].z * ( 1 - coef ) + yVert[i2].z * coef );
            newYVert = new Vec3[yVert.length + 1];
            newSmoothness = new float[yVert.length + 1];
            newThickness = new double[yVert.length + 1];
            insSmooth = (float) ( smooth[i1] * ( 1.0f - coef ) + smooth[i2] * coef );
            insThickness = rShape.calcValue( yPos ) * rSize;
            newYPositions = new double[yVert.length + 1];
            newMeshVert = new MeshVertex[yVert.length + 1];

            for ( i = 0; i <= i1; ++i )
            {
                newYVert[i] = yVert[i];
                newSmoothness[i] = smooth[i];
                newThickness[i] = thick[i];
                newYPositions[i] = yPositions[i];
                newMeshVert[i] = meshVert[i];
            }

            for ( i = i2; i < yVert.length; ++i )
            {
                newYVert[i + 1] = yVert[i];
                newSmoothness[i + 1] = smooth[i];
                newThickness[i + 1] = thick[i];
                newYPositions[i + 1] = yPositions[i];
                newMeshVert[i + 1] = meshVert[i];
            }

            newYVert[i2] = position;
            newSmoothness[i2] = insSmooth;
            newThickness[i2] = insThickness;
            newYPositions[i2] = yPos;
            newMeshVert[i2] = MeshVertex.blend( meshVert[i1], meshVert[i2], 1 - coef, coef );
            yVert = newYVert;
            smooth = newSmoothness;
            thick = newThickness;
            yPositions = newYPositions;
            meshVert = newMeshVert;
        }
        else if ( coef <= 0.005 )
            position = yVert[i1];
        else
            position = yVert[i2];

        double reacAngle = parms.counterAction * Math.PI / 180;
        axis = yVert[i2].minus( yVert[i1] );
        axis.normalize();

        Vec3 deriv = new Vec3( axis );
        Vec3 raxis = new Vec3( 0, 1, 0 );
        m = Mat4.axisRotation( raxis, angle );
        raxis = new Vec3( 1, 0, 0 );
        m.transform( raxis );
        axis = axis.cross( raxis );

        Vec3 naxis = new Vec3( axis );
        m = Mat4.axisRotation( axis, reacAngle );

        for ( i = i2; i < yVert.length; ++i )
        {
            yVert[i] = yVert[i].minus( yVert[i2 - 1] );
            m.transform( yVert[i] );
            yVert[i] = yVert[i].plus( yVert[i2 - 1] );
        }

        double dumAngle;
        double ratio;

        if ( ( i2 + 1 < yVert.length ) && ( parms.recover ) )
        {
            for ( i = i2 + 1; i < yVert.length; ++i )
            {
                ratio = ( yPositions[i] - yPositions[i - 1] ) / ( 1.0 - yPositions[i - 1] );
                dumAngle = reacAngle * ratio * parms.recoverRate;
                reacAngle = reacAngle - dumAngle;

                if ( reacAngle < 0 )
                    reacAngle = 0;

                m = Mat4.axisRotation( naxis, -dumAngle );

                for ( j = i; j < yVert.length; ++j )
                {
                    yVert[j] = yVert[j].minus( yVert[i - 1] );
                    m.transform( yVert[j] );
                    yVert[j] = yVert[j].plus( yVert[i - 1] );
                }
            }
        }

        Vec3 yVec = new Vec3( 0, 1, 0 );
        axis = yVec.cross( deriv );
        m = Mat4.axisRotation( axis, Math.acos( yVec.dot( deriv ) ) );

        Mat4 result = Mat4.translation( position.x, position.y, position.z );
        result = result.times( m );

        for ( i = 0; i < yVert.length; ++i )
            meshVert[i].r = yVert[i];

        super.setShape( meshVert, smooth, thick );

        return result;
    }


    /*
     *  setSize resizes the object whithout registering the scaling parameters into the TapObjectInfo.
     *  Used when value modules are plugged into the size entries of an ojbect module, for example.
     */
    /**
     *  Sets the size attribute of the TapTube object
     *
     *@param  xsize  The new size value
     *@param  ysize  The new size value
     *@param  zsize  The new size value
     */
    public void setSize( double xsize, double ysize, double zsize )
    {
        setSize( xsize, ysize, zsize, -rSize, null );
    }


    /**
     *  Description of the Method
     *
     *@param  size   Description of the Parameter
     *@param  sizeR  Description of the Parameter
     *@param  sizeY  Description of the Parameter
     *@param  parms  Description of the Parameter
     */
    public void resizeAndDistort( Vec3 size, double sizeR, double sizeY, TapDistortParameters parms )
    {
        setSize( size.x, size.y * sizeY, size.z, sizeR, parms );
    }


    /*
     *  setSize keeps track of the resizing in order to be able to scale further decoration processes
     */
    /**
     *  Sets the size attribute of the TapTube object
     *
     *@param  xsize  The new size value
     *@param  ysize  The new size value
     *@param  zsize  The new size value
     *@param  rs     The new size value
     *@param  parms  The new size value
     */
    public void setSize( double xsize, double ysize, double zsize, double rs, TapDistortParameters parms )
    {
        Vec3 size = getBounds().getSize();
        super.setSize( xsize, ysize, zsize );

        if ( rs < 0 )
            rSize = -rs;
        else
            rSize = rSize * rs;

        setShape( parms );
        clearCachedMesh();
    }


    /*
     *  There's no need to regenerate the mesh, it's done by the Tube each time the geometry is changed
     */
    /**
     *  Description of the Method
     */
    public void regenerateMesh()
    {
        //only for TapObject interface
    }


    /**
     *  Gets the plainAoIObject attribute of the TapTube object
     *
     *@return    The plainAoIObject value
     */
    public Object3D getPlainAoIObject()
    {
        return super.duplicate();
    }
}

