/*
 *  Binary operation result=f(a,b)
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
import javax.swing.*;
import artofillusion.tapDesigner.TapModule.*;


/**
 *  This module represents a unary operation : result=f(a,b)
 *
 *@author     Francois Guillet
 *@created    19 avril 2004
 */
public class BinaryOpModule
         extends TapModule
{
    //{{{ Variables
    private static TapModule.ModuleTypeInfo typeInfo;
    private short opType;
    private short backOpType;
    private final static short PLUS = 0;
    private final static short MINUS = 1;
    private final static short MULTIPLY = 2;
    private final static short DIVIDE = 3;
    private final static short GREATER_THAN = 4;
    private final static short LOWER_THAN = 5;
    private final static short MIN = 6;
    private final static short MAX = 7;
    private final static short POWER = 8;
    private static String[] opNames =
            {
            "+", "-", "*", "/", ">", "<", "Min", "Max", "Power"
            };


//}}}

    //{{{ Constructor
    /**
     *  Constructor for the BinaryOpModule object
     *
     *@param  procedure  The procedure which holds the module
     *@param  position   The module graphical location
     */
    public BinaryOpModule( TapProcedure procedure, Point position )
    {
        super( procedure, opNames[0], position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "binaryOpName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/binary_tree.png" ) ) );
        opType = PLUS;
        setNumInput( 2 );
        setNumOutput( 1 );
        setup();
    }


    //}}}

    //{{{ Module setup
    /**
     *  Module setup
     */
    private void setup()
    {
        outputNature[0] = VALUE_PORT;
        outputTooltips = new String[1];
        outputTooltips[0] = TapDesignerTranslate.text( "resultOutput" );
        inputNature[0] = VALUE_PORT;
        inputNature[1] = VALUE_PORT;
        inputTooltips = new String[2];
        inputTooltips[0] = TapDesignerTranslate.text( "a" );
        inputTooltips[1] = TapDesignerTranslate.text( "b" );
        setBackgroundColor( Color.black );
        opNames[POWER] = TapBTranslate.text( "power" );
        opNames[MIN] = TapBTranslate.text( "min" );
        opNames[MAX] = TapBTranslate.text( "max" );
    }


    //}}}

    //{{{ read/write method
    /**
     *  Reads a BinaryOpModule object from file
     *
     *@param  in                          Input stream to read the module from
     *@param  theScene                    The small, standalone, scene to which
     *      the module is attached
     *@exception  IOException             Read exception
     *@exception  InvalidObjectException  Object version Exception
     */
    public BinaryOpModule( DataInputStream in, Scene theScene )
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
     *  Save a BinaryOpModule to file
     *
     *@param  out              OutputStream to save to
     *@param  theScene         The small, standalone, scene to which the module
     *      is attached
     *@exception  IOException  Write exception
     */
    public void writeToFile( DataOutputStream out, Scene theScene )
        throws IOException
    {
        super.writeToFile( out, theScene );
        out.writeShort( 0 );
        out.writeShort( opType );
    }


    //}}}

    //{{{ duplicate
    /**
     *  Returns a duplicate of this module
     *
     *@return    New module
     */
    public TapModule duplicate()
    {
        BinaryOpModule module = new BinaryOpModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
        module.opType = this.opType;

        return (TapModule) module;
    }


    //}}}

    //{{{ get value
    /**
     *  Gets the value output of the BinaryOpModule object
     *
     *@param  outputPort  The index of the output port concerned
     *@param  var         The parameters array used (if need be) for calculation
     *@param  seed        The random seed
     *@return             The value output
     */
    public double getValue( int outputPort, double[] var, long seed )
    {
        if ( outputPort == 0 )
        {
            TapRandomGenerator gen = new TapRandomGenerator( seed );
            BackModuleLink backLinks = procedure.getBackLink();
            BackModuleLink.BackLink bl = null;
            double aValue;
            double bValue;
            double[] yVal = {0.0};

            bl = backLinks.findModule( this, 0 );

            if ( bl != null )
                aValue = bl.fromModule.getValue( bl.outputPort, yVal, gen.getSeed() );
            else
                aValue = 0;

            bl = backLinks.findModule( this, 1 );

            if ( bl != null )
                bValue = bl.fromModule.getValue( bl.outputPort, yVal, gen.getSeed() );
            else
                bValue = 0;

            /*
             *  System.out.println("a, b, op, result : "+aValue+" "+bValue+" "+opNames[opType]+" "+
             *  binaryOperation(aValue,bValue));
             */
            return binaryOperation( aValue, bValue );
        }

        else
        {
            System.out.println( "wrong output port for binary operation : " + outputPort );

            return (double) 0.0;
            //should never happen !!!
        }
    }


    //}}}

    //{{{ value calculus

    /**
     *  Actual calculation of the value
     *
     *@param  a  first input parameter
     *@param  b  second input parameter
     *@return    Calculated value
     */

    double binaryOperation( double a, double b )
    {
        switch ( opType )
        {
            default:
            case PLUS:
                return a + b;
            case MINUS:
                return a - b;
            case MULTIPLY:
                return a * b;
            case DIVIDE:

                if ( b != 0 )
                    return a / b;
                else
                    System.out.println( "Divide by zero in binary operation module." );

                return 0;
            case GREATER_THAN:

                if ( a > b )
                    return a;

                return b;
            case LOWER_THAN:

                if ( a < b )
                    return a;

                return b;
            case MIN:

                if ( a < b )
                    return a;

                return b;
            case MAX:

                if ( a > b )
                    return a;

                return b;
            case POWER:
                return Math.pow( a, b );
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
    public void edit( BFrame parentFrame )
    {
        super.edit( parentFrame );

        editDialog = null;

        if ( isEditDialogOn )
            editBDialog.toFront();
        else
        {
            editBDialog = new EditWidgetDialogBase( parentFrame, this );
            isEditDialogOn = true;
        }
    }


    //}}}

    //{{{ get module type info
    /**
     *  Gets the moduleTypeInfo associated to the BinaryOpModule object
     *
     *@return    The module type info
     */
    public ModuleTypeInfo getModuleTypeInfo()
    {
        return typeInfo;
    }


    //}}}

    //{{{ edit frame methods and class
    /**
     *  Gets the number of edit frames used by the unary operation module
     *
     *@return    The number of edit frames to take into account
     */
    public int getNumEditWidgets()
    {
        return 1;
    }


    /**
     *  Gets the edit frame referenced by index.
     *
     *@param  index       The reference to the edit frame
     *@param  cb          The Runnable called when validating modifications
     *@param  standalone  Whether the widget is in standalone frame or embedded
     *@return             The edit frame widget
     */
    public Widget getEditWidget( int index, Runnable cb, boolean standalone )
    {
        //System.out.println( "getEditWidget" );
        return new BinaryOpEditWidget( cb, standalone, this );
    }


    /**
     *  Returns the name of the edit frame referenced by index.
     *
     *@param  index  The reference to the edit frame
     *@return        The edit frame value
     */
    public String getEditWidgetName( int index )
    {
        return "";
    }


    /**
     *  Binary Operation module edit widget
     *
     *@author     Francois Guillet
     *@created    14 mai 2004
     */
    public class BinaryOpEditWidget
             extends EditWidgetBase
    {
        private BComboBox opCombo;


        /**
         *  Constructor for the BinaryOpEditWidget object
         *
         *@param  cb          Description of the Parameter
         *@param  standalone  Description of the Parameter
         *@param  module      Description of the Parameter
         */
        public BinaryOpEditWidget( Runnable cb, boolean standalone, TapModule module )
        {
            super( cb, standalone, "binaryOpModuleTitle", module );

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
        protected void initBackValues()
        {
            backOpType = opType;
        }


        /**
         *  Gets the backup values
         */
        protected void getBackValues()
        {
            getUndoValues();
            module.getProcedure().getProcPanel().minorViewSync( null );
        }


        /**
         *  Gets the undo values
         */
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

