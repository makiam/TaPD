/*
 *  The TapSplineMesh class represents a parametric surface defined as a tensor product of
 *  spline curves.  These objects are modified SplineMesh for integration into the TaPD designer.
 *  Depending on the selected smoothing method, the surface may either
 *  interpolate or approximate the vertices of the control mesh.
 */
/*
 *  Copyright (C) 1999-2002 by Peter Eastman, (C) 2003 by Francois Guillet
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
import artofillusion.texture.*;
import artofillusion.ui.*;
import java.awt.*;
import java.io.*;
import java.util.Vector;


/**
 *  Description of the Class
 *
 *@author     pims
 *@created    20 juin 2004
 */
public class TapSplineMesh
         extends Object3D
         implements TapObject
{
    double xscale;
    double yscale;
    double zscale;
    float uSmoothness;
    float vSmoothness;
    Vec3[] curvePositions;
    Vector crossSections;
    Vector currentCrossSections;
    TapFunction rShape;
    Curve yCurve;
    Vec3[] yVert;
    double[] yPositions;
    double maxY;
    SplineMesh splineMesh;
    TapDistortParameters dparms;


    /*
     *  instances of this object hold a SplineMesh, they are not SplineMeshes by themselves,
     *  as opposed to TapTube which extend Tube.
     */
    /**
     *  Constructor for the TapSplineMesh object
     *
     *@param  crossSections  Description of the Parameter
     *@param  yCurve         Description of the Parameter
     *@param  rShape         Description of the Parameter
     *@param  us             Description of the Parameter
     *@param  vs             Description of the Parameter
     */
    public TapSplineMesh( Vector crossSections, Curve yCurve, TapFunction rShape, float us, float vs )
    {
        xscale = yscale = zscale = 1.0;
        setShape( crossSections, yCurve, rShape, null, us, vs );
    }


    /**
     *  Constructor for the TapSplineMesh object
     */
    private TapSplineMesh()
    {
        crossSections = new Vector();
        currentCrossSections = new Vector();
    }


    /**
     *  Gets the crossSections attribute of the TapSplineMesh object
     *
     *@return    The crossSections value
     */
    public Vector getCrossSections()
    {
        return crossSections;
    }


    // all these setters are ugly. To modify.
    /**
     *  Sets the crossSections attribute of the TapSplineMesh object
     *
     *@param  cs  The new crossSections value
     */
    public void setCrossSections( Vector cs )
    {
        setShape( cs, yCurve, rShape, null, uSmoothness, vSmoothness );
    }


    /**
     *  Gets the yCurve attribute of the TapSplineMesh object
     *
     *@return    The yCurve value
     */
    public Curve getYCurve()
    {
        return yCurve;
    }


    /**
     *  Sets the ycurve attribute of the TapSplineMesh object
     *
     *@param  yc  The new ycurve value
     */
    public void setYcurve( Curve yc )
    {
        setShape( crossSections, yc, rShape, null, uSmoothness, vSmoothness );
    }


    /**
     *  Gets the rShape attribute of the TapSplineMesh object
     *
     *@return    The rShape value
     */
    public TapFunction getRShape()
    {
        return rShape;
    }


    /**
     *  Sets the rShape attribute of the TapSplineMesh object
     *
     *@param  rs  The new rShape value
     */
    public void setRShape( TapFunction rs )
    {
        setShape( crossSections, yCurve, rs, null, uSmoothness, vSmoothness );
    }


    /**
     *  Sets the uVSmoothness attribute of the TapSplineMesh object
     *
     *@param  us  The new uVSmoothness value
     *@param  vs  The new uVSmoothness value
     */
    public void setUVSmoothness( float us, float vs )
    {
        uSmoothness = us;
        vSmoothness = vs;
        setShape( crossSections, yCurve, rShape, null, uSmoothness, vSmoothness );
    }


    /**
     *  Sets the shape attribute of the TapSplineMesh object
     *
     *@param  parms  The new shape value
     */
    public void setShape( TapDistortParameters parms )
    {
        if ( parms != null )
        {
            dparms = parms;
            setShape( crossSections, yCurve, rShape, parms, uSmoothness, vSmoothness );
        }
        else
            setShape( crossSections, yCurve, rShape, dparms, uSmoothness, vSmoothness );

    }


    /**
     *  Gets the splineMeshUSmoothness attribute of the TapSplineMesh object
     *
     *@return    The splineMeshUSmoothness value
     */
    public float getSplineMeshUSmoothness()
    {
        return uSmoothness;
    }


    /**
     *  Gets the splineMeshVSmoothness attribute of the TapSplineMesh object
     *
     *@return    The splineMeshVSmoothness value
     */
    public float getSplineMeshVSmoothness()
    {
        return vSmoothness;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Object3D duplicate()
    {
        TapSplineMesh mesh = new TapSplineMesh();
        int i;
        int j;

        for ( i = 0; i < crossSections.size(); ++i )
            mesh.crossSections.add( ( (Curve) crossSections.elementAt( i ) ).duplicate() );

        mesh.currentCrossSections = new Vector();

        for ( i = 0; i < currentCrossSections.size(); ++i )
            mesh.currentCrossSections.add( ( (Curve) currentCrossSections.elementAt( i ) ).duplicate() );

        mesh.yCurve = (Curve) yCurve.duplicate();
        mesh.rShape = rShape.duplicate();
        mesh.uSmoothness = uSmoothness;
        mesh.vSmoothness = vSmoothness;
        mesh.xscale = xscale;
        mesh.yscale = yscale;
        mesh.zscale = zscale;
        mesh.maxY = maxY;

        int vLength = yPositions.length;
        mesh.yPositions = new double[vLength];
        mesh.yVert = new Vec3[vLength];

        for ( i = 0; i < vLength; ++i )
        {
            mesh.yPositions[i] = yPositions[i];
            mesh.yVert[i] = new Vec3( yVert[i] );
        }

        mesh.splineMesh = (SplineMesh) splineMesh.duplicate();
        mesh.setTexture( getTexture(), getTextureMapping() );

        return (Object3D) mesh;
    }


    /**
     *  Description of the Method
     *
     *@param  obj  Description of the Parameter
     */
    public void copyObject( Object3D obj )
    {
        TapSplineMesh mesh = (TapSplineMesh) obj;
        int i;
        int j;

        crossSections = new Vector();

        for ( i = 0; i < mesh.crossSections.size(); ++i )
            crossSections.add( ( (Curve) mesh.crossSections.elementAt( i ) ).duplicate() );

        for ( i = 0; i < mesh.currentCrossSections.size(); ++i )
            currentCrossSections.add( ( (Curve) mesh.currentCrossSections.elementAt( i ) ).duplicate() );

        rShape = mesh.rShape.duplicate();
        yCurve = (Curve) mesh.yCurve.duplicate();
        uSmoothness = mesh.uSmoothness;
        vSmoothness = mesh.vSmoothness;
        xscale = mesh.xscale;
        yscale = mesh.yscale;
        zscale = mesh.zscale;
        maxY = mesh.maxY;

        int vLength = yPositions.length;
        yPositions = new double[vLength];
        yVert = new Vec3[vLength];

        for ( i = 0; i < vLength; ++i )
        {
            yPositions[i] = mesh.yPositions[i];
            yVert[i] = new Vec3( mesh.yVert[i] );
        }

        splineMesh = (SplineMesh) mesh.splineMesh.duplicate();
        setTexture( mesh.getTexture(), mesh.getTextureMapping() );
    }


    /*
     *  This method rebuilds the mesh based on new cross sections, y path and size shape.
     */
    /**
     *  Description of the Method
     *
     *@param  parms  Description of the Parameter
     */
    public void updateMesh( TapDistortParameters parms )
    {
        if ( parms != null )
        {
            dparms = parms;
            setShape( crossSections, yCurve, rShape, parms, uSmoothness, vSmoothness );
        }
        else
            setShape( crossSections, yCurve, rShape, dparms, uSmoothness, vSmoothness );
    }


    /*
     *  this method gives a shape to the mesh based on spline parameters and on distortion parameters
     */
    /**
     *  Sets the shape attribute of the TapSplineMesh object
     *
     *@param  crossSections  The new shape value
     *@param  yCurve         The new shape value
     *@param  rShape         The new shape value
     *@param  parms          The new shape value
     *@param  uSmooth        The new shape value
     *@param  vSmooth        The new shape value
     */
    public void setShape( Vector crossSections, Curve yCurve, TapFunction rShape, TapDistortParameters parms, float uSmooth, float vSmooth )
    {
        int i;
        int j;
        int k;
        TapRandomGenerator gen = null;

        if ( parms != null )
            gen = new TapRandomGenerator( parms.seed );

        currentCrossSections = new Vector();

        int vLength = yCurve.getVertices().length;
        curvePositions = new Vec3[vLength];

        int uLength = ( (Curve) crossSections.elementAt( 0 ) ).getVertices().length;
        float[] usmoothness = new float[uLength];
        float[] vsmoothness = new float[vLength];
        Vec3[][] vert = new Vec3[uLength][vLength];
        yVert = ( (Curve) yCurve.duplicate() ).getVertexPositions();
        yPositions = new double[vLength];

        double curYDist = 0;
        maxY = 0;
        yPositions[0] = 0;
        yVert[0].set( yVert[0].x * xscale, yVert[0].y * yscale, yVert[0].z * zscale );

        for ( i = 1; i < yVert.length; ++i )
        {
            yVert[i].set( yVert[i].x * xscale - yVert[0].x, yVert[i].y * yscale - yVert[0].y, yVert[i].z * zscale - yVert[0].z );
            maxY += yVert[i].distance( yVert[i - 1] );
            yPositions[i] = maxY;
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
                ratio = ( yPositions[i] - yPositions[i - 1] ) / ( maxY - yPositions[i - 1] );
                yRatio = ( yPositions[i] - yPositions[i - 1] ) * ( yVert.length - 1 ) / maxY;
                m = Mat4.identity();

                if ( parms.randomTilt > 0 && parms.randomTiltDiv > 0 )
                {
                    if ( yPositions[i] / maxY > segment )
                    {
                        RrandomTilt = gen.getDistribution( 0, parms.randomTilt, TapRandomGenerator.UNIFORM ) * Math.PI / 180;
                        YrandomTilt = gen.getDistribution( 0, parms.randomTilt, TapRandomGenerator.UNIFORM ) * Math.PI / 180;
                        segment += 1.0 / parms.randomTiltDiv;
                        randomChangeSign = false;
                    }
                    else if ( ( yPositions[i] / maxY > ( segment - 0.5 / parms.randomTiltDiv ) ) && !randomChangeSign )
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

        double twistAngle = 0;

        if ( parms != null )
            if ( ( Math.abs( parms.twistTurns ) > 0 ) || ( parms.twistTurnsDist > 0 ) )
                twistAngle = ( parms.twistTurns + gen.getDistribution( 0, parms.twistTurnsDist, parms.twistDistType ) ) * 2 * Math.PI;

        double twist = 0;

        for ( i = 0; i < vLength; i++ )
        {
            vsmoothness[i] = vSmooth;
            yPositions[i] = yPositions[i] / maxY;

            Curve tmpCurve = (Curve) ( (Curve) crossSections.elementAt( i ) ).duplicate();
            Vec3 size = tmpCurve.getBounds().getSize();
            double sizeR = rShape.calcValue( yPositions[i] );
            tmpCurve.setSize( size.x * sizeR * xscale, size.y * yscale, size.z * sizeR * zscale );

            MeshVertex[] meshVert = tmpCurve.getVertices();

            if ( parms != null )
            {
                if ( parms.sectionJitter > 0 )
                {
                    double dum;

                    for ( j = 0; j < meshVert.length; ++j )
                    {
                        dum = gen.getDistribution( 1, parms.sectionJitter, TapRandomGenerator.UNIFORM );

                        if ( dum < 0 )
                            dum = 0;

                        meshVert[j].r = new Vec3( meshVert[j].r.x * dum, meshVert[j].r.y, meshVert[j].r.z * dum );
                    }
                }
            }

            currentCrossSections.add( tmpCurve.duplicate() );
        }

        if ( parms != null )
            for ( i = 0; i < vLength; i++ )
            {
                if ( ( ( Math.abs( parms.twistTurns ) > 0 ) || ( parms.twistTurnsDist > 0 ) ) && ( i > 0 ) )
                {
                    twist = twistAngle * ( yPositions[i] - yPositions[i - 1] );

                    Mat4 twistMat = Mat4.yrotation( twist );

                    for ( k = i; k < vLength; ++k )
                    {
                        MeshVertex[] meshVert = ( (Curve) currentCrossSections.elementAt( k ) ).getVertices();

                        for ( j = 0; j < meshVert.length; ++j )
                            twistMat.transform( meshVert[j].r );
                    }
                }
            }

        for ( i = 0; i < vLength; i++ )
        {
            Vec3 y1 = null;
            Vec3 y2 = null;

            if ( i == 0 )
            {
                y1 = yVert[0];
                y2 = yVert[1];
            }
            else if ( i == vLength - 1 )
            {
                y1 = yVert[i - 1];
                y2 = yVert[i];
            }
            else
            {
                y1 = yVert[i - 1];
                y2 = yVert[i + 1];
            }

            Vec3 deriv = y2.minus( y1 );
            deriv.normalize();

            Vec3 yVec = new Vec3( 0, 1, 0 );
            Vec3 axis = yVec.cross( deriv );
            double angle = Math.acos( yVec.dot( deriv ) );
            m = Mat4.axisRotation( axis, angle );

            Vec3[] curveVert = ( (Curve) currentCrossSections.elementAt( i ) ).getVertexPositions();

            for ( j = 0; j < uLength; j++ )
            {
                Vec3 vec = new Vec3( curveVert[j] );
                m.transform( vec );
                vec = vec.plus( yVert[i] );
                vert[j][i] = vec;
            }
        }

        for ( i = 0; i < uLength; i++ )
            usmoothness[i] = uSmooth;

        this.crossSections = crossSections;
        this.yCurve = yCurve;
        this.rShape = rShape;
        this.uSmoothness = uSmooth;
        this.vSmoothness = vSmooth;
        splineMesh = new SplineMesh( vert, usmoothness, vsmoothness, Mesh.APPROXIMATING, true, false );

        if ( getTexture() != null )
            splineMesh.setTexture( getTexture(), getTextureMapping() );

    }


    /*
     *  regenerates the mesh once the decoration process is finished, to be called by the user function.
     *  This avoids regenrating the mesh each time a counter action is taken into account
     */
    /**
     *  Description of the Method
     */
    public void regenerateMesh()
    {
        int i;
        int j;
        int vLength = yVert.length;
        int uLength = ( (Curve) crossSections.elementAt( 0 ) ).getVertices().length;
        float[] vsmoothness = new float[vLength];
        float[] usmoothness = new float[uLength];
        Vec3[][] vert = new Vec3[uLength][vLength];
        Mat4 m = null;

        for ( i = 0; i < vLength; i++ )
        {
            vsmoothness[i] = vSmoothness;

            Curve tmpCurve = (Curve) ( (Curve) currentCrossSections.elementAt( i ) ).duplicate();
            Vec3 size = tmpCurve.getBounds().getSize();

            Vec3 y1 = null;
            Vec3 y2 = null;

            if ( i == 0 )
            {
                y1 = yVert[0];
                y2 = yVert[1];
            }
            else if ( i == vLength - 1 )
            {
                y1 = yVert[i - 1];
                y2 = yVert[i];
            }
            else
            {
                y1 = yVert[i - 1];
                y2 = yVert[i + 1];
            }

            Vec3 deriv = y2.minus( y1 );
            deriv.normalize();

            Vec3 yVec = new Vec3( 0, 1, 0 );
            Vec3 axis = yVec.cross( deriv );
            double angle = Math.acos( yVec.dot( deriv ) );
            m = Mat4.axisRotation( axis, angle );

            MeshVertex[] meshVert = tmpCurve.getVertices();

            for ( j = 0; j < uLength; j++ )
            {
                Vec3 vec = new Vec3( meshVert[j].r );
                m.transform( vec );
                vec = vec.plus( yVert[i] );
                vert[j][i] = vec;
            }
        }

        for ( i = 0; i < uLength; i++ )
            usmoothness[i] = uSmoothness;

        splineMesh = new SplineMesh( vert, usmoothness, vsmoothness, Mesh.APPROXIMATING, true, false );

        if ( getTexture() != null )
            splineMesh.setTexture( getTexture(), getTextureMapping() );
    }


    /*
     *  distorts the spline mesh each time a branch is plugged into it (kind of counter reaction)
     */
    /**
     *  Sets the counterAction attribute of the TapSplineMesh object
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

        int vLength = yPositions.length;
        float[] usmoothness = splineMesh.getUSmoothness();
        float[] vsmoothness = splineMesh.getVSmoothness();
        i1 = 0;
        i2 = 1;
        i = (int) Math.round( yPos * vLength );

        if ( i < 0 )
            i = 0;

        if ( i > vLength - 1 )
            i = vLength - 1;

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
            while ( yPositions[i] < yPos && i < vLength - 1 )
                i++;

            if ( i == vLength - 2 )
                if ( yPositions[i] < yPos )
                    ++i;

            i1 = i - 1;
            i2 = i;
        }

        Vec3 position = null;
        float[] newVsmoothness = null;
        Vec3[] newYVert = null;
        double coef = ( yPos - yPositions[i1] ) / ( yPositions[i2] - yPositions[i1] );

        if ( ( coef > 0.005 ) && ( coef < 0.995 ) )
        {
            position = new Vec3( yVert[i1].x * ( 1 - coef ) + yVert[i2].x * coef, yVert[i1].y * ( 1 - coef ) + yVert[i2].y * coef, yVert[i1].z * ( 1 - coef ) + yVert[i2].z * coef );
            newYVert = new Vec3[yVert.length + 1];
            newVsmoothness = new float[yVert.length + 1];

            for ( i = 0; i <= i1; ++i )
            {
                newYVert[i] = yVert[i];
                newVsmoothness[i] = vSmoothness;
            }

            for ( i = i2; i < yVert.length; ++i )
            {
                newYVert[i + 1] = yVert[i];
                newVsmoothness[i + 1] = vSmoothness;
            }

            newYVert[i2] = position;
            newVsmoothness[i2] = vSmoothness;
            yVert = newYVert;
            vsmoothness = newVsmoothness;

            Vec3[] ci1 = ( (Curve) currentCrossSections.elementAt( i1 ) ).getVertexPositions();
            Vec3[] ci2 = ( (Curve) currentCrossSections.elementAt( i2 ) ).getVertexPositions();
            Curve newCurve = (Curve) ( (Curve) currentCrossSections.elementAt( i1 ) ).duplicate();
            Vec3[] newCurveVert = newCurve.getVertexPositions();

            for ( i = 0; i < newCurveVert.length; ++i )
                newCurveVert[i] = new Vec3( ci1[i].x * ( 1 - coef ) + ci2[i].x * coef, ci1[i].y * ( 1 - coef ) + ci2[i].y * coef, ci1[i].z * ( 1 - coef ) + ci2[i].z * coef );

            newCurve.setVertexPositions( newCurveVert );
            currentCrossSections.add( i2, newCurve );
            maxY = 0;
            yPositions = new double[yVert.length];
            yPositions[0] = 0;

            for ( i = 1; i < yVert.length; ++i )
            {
                maxY += yVert[i].distance( yVert[i - 1] );
                yPositions[i] = maxY;
            }

            for ( i = 1; i < yVert.length; ++i )
                yPositions[i] /= maxY;
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

        return result;
    }


    /**
     *  Gets the position attribute of the TapSplineMesh object
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

        double sizeR = rShape.calcValue( yPos );
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
     *  Gets the yCurveOrigin attribute of the TapSplineMesh object
     *
     *@return    The yCurveOrigin value
     */
    public Vec3 getYCurveOrigin()
    {
        return new Vec3( yVert[0] );
    }


    /**
     *  Constructor for the TapSplineMesh object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public TapSplineMesh( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        int i;
        int j;
        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        xscale = in.readDouble();
        yscale = in.readDouble();
        zscale = in.readDouble();

        int count = in.readInt();
        crossSections = new Vector( count );

        for ( i = 0; i < count; ++i )
            crossSections.add( new Curve( in, theScene ) );

        rShape = new TapFunction( in );
        yCurve = new Curve( in, theScene );
        uSmoothness = in.readFloat();
        vSmoothness = in.readFloat();
        dparms = null;
        setShape( crossSections, yCurve, rShape, null, uSmoothness, vSmoothness );
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

        int i;
        int j;

        out.writeShort( 0 );
        out.writeDouble( xscale );
        out.writeDouble( yscale );
        out.writeDouble( zscale );
        out.writeInt( crossSections.size() );

        for ( i = 0; i < crossSections.size(); i++ )
            ( (Curve) crossSections.elementAt( i ) ).writeToFile( out, theScene );

        rShape.writeToFile( out );
        yCurve.writeToFile( out, theScene );
        out.writeFloat( uSmoothness );
        out.writeFloat( vSmoothness );
    }


    /**
     *  Sets the size attribute of the TapSplineMesh object
     *
     *@param  xsize  The new size value
     *@param  ysize  The new size value
     *@param  zsize  The new size value
     */
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
    public void resizeAndDistort( Vec3 size, double sizeR, double sizeY, TapDistortParameters parms )
    {
        setSize( size.x * sizeR, size.y * sizeY, size.z * sizeR, parms );
    }


    /**
     *  Sets the size attribute of the TapSplineMesh object
     *
     *@param  xsize  The new size value
     *@param  ysize  The new size value
     *@param  zsize  The new size value
     *@param  parms  The new size value
     */
    public void setSize( double xsize, double ysize, double zsize, TapDistortParameters parms )
    {
        Vec3 size = splineMesh.getBounds().getSize();
        double nxscale;
        double nyscale;
        double nzscale;

        if ( size.x == 0.0 )
            nxscale = 1.0;
        else
            nxscale = xsize / size.x;

        if ( size.y == 0.0 )
            nyscale = 1.0;
        else
            nyscale = ysize / size.y;

        if ( size.z == 0.0 )
            nzscale = 1.0;
        else
            nzscale = zsize / size.z;

        xscale *= nxscale;
        yscale *= nyscale;
        zscale *= nzscale;
        updateMesh( parms );
    }


    /*
     *  these functions are for compatibility with the Object3D class
     */
    /**
     *  Gets the uSize attribute of the TapSplineMesh object
     *
     *@return    The uSize value
     */
    public int getUUSize()
    {
        return ( (Curve) crossSections.elementAt( 0 ) ).getVertices().length;
    }


    /**
     *  Gets the vSize attribute of the TapSplineMesh object
     *
     *@return    The vSize value
     */
    public int getVVSize()
    {
        return yVert.length;
    }


    /**
     *  Gets the bounds attribute of the TapSplineMesh object
     *
     *@return    The bounds value
     */
    public BoundingBox getBounds()
    {
        return splineMesh.getBounds();
    }


    /**
     *  Gets the closed attribute of the TapSplineMesh object
     *
     *@return    The closed value
     */
    public boolean isClosed()
    {
        return splineMesh.isClosed();
    }


    /**
     *  Sets the texture attribute of the TapSplineMesh object
     *
     *@param  tex      The new texture value
     *@param  mapping  The new texture value
     */
    public void setTexture( Texture tex, TextureMapping mapping )
    {
        if ( splineMesh != null )
            splineMesh.setTexture( tex, mapping );
        super.setTexture( tex, mapping );
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean isEditable()
    {
        return false;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public int canConvertToTriangleMesh()
    {
        return splineMesh.canConvertToTriangleMesh();
    }


    /**
     *  Description of the Method
     *
     *@param  tol  Description of the Parameter
     *@return      Description of the Return Value
     */
    public TriangleMesh convertToTriangleMesh( double tol )
    {
        return splineMesh.convertToTriangleMesh( tol );
    }


    /**
     *  Gets the renderingMesh attribute of the TapSplineMesh object
     *
     *@param  tol          Description of the Parameter
     *@param  interactive  Description of the Parameter
     *@param  info         Description of the Parameter
     *@return              The renderingMesh value
     */
    public RenderingMesh getRenderingMesh( double tol, boolean interactive, ObjectInfo info )
    {
        return splineMesh.getRenderingMesh( tol, interactive, info );
    }


    /**
     *  Gets the wireframeMesh attribute of the TapSplineMesh object
     *
     *@return    The wireframeMesh value
     */
    public WireframeMesh getWireframeMesh()
    {
        return splineMesh.getWireframeMesh();
    }


    /**
     *  Gets the plainAoIObject attribute of the TapSplineMesh object
     *
     *@return    The plainAoIObject value
     */
    public Object3D getPlainAoIObject()
    {
        return splineMesh.duplicate();
    }


    /**
     *  Gets the poseKeyframe attribute of the TapSplineMesh object
     *
     *@return    The poseKeyframe value
     */
    public Keyframe getPoseKeyframe()
    {
        return null;
    }


    /**
     *  Description of the Method
     *
     *@param  k  Description of the Parameter
     */
    public void applyPoseKeyframe( Keyframe k )
    {
    }

    /*
     *  public MeshVertex[] getVertices()
     *  {
     *  return splineMesh.getVertices();
     *  }
     */
    /*
     *  public void setVertices( Vec3 v[] )
     *  {
     *  splineMesh.setVertices( v );
     *  /won't last till next spline mesh generation
     *  /provided in doubt for compatibility
     *  }
     */
    /*
     *  public Vec3[] getNormals()
     *  {
     *  return splineMesh.getNormals();
     *  }
     */
    /*
     *  public Skeleton getSkeleton()
     *  {
     *  return null;
     *  }
     */
    /*
     *  public void setSkeleton( Skeleton s )
     *  {
     *  }
     */
    /*
     *  public void setParameterValue( TextureParameter param, ParameterValue val )
     *  {
     *  super.setParameterValue( param, val );
     *  if ( splineMesh != null )
     *  splineMesh.setParameterValue( param, val );
     *  }
     */
    /*
     *  public void setParameterValues( ParameterValue val[] )
     *  {
     *  super.setParameterValues( val );
     *  if ( splineMesh != null )
     *  splineMesh.setParameterValues( val );
     *  }
     */
    /*
     *  public void setParameters( TextureParameter param[] )
     *  {
     *  super.setParameters( param );
     *  if ( splineMesh != null )
     *  splineMesh.setParameters( param );
     *  }
     */
}

