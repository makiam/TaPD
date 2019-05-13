/*
 *  Unary operation result=f(a)
 */
/*
 *  Copyright (C) 2004 by François Guillet
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
import javax.swing.*;
import artofillusion.tapDesigner.TapModule.*;



/**
 *  This module represents a unary operation : result=f(a)
 *
 *@author     François Guillet
 *@created    19 avril 2004
 */
public class UnaryOpModule extends TapModule
{
    //{{{ variables
    private static TapModule.ModuleTypeInfo typeInfo;
    private short opType;
    private short backOpType;
    private final static short ABS = 0;
    private final static short SINE = 1;
    private final static short COSINE = 2;
    private final static short EXP = 3;
    private final static short LOG = 4;
    private final static short SQUARE_ROOT = 5;
    private static String[] opNames =
            {
            "Abs", "Sine", "Cosine", "Exp", "Log", "Square root"
            };


    //}}}

    //{{{ constructor
    /**
     *  Constructor for the UnaryOpModule object
     *
     *@param  procedure  The procedure which holds the module
     *@param  position   The module graphical location
     */

    public UnaryOpModule( TapProcedure procedure, Point position )
    {
        super( procedure, opNames[0], position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "unaryOpName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/unary_tree.png" ) ) );

        opType = ABS;
        setNumInput( 1 );
        setNumOutput( 1 );
        setup();
    }


    //}}}

    //{{{ module setup method
    /**
     *  Module setup
     */
    private void setup()
    {
        outputNature[0] = VALUE_PORT;
        outputTooltips = new String[1];
        outputTooltips[0] = TapBTranslate.text( "resultOutput" );
        inputNature[0] = VALUE_PORT;
        inputTooltips = new String[1];
        inputTooltips[0] = TapBTranslate.text( "a" );
        opNames[ABS] = TapBTranslate.text( "abs" );
        opNames[SINE] = TapBTranslate.text( "sine" );
        opNames[COSINE] = TapBTranslate.text( "cosine" );
        opNames[SQUARE_ROOT] = TapBTranslate.text( "sqrt" );
        opNames[EXP] = TapBTranslate.text( "exp" );
        opNames[LOG] = TapBTranslate.text( "log" );

        setBackgroundColor( Color.black );
    }


    //}}}

    //{{{ read/write method
    /**
     *  Reads a UnaryOpModule object from file
     *
     *@param  in                          Input stream to read the module from
     *@param  theScene                    The small, standalone, scene to which
     *      the module is attached
     *@exception  IOException             Read exception
     *@exception  InvalidObjectException  Object version Exception
     */
    public UnaryOpModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        opType = in.readShort();
        setName( opNames[opType] );
        setup();
    }


    /**
     *  Save a UnaryOpModule to file
     *
     *@param  out              OutputStream to save to
     *@param  theScene         The small, standalone, scene to which the module
     *      is attached
     *@exception  IOException  Write exception
     */
    @Override
    public void writeToFile( DataOutputStream out, Scene theScene )
        throws IOException
    {
        super.writeToFile( out, theScene );
        out.writeShort( 0 );
        out.writeShort( opType );
    }


    //}}}

    //{{{ get module type info

    /**
     *  Gets the moduleTypeInfo associated to the UnaryOpModule object
     *
     *@return    The module type info
     */
    @Override
    public ModuleTypeInfo getModuleTypeInfo()
    {
        return typeInfo;
    }


    //}}}

    //{{{ duplicate method
    /**
     *  Returns a duplicate of this module.
     *
     *@return    New module
     */
    @Override
    public TapModule duplicate()
    {
        UnaryOpModule module = new UnaryOpModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
        module.opType = this.opType;

        return (TapModule) module;
    }


    //}}}

    //{{{ getValue stuff
    /**
     *  Gets the value output of the UnaryOpModule object
     *
     *@param  outputPort  The index of the output port concerned
     *@param  var         The parameters array used (if need be) for calculation
     *@param  seed        The random seed
     *@return             The value output
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

            System.out.println( "a, op, result : " + aValue + " " + opNames[opType] + " " + unaryOperation( aValue ) );

            return unaryOperation( aValue );
        }

        else
        {
            System.out.println( "wrong output port for unary operation : " + outputPort );

            return (double) 0.0;
            //should never happen !!!
        }
    }


    /**
     *  Actual calculation of the value
     *
     *@param  a  input parameter
     *@return    Calculated value
     */
    double unaryOperation( double a )
    {
        switch ( opType )
        {
            default:
            case ABS:
                return Math.abs( a );
            case SINE:
                return Math.sin( a * Math.PI / 2 );
            case COSINE:
                return Math.cos( a * Math.PI / 2 );
            case LOG:

                if ( a > 0 )
                    return Math.log( 0 );
                else
                    System.out.println( "log(<=0) unary funtion module." );

                return 0;
            case EXP:
                return Math.exp( a );
            case SQUARE_ROOT:

                if ( a >= 0 )
                    return Math.sqrt( 0 );
                else
                    System.out.println( "sqrt(<0) unary function module." );

                return 0;
        }
    }


    //}}}

    //{{{ acceptsMainEntry : doesn't accept main entry nor preview
    /**
     *  This module isn't eligible for being a main entry and thus this method
     *  will always return false.
     *
     *@return    Always false
     */
    @Override
    public boolean acceptsMainEntry()
    {
        return false;
    }


    /**
     *  This module doesn't accept previews and thus this method will always
     *  return false.
     *
     *@return    Always false
     */
    @Override
    public boolean acceptsPreview()
    {
        return false;
    }


    //}}}

    //{{{ edit
    /**
     *  This method creates and displays an edit window.
     *
     *@param  parentFrame  the parent frame
     */
    @Override
    public void edit( BFrame parentFrame )
    {

        editDialog = null;

        if ( isEditDialogOn )
            editBDialog.toFront();
        else
        {
            editBDialog = new EditWidgetDialogBase( parentFrame, this );
            isEditDialogOn = true;
        }
    }

    /**
     *  Gets the number of edit frames used by the unary operation module
     *
     *@return    The number of edit frames to take into account
     */
    @Override
    public int getNumEditWidgets()
    {
        return 1;
    }


    /**
     *  Gets the edit frame referenced by index
     *
     *@param  index       The reference to the edit frame
     *@param  cb          The Runnable called when validating modifications
     *@param  standalone  Whether the widget is in standalone frame or embedded
     *@return             The edit frame widget
     */
    @Override
    public Widget getEditWidget( int index, Runnable cb, boolean standalone )
    {
        //System.out.println( "getEditWidget" );
        return new UnaryOpEditWidget( cb, standalone, this );
    }


    /**
     *  Returns the name of the edit frame referenced by index
     *
     *@param  index  The reference to the edit frame
     *@return        The edit frame value
     */
    @Override
    public String getEditWidgetName( int index )
    {
        return "";
    }


    /**
     *  Unary Operation module edit widget.
     *
     *@author     François Guillet
     *@created    14 mai 2004
     */
    public class UnaryOpEditWidget extends EditWidgetBase
    {
        private BComboBox opCombo;


        /**
         *  Constructor for the UnaryOpEditWidget object
         *
         *@param  cb          Runnable to call when updating.
         *@param  standalone  In a standalone window of in a properties split
         *      pane.
         *@param  module      The module that owns the edit widget.
         */
        public UnaryOpEditWidget( Runnable cb, boolean standalone, TapModule module )
        {
            super( cb, standalone, "unaryOpModuleTitle", module );

            int i;

            Object[] objects = new Object[opNames.length];

            for ( i = 0; i < opNames.length; ++i )
                objects[i] = (Object) opNames[i];

            opCombo = new BComboBox( objects );
            opCombo.setSelectedIndex( opType );

            BorderContainer content = new BorderContainer();
            LayoutInfo layout = new LayoutInfo( LayoutInfo.SOUTH, LayoutInfo.NONE, new Insets( 5, 5, 5, 5 ), new Dimension( 0, 0 ) );
            content.add( TapBTranslate.bLabel( "chooseFunction" ), BorderContainer.NORTH, layout );
            layout = new LayoutInfo( LayoutInfo.NORTH, LayoutInfo.NONE, new Insets( 5, 5, 5, 5 ), new Dimension( 0, 0 ) );
            content.add( opCombo, BorderContainer.CENTER, layout );
            layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.BOTH, new Insets( 5, 5, 5, 5 ), new Dimension( 0, 0 ) );
            add( content, BorderContainer.CENTER, layout );

            opCombo.addEventLink( ValueChangedEvent.class, this, "doModified" );
        }


        /**
         *  Fetch the values currently displayed object
         */
        @Override
        protected void getValues()
        {
            opType = (short) opCombo.getSelectedIndex();

            if ( opType >= opNames.length )
                opType = 0;
            initButtons();
            module.setName( opNames[opType] );
            module.getProcedure().getProcPanel().minorViewSync( null );

        }


        /**
         *  Initializes backup values
         */
        @Override
        protected void initBackValues()
        {
            backOpType = opType;
        }


        /**
         *  Gets the undo values
         */
        @Override
        protected void getUndoValues()
        {
            opType = backOpType;
            module.setName( opNames[opType] );
        }


        /**
         *  Reload displayed values from module
         *
         *@param  force  Description of the Parameter
         */
        @Override
        public void showValues( boolean force )
        {
            if ( force || changed )
            {
                opCombo.setSelectedIndex( opType );
                if ( opType == backOpType )
                {
                    initButtons();
                }
                else
                {
                    if ( !standalone )
                    {
                        validateButton.setEnabled( true );
                        testButton.setEnabled( false );
                        revertButton.setEnabled( true );
                    }
                    else
                    {
                        validateButton.setEnabled( true );
                        testButton.setEnabled( false );
                    }
                }
                updateNameLabel();
                super.showValues( force );
            }
        }

    }

    //}}}

}

