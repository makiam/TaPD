/*
 *  This class represents an constant value module
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

import artofillusion.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;

import artofillusion.tapDesigner.TapModule.*;


/**
 *  Description of the Class
 *
 *@author     pims
 *@created    19 avril 2004
 */
public class ConstantValueModule
         extends TapModule
{
    private double constantValue;
    private ConstantValueModule module;

    private static TapModule.ModuleTypeInfo typeInfo;


    //private NumberFormat form;
    /**
     *  Constructor for the ConstantValueModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public ConstantValueModule( TapProcedure procedure, Point position )
    {
        super( procedure, TapDesignerTranslate.text( "value" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "constantValueName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/value_tree.png" ) ) );

        constantValue = (double) 0.0;
        setName( String.valueOf( constantValue ) );
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
        outputTooltips[0] = TapDesignerTranslate.text( "valueOutput" );
        setBackgroundColor( Color.black );
        module = this;
    }


    /**
     *  Constructor for the ConstantValueModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public ConstantValueModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        constantValue = in.readDouble();
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
        out.writeDouble( constantValue );
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public TapModule duplicate()
    {
        ConstantValueModule module = new ConstantValueModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
        module.constantValue = this.constantValue;

        return (TapModule) module;
    }


    /**
     *  Gets the value attribute of the ConstantValueModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  var         Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The value value
     */
    public double getValue( int outputPort, double[] var, long seed )
    {
        if ( outputPort == 0 )
            return constantValue;
        else
        {
            System.out.println( "wrong output port for constant value : " + outputPort );

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
     *@param  parentFrame  Description of the Parameter
     */
    public void edit( BFrame parentFrame )
    {
        super.edit( parentFrame );

        if ( isEditDialogOn )
            editDialog.toFront();
        else
        {
            editDialog = new ConstantValueModuleDialog( (JFrame) parentFrame.getComponent() );
            isEditDialogOn = true;
        }
    }


    /**
     *  Gets the moduleTypeInfo attribute of the ConstantValueModule object
     *
     *@return    The moduleTypeInfo value
     */
    public ModuleTypeInfo getModuleTypeInfo()
    {
        return typeInfo;
    }


    /**
     *  ConstantValueModule editor window
     *
     *@author     Francois Guillet
     *@created    19 avril 2004
     */
    private class ConstantValueModuleDialog
             extends JFrame
             implements ActionListener,
            DocumentListener
    {
        private JButton okButton;
        private JButton applyButton;
        private JButton cancelButton;
        private double backupValue;
        private boolean modified;
        private JTextField textField;
        JFrame parentFrame;
        NumberFormat format;


        /**
         *  Constructor for the ConstantValueModuleDialog object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public ConstantValueModuleDialog( JFrame parentFrame )
        {
            super( TapBTranslate.text( "constantValueModuleTitle", name ) );
            this.parentFrame = parentFrame;
            backupValue = constantValue;

            Container contentPane = this.getContentPane();
            GridBagConstraints gc;
            GridBagLayout gridbag = new GridBagLayout();
            contentPane.setLayout( gridbag );

            gc = new java.awt.GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 0;
            gc.gridwidth = 2;
            gc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gc.insets = new java.awt.Insets( 5, 5, 5, 5 );

            JLabel tmpLabel = TapDesignerTranslate.jlabel( "enterValue" );
            contentPane.add( tmpLabel, gc );

            textField = new JTextField( String.valueOf( backupValue ) );
            gc = new java.awt.GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 1;
            gc.gridwidth = 3;
            gc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gc.insets = new java.awt.Insets( 5, 5, 5, 5 );
            contentPane.add( textField, gc );
            textField.getDocument().addDocumentListener( this );

            gc = new java.awt.GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 2;
            gc.insets = new java.awt.Insets( 5, 5, 5, 5 );
            okButton = TapDesignerTranslate.jButton( "ok", this );
            contentPane.add( okButton, gc );

            gc = new java.awt.GridBagConstraints();
            gc.gridx = 1;
            gc.gridy = 2;
            gc.insets = new java.awt.Insets( 5, 5, 5, 5 );
            applyButton = TapDesignerTranslate.jButton( "apply", this );
            contentPane.add( applyButton, gc );

            gc = new java.awt.GridBagConstraints();
            gc.gridx = 2;
            gc.gridy = 2;
            gc.insets = new java.awt.Insets( 5, 5, 5, 5 );
            cancelButton = TapDesignerTranslate.jButton( "cancel", this );
            contentPane.add( cancelButton, gc );

            this.setTitle( TapDesignerTranslate.text( "constantValueModuleTitle", module.getName() ) );
            addWindowListener(
                new java.awt.event.WindowAdapter()
                {
                    public void windowClosing( java.awt.event.WindowEvent evt )
                    {
                        exitForm( evt );
                    }
                } );
            setResizable( false );
            setLocationRelativeTo( parentFrame );
            pack();
            setVisible( true );
            modified = false;
            format = NumberFormat.getInstance();
            format.setMaximumFractionDigits( 3 );
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
                try
                {
                    constantValue = Double.parseDouble( textField.getText().trim().replace( ',', '.' ) );
                    updateVisualModuleName();
                    setTitle( TapBTranslate.text( "constantValueModuleTitle", name ) );
                    doApply();
                }
                catch ( NumberFormatException e )
                {
                    constantValue = backupValue;
                    JOptionPane.showMessageDialog( null, TapDesignerTranslate.text( "nonValueMessage" ), TapDesignerTranslate.text( "error" ), JOptionPane.ERROR_MESSAGE );
                }
            }
            else if ( command.equals( okButton.getActionCommand() ) )
            {
                try
                {
                    constantValue = Double.parseDouble( textField.getText().trim().replace( ',', '.' ) );
                    if ( modified )
                    {
                        //put back the module in initial state for undo record
                        constantValue = backupValue;
                        setName( getValueString() );
                        procedure.addUndoRecord();
                        //redo the changes
                        constantValue = Double.parseDouble( textField.getText().trim().replace( ',', '.' ) );
                    }
                    doApply();
                    updateVisualModuleName();
                    editDialogClosed();

                    //System.out.println("Disposed");
                }
                catch ( NumberFormatException e )
                {
                    constantValue = backupValue;
                    updateVisualModuleName();
                    JOptionPane.showMessageDialog( null, TapDesignerTranslate.text( "nonValueMessage" ), TapDesignerTranslate.text( "error" ), JOptionPane.ERROR_MESSAGE );
                }
            }
        }


        /**
         *  Description of the Method
         */
        private void updateVisualModuleName()
        {
            module.setName( getValueString() );
            module.getProcedure().getProcPanel().minorViewSync( null );
        }


        /**
         *  Description of the Method
         */
        public void doLiveCheck()
        {
            modified = true;

            try
            {
                double dum = Double.parseDouble( textField.getText().trim().replace( ',', '.' ) );
                textField.setForeground( Color.black );
            }
            catch ( NumberFormatException e )
            {
                textField.setForeground( Color.red );
            }
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void changedUpdate( DocumentEvent e )
        {
            doLiveCheck();
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void insertUpdate( DocumentEvent e )
        {
            doLiveCheck();
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void removeUpdate( DocumentEvent e )
        {
            doLiveCheck();
        }


        /**
         *  Gets the valueString attribute of the ConstantValueModuleDialog
         *  object
         *
         *@return    The valueString value
         */
        private String getValueString()
        {
            return format.format( constantValue );
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
                constantValue = backupValue;
                editDialogClosed();
            }

            updateVisualModuleName();
        }
    }
}

