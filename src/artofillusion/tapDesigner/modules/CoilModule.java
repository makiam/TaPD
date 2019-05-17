/*
 *  This represents a coil module, which decorates any object sent in second entry port
 *  with decorator object sent in first entry port.
 */
/*
 *  Copyright (C) 2003 by François Guillet
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
 *  Description of the Class
 *
 *@author     pims
 *@created    19 avril 2004
 */
public class CoilModule extends TapModule
{
    private CoilModule module;
    private static TapModule.ModuleTypeInfo typeInfo;

    int Rstep;
    int Rstart;
    double Ystep;
    double Yfrom;
    double Yto;
    double vertAngle;
    double horAngle;
    double vertAngleDist;
    double horAngleDist;
    double probability;
    double Ydist;
    boolean sizeChildren;
    double rSizeFactor;
    double ySizeFactor;
    double sizeDist;
    short vertAngleType;
    short horAngleType;
    short YdistType;
    short sizeDistType;
    boolean isRings;
    boolean useGoldenRatio;
    boolean flipChildren;
    double densityParameter;
    double rShift;
    double yPortValue;
    int dampingFactor;
    TapDistortParameters smParms;
    TapFunction vertAngleFunction;
    TapFunction curveAngleFunction;
    TapFunction backAngleFunction;
    TapFunction perpAngleFunction;
    TapFunction branchingAngleFunction;
    TapFunction probFunction;
    TapFunction curveRateFunction;
    TapFunction yStepFunction;
    TapFunction rShiftFunction;
    TapFunction yCurveLeafFunction;
    TapFunction yLeafDepartureAngleFunction;
    boolean vertAngleFunctionInput;
    boolean curveAngleFunctionInput;
    boolean backAngleFunctionInput;
    boolean perpAngleFunctionInput;
    boolean probFunctionInput;
    boolean curveRateFunctionInput;
    boolean yStepFunctionInput;
    boolean rShiftFunctionInput;
    boolean yCurveLeafFunctionInput;
    boolean yLeafDepartureAngleFunctionInput;
    final static double gr = 2 * Math.PI / ( ( 1 + Math.sqrt( 5 ) ) / 2 );


