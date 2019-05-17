/*
 *  Function Module class. Most of its code is taken from FunctionModule.java, written by Peter
 *  Eastman
 */
/*
 *  Copyright (C) 2000,2002 by Peter Eastman, 2003 by François Guillet
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
import buoy.widget.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;

import artofillusion.tapDesigner.TapModule.*;

/**
 *  Description of the Class
 *
 *@author     pims
 *@created    19 avril 2004
 */
public class ValueFunctionModule extends TapModule
{
    private ValueFunctionModule module;
    private static TapModule.ModuleTypeInfo typeInfo;
    TapFunction function;
    TapFunction previousFunction;
    boolean addUndoRecord;


    /**
     *  Constructor for the ValueFunctionModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public ValueFunctionModule( TapProcedure procedure, Point position )
    {
        super( procedure, TapBTranslate.text( "function" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "functionName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/function_tree.png" ) ) );

        setNumInput( 0 );
        setNumOutput( 1 );
        function = new TapFunction();
        setup();
    }


    /**
     *  Description of the Method
     */
    private void setup()
    {
        outputNature[0] = VALUE_PORT;
        outputTooltips = new String[1];
        outputTooltips[0] = TapBTranslate.text( "value" );
        setBackgroundColor( Color.black );
        module = this;
    }


    /**
     *  Constructor for the ValueFunctionModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public ValueFunctionModule( DataInputStream in, Scene theScene ) throws IOException, InvalidObjectException
    {
        super( in, theScene );
        short version = in.readShort();
        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );
        function = new TapFunction( in );
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
        function.writeToFile( out );
    }


    /**
     *  Gets the moduleTypeInfo attribute of the ValueFunctionModule object
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
        ValueFunctionModule module = new ValueFunctionModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
        module.function = function.duplicate();
        return (TapModule) module;
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame   Description of the Parameter
     */
    @Override
    public void edit( BFrame parentFrame )
    {
        super.edit( parentFrame );
        if ( isEditDialogOn )
            editDialog.toFront();
        else
        {
            previousFunction = function.duplicate();
            editDialog = function.edit((JFrame) parentFrame.getComponent(), TapBTranslate.text( "valueFunctionModuleTitle", module.getName() ),
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        doRunnableUpdate();
                    }
                } );
            isEditDialogOn = true;
        }
    }


    /**
     *  Description of the Method
     */
    public void doRunnableUpdate()
    {
        if ( ( (TapFunction.FunctionDialog) editDialog ).isClosing() )
        {
            if ( ( (TapFunction.FunctionDialog) editDialog ).isModified() )
            {
                TapFunction dummyFunction = function;
                function = previousFunction;
                procedure.addUndoRecord();
                function = dummyFunction;
            }
            isEditDialogOn = false;
            editDialog = null;
        }
        doApply();
    }


    /**
     *  Gets the value attribute of the ValueFunctionModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  var         Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The value value
     */
    @Override
    public double getValue( int outputPort, double[] var, long seed )
    {
        if ( outputPort != 0 )
        {
            System.out.println( "Erreur sur le port module fonction :" + outputPort );
            return 0.0;
        }
        if ( var == null )
            return function.calcValue( 0.0 );
        else
            return function.calcValue( var[0] );
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
}

