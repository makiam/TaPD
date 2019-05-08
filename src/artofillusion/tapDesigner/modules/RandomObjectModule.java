/*
 *  This module allows to choose randomly the upstream or downstream path based
 *  on occurence probabilities entered by the user in the edit window.
 */
/*
 *  Copyright (C) 2003 by Francois Guillet
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
import artofillusion.object.*;
import artofillusion.tapDesigner.BackModuleLink.BackLink;

import buoy.event.*;
import buoy.widget.*;


import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import artofillusion.tapDesigner.TapModule.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;


/**
 *  Description of the Class
 *
 *@author     pims
 *@created    19 avril 2004
 */
public class RandomObjectModule extends TapModule
{
    private RandomObjectModule module;
    private static TapModule.ModuleTypeInfo typeInfo;

    private int numInputProbs;
    private int numOutputProbs;
    private int[] inputProbsTable;
    private int[] outputProbsTable;
    private double[] inputProbs;
    private double[] outputProbs;
    private double[] cumulativeInputProbs;
    private double[] cumulativeOutputProbs;
    private int[] outputPortTable;


    /**
     *  Constructor for the RandomObjectModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public RandomObjectModule( TapProcedure procedure, Point position )
    {
        super(procedure, TapBTranslate.text( "random" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "randomObjectName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/randobj_tree.png" ) ) );

        setNumInput( 1 );
        setNumOutput( 1 );
        setup();
        numInputProbs = 0;
        numOutputProbs = 0;
        inputProbs = null;
        outputProbs = null;
        inputProbsTable = null;
        outputProbsTable = null;
    }


    /**
     *  Description of the Method
     */
    private void setup()
    {
        inputNature[0] = OBJECT_PORT;
        outputNature[0] = OBJECT_PORT;
        inputTooltips = new String[1];
        inputTooltips[0] = TapBTranslate.text( "portInput" );
        outputTooltips = new String[1];
        outputTooltips[0] = TapBTranslate.text( "portOutput" );
        setBackgroundColor( Color.orange.darker() );
        module = this;
    }


    /**
     *  Constructor for the RandomObjectModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public RandomObjectModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        int i;
        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        numInputProbs = in.readInt();
        numOutputProbs = in.readInt();

        if ( numInputProbs > 0 )
        {
            inputProbsTable = new int[numInputProbs];
            inputProbs = new double[numInputProbs];

            for ( i = 0; i < numInputProbs; ++i )
            {
                inputProbsTable[i] = in.readInt();
                inputProbs[i] = in.readDouble();
            }
        }
        else
        {
            inputProbsTable = null;
            inputProbs = null;
        }

        if ( numOutputProbs > 0 )
        {
            outputProbsTable = new int[numOutputProbs];
            outputProbs = new double[numOutputProbs];

            for ( i = 0; i < numOutputProbs; ++i )
            {
                outputProbsTable[i] = in.readInt();
                outputProbs[i] = in.readDouble();
            }
        }
        else
        {
            outputProbsTable = null;
            outputProbs = null;
        }

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

        int i;
        out.writeShort( 0 );
        out.writeInt( numInputProbs );
        out.writeInt( numOutputProbs );

        if ( numInputProbs > 0 )
        {
            for ( i = 0; i < numInputProbs; ++i )
            {
                out.writeInt( inputProbsTable[i] );
                out.writeDouble( inputProbs[i] );
            }
        }

        if ( numOutputProbs > 0 )
        {
            for ( i = 0; i < numOutputProbs; ++i )
            {
                out.writeInt( outputProbsTable[i] );
                out.writeDouble( outputProbs[i] );
            }
        }
    }


    /**
     *  Gets the moduleTypeInfo attribute of the RandomObjectModule object
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
        return duplicate( 0 );
    }


    /**
     *  Description of the Method
     *
     *@param  offset  Description of the Parameter
     *@return         Description of the Return Value
     */
    @Override
    public TapModule duplicate( int offset )
    {
        int i;
        RandomObjectModule module = new RandomObjectModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
        module.numInputProbs = numInputProbs;
        module.numOutputProbs = numOutputProbs;

        if ( numInputProbs > 0 )
        {
            module.inputProbs = new double[numInputProbs];
            module.inputProbsTable = new int[numInputProbs];

            for ( i = 0; i < numInputProbs; ++i )
            {
                module.inputProbs[i] = inputProbs[i];
                module.inputProbsTable[i] = inputProbsTable[i] + offset;
            }

        }

        if ( numOutputProbs > 0 )
        {
            module.outputProbs = new double[numOutputProbs];
            module.outputProbsTable = new int[numOutputProbs];

            for ( i = 0; i < numOutputProbs; ++i )
            {
                module.outputProbs[i] = outputProbs[i];
                module.outputProbsTable[i] = outputProbsTable[i] + offset;
            }
        }
        return (TapModule) module;
    }


