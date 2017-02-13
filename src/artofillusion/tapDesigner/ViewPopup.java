/*
 *  The popup menu that appears on any view to split it and add a new view, or to remove a view.
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

//{{{ imports
import buoy.event.*;
import buoy.widget.*;
import javax.swing.*;

//}}}

/**
 *  The popup menu that appears on any view to split it and add a new view.
 *
 *@author     François Guillet
 *@created    1 mai 2004
 */
public class ViewPopup
         extends BPopupMenu
{
    private TapView view;
    private TapProcPanel procPanel;


    /**
     *  Constructor for the ViewPopup object
     *
     *@param  view  Description of the Parameter
     */
    public ViewPopup( TapView view )
    {
        super();
        this.view = view;
        procPanel = view.getProcPanel();
        BMenu tmp2 = TapBTranslate.bMenu( "addHSplit" );
        tmp2.add( TapBTranslate.bMenuItem( "modulesView", this, "doAddHSplitModuleView" ) );
        tmp2.add( TapBTranslate.bMenuItem( "parametersView", this, "doAddHSplitParamView" ) );
        tmp2.add( TapBTranslate.bMenuItem( "previewView", this, "doAddHSplitPreviewView" ) );
        add( tmp2 );
        tmp2 = TapBTranslate.bMenu( "addVSplit" );
        tmp2.add( TapBTranslate.bMenuItem( "modulesView", this, "doAddVSplitModuleView" ) );
        tmp2.add( TapBTranslate.bMenuItem( "parametersView", this, "doAddHSplitParamView" ) );
        tmp2.add( TapBTranslate.bMenuItem( "previewView", this, "doAddHSplitPreviewView" ) );
        add( tmp2 );
        addSeparator();
        add( TapBTranslate.bMenuItem( "removeView", this, "doRemoveView" ) );
    }


    /**
     *  Description of the Method
     */
    public void doAddHSplitModuleView()
    {
        addView( new TapModulePanel( procPanel ), true );

    }


    /**
     *  Description of the Method
     */
    public void doAddHSplitParamView()
    {
        addView( new TapParametersPanel( procPanel ), true );
    }


    /**
     *  Description of the Method
     */
    public void doAddHSplitPreviewView()
    {
        addView( new PreviewTapView( procPanel ), true );
    }


    /**
     *  Description of the Method
     */
    public void doAddVSplitModuleView()
    {
        addView( new TapModulePanel( procPanel ), false );
    }


    /**
     *  Description of the Method
     */
    public void doAddVSplitParamView()
    {
        addView( new TapParametersPanel( procPanel ), false );

    }


    /**
     *  Description of the Method
     */
    public void doAddVSplitPreviewView()
    {
        addView( new PreviewTapView( procPanel ), false );

    }


    /**
     *  Adds a new widget in a new splitpane.
     *
     *@param  newView     The new view to add
     *@param  horizontal  Whether the new split pane is horizontal (true) or
     *      vertical (false).
     */
    public void addView( TapView newView, boolean horizontal )
    {
        Widget newWidget = null;
        if ( newView.needsScrollPane() )
        {
            ( (Widget) newView ).addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
            ( (Widget) newView ).addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
            newWidget = (Widget) newView.newScrollPane();
        }
        else
            newWidget = (Widget) newView;
        newWidget.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        newWidget.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        Widget w = (Widget) view;
        WidgetContainer wc = (WidgetContainer) w.getParent();
        if ( wc instanceof BScrollPane )
        {
            w = wc;
            wc = (WidgetContainer) wc.getParent();
        }
        if ( wc instanceof BorderContainer )
        {
            BorderContainer bc = (BorderContainer) wc;
            bc.remove( w );
            BSplitPane newSplit = new BSplitPane( horizontal ? BSplitPane.HORIZONTAL : BSplitPane.VERTICAL,
                    w, newWidget );
            newSplit.setOneTouchExpandable( true );
            newSplit.setResizeWeight( 0.5 );
            newSplit.setDividerLocation( 0.5 );
            bc.add( newSplit, BorderContainer.CENTER );
            procPanel.register( newView );
            procPanel.layoutChildren();
        }
        else if ( wc instanceof BSplitPane )
        {
            BSplitPane curSplit = (BSplitPane) wc;
            int loc = curSplit.getDividerLocation();
            int size = ( curSplit.getOrientation() == BSplitPane.HORIZONTAL ?
                    curSplit.getComponent().getSize().width : curSplit.getComponent().getSize().height );
            double location = 0;
            if ( size != 0 )
                location = ( (double) loc ) / ( (double) size );
            int index = 0;
            if ( curSplit.getChild( 1 ) == w )
                index = 1;
            BSplitPane newSplit = new BSplitPane( horizontal ? BSplitPane.HORIZONTAL : BSplitPane.VERTICAL,
                    w, newWidget );
            newSplit.setOneTouchExpandable( true );
            newSplit.setResizeWeight( 0.5 );
            newSplit.setDividerLocation( 0.5 );
            curSplit.add( newSplit, index );
            curSplit.setDividerLocation( location );
            procPanel.register( newView );
            procPanel.layoutChildren();
        }
        procPanel.getProcedure().notifyMinorChange();
    }


    /**
     *  Description of the Method
     */
    public void doRemoveView()
    {

        Widget w = (Widget) view;
        procPanel.removeView( view );
        WidgetContainer wc = (WidgetContainer) w.getParent();
        if ( wc instanceof BScrollPane )
        {
            w = wc;
            wc = (WidgetContainer) wc.getParent();
        }
        if ( wc instanceof BorderContainer )
        {
            procPanel.doRemoveTab();
        }
        else if ( wc instanceof BSplitPane )
        {
            WidgetContainer wc2 = wc.getParent();
            BSplitPane split = (BSplitPane) wc;
            int index = 0;
            if ( split.getChild( 1 ) == w )
                index = 1;
            if ( wc2 instanceof BorderContainer )
            {
                wc2.remove( wc );
                ( (BorderContainer) wc2 ).add( split.getChild( 1 - index ), BorderContainer.CENTER );
                procPanel.layoutChildren();
            }
            else if ( wc2 instanceof BSplitPane )
            {

                BSplitPane curSplit = (BSplitPane) wc2;
                int curIndex = 0;
                if ( curSplit.getChild( 1 ) == wc )
                    curIndex = 1;
                int loc = curSplit.getDividerLocation();
                int size = ( curSplit.getOrientation() == BSplitPane.HORIZONTAL ?
                        curSplit.getComponent().getSize().width : curSplit.getComponent().getSize().height );
                double location = 0;
                if ( size != 0 )
                    location = ( (double) loc ) / ( (double) size );
                curSplit.add( split.getChild( 1 - index ), curIndex );
                procPanel.layoutChildren();
                curSplit.setDividerLocation( location );

            }

        }
        procPanel.getProcedure().notifyMinorChange();

    }
}

