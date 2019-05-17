/*
 *  Clip range module
 */
/*
 *  Copyright (C) 2003 by François Guillet
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
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.io.*;
import java.text.*;
import javax.swing.*;

import artofillusion.tapDesigner.TapModule.*;


/**
 *  Description of the Class
 *
 *@author     François Guillet
 *@created    19 avril 2004
 */
public class RandomValueModule extends TapModule
{
    private static TapModule.ModuleTypeInfo typeInfo;

    private double mean;
    private double stdDev;
    private short distType;
    private RandomValueModule module;


    /**
     *  Constructor for the RandomValueModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public RandomValueModule( TapProcedure procedure, Point position )
    {
        super( procedure, TapBTranslate.text( "random" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "randomName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/randval_tree.png" ) ) );

        mean = 0.5;
        stdDev = 0.5;
        distType = TapRandomGenerator.UNIFORM;
        setNumInput( 0 );
        setNumOutput( 1 );
        setup();
    }


    /**
     *  Description of the Method
     */
    private void setup()
    {
        outputNature[0] = VALUE_PORT;
        outputTooltips = new String[1];
        outputTooltips[0] = TapBTranslate.text( "randomValueOutput" );

        setBackgroundColor( Color.black );
        module = this;
    }


    /**
     *  Constructor for the RandomValueModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public RandomValueModule( DataInputStream in, Scene theScene ) throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        mean = in.readDouble();
        stdDev = in.readDouble();
        distType = in.readShort();
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
    public void writeToFile( DataOutputStream out, Scene theScene ) throws IOException
    {
        super.writeToFile( out, theScene );
        out.writeShort( 0 );
        out.writeDouble( mean );
        out.writeDouble( stdDev );
        out.writeShort( distType );
    }


    /**
     *  Gets the moduleTypeInfo attribute of the RandomValueModule object
     *
     *@return    The moduleTypeInfo value
     */
    @Override
    public ModuleTypeInfo getModuleTypeInfo()
    {
        return typeInfo;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public TapModule duplicate()
    {
        RandomValueModule module = new RandomValueModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
        module.mean = this.mean;
        module.stdDev = this.stdDev;
        module.distType = this.distType;

        return (TapModule) module;
    }


    /**
     *  Gets the value attribute of the RandomValueModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  var         Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The value value
     */
    @Override
    public double getValue( int outputPort, double[] var, long seed )
    {
        if ( outputPort == 0 )
        {
            TapRandomGenerator gen = new TapRandomGenerator( seed );
            double[] yVal = {0.0};

            return gen.getDistribution( mean, stdDev, distType );
        }

        else
        {
            System.out.println( "wrong output port for random value : " + outputPort );

            return (double) 0.0;
            //should never happen !!!
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
        return false;
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

        editDialog = null;

        if ( isEditDialogOn )
            editBDialog.toFront();
        else
        {
            editBDialog = new RandomValueModuleDialog( (JFrame) parentFrame.getComponent() );
            isEditDialogOn = true;
        }
    }


    /*
     *  Mean/stdDev module editor window
     */
    /**
     *  Description of the Class
     *
     *@author     pims
     *@created    19 avril 2004
     */
    private class RandomValueModuleDialog
             extends BFrame
    {
        private BButton okButton;
        private BButton applyButton;
        private BButton cancelButton;
        private BTextField meanTF;
        private BTextField stdDevTF;
        private BRadioButton uniformRB;
        private JFrame parentFrame;
        private double backMean;
        private double backStdDev;
        private short backDistType;
        private boolean modified;
        private NumberFormat format;


        /**
         *  Constructor for the RandomValueModuleDialog object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public RandomValueModuleDialog( JFrame parentFrame )
        {
            super( TapBTranslate.text( "randomValueModuleTitle", name ) );

            int i;
            modified = false;
            backMean = mean;
            backStdDev = stdDev;
            backDistType = distType;

            format = NumberFormat.getInstance();
            format.setMaximumFractionDigits( 3 );
            meanTF = new BTextField( format.format( mean ), 6 );
            stdDevTF = new BTextField( format.format( stdDev ), 6 );

            RadioButtonGroup rbg = new RadioButtonGroup();
            uniformRB = TapBTranslate.bRadioButton( "uniform", distType == TapRandomGenerator.UNIFORM, rbg );

            BRadioButton gaussianRB = TapBTranslate.bRadioButton( "gaussian", distType == TapRandomGenerator.GAUSSIAN, rbg );

            LayoutInfo layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 6, 6, 6, 6 ), new Dimension( 0, 0 ) );
            LayoutInfo layoutTF = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 2, 6, 2, 6 ), new Dimension( 0, 0 ) );
            ColumnContainer cc = new ColumnContainer();
            cc.add( TapBTranslate.bLabel( "enterRandomCoefs" ), layout );

            ColumnContainer ccc = new ColumnContainer();
            RowContainer rc = new RowContainer();
            rc.add( TapBTranslate.bLabel( "randomMean" ), layoutTF );
            rc.add( meanTF, layoutTF );
            ccc.add( rc, layoutTF );
            rc = new RowContainer();
            rc.add( TapBTranslate.bLabel( "pm" ), layoutTF );
            rc.add( stdDevTF, layoutTF );
            ccc.add( rc, layoutTF );

            rc = new RowContainer();
            rc.add( uniformRB, layoutTF );
            rc.add( gaussianRB, layoutTF );
            ccc.add( rc );

            cc.add( ccc, layoutTF );

            okButton = TapBTranslate.bButton( "ok", this, "doOK" );
            cancelButton = TapBTranslate.bButton( "cancel", this, "doCancel" );
            applyButton = TapBTranslate.bButton( "apply", this, "doApplyButton" );

            LayoutInfo buttonLayout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.BOTH, new Insets( 6, 10, 6, 6 ), new Dimension( 0, 0 ) );
            GridContainer gc = new GridContainer( 3, 1 );
            gc.add( okButton, 0, 0, buttonLayout );
            gc.add( applyButton, 1, 0, buttonLayout );
            gc.add( cancelButton, 2, 0, buttonLayout );
            cc.add( gc, layout );
            setContent( cc );
            ( (JFrame) getComponent() ).setLocationRelativeTo( parentFrame );
            pack();
            setVisible( true );

            addEventLink( WindowClosingEvent.class, this, "doCancel" );
            meanTF.addEventLink( ValueChangedEvent.class, this, "doValueChanged" );
            stdDevTF.addEventLink( ValueChangedEvent.class, this, "doValueChanged" );
            uniformRB.addEventLink( SelectionChangedEvent.class, this, "doSelectionChanged" );
        }


        /**
         *  Description of the Method
         */
        private void doSelectionChanged()
        {
            modified = true;
        }


        /**
         *  Description of the Method
         *
         *@param  evt  Description of the Parameter
         */
        private void doValueChanged( ValueChangedEvent evt )
        {
            BTextField tf = (BTextField) evt.getWidget();
            double dum;
            modified = true;

            try
            {
                dum = Double.parseDouble( tf.getText().trim().replace( ',', '.' ) );
                ( (JTextField) tf.getComponent() ).setForeground( Color.black );
            }
            catch ( NumberFormatException ex )
            {
                ( (JTextField) tf.getComponent() ).setForeground( Color.red );
            }

        }


        /**
         *  Gets the values attribute of the RandomValueModuleDialog object
         *
         *@return    The values value
         */
        private boolean getValues()
        {
            double dum;

            try
            {
                mean = Double.parseDouble( meanTF.getText().trim().replace( ',', '.' ) );
                stdDev = Double.parseDouble( stdDevTF.getText().trim().replace( ',', '.' ) );
                distType = uniformRB.getState() ? TapRandomGenerator.UNIFORM : TapRandomGenerator.GAUSSIAN;

                return true;
            }
            catch ( NumberFormatException ex )
            {
                getBackValues();

                return false;
            }

        }


        /**
         *  Gets the backValues attribute of the RandomValueModuleDialog object
         */
        private void getBackValues()
        {
            mean = backMean;
            stdDev = backStdDev;
            distType = backDistType;
        }


        /**
         *  Description of the Method
         */
        private void doOK()
        {
            if ( getValues() )
            {
                if ( modified )
                {
                    //put back the module in initial state for undo record
                    getBackValues();
                    procedure.addUndoRecord();
                    getValues();
                }

                doApply();
                editDialogClosed();
            }
        }


        /**
         *  Description of the Method
         */
        private void doApplyButton()
        {
            if ( getValues() )
                doApply();
        }


        /**
         *  Description of the Method
         */
        private void doCancel()
        {
            if ( modified )
            {
                int r = JOptionPane.showConfirmDialog( (JFrame) ( this.getComponent() ), TapBTranslate.text( "parametersModified" ), TapBTranslate.text( "warning" ), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION );

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

