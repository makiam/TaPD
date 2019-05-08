/*
 *  This class encapsulates parameters sent to TapSplineMesh instances .
 */
/*
 *  Copyright (C) 2003 by Francois Guillet
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.tapDesigner;

import java.io.*;

/**
 *  Description of the Class
 *
 *@author     pims
 *@created    12 ao�t 2004
 */
public class TapDistortParameters
{
    /**
     *  Description of the Field
     */
    public double curveAngle, curveAngleBack, curveAngleDist, sectionJitter;
    /**
     *  Description of the Field
     */
    public double perpCurveAngle, perpCurveAngleDist, perpCurveAngleBack;
    /**
     *  Description of the Field
     */
    public double curveRate, perpCurveRate;
    /**
     *  Description of the Field
     */
    public double counterAction, recoverRate;
    /**
     *  Description of the Field
     */
    public double randomTilt, twistTurns, twistTurnsDist;
    /**
     *  Description of the Field
     */
    public double leafCurveAngle, leafCurveAngleBack, leafCurveAngleDist, leafCurveRate;
    /**
     *  Description of the Field
     */
    public double leafDepartureAngle, leafRRatio;
    /**
     *  Description of the Field
     */
    public short curveAngleDistType, perpCurveAngleDistType, twistDistType, leafCurveAngleDistType;
    /**
     *  Description of the Field
     */
    public int randomTiltDiv;
    /**
     *  Description of the Field
     */
    public boolean recover;
    long seed;


