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

import buoy.widget.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;


/**
 *  EditWidget base class. This class is provided for convenience, and modules
 *  can use it for their edit widgets. It is however not mandatory to derive
 *  this class : only implementing the EditWidget interface is mandatory.
 *
 *@author     Francois Guillet
 *@created    14 mai 2004
 */
public abstract class EditWidgetBase extends BorderContainer implements EditWidget
{
    /**
     *  Description of the Field
     */
    protected BButton validateButton;
    /**
     *  Description of the Field
     */
    protected BButton testButton;
    /**
     *  Description of the Field
     */
    protected BButton revertButton;
    /**
     *  Description of the Field
     */
    protected Runnable runnable;
    /**
     *  Description of the Field
     */
    protected boolean standalone;
    /**
     *  Description of the Field
     */
    private String labelString;

    /**
     *  Description of the Field
     */
    protected TapModule module;
    /**
     *  Description of the Field
     */
    protected boolean disposeOK;
    private LayoutInfo borderLayout;
    /**
     *  Description of the Field
     */
    protected boolean modified;



    /**
     *  Constructor for the EditWidgetBase object.
     *
     *@param  cb           Description of the Parameter
     *@param  standalone   Description of the Parameter
     *@param  labelString  Description of the Parameter
     *@param  module       Description of the Parameter
     */
    public EditWidgetBase( Runnable cb, boolean standalone, String labelString, TapModule module )
    {
        LayoutInfo borderLayout = new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 5, 5, 5, 5 ), new Dimension( 0, 0 ) );
        this.standalone = standalone;
        this.labelString = labelString;
        this.module = module;
        modified = false;
        if ( !standalone )
            add( new BLabel( TapBTranslate.text( labelString, module.getName() ) ), BorderContainer.NORTH, borderLayout );

        int i;

        runnable = cb;
        modified = false;
        initBackValues();

        LayoutInfo buttonLayout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 5, 5, 5, 5 ), new Dimension( 0, 0 ) );
        if ( standalone )
        {
            validateButton = TapBTranslate.bButton( "ok", this, "doValidate" );
            revertButton = TapBTranslate.bButton( "cancel", this, "doRevert" );
            testButton = TapBTranslate.bButton( "apply", this, "doTest" );
        }
        else
        {
            validateButton = TapBTranslate.bButton( "validate", this, "doValidate" );
            revertButton = TapBTranslate.bButton( "revert", this, "doRevert" );
            testButton = TapBTranslate.bButton( "test", this, "doTest" );
        }
        GridContainer gc = new GridContainer( 3, 1 );
        gc.add( validateButton, 0, 0, buttonLayout );
        gc.add( testButton, 1, 0, buttonLayout );
        gc.add( revertButton, 2, 0, buttonLayout );
        add( gc, BorderContainer.SOUTH );
        initButtons();
    }


    /**
     *  Sets up the initial state of buttons (enabled/disabled).
     */
    protected void initButtons()
    {
        if ( !modified )
        {
            testButton.setEnabled( false );
            if ( module.testMode )
                validateButton.setEnabled( true );
            else
                validateButton.setEnabled( false );
            if ( !standalone )
            {
                if ( module.testMode )
                    revertButton.setEnabled( true );
                else
                    revertButton.setEnabled( false );
            }
        }
        else
        {
            validateButton.setEnabled( true );
            testButton.setEnabled( true );
            if ( !standalone )
            {
                revertButton.setEnabled( true );
            }
        }

    }


    /**
     *  Gets the validate button of the EditWidgetBase object
     *
     *@return    The validate button value
     */
    public BButton getValidateButton()
    {
        return validateButton;
    }


    /**
     *  Gets the revert button of the EditWidgetBase object
     *
     *@return    The revert button value
     */
    public BButton getRevertButton()
    {
        return revertButton;
    }


    /**
     *  Fetch the values currently displayed object
     */
    protected abstract void getValues();


    /**
     *  Initializes backup values
     */
    protected abstract void initBackValues();


    /**
     *  Gets the backup values and validates them
     */
    protected void getBackValues()
    {
        getUndoValues();
        module.getProcedure().getProcPanel().minorViewSync( null );
    }


    /**
     *  Gets the backup values for undo purposes, the values are not validated.
     */
    protected abstract void getUndoValues();


    /**
     *  Reload displayed values from module
     *
     *@param  force  Description of the Parameter
     */
    public void showValues( boolean force )
    {
        if ( force || module.changed )
        {
            modified = false;
            initButtons();
            if ( !standalone )
            {
                add( new BLabel( TapBTranslate.text( labelString, module.getName() ) ), BorderContainer.NORTH, borderLayout );
            }
            else
            {
                if ( TapUtils.getParentBFrame( this ) != null )
                    TapUtils.getParentBFrame( this ).setTitle( TapBTranslate.text( labelString, module.getName() ) );
            }
        }
    }


    /**
     *  Updates the top label
     */
    protected void updateNameLabel()
    {
        if ( !standalone )
        {
            BLabel label = (BLabel) getChild( BorderContainer.NORTH );
            label.setText( TapBTranslate.text( labelString, module.getName() ) );
            //layoutChildren();
        }
        else
        {
            TapUtils.getParentBFrame( this ).setTitle( TapBTranslate.text( labelString, module.getName() ) );
        }
    }


    /**
     *  This method has to be called when a parameter has been modified by the
     *  user. It enables Validate and Test buttons
     */
    protected void doModified()
    {
        modified = true;
        initButtons();
    }


    /**
     *  Description of the Method
     */
    public void doValidate()
    {
        if ( modified || module.testMode )
        {
            //put back the module in initial state for undo record
            pushValues();
            getUndoValues();
            module.getProcedure().addUndoRecord();
            popValues();
        }
        getValues();
        module.changed = true;
        module.doApply();
        module.testMode = false;
        updateNameLabel();
        runnable.run();
        initBackValues();
        initButtons();
    }


    /**
     *  Description of the Method
     */
    public void pushValues()
    {

    }


    /**
     *  Description of the Method
     */
    public void popValues()
    {

    }


    /**
     *  Allows the user to test the values currently entered
     */
    public void doTest()
    {
        getValues();
        module.changed = true;
        module.testMode = true;
        updateNameLabel();
        module.doApply();
        runnable.run();
        modified = false;
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


    /**
     *  Description of the Method
     */
    public void doClose()
    {
        Vector modules = module.getProcedure().getModules();
        if ( modules == null )
            return;
        for ( int i = 0; i < modules.size(); ++i )
        {
            if ( (TapModule) modules.elementAt( i ) == module )
                getBackValues();
        }
    }


    /**
     *  Reverts to previous settings
     *
     *@return    Description of the Return Value
     */
    public boolean doRevert()
    {
        if ( modified || module.testMode )
        {
            int r = JOptionPane.showConfirmDialog( null, TapBTranslate.text( "parametersModified" ), TapBTranslate.text( "warning" ), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION );

            if ( r == JOptionPane.YES_OPTION )
            {
                module.changed = true;
                modified = false;
                module.testMode = false;
                getBackValues();
                showValues( true );
                runnable.run();
                return true;
            }
            else
            {
                return false;
            }
        }
        return true;
    }
}

