/*
 *  Scale-shift module
 */
/*
 *  Copyright (C) 2004 by Francois Guillet
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
 *@author     pims
 *@created    19 avril 2004
 */
public class ScaleShiftModule extends TapModule
{
    private double scale;
    private double shift;
    private ScaleShiftModule module;

    private static TapModule.ModuleTypeInfo typeInfo;


    /**
     *  Constructor for the ScaleShiftModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public ScaleShiftModule( TapProcedure procedure, Point position )
    {
        super( procedure, TapBTranslate.text( "scaleShift" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "scaleShiftName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/scale_tree.png" ) ) );

        scale = 1.0;
        shift = 0.0;
        setNumInput( 1 );
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
        outputTooltips[0] = TapDesignerTranslate.text( "resultOutput" );
        inputNature[0] = VALUE_PORT;
        inputTooltips = new String[1];
        inputTooltips[0] = TapDesignerTranslate.text( "valueToScaleShift" );

        setBackgroundColor( Color.black );
        module = this;
    }


    /**
     *  Constructor for the ScaleShiftModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public ScaleShiftModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        scale = in.readDouble();
        shift = in.readDouble();
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
        out.writeDouble( scale );
        out.writeDouble( shift );
    }


    /**
     *  Gets the moduleTypeInfo attribute of the ScaleShiftModule object
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
        ScaleShiftModule module = new ScaleShiftModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
        module.scale = this.scale;
        module.shift = this.shift;

        return (TapModule) module;
    }


    /**
     *  Gets the value attribute of the ScaleShiftModule object
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
            BackModuleLink backLinks = procedure.getBackLink();
            BackModuleLink.BackLink bl = null;
            double aValue;
            double[] yVal = {0.0};

            bl = backLinks.findModule( this, 0 );

            if ( bl != null )
                aValue = bl.fromModule.getValue( bl.outputPort, yVal, gen.getSeed() );
            else
                aValue = 0;

            System.out.println( "a, result : " + aValue + " " + ( aValue * scale + shift ) );

            return aValue * scale + shift;
        }

        else
        {
            System.out.println( "wrong output port for scale/shift : " + outputPort );

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
            editBDialog = new ScaleShiftModuleDialog( (JFrame) parentFrame.getComponent() );
            isEditDialogOn = true;
        }
    }


    /*
     *  Scale/shift module editor window
     */
    /**
     *  Description of the Class
     *
     *@author     pims
     *@created    19 avril 2004
     */
    private class ScaleShiftModuleDialog
             extends BFrame
    {
        private BButton okButton;
        private BButton applyButton;
        private BButton cancelButton;
        private BTextField scaleTF;
        private BTextField shiftTF;
        private JFrame parentFrame;
        private double backScale;
        private double backShift;
        private boolean modified;
        private NumberFormat format;


        /**
         *  Constructor for the ScaleShiftModuleDialog object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public ScaleShiftModuleDialog( JFrame parentFrame )
        {
            super( TapBTranslate.text( "scaleShiftModuleTitle", name ) );

            int i;
            modified = false;
            backScale = scale;
            backShift = shift;

            format = NumberFormat.getInstance();
            format.setMaximumFractionDigits( 3 );
            scaleTF = new BTextField( format.format( scale ), 6 );
            shiftTF = new BTextField( format.format( shift ), 6 );

            LayoutInfo layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 6, 6, 6, 6 ), new Dimension( 0, 0 ) );
            LayoutInfo layoutTF = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 2, 6, 2, 6 ), new Dimension( 0, 0 ) );
            ColumnContainer cc = new ColumnContainer();
            cc.add( TapBTranslate.bLabel( "enterCoefs" ), layout );

            RowContainer rc = new RowContainer();
            rc.add( TapBTranslate.bLabel( "scale" ), layoutTF );
            rc.add( scaleTF, layoutTF );
            cc.add( rc, layoutTF );
            rc = new RowContainer();
            rc.add( TapBTranslate.bLabel( "shift" ), layoutTF );
            rc.add( shiftTF, layoutTF );
            cc.add( rc, layoutTF );

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
            scaleTF.addEventLink( ValueChangedEvent.class, this, "doValueChanged" );
            scaleTF.addEventLink( ValueChangedEvent.class, this, "doValueChanged" );
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
         *  Gets the values attribute of the ScaleShiftModuleDialog object
         *
         *@return    The values value
         */
        private boolean getValues()
        {
            double dum;

            try
            {
                scale = Double.parseDouble( scaleTF.getText().trim().replace( ',', '.' ) );
                shift = Double.parseDouble( shiftTF.getText().trim().replace( ',', '.' ) );

                return true;
            }
            catch ( NumberFormatException ex )
            {
                getBackValues();

                return false;
            }
        }


        /**
         *  Gets the backValues attribute of the ScaleShiftModuleDialog object
         */
        private void getBackValues()
        {
            scale = backScale;
            shift = backShift;
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