    /**
     *  If the modules vector is modified, links reference table must be
     *  updated. the translationTable parameter is such that newindex =
     *  translationTable[index]. If newIndex == -1, then this module is going to
     *  be deleted, and the link must be destroyed.
     *
     *@param  translationTable  Translation table to apply
     */
    @Override
    public void applyTranslation( int[] translationTable )
    {
        super.applyTranslation( translationTable );
        if ( numInputProbs > 0 )
        {
            for ( int i = 0; i < numInputProbs; ++i )
            {
                if ( inputProbsTable[i] < translationTable.length )
                    if ( translationTable[inputProbsTable[i]] >= 0 )
                        inputProbsTable[i] = translationTable[inputProbsTable[i]];
            }

        }

        if ( numOutputProbs > 0 )
        {
            for ( int i = 0; i < numOutputProbs; ++i )
            {
                if ( outputProbsTable[i] < translationTable.length )
                    if ( translationTable[outputProbsTable[i]] >= 0 )
                        outputProbsTable[i] = translationTable[outputProbsTable[i]];
            }
        }
    }



    /*
     *  the following method is called every time a new input link to this module is created
     */
    /**
     *  Description of the Method
     */
    @Override
    public void newInputLinkCreated()
    {
        if ( isEditDialogOn )
            ( (RandomObjectModuleDialog) editBDialog ).update();
    }


