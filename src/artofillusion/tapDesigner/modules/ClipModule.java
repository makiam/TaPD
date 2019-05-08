/*
 *  Clip range module
 */
/*
 *  Copyright (C) 2004 by Francois Guillet
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
public class ClipModule
         extends TapModule
{
    private static TapModule.ModuleTypeInfo typeInfo;
    private double min;
    private double max;
    private ClipModule module;


    /**
     *  Constructor for the ClipModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public ClipModule( TapProcedure procedure, Point position )
    {
        super( procedure, TapBTranslate.text( "clip" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "clipName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/clip_tree.png" ) ) );

        min = 0.0;
        max = 1.0;
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
        inputTooltips[0] = TapDesignerTranslate.text( "valueToClip" );

        setBackgroundColor( Color.black );
        module = this;
    }


    /**
     *  Constructor for the ClipModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public ClipModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        min = in.readDouble();
        max = in.readDouble();
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
        out.writeDouble( min );
        out.writeDouble( max );
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public TapModule duplicate()
    {
        ClipModule module = new ClipModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
        module.min = this.min;
        module.max = this.max;

        return (TapModule) module;
    }


    /**
     *  Gets the value attribute of the ClipModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  var         Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The value value
     */
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
            if ( aValue <= min )
                aValue = min;
            else if ( aValue >= max )
                aValue = max;
            return aValue;
        }

        else
        {
            System.out.println( "wrong output port for clip : " + outputPort );

            return (double) 0.0;
            //should never happen !!!
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
        return false;
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame   Description of the Parameter
     */
    public void edit( BFrame parentFrame )
    {
        super.edit( parentFrame );

        editDialog = null;

        if ( isEditDialogOn )
            ( (JFrame) ( editBDialog.getComponent() ) ).toFront();
        else
        {
            editBDialog = new ClipModuleDialog( (JFrame) parentFrame.getComponent() );
            isEditDialogOn = true;
        }
    }


    /**
     *  Gets the moduleTypeInfo attribute of the ClipModule object
     *
     *@return    The moduleTypeInfo value
     */
    public ModuleTypeInfo getModuleTypeInfo()
    {
        return typeInfo;
    }


    /**
     *  Min/max module editor window
     *
     *@author     Francois Guillet
     *@created    19 avril 2004
     */
    private class ClipModuleDialog
             extends BFrame
    {
        private BButton okButton;
        private BButton applyButton;
        private BButton cancelButton;
        private BTextField minTF;
        private BTextField maxTF;
        private JFrame parentFrame;
        private double backMin;
        private double backMax;
        private boolean modified;
        private NumberFormat format;


        /**
         *  Constructor for the ClipModuleDialog object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public ClipModuleDialog( JFrame parentFrame )
        {
            super( TapBTranslate.text( "clipModuleTitle", name ) );

            int i;
            modified = false;
            backMin = min;
            backMax = max;

            format = NumberFormat.getInstance();
            format.setMaximumFractionDigits( 3 );
            minTF = new BTextField( format.format( min ), 6 );
            maxTF = new BTextField( format.format( max ), 6 );

            LayoutInfo layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 6, 6, 6, 6 ), new Dimension( 0, 0 ) );
            LayoutInfo layoutTF = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 2, 6, 2, 6 ), new Dimension( 0, 0 ) );
            ColumnContainer cc = new ColumnContainer();
            cc.add( TapBTranslate.bLabel( "enterClipRange" ), layout );

            RowContainer rc = new RowContainer();
            rc.add( TapBTranslate.bLabel( "clipFrom" ), layoutTF );
            rc.add( minTF, layoutTF );
            cc.add( rc, layoutTF );
            rc = new RowContainer();
            rc.add( TapBTranslate.bLabel( "clipTo" ), layoutTF );
            rc.add( maxTF, layoutTF );
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
            minTF.addEventLink( ValueChangedEvent.class, this, "doValueChanged" );
            minTF.addEventLink( ValueChangedEvent.class, this, "doValueChanged" );
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
         *  Gets the values attribute of the ClipModuleDialog object
         *
         *@return    The values value
         */
        private boolean getValues()
        {
            double dum;

            try
            {
                min = Double.parseDouble( minTF.getText().trim().replace( ',', '.' ) );
                max = Double.parseDouble( maxTF.getText().trim().replace( ',', '.' ) );

                if ( min <= max )
                    return true;
                return false;
            }
            catch ( NumberFormatException ex )
            {
                getBackValues();
                return false;
            }
        }


        /**
         *  Gets the backValues attribute of the ClipModuleDialog object
         */
        private void getBackValues()
        {
            min = backMin;
            max = backMax;
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

