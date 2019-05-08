/*
 *  This frame manages imported objects from AoI
 */
/*
 *  Copyright (C) 2003 by Francois Guillet
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


import javax.swing.*;


/**
 *  Description of the Class
 *
 *@author     pims
 *@created    14 mars 2004
 */
public class TapObjectsFrame extends BDialog
{
    BList list;
    Scene theScene;
    TapProcedure procedure;


    /**
     *  Constructor for the TapObjectsFrame object
     *
     *@param  parentFrame  Description of the Parameter
     *@param  procedure    Description of the Parameter
     */
    public TapObjectsFrame( BFrame parentFrame, TapProcedure procedure )
    {
        super( parentFrame, TapBTranslate.text( "importedObjectsList" ), true );

        int i;

        theScene = procedure.getScene();
        this.procedure = procedure;

        BorderContainer border = new BorderContainer();
        setContent( border );

        BButton deleteButton = TapBTranslate.bButton( "delete", this, "doDelete" );

        if ( getObjectNames() != null )
        {
            list = new BList( getObjectNames() );
        }
        else
        {
            list = new BList();
        }

        BScrollPane scrollPane = new BScrollPane( list );

        LayoutInfo layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 5, 5, 5, 5 ), new Dimension( 0, 0 ) );
        LayoutInfo listLayout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.BOTH, new Insets( 5, 5, 5, 5 ), new Dimension( 0, 0 ) );
        FormContainer fc = new FormContainer( 1, 4 );
        fc.add( TapBTranslate.bLabel( "deleteObjects" ), 0, 0, layout );
        fc.add( scrollPane, 0, 1, listLayout );
        fc.add( deleteButton, 0, 2, layout );

        BButton quitButton = TapBTranslate.bButton( "quit", this, "doQuit" );
        fc.add( quitButton, 0, 3, layout );
        border.add( fc, BorderContainer.CENTER );

        pack();

        ( (JDialog) getComponent() ).setLocationRelativeTo( parentFrame.getComponent() );

        Dimension size = getComponent().getSize();
        size.width = (int) ( size.width * 1.1 );
        size.height = (int) ( size.height * 1.1 );
        getComponent().setSize( size );
        setVisible( true );

        addEventLink( WindowClosingEvent.class, this, "doQuit" );
    }


    /**
     *  Description of the Method
     */
    private void doQuit()
    {
        dispose();
    }


    /**
     *  Gets the objectNames attribute of the TapObjectsFrame object
     *
     *@return    The objectNames value
     */
    private Object[] getObjectNames()
    {
        int numObjects = procedure.getNumObjects();

        if ( numObjects == 0 )
        {
            return null;
        }

        Object[] names = new Object[numObjects];

        for ( int i = 0; i < numObjects; ++i )
        {
            names[i] = (Object) theScene.getObject( i ).name;
        }

        return names;
    }


    /**
     *  Description of the Method
     */
    private void doDelete()
    {
        int[] selection = null;

        if ( list.getSelectedIndex() >= 0 )
        {
            selection = list.getSelectedIndices();

            for ( int i = selection.length - 1; i >= 0; --i )
            {
                procedure.deleteObject( selection[i] );
            }

            if ( getObjectNames() != null )
            {
                list.setContents( getObjectNames() );
            }
            else
            {
                list.removeAll();
            }

            list.clearSelection();
        }
    }
}