    /*
     *  the following method is called every time a new ouput link from this module is created
     */
    /**
     *  Description of the Method
     */
    @Override
    public void newOutputLinkCreated()
    {
        if ( isEditDialogOn )
            ( (RandomObjectModuleDialog) editBDialog ).update();
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
            editBDialog = new RandomObjectModuleDialog( (JFrame) parentFrame.getComponent() );
            isEditDialogOn = true;
        }
    }


    /*
     *  upstream random selection
     */
    /**
     *  Gets the object attribute of the RandomObjectModule object
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
        int index;
        double rand;

        if ( outputPort == 0 )
        {
            col = null;

            if ( numInputProbs > 0 )
            {
                rand = gen.uniformDeviate();
                index = 0;

                while ( rand >= cumulativeInputProbs[index] )
                {
                    ++index;

                    if ( index == numInputProbs )
                    {
                        --index;

                        break;
                    }
                }

                col =  modules.get( inputProbsTable[index] ).getObject( outputPortTable[index], gen.getSeed() );
            }

            if ( col == null )
                return null;

            return col;
        }
        else
            return null;
    }


    /*
     *  downstream random selection
     */
    /**
     *  Gets the object attribute of the RandomObjectModule object
     *
     *@param  collection  Description of the Parameter
     *@param  inputPort   Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The object value
     */
    @Override
    public TapDesignerObjectCollection getObject( TapDesignerObjectCollection collection, int inputPort, long seed )
    {
        ObjectInfo anInfo;
        ObjectInfo mainObject;
        TapRandomGenerator gen = new TapRandomGenerator( seed );
        double rand;
        int index;

        if ( inputPort == 0 )
        {
            mainObject = collection.elementAt( 0 ).objectInfo;

            if ( inputPortLink[0] != null )
            {
                rand = gen.uniformDeviate();
                index = 0;

                while ( rand >= cumulativeOutputProbs[index] )
                {
                    ++index;

                    if ( index == numOutputProbs )
                    {
                        --index;

                        break;
                    }
                }

                TapModule mod = modules.get( linkToIndex[0][index] );
                TapDesignerObjectCollection modCol = mod.getObject( collection, inputPortLink[0][index], gen.getSeed() );

                if ( modCol != null )
                    return modCol;
            }

            return null;
        }
        else
            return null;
    }


    /*
     *  calculates the sum of probability array
     */
    /**
     *  Description of the Method
     *
     *@param  probs  Description of the Parameter
     *@return        Description of the Return Value
     */
    private double sumProbs( double[] probs )
    {
        double sum = 0;

        for ( int i = 0; i < probs.length; ++i )
            sum += probs[i];

        return sum;
    }


    /**
     *  Description of the Method
     */
    @Override
    public void initGenerationProcess()
    {
        checkConsistency();
    }


    /*
     *  checkConsistency ensures that probability arrays reflect the current procedure links state
     */
    /**
     *  Description of the Method
     */
    private void checkConsistency()
    {
        BackModuleLink bl = new BackModuleLink( modules, 0 );
        List<BackLink> back = bl.findAllModules( module, 0 );
        int i;
        double dum;

        if ( back.size() == 0 )
        {
            numInputProbs = 0;
            inputProbs = null;
        }
        else
        {
            if ( inputProbs == null )
            {
                numInputProbs = back.size();
                inputProbs = new double[numInputProbs];
                inputProbsTable = new int[numInputProbs];

                for ( i = 0; i < numInputProbs; ++i )
                {
                    inputProbs[i] = 1.0 / numInputProbs;
                    inputProbsTable[i] = modules.indexOf(  back.get( i ).fromModule );
                }
            }
            else
            {
                numInputProbs = back.size();

                int[] newInputProbsTable = new int[numInputProbs];
                double[] newInputProbs = new double[numInputProbs];
                outputPortTable = new int[numInputProbs];

                for ( i = 0; i < numInputProbs; ++i )
                {
                    newInputProbsTable[i] = modules.indexOf(back.get( i ).fromModule );
                    newInputProbs[i] = 0;
                    outputPortTable[i] = ( (BackLink) back.get( i ) ).outputPort;
                }

                for ( i = 0; i < inputProbs.length; ++i )
                    for ( int j = 0; j < numInputProbs; ++j )
                        if ( newInputProbsTable[j] == inputProbsTable[i] )
                            newInputProbs[j] = inputProbs[i];

                dum = sumProbs( newInputProbs );

                for ( i = 0; i < numInputProbs; ++i )
                    if ( newInputProbs[i] == 0 )
                        newInputProbs[i] = dum / inputProbs.length;

                inputProbs = newInputProbs;
                inputProbsTable = newInputProbsTable;
            }

            cumulativeInputProbs = calculateCumulativeProbs( inputProbs );
        }

        if ( linkToIndex[0] == null )
        {
            numOutputProbs = 0;
            outputProbs = null;
            outputProbsTable = null;
        }
        else
        {
            if ( outputProbs == null )
            {
                numOutputProbs = linkToIndex[0].length;
                outputProbs = new double[numOutputProbs];
                outputProbsTable = new int[numOutputProbs];

                for ( i = 0; i < numOutputProbs; ++i )
                {
                    outputProbs[i] = 1.0 / numOutputProbs;
                    outputProbsTable[i] = linkToIndex[0][i];
                }
            }
            else
            {
                numOutputProbs = linkToIndex[0].length;

                int[] newOutputProbsTable = new int[numOutputProbs];
                double[] newOutputProbs = new double[numOutputProbs];

                for ( i = 0; i < numOutputProbs; ++i )
                {
                    newOutputProbsTable[i] = linkToIndex[0][i];
                    newOutputProbs[i] = 0;
                }

                for ( i = 0; i < outputProbs.length; ++i )
                    for ( int j = 0; j < numOutputProbs; ++j )
                        if ( newOutputProbsTable[j] == outputProbsTable[i] )
                            newOutputProbs[j] = outputProbs[i];

                dum = sumProbs( newOutputProbs );

                for ( i = 0; i < numOutputProbs; ++i )
                    if ( newOutputProbs[i] == 0 )
                        newOutputProbs[i] = dum / outputProbs.length;

                outputProbs = newOutputProbs;
                outputProbsTable = newOutputProbsTable;

            }

            cumulativeOutputProbs = calculateCumulativeProbs( outputProbs );

        }
    }


    /*
     *  cumulative probabilities used for random choice
     */
    /**
     *  Description of the Method
     *
     *@param  probs  Description of the Parameter
     *@return        Description of the Return Value
     */
    private double[] calculateCumulativeProbs( double[] probs )
    {
        double[] cumulativeProbs = new double[probs.length];
        double dum = sumProbs( probs );

        int i;

        for ( i = 0; i < probs.length; ++i )
        {
            if ( i == 0 )
                cumulativeProbs[i] = probs[i] / dum;
            else
                cumulativeProbs[i] = probs[i] / dum + cumulativeProbs[i - 1];
        }

        return cumulativeProbs;
    }


    /*
     *  random module doesn't deliver values
     */
    /**
     *  Gets the value attribute of the RandomObjectModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  var         Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The value value
     */
    @Override
    public double getValue( int outputPort, double[] var, long seed )
    {
        return (double) 0.0;
    }


    /*
     *  not an object module
     */
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


    /*
     *  see showPreviewFrame comment
     */
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


    /*
     *  Random module editor window
     */
    /**
     *  Description of the Class
     *
     *@author     pims
     *@created    19 avril 2004
     */
    private class RandomObjectModuleDialog
             extends BFrame
    {
        private BButton okButton;
        private BButton applyButton;
        private BButton cancelButton;
        private JFrame parentFrame;
        private NumberFormat format;
        private BTable inputTable;
        private BTable outputTable;
        private BTabbedPane tabbedPane;
        private double[] backInputProbs;
        private double[] backOutputProbs;
        private int[] backInputProbsTable;
        private int[] backOutputProbsTable;
        private int backNumInputProbs;
        private int backNumOutputProbs;
        private boolean modified;


        /**
         *  Constructor for the RandomObjectModuleDialog object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public RandomObjectModuleDialog( JFrame parentFrame )
        {
            super( TapBTranslate.text( "randomObjectModuleTitle", name ) );

            int i;
            modified = false;
            format = NumberFormat.getInstance();
            format.setMaximumFractionDigits( 3 );

            checkConsistency();

            backNumInputProbs = numInputProbs;
            backNumOutputProbs = numOutputProbs;
            if ( numInputProbs > 0 )
            {
                backInputProbs = new double[numInputProbs];
                backInputProbsTable = new int[numInputProbs];

                for ( i = 0; i < numInputProbs; ++i )
                {
                    backInputProbs[i] = inputProbs[i];
                    backInputProbsTable[i] = inputProbsTable[i];
                }

            }

            if ( numOutputProbs > 0 )
            {
                backOutputProbs = new double[numOutputProbs];
                backOutputProbsTable = new int[numOutputProbs];

                for ( i = 0; i < numOutputProbs; ++i )
                {
                    backOutputProbs[i] = outputProbs[i];
                    backOutputProbsTable[i] = outputProbsTable[i];
                }
            }

            BorderContainer content = new BorderContainer();
            inputTable = new BTable( numInputProbs > 0 ? numInputProbs : 1, 2 );
            inputTable.setColumnHeader( 0, TapBTranslate.text( "module" ) );
            inputTable.setColumnHeader( 1, TapBTranslate.text( "probability" ) );
            inputTable.setColumnEditable( 1, true );
            outputTable = new BTable( numOutputProbs > 0 ? numOutputProbs : 1, 2 );
            outputTable.setColumnHeader( 0, TapBTranslate.text( "module" ) );
            outputTable.setColumnHeader( 1, TapBTranslate.text( "probability" ) );
            outputTable.setColumnEditable( 1, true );
            update();

            tabbedPane = new BTabbedPane();

            double[] colWeight = {1.0};
            double[] rowWeight = {1.0, 1.0};
            FormContainer fct = new FormContainer( colWeight, rowWeight );
            fct.add( inputTable.getTableHeader(), 0, 0, new LayoutInfo( LayoutInfo.SOUTH, LayoutInfo.BOTH, new Insets( 0, 10, 10, 10 ), new Dimension( 0, 0 ) ) );
            fct.add( inputTable, 0, 1, new LayoutInfo( LayoutInfo.NORTH, LayoutInfo.BOTH, new Insets( 10, 10, 0, 10 ), new Dimension( 0, 0 ) ) );
            tabbedPane.add( fct, TapBTranslate.text( "input" ) );

            fct = new FormContainer( colWeight, rowWeight );
            fct.add( outputTable.getTableHeader(), 0, 0, new LayoutInfo( LayoutInfo.SOUTH, LayoutInfo.BOTH, new Insets( 0, 10, 10, 10 ), new Dimension( 0, 0 ) ) );
            fct.add( outputTable, 0, 1, new LayoutInfo( LayoutInfo.NORTH, LayoutInfo.BOTH, new Insets( 10, 10, 0, 10 ), new Dimension( 0, 0 ) ) );
            tabbedPane.add( fct, TapBTranslate.text( "output" ) );

            okButton = TapBTranslate.bButton( "ok", this, "doOK" );
            cancelButton = TapBTranslate.bButton( "cancel", this, "doCancel" );
            applyButton = TapBTranslate.bButton( "apply", this, "doApplyButton" );

            GridContainer gc = new GridContainer( 3, 1 );
            gc.add( okButton, 0, 0 );
            gc.add( applyButton, 1, 0 );
            gc.add( cancelButton, 2, 0 );
            content.add( tabbedPane, BorderContainer.CENTER );
            content.add( gc, BorderContainer.SOUTH );
            setContent( content );
            ( (JFrame) getComponent() ).setLocationRelativeTo( parentFrame );
            pack();
            setVisible( true );

            addEventLink( WindowClosingEvent.class, this, "doCancel" );
            inputTable.addEventLink( CellValueChangedEvent.class, this, "doCellValueChanged" );
            outputTable.addEventLink( CellValueChangedEvent.class, this, "doCellValueChanged" );

        }


        /**
         *  Description of the Method
         */
        public void update()
        {
            int i;

            checkConsistency();

            String[] inputNames = getInputModulesNames();

            if ( inputNames != null )
                for ( i = 0; i < inputNames.length; ++i )
                {
                    inputTable.setCellValue( i, 0, inputNames[i] );
                    inputTable.setCellValue( i, 1, format.format( inputProbs[i] ) );
                }

            String[] outputNames = getOutputModulesNames();

            if ( outputNames != null )
                for ( i = 0; i < outputNames.length; ++i )
                {
                    outputTable.setCellValue( i, 0, outputNames[i] );
                    outputTable.setCellValue( i, 1, format.format( outputProbs[i] ) );
                }
        }


        /**
         *  Description of the Method
         *
         *@param  evt  Description of the Parameter
         */
        private void doCellValueChanged( CellValueChangedEvent evt )
        {

            BTable table = (BTable) evt.getWidget();
            double[] probs = table == inputTable ? inputProbs : outputProbs;
            int col = evt.getColumn();
            int row = evt.getRow();

            String val = (String) ( table.getCellValue( row, col ) );
            double dum;
            modified = true;
            try
            {
                dum = Double.parseDouble( val.trim().replace( ',', '.' ) );
                ( (JTable) table.getComponent() ).setGridColor( Color.black );
            }
            catch ( NumberFormatException ex )
            {
                ( (JTable) table.getComponent() ).setGridColor( Color.red );
            }
        }


        /**
         *  Gets the inputModulesNames attribute of the RandomObjectModuleDialog
         *  object
         *
         *@return    The inputModulesNames value
         */
        private String[] getInputModulesNames()
        {
            if ( numInputProbs == 0 )
                return null;

            String[] names = new String[numInputProbs];

            for ( int i = 0; i < numInputProbs; ++i )
                names[i] = modules.get( inputProbsTable[i] ).getName();

            return names;
        }


        /**
         *  Gets the outputModulesNames attribute of the
         *  RandomObjectModuleDialog object
         *
         *@return    The outputModulesNames value
         */
        private String[] getOutputModulesNames()
        {
            if ( numOutputProbs == 0 )
                return null;

            String[] names = new String[numOutputProbs];

            for ( int i = 0; i < numOutputProbs; ++i )
                names[i] = modules.get( outputProbsTable[i] ).getName();

            return names;
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
         *  Gets the tableValues attribute of the RandomObjectModuleDialog
         *  object
         *
         *@param  table  Description of the Parameter
         *@param  probs  Description of the Parameter
         */
        private void getTableValues( BTable table, double[] probs )
        {
            int i;
            String val;

            for ( i = 0; i < probs.length; ++i )
            {
                try
                {
                    val = (String) ( table.getCellValue( i, 1 ) );
                    probs[i] = Double.parseDouble( val.trim().replace( ',', '.' ) );
                }
                catch ( NumberFormatException ex )
                {
                    probs[i] = 0;
                }
            }
        }


        /**
         *  Gets the values attribute of the RandomObjectModuleDialog object
         *
         *@return    The values value
         */
        private boolean getValues()
        {
            boolean editing = ( (JTable) inputTable.getComponent() ).isEditing();
            if ( editing )
            {
                int r = JOptionPane.showConfirmDialog( (JFrame) ( this.getComponent() ), TapBTranslate.text( "inputCellBeingEdited" ), TapBTranslate.text( "warning" ), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION );

                if ( r == JOptionPane.YES_OPTION )
                    editing = false;

            }
            if ( editing )
                return false;
            editing = ( (JTable) outputTable.getComponent() ).isEditing();
            if ( editing )
            {
                int r = JOptionPane.showConfirmDialog( (JFrame) ( this.getComponent() ), TapBTranslate.text( "outputCellBeingEdited" ), TapBTranslate.text( "warning" ), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION );

                if ( r == JOptionPane.YES_OPTION )
                    editing = false;

            }
            if ( editing )
                return false;
            else
            {
                if ( numInputProbs > 0 )
                    getTableValues( inputTable, inputProbs );
                if ( numOutputProbs > 0 )
                    getTableValues( outputTable, outputProbs );
                return true;
            }
        }


        /**
         *  Gets the backValues attribute of the RandomObjectModuleDialog object
         */
        private void getBackValues()
        {
            numInputProbs = backNumInputProbs;
            numOutputProbs = backNumOutputProbs;
            inputProbs = backInputProbs;
            outputProbs = backOutputProbs;
            inputProbsTable = backInputProbsTable;
            outputProbsTable = backOutputProbsTable;
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
                    //redo the changes
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