    /**
     *  Constructor for the TapDistortParameters object
     */
    public TapDistortParameters()
    {
        curveAngle = 0;
        curveAngleBack = 0;
        curveAngleDist = 0;
        curveAngleDistType = TapRandomGenerator.UNIFORM;
        sectionJitter = 0;
        perpCurveAngle = 0;
        perpCurveAngleDist = 0;
        perpCurveAngleBack = 0;
        perpCurveAngleDistType = TapRandomGenerator.UNIFORM;
        curveRate = 1.0;
        perpCurveRate = 1.0;
        counterAction = 0;
        recover = true;
        recoverRate = 1.0;
        randomTiltDiv = 3;
        randomTilt = 0.0;
        twistTurns = 0.0;
        twistTurnsDist = 0.0;
        twistDistType = TapRandomGenerator.UNIFORM;
        leafCurveAngle = 0;
        leafCurveAngleBack = 0;
        leafCurveAngleDist = 0;
        leafCurveAngleDistType = TapRandomGenerator.UNIFORM;
        leafCurveRate = 1;
        leafRRatio = 1.0;
        leafDepartureAngle = 0;
        seed = 0;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public TapDistortParameters duplicate()
    {
        TapDistortParameters newParms = new TapDistortParameters();
        newParms.curveAngle = curveAngle;
        newParms.curveAngleBack = curveAngleBack;
        newParms.curveAngleDist = curveAngleDist;
        newParms.sectionJitter = sectionJitter;
        newParms.perpCurveAngle = perpCurveAngle;
        newParms.perpCurveAngleBack = perpCurveAngleBack;
        newParms.perpCurveAngleDist = perpCurveAngleDist;
        newParms.curveRate = curveRate;
        newParms.perpCurveRate = perpCurveRate;
        newParms.seed = seed;
        newParms.curveAngleDistType = curveAngleDistType;
        newParms.perpCurveAngleDistType = perpCurveAngleDistType;
        newParms.counterAction = counterAction;
        newParms.recover = recover;
        newParms.recoverRate = recoverRate;
        newParms.randomTiltDiv = randomTiltDiv;
        newParms.randomTilt = randomTilt;
        newParms.twistTurns = twistTurns;
        newParms.twistTurnsDist = twistTurnsDist;
        newParms.twistDistType = twistDistType;
        newParms.leafCurveAngle = leafCurveAngle;
        newParms.leafCurveAngleBack = leafCurveAngleBack;
        newParms.leafCurveAngleDist = leafCurveAngleDist;
        newParms.leafCurveAngleDistType = leafCurveAngleDistType;
        newParms.leafCurveRate = leafCurveRate;
        newParms.leafRRatio = leafRRatio;
        newParms.leafDepartureAngle = leafDepartureAngle;
        return newParms;
    }


    /**
     *  Constructor for the TapDistortParameters object
     *
     *@param  in               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public TapDistortParameters( DataInputStream in )
        throws IOException
    {
        short version = in.readShort();
        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );
        curveAngle = in.readDouble();
        curveAngleBack = in.readDouble();
        curveAngleDist = in.readDouble();
        curveAngleDistType = in.readShort();
        sectionJitter = in.readDouble();
        perpCurveAngle = in.readDouble();
        perpCurveAngleDist = in.readDouble();
        perpCurveAngleBack = in.readDouble();
        perpCurveAngleDistType = in.readShort();
        curveRate = in.readDouble();
        perpCurveRate = in.readDouble();
        counterAction = in.readDouble();
        recover = in.readBoolean();
        recoverRate = in.readDouble();
        randomTilt = in.readDouble();
        randomTiltDiv = in.readInt();
        twistTurns = in.readDouble();
        twistTurnsDist = in.readDouble();
        twistDistType = in.readShort();
        leafCurveAngle = in.readDouble();
        leafCurveAngleBack = in.readDouble();
        leafCurveAngleDist = in.readDouble();
        leafCurveRate = in.readDouble();
        leafDepartureAngle = in.readDouble();
        leafRRatio = in.readDouble();
        leafCurveAngleDistType = in.readShort();
        seed = 0;
    }


    /**
     *  Description of the Method
     *
     *@param  out              Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void writeToFile( DataOutputStream out )
        throws IOException
    {
        out.writeShort( 0 );
        out.writeDouble( curveAngle );
        out.writeDouble( curveAngleBack );
        out.writeDouble( curveAngleDist );
        out.writeShort( curveAngleDistType );
        out.writeDouble( sectionJitter );
        out.writeDouble( perpCurveAngle );
        out.writeDouble( perpCurveAngleDist );
        out.writeDouble( perpCurveAngleBack );
        out.writeShort( perpCurveAngleDistType );
        out.writeDouble( curveRate );
        out.writeDouble( perpCurveRate );
        out.writeDouble( counterAction );
        out.writeBoolean( recover );
        out.writeDouble( recoverRate );
        out.writeDouble( randomTilt );
        out.writeInt( randomTiltDiv );
        out.writeDouble( twistTurns );
        out.writeDouble( twistTurnsDist );
        out.writeShort( twistDistType );
        out.writeDouble( leafCurveAngle );
        out.writeDouble( leafCurveAngleBack );
        out.writeDouble( leafCurveAngleDist );
        out.writeDouble( leafCurveRate );
        out.writeDouble( leafDepartureAngle );
        out.writeDouble( leafRRatio );
        out.writeShort( leafCurveAngleDistType );
    }


    /**
     *  Gets the firstEditWidget attribute of the TapDistortParameters object
     *
     *@param  cb          Description of the Parameter
     *@param  standalone  Description of the Parameter
     *@param  title       Description of the Parameter
     *@param  module      Description of the Parameter
     *@return             The firstEditWidget value
     */
    public FirstEditWidget getFirstEditWidget( Runnable cb, boolean standalone, String title, TapModule module )
    {
        return new FirstEditWidget( cb, standalone, title, module );
    }


    /**
     *  Description of the Class
     *
     *@author     pims
     *@created    12 ao�t 2004
     */
    public class FirstEditWidget
             extends EditWidgetBase
    {

        /**
         *  Constructor for the FirstEditWidget object
         *
         *@param  cb          Runnable to call when updating.
         *@param  standalone  In a standalone window of in a properties split
         *      pane.
         *@param  module      The module that owns the edit widget.
         *@param  title       Description of the Parameter
         */
        public FirstEditWidget( Runnable cb, boolean standalone, String title, TapModule module )
        {
            super( cb, standalone, title, module );
        }


        /**
         *  Description of the Method
         */
        private void updateValues()
        {
        }


        /**
         *  Gets the undoValues
         */
        protected void getUndoValues()
        {
        }


        /**
         *  Gets the backValues
         */
        protected void getValues()
        {
        }


        /**
         *  Initializes backup values
         */
        protected void initBackValues()
        {
        }
    }
}

