/*
 *  This represents a golden ratio module, which decorates any object sent in second entry port
 *  with decorator object sent in first entry port according to golden ratio rules.
 */
/*
 *  Copyright (C) 2003 by Francois Guillet
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
import artofillusion.texture.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import artofillusion.tapDesigner.TapModule.*;


/**
 *  This modules layouts decorator objects along a sphere, following the golden
 *  ratio rule.
 *
 *@author     Francois Guillet
 *@created    19 avril 2004
 */
public class GoldenBallModule
         extends TapModule
{
    private GoldenBallModule module;
    private static TapModule.ModuleTypeInfo typeInfo;

    private double rangeFrom;
    private double rangeTo;
    private double sizeDist;
    private double densityParameter;
    private double yPortValue;
    private boolean fullSphere;
    private boolean sizeChildren;
    private double rSizeFactor;
    private double ySizeFactor;
    private double inward;
    private int estimate;
    private short sizeDistType;
    private TapDistortParameters smParms;
    private TapFunction curveAngleFunction;
    private TapFunction backAngleFunction;
    private TapFunction perpAngleFunction;
    private TapFunction curveRateFunction;
    private TapFunction densityFunction;
    private TapFunction yCurveLeafFunction;
    private TapFunction yLeafDepartureAngleFunction;
    private final static double gr = 2 * Math.PI / ( ( 1 + Math.sqrt( 5 ) ) / 2 );
    private boolean curveAngleFunctionInput;
    private boolean backAngleFunctionInput;
    private boolean perpAngleFunctionInput;
    private boolean densityFunctionInput;
    private boolean curveRateFunctionInput;
    private boolean yCurveLeafFunctionInput;
    private boolean yLeafDepartureAngleFunctionInput;


    /**
     *  Constructor for the GoldenBallModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public GoldenBallModule( TapProcedure procedure, Point position )
    {
        super( procedure, TapDesignerTranslate.text( "goldenBall" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "goldenBallName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/golden_tree.png" ) ) );

        setNumInput( 5 );
        setNumOutput( 2 );
        rangeFrom = 0;
        rangeTo = 1;
        sizeDist = 0.0;
        sizeDistType = TapRandomGenerator.UNIFORM;
        fullSphere = false;
        sizeChildren = true;
        rSizeFactor = 1.0;
        ySizeFactor = 1.0;
        densityParameter = 1.0;
        estimate = 10;
        inward = 0.0;
        smParms = new TapDistortParameters();
        curveAngleFunction = new TapFunction( 1.0, 1.0 );
        curveRateFunction = new TapFunction( 1.0, 1.0 );
        backAngleFunction = new TapFunction( 1.0, 1.0 );
        perpAngleFunction = new TapFunction( 1.0, 1.0 );
        densityFunction = new TapFunction( 1.0, 1.0 );
        yCurveLeafFunction = new TapFunction( 1.0, 1.0 );
        yLeafDepartureAngleFunction = new TapFunction( 1.0, 1.0 );
        curveAngleFunctionInput = false;
        backAngleFunctionInput = false;
        perpAngleFunctionInput = false;
        densityFunctionInput = false;
        curveRateFunctionInput = false;
        yCurveLeafFunctionInput = false;
        yLeafDepartureAngleFunctionInput = false;
        setup();
    }


    /**
     *  Description of the Method
     */
    private void setup()
    {
        inputNature[0] = OBJECT_PORT;
        inputNature[1] = OBJECT_PORT;
        inputNature[2] = VALUE_PORT;
        inputNature[3] = VALUE_PORT;
        inputNature[4] = VALUE_PORT;
        outputNature[0] = OBJECT_PORT;
        outputNature[1] = VALUE_PORT;
        inputTooltips = new String[5];
        inputTooltips[0] = TapDesignerTranslate.text( "objectDecorate" );
        inputTooltips[1] = TapDesignerTranslate.text( "objectDecorated" );
        inputTooltips[2] = TapDesignerTranslate.text( "yValueInput" );
        inputTooltips[3] = TapDesignerTranslate.text( "rSizeValue" );
        inputTooltips[4] = TapDesignerTranslate.text( "ySizeValue" );
        outputTooltips = new String[2];
        outputTooltips[0] = TapDesignerTranslate.text( "objectOutput" );
        outputTooltips[1] = TapDesignerTranslate.text( "yValueOutput" );
        setBackgroundColor( Color.orange.darker() );
        module = this;
    }


    /**
     *  Constructor for the GoldenBallModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public GoldenBallModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        rangeFrom = in.readDouble();
        rangeTo = in.readDouble();
        sizeDist = in.readDouble();
        sizeDistType = in.readShort();
        densityParameter = in.readDouble();
        fullSphere = in.readBoolean();
        sizeChildren = in.readBoolean();
        rSizeFactor = in.readDouble();
        ySizeFactor = in.readDouble();
        estimate = in.readInt();
        inward = in.readDouble();
        smParms = new TapDistortParameters( in );
        curveAngleFunction = new TapFunction( in );
        curveRateFunction = new TapFunction( in );
        backAngleFunction = new TapFunction( in );
        perpAngleFunction = new TapFunction( in );
        densityFunction = new TapFunction( in );
        yCurveLeafFunction = new TapFunction( in );
        yLeafDepartureAngleFunction = new TapFunction( in );
        curveAngleFunctionInput = in.readBoolean();
        backAngleFunctionInput = in.readBoolean();
        perpAngleFunctionInput = in.readBoolean();
        densityFunctionInput = in.readBoolean();
        curveRateFunctionInput = in.readBoolean();
        yCurveLeafFunctionInput = in.readBoolean();
        yLeafDepartureAngleFunctionInput = in.readBoolean();
        setup();
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
        out.writeDouble( rangeFrom );
        out.writeDouble( rangeTo );
        out.writeDouble( sizeDist );
        out.writeShort( sizeDistType );
        out.writeDouble( densityParameter );
        out.writeBoolean( fullSphere );
        out.writeBoolean( sizeChildren );
        out.writeDouble( rSizeFactor );
        out.writeDouble( ySizeFactor );
        out.writeInt( estimate );
        out.writeDouble( inward );
        smParms.writeToFile( out );
        curveAngleFunction.writeToFile( out );
        curveRateFunction.writeToFile( out );
        backAngleFunction.writeToFile( out );
        perpAngleFunction.writeToFile( out );
        densityFunction.writeToFile( out );
        yCurveLeafFunction.writeToFile( out );
        yLeafDepartureAngleFunction.writeToFile( out );
        out.writeBoolean( curveAngleFunctionInput );
        out.writeBoolean( backAngleFunctionInput );
        out.writeBoolean( perpAngleFunctionInput );
        out.writeBoolean( densityFunctionInput );
        out.writeBoolean( curveRateFunctionInput );
        out.writeBoolean( yCurveLeafFunctionInput );
        out.writeBoolean( yLeafDepartureAngleFunctionInput );
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public TapModule duplicate()
    {
        GoldenBallModule module = new GoldenBallModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
        module.rangeFrom = this.rangeFrom;
        module.rangeTo = this.rangeTo;
        module.sizeDist = this.sizeDist;
        module.sizeDistType = this.sizeDistType;
        module.densityParameter = this.densityParameter;
        module.fullSphere = this.fullSphere;
        module.sizeChildren = this.sizeChildren;
        module.rSizeFactor = this.rSizeFactor;
        module.ySizeFactor = this.ySizeFactor;
        module.estimate = this.estimate;
        module.inward = this.inward;
        module.smParms = this.smParms.duplicate();
        module.curveAngleFunction = curveAngleFunction.duplicate();
        module.curveRateFunction = curveRateFunction.duplicate();
        module.backAngleFunction = backAngleFunction.duplicate();
        module.perpAngleFunction = perpAngleFunction.duplicate();
        module.densityFunction = densityFunction.duplicate();
        module.yCurveLeafFunction = yCurveLeafFunction.duplicate();
        module.yLeafDepartureAngleFunction = yLeafDepartureAngleFunction.duplicate();
        module.curveAngleFunctionInput = curveAngleFunctionInput;
        module.backAngleFunctionInput = backAngleFunctionInput;
        module.perpAngleFunctionInput = perpAngleFunctionInput;
        module.densityFunctionInput = densityFunctionInput;
        module.curveRateFunctionInput = curveRateFunctionInput;
        module.yCurveLeafFunctionInput = yCurveLeafFunctionInput;
        module.yLeafDepartureAngleFunctionInput = yLeafDepartureAngleFunctionInput;

        return (TapModule) module;
    }


    /**
     *  Description of the Method
     *
     *@param  inputPort  Description of the Parameter
     *@return            Description of the Return Value
     */
    public int remap( int inputPort )
    {
        return inputPort;
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void edit( BFrame parentFrame )
    {
        super.edit( parentFrame );

        if ( isEditDialogOn )
            editDialog.toFront();
        else
        {
            editDialog = new GoldenBallModuleDialog( (JFrame) parentFrame.getComponent() );
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
        //A golden ratio module is not asked;
        else

            return null;
    }


    /**
     *  Gets the decoratedDecorator attribute of the GoldenBallModule object
     *
     *@param  gen       Description of the Parameter
     *@param  sR        Description of the Parameter
     *@param  sY        Description of the Parameter
     *@param  parms     Description of the Parameter
     *@param  level     Description of the Parameter
     *@param  evaluate  Description of the Parameter
     *@param  yVal      Description of the Parameter
     *@param  rVal      Description of the Parameter
     *@return           The decoratedDecorator value
     */
    private TapDesignerObjectCollection getDecoratedDecorator( TapRandomGenerator gen, double sR, double sY, TapDistortParameters parms, int level, boolean evaluate, double yVal, double rVal )
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
        double[] yValArray = new double[1];
        yValArray[0] = yVal;

        BackModuleLink backLinks = procedure.getBackLink();
        BackModuleLink.BackLink bl = backLinks.findModule( this, 0 );

        if ( bl != null )
        {
            col = bl.fromModule.getObject( bl.outputPort, gen.getSeed() );
            anInfo = col.elementAt( 0 ).objectInfo;
            if ( anInfo.object != null )
            {
                Object3D obj = (Object3D) anInfo.object;
                TextureMapping map = obj.getTextureMapping();
                TextureParameter[] tparms = map.getParameters();
                for ( int j = 0; j < tparms.length; ++j )
                {
                    if ( tparms[j].name.equals( "YTAPD" ) )
                    {
                        ParameterValue val = obj.getParameterValue( tparms[j] );
                        if ( val instanceof ConstantParameterValue )
                            ( (ConstantParameterValue) val ).setValue( yVal );
                    }
                    else if ( tparms[j].name.equals( "RTAPD" ) )
                    {
                        ParameterValue val = obj.getParameterValue( tparms[j] );
                        if ( val instanceof ConstantParameterValue )
                            ( (ConstantParameterValue) val ).setValue( rVal );
                    }
                }
            }

            if ( bl.fromModule instanceof ObjectModule )
                duplicate = ( (ObjectModule) bl.fromModule ).isDuplicate();
            else
                duplicate = false;

            objName = bl.fromModule.getName();
            sizeR = 1.0;
            sizeY = 1.0;

            if ( !duplicate )
            {
                bl = backLinks.findModule( this, 3 );

                if ( bl != null )
                    sizeR = bl.fromModule.getValue( bl.outputPort, yValArray, gen.getSeed() );

                bl = backLinks.findModule( this, 4 );

                if ( bl != null )
                    sizeY = bl.fromModule.getValue( bl.outputPort, yValArray, gen.getSeed() );

                if ( sizeChildren )
                {
                    sizeR *= sR * rSizeFactor + 1 - rSizeFactor;
                    sizeY *= sY * ySizeFactor + 1 - ySizeFactor;
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

                        if ( parms != null )
                            TapUtils.distortSplineMesh( (SplineMesh) anInfo.object, parms );
                    }
                    else if ( !( anInfo.object instanceof TapObject ) )
                    {
                        anInfo.object.setSize( size.x * sizeR, size.y * sizeY, size.z * sizeR );
                        TapUtils.setObjectAtMinY( anInfo );
                    }
                    else
                        ( (TapObject) anInfo.object ).resizeAndDistort( size, sizeR, sizeY, parms );
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
    public TapDesignerObjectCollection getObject( TapDesignerObjectCollection collection, int inputPort, long seed )
    {
        double Ysize;
        Mat4 m;
        Mat4 msm;
        Vec3 size;
        ObjectInfo anInfo;
        ObjectInfo mainObject;
        ObjectInfo dummyInfo;
        int i;
        int j;
        int k;
        int level;
        int count;
        int objectNumber;
        int phicount;
        double theta;
        double phi;
        double radius;
        double cs1;
        double csdiff;
        double from;
        double to;
        double sizeR;
        double sizeY;
        double objectArea;
        double yRefValue;

        CoordinateSystem dummyCS = new CoordinateSystem();
        CoordinateSystem coords = null;
        TapDesignerObjectCollection col = null;
        TapDesignerObjectCollection tmpCollection = null;
        level = collection.elementAt( 0 ).getDecorationLevel();

        TapRandomGenerator gen = new TapRandomGenerator( seed );
        BackModuleLink.BackLink bl;
        double dum;
        double thetaFactor;
        double thetaOrigin;
        double normalize;
        double densityInput;
        BoundingBox bounds;
        TapDistortParameters tmpParms;
        Vec3 tr = null;
        boolean go_on;
        TapFunction inverseDFunction = densityFunction.getOneOverFunction();
        double[] yVal = new double[1];

        if ( inputPort == 1 )
        {
            TapDesignerObjectCollection newCollection = new TapDesignerObjectCollection( procedure );
            BackModuleLink backLinks = procedure.getBackLink();
            mainObject = collection.elementAt( 0 ).objectInfo;
            size = mainObject.object.getBounds().getSize();
            sizeR = collection.elementAt( 0 ).sizeR;
            sizeY = collection.elementAt( 0 ).sizeY;
            Ysize = size.y;
            theta = rangeFrom * Math.PI / 2;
            phi = 0;
            tmpParms = smParms.duplicate();

            //find out maximum size
            objectArea = 0;
            radius = 0;
            count = 0;

            for ( i = 0; i < estimate; ++i )
            {
                tmpCollection = getDecoratedDecorator( gen, sizeR, sizeY, null, level, true, 0, 0 );

                if ( tmpCollection != null )
                {
                    dummyInfo = new ObjectInfo( tmpCollection, dummyCS, "dummy" );
                    tmpCollection.sceneChanged( dummyInfo, procedure.getScene() );
                    //trick the Object Collection for calculating size
                    size = tmpCollection.getBounds().getSize();
                    dum = size.x;
                    if ( dum < 0.005 )
                        dum = 0.005;
                    if ( size.z > 0.005 )
                        dum = dum * size.z;
                    else
                        dum = dum * 0.005;
                    radius += size.y;
                    ++count;

                    if ( objectArea < dum )
                        objectArea = dum;
                }
            }

            if ( objectArea < 0.00001 )

                return null;

            radius /= count;

            if ( radius < 0.0001 )

                return null;

            if ( fullSphere )
            {
                from = rangeFrom * Math.PI;
                to = rangeTo * Math.PI;
                thetaFactor = Math.PI;
                thetaOrigin = 0.5;
            }
            else
            {
                from = rangeFrom * Math.PI / 2;
                to = rangeTo * Math.PI / 2;
                thetaFactor = Math.PI / 2;
                thetaOrigin = 1;
            }

            cs1 = Math.cos( from );
            csdiff = cs1 - Math.cos( to );

            //do the decoration
            bl = backLinks.findModule( this, 2 );
            densityInput = 0;
            yVal[0] = 0;

            if ( bl != null )
            {
                yRefValue = bl.fromModule.getValue( bl.outputPort, yVal, gen.getSeed() );
                normalize = inverseDFunction.calcValue( yRefValue );
                densityInput = densityFunction.calcValue( yRefValue );
            }
            else
                normalize = inverseDFunction.calcIntegral( rangeFrom, rangeTo );

            objectNumber = (int) ( ( 2 * radius * radius * ( Math.cos( from ) - Math.cos( to ) ) * densityParameter ) / ( objectArea * normalize ) );

            //System.out.println("objectNumber : "+objectNumber);
            if ( bl != null )
                dum = cs1 - csdiff / objectNumber;
            else
                dum = cs1 - csdiff * inverseDFunction.calcValue( 0 ) / objectNumber;

            theta = Math.asin( dum );
            phicount = 1;
            go_on = true;

            while ( go_on )
            {
                //dum = gen.uniformDeviate();
                m = Mat4.identity();
                m = m.times( Mat4.yrotation( phi ) );
                m = m.times( Mat4.zrotation( Math.PI / 2 - theta ) );
                yPortValue = ( thetaOrigin - theta / thetaFactor - rangeFrom ) / ( rangeTo - rangeFrom );
                yVal[0] = yPortValue;
                bl = backLinks.findModule( this, 2 );

                if ( bl != null )
                    yRefValue = bl.fromModule.getValue( bl.outputPort, yVal, gen.getSeed() );
                else
                    yRefValue = yPortValue;

                if ( curveAngleFunctionInput )
                {
                    tmpParms.curveAngle = smParms.curveAngle * curveAngleFunction.calcValue( yRefValue );
                    tmpParms.curveAngleDist = smParms.curveAngleDist * curveAngleFunction.calcValue( yRefValue );
                }
                else
                {
                    tmpParms.curveAngle = smParms.curveAngle * curveAngleFunction.calcValue( yPortValue );
                    tmpParms.curveAngleDist = smParms.curveAngleDist * curveAngleFunction.calcValue( yPortValue );
                }
                if ( curveRateFunctionInput )
                    tmpParms.curveRate = smParms.curveRate * curveRateFunction.calcValue( yRefValue );
                else
                    tmpParms.curveRate = smParms.curveRate * curveRateFunction.calcValue( yPortValue );

                if ( backAngleFunctionInput )
                    tmpParms.curveAngleBack = smParms.curveAngleBack * backAngleFunction.calcValue( yRefValue );
                else
                    tmpParms.curveAngleBack = smParms.curveAngleBack * backAngleFunction.calcValue( yPortValue );

                if ( perpAngleFunctionInput )
                {
                    tmpParms.perpCurveAngle = smParms.perpCurveAngle * perpAngleFunction.calcValue( yRefValue );
                    tmpParms.perpCurveAngleBack = smParms.perpCurveAngleBack * perpAngleFunction.calcValue( yRefValue );
                    tmpParms.perpCurveAngleDist = smParms.perpCurveAngleDist * perpAngleFunction.calcValue( yRefValue );
                }
                else
                {
                    tmpParms.perpCurveAngle = smParms.perpCurveAngle * perpAngleFunction.calcValue( yPortValue );
                    tmpParms.perpCurveAngleBack = smParms.perpCurveAngleBack * perpAngleFunction.calcValue( yPortValue );
                    tmpParms.perpCurveAngleDist = smParms.perpCurveAngleDist * perpAngleFunction.calcValue( yPortValue );
                }

                if ( yCurveLeafFunctionInput )
                {
                    tmpParms.leafCurveAngle = smParms.leafCurveAngle * yCurveLeafFunction.calcValue( yRefValue );
                    tmpParms.leafCurveAngleDist = smParms.leafCurveAngleDist * yCurveLeafFunction.calcValue( yRefValue );
                    tmpParms.leafCurveAngleBack = smParms.leafCurveAngleBack * yCurveLeafFunction.calcValue( yRefValue );
                }
                else
                {
                    tmpParms.leafCurveAngle = smParms.leafCurveAngle * yCurveLeafFunction.calcValue( yPortValue );
                    tmpParms.leafCurveAngleDist = smParms.leafCurveAngleDist * yCurveLeafFunction.calcValue( yPortValue );
                    tmpParms.leafCurveAngleBack = smParms.leafCurveAngleBack * yCurveLeafFunction.calcValue( yPortValue );
                }

                if ( yLeafDepartureAngleFunctionInput )
                    tmpParms.leafDepartureAngle = smParms.leafDepartureAngle * yLeafDepartureAngleFunction.calcValue( yRefValue );
                else
                    tmpParms.leafDepartureAngle = smParms.leafDepartureAngle * yLeafDepartureAngleFunction.calcValue( yPortValue );

                tmpParms.seed = gen.getSeed();
                tmpCollection = getDecoratedDecorator( gen, sizeR, sizeY, tmpParms, level, false, yRefValue, phi / ( 2 * Math.PI ) );
                count = tmpCollection.size();
                tr = new Vec3( 0, Ysize * ( 1 - inward ), 0 );
                msm = null;

                if ( mainObject.object instanceof TapObject )
                {
                    //we know better how to place the object

                    TapObject tapObj = (TapObject) mainObject.object;
                    msm = tapObj.getPosition( 1 - inward, 0, false );
                }

                for ( j = 0; j < count; ++j )
                {
                    anInfo = tmpCollection.elementAt( j ).objectInfo;
                    coords = anInfo.coords;
                    coords.transformCoordinates( m );

                    if ( msm != null )
                        coords.transformCoordinates( msm );
                    else
                        coords.setOrigin( coords.getOrigin().plus( tr ) );
                }

                newCollection.mergeCollection( tmpCollection, 0 );

                //System.out.println("phi : "+phi*180/Math.PI+" theta : "+theta*180/Math.PI);
                phi += gr;

                if ( phi > 2 * Math.PI )
                    phi -= 2 * Math.PI;

                if ( densityInput > 0 )
                    dum = cs1 - ( phicount * csdiff * inverseDFunction.calcValue( yRefValue ) ) / ( objectNumber * normalize );
                else
                {
                    dum = cs1 - ( phicount * csdiff * inverseDFunction.calcValue( yPortValue ) ) / ( objectNumber * normalize );
                    //System.out.println(yPortValue+" "+inverseDFunction.calcValue(yPortValue)+" "+
                    //   densityFunction.calcValue(yPortValue)+" "+theta*180/Math.PI);
                }

                if ( ( dum < -1 ) || ( dum > 1 ) )
                {
                    //System.out.println("dum stop");

                    go_on = false;

                    //System.out.println("phicount :"+phicount);
                }
                else
                {
                    theta = Math.asin( dum );

                    //System.out.println(thetaOrigin-theta/thetaFactor);
                    ++phicount;

                    if ( thetaOrigin - theta / thetaFactor > rangeTo )
                    {
                        go_on = false;

                        //System.out.println("phicount :"+phicount);
                    }
                }
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
    public double getValue( int outputPort, double[] var, long seed )
    {
        if ( outputPort == 1 )

            return yPortValue;
        else

            return 0.0;
    }


    /**
     *  Description of the Method
     *
     *@param  modifiers  Description of the Parameter
     */
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
    public boolean acceptsMainEntry()
    {
        return false;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean acceptsPreview()
    {
        return true;
    }


    /**
     *  Gets the moduleTypeInfo attribute of the GoldenBallModule object
     *
     *@return    The moduleTypeInfo value
     */
    public ModuleTypeInfo getModuleTypeInfo()
    {
        return typeInfo;
    }


    /**
     *  GoldenBallModule editor window
     *
     *@author     Francois Guillet
     *@created    19 avril 2004
     */
    private class GoldenBallModuleDialog
             extends JFrame
             implements ActionListener,
            DocumentListener,
            ChangeListener
    {
        private JButton okButton;
        private JButton applyButton;
        private JButton cancelButton;
        private JRadioButton curveDistGRB;
        private JRadioButton perpCurveDistGRB;
        private JRadioButton curveDistURB;
        private JRadioButton perpCurveDistURB;
        private JRadioButton sizeDistURB;
        private JRadioButton sizeDistGRB;
        private JRadioButton sphereRB;
        private JRadioButton hemisphereRB;
        private JCheckBox sizeChildrenCB;
        private boolean modified;
        private JTextField rangeFromTF;
        private JTextField rangeToTF;
        private JTextField sizeDistTF;
        private JTextField densityParameterTF;
        private JTextField estimateTF;
        private JTextField inwardTF;
        private JTextField randomTiltTF;
        private JTextField randomTiltDivTF;
        private JTextField curveAngleTF;
        private JTextField curveAngleDistTF;
        private JTextField curveAngleBackTF;
        private JTextField sectionJitterTF;
        private JTextField perpCurveAngleTF;
        private JTextField perpCurveAngleDistTF;
        private JTextField perpCurveAngleBackTF;
        private JTextField curveRateTF;
        private JTextField perpCurveRateTF;
        private JTextField recoverRateTF;
        private JTextField twistTurnsTF;
        private JTextField twistTurnsDistTF;
        private JRadioButton twistDistURB;
        private JRadioButton twistDistGRB;
        private ButtonGroup curveDistBG;
        private ButtonGroup perpCurveDistBG;
        private ButtonGroup twistDistBG;
        private ButtonGroup sphereBG;
        private ButtonGroup sizeDistBG;
        private JTabbedPane tabbedPane;
        private double backRangeFrom;
        private double backRangeTo;
        private double backSizeDist;
        private double backDensityParameter;
        private double backRSizeFactor;
        private double backYSizeFactor;
        private double backInward;
        private boolean backFullSphere;
        private boolean backSizeChildren;
        private int backEstimate;
        private short backSizeDistType;
        private boolean setupFlag;
        private NumberFormat format;
        private TapDistortParameters backParms;
        private TapFunction backCurveAngleFunction;
        private TapFunction backBackAngleFunction;
        private TapFunction backPerpAngleFunction;
        private TapFunction backCurveRateFunction;
        private TapFunction backDensityFunction;
        private BButton curveAngleButton;
        private BButton backAngleButton;
        private BButton perpAngleButton;
        private BButton curveRateButton;
        private BButton densityButton;
        private JSlider rSizeFactorSL;
        private JSlider ySizeFactorSL;
        private JTextField leafCurveAngleTF;
        private JTextField leafCurveAngleDistTF;
        private JTextField leafCurveAngleBackTF;
        private JTextField leafCurveRateTF;
        private JTextField leafDepartureAngleTF;
        private JSlider leafRRatioSL;
        private ButtonGroup leafCurveDistBG;
        private JRadioButton leafCurveDistGRB;
        private JRadioButton leafCurveDistURB;
        private BButton leafCurveAngleButton;
        private BButton leafDepartureAngleButton;
        private TapFunction backYCurveLeafFunction;
        private TapFunction backYLeafDepartureAngleFunction;
        private boolean backCurveAngleFunctionInput;
        private boolean backBackAngleFunctionInput;
        private boolean backPerpAngleFunctionInput;
        private boolean backDensityFunctionInput;
        private boolean backCurveRateFunctionInput;
        private boolean backYCurveLeafFunctionInput;
        private boolean backYLeafDepartureAngleFunctionInput;
        private JCheckBox curveAngleFunctionInputCB;
        private JCheckBox backAngleFunctionInputCB;
        private JCheckBox perpAngleFunctionInputCB;
        private JCheckBox densityFunctionInputCB;
        private JCheckBox curveRateFunctionInputCB;
        private JCheckBox yCurveLeafFunctionInputCB;
        private JCheckBox yLeafDepartureAngleFunctionInputCB;
        private TapFunction.FunctionDialog editDialog;


        /**
         *  Constructor for the GoldenBallModuleDialog object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public GoldenBallModuleDialog( JFrame parentFrame )
        {
            setupFlag = true;
            format = NumberFormat.getInstance();
            backRangeFrom = rangeFrom;
            backRangeTo = rangeTo;
            backSizeDist = sizeDist;
            backSizeDistType = sizeDistType;
            backDensityParameter = densityParameter;
            backFullSphere = fullSphere;
            backSizeChildren = sizeChildren;
            backRSizeFactor = rSizeFactor;
            backYSizeFactor = ySizeFactor;
            backEstimate = estimate;
            backInward = inward;
            backParms = smParms.duplicate();
            backCurveAngleFunction = curveAngleFunction.duplicate();
            backCurveRateFunction = curveRateFunction.duplicate();
            backBackAngleFunction = backAngleFunction.duplicate();
            backPerpAngleFunction = perpAngleFunction.duplicate();
            backDensityFunction = densityFunction.duplicate();
            backYCurveLeafFunction = yCurveLeafFunction.duplicate();
            backYLeafDepartureAngleFunction = yLeafDepartureAngleFunction.duplicate();
            backCurveAngleFunctionInput = curveAngleFunctionInput;
            backBackAngleFunctionInput = backAngleFunctionInput;
            backPerpAngleFunctionInput = perpAngleFunctionInput;
            backDensityFunctionInput = densityFunctionInput;
            backCurveRateFunctionInput = curveRateFunctionInput;
            backYCurveLeafFunctionInput = yCurveLeafFunctionInput;
            backYLeafDepartureAngleFunctionInput = yLeafDepartureAngleFunctionInput;

            Container contentPane = this.getContentPane();
            sphereBG = new ButtonGroup();
            curveDistBG = new ButtonGroup();
            perpCurveDistBG = new ButtonGroup();
            twistDistBG = new ButtonGroup();
            sizeDistBG = new ButtonGroup();
            leafCurveDistBG = new ButtonGroup();
            sphereRB = TapDesignerTranslate.jRadioButton( "sphere", this );
            hemisphereRB = TapDesignerTranslate.jRadioButton( "hemisphere", this );
            curveDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            curveDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            perpCurveDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            perpCurveDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            twistDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            twistDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            sizeDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            sizeDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            leafCurveDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            leafCurveDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            sphereBG.add( sphereRB );
            sphereBG.add( hemisphereRB );

            if ( fullSphere )
                sphereRB.setSelected( true );
            else
                hemisphereRB.setSelected( true );

            sizeDistBG.add( sizeDistURB );
            sizeDistBG.add( sizeDistGRB );
            setRadioButton( sizeDistType, sizeDistURB, sizeDistGRB );
            curveDistBG.add( curveDistURB );
            curveDistBG.add( curveDistGRB );
            setRadioButton( smParms.curveAngleDistType, curveDistURB, curveDistGRB );
            perpCurveDistBG.add( perpCurveDistURB );
            perpCurveDistBG.add( perpCurveDistGRB );
            setRadioButton( smParms.perpCurveAngleDistType, perpCurveDistURB, perpCurveDistGRB );
            twistDistBG.add( twistDistURB );
            twistDistBG.add( twistDistGRB );
            setRadioButton( smParms.twistDistType, twistDistURB, twistDistGRB );
            leafCurveDistBG.add( leafCurveDistURB );
            leafCurveDistBG.add( leafCurveDistGRB );
            setRadioButton( smParms.leafCurveAngleDistType, leafCurveDistURB, leafCurveDistGRB );
            tabbedPane = new JTabbedPane();

            JPanel first = new JPanel();
            contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS ) );
            first.setLayout( new BoxLayout( first, BoxLayout.Y_AXIS ) );

            JPanel pp = new JPanel();
            pp.setLayout( new BoxLayout( pp, BoxLayout.Y_AXIS ) );

            JPanel p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.LEFT, 10, 5 ) );
            p.add( TapDesignerTranslate.jlabel( "from" ) );
            p.add( rangeFromTF = new JTextField( format.format( rangeFrom ) ) );
            rangeFromTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "to" ) );
            p.add( rangeToTF = new JTextField( format.format( rangeTo ) ) );
            rangeToTF.setColumns( 4 );

            JPanel pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( sphereRB );
            pRB.add( hemisphereRB );
            p.add( pRB );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.LEFT, 10, 5 ) );
            p.add( TapDesignerTranslate.jlabel( "sizeDist" ) );
            p.add( sizeDistTF = new JTextField( format.format( sizeDist ) ) );
            sizeDistTF.setColumns( 4 );
            pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( sizeDistURB );
            pRB.add( sizeDistGRB );
            p.add( pRB );
            p.add( TapDesignerTranslate.jlabel( "grDensityParameter" ) );
            p.add( densityParameterTF = new JTextField( format.format( densityParameter ) ) );
            densityParameterTF.setColumns( 4 );

            JPanel yvPanel = new JPanel();
            yvPanel.setLayout( new BoxLayout( yvPanel, BoxLayout.Y_AXIS ) );
            densityButton = TapBTranslate.bButton( "densityFunction", this, "doDensityButton" );
            yvPanel.add( densityButton.getComponent() );
            yvPanel.add( densityFunctionInputCB = TapDesignerTranslate.jCheckBox( "useYInput", this ) );
            ( (JComponent) densityButton.getComponent() ).setAlignmentX( 0.5f );
            densityFunctionInputCB.setAlignmentX( 0.5f );
            p.add( yvPanel );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.LEFT ) );
            p.add( TapDesignerTranslate.jlabel( "toEstimate" ) );
            p.add( estimateTF = new JTextField( format.format( estimate ) ) );
            estimateTF.setColumns( 4 );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.LEFT ) );
            p.add( TapDesignerTranslate.jlabel( "enterInward" ) );
            inwardTF = new JTextField( String.valueOf( inward ) );
            inwardTF.setColumns( 4 );
            p.add( inwardTF );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.LEFT ) );
            p.add( sizeChildrenCB = TapDesignerTranslate.jCheckBox( "sizeChildren", this ) );
            sizeChildrenCB.setSelected( sizeChildren );
            pp.add( p );

            JPanel pg = new JPanel();
            pg.setLayout( new GridLayout( 2, 2 ) );
            pg.add( TapDesignerTranslate.jlabel( "sizeR" ) );
            pg.add( TapDesignerTranslate.jlabel( "sizeY" ) );
            pg.add( rSizeFactorSL = new JSlider( JSlider.HORIZONTAL, 0, 100, (int) ( rSizeFactor * 100 ) ) );
            rSizeFactorSL.setMajorTickSpacing( 100 );
            rSizeFactorSL.setMinorTickSpacing( 10 );
            rSizeFactorSL.setPaintTicks( true );

            Hashtable labelTable = new Hashtable();
            JLabel tmpLabel;
            Dimension dim = rSizeFactorSL.getPreferredSize();
            labelTable.put( new Integer( 0 ), tmpLabel = TapDesignerTranslate.jlabel( "none" ) );

            //dim.width += tmpLabel.getPreferredSize().width/2;
            labelTable.put( new Integer( 100 ), tmpLabel = TapDesignerTranslate.jlabel( "full" ) );

            //dim.width += tmpLabel.getPreferredSize().width/2;
            rSizeFactorSL.setLabelTable( labelTable );
            rSizeFactorSL.setPaintLabels( true );

            //dim.height = rSizeFactorSL.getPreferredSize().height;
            //rSizeFactorSL.setPreferredSize(dim);
            pg.add( ySizeFactorSL = new JSlider( JSlider.HORIZONTAL, 0, 100, (int) ( ySizeFactor * 100 ) ) );
            ySizeFactorSL.setMajorTickSpacing( 100 );
            ySizeFactorSL.setMinorTickSpacing( 10 );
            ySizeFactorSL.setPaintTicks( true );
            ySizeFactorSL.setLabelTable( labelTable );
            ySizeFactorSL.setPaintLabels( true );

            if ( !sizeChildrenCB.isSelected() )
            {
                rSizeFactorSL.setEnabled( false );
                ySizeFactorSL.setEnabled( false );
            }

            p.add( pg );
            pp.add( p );

            JPanel ppp = new JPanel();
            ppp.setLayout( new FlowLayout() );
            ppp.add( Box.createHorizontalGlue() );
            ppp.add( pp );
            ppp.add( Box.createHorizontalGlue() );

            TitledBorder border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapBTranslate.text( "generationParameters" ) );
            ppp.setBorder( border );
            first.add( ppp );
            first.add( Box.createVerticalGlue() );

            JPanel second = new JPanel();
            second.setLayout( new BoxLayout( second, BoxLayout.Y_AXIS ) );
            pp = new JPanel();
            pp.setLayout( new BoxLayout( pp, BoxLayout.Y_AXIS ) );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "curvatureAngle" ) );
            p.add( curveAngleTF = new JTextField( format.format( smParms.curveAngle ) ) );
            curveAngleTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "pm" ) );
            p.add( curveAngleDistTF = new JTextField( format.format( smParms.curveAngleDist ) ) );
            curveAngleDistTF.setColumns( 4 );
            pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( curveDistURB );
            pRB.add( curveDistGRB );
            p.add( pRB );
            curveAngleButton = TapBTranslate.bButton( "yvalue", this, "doCurveAngleButton" );
            yvPanel = new JPanel();
            yvPanel.setLayout( new BoxLayout( yvPanel, BoxLayout.Y_AXIS ) );
            yvPanel.add( curveAngleButton.getComponent() );
            yvPanel.add( curveAngleFunctionInputCB = TapDesignerTranslate.jCheckBox( "useYInput", this ) );
            ( (JComponent) curveAngleButton.getComponent() ).setAlignmentX( 0.5f );
            curveAngleFunctionInputCB.setAlignmentX( 0.5f );
            p.add( yvPanel );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "backAngle" ) );
            p.add( curveAngleBackTF = new JTextField( format.format( smParms.curveAngleBack ) ) );
            curveAngleBackTF.setColumns( 4 );
            backAngleButton = TapBTranslate.bButton( "yvalue", this, "doBackAngleButton" );
            yvPanel = new JPanel();
            yvPanel.setLayout( new BoxLayout( yvPanel, BoxLayout.Y_AXIS ) );
            yvPanel.add( backAngleButton.getComponent() );
            yvPanel.add( backAngleFunctionInputCB = TapDesignerTranslate.jCheckBox( "useYInput", this ) );
            ( (JComponent) backAngleButton.getComponent() ).setAlignmentX( 0.5f );
            backAngleFunctionInputCB.setAlignmentX( 0.5f );
            p.add( yvPanel );
            p.add( TapDesignerTranslate.jlabel( "curvatureRate" ) );
            p.add( curveRateTF = new JTextField( format.format( smParms.curveRate ) ) );
            curveRateTF.setColumns( 4 );
            curveRateButton = TapBTranslate.bButton( "yvalue", this, "doCurveRateButton" );
            yvPanel = new JPanel();
            yvPanel.setLayout( new BoxLayout( yvPanel, BoxLayout.Y_AXIS ) );
            yvPanel.add( curveRateButton.getComponent() );
            yvPanel.add( curveRateFunctionInputCB = TapDesignerTranslate.jCheckBox( "useYInput", this ) );
            ( (JComponent) curveRateButton.getComponent() ).setAlignmentX( 0.5f );
            curveRateFunctionInputCB.setAlignmentX( 0.5f );
            p.add( yvPanel );
            pp.add( p );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapDesignerTranslate.text( "yCurveParameters" ) );
            pp.setBorder( border );
            second.add( pp );
            pp = new JPanel();
            pp.setLayout( new BoxLayout( pp, BoxLayout.Y_AXIS ) );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "curvatureAngle" ) );
            p.add( perpCurveAngleTF = new JTextField( format.format( smParms.perpCurveAngle ) ) );
            perpCurveAngleTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "pm" ) );
            p.add( perpCurveAngleDistTF = new JTextField( format.format( smParms.perpCurveAngleDist ) ) );
            perpCurveAngleDistTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "backAngle" ) );
            p.add( perpCurveAngleBackTF = new JTextField( format.format( smParms.perpCurveAngleBack ) ) );
            perpCurveAngleBackTF.setColumns( 4 );
            pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( perpCurveDistURB );
            pRB.add( perpCurveDistGRB );
            p.add( pRB );
            perpAngleButton = TapBTranslate.bButton( "yvalue", this, "doPerpAngleButton" );
            yvPanel = new JPanel();
            yvPanel.setLayout( new BoxLayout( yvPanel, BoxLayout.Y_AXIS ) );
            yvPanel.add( perpAngleButton.getComponent() );
            yvPanel.add( perpAngleFunctionInputCB = TapDesignerTranslate.jCheckBox( "useYInput", this ) );
            ( (JComponent) perpAngleButton.getComponent() ).setAlignmentX( 0.5f );
            perpAngleFunctionInputCB.setAlignmentX( 0.5f );
            p.add( yvPanel );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "curvatureRate" ) );
            p.add( perpCurveRateTF = new JTextField( format.format( smParms.perpCurveRate ) ) );
            perpCurveRateTF.setColumns( 4 );
            pp.add( p );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapDesignerTranslate.text( "rCurveParameters" ) );
            pp.setBorder( border );
            second.add( pp );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "randomTilt" ) );
            p.add( randomTiltTF = new JTextField( format.format( smParms.randomTilt ) ) );
            randomTiltTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "randomTiltDiv" ) );
            p.add( randomTiltDivTF = new JTextField( format.format( smParms.randomTiltDiv ) ) );
            randomTiltDivTF.setColumns( 3 );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapDesignerTranslate.text( "randomTilt" ) );
            p.setBorder( border );
            second.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "sectionJitter" ) );
            p.add( sectionJitterTF = new JTextField( format.format( smParms.sectionJitter ) ) );
            sectionJitterTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "twistTurns" ) );
            p.add( twistTurnsTF = new JTextField( format.format( smParms.twistTurns ) ) );
            twistTurnsTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "pm" ) );
            p.add( twistTurnsDistTF = new JTextField( format.format( smParms.twistTurnsDist ) ) );
            twistTurnsDistTF.setColumns( 4 );
            pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( twistDistURB );
            pRB.add( twistDistGRB );
            p.add( pRB );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapDesignerTranslate.text( "sectionParameters" ) );
            p.setBorder( border );
            second.add( p );
            pp = new JPanel();
            pp.setLayout( new BoxLayout( pp, BoxLayout.Y_AXIS ) );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "leafCurvatureAngle" ) );
            p.add( leafCurveAngleTF = new JTextField( format.format( smParms.leafCurveAngle ) ) );
            leafCurveAngleTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "pm" ) );
            p.add( leafCurveAngleDistTF = new JTextField( format.format( smParms.leafCurveAngleDist ) ) );
            leafCurveAngleDistTF.setColumns( 4 );
            pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( leafCurveDistURB );
            pRB.add( leafCurveDistGRB );
            p.add( pRB );
            leafCurveAngleButton = TapBTranslate.bButton( "yvalue", this, "doLeafCurveAngleButton" );
            yvPanel = new JPanel();
            yvPanel.setLayout( new BoxLayout( yvPanel, BoxLayout.Y_AXIS ) );
            yvPanel.add( leafCurveAngleButton.getComponent() );
            yvPanel.add( yCurveLeafFunctionInputCB = TapDesignerTranslate.jCheckBox( "useYInput", this ) );
            ( (JComponent) leafCurveAngleButton.getComponent() ).setAlignmentX( 0.5f );
            yCurveLeafFunctionInputCB.setAlignmentX( 0.5f );
            p.add( yvPanel );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "backAngle" ) );
            p.add( leafCurveAngleBackTF = new JTextField( format.format( smParms.leafCurveAngleBack ) ) );
            leafCurveAngleBackTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "curvatureRate" ) );
            p.add( leafCurveRateTF = new JTextField( format.format( smParms.leafCurveRate ) ) );
            leafCurveRateTF.setColumns( 4 );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "leafDepartureAngle" ) );
            p.add( leafDepartureAngleTF = new JTextField( format.format( smParms.leafDepartureAngle ) ) );
            leafDepartureAngleTF.setColumns( 4 );
            leafDepartureAngleButton = TapBTranslate.bButton( "yvalue", this, "doLeafDepartureAngleButton" );
            yvPanel = new JPanel();
            yvPanel.setLayout( new BoxLayout( yvPanel, BoxLayout.Y_AXIS ) );
            yvPanel.add( leafDepartureAngleButton.getComponent() );
            yvPanel.add( yLeafDepartureAngleFunctionInputCB = TapDesignerTranslate.jCheckBox( "useYInput", this ) );
            ( (JComponent) leafDepartureAngleButton.getComponent() ).setAlignmentX( 0.5f );
            yLeafDepartureAngleFunctionInputCB.setAlignmentX( 0.5f );
            p.add( yvPanel );
            p.add( TapDesignerTranslate.jlabel( "leafYCurvatureRatio" ) );
            p.add( leafRRatioSL = new JSlider( JSlider.HORIZONTAL, 0, 100, (int) ( smParms.leafRRatio * 100 ) ) );
            leafRRatioSL.setMajorTickSpacing( 100 );
            leafRRatioSL.setMinorTickSpacing( 10 );
            leafRRatioSL.setPaintTicks( true );
            labelTable = new Hashtable();
            labelTable.put( new Integer( 0 ), tmpLabel = TapDesignerTranslate.jlabel( "constant" ) );
            labelTable.put( new Integer( 100 ), tmpLabel = TapDesignerTranslate.jlabel( "normalized" ) );
            leafRRatioSL.setLabelTable( labelTable );
            leafRRatioSL.setPaintLabels( true );
            pp.add( p );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapDesignerTranslate.text( "leafCurveParameters" ) );
            pp.setBorder( border );
            second.add( pp );
            curveAngleFunctionInputCB.setSelected( curveAngleFunctionInput );
            backAngleFunctionInputCB.setSelected( backAngleFunctionInput );
            perpAngleFunctionInputCB.setSelected( perpAngleFunctionInput );
            densityFunctionInputCB.setSelected( densityFunctionInput );
            curveRateFunctionInputCB.setSelected( curveRateFunctionInput );
            yCurveLeafFunctionInputCB.setSelected( yCurveLeafFunctionInput );
            yLeafDepartureAngleFunctionInputCB.setSelected( yLeafDepartureAngleFunctionInput );
            tabbedPane.addTab( TapDesignerTranslate.text( "commonParameters" ), null, first, null );
            tabbedPane.addTab( TapDesignerTranslate.text( "tapdParameters" ), null, second, null );
            tabbedPane.setSelectedIndex( 0 );
            contentPane.add( tabbedPane );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( okButton = TapDesignerTranslate.jButton( "ok", this ) );
            p.add( applyButton = TapDesignerTranslate.jButton( "apply", this ) );
            p.add( cancelButton = TapDesignerTranslate.jButton( "cancel", this ) );
            contentPane.add( p );
            this.setTitle( TapDesignerTranslate.text( "goldenBallModuleTitle", module.getName() ) );
            addWindowListener(
                new java.awt.event.WindowAdapter()
                {
                    public void windowClosing( java.awt.event.WindowEvent evt )
                    {
                        exitForm( evt );
                    }
                } );
            rangeFromTF.getDocument().addDocumentListener( this );
            rangeToTF.getDocument().addDocumentListener( this );
            densityParameterTF.getDocument().addDocumentListener( this );
            sizeDistTF.getDocument().addDocumentListener( this );
            estimateTF.getDocument().addDocumentListener( this );
            inwardTF.getDocument().addDocumentListener( this );
            curveAngleTF.getDocument().addDocumentListener( this );
            curveAngleDistTF.getDocument().addDocumentListener( this );
            curveAngleBackTF.getDocument().addDocumentListener( this );
            sectionJitterTF.getDocument().addDocumentListener( this );
            perpCurveAngleTF.getDocument().addDocumentListener( this );
            perpCurveAngleDistTF.getDocument().addDocumentListener( this );
            perpCurveAngleBackTF.getDocument().addDocumentListener( this );
            perpCurveRateTF.getDocument().addDocumentListener( this );
            randomTiltTF.getDocument().addDocumentListener( this );
            randomTiltDivTF.getDocument().addDocumentListener( this );
            twistTurnsTF.getDocument().addDocumentListener( this );
            twistTurnsDistTF.getDocument().addDocumentListener( this );
            leafCurveAngleTF.getDocument().addDocumentListener( this );
            leafCurveAngleBackTF.getDocument().addDocumentListener( this );
            leafCurveAngleDistTF.getDocument().addDocumentListener( this );
            leafCurveRateTF.getDocument().addDocumentListener( this );
            leafDepartureAngleTF.getDocument().addDocumentListener( this );

            rSizeFactorSL.addChangeListener( this );
            ySizeFactorSL.addChangeListener( this );
            leafRRatioSL.addChangeListener( this );

            pack();
            setLocationRelativeTo( parentFrame );
            setResizable( false );
            setVisible( true );
            setupFlag = false;
            modified = false;
        }


        /**
         *  Selects relevant radio button depending on random distribution
         *
         *@param  type  The random distribution type
         *@param  rURB  The uniform radio button
         *@param  rGRB  The gaussian radio button
         */
        private void setRadioButton( short type, JRadioButton rURB, JRadioButton rGRB )
        {
            switch ( type )
            {
                case TapRandomGenerator.UNIFORM:
                    rURB.setSelected( true );

                    break;
                case TapRandomGenerator.GAUSSIAN:
                    rGRB.setSelected( true );

                    break;
            }
        }


        /**
         *  Gets the radioType attribute of the GoldenBallModuleDialog object
         *
         *@param  rURB  Description of the Parameter
         *@param  rGRB  Description of the Parameter
         *@return       The radioType value
         */
        private short getRadioType( JRadioButton rURB, JRadioButton rGRB )
        {
            if ( rURB.isSelected() )

                return TapRandomGenerator.UNIFORM;
            else

                return TapRandomGenerator.GAUSSIAN;
        }


        /**
         *  Description of the Method
         */
        private void doLeafDepartureAngleButton()
        {
            editDialog = yLeafDepartureAngleFunction.edit( this, TapBTranslate.text( "leafDepartureAngleYValue", module.getName() ),
                new Runnable()
                {
                    public void run()
                    {
                        doRunnableUpdate();
                    }
                } );
        }


        /**
         *  Description of the Method
         */
        private void doLeafCurveAngleButton()
        {
            editDialog = yCurveLeafFunction.edit( this, TapBTranslate.text( "leafCurveYValue", module.getName() ),
                new Runnable()
                {
                    public void run()
                    {
                        doRunnableUpdate();
                    }
                } );
        }


        /**
         *  Description of the Method
         */
        private void doDensityButton()
        {
            editDialog = densityFunction.edit( this, TapBTranslate.text( "densityFunctionValue", module.getName() ),
                new Runnable()
                {
                    public void run()
                    {
                        doRunnableUpdate();
                    }
                } );
        }


        /**
         *  Description of the Method
         */
        private void doPerpAngleButton()
        {
            editDialog = perpAngleFunction.edit( this, TapBTranslate.text( "perpAngleYValue", module.getName() ),
                new Runnable()
                {
                    public void run()
                    {
                        doRunnableUpdate();
                    }
                } );
        }


        /**
         *  Description of the Method
         */
        private void doBackAngleButton()
        {
            editDialog = backAngleFunction.edit( this, TapBTranslate.text( "backAngleYValue", module.getName() ),
                new Runnable()
                {
                    public void run()
                    {
                        doRunnableUpdate();
                    }
                } );
        }


        /**
         *  Description of the Method
         */
        private void doCurveRateButton()
        {
            editDialog = curveRateFunction.edit( this, TapBTranslate.text( "curveRateYValue", module.getName() ),
                new Runnable()
                {
                    public void run()
                    {
                        doRunnableUpdate();
                    }
                } );
        }


        /**
         *  Description of the Method
         */
        private void doCurveAngleButton()
        {
            editDialog = curveAngleFunction.edit( this, TapBTranslate.text( "curveAngleYValue", module.getName() ),
                new Runnable()
                {
                    public void run()
                    {
                        doRunnableUpdate();
                    }
                } );
        }


        /**
         *  Description of the Method
         */
        public void doRunnableUpdate()
        {
            if ( editDialog.isClosing() )
            {
                if ( editDialog.isModified() )
                    modified = true;

                editDialog = null;
            }
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
         *
         *@param  evt  Description of the Parameter
         */
        public void actionPerformed( java.awt.event.ActionEvent evt )
        {
            String command = evt.getActionCommand();

            if ( command.equals( cancelButton.getActionCommand() ) )
                doCancel();
            else if ( command.equals( applyButton.getActionCommand() ) )
            {
                getValues();
                doApply();
            }
            else if ( command.equals( okButton.getActionCommand() ) )
            {
                if ( modified )
                {
                    TapFunction tmpCurveAngleFunction = curveAngleFunction;
                    TapFunction tmpCurveRateFunction = curveRateFunction;
                    TapFunction tmpBackAngleFunction = backAngleFunction;
                    TapFunction tmpPerpAngleFunction = perpAngleFunction;
                    TapFunction tmpDensityFunction = densityFunction;
                    TapFunction tmpYCurveLeafFunction = yCurveLeafFunction;
                    TapFunction tmpYLeafDepartureAngleFunction = yLeafDepartureAngleFunction;
                    getBackValues();
                    procedure.addUndoRecord();
                    curveAngleFunction = tmpCurveAngleFunction;
                    curveRateFunction = tmpCurveRateFunction;
                    backAngleFunction = tmpBackAngleFunction;
                    perpAngleFunction = tmpPerpAngleFunction;
                    densityFunction = tmpDensityFunction;
                    yCurveLeafFunction = tmpYCurveLeafFunction;
                    yLeafDepartureAngleFunction = tmpYLeafDepartureAngleFunction;
                }

                getValues();
                doApply();
                editDialogClosed();
            }
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void stateChanged( ChangeEvent e )
        {
            if ( setupFlag )

                return;

            modified = true;

            if ( sizeChildrenCB.isSelected() )
            {
                rSizeFactorSL.setEnabled( true );
                ySizeFactorSL.setEnabled( true );
            }
            else
            {
                rSizeFactorSL.setEnabled( false );
                ySizeFactorSL.setEnabled( false );
            }
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void doLiveCheck( DocumentEvent e )
        {
            modified = true;

            Document sourceDoc = e.getDocument();
            JTextField source = null;

            if ( sourceDoc == rangeFromTF.getDocument() )
                source = rangeFromTF;
            else if ( sourceDoc == rangeToTF.getDocument() )
                source = rangeToTF;
            else if ( sourceDoc == sizeDistTF.getDocument() )
                source = sizeDistTF;
            else if ( sourceDoc == densityParameterTF.getDocument() )
                source = densityParameterTF;
            else if ( sourceDoc == curveAngleTF.getDocument() )
                source = curveAngleTF;
            else if ( sourceDoc == curveAngleDistTF.getDocument() )
                source = curveAngleDistTF;
            else if ( sourceDoc == curveAngleBackTF.getDocument() )
                source = curveAngleBackTF;
            else if ( sourceDoc == sectionJitterTF.getDocument() )
                source = sectionJitterTF;
            else if ( sourceDoc == perpCurveAngleTF.getDocument() )
                source = perpCurveAngleTF;
            else if ( sourceDoc == perpCurveAngleDistTF.getDocument() )
                source = perpCurveAngleDistTF;
            else if ( sourceDoc == perpCurveAngleBackTF.getDocument() )
                source = perpCurveAngleBackTF;
            else if ( sourceDoc == curveRateTF.getDocument() )
                source = curveRateTF;
            else if ( sourceDoc == perpCurveRateTF.getDocument() )
                source = perpCurveRateTF;
            else if ( sourceDoc == randomTiltTF.getDocument() )
                source = randomTiltTF;
            else if ( sourceDoc == randomTiltDivTF.getDocument() )
                source = randomTiltDivTF;
            else if ( sourceDoc == twistTurnsTF.getDocument() )
                source = randomTiltTF;
            else if ( sourceDoc == twistTurnsDistTF.getDocument() )
                source = randomTiltDivTF;
            else if ( sourceDoc == inwardTF.getDocument() )
                source = inwardTF;
            else if ( sourceDoc == leafCurveAngleTF.getDocument() )
                source = leafCurveAngleTF;
            else if ( sourceDoc == leafCurveAngleBackTF.getDocument() )
                source = leafCurveAngleBackTF;
            else if ( sourceDoc == leafCurveAngleDistTF.getDocument() )
                source = leafCurveAngleDistTF;
            else if ( sourceDoc == leafCurveRateTF.getDocument() )
                source = leafCurveRateTF;
            else if ( sourceDoc == leafDepartureAngleTF.getDocument() )
                source = leafDepartureAngleTF;

            if ( source == estimateTF )
            {
                try
                {
                    int dum = ( Integer.valueOf( source.getText().trim() ) ).intValue();
                    source.setForeground( Color.black );
                }
                catch ( NumberFormatException ex )
                {
                    source.setForeground( Color.red );
                }
            }
            else if ( source != null )
            {
                try
                {
                    double dum = Double.parseDouble( source.getText().trim().replace( ',', '.' ) );
                    source.setForeground( Color.black );
                }
                catch ( NumberFormatException ex )
                {
                    source.setForeground( Color.red );
                }
            }
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void changedUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void insertUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void removeUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Gets the values attribute of the GoldenBallModuleDialog object
         */
        private void getValues()
        {
            try
            {
                rangeFrom = Double.parseDouble( rangeFromTF.getText().trim().replace( ',', '.' ) );
                rangeTo = Double.parseDouble( rangeToTF.getText().trim().replace( ',', '.' ) );
                sizeDist = Double.parseDouble( sizeDistTF.getText().trim().replace( ',', '.' ) );
                sizeDistType = getRadioType( sizeDistURB, sizeDistGRB );
                densityParameter = Double.parseDouble( densityParameterTF.getText().trim().replace( ',', '.' ) );
                fullSphere = sphereRB.isSelected();
                sizeChildren = sizeChildrenCB.isSelected();
                rSizeFactor = (double) rSizeFactorSL.getValue() / 100.0;
                ySizeFactor = (double) ySizeFactorSL.getValue() / 100.0;
                estimate = ( Integer.valueOf( estimateTF.getText().trim() ) ).intValue();
                inward = Double.parseDouble( inwardTF.getText().trim().replace( ',', '.' ) );

                if ( estimate < 1 )
                    estimate = 1;

                smParms.curveAngle = Double.parseDouble( curveAngleTF.getText().trim().replace( ',', '.' ) );
                smParms.curveAngleDist = Double.parseDouble( curveAngleDistTF.getText().trim().replace( ',', '.' ) );
                smParms.curveAngleBack = Double.parseDouble( curveAngleBackTF.getText().trim().replace( ',', '.' ) );
                smParms.perpCurveAngle = Double.parseDouble( perpCurveAngleTF.getText().trim().replace( ',', '.' ) );
                smParms.perpCurveAngleDist = Double.parseDouble( perpCurveAngleDistTF.getText().trim().replace( ',', '.' ) );
                smParms.perpCurveAngleBack = Double.parseDouble( perpCurveAngleBackTF.getText().trim().replace( ',', '.' ) );
                smParms.curveRate = Double.parseDouble( curveRateTF.getText().trim().replace( ',', '.' ) );
                smParms.perpCurveRate = Double.parseDouble( perpCurveRateTF.getText().trim().replace( ',', '.' ) );
                smParms.sectionJitter = Double.parseDouble( sectionJitterTF.getText().trim().replace( ',', '.' ) );
                smParms.curveAngleDistType = getRadioType( curveDistURB, curveDistGRB );
                smParms.perpCurveAngleDistType = getRadioType( perpCurveDistURB, perpCurveDistGRB );
                smParms.randomTilt = Double.parseDouble( randomTiltTF.getText().trim().replace( ',', '.' ) );
                smParms.randomTiltDiv = ( Integer.valueOf( randomTiltDivTF.getText().trim() ) )
                        .intValue();
                smParms.twistTurns = Double.parseDouble( twistTurnsTF.getText().trim().replace( ',', '.' ) );
                smParms.twistTurnsDist = Double.parseDouble( twistTurnsDistTF.getText().trim().replace( ',', '.' ) );
                smParms.twistDistType = getRadioType( twistDistURB, twistDistGRB );
                smParms.leafCurveAngle = Double.parseDouble( leafCurveAngleTF.getText().trim().replace( ',', '.' ) );
                smParms.leafCurveAngleDist = Double.parseDouble( leafCurveAngleDistTF.getText().trim().replace( ',', '.' ) );
                smParms.leafCurveAngleBack = Double.parseDouble( leafCurveAngleBackTF.getText().trim().replace( ',', '.' ) );
                smParms.leafCurveRate = Double.parseDouble( leafCurveRateTF.getText().trim().replace( ',', '.' ) );
                smParms.leafDepartureAngle = Double.parseDouble( leafDepartureAngleTF.getText().trim().replace( ',', '.' ) );
                smParms.leafRRatio = (double) leafRRatioSL.getValue() / 100.0;
                curveAngleFunctionInput = curveAngleFunctionInputCB.isSelected();
                backAngleFunctionInput = backAngleFunctionInputCB.isSelected();
                perpAngleFunctionInput = perpAngleFunctionInputCB.isSelected();
                densityFunctionInput = densityFunctionInputCB.isSelected();
                curveRateFunctionInput = curveRateFunctionInputCB.isSelected();
                yCurveLeafFunctionInput = yCurveLeafFunctionInputCB.isSelected();
                yLeafDepartureAngleFunctionInput = yLeafDepartureAngleFunctionInputCB.isSelected();
            }
            catch ( NumberFormatException e )
            {
                System.out.println( "Exception" );
                getBackValues();
            }
        }


        /**
         *  Gets the backValues attribute of the GoldenBallModuleDialog object
         */
        private void getBackValues()
        {
            rangeFrom = backRangeFrom;
            rangeTo = backRangeTo;
            sizeDist = backSizeDist;
            sizeDistType = backSizeDistType;
            densityParameter = backDensityParameter;
            fullSphere = backFullSphere;
            sizeChildren = backSizeChildren;
            estimate = backEstimate;
            inward = backInward;
            rSizeFactor = backRSizeFactor;
            ySizeFactor = backYSizeFactor;
            smParms = backParms.duplicate();
            curveAngleFunction = backCurveAngleFunction;
            curveRateFunction = backCurveRateFunction;
            backAngleFunction = backBackAngleFunction;
            perpAngleFunction = backPerpAngleFunction;
            densityFunction = backDensityFunction;
            yCurveLeafFunction = backYCurveLeafFunction;
            yLeafDepartureAngleFunction = backYLeafDepartureAngleFunction;
            curveAngleFunctionInput = backCurveAngleFunctionInput;
            backAngleFunctionInput = backBackAngleFunctionInput;
            perpAngleFunctionInput = backPerpAngleFunctionInput;
            densityFunctionInput = backDensityFunctionInput;
            curveRateFunctionInput = backCurveRateFunctionInput;
            yCurveLeafFunctionInput = backYCurveLeafFunctionInput;
            yLeafDepartureAngleFunctionInput = backYLeafDepartureAngleFunctionInput;
        }


        /**
         *  Description of the Method
         */
        private void doCancel()
        {
            if ( modified )
            {
                int r = JOptionPane.showConfirmDialog( this, TapDesignerTranslate.text( "parametersModified" ), TapDesignerTranslate.text( "warning" ), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION );

                if ( r == JOptionPane.YES_OPTION )
                    modified = false;
            }

            if ( !modified )
            {
                getBackValues();
                editDialogClosed();
            }
        }
    }
}