    /**
     *  Constructor for the CoilModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public CoilModule( TapProcedure procedure, Point position )
    {
        super( procedure, TapBTranslate.text( "coil" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "coilName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/coil_tree.png" ) ) );
        setNumInput( 5 );
        setNumOutput( 2 );
        useGoldenRatio = false;
        Rstep = 6;
        Rstart = 0;
        Ystep = 0.1;
        Yfrom = 0.0;
        Yto = 1.0;
        vertAngle = 45.0;
        horAngle = 0.0;
        vertAngleDist = 3;
        horAngleDist = 3;
        vertAngleType = TapRandomGenerator.UNIFORM;
        horAngleType = TapRandomGenerator.UNIFORM;
        probability = 0.2;
        Ydist = 0;
        YdistType = TapRandomGenerator.UNIFORM;
        sizeDist = 0.15;
        sizeDistType = TapRandomGenerator.UNIFORM;
        sizeChildren = true;
        rSizeFactor = 1.0;
        ySizeFactor = 1.0;
        isRings = false;
        densityParameter = 0.0;
        dampingFactor = 0;
        smParms = new TapDistortParameters();
        probFunction = new TapFunction( 1.0, 1.0 );
        vertAngleFunction = new TapFunction( 1.0, 1.0 );
        curveAngleFunction = new TapFunction( 1.0, 1.0 );
        curveRateFunction = new TapFunction( 1.0, 1.0 );
        backAngleFunction = new TapFunction( 1.0, 1.0 );
        perpAngleFunction = new TapFunction( 1.0, 1.0 );
        branchingAngleFunction = new TapFunction( 1.0, 0.0 );
        yStepFunction = new TapFunction( 1.0, 1.0 );
        rShift = 0;
        rShiftFunction = new TapFunction( 1.0, 1.0 );
        yCurveLeafFunction = new TapFunction( 1.0, 1.0 );
        yLeafDepartureAngleFunction = new TapFunction( 1.0, 1.0 );
        flipChildren = false;
        vertAngleFunctionInput = false;
        curveAngleFunctionInput = false;
        backAngleFunctionInput = false;
        perpAngleFunctionInput = false;
        probFunctionInput = false;
        curveRateFunctionInput = false;
        yStepFunctionInput = false;
        rShiftFunctionInput = false;
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
        inputTooltips[0] = TapBTranslate.text( "objectDecorate" );
        inputTooltips[1] = TapBTranslate.text( "objectDecorated" );
        inputTooltips[2] = TapBTranslate.text( "yValueInput" );
        inputTooltips[3] = TapBTranslate.text( "rSizeValue" );
        inputTooltips[4] = TapBTranslate.text( "ySizeValue" );
        outputTooltips = new String[2];
        outputTooltips[0] = TapBTranslate.text( "objectOutput" );
        outputTooltips[1] = TapBTranslate.text( "yValueOutput" );
        setBackgroundColor( Color.orange.darker() );
        module = this;
    }


    /**
     *  Constructor for the CoilModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public CoilModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        Rstep = in.readInt();
        Rstart = in.readInt();
        Ystep = in.readDouble();
        Yfrom = in.readDouble();
        Yto = in.readDouble();
        vertAngle = in.readDouble();
        horAngle = in.readDouble();
        vertAngleDist = in.readDouble();
        horAngleDist = in.readDouble();
        vertAngleType = in.readShort();
        horAngleType = in.readShort();
        probability = in.readDouble();
        Ydist = in.readDouble();
        YdistType = in.readShort();
        sizeDist = in.readDouble();
        sizeDistType = in.readShort();
        sizeChildren = in.readBoolean();
        isRings = in.readBoolean();
        dampingFactor = in.readInt();
        smParms = new TapDistortParameters( in );
        probFunction = new TapFunction( in );
        vertAngleFunction = new TapFunction( in );
        curveAngleFunction = new TapFunction( in );
        curveRateFunction = new TapFunction( in );
        backAngleFunction = new TapFunction( in );
        perpAngleFunction = new TapFunction( in );
        branchingAngleFunction = new TapFunction( in );
        yStepFunction = new TapFunction( in );
        densityParameter = in.readDouble();
        rShift = in.readDouble();
        rShiftFunction = new TapFunction( in );
        rSizeFactor = in.readDouble();
        ySizeFactor = in.readDouble();
        useGoldenRatio = in.readBoolean();
        yCurveLeafFunction = new TapFunction( in );
        yLeafDepartureAngleFunction = new TapFunction( in );
        flipChildren = in.readBoolean();
        vertAngleFunctionInput = in.readBoolean();
        curveAngleFunctionInput = in.readBoolean();
        backAngleFunctionInput = in.readBoolean();
        perpAngleFunctionInput = in.readBoolean();
        probFunctionInput = in.readBoolean();
        curveRateFunctionInput = in.readBoolean();
        yStepFunctionInput = in.readBoolean();
        rShiftFunctionInput = in.readBoolean();
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
    @Override
    public void writeToFile( DataOutputStream out, Scene theScene )
        throws IOException
    {
        super.writeToFile( out, theScene );
        out.writeShort( 0 );
        out.writeInt( Rstep );
        out.writeInt( Rstart );
        out.writeDouble( Ystep );
        out.writeDouble( Yfrom );
        out.writeDouble( Yto );
        out.writeDouble( vertAngle );
        out.writeDouble( horAngle );
        out.writeDouble( vertAngleDist );
        out.writeDouble( horAngleDist );
        out.writeShort( vertAngleType );
        out.writeShort( horAngleType );
        out.writeDouble( probability );
        out.writeDouble( Ydist );
        out.writeShort( YdistType );
        out.writeDouble( sizeDist );
        out.writeShort( sizeDistType );
        out.writeBoolean( sizeChildren );
        out.writeBoolean( isRings );
        out.writeInt( dampingFactor );
        smParms.writeToFile( out );
        probFunction.writeToFile( out );
        vertAngleFunction.writeToFile( out );
        curveAngleFunction.writeToFile( out );
        curveRateFunction.writeToFile( out );
        backAngleFunction.writeToFile( out );
        perpAngleFunction.writeToFile( out );
        branchingAngleFunction.writeToFile( out );
        yStepFunction.writeToFile( out );
        out.writeDouble( densityParameter );
        out.writeDouble( rShift );
        rShiftFunction.writeToFile( out );
        out.writeDouble( rSizeFactor );
        out.writeDouble( ySizeFactor );
        out.writeBoolean( useGoldenRatio );
        yCurveLeafFunction.writeToFile( out );
        yLeafDepartureAngleFunction.writeToFile( out );
        out.writeBoolean( flipChildren );
        out.writeBoolean( vertAngleFunctionInput );
        out.writeBoolean( curveAngleFunctionInput );
        out.writeBoolean( backAngleFunctionInput );
        out.writeBoolean( perpAngleFunctionInput );
        out.writeBoolean( probFunctionInput );
        out.writeBoolean( curveRateFunctionInput );
        out.writeBoolean( yStepFunctionInput );
        out.writeBoolean( rShiftFunctionInput );
        out.writeBoolean( yCurveLeafFunctionInput );
        out.writeBoolean( yLeafDepartureAngleFunctionInput );
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public TapModule duplicate()
    {
        CoilModule module = new CoilModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
        module.useGoldenRatio = this.useGoldenRatio;
        module.Rstart = this.Rstart;
        module.Ystep = this.Ystep;
        module.Rstep = this.Rstep;
        module.Yfrom = this.Yfrom;
        module.Yto = this.Yto;
        module.vertAngle = this.vertAngle;
        module.horAngle = this.horAngle;
        module.vertAngleDist = this.vertAngleDist;
        module.horAngleDist = this.horAngleDist;
        module.vertAngleType = this.vertAngleType;
        module.horAngleType = this.horAngleType;
        module.probability = this.probability;
        module.Ydist = this.Ydist;
        module.YdistType = this.YdistType;
        module.sizeDist = this.sizeDist;
        module.sizeDistType = this.sizeDistType;
        module.sizeChildren = this.sizeChildren;
        module.rSizeFactor = this.rSizeFactor;
        module.ySizeFactor = this.ySizeFactor;
        module.isRings = this.isRings;
        module.dampingFactor = this.dampingFactor;
        module.smParms = this.smParms.duplicate();
        module.probFunction = probFunction.duplicate();
        module.vertAngleFunction = vertAngleFunction.duplicate();
        module.curveAngleFunction = curveAngleFunction.duplicate();
        module.curveRateFunction = curveRateFunction.duplicate();
        module.backAngleFunction = backAngleFunction.duplicate();
        module.perpAngleFunction = perpAngleFunction.duplicate();
        module.branchingAngleFunction = branchingAngleFunction.duplicate();
        module.yStepFunction = yStepFunction.duplicate();
        module.densityParameter = this.densityParameter;
        module.rShift = this.rShift;
        module.rShiftFunction = this.rShiftFunction.duplicate();
        module.yCurveLeafFunction = yCurveLeafFunction.duplicate();
        module.yLeafDepartureAngleFunction = yLeafDepartureAngleFunction.duplicate();
        module.flipChildren = flipChildren;
        module.vertAngleFunctionInput = vertAngleFunctionInput;
        module.curveAngleFunctionInput = curveAngleFunctionInput;
        module.backAngleFunctionInput = backAngleFunctionInput;
        module.perpAngleFunctionInput = perpAngleFunctionInput;
        module.probFunctionInput = probFunctionInput;
        module.curveRateFunctionInput = curveRateFunctionInput;
        module.yStepFunctionInput = yStepFunctionInput;
        module.rShiftFunctionInput = rShiftFunctionInput;
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
            editDialog.toFront();
        else
        {
            editDialog = new CoilModuleDialog( (JFrame) parentFrame.getComponent() );
            isEditDialogOn = true;
        }
    }


    /**
     *  Gets the object attribute of the CoilModule object
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
        else if ( outputPort == -2 )
        {
            //preview up to

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
        else
            //A coil module is not asked;
            return null;
    }


    /**
     *  Gets the object attribute of the CoilModule object
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
        Mat4 msm;
        Mat4 flipMat;
        Vec3 size;
        Vec3 tmpVec;
        Vec3 tr;
        ObjectInfo anInfo;
        ObjectInfo mainObject;
        int i;
        int j;
        int k;
        int level;
        int count;
        double yref;
        double RotR;
        double nvertAngle;
        double nhorAngle;
        CoordinateSystem coords = null;
        TapDesignerObjectCollection col = null;
        level = collection.elementAt( 0 ).getDecorationLevel();

        TapRandomGenerator gen = new TapRandomGenerator( seed );
        BackModuleLink.BackLink bl;
        double[] Yval = new double[1];
        double sizeR;
        double sizeY;
        double yRefValue;
        boolean duplicate;
        double dum;
        double curProb;
        double ysizestep;
        double objectYSize;
        int curDamp;
        BoundingBox bounds;
        TapDistortParameters tmpParms;
        String objName;

        if ( inputPort == 1 )
        {
            TapDesignerObjectCollection newCollection = new TapDesignerObjectCollection( procedure );
            BackModuleLink backLinks = procedure.getBackLink();
            mainObject = collection.elementAt( 0 ).objectInfo;
            size = mainObject.object.getBounds().getSize();
            Ysize = size.y;
            objectYSize = collection.elementAt( 0 ).sizeY * ( 1 - densityParameter ) + densityParameter;
            yref = Yfrom * Ysize;

            if ( ( Rstart != 0 ) && ( !useGoldenRatio ) )
                RotR = 2 * Math.PI / Rstart;
            else
                RotR = 0;

            curProb = probability * probFunction.calcValue( Yfrom );
            curDamp = 0;
            ysizestep = Ystep / objectYSize;
            tmpParms = smParms.duplicate();
            flipMat = Mat4.yrotation( Math.PI );

            while ( yref <= Ysize * Yto )
            {
                yPortValue = yref / Ysize;
                Yval[0] = yPortValue;
                bl = backLinks.findModule( this, 2 );

                if ( bl != null )
                    yRefValue = bl.fromModule.getValue( bl.outputPort, Yval, gen.getSeed() );
                else
                    yRefValue = yref / Ysize;

                if ( !useGoldenRatio )
                {
                    if ( curDamp > 0 )
                    {
                        --curDamp;

                        if ( probFunctionInput )
                            curProb = probability * probFunction.calcValue( yRefValue ) * ( dampingFactor - curDamp ) / dampingFactor;
                        else
                            curProb = probability * probFunction.calcValue( yPortValue ) * ( dampingFactor - curDamp ) / dampingFactor;
                    }
                    else
                    {
                        if ( probFunctionInput )
                            curProb = probability * probFunction.calcValue( yRefValue );
                        else
                            curProb = probability * probFunction.calcValue( yPortValue );
                    }

                    dum = gen.uniformDeviate();
                }
                else
                    dum = 0;

                if ( dum < curProb )
                {
                    bl = backLinks.findModule( this, 0 );

                    if ( bl == null )
                        return null;

                    col = bl.fromModule.getObject( bl.outputPort, gen.getSeed() );
                    objName = bl.fromModule.getName();

                    if ( col != null )
                    {
                        if ( !useGoldenRatio )
                            curDamp = dampingFactor;

                        if ( bl.fromModule instanceof ObjectModule )
                            duplicate = ( (ObjectModule) bl.fromModule ).isDuplicate();
                        else
                            duplicate = false;

                        m = Mat4.identity();
                        nvertAngle = gen.getDistribution( vertAngle * Math.PI / 180, vertAngleDist * Math.PI / 180, vertAngleType );

                        if ( vertAngleFunctionInput )
                            nvertAngle *= vertAngleFunction.calcValue( yRefValue );
                        else
                            nvertAngle *= vertAngleFunction.calcValue( yPortValue );

                        nhorAngle = RotR + gen.getDistribution( horAngle * Math.PI / 180, horAngleDist * Math.PI / 180, horAngleType );

                        while ( nvertAngle >= 2 * Math.PI )
                            nvertAngle -= 2 * Math.PI;

                        while ( nhorAngle >= 2 * Math.PI )
                            nhorAngle -= 2 * Math.PI;

                        m = m.times( Mat4.yrotation( nhorAngle ) );
                        m = m.times( Mat4.zrotation( nvertAngle ) );
                        anInfo = col.elementAt( 0 ).objectInfo;
                        coords = anInfo.coords;
                        sizeR = 1.0;
                        sizeY = 1.0;
                        if ( anInfo.object != null )
                        {
                            Object3D obj = (Object3D) anInfo.object;
                            TextureMapping map = obj.getTextureMapping();
                            TextureParameter[] tparms = map.getParameters();
                            for ( j = 0; j < tparms.length; ++j )
                            {
                                if ( tparms[j].name.equals( "YTAPD" ) )
                                {
                                    ParameterValue val = obj.getParameterValue( tparms[j] );
                                    if ( val instanceof ConstantParameterValue )
                                        ( (ConstantParameterValue) val ).setValue( yRefValue );
                                }
                                else if ( tparms[j].name.equals( "RTAPD" ) )
                                {
                                    ParameterValue val = obj.getParameterValue( tparms[j] );
                                    if ( val instanceof ConstantParameterValue )
                                        ( (ConstantParameterValue) val ).setValue( nhorAngle / ( 2 * Math.PI ) );
                                }
                            }
                        }
                        if ( !duplicate )
                        {
                            bl = backLinks.findModule( this, 3 );

                            if ( bl != null )
                                sizeR = bl.fromModule.getValue( bl.outputPort, Yval, gen.getSeed() );

                            bl = backLinks.findModule( this, 4 );

                            if ( bl != null )
                                sizeY = bl.fromModule.getValue( bl.outputPort, Yval, gen.getSeed() );

                            if ( sizeDist > 0 )
                            {
                                dum = gen.getDistribution( 0, sizeDist, sizeDistType );

                                if ( dum < 0.0001 )
                                    dum = 0.0001;

                                sizeR *= ( 1 - dum );
                                sizeY *= ( 1 - dum );
                            }

                            if ( sizeChildren )
                            {
                                sizeR *= collection.elementAt( 0 ).sizeR * rSizeFactor + 1 - rSizeFactor;
                                sizeY *= collection.elementAt( 0 ).sizeY * ySizeFactor + 1 - ySizeFactor;
                            }
                        }

                        if ( ( sizeR > 0 ) && ( sizeY > 0 ) )
                        {
                            size = anInfo.object.getBounds().getSize();

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

                            if ( anInfo.object instanceof SplineMesh )
                            {
                                anInfo.object.setSize( size.x * sizeR, size.y * sizeY, size.z * sizeR );
                                TapUtils.setObjectAtMinY( anInfo );
                                TapUtils.distortSplineMesh( (SplineMesh) anInfo.object, tmpParms );
                            }
                            else if ( !( anInfo.object instanceof TapObject ) )
                            {
                                anInfo.object.setSize( size.x * sizeR, size.y * sizeY, size.z * sizeR );
                                TapUtils.setObjectAtMinY( anInfo );
                            }
                            else
                                ( (TapObject) anInfo.object ).resizeAndDistort( size, sizeR, sizeY, tmpParms );

                            //else coords.setOrigin(((TapSplineMesh)anInfo.object).getYCurveOrigin());
                            TapDesignerObjectCollection tmpCollection = new TapDesignerObjectCollection( procedure );
                            tmpCollection.addObject( anInfo, level + 1, sizeR, sizeY, objName );

                            if ( ( inputPortLink[0] != null ) && ( !stopHere ) )
                            {
                                //modules linked to output will decorate object

                                for ( j = 0; j < inputPortLink[0].length; ++j )
                                {
                                    TapModule mod = (TapModule) modules.elementAt( linkToIndex[0][j] );
                                    TapDesignerObjectCollection modCol = mod.getObject( tmpCollection, inputPortLink[0][j], gen.getSeed() );

                                    if ( modCol != null )
                                        tmpCollection.mergeCollection( modCol, 0 );
                                }
                            }

                            if ( ( inputPortLink[0] != null ) && ( stopHere ) )
                                for ( j = 0; j < inputPortLink[0].length; ++j )
                                    //preserve random sequence
                                    gen.getSeed();

                            count = tmpCollection.size();
                            dum = yref + Ysize * gen.getDistribution( 0, Ydist, YdistType );
                            tr = new Vec3( 0, dum, 0 );
                            msm = null;

                            if ( mainObject.object instanceof TapObject )
                            {
                                //we know better how to place the object

                                TapObject tapObj = (TapObject) mainObject.object;

                                if ( smParms.counterAction > 0 )
                                {
                                    tmpParms.counterAction = smParms.counterAction * branchingAngleFunction.calcValue( dum / Ysize );
                                    msm = tapObj.setCounterAction( dum / Ysize, nhorAngle, tmpParms );
                                }
                                else
                                    msm = tapObj.getPosition( dum / Ysize, nhorAngle, true );
                            }

                            for ( j = 0; j < count; ++j )
                            {
                                anInfo = tmpCollection.elementAt( j ).objectInfo;
                                coords = anInfo.coords;

                                if ( flipChildren && ( ( nhorAngle > Math.PI && nhorAngle <= 2 * Math.PI ) || ( nhorAngle <= 0 && nhorAngle > -Math.PI ) ) )
                                    coords.transformCoordinates( flipMat );

                                coords.transformCoordinates( m );

                                if ( msm != null )
                                    coords.transformCoordinates( msm );
                                else
                                    coords.setOrigin( coords.getOrigin().plus( tr ) );

                                if ( rShiftFunctionInput )
                                    dum = rShift * rShiftFunction.calcValue( yRefValue );
                                else
                                    dum = rShift * rShiftFunction.calcValue( yPortValue );

                                dum *= collection.elementAt( 0 ).sizeR;
                                tmpVec = new Vec3( -dum, 0, 0 );

                                Mat4 mtr = Mat4.yrotation( nhorAngle );
                                mtr.transform( tmpVec );
                                coords.setOrigin( coords.getOrigin().plus( tmpVec ) );
                            }

                            newCollection.mergeCollection( tmpCollection, 0 );
                        }
                    }
                }

                if ( !useGoldenRatio )
                {
                    if ( isRings )
                    {
                        RotR += 2 * Math.PI / Rstep;

                        if ( RotR >= 2 * Math.PI )
                        {
                            if ( Rstart != 0 )
                                RotR = 2 * Math.PI / Rstart;
                            else
                                RotR = 0;

                            if ( yStepFunctionInput )
                                yref += ysizestep * Ysize * yStepFunction.calcValue( yRefValue );
                            else
                                yref += ysizestep * Ysize * yStepFunction.calcValue( yPortValue );
                        }
                    }
                    else
                    {
                        //spiral

                        if ( yStepFunctionInput )
                            yref += ysizestep * Ysize * yStepFunction.calcValue( yRefValue ) / Rstep;
                        else
                            yref += ysizestep * Ysize * yStepFunction.calcValue( yPortValue ) / Rstep;

                        RotR += 2 * Math.PI / Rstep;

                        if ( RotR >= 2 * Math.PI )
                            RotR -= 2 * Math.PI;
                    }
                }
                else
                {
                    //use golden ratio

                    if ( yStepFunctionInput )
                        yref += ysizestep * Ysize * yStepFunction.calcValue( yRefValue );
                    else
                        yref += ysizestep * Ysize * yStepFunction.calcValue( yPortValue );

                    RotR += gr;

                    if ( RotR >= 2 * Math.PI )
                        RotR -= 2 * Math.PI;
                }
            }

            if ( mainObject.object instanceof TapObject )
                ( (TapObject) mainObject.object ).regenerateMesh();

            return newCollection;
        }
        else
            return null;
    }


    /**
     *  Gets the value attribute of the CoilModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  var         Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The value value
     */
    @Override
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
            if ( collection.size() > 0 )
            {
                ObjectInfo newObjectInfo = new ObjectInfo( collection, new CoordinateSystem(), getName() );
                newObjectInfo.object.setTexture( procedure.getScene().getDefaultTexture(), procedure.getScene().getDefaultTexture().getDefaultMapping(newObjectInfo.object) );
                setupPreviewFrame( newObjectInfo );
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
     *  Gets the moduleTypeInfo attribute of the CoilModule object
     *
     *@return    The moduleTypeInfo value
     */
    @Override
    public ModuleTypeInfo getModuleTypeInfo()
    {
        return typeInfo;
    }


    /**
     *  Coil editor window
     *
     *@author     Francois Guillet
     *@created    19 avril 2004
     */
    private class CoilModuleDialog
             extends JFrame
             implements ActionListener,
            DocumentListener,
            ChangeListener
    {
        private JButton okButton;
        private JButton applyButton;
        private JButton cancelButton;
        private JRadioButton YdistURB;
        private JRadioButton vertAngleURB;
        private JRadioButton horAngleURB;
        private JRadioButton sizeDistURB;
        private JRadioButton YdistGRB;
        private JRadioButton vertAngleGRB;
        private JRadioButton horAngleGRB;
        private JRadioButton sizeDistGRB;
        private JRadioButton curveDistGRB;
        private JRadioButton perpCurveDistGRB;
        private JRadioButton curveDistURB;
        private JRadioButton perpCurveDistURB;
        private JRadioButton spiralRB;
        private JRadioButton ringsRB;
        private JCheckBox sizeChildrenCB;
        private JCheckBox dampingCB;
        private JCheckBox recoverCB;
        private JCheckBox useGoldenCB;
        private JCheckBox flipChildrenCB;
        private JSlider densityParameterSL;
        private boolean modified;
        private JTextField RstartTF;
        private JTextField RstepTF;
        private JTextField YdistTF;
        private JTextField YstepTF;
        private JTextField YfromTF;
        private JTextField YtoTF;
        private JTextField vertAngleTF;
        private JTextField horAngleTF;
        private JTextField probabilityTF;
        private JTextField vertAngleDistTF;
        private JTextField horAngleDistTF;
        private JTextField sizeDistTF;
        private JTextField dampingTF;
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
        private JTextField counterActionTF;
        private JTextField recoverRateTF;
        private JTextField twistTurnsTF;
        private JTextField twistTurnsDistTF;
        private JTextField rShiftTF;
        private JRadioButton twistDistURB;
        private JRadioButton twistDistGRB;
        private ButtonGroup YdistBG;
        private ButtonGroup vertAngleDistBG;
        private ButtonGroup horAngleDistBG;
        private ButtonGroup sizeDistBG;
        private ButtonGroup spiralBG;
        private ButtonGroup curveDistBG;
        private ButtonGroup perpCurveDistBG;
        private ButtonGroup twistDistBG;
        private JTabbedPane tabbedPane;
        private int backRstep;
        private int backRstart;
        private double backYstep;
        private double backYfrom;
        private double backYto;
        private double backVertAngle;
        private double backHorAngle;
        private double backVertAngleDist;
        private double backHorAngleDist;
        private double backSizeDist;
        private double backProbability;
        private double backRShift;
        private double backYdist;
        private boolean backSizeChildren;
        private boolean backIsRings;
        private boolean backUseGoldenRatio;
        private boolean backFlipChildren;
        private double backDensityParameter;
        private boolean setupFlag;
        private short backVertAngleType;
        private short backHorAngleType;
        private short backYdistType;
        private short backSizeDistType;
        private int backDampingFactor;
        private NumberFormat format;
        private TapDistortParameters backParms;
        private TapFunction backVertAngleFunction;
        private TapFunction backCurveAngleFunction;
        private TapFunction backBackAngleFunction;
        private TapFunction backPerpAngleFunction;
        private TapFunction backProbFunction;
        private TapFunction backBranchingAngleFunction;
        private TapFunction backCurveRateFunction;
        private TapFunction backYStepFunction;
        private TapFunction backRShiftFunction;
        private BButton vertAngleButton;
        private BButton curveAngleButton;
        private BButton backAngleButton;
        private BButton perpAngleButton;
        private BButton probButton;
        private BButton branchingAngleButton;
        private BButton curveRateButton;
        private BButton yStepButton;
        private BButton rShiftButton;
        private JSlider rSizeFactorSL;
        private JSlider ySizeFactorSL;
        private double backRSizeFactor;
        private double backYSizeFactor;
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
        private boolean backVertAngleFunctionInput;
        private boolean backCurveAngleFunctionInput;
        private boolean backBackAngleFunctionInput;
        private boolean backPerpAngleFunctionInput;
        private boolean backProbFunctionInput;
        private boolean backCurveRateFunctionInput;
        private boolean backYStepFunctionInput;
        private boolean backRShiftFunctionInput;
        private boolean backYCurveLeafFunctionInput;
        private boolean backYLeafDepartureAngleFunctionInput;
        private JCheckBox vertAngleFunctionInputCB;
        private JCheckBox curveAngleFunctionInputCB;
        private JCheckBox backAngleFunctionInputCB;
        private JCheckBox perpAngleFunctionInputCB;
        private JCheckBox probFunctionInputCB;
        private JCheckBox curveRateFunctionInputCB;
        private JCheckBox yStepFunctionInputCB;
        private JCheckBox rShiftFunctionInputCB;
        private JCheckBox yCurveLeafFunctionInputCB;
        private JCheckBox yLeafDepartureAngleFunctionInputCB;
        private TapFunction.FunctionDialog editDialog;


        /**
         *  Constructor for the CoilModuleDialog object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public CoilModuleDialog( JFrame parentFrame )
        {
            setupFlag = true;
            format = NumberFormat.getInstance();
            backUseGoldenRatio = useGoldenRatio;
            backFlipChildren = flipChildren;
            backRstep = Rstep;
            backRstart = Rstart;
            backYstep = Ystep;
            backYfrom = Yfrom;
            backYto = Yto;
            backVertAngle = vertAngle;
            backHorAngle = horAngle;
            backVertAngleDist = vertAngleDist;
            backHorAngleDist = horAngleDist;
            backProbability = probability;
            backYdist = Ydist;
            backYdistType = YdistType;
            backVertAngleType = vertAngleType;
            backHorAngleType = horAngleType;
            backSizeDist = sizeDist;
            backSizeDistType = sizeDistType;
            backSizeChildren = sizeChildren;
            backRSizeFactor = rSizeFactor;
            backYSizeFactor = ySizeFactor;
            backIsRings = isRings;
            backDampingFactor = dampingFactor;
            backParms = smParms.duplicate();
            backVertAngleFunction = vertAngleFunction.duplicate();
            backCurveAngleFunction = curveAngleFunction.duplicate();
            backCurveRateFunction = curveRateFunction.duplicate();
            backBackAngleFunction = backAngleFunction.duplicate();
            backPerpAngleFunction = perpAngleFunction.duplicate();
            backProbFunction = probFunction.duplicate();
            backBranchingAngleFunction = branchingAngleFunction.duplicate();
            backYStepFunction = yStepFunction.duplicate();
            backDensityParameter = densityParameter;
            backRShift = rShift;
            backRShiftFunction = rShiftFunction.duplicate();
            backYCurveLeafFunction = yCurveLeafFunction.duplicate();
            backYLeafDepartureAngleFunction = yLeafDepartureAngleFunction.duplicate();
            backVertAngleFunctionInput = vertAngleFunctionInput;
            backCurveAngleFunctionInput = curveAngleFunctionInput;
            backBackAngleFunctionInput = backAngleFunctionInput;
            backPerpAngleFunctionInput = perpAngleFunctionInput;
            backProbFunctionInput = probFunctionInput;
            backCurveRateFunctionInput = curveRateFunctionInput;
            backYStepFunctionInput = yStepFunctionInput;
            backRShiftFunctionInput = rShiftFunctionInput;
            backYCurveLeafFunctionInput = yCurveLeafFunctionInput;
            backYLeafDepartureAngleFunctionInput = yLeafDepartureAngleFunctionInput;

            Container contentPane = this.getContentPane();
            YdistBG = new ButtonGroup();
            vertAngleDistBG = new ButtonGroup();
            horAngleDistBG = new ButtonGroup();
            sizeDistBG = new ButtonGroup();
            spiralBG = new ButtonGroup();
            curveDistBG = new ButtonGroup();
            perpCurveDistBG = new ButtonGroup();
            twistDistBG = new ButtonGroup();
            leafCurveDistBG = new ButtonGroup();

            spiralRB = TapDesignerTranslate.jRadioButton( "spiral", this );
            ringsRB = TapDesignerTranslate.jRadioButton( "rings", this );
            YdistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            YdistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            vertAngleURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            vertAngleGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            horAngleURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            horAngleGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            sizeDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            sizeDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            curveDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            curveDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            perpCurveDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            perpCurveDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            twistDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            twistDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            leafCurveDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            leafCurveDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );

            spiralBG.add( spiralRB );
            spiralBG.add( ringsRB );

            if ( isRings )
                ringsRB.setSelected( true );
            else
                spiralRB.setSelected( true );

            YdistBG.add( YdistURB );
            YdistBG.add( YdistGRB );
            setRadioButton( YdistType, YdistURB, YdistGRB );
            vertAngleDistBG.add( vertAngleURB );
            vertAngleDistBG.add( vertAngleGRB );
            setRadioButton( vertAngleType, vertAngleURB, vertAngleGRB );
            horAngleDistBG.add( horAngleURB );
            horAngleDistBG.add( horAngleGRB );
            setRadioButton( horAngleType, horAngleURB, horAngleGRB );
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

            JPanel p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );

            JPanel pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( spiralRB );
            pRB.add( ringsRB );
            p.add( pRB );
            p.add( TapDesignerTranslate.jlabel( "probability" ) );
            p.add( probabilityTF = new JTextField( format.format( probability ) ) );
            probabilityTF.setColumns( 4 );

            JPanel yvPanel = new JPanel();
            yvPanel.setLayout( new BoxLayout( yvPanel, BoxLayout.Y_AXIS ) );
            probButton = TapBTranslate.bButton( "yvalue", this, "doProbButton" );
            yvPanel.add( probButton.getComponent() );
            yvPanel.add( probFunctionInputCB = TapDesignerTranslate.jCheckBox( "useYInput", this ) );
            ( (JComponent) probButton.getComponent() ).setAlignmentX( 0.5f );
            probFunctionInputCB.setAlignmentX( 0.5f );
            p.add( yvPanel );
            dampingTF = new JTextField( format.format( dampingFactor ) );
            dampingTF.setColumns( 4 );
            p.add( dampingCB = TapDesignerTranslate.jCheckBox( "dampFactor", this ) );
            p.add( dampingTF );

            if ( dampingFactor != 0 )
                dampingCB.setSelected( true );
            else
                dampingTF.setEnabled( false );

            TitledBorder border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapBTranslate.text( "generationParameters" ) );
            p.setBorder( border );
            first.add( p );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "Rstart" ) );
            p.add( RstartTF = new JTextField( format.format( Rstart ) ) );
            RstartTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "Rstep" ) );
            p.add( RstepTF = new JTextField( format.format( Rstep ) ) );
            RstepTF.setColumns( 4 );
            p.add( useGoldenCB = TapDesignerTranslate.jCheckBox( "useGoldenRatio", this ) );
            useGoldenCB.setSelected( useGoldenRatio );
            p.add( flipChildrenCB = TapDesignerTranslate.jCheckBox( "flipChildren", this ) );
            flipChildrenCB.setSelected( flipChildren );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapBTranslate.text( "RParameters" ) );
            p.setBorder( border );
            first.add( p );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "Yrange" ) );
            p.add( YfromTF = new JTextField( format.format( Yfrom ) ) );
            YfromTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "Yto" ) );
            p.add( YtoTF = new JTextField( format.format( Yto ) ) );
            YtoTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "Ystep" ) );
            p.add( YstepTF = new JTextField( format.format( Ystep ) ) );
            YstepTF.setColumns( 4 );
            yStepButton = TapBTranslate.bButton( "yvalue", this, "doYStepButton" );
            yvPanel = new JPanel();
            yvPanel.setLayout( new BoxLayout( yvPanel, BoxLayout.Y_AXIS ) );
            yvPanel.add( yStepButton.getComponent() );
            yvPanel.add( yStepFunctionInputCB = TapDesignerTranslate.jCheckBox( "useYInput", this ) );
            ( (JComponent) yStepButton.getComponent() ).setAlignmentX( 0.5f );
            yStepFunctionInputCB.setAlignmentX( 0.5f );
            p.add( yvPanel );
            p.add( TapDesignerTranslate.jlabel( "Ypm" ) );
            p.add( YdistTF = new JTextField( format.format( Ydist ) ) );
            YdistTF.setColumns( 4 );
            pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( YdistURB );
            pRB.add( YdistGRB );
            p.add( pRB );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapBTranslate.text( "YParameters" ) );
            p.setBorder( border );
            first.add( p );

            JPanel pp = new JPanel();
            pp.setLayout( new BoxLayout( pp, BoxLayout.Y_AXIS ) );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "vertAngle" ) );
            p.add( vertAngleTF = new JTextField( format.format( vertAngle ) ) );
            vertAngleTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "pm" ) );
            p.add( vertAngleDistTF = new JTextField( format.format( vertAngleDist ) ) );
            vertAngleDistTF.setColumns( 4 );
            pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( vertAngleURB );
            pRB.add( vertAngleGRB );
            p.add( pRB );
            vertAngleButton = TapBTranslate.bButton( "yvalue", this, "doVertAngleButton" );
            yvPanel = new JPanel();
            yvPanel.setLayout( new BoxLayout( yvPanel, BoxLayout.Y_AXIS ) );
            yvPanel.add( vertAngleButton.getComponent() );
            yvPanel.add( vertAngleFunctionInputCB = TapDesignerTranslate.jCheckBox( "useYInput", this ) );
            ( (JComponent) vertAngleButton.getComponent() ).setAlignmentX( 0.5f );
            vertAngleFunctionInputCB.setAlignmentX( 0.5f );
            p.add( yvPanel );
            pp.add( p );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "horAngle" ) );
            p.add( horAngleTF = new JTextField( format.format( horAngle ) ) );
            horAngleTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "pm" ) );
            p.add( horAngleDistTF = new JTextField( format.format( horAngleDist ) ) );
            horAngleDistTF.setColumns( 4 );
            pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( horAngleURB );
            pRB.add( horAngleGRB );
            p.add( pRB );

            //pp.add(p);
            //p = new JPanel();
            //p.setLayout(new FlowLayout());
            p.add( TapDesignerTranslate.jlabel( "Rshift" ) );
            p.add( rShiftTF = new JTextField( format.format( rShift ) ) );
            rShiftTF.setColumns( 4 );
            rShiftButton = TapBTranslate.bButton( "yvalue", this, "doRShiftButton" );
            yvPanel = new JPanel();
            yvPanel.setLayout( new BoxLayout( yvPanel, BoxLayout.Y_AXIS ) );
            yvPanel.add( rShiftButton.getComponent() );
            yvPanel.add( rShiftFunctionInputCB = TapDesignerTranslate.jCheckBox( "useYInput", this ) );
            ( (JComponent) rShiftButton.getComponent() ).setAlignmentX( 0.5f );
            rShiftFunctionInputCB.setAlignmentX( 0.5f );
            p.add( yvPanel );
            pp.add( p );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapBTranslate.text( "angularParameters" ) );
            pp.setBorder( border );
            first.add( pp );

            pp = new JPanel();
            pp.setLayout( new BoxLayout( pp, BoxLayout.Y_AXIS ) );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "sizeDist" ) );
            p.add( sizeDistTF = new JTextField( format.format( sizeDist ) ) );
            sizeDistTF.setColumns( 4 );
            pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( sizeDistURB );
            pRB.add( sizeDistGRB );
            p.add( pRB );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( sizeChildrenCB = TapDesignerTranslate.jCheckBox( "sizeChildren", this ) );
            sizeChildrenCB.setSelected( sizeChildren );

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

            //Dimension dim = rSizeFactorSL.getPreferredSize();
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
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "decorationDensityVariation" ) );
            p.add( densityParameterSL = new JSlider( JSlider.HORIZONTAL, 0, 100, (int) ( densityParameter * 100 ) ) );
            densityParameterSL.setMajorTickSpacing( 100 );
            densityParameterSL.setMinorTickSpacing( 10 );
            densityParameterSL.setPaintTicks( true );
            labelTable = new Hashtable();

            Dimension dim = densityParameterSL.getPreferredSize();
            labelTable.put( new Integer( 0 ), tmpLabel = TapDesignerTranslate.jlabel( "constantDensity" ) );
            dim.width += tmpLabel.getPreferredSize().width / 2;
            labelTable.put( new Integer( 100 ), tmpLabel = TapDesignerTranslate.jlabel( "constantNumber" ) );
            dim.width += tmpLabel.getPreferredSize().width / 2;
            densityParameterSL.setLabelTable( labelTable );
            densityParameterSL.setPaintLabels( true );
            dim.height = densityParameterSL.getPreferredSize().height;
            densityParameterSL.setPreferredSize( dim );
            pp.add( p );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapBTranslate.text( "sizeParameters" ) );
            pp.setBorder( border );
            first.add( pp );

            if ( useGoldenRatio )
            {
                RstartTF.setEnabled( false );
                RstepTF.setEnabled( false );
                probabilityTF.setEnabled( false );
                probButton.setEnabled( false );
                dampingTF.setEnabled( false );
                dampingCB.setEnabled( false );
                spiralRB.setEnabled( false );
                ringsRB.setEnabled( false );
            }

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
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapBTranslate.text( "yCurveParameters" ) );
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
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapBTranslate.text( "rCurveParameters" ) );
            pp.setBorder( border );
            second.add( pp );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "counterAction" ) );
            p.add( counterActionTF = new JTextField( format.format( smParms.counterAction ) ) );
            counterActionTF.setColumns( 4 );
            branchingAngleButton = TapBTranslate.bButton( "yvalue", this, "doBranchingAngleButton" );
            p.add( branchingAngleButton.getComponent() );
            p.add( recoverCB = TapDesignerTranslate.jCheckBox( "recover", this ) );
            recoverCB.setSelected( smParms.recover );
            p.add( TapDesignerTranslate.jlabel( "recoverRate" ) );
            p.add( recoverRateTF = new JTextField( format.format( smParms.recoverRate ) ) );
            recoverRateTF.setColumns( 4 );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapBTranslate.text( "branchReaction" ) );
            p.setBorder( border );
            second.add( p );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "randomTilt" ) );
            p.add( randomTiltTF = new JTextField( format.format( smParms.randomTilt ) ) );
            randomTiltTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "randomTiltDiv" ) );
            p.add( randomTiltDivTF = new JTextField( format.format( smParms.randomTiltDiv ) ) );
            randomTiltDivTF.setColumns( 3 );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapBTranslate.text( "randomTilt" ) );
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
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapBTranslate.text( "sectionParameters" ) );
            p.setBorder( border );

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
            labelTable.put(0, tmpLabel = TapDesignerTranslate.jlabel( "constant" ) );
            labelTable.put(100, tmpLabel = TapDesignerTranslate.jlabel( "normalized" ) );
            leafRRatioSL.setLabelTable( labelTable );
            leafRRatioSL.setPaintLabels( true );
            pp.add( p );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapBTranslate.text( "leafCurveParameters" ) );
            pp.setBorder( border );
            second.add( pp );

            vertAngleFunctionInputCB.setSelected( vertAngleFunctionInput );
            curveAngleFunctionInputCB.setSelected( curveAngleFunctionInput );
            backAngleFunctionInputCB.setSelected( backAngleFunctionInput );
            perpAngleFunctionInputCB.setSelected( perpAngleFunctionInput );
            probFunctionInputCB.setSelected( probFunctionInput );
            curveRateFunctionInputCB.setSelected( curveRateFunctionInput );
            yStepFunctionInputCB.setSelected( yStepFunctionInput );
            rShiftFunctionInputCB.setSelected( rShiftFunctionInput );
            yCurveLeafFunctionInputCB.setSelected( yCurveLeafFunctionInput );
            yLeafDepartureAngleFunctionInputCB.setSelected( yLeafDepartureAngleFunctionInput );

            tabbedPane.addTab( TapBTranslate.text( "commonParameters" ), null, first, null );
            tabbedPane.addTab( TapBTranslate.text( "tapdParameters" ), null, second, null );
            tabbedPane.setSelectedIndex( 0 );
            contentPane.add( tabbedPane );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( okButton = TapDesignerTranslate.jButton( "ok", this ) );
            p.add( applyButton = TapDesignerTranslate.jButton( "apply", this ) );
            p.add( cancelButton = TapDesignerTranslate.jButton( "cancel", this ) );
            contentPane.add( p );
            this.setTitle( TapBTranslate.text( "coilModuleTitle", module.getName() ) );

            addWindowListener(
                new java.awt.event.WindowAdapter()
                {
                    @Override
                    public void windowClosing( java.awt.event.WindowEvent evt )
                    {
                        exitForm( evt );
                    }
                } );
            RstartTF.getDocument().addDocumentListener( this );
            RstepTF.getDocument().addDocumentListener( this );
            YdistTF.getDocument().addDocumentListener( this );
            YfromTF.getDocument().addDocumentListener( this );
            YstepTF.getDocument().addDocumentListener( this );
            YtoTF.getDocument().addDocumentListener( this );
            vertAngleTF.getDocument().addDocumentListener( this );
            horAngleTF.getDocument().addDocumentListener( this );
            vertAngleDistTF.getDocument().addDocumentListener( this );
            horAngleDistTF.getDocument().addDocumentListener( this );
            probabilityTF.getDocument().addDocumentListener( this );
            sizeDistTF.getDocument().addDocumentListener( this );
            dampingTF.getDocument().addDocumentListener( this );
            rShiftTF.getDocument().addDocumentListener( this );

            curveAngleTF.getDocument().addDocumentListener( this );
            curveAngleDistTF.getDocument().addDocumentListener( this );
            curveAngleBackTF.getDocument().addDocumentListener( this );
            sectionJitterTF.getDocument().addDocumentListener( this );
            perpCurveAngleTF.getDocument().addDocumentListener( this );
            perpCurveAngleDistTF.getDocument().addDocumentListener( this );
            perpCurveAngleBackTF.getDocument().addDocumentListener( this );
            perpCurveRateTF.getDocument().addDocumentListener( this );
            counterActionTF.getDocument().addDocumentListener( this );
            horAngleDistTF.getDocument().addDocumentListener( this );
            recoverRateTF.getDocument().addDocumentListener( this );
            randomTiltTF.getDocument().addDocumentListener( this );
            randomTiltDivTF.getDocument().addDocumentListener( this );
            twistTurnsTF.getDocument().addDocumentListener( this );
            twistTurnsDistTF.getDocument().addDocumentListener( this );
            leafCurveAngleTF.getDocument().addDocumentListener( this );
            leafCurveAngleBackTF.getDocument().addDocumentListener( this );
            leafCurveAngleDistTF.getDocument().addDocumentListener( this );
            leafCurveRateTF.getDocument().addDocumentListener( this );
            leafDepartureAngleTF.getDocument().addDocumentListener( this );

            densityParameterSL.addChangeListener( this );
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
         *  Sets the radioButton attribute of the CoilModuleDialog object
         *
         *@param  type  The new radioButton value
         *@param  rURB  The new radioButton value
         *@param  rGRB  The new radioButton value
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
         *  Gets the radioType attribute of the CoilModuleDialog object
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
                    @Override
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
                    @Override
                    public void run()
                    {
                        doRunnableUpdate();
                    }
                } );
        }


        /**
         *  Description of the Method
         */
        private void doRShiftButton()
        {
            editDialog = rShiftFunction.edit( this, TapBTranslate.text( "rShiftYValue", module.getName() ),
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
         */
        private void doYStepButton()
        {
            editDialog = yStepFunction.edit( this, TapBTranslate.text( "yStepYValue", module.getName() ),
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
         */
        private void doBranchingAngleButton()
        {
            editDialog = branchingAngleFunction.edit( this, TapBTranslate.text( "branchingAngleYValue", module.getName() ),
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
         */
        private void doPerpAngleButton()
        {
            editDialog = perpAngleFunction.edit( this, TapBTranslate.text( "perpAngleYValue", module.getName() ),
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
         */
        private void doBackAngleButton()
        {
            editDialog = backAngleFunction.edit( this, TapBTranslate.text( "backAngleYValue", module.getName() ),
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
         */
        private void doCurveRateButton()
        {
            editDialog = curveRateFunction.edit( this, TapBTranslate.text( "curveRateYValue", module.getName() ),
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
         */
        private void doCurveAngleButton()
        {
            editDialog = curveAngleFunction.edit( this, TapBTranslate.text( "curveAngleYValue", module.getName() ),
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
         */
        private void doVertAngleButton()
        {
            editDialog = vertAngleFunction.edit( this, TapBTranslate.text( "vertAngleYValue", module.getName() ),
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
         */
        private void doProbButton()
        {
            editDialog = probFunction.edit( this, TapBTranslate.text( "probYValue", module.getName() ),
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
        @Override
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
                    //put back the module in initial state for undo record

                    TapFunction tmpVertAngleFunction = vertAngleFunction;
                    TapFunction tmpCurveAngleFunction = curveAngleFunction;
                    TapFunction tmpCurveRateFunction = curveRateFunction;
                    TapFunction tmpBackAngleFunction = backAngleFunction;
                    TapFunction tmpPerpAngleFunction = perpAngleFunction;
                    TapFunction tmpProbFunction = probFunction;
                    TapFunction tmpBranchingAngleFunction = branchingAngleFunction;
                    TapFunction tmpYStepFunction = yStepFunction;
                    TapFunction tmpRShiftFunction = rShiftFunction;
                    TapFunction tmpYCurveLeafFunction = yCurveLeafFunction;
                    TapFunction tmpYLeafDepartureAngleFunction = yLeafDepartureAngleFunction;
                    getBackValues();
                    procedure.addUndoRecord();
                    vertAngleFunction = tmpVertAngleFunction;
                    curveAngleFunction = tmpCurveAngleFunction;
                    curveRateFunction = tmpCurveRateFunction;
                    backAngleFunction = tmpBackAngleFunction;
                    perpAngleFunction = tmpPerpAngleFunction;
                    probFunction = tmpProbFunction;
                    branchingAngleFunction = tmpBranchingAngleFunction;
                    yStepFunction = tmpYStepFunction;
                    rShiftFunction = tmpRShiftFunction;
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
        @Override
        public void stateChanged( ChangeEvent e )
        {
            if ( setupFlag )
                return;
            modified = true;

            if ( dampingCB.isSelected() )
                dampingTF.setEnabled( true );
            else
                dampingTF.setEnabled( false );

            if ( recoverCB.isSelected() )
                recoverRateTF.setEnabled( true );
            else
                recoverRateTF.setEnabled( false );

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

            if ( useGoldenCB.isSelected() )
            {
                RstartTF.setEnabled( false );
                RstepTF.setEnabled( false );
                probabilityTF.setEnabled( false );
                probButton.setEnabled( false );
                dampingTF.setEnabled( false );
                dampingCB.setEnabled( false );
                ringsRB.setEnabled( false );
                spiralRB.setEnabled( false );
            }
            else
            {
                RstartTF.setEnabled( true );
                RstepTF.setEnabled( true );
                probabilityTF.setEnabled( true );
                probButton.setEnabled( true );
                dampingTF.setEnabled( true );
                dampingCB.setEnabled( true );
                ringsRB.setEnabled( true );
                spiralRB.setEnabled( true );
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

            if ( sourceDoc == RstartTF.getDocument() )
                source = RstartTF;
            else if ( sourceDoc == RstepTF.getDocument() )
                source = RstepTF;
            else if ( sourceDoc == YfromTF.getDocument() )
                source = YfromTF;
            else if ( sourceDoc == YtoTF.getDocument() )
                source = YtoTF;
            else if ( sourceDoc == YstepTF.getDocument() )
                source = YstepTF;
            else if ( sourceDoc == vertAngleTF.getDocument() )
                source = vertAngleTF;
            else if ( sourceDoc == horAngleTF.getDocument() )
                source = horAngleTF;
            else if ( sourceDoc == vertAngleDistTF.getDocument() )
                source = vertAngleDistTF;
            else if ( sourceDoc == horAngleDistTF.getDocument() )
                source = horAngleDistTF;
            else if ( sourceDoc == probabilityTF.getDocument() )
                source = probabilityTF;
            else if ( sourceDoc == YdistTF.getDocument() )
                source = YdistTF;
            else if ( sourceDoc == sizeDistTF.getDocument() )
                source = sizeDistTF;
            else if ( sourceDoc == dampingTF.getDocument() )
                source = dampingTF;
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
            else if ( sourceDoc == counterActionTF.getDocument() )
                source = counterActionTF;
            else if ( sourceDoc == recoverRateTF.getDocument() )
                source = recoverRateTF;
            else if ( sourceDoc == randomTiltTF.getDocument() )
                source = randomTiltTF;
            else if ( sourceDoc == randomTiltDivTF.getDocument() )
                source = randomTiltDivTF;
            else if ( sourceDoc == twistTurnsTF.getDocument() )
                source = randomTiltTF;
            else if ( sourceDoc == twistTurnsDistTF.getDocument() )
                source = randomTiltDivTF;
            else if ( sourceDoc == rShiftTF.getDocument() )
                source = rShiftTF;
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

            if ( ( source == RstartTF ) || ( source == RstepTF ) || ( source == dampingTF ) || ( source == randomTiltDivTF ) )
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
        @Override
        public void changedUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
        public void insertUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
        public void removeUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Gets the values attribute of the CoilModuleDialog object
         */
        private void getValues()
        {
            try
            {
                useGoldenRatio = useGoldenCB.isSelected();
                flipChildren = flipChildrenCB.isSelected();
                Rstep = ( Integer.valueOf( RstepTF.getText().trim() ) ).intValue();
                Rstart = ( Integer.valueOf( RstartTF.getText().trim() ) ).intValue();
                Ystep = Double.parseDouble( YstepTF.getText().trim().replace( ',', '.' ) );
                Yfrom = Double.parseDouble( YfromTF.getText().trim().replace( ',', '.' ) );
                Yto = Double.parseDouble( YtoTF.getText().trim().replace( ',', '.' ) );
                vertAngle = Double.parseDouble( vertAngleTF.getText().trim().replace( ',', '.' ) );
                horAngle = Double.parseDouble( horAngleTF.getText().trim().replace( ',', '.' ) );
                vertAngleDist = Double.parseDouble( vertAngleDistTF.getText().trim().replace( ',', '.' ) );
                horAngleDist = Double.parseDouble( horAngleDistTF.getText().trim().replace( ',', '.' ) );
                probability = Double.parseDouble( probabilityTF.getText().trim().replace( ',', '.' ) );
                Ydist = Double.parseDouble( YdistTF.getText().trim().replace( ',', '.' ) );
                YdistType = getRadioType( YdistURB, YdistGRB );
                vertAngleType = getRadioType( vertAngleURB, vertAngleGRB );
                horAngleType = getRadioType( horAngleURB, horAngleGRB );
                sizeDist = Double.parseDouble( sizeDistTF.getText().trim().replace( ',', '.' ) );
                sizeDistType = getRadioType( sizeDistURB, sizeDistGRB );
                sizeChildren = sizeChildrenCB.isSelected();
                rSizeFactor = (double) rSizeFactorSL.getValue() / 100.0;
                ySizeFactor = (double) ySizeFactorSL.getValue() / 100.0;
                densityParameter = (double) densityParameterSL.getValue() / 100.0;
                rShift = Double.parseDouble( rShiftTF.getText().trim().replace( ',', '.' ) );

                if ( dampingCB.isSelected() )
                    dampingFactor = ( Integer.valueOf( dampingTF.getText().trim() ) )
                            .intValue();
                else
                    dampingFactor = 0;

                isRings = ringsRB.isSelected();
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
                smParms.counterAction = Double.parseDouble( counterActionTF.getText().trim().replace( ',', '.' ) );
                smParms.recover = recoverCB.isSelected();
                smParms.recoverRate = Double.parseDouble( recoverRateTF.getText().trim().replace( ',', '.' ) );
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
                vertAngleFunctionInput = vertAngleFunctionInputCB.isSelected();
                curveAngleFunctionInput = curveAngleFunctionInputCB.isSelected();
                backAngleFunctionInput = backAngleFunctionInputCB.isSelected();
                perpAngleFunctionInput = perpAngleFunctionInputCB.isSelected();
                probFunctionInput = probFunctionInputCB.isSelected();
                curveRateFunctionInput = curveRateFunctionInputCB.isSelected();
                yStepFunctionInput = yStepFunctionInputCB.isSelected();
                rShiftFunctionInput = rShiftFunctionInputCB.isSelected();
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
         *  Gets the backValues attribute of the CoilModuleDialog object
         */
        private void getBackValues()
        {
            useGoldenRatio = backUseGoldenRatio;
            flipChildren = backFlipChildren;
            Rstep = backRstep;
            Rstart = backRstart;
            Ystep = backYstep;
            Yfrom = backYfrom;
            Yto = backYto;
            vertAngle = backVertAngle;
            horAngle = backHorAngle;
            vertAngleDist = backVertAngleDist;
            horAngleDist = backHorAngleDist;
            probability = backProbability;
            Ydist = backYdist;
            YdistType = backYdistType;
            vertAngleType = backVertAngleType;
            horAngleType = backHorAngleType;
            sizeDist = backSizeDist;
            sizeDistType = backSizeDistType;
            sizeChildren = backSizeChildren;
            rSizeFactor = backRSizeFactor;
            ySizeFactor = backYSizeFactor;
            densityParameter = backDensityParameter;
            dampingFactor = backDampingFactor;
            smParms = backParms.duplicate();
            isRings = backIsRings;
            vertAngleFunction = backVertAngleFunction;
            curveAngleFunction = backCurveAngleFunction;
            curveRateFunction = backCurveRateFunction;
            backAngleFunction = backBackAngleFunction;
            perpAngleFunction = backPerpAngleFunction;
            probFunction = backProbFunction;
            branchingAngleFunction = backBranchingAngleFunction;
            yStepFunction = backYStepFunction;
            rShift = backRShift;
            rShiftFunction = backRShiftFunction;
            yCurveLeafFunction = backYCurveLeafFunction;
            yLeafDepartureAngleFunction = backYLeafDepartureAngleFunction;
            vertAngleFunctionInput = backVertAngleFunctionInput;
            curveAngleFunctionInput = backCurveAngleFunctionInput;
            backAngleFunctionInput = backBackAngleFunctionInput;
            perpAngleFunctionInput = backPerpAngleFunctionInput;
            probFunctionInput = backProbFunctionInput;
            curveRateFunctionInput = backCurveRateFunctionInput;
            yStepFunctionInput = backYStepFunctionInput;
            rShiftFunctionInput = backRShiftFunctionInput;
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
                int r = JOptionPane.showConfirmDialog( this, TapBTranslate.text( "parametersModified" ), TapBTranslate.text( "warning" ), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION );

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

