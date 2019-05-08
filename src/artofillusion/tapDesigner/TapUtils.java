/*
 *  This class implements utilities
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
import artofillusion.object.TriangleMesh.*;
import artofillusion.texture.*;
import artofillusion.material.*;
import buoy.widget.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 *  Description of the Class
 *
 *@author     pims
 *@created    31 mai 2004
 */
public class TapUtils
{


    /**
     *  Description of the Method
     *
     *@param  ptArray  Description of the Parameter
     *@return          Description of the Return Value
     */
    public static Point[] increasePointArray( Point[] ptArray )
    {
        if ( ptArray == null )
        {
            Point[] tmpArray = new Point[1];
            tmpArray[0] = new Point();
            return tmpArray;
        }
        else
        {
            Point[] tmpArray = new Point[ptArray.length + 1];
            System.arraycopy( ptArray, 0, tmpArray, 0, ptArray.length );
            tmpArray[tmpArray.length - 1] = new Point();
            return tmpArray;
        }
    }


    /**
     *  Description of the Method
     *
     *@param  intArray  Description of the Parameter
     *@return           Description of the Return Value
     */
    public static int[] increaseIntArray( int[] intArray )
    {
        if ( intArray == null )
        {
            int[] tmpArray = new int[1];
            return tmpArray;
        }
        else
        {
            int[] tmpArray = new int[intArray.length + 1];
            System.arraycopy( intArray, 0, tmpArray, 0, intArray.length );
            return tmpArray;
        }
    }


    /**
     *  Description of the Method
     *
     *@param  intArray  Description of the Parameter
     *@param  element   Description of the Parameter
     *@return           Description of the Return Value
     */
    public static int[] deleteIntArrayElement( int[] intArray, int element )
    {
        if ( intArray == null )
            return null;
        else if ( intArray.length == 1 )
            return null;
        else
        {
            int[] tmpArray = new int[intArray.length - 1];
            if ( element > 0 )
                System.arraycopy( intArray, 0, tmpArray, 0, element );
            if ( element < intArray.length - 1 )
                System.arraycopy( intArray, element + 1, tmpArray, element, tmpArray.length - element );
            int i;
            return tmpArray;
        }
    }


    /**
     *  Gets the parentBFrame attribute of the AoIObjectEditWidget object
     *
     *@param  from  Description of the Parameter
     *@return       The parentBFrame value
     */
    public static BFrame getParentBFrame( Widget from )
    {
        Widget w = from.getParent();
        while ( !( w instanceof BFrame ) && ( w != null ) )
            w = w.getParent();
        return (BFrame) w;
    }


    /**
     *  Description of the Method
     *
     *@param  frame  Description of the Parameter
     */
    public static void centerAndSizeWindow( BFrame frame )
    {
        Dimension d1 = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension d2 = frame.getComponent().getSize();
        int x;
        int y;

        x = ( d1.width - d2.width ) / 2;
        y = ( d1.height - d2.height ) / 2;
        if ( x < 0 )
        {
            x = 0;
        }
        if ( y < 0 )
        {
            y = 0;
        }
        frame.setBounds( new Rectangle( x, y, d2.width, d2.height ) );
    }


    /**
     *  Sets the dialogLocation attribute of the TapUtils class
     *
     *@param  frame        The new dialogLocation value
     *@param  parentFrame  The new dialogLocation value
     */
    public static void setDialogLocation( JFrame frame, JFrame parentFrame )
    {
        Point location = new Point();
        location.x = parentFrame.getLocation().x + parentFrame.getWidth() / 2 - frame.getWidth() / 2;
        location.y = parentFrame.getLocation().y + parentFrame.getHeight() / 2 - frame.getHeight() / 2;
        if ( location.x < 0 )
            location.x = 0;
        if ( location.y < 0 )
            location.y = 0;
        frame.setLocation( location );
    }


    /**
     *  Sets the objectAtMinY attribute of the TapUtils class
     *
     *@param  info  The new objectAtMinY value
     */
    public static void setObjectAtMinY( ObjectInfo info )
    {
        BoundingBox bounds = info.object.getBounds();
        Vec3 tr = new Vec3( 0, -bounds.miny, 0 );
        info.coords.setOrigin( tr );
    }


