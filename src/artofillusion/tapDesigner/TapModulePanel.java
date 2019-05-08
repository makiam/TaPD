/*
 *  This class is responsible for displaying the modules and links. It extends OverlayContainer in
 *  order to place the visual modules in one container and the links in another container
 *  on top of the first one.
 */
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


import artofillusion.math.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;


/**
 *  This panel shows a graphical representation of the procedure (modules and
 *  links).
 *
 *@author     Francois Guillet
 *@created    14 mars 2004
 */
public class TapModulePanel extends OverlayContainer implements TapView
{
    //{{{ variables
    private ModuleContainer moduleContainer;
    private CustomWidget linkContainer;
    private boolean draggingBoxState;
    private int dragX1;
    private int dragX2;
    private int dragY1;
    private int dragY2;
    private Point linePoint1;
    private Point linePoint2;
    private Point[] linksLinesFrom;
    private Point[] linksLinesTo;
    int[] selectedLinks;
    private Vector visualModules;
    private int movingVM;
    private Point movingVMLocation;
    private Point magneticLocation;
    private boolean magneticLine;
    private int fromVisualModule;
    private int toVisualModule;
    private int fromPortIndex;
    private int toPortIndex;
    private int fromPortNature;
    private boolean fromPortIsOutput;
    private BPopupMenu popup;
    private BPopupMenu viewPopup;
    private TapProcPanelHolder holder;
    private Vector clipboardModules;
    private boolean popupOn = false;
    private boolean init;
    private int popupClicked = -1;
    private TapProcPanel procPanel;
    private TapProcedure procedure;



//}}}

    //{{{  constructor
    /**
     *  Constructor for the TapProcPanel object
     *
     *@param  procPanel  Description of the Parameter
     */
    public TapModulePanel( TapProcPanel procPanel )
    {
        super();
        this.procPanel = procPanel;
        draggingBoxState = false;
        linePoint1 = null;
        linePoint2 = null;

        moduleContainer = new ModuleContainer();
        linkContainer = new CustomWidget();
        linkContainer.setOpaque( false );
        add( moduleContainer );
        add( linkContainer );

        moduleContainer.addEventLink( MouseClickedEvent.class, this, "doWidgetMouseClicked" );
        moduleContainer.addEventLink( MousePressedEvent.class, this, "doWidgetMousePressed" );
        moduleContainer.addEventLink( MouseReleasedEvent.class, this, "doWidgetMouseReleased" );
        moduleContainer.addEventLink( MouseDraggedEvent.class, this, "doWidgetMouseDragged" );
        moduleContainer.addEventLink( KeyPressedEvent.class, this, "doKeyPressed" );
        moduleContainer.addEventLink( KeyReleasedEvent.class, this, "doKeyReleased" );
        linkContainer.addEventLink( RepaintEvent.class, this, "doLinkRepaintEvent" );

        popup = new BPopupMenu();
        popup.add( TapBTranslate.bMenuItem( "rename", this, "popupRename" ) );
        popup.add( TapBTranslate.bMenuItem( "mainEntry", this, "popupMainEntry" ) );
        popup.add( TapBTranslate.bMenuItem( "seed", this, "doChooseSeed" ) );
        popup.addSeparator();
        popup.add( TapBTranslate.bMenuItem( "left_to_right", this, "popupLeftToRight" ) );
        popup.add( TapBTranslate.bMenuItem( "top_to_bottom", this, "popupTopToBottom" ) );
        popup.add( TapBTranslate.bMenuItem( "right_to_left", this, "popupRightToLeft" ) );
        popup.add( TapBTranslate.bMenuItem( "bottom_to_top", this, "popupBottomToTop" ) );
        viewPopup = new ViewPopup( this );
        moduleContainer.addEventLink( WidgetMouseEvent.class, this, "doShowViewPopup" );

        this.procedure = procedure;
        //initialize();

    }


    /**
     *  Gets the minimumSize attribute of the TapModulePanel object
     *
     *@return    The minimumSize value
     */
    /*
     *  public Dimension getMinimumSize()
     *  {
     *  return new Dimension( 0, 0 );
     *  }
     */
    /**
     *  Gets the procPanel attribute of the TapModulePanel object
     *
     *@return    The procPanel value
     */
    public TapProcPanel getProcPanel()
    {
        return procPanel;
    }


    //}}}

    //{{{ initialization
    /**
     *  Initialization. Call this method each time the procedure attached to the
     *  proc panel has changed.
     */
    public void initialize()
    {
        procedure = procPanel.getProcedure();
        visualModules = new Vector();
        clipboardModules = new Vector();
        moduleContainer.removeAll();
        movingVM = -1;
        fromPortIndex = -1;
        magneticLine = false;
        linksLinesFrom = null;
        linksLinesTo = null;
        selectedLinks = null;
        init = true;

        Vector modules = procedure.getModules();

        if ( modules.size() > 0 )
        {
            for ( int i = 0; i < modules.size(); ++i )
            {
                ( (TapModule) modules.elementAt( i ) ).setProcedure( procedure );
                addModule( (TapModule) modules.elementAt( i ) );
            }

            buildModuleLinks();
        }

        init = false;
        layoutChildren();
        repaint();
    }


    //}}}

