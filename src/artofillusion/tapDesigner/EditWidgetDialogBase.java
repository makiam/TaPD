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

//{{{ imports
import artofillusion.*;
import buoy.event.*;
import buoy.widget.*;
import artofillusion.tapDesigner.TapModule.*;

//}}}

/**
 *  Edit widget dialog base
 *
 *@author     Francois Guillet
 *@created    19 mai 2004
 */
public class EditWidgetDialogBase
         extends BFrame implements EditWidgetDialog
{
    private EditWidgetBase ew;
    private TapModule module;


    /**
     *  Constructor for the EditWidgetDialogBase object
     *
     *@param  parentFrame  Description of the Parameter
     *@param  module       Description of the Parameter
     */
    public EditWidgetDialogBase( BFrame parentFrame, TapModule module )
    {
        super( "" );
        this.module = module;
        setTitle( module.getName() );

        setContent( ew = (EditWidgetBase) module.getEditWidget( 0,
            new Runnable()
            {
                public void run()
                {
                    doRunnableUpdate();
                }
            }, true ) );
        pack();
        TapUtils.centerAndSizeWindow( this );
        setVisible( true );
        addEventLink( WindowClosingEvent.class, ew, "doRevert" );
        addEventLink( WindowClosingEvent.class, this, "doCancel" );
        ew.getValidateButton().addEventLink( CommandEvent.class, this, "doClose" );
        ew.getRevertButton().addEventLink( CommandEvent.class, this, "doCancel" );
        ew.showValues( true );
    }


    /**
     *  Called when the edit widget validates changes.
     */
    private void doRunnableUpdate()
    {
        //module.getProcedure().getObject().getPreviewMesh();
        module.getProcedure().getProcPanel().minorViewSync( null );
        layoutChildren();
        repaint();
    }


    /**
     *  Description of the Method
     */
    public void doCancel()
    {
        if ( ew.doRevert() )
        {
            dispose();
            module.editDialogClosed();
        }
    }


    /**
     *  Description of the Method
     */
    public void doClose()
    {
        if ( ew.doRevert() )
        {
            dispose();
            module.editDialogClosed();
        }
    }


    /**
     *  Description of the Method
     *
     *@param  force  Description of the Parameter
     */
    public void showValues( boolean force )
    {
        ew.showValues( force );
    }

}