    /**
     *  Description of the Method
     *
     *@param  mesh   Description of the Parameter
     *@param  parms  Description of the Parameter
     */
    public static void distortSplineMesh( SplineMesh mesh, TapDistortParameters parms )
    {
        int i;
        int j;
        int k;
        int usize;
        int vsize;
        int ucenter;
        TapRandomGenerator gen = null;

        if ( parms != null )
            gen = new TapRandomGenerator( parms.seed );
        MeshVertex[] vert = mesh.getVertices();
        usize = mesh.getUSize();
        vsize = mesh.getVSize();
        ucenter = (int) ( usize / 2 );
        //System.out.println("usize ucenter "+usize+ " "+ucenter);
        MeshVertex[][] vertices = new MeshVertex[usize][vsize];
        for ( i = 0; i < usize; ++i )
            for ( j = 0; j < vsize; ++j )
                vertices[i][j] = vert[i + usize * j];
        Vec3[] yVert = new Vec3[vsize];
        for ( i = 0; i < vsize; ++i )
        {
            yVert[i] = vertices[ucenter][i].r;
        }
        double[] yPositions = new double[vsize];
        double[] rPositions = new double[usize];
        double maxY = 0;
        double maxR = 0;
        double localMaxR = 0;
        yPositions[0] = 0;
        for ( i = 1; i < vsize; ++i )
        {
            maxY += yVert[i].distance( yVert[i - 1] );
            yPositions[i] = maxY;
        }
        for ( i = 0; i < vsize; ++i )
        {
            yPositions[i] /= maxY;
            localMaxR = 0;
            for ( j = ucenter + 1; j < usize; ++j )
                localMaxR += vertices[j][i].r.distance( vertices[j - 1][i].r );
            if ( localMaxR > maxR )
                maxR = localMaxR;
        }
        Mat4 m = null;
        Mat4 mr = null;
        if ( parms != null )
        {
            double yangle;
            double rangle;
            double ratio;
            double curveAngle;
            double perpCurveAngle;
            double ysign;
            double rsign;
            double dum;
            double bysign = 1;
            double brsign = 1;
            double curveAngleBack = 0;
            double perpCurveAngleBack = 0;
            int yback = vsize;
            double rback = 1.001;
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
            curveAngle = ( parms.curveAngle + gen.getDistribution( 0, parms.curveAngleDist,
                    parms.curveAngleDistType ) ) * Math.PI / 180;
            if ( Math.abs( curveAngle ) > 0.001 )
                ysign = Math.abs( curveAngle ) / curveAngle;
            else
                ysign = 1;
            curveAngle = Math.abs( curveAngle );
            if ( Math.abs( parms.curveAngleBack ) > 0.001 )
            {
                yback = vsize / 2 - 1;
                curveAngleBack = ( parms.curveAngleBack + gen.getDistribution( 0, parms.curveAngleDist,
                        parms.curveAngleDistType ) ) * Math.PI / 180;
                if ( Math.abs( curveAngleBack ) > 0.001 )
                    bysign = Math.abs( curveAngleBack ) / curveAngleBack;
                else
                    bysign = 1;
                curveAngleBack = Math.abs( curveAngleBack );
                curveAngle *= 2;
            }
            for ( i = 0; i < vsize; ++i )
            {
                perpCurveAngle = ( parms.perpCurveAngle + gen.getDistribution( 0, parms.perpCurveAngleDist,
                        parms.perpCurveAngleDistType ) ) * Math.PI / 180;
                if ( Math.abs( perpCurveAngle ) > 0.001 )
                    rsign = Math.abs( perpCurveAngle ) / perpCurveAngle;
                else
                    rsign = 1;
                perpCurveAngle = Math.abs( perpCurveAngle );
                if ( Math.abs( parms.perpCurveAngleBack ) > 0.001 )
                {
                    rback = 0.501;
                    perpCurveAngleBack = ( parms.perpCurveAngleBack + gen.getDistribution( 0, parms.perpCurveAngleDist,
                            parms.perpCurveAngleDistType ) ) * Math.PI / 180;
                    if ( Math.abs( perpCurveAngleBack ) > 0.001 )
                        brsign = Math.abs( perpCurveAngleBack ) / perpCurveAngleBack;
                    else
                        brsign = 1;
                    perpCurveAngleBack = Math.abs( perpCurveAngleBack );
                    perpCurveAngle *= 2;
                }
                rPositions[ucenter] = 0;
                localMaxR = 0;
                for ( j = ucenter + 1; j < usize; ++j )
                {
                    localMaxR += vertices[j][i].r.distance( vertices[j - 1][i].r );
                    rPositions[j] = localMaxR;
                }
                localMaxR = localMaxR * parms.leafRRatio + maxR * ( 1 - parms.leafRRatio );
                for ( j = ucenter + 1; j < usize; ++j )
                    rPositions[j] /= localMaxR;
                //System.out.println("**************");
                for ( j = ucenter + 1; j < usize; ++j )
                {
                    ratio = ( rPositions[j] - rPositions[j - 1] ) / ( 1.0 - rPositions[j - 1] );
                    m = Mat4.identity();
                    if ( rPositions[j] <= rback )
                    {
                        rangle = perpCurveAngle * ratio * parms.perpCurveRate;
                        perpCurveAngle = perpCurveAngle - rangle;
                        if ( perpCurveAngle < 0 )
                            perpCurveAngle = 0;
                        if ( j == ucenter + 1 )
                            dum = parms.leafDepartureAngle * Math.PI / 180;
                        else
                            dum = 0;
                        m = Mat4.yrotation( rsign * rangle + dum );
                        mr = Mat4.yrotation( -rsign * rangle - dum );
                        //System.out.println("rangle :"+rangle*rsign*180/Math.PI+" ratio :"+ratio+" rPos :"+rPositions[j]);
                    }
                    else
                    {
                        rangle = perpCurveAngleBack * ratio * parms.perpCurveRate;
                        perpCurveAngleBack = perpCurveAngleBack - rangle;
                        if ( perpCurveAngleBack < 0 )
                            perpCurveAngleBack = 0;
                        m = Mat4.yrotation( brsign * rangle );
                        mr = Mat4.yrotation( -brsign * rangle );
                        //System.out.println("brangle :"+rangle*brsign*180/Math.PI+" ratio :"+ratio+" rPos :"+rPositions[j]);
                    }
                    for ( int l = j; l < usize; ++l )
                    {
                        vertices[l][i].r = vertices[l][i].r.minus( yVert[i] );
                        m.transform( vertices[l][i].r );
                        vertices[l][i].r = vertices[l][i].r.plus( yVert[i] );
                        k = 2 * ucenter - l;
                        if ( k >= 0 )
                        {
                            vertices[k][i].r = vertices[k][i].r.minus( yVert[i] );
                            mr.transform( vertices[k][i].r );
                            vertices[k][i].r = vertices[k][i].r.plus( yVert[i] );
                        }
                    }
                    //System.out.println("j k "+j+" "+k);
                }
            }
            segment = 0;
            randomChangeSign = false;
            if ( parms.randomTilt > 0 )
            {
                RrandomTilt = gen.getDistribution( 0, parms.randomTilt,
                        TapRandomGenerator.UNIFORM ) * Math.PI / 180;
                YrandomTilt = gen.getDistribution( 0, parms.randomTilt,
                        TapRandomGenerator.UNIFORM ) * Math.PI / 180;
            }
            else
            {
                RrandomTilt = 0;
                YrandomTilt = 0;
            }
            if ( parms.randomTiltDiv > 0 )
                segment = 1.0 / parms.randomTiltDiv;
            for ( i = 1; i < vsize; ++i )
            {
                ratio = ( yPositions[i] - yPositions[i - 1] ) / ( 1.0 - yPositions[i - 1] );
                yRatio = ( yPositions[i] - yPositions[i - 1] ) * ( vsize - 1 );
                m = Mat4.identity();
                if ( parms.randomTilt > 0 && parms.randomTiltDiv > 0 )
                {
                    if ( yPositions[i] > segment )
                    {
                        RrandomTilt = gen.getDistribution( 0, parms.randomTilt,
                                TapRandomGenerator.UNIFORM ) * Math.PI / 180;
                        /*
                         *  YrandomTilt = gen.getDistribution(0,parms.randomTilt,
                         *  TapRandomGenerator.UNIFORM)*Math.PI/180;
                         */
                        YrandomTilt = 0;
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
                m = m.times( Mat4.yrotation( RrandomTilt * yRatio ) );
                if ( i <= yback )
                {
                    yangle = -curveAngle * ratio * parms.curveRate;
                    curveAngle = curveAngle + yangle;
                    if ( curveAngle < 0 )
                        curveAngle = 0;
                    m = m.times( Mat4.xrotation( ysign * yangle + YrandomTilt * yRatio ) );
                }
                else
                {
                    yangle = -curveAngleBack * ratio * parms.curveRate;
                    curveAngleBack = curveAngleBack + yangle;
                    if ( curveAngleBack < 0 )
                        curveAngleBack = 0;
                    m = m.times( Mat4.xrotation( bysign * yangle + YrandomTilt * yRatio ) );
                }
                for ( j = i; j < vsize; ++j )
                {
                    for ( k = 0; k < usize; ++k )
                    {
                        vertices[k][j].r = vertices[k][j].r.minus( yVert[i - 1] );
                        m.transform( vertices[k][j].r );
                        vertices[k][j].r = vertices[k][j].r.plus( yVert[i - 1] );
                    }
                }
            }

            //Leaf curvature
            yback = vsize;
            if ( Math.abs( parms.leafCurveAngle ) > 0.001 )
                ysign = Math.abs( parms.leafCurveAngle ) / parms.leafCurveAngle;
            else
                ysign = 1;
            curveAngle = ( parms.leafCurveAngle + gen.getDistribution( 0, parms.leafCurveAngleDist,
                    parms.leafCurveAngleDistType ) ) * Math.PI / 180;
            if ( Math.abs( curveAngle ) > 0.001 )
                ysign = Math.abs( curveAngle ) / curveAngle;
            else
                ysign = 1;
            curveAngle = Math.abs( curveAngle );
            if ( Math.abs( parms.leafCurveAngleBack ) > 0.001 )
            {
                yback = vsize / 2 - 1;
                curveAngleBack = ( parms.leafCurveAngleBack + gen.getDistribution( 0, parms.leafCurveAngleDist,
                        parms.leafCurveAngleDistType ) ) * Math.PI / 180;
                if ( Math.abs( curveAngleBack ) > 0.001 )
                    bysign = Math.abs( curveAngleBack ) / curveAngleBack;
                else
                    bysign = 1;
                curveAngleBack = Math.abs( curveAngleBack );
                curveAngle *= 2;
            }
            for ( i = 1; i < vsize; ++i )
            {
                ratio = ( yPositions[i] - yPositions[i - 1] ) / ( 1.0 - yPositions[i - 1] );
                yRatio = ( yPositions[i] - yPositions[i - 1] ) * ( vsize - 1 );
                m = Mat4.identity();
                if ( i <= yback )
                {
                    yangle = -curveAngle * ratio * parms.leafCurveRate;
                    curveAngle = curveAngle + yangle;
                    if ( curveAngle < 0 )
                        curveAngle = 0;
                    m = m.times( Mat4.zrotation( ysign * yangle ) );
                }
                else
                {
                    yangle = -curveAngleBack * ratio * parms.leafCurveRate;
                    curveAngleBack = curveAngleBack + yangle;
                    if ( curveAngleBack < 0 )
                        curveAngleBack = 0;
                    m = m.times( Mat4.zrotation( bysign * yangle ) );
                }
                for ( j = i; j < vsize; ++j )
                {
                    for ( k = 0; k < usize; ++k )
                    {
                        vertices[k][j].r = vertices[k][j].r.minus( yVert[i - 1] );
                        m.transform( vertices[k][j].r );
                        vertices[k][j].r = vertices[k][j].r.plus( yVert[i - 1] );
                    }
                }
            }
            mesh.setShape( vertices, mesh.getUSmoothness(), mesh.getVSmoothness() );
        }
    }


    /**
     *  Description of the Method
     *
     *@param  obj        Description of the Parameter
     *@param  fromScene  Description of the Parameter
     *@param  toScene    Description of the Parameter
     */
    public static void duplicateObjectTextureAndMaterial( ObjectInfo obj, Scene fromScene, Scene toScene )
    {

        Texture tex = obj.object.getTexture();
        if ( tex instanceof LayeredTexture )
        {
            //System.out.println("Layered Mapping");
            LayeredMapping map = (LayeredMapping) obj.object.getTextureMapping();
            Texture layer[] = map.getLayers();
            for ( int j = 0; j < layer.length; j++ )
            {
                for ( int i = 0; i < fromScene.getNumTextures(); i++ )
                {
                    if ( layer[j] == fromScene.getTexture( i ) )
                    {
                        map.setLayer( j, toScene.getTexture( i ) );
                        map.setLayerMapping( j, map.getLayerMapping( j ).duplicate( obj.object, toScene.getTexture( i ) ) );
                    }
                }
            }
            /*
             *  for (int i=0; i < fromScene.getNumTextures(); i++)
             *  {	  if (tex == fromScene.getTexture(i))
             *  {	  obj.object.setTextureMapping(obj.object.getTextureMapping().duplicate(toScene.getTexture(i)));
             *  obj.object.setTexture(toScene.getTexture(i));
             *  }
             *  }
             */
        }
        else if ( tex != null )
        {
            //System.out.println("Usual mapping");
            for ( int i = 0; i < fromScene.getNumTextures(); i++ )
            {
                if ( tex == fromScene.getTexture( i ) )
                {
                    obj.object.setTexture( toScene.getTexture( i ),
                            obj.object.getTextureMapping().duplicate( obj.object, toScene.getTexture( i ) ) );
                }
            }
        }

        Material mat = obj.object.getMaterial();
        if ( mat != null )
        {
            for ( int i = 0; i < fromScene.getNumMaterials(); i++ )
            {
                if ( mat == fromScene.getMaterial( i ) )
                {
                    obj.object.setMaterial( toScene.getMaterial( i ),
                            obj.object.getMaterialMapping().duplicate( obj.object, toScene.getMaterial( i ) ) );
                }
            }
        }
    }


    /**
     *  Selects relevant radio button depending on random distribution
     *
     *@param  type  The random distribution type
     *@param  rURB  The uniform radio button
     *@param  rGRB  The gaussian radio button
     */
    public static void setDistRadioButton( short type, BRadioButton rURB, BRadioButton rGRB )
    {
        switch ( type )
        {
            case TapRandomGenerator.UNIFORM:
                rURB.setState( true );
                break;
            case TapRandomGenerator.GAUSSIAN:
                rGRB.setState( true );
                break;
        }
    }


    /**
     *  Gets the random distribution type depending on the state of gaussian and
     *  uniform radio buttons
     *
     *@param  rURB  uniform radio button
     *@param  rGRB  gaussian radio button
     *@return       The distribution type
     */
    public static short getDistTypeFromRadio( BRadioButton rURB, BRadioButton rGRB )
    {
        if ( rURB.getState() )

            return TapRandomGenerator.UNIFORM;
        else

            return TapRandomGenerator.GAUSSIAN;
    }


    /**
     *  Gets the sampleToolTip attribute of the TapUtils class
     *
     *@return    The sampleToolTip value
     */
    public static Color getToolTipBackground()
    {
        return (Color) UIManager.get( "ToolTip.background" );
    }


    /**
     *  Gets the toolTipBorder attribute of the TapUtils class
     *
     *@return    The toolTipBorder value
     */
    public static Border getToolTipBorder()
    {
        return (Border) UIManager.get( "ToolTip.border" );
    }


    /**
     *  This method calculates the length of the edge of a tri mesh.
     *
     *@param  e  Edge the length of which must be calculated
     *@param  v  The vertices array
     *@return    Edge length
     */
    public static double edgeLength( Edge e, MeshVertex[] v )
    {
        return v[e.v2].r.minus( v[e.v1].r ).length();
    }


    /**
     *  Description of the Method
     *
     *@param  pt  Description of the Parameter
     *@param  v1  Description of the Parameter
     *@param  v2  Description of the Parameter
     *@param  v3  Description of the Parameter
     *@return     Description of the Return Value
     */
    public static boolean ptInTriangle( Vec3 pt, Vec3 v1, Vec3 v2, Vec3 v3 )
    {
        Vec3 n = v2.minus( v1 ).cross( v3.minus( v1 ) );
        Vec3 vert1p = v1.minus( pt );
        Vec3 vert2p = v2.minus( pt );
        double d = vert1p.cross( vert2p ).dot( n );
        if ( d < 0 )
            return false;

        Vec3 vert3p = v3.minus( pt );
        d = vert2p.cross( vert3p ).dot( n );
        if ( d < 0 )
            return false;

        d = vert3p.cross( vert1p ).dot( n );
        if ( d < 0 )
            return false;

        return true;
    }

}