    //{{{ Events
    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    protected void doKeyPressed( KeyPressedEvent ev )
    {
        int code = ev.getKeyCode();

        if ( code == KeyEvent.VK_BACK_SPACE || code == KeyEvent.VK_DELETE )
        {
            doClear();
        }
        else if ( code == KeyEvent.VK_CONTROL )
        {
        }
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    protected void doKeyReleased( KeyReleasedEvent ev )
    {
        int code = ev.getKeyCode();
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    private void doWidgetMouseClicked( MouseClickedEvent ev )
    {
        procPanel.setActiveModulePanel( this );
        Point where = ev.getPoint();

        if ( linksLinesFrom != null )
        {
            for ( int i = 0; i < linksLinesFrom.length; ++i )
            {
                //code taken from ProcedureEditor.java written by P. Eastman

                Point p1 = linksLinesFrom[i];
                Point p2 = linksLinesTo[i];
                int tol = 2;

                if ( where.x < p1.x - tol && where.x < p2.x - tol )
                {
                    continue;
                }

                if ( where.x > p1.x + tol && where.x > p2.x + tol )
                {
                    continue;
                }

                if ( where.y < p1.y - tol && where.y < p2.y - tol )
                {
                    continue;
                }

                if ( where.y > p1.y + tol && where.y > p2.y + tol )
                {
                    continue;
                }

                Vec2 v1 = new Vec2( p2.x - p1.x, p2.y - p1.y );
                Vec2 v2 = new Vec2( where.x - p1.x, where.y - p1.y );
                v1.normalize();

                double dot = v1.dot( v2 );
                v1.scale( dot );
                v2.subtract( v1 );

                if ( v2.length2() <= tol * tol )
                {
                    if ( !ev.isShiftDown() )
                    {
                        unselectAllModules();
                    }

                    selectedLinks = TapUtils.increaseIntArray( selectedLinks );
                    selectedLinks[selectedLinks.length - 1] = i;
                    doSelectionChanged();
                    repaint();
                    procPanel.minorViewSync( this );
                    return;
                }
            }
        }

        /*
         *  if ( !ev.isShiftDown() )
         *  {
         *  unselectAllModules();
         *  selectedLinks = null;
         *  }
         */
        repaint();
        movingVM = -1;
        fromPortIndex = -1;
        doSelectionChanged();

    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    private void doWidgetMousePressed( MousePressedEvent ev )
    {
        procPanel.setActiveModulePanel( this );
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    private void doWidgetMouseReleased( MouseReleasedEvent ev )
    {
        procPanel.setActiveModulePanel( this );
        if ( draggingBoxState )
        {
            draggingBoxState = false;
            repaint();
        }
        else if ( !ev.isShiftDown() )
        {
            unselectAllModules();
        }

        if ( fromPortIndex > -1 )
        {
            if ( magneticLine )
            {
                procPanel.addUndoRecord();

                if ( fromPortIsOutput )
                {
                    ( (TapVisualModule) visualModules.elementAt( fromVisualModule ) ).getModule()
                            .setLink( toVisualModule, fromPortIndex, toPortIndex );
                }
                else
                {
                    ( (TapVisualModule) visualModules.elementAt( toVisualModule ) ).getModule()
                            .setLink( fromVisualModule, toPortIndex, fromPortIndex );
                }

                buildModuleLinks();
                procPanel.minorViewSync( this );
                procedure.doLiveUpdate();
            }

            fromPortIndex = -1;
            linePoint1 = null;
            linePoint2 = null;
            repaint();
        }

        doSelectionChanged();
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    private void doWidgetMouseDragged( MouseDraggedEvent ev )
    {
        int i;
        Point where = ev.getPoint();

        if ( draggingBoxState )
        {
            dragX2 = where.x;
            dragY2 = where.y;

            for ( i = 0; i < visualModules.size(); ++i )
            {
                if ( ( (TapVisualModule) visualModules.elementAt( i ) ).getSelected() )
                {
                    ( (TapVisualModule) visualModules.elementAt( i ) ).setReverseSelected( false );
                }
            }

            Rectangle selRect = getDragBox();
            Point location = new Point();
            Point location2 = new Point();

            for ( i = 0; i < visualModules.size(); ++i )
            {
                TapVisualModule mod = (TapVisualModule) visualModules.elementAt( i );
                location = mod.getLocation();
                location2.x = location.x + mod.getWidth();
                location2.y = location.y + mod.getHeight();

                if ( selRect.contains( location ) && selRect.contains( location2 ) )
                {
                    mod.setReverseSelected( true );
                }
                else
                {
                    mod.setReverseSelected( false );
                }
            }

            repaint();
        }
        else
        {
            draggingBoxState = true;
            dragX1 = where.x;
            dragY1 = where.y;

            for ( i = 0; i < visualModules.size(); ++i )
            {
                TapVisualModule mod = (TapVisualModule) visualModules.elementAt( i );

                if ( ev.isShiftDown() )
                {
                    if ( mod.getSelected() )
                    {
                        mod.setReversibleSelect( false );
                    }
                    else
                    {
                        mod.setReversibleSelect( true );
                    }
                }
                else
                {
                    mod.setSelected( false );
                    mod.setReversibleSelect( true );
                }
            }
        }
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    private void doModuleMouseClicked( MouseClickedEvent ev )
    {
        procPanel.setActiveModulePanel( this );
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    private void doModuleMousePressed( MousePressedEvent ev )
    {
        procPanel.setActiveModulePanel( this );
        int vm = getVisualModule( ev.getWidget() );
        if ( !ev.isPopupTrigger() )
        {
            Point where = ev.getPoint();

            if ( vm >= 0 )
            {
                TapVisualModule mod = (TapVisualModule) visualModules.elementAt( vm );

                if ( mod.inPorts( where ) )
                {
                    fromVisualModule = vm;
                    fromPortIndex = mod.getClickedPortIndex( where );
                    fromPortNature = mod.getClickedPortNature( where );
                    fromPortIsOutput = mod.isClickedPortOutput( where );
                }
                else
                {
                    if ( ( !ev.isShiftDown() ) && ( !mod.getSelected() ) )
                    {
                        unselectAllModules();
                    }

                    mod.setSelected( true );
                    repaint();
                }
            }

            doSelectionChanged();
        }
        else
        {
            popupOn = true;
            popupClicked = vm;
        }
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    private void doModuleMouseDragged( MouseDraggedEvent ev )
    {
        if ( !popupOn )
        {
            Point where = ev.getPoint();
            int vm = getVisualModule( ev.getWidget() );

            if ( vm >= 0 )
            {
                if ( movingVM > -1 )
                {
                    TapVisualModule mod = (TapVisualModule) visualModules.elementAt( movingVM );
                    Point newLocation = mod.getLocation();
                    newLocation.x = newLocation.x + ev.getX() - movingVMLocation.x;
                    newLocation.y = newLocation.y + ev.getY() - movingVMLocation.y;

                    if ( newLocation.x + 10 > getComponent().getWidth() )
                    {
                        newLocation.x = getComponent().getWidth() - 10;
                    }

                    if ( newLocation.y + 10 > getComponent().getHeight() )
                    {
                        newLocation.y = getComponent().getHeight() - 10;
                    }

                    if ( newLocation.x < 0 )
                    {
                        newLocation.x = 0;
                    }

                    if ( newLocation.y < 0 )
                    {
                        newLocation.y = 0;
                    }

                    dragX1 = newLocation.x;
                    dragY1 = newLocation.y;
                    dragX2 = newLocation.x + mod.getWidth();
                    dragY2 = newLocation.y + mod.getHeight();
                    repaint();
                }
                else if ( fromPortIndex == -1 )
                {
                    movingVMLocation = where;
                    movingVM = vm;

                    TapVisualModule mod = (TapVisualModule) visualModules.elementAt( movingVM );
                    Point newLocation = mod.getLocation();
                    dragX1 = newLocation.x;
                    dragY1 = newLocation.y;
                    dragX2 = newLocation.x + mod.getWidth();
                    dragY2 = newLocation.y + mod.getHeight();
                    draggingBoxState = true;

                    if ( !ev.isShiftDown() && !mod.getSelected() )
                    {
                        unselectAllModules();
                    }

                    mod.setSelected( true );
                    repaint();
                }
                else if ( fromPortIndex > -1 )
                {
                    Point u;
                    TapVisualModule mod = (TapVisualModule) visualModules.elementAt( fromVisualModule );
                    Point location = mod.getLocation();

                    if ( fromPortIsOutput )
                    {
                        u = mod.getOutputPortLocation( fromPortIndex );
                    }
                    else
                    {
                        u = mod.getInputPortLocation( fromPortIndex );
                    }

                    u.x = u.x + location.x;
                    u.y = u.y + location.y;

                    Point v = where;
                    v.x = v.x + location.x;
                    v.y = v.y + location.y;

                    Point moduleLocation = new Point();

                    for ( int i = 0; i < visualModules.size(); ++i )
                    {
                        mod = (TapVisualModule) visualModules.elementAt( i );
                        location = mod.getLocation();
                        moduleLocation.x = v.x - location.x;
                        moduleLocation.y = v.y - location.y;

                        if ( mod.inPorts( moduleLocation ) && mod.getClickedPortNature( moduleLocation ) == fromPortNature && ( mod.isClickedPortOutput( moduleLocation ) == ( !fromPortIsOutput ) ) && ( i != fromVisualModule ) )
                        {
                            toVisualModule = i;
                            toPortIndex = mod.getClickedPortIndex( moduleLocation );
                            magneticLine = true;

                            if ( fromPortIsOutput )
                            {
                                magneticLocation = mod.getInputPortLocation( toPortIndex );
                            }
                            else
                            {
                                magneticLocation = mod.getOutputPortLocation( toPortIndex );
                            }

                            magneticLocation.x = magneticLocation.x + location.x;
                            magneticLocation.y = magneticLocation.y + location.y;
                        }
                    }

                    if ( magneticLine )
                    {
                        if ( ( Math.abs( v.x - magneticLocation.x ) <= 30 ) && ( Math.abs( v.y - magneticLocation.y ) <= 30 ) )
                        {
                            v = magneticLocation;
                        }
                        else
                        {
                            magneticLine = false;
                        }
                    }

                    linePoint1 = u;
                    linePoint2 = v;
                    repaint();
                }
            }
        }
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    private void doModuleMouseReleased( MouseReleasedEvent ev )
    {
        if ( !popupOn )
        {
            if ( movingVM >= 0 )
            {
                Point newLocation = new Point();

                for ( int i = 0; i < visualModules.size(); ++i )
                {
                    if ( ( (TapVisualModule) visualModules.elementAt( i ) ).getSelected() )
                    {
                        TapVisualModule mod = (TapVisualModule) visualModules.elementAt( i );
                        newLocation = mod.getLocation();
                        newLocation.x = newLocation.x + ev.getX() - movingVMLocation.x;
                        newLocation.y = newLocation.y + ev.getY() - movingVMLocation.y;

                        if ( newLocation.x + 10 > getComponent().getWidth() )
                        {
                            newLocation.x = getComponent().getWidth() - 10;
                        }

                        if ( newLocation.y + 10 > getComponent().getHeight() )
                        {
                            newLocation.y = getComponent().getHeight() - 10;
                        }

                        if ( newLocation.x < 0 )
                        {
                            newLocation.x = 0;
                        }

                        if ( newLocation.y < 0 )
                        {
                            newLocation.y = 0;
                        }

                        procPanel.addUndoRecord();
                        mod.setLocation( newLocation );
                        moduleContainer.setChildBounds( mod, mod.getBounds() );
                        moduleContainer.layoutChildren();
                    }
                }

                movingVM = -1;
                draggingBoxState = false;

                if ( linksLinesFrom != null )
                {
                    buildModuleLinks();
                }
                procPanel.minorViewSync( this );
                repaint();
            }

            if ( draggingBoxState )
            {
                draggingBoxState = false;
                /*
                 *  unselectAllModules();
                 *  Rectangle selRect = getDragBox();
                 *  Point location = new Point();
                 *  Point location2 = new Point();
                 *  for ( int i = 0; i < visualModules.size(); ++i )
                 *  {
                 *  TapVisualModule mod = (TapVisualModule) visualModules.elementAt( i );
                 *  location = mod.getLocation();
                 *  location2.x = location.x + mod.getWidth();
                 *  location2.y = location.y + mod.getHeight();
                 *  if ( selRect.contains( location ) && selRect.contains( location2 ) )
                 *  {
                 *  mod.setSelected( true );
                 *  }
                 *  }
                 */
                repaint();
            }

            if ( fromPortIndex > -1 )
            {
                if ( magneticLine )
                {
                    procPanel.addUndoRecord();

                    if ( fromPortIsOutput )
                    {
                        ( (TapVisualModule) visualModules.elementAt( fromVisualModule ) ).getModule()
                                .setLink( toVisualModule, fromPortIndex, toPortIndex );
                    }
                    else
                    {
                        ( (TapVisualModule) visualModules.elementAt( toVisualModule ) ).getModule()
                                .setLink( fromVisualModule, toPortIndex, fromPortIndex );
                    }

                    buildModuleLinks();
                    procPanel.minorViewSync( this );
                    procedure.doLiveUpdate();
                }

                fromPortIndex = -1;
                linePoint1 = null;
                linePoint2 = null;
                repaint();
            }

            doSelectionChanged();
        }
        else
        {
            popupOn = false;
        }
    }


//}}}

    /**
     *  Cut command
     */
    public void doCut()
    {
        doCopy();
        doClear();
        procedure.setModified( true );
    }


    /**
     *  Copy command
     */
    public void doCopy()
    {
        boolean isEmptied = false;

        int[] translationTable = new int[visualModules.size()];
        int i;
        int l = 0;

        for ( i = 0; i < visualModules.size(); ++i )
        {
            TapVisualModule mod = (TapVisualModule) visualModules.elementAt( i );

            if ( mod.getSelected() )
            {
                if ( !isEmptied )
                {
                    isEmptied = true;
                    clipboardModules.clear();
                }

                translationTable[i] = l++;
                clipboardModules.addElement( mod.getModule().duplicate() );
            }
            else
            {
                translationTable[i] = -1;
            }
        }

        if ( l < translationTable.length )
        {
            for ( i = 0; i < clipboardModules.size(); ++i )
            {
                ( (TapModule) clipboardModules.elementAt( i ) ).applyTranslation( translationTable );
            }

            //procedure.setModified(true);
        }
    }


//{{{ Cut, copy and paste commands
    /**
     *  Paste command
     */
    public void doPaste()
    {
        if ( !clipboardModules.isEmpty() )
        {
            procPanel.addUndoRecord();

            unselectAllModules();

            int[] translationTable = new int[clipboardModules.size()];
            int count = visualModules.size();
            int i;

            for ( i = 0; i < clipboardModules.size(); ++i )
            {
                procPanel.addModule( (TapModule) ( (TapModule) clipboardModules.elementAt( i ) ).duplicate() );

                TapVisualModule mod = (TapVisualModule) visualModules.lastElement();
                Point newLocation = mod.getLocation();
                newLocation.x = newLocation.x + 30;
                newLocation.y = newLocation.y + 30;

                if ( newLocation.x > getComponent().getWidth() )
                {
                    newLocation.x = getComponent().getWidth() - 10;
                }

                if ( newLocation.y > getComponent().getHeight() )
                {
                    newLocation.y = getComponent().getHeight() - 10;
                }

                mod.setLocation( newLocation );
                moduleContainer.setChildBounds( mod, mod.getBounds() );
                mod.setSelected( true );
                translationTable[i] = count + i;
            }

            for ( i = 0; i < clipboardModules.size(); ++i )
            {
                ( (TapVisualModule) visualModules.elementAt( count + i ) ).applyTranslation( translationTable );
            }

            procedure.setModified( true );
            procedure.doLiveUpdate();
            layoutChildren();
        }

        buildModuleLinks();
        repaint();
        doSelectionChanged();
        procPanel.minorViewSync( this );
    }


    /**
     *  Clear command
     */
    public void doClear()
    {
        int[] translationTable = new int[visualModules.size()];
        int i;
        int numSel = 0;
        boolean update = false;

        for ( i = 0; i < translationTable.length; ++i )
        {
            TapVisualModule mod = (TapVisualModule) visualModules.elementAt( i );

            if ( mod.getSelected() )
            {
                translationTable[i] = -1;
            }
            else
            {
                translationTable[i] = numSel++;
            }
        }

        if ( ( selectedLinks != null ) || ( numSel < translationTable.length ) )
        {
            procPanel.addUndoRecord();
            update = true;
        }

        if ( selectedLinks != null )
        {
            int j;
            int k;
            int l;
            int m;
            int linkToIndex;
            int inputPortLink;
            int d;
            Point location1 = new Point();
            Point location2 = new Point();

            for ( i = 0; i < visualModules.size(); ++i )
            {
                TapVisualModule mod = (TapVisualModule) visualModules.elementAt( i );

                for ( j = 0; j < mod.getModule().getNumOutput(); ++j )
                {
                    for ( k = 0; k < mod.getModule().getNumLinks( j ); ++k )
                    {
                        linkToIndex = mod.getModule().getLinkToIndex( j, k );
                        inputPortLink = mod.getModule().getInputPortLink( j, k );
                        location1 = mod.getOutputPortLinkLocation( j );
                        location2 = ( (TapVisualModule) visualModules.elementAt( linkToIndex ) ).getInputPortLinkLocation( inputPortLink );
                        location1.x = location1.x + mod.getLocation().x;
                        location1.y = location1.y + mod.getLocation().y;
                        location2.x = location2.x + ( (TapVisualModule) visualModules.elementAt( linkToIndex ) ).getLocation().x;
                        location2.y = location2.y + ( (TapVisualModule) visualModules.elementAt( linkToIndex ) ).getLocation().y;

                        for ( l = 0; l < selectedLinks.length; ++l )
                        {
                            m = selectedLinks[l];
                            d = java.lang.Math.abs( linksLinesFrom[m].x - location1.x ) + java.lang.Math.abs( linksLinesFrom[m].y - location1.y );
                            d = d + java.lang.Math.abs( linksLinesTo[m].x - location2.x ) + java.lang.Math.abs( linksLinesTo[m].y - location2.y );

                            if ( d == 0 )
                            {
                                mod.getModule().deleteLink( j, k );
                            }
                        }
                    }
                }
            }

            selectedLinks = null;
            procedure.setModified( true );
            procPanel.minorViewSync( this );
        }

        if ( numSel < translationTable.length )
        {
            for ( i = 0; i < translationTable.length; ++i )
            {
                if ( translationTable[i] != -1 )
                {
                    ( (TapVisualModule) visualModules.elementAt( i ) ).applyTranslation( translationTable );
                }
            }

            int l = 0;

            for ( i = 0; i < translationTable.length; ++i )
            {
                if ( translationTable[i] == -1 )
                {
                    TapVisualModule mod = (TapVisualModule) visualModules.elementAt( i - l );
                    mod.prepareToBeDeleted();
                    mod.removeEventLink( MousePressedEvent.class, this );
                    mod.removeEventLink( MouseDraggedEvent.class, this );
                    mod.removeEventLink( MouseReleasedEvent.class, this );
                    mod.removeEventLink( KeyPressedEvent.class, this );
                    mod.removeEventLink( WidgetMouseEvent.class, popup );
                    //( (JPanel) moduleContainer.getComponent() ).remove( mod.getPanel() );
                    moduleContainer.remove( mod );
                    visualModules.remove( i - l );
                    procedure.getModules().remove( i - l );
                    ++l;
                }
            }

            procedure.setModified( true );
        }

        buildModuleLinks();
        if ( update )
        {
            procedure.doLiveUpdate();
        }
        repaint();
        doSelectionChanged();
        procPanel.checkValidObject();
        procPanel.majorViewSync( this );
    }


    //}}}

//{{{ Rename command

    /**
     *  Rename command. Must be called only if one module is selected
     */
    public void doRename()
    {
        procPanel.addUndoRecord();

        for ( int i = 0; i < visualModules.size(); ++i )
        {
            if ( ( (TapVisualModule) visualModules.elementAt( i ) ).getSelected() )
            {
                String s = (String) JOptionPane.showInputDialog( this.getComponent(), TapBTranslate.text( "enterModuleName" ), TapBTranslate.text( "renameModule" ), JOptionPane.QUESTION_MESSAGE, null, null, (Object) ( (TapVisualModule) visualModules.elementAt( i ) ).getModule().getName() );
                if ( s != null )
                    if ( s.length() > 0 )
                    {
                        TapVisualModule vmod = (TapVisualModule) visualModules.elementAt( i );
                        vmod.setName( s );
                        vmod.getModule().changed = true;
                        vmod.getModule().updateModuleWindow();
                        procedure.notifyMinorChange();
                    }

            }
        }
        layoutChildren();
        procPanel.minorViewSync( this );
    }


    /**
     *  Updates a visual module child bounds Called by modules when they change
     *  of name
     *
     *@param  vmod  The visual module the bounds of which or to update
     */
    public void updateChildBounds( TapVisualModule vmod )
    {
        moduleContainer.setChildBounds( vmod, vmod.getBounds() );
    }


    /**
     *  Same as above but triggered by the popup menu
     *
     *@param  ev  Command Event
     */
    public void popupRename( CommandEvent ev )
    {
        if ( popupClicked >= 0 )
        {
            procPanel.addUndoRecord();

            String s = (String) JOptionPane.showInputDialog( this.getComponent(), TapBTranslate.text( "enterModuleName" ), TapBTranslate.text( "renameModule" ), JOptionPane.QUESTION_MESSAGE, null, null, (Object) ( (TapVisualModule) visualModules.elementAt( popupClicked ) ).getModule().getName() );

            if ( ( s != null ) && ( s.length() > 0 ) )
            {
                TapVisualModule vmod = (TapVisualModule) visualModules.elementAt( popupClicked );
                vmod.setName( s );
                vmod.getModule().changed = true;
                vmod.getModule().updateModuleWindow();
                moduleContainer.setChildBounds( vmod, vmod.getBounds() );
                procedure.notifyMinorChange();
            }

            repaint();
            procPanel.minorViewSync( this );
        }
    }


    //}}}


    //{{{ Main entry selection
    /**
     *  Main entry selection
     */
    public void doSelectMainEntry()
    {
        for ( int i = 0; i < visualModules.size(); ++i )
        {
            if ( ( (TapVisualModule) visualModules.elementAt( i ) ).getSelected() )
            {
                doSelectMainEntry( i );
            }
        }
        procPanel.minorViewSync( this );
    }


    /**
     *  Main entry selection using the popup menu
     *
     *@param  ev  CommandEvent
     */
    public void popupMainEntry( CommandEvent ev )
    {
        doSelectMainEntry( popupClicked );
        procPanel.minorViewSync( this );
    }


    //}}}

    /**
     *  Description of the Method
     *
     *@param  which  Description of the Parameter
     */
    public void doSelectMainEntry( int which )
    {
        TapVisualModule vmod;
        Vector modules = procPanel.getProcedure().getModules();

        vmod = (TapVisualModule) visualModules.elementAt( which );

        if ( vmod.getModule().acceptsMainEntry() )
        {
            procPanel.addUndoRecord();

            for ( int i = 0; i < modules.size(); ++i )
            {
                ( (TapModule) modules.elementAt( i ) ).setMainEntry( false );
            }

            vmod.getModule().setMainEntry( true );
            procedure.setModified( true );
            procedure.doLiveUpdate();
        }

        procPanel.checkMainEntry();
        repaint();
    }


    /**
     *  Description of the Method
     */
    public void doLeftToRight()
    {
        setSelectionModuleDecoration( TapModule.LEFT_TO_RIGHT );
        procPanel.minorViewSync( this );
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    public void popupLeftToRight( CommandEvent ev )
    {
        setPopupModuleDecoration( TapModule.LEFT_TO_RIGHT );
        procPanel.minorViewSync( this );
    }


    /**
     *  Description of the Method
     */
    public void doTopToBottom()
    {
        setSelectionModuleDecoration( TapModule.TOP_TO_BOTTOM );
        procPanel.minorViewSync( this );
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    public void popupTopToBottom( CommandEvent ev )
    {
        setPopupModuleDecoration( TapModule.TOP_TO_BOTTOM );
        procPanel.minorViewSync( this );
    }


    /**
     *  Description of the Method
     */
    public void doRightToLeft()
    {
        setSelectionModuleDecoration( TapModule.RIGHT_TO_LEFT );
        procPanel.minorViewSync( this );
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    public void popupRightToLeft( CommandEvent ev )
    {
        setPopupModuleDecoration( TapModule.RIGHT_TO_LEFT );
        procPanel.minorViewSync( this );
    }


    /**
     *  Description of the Method
     */
    public void doBottomToTop()
    {
        setSelectionModuleDecoration( TapModule.BOTTOM_TO_TOP );
        procPanel.minorViewSync( this );
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    public void popupBottomToTop( CommandEvent ev )
    {
        setPopupModuleDecoration( TapModule.BOTTOM_TO_TOP );
        procPanel.minorViewSync( this );
    }


    /**
     *  Unselects all modules of the proc panel
     */
    private void unselectAllModules()
    {
        selectedLinks = null;

        for ( int i = 0; i < visualModules.size(); ++i )
        {
            if ( ( (TapVisualModule) visualModules.elementAt( i ) ).getSelected() )
            {
                ( (TapVisualModule) visualModules.elementAt( i ) ).setSelected( false );
            }
        }
    }


    /**
     *  Gets a location for a new module
     *
     *@return    The newLocation value
     */
    protected Point getNewLocation()
    {
        Point p = new Point();
        Dimension dim = getComponent().getSize();
        p.x = (int) dim.width / 2 + (int) ( 0.6 * dim.width * ( Math.random() - 0.5 ) );
        p.y = (int) dim.height / 2 + (int) ( 0.6 * dim.height * ( Math.random() - 0.5 ) );

        return p;
    }


    /**
     *  This method rebuilds links geometry
     */
    public void buildModuleLinks()
    {
        int i;
        int j;
        int k;
        int l;
        int linkToIndex;
        int inputPortLink;
        ;

        linksLinesFrom = null;
        linksLinesTo = null;

        for ( i = 0; i < visualModules.size(); ++i )
        {
            TapVisualModule mod = (TapVisualModule) visualModules.elementAt( i );

            for ( j = 0; j < mod.getModule().getNumOutput(); ++j )
            {
                for ( k = 0; k < mod.getModule().getNumLinks( j ); ++k )
                {
                    linksLinesFrom = TapUtils.increasePointArray( linksLinesFrom );
                    linksLinesTo = TapUtils.increasePointArray( linksLinesTo );
                    l = linksLinesTo.length - 1;
                    linkToIndex = mod.getModule().getLinkToIndex( j, k );
                    inputPortLink = mod.getModule().getInputPortLink( j, k );
                    linksLinesFrom[l] = mod.getOutputPortLinkLocation( j );
                    linksLinesTo[l] = ( (TapVisualModule) visualModules.elementAt( linkToIndex ) ).getInputPortLinkLocation( inputPortLink );
                    linksLinesFrom[l].x = linksLinesFrom[l].x + mod.getLocation().x;
                    linksLinesFrom[l].y = linksLinesFrom[l].y + mod.getLocation().y;
                    linksLinesTo[l].x = linksLinesTo[l].x + ( (TapVisualModule) visualModules.elementAt( linkToIndex ) ).getLocation().x;
                    linksLinesTo[l].y = linksLinesTo[l].y + ( (TapVisualModule) visualModules.elementAt( linkToIndex ) ).getLocation().y;

                }
            }
        }
    }


    /**
     *  Called when a repaint event occurs in the link container
     *
     *@param  ev  repaint event
     */
    public void doLinkRepaintEvent( RepaintEvent ev )
    {
        Graphics g = ev.getGraphics();
        paintVisualModulesLinks( g );

        if ( draggingBoxState )
        {
            paintDraggingBox( g );
        }

        if ( linePoint1 != null && linePoint2 != null )
        {
            paintLine( g );
        }
    }


    /**
     *  draws the contents of the proc panel
     *
     *@param  g  Graphics object
     */
    public void paint( Graphics g )
    {
        paintVisualModulesLinks( g );

        if ( draggingBoxState )
        {
            paintDraggingBox( g );
        }

        if ( linePoint1 != null && linePoint2 != null )
        {
            paintLine( g );
        }
    }


    /**
     *  Gets the current dragging box
     *
     *@return    The dragBox value
     */
    private Rectangle getDragBox()
    {
        int x1;
        int y1;
        int x2;
        int y2;

        if ( dragX1 <= dragX2 )
        {
            x1 = dragX1;
            x2 = dragX2;
        }
        else
        {
            x2 = dragX1;
            x1 = dragX2;
        }

        if ( dragY1 <= dragY2 )
        {
            y1 = dragY1;
            y2 = dragY2;
        }
        else
        {
            y2 = dragY1;
            y1 = dragY2;
        }

        return new Rectangle( x1, y1, x2 - x1, y2 - y1 );
    }


    /**
     *  draws the dragging box on the proc panel
     *
     *@param  g  Graphics object
     */
    private void paintDraggingBox( Graphics g )
    {
        g.setColor( Color.black );
        //g.setXORMode( Color.white );

        Rectangle dragBox = getDragBox();
        g.drawRect( dragBox.x, dragBox.y, dragBox.getSize().width, dragBox.getSize().height );

        if ( linePoint1 != null && linePoint2 != null )
        {
            g.drawLine( linePoint1.x, linePoint1.y, linePoint2.x, linePoint2.y );
        }

        //g.setXORMode( null );
    }


    /**
     *  Paints a line while dragging a new link
     *
     *@param  g  Graphics object
     */
    private void paintLine( Graphics g )
    {
        g.setColor( Color.black );
        //g.setXORMode( Color.white );
        g.drawLine( linePoint1.x, linePoint1.y, linePoint2.x, linePoint2.y );
        //g.setXORMode( null );
    }


    /**
     *  This method is used to check if a particular link is selected
     *
     *@param  link  Index of the item to check for sleection
     *@return       true if the link is selected, false otherwise.
     */
    public boolean isSelectedLink( int link )
    {
        if ( selectedLinks != null )
        {
            for ( int i = 0; i < selectedLinks.length; ++i )
            {
                if ( selectedLinks[i] == link )
                {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     *  Paints the links between modules
     *
     *@param  g  Graphics object
     */
    private void paintVisualModulesLinks( Graphics g )
    {
        if ( linksLinesFrom != null )
        {
            for ( int i = 0; i < linksLinesFrom.length; ++i )
            {
                if ( isSelectedLink( i ) )
                {
                    g.setColor( Color.red );
                }
                else
                {
                    g.setColor( Color.black );
                }

                g.drawLine( linksLinesFrom[i].x, linksLinesFrom[i].y, linksLinesTo[i].x, linksLinesTo[i].y );
            }
        }
    }


    /**
     *  Gets the visualModule attribute of the TapProcPanel object
     *
     *@param  obj  Description of the Parameter
     *@return      The visualModule value
     */
    public int getVisualModule( Object obj )
    {
        int vm = -1;
        for ( int i = 0; i < visualModules.size(); ++i )
        {
            if ( visualModules.elementAt( i ) == obj )
            {
                vm = i;
            }
        }

        return vm;
    }


    /**
     *  Adds a new visual module to the module panel
     *
     *@param  newModule  The module to add to the proc panel
     */
    public void addModule( TapModule newModule )
    {
        TapVisualModule mod = new TapVisualModule( this, newModule );
        visualModules.add( mod );

        moduleContainer.add( mod, mod.getBounds() );
        mod.addEventLink( MouseClickedEvent.class, this, "doModuleMouseClicked" );
        mod.addEventLink( MousePressedEvent.class, this, "doModuleMousePressed" );
        mod.addEventLink( MouseDraggedEvent.class, this, "doModuleMouseDragged" );
        mod.addEventLink( MouseReleasedEvent.class, this, "doModuleMouseReleased" );
        mod.addEventLink( KeyPressedEvent.class, this, "doKeyPressed" );
        mod.addEventLink( KeyReleasedEvent.class, this, "doKeyReleased" );
        mod.addEventLink( WidgetMouseEvent.class, this, "doShowPopup" );
        layoutChildren();
        repaint();
    }



    /**
     *  Sets the selectionModuleDecoration attribute of the TapProcPanel object
     *
     *@param  decoration  The new selectionModuleDecoration value
     */
    public void setSelectionModuleDecoration( int decoration )
    {
        procPanel.addUndoRecord();

        for ( int i = 0; i < visualModules.size(); ++i )
        {
            if ( ( (TapVisualModule) visualModules.elementAt( i ) ).getSelected() )
            {
                ( (TapVisualModule) visualModules.elementAt( i ) ).setDecoration( decoration );
            }
        }

        buildModuleLinks();
        repaint();
        procedure.notifyMinorChange();
    }


    /**
     *  Sets the popupModuleDecoration attribute of the TapProcPanel object
     *
     *@param  decoration  The new popupModuleDecoration value
     */
    public void setPopupModuleDecoration( int decoration )
    {
        procPanel.addUndoRecord();
        ( (TapVisualModule) visualModules.elementAt( popupClicked ) ).setDecoration( decoration );
        buildModuleLinks();
        procedure.notifyMinorChange();
        repaint();
    }


    /**
     *  Description of the Method
     */
    public void closeWindows()
    {
        for ( int i = 0; i < visualModules.size(); ++i )
        {
            ( (TapVisualModule) visualModules.elementAt( i ) ).getModule().closeAllWindows();
        }
    }


    /**
     *  Description of the Method
     */
    public void doSelectionChanged()
    {
        short num = 0;

        for ( int i = 0; i < visualModules.size(); ++i )
        {
            if ( ( ( (TapVisualModule) visualModules.elementAt( i ) ).getSelected() ) && ( num < 2 ) )
            {
                ++num;
            }
        }

        procPanel.getHolder().selectionChanged( num );

        if ( selectedLinks != null )
        {
            procPanel.getHolder().selectionChanged( (short) 2 );
        }
    }


    /**
     *  Shows module popup menu
     *
     *@param  ev  The WidgetMouseEvent that triggers the popup
     */
    public void doShowPopup( WidgetMouseEvent ev )
    {
        popupClicked = getVisualModule( ev.getWidget() );
        popup.show( ev );
    }


    /**
     *  Shows view popup menu
     *
     *@param  ev  The WidgetMouseEvent that triggers the popup
     */
    public void doShowViewPopup( WidgetMouseEvent ev )
    {
        viewPopup.show( ev );
    }


    /**
     *  Triggers the choose seed dialog
     */
    public void doChooseSeed()
    {
        procPanel.doChooseSeed();
    }


    /**
     *  Sets the preferred size of the TapModulePanel object
     *
     *@param  size  The new preferred size value
     */
    public void setPreferredSize( Dimension size )
    {
        linkContainer.setPreferredSize( size );
    }


    /**
     *  Returns the minimum size required to show all the module panels
     *
     *@return    The requiredSize value
     */

    public Dimension getRequiredSize()
    {
        return moduleContainer.getRequiredSize();
    }


    /**
     *  Synchronizes minor features of a procedures : modules links, names, etc.
     *  supposing the number of modules and their natures haven't changed. Call
     *  addSync if modules have been added. Call initialize() for any other
     *  change.
     */
    public void minorSync()
    {
        for ( int i = 0; i < visualModules.size(); ++i )
        {
            TapVisualModule vmod = (TapVisualModule) visualModules.elementAt( i );
            vmod.setPortsBounds();
            vmod.setName( vmod.getModule().getName() );
            moduleContainer.setChildBounds( vmod, vmod.getBounds() );
        }
        buildModuleLinks();
        layoutChildren();
        repaint();
    }


    /**
     *  Description of the Method
     */
    public void triggerMinorSync()
    {
        procPanel.minorViewSync( this );
    }


    /**
     *  Synchronizes module panels when modules have been added in a panel.
     */
    public void syncModuleAddition()
    {
        Vector modules = procedure.getModules();
        if ( modules.size() <= visualModules.size() )
            return;
        for ( int i = visualModules.size(); i < modules.size(); ++i )
            addModule( (TapModule) modules.elementAt( i ) );
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public BScrollPane newScrollPane()
    {
        return new TapModuleScrollPane( this );
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean needsScrollPane()
    {
        return true;
    }



    /**
     *  An Explicit Container for which the getMinimumSize method is customized
     *
     *@author     francois guillet
     *@created    8 avril 2004
     */
    class ModuleContainer extends ExplicitContainer
    {

        /**
         *  Gets the requiredSize attribute of the ModuleContainer object
         *
         *@return    The requiredSize value
         */
        public Dimension getRequiredSize()
        {
            Dimension requiredSize = new Dimension();
            for ( int i = getChildCount() - 1; i >= 0; i-- )
            {
                Rectangle r = getChildBounds( i );
                requiredSize.width = Math.max( requiredSize.width, r.x + r.width );
                requiredSize.height = Math.max( requiredSize.height, r.y + r.height );
            }
            return requiredSize;
        }

    }


    /**
     *  Sets the layoutData attribute of the TapView object
     *
     *@param  data  The new layoutData value
     */
    public void setLayoutData( ProcPanelLayoutData data )
    {
    }


    /**
     *  Gets the layoutData attribute of the TapView object
     *
     *@return    The layoutData value
     */
    public ProcPanelLayoutData getLayoutData()
    {
        return null;
    }

}

