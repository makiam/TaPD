/*
 *  This class is responsible for display and edition of a procedure
 *  It contains every method needed to edit the procedure and hence could be used in any
 *  frame, provided the frame calls the relevant methods for menu commands.
 *  The container of a procpanel should implement the TapProcPanelHolder interface.
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

//{{{ imports
import artofillusion.*;
import artofillusion.math.*;
import artofillusion.tapDesigner.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;

//}}}

/**
 *  This tabbed pane holds all the panels necessary for procedure editing
 *
 *@author     François Guillet
 *@created    14 mars 2004
 */
public class TapProcPanel
         extends BTabbedPane
{
    //{{{ variables
    private TapModuleScrollPane scrollpane;
    private Vector views;
    private TapProcPanelHolder holder;
    private TapProcedure procedure;
    private TapUndoRecord undoRecord;
    private TapModulePanel activeModulePanel;
    private boolean init;

    /**
     *  No module is selected in the procedure windows
     */
    public final static short NO_SELECTION = 0;
    /**
     *  One and only one module is selected
     */
    public final static short SINGLE_SELECTION = 1;
    /**
     *  Several modules are selected
     */
    public final static short MULTIPLE_SELECTION = 2;


//}}}

    //{{{  constructor
    /**
     *  Constructor for the TapProcPanel object
     *
     *@param  procedure  TaPD procedure attached to the procPanel
     *@param  holder     procPanel holder
     */
    public TapProcPanel( TapProcedure procedure, TapProcPanelHolder holder )
    {
        super();
        init = true;
        setTabPosition( BTabbedPane.BOTTOM );
        this.holder = holder;
        this.procedure = procedure;
        procedure.setProcPanel( this );
        undoRecord = new TapUndoRecord( holder, procedure.getUndoRecordSize() );
        this.procedure.registerUndoRecord( undoRecord );
        views = new Vector();
        ProcPanelLayout[] layouts = procedure.getProcPanelLayouts();
        if ( layouts == null )
        {
            doAddTabbedModuleView();
            doAddTabbedParamView();
            doAddTabbedPreviewView();
        }
        else
        {
            for ( int i = 0; i < layouts.length; ++i )
            {
                BorderContainer bc;
                add( bc = new BorderContainer(), layouts[i].getName() );
                bc.add( layouts[i].createWidget( this ), BorderContainer.CENTER );
            }
        }
        initialize();
        init = false;
        addEventLink( SelectionChangedEvent.class, this, "doSelectedTabChanged" );
        setSelectedTab( 0 );
        activeModulePanel = (TapModulePanel) views.elementAt( 0 );
        addEventLink( KeyPressedEvent.class, this, "doKeyPressed" );
        addEventLink( KeyReleasedEvent.class, this, "doKeyReleased" );

    }


    //}}}

    //{{{ initialization
    /**
     *  Initialization
     */
    private void initialize()
    {
        for ( int i = 0; i < views.size(); ++i )
        {
            ( (TapView) views.elementAt( i ) ).initialize();
        }
    }


    //}}}

//{{{ Cleanup when closing

    /**
     *  Cleanup when closing editor window
     */
    public void closeWindows()
    {
        for ( int i = 0; i < views.size(); ++i )
        {
            ( (TapView) views.elementAt( i ) ).closeWindows();

        }
    }


    //}}}

//{{{ Events
    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    private void doKeyPressed( KeyPressedEvent ev )
    {
        if ( activeModulePanel != null )
            activeModulePanel.doKeyPressed( ev );
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    private void doKeyReleased( KeyReleasedEvent ev )
    {
        if ( activeModulePanel != null )
            activeModulePanel.doKeyReleased( ev );
    }


    //}}}

//{{{ Undo operations
    /**
     *  Adds a feature to the UndoRecord attribute of the TapProcPanel object
     */
    public void addUndoRecord()
    {
        undoRecord.addRecord( procedure.duplicate() );
    }


    /**
     *  Undo command
     */
    public void doUndo()
    {
        TapProcedure newProcedure = undoRecord.getUndoRecord( procedure );
        setNewProcedure( newProcedure );

    }


    /**
     *  Redo command
     */
    public void doRedo()
    {
        TapProcedure newProcedure = undoRecord.getRedoRecord();
        setNewProcedure( newProcedure );
    }


    /**
     *  Triggers the undo level edition from the procedure
     */
    public void doEditUndoLevel()
    {
        procedure.editUndoLevel( holder.getBFrame() );
        undoRecord.setRecordSize( procedure.getUndoRecordSize() );
    }


    //}}}

//{{{ Getters for modulePanel, main frame as BFrame, procedure and holder
    /**
     *  Gets the modulePanel attribute of the TapProcPanel object
     *
     *@return    The module panel variable of the proc panel
     */
    public TapModulePanel getModulePanel()
    {
        return activeModulePanel;
    }


    /**
     *  Gets the main frame as BFrame object
     *
     *@return    The BFrame value
     */
    public BFrame getBFrame()
    {
        return holder.getBFrame();
    }


    /**
     *  Returns the procedure attached to the proc panel
     *
     *@return    The procedure value
     */
    public TapProcedure getProcedure()
    {
        return procedure;
    }


    /**
     *  Gets the procPanelHolder attribute of the TapProcPanel object
     *
     *@return    The procPanelHolder value
     */
    public TapProcPanelHolder getHolder()
    {
        return holder;
    }


    //}}}

//{{{  attaches a new procedure to the ProcPanel
    /**
     *  Attaches a new procedure to the TapProcPanel
     *
     *@param  newProcedure  The new newProcedure value
     */
    public void setNewProcedure( TapProcedure newProcedure )
    {
        if ( newProcedure != null )
        {
            procedure = newProcedure;
            for ( int i = 0; i < views.size(); ++i )
            {
                ( (TapView) views.elementAt( i ) ).closeWindows();

            }
            initialize();
            layoutChildren();
            repaint();
            //holder.requestFocus();
        }
    }


    //}}}

//{{{ edit seed dialog
    /**
     *  Triggers the choose seed dialog from the procedure
     */
    public void doChooseSeed()
    {
        procedure.editSeed( holder.getBFrame() );
    }


    //}}}

//{{{ Texture and material management

    /**
     *  Imports/exports textures from/to the AoI Scene
     */
    public void importTextures()
    {
        undoRecord.addRecord( procedure.duplicate() );
        procedure.importTextures( holder.getBFrame() );
    }


    /**
     *  Textures management
     */
    public void manageTextures()
    {
        undoRecord.addRecord( procedure.duplicate() );
        procedure.manageTextures( holder.getBFrame() );
    }


    /**
     *  Imports/exports materials from/to the AoI Scene
     */
    public void importMaterials()
    {
        undoRecord.addRecord( procedure.duplicate() );
        procedure.importMaterials( holder.getBFrame() );
    }


    /**
     *  Materials management
     */
    public void manageMaterials()
    {
        undoRecord.addRecord( procedure.duplicate() );
        procedure.manageMaterials( holder.getBFrame() );
    }


    /**
     *  Images management
     */
    public void manageImages()
    {
        undoRecord.addRecord( procedure.duplicate() );
        procedure.manageImages( holder.getBFrame() );
    }


    /**
     *  Objects management
     */
    public void manageObjects()
    {
        undoRecord.addRecord( procedure.duplicate() );
        procedure.manageObjects( holder.getBFrame() );
    }


//}}}

//{{{ Cut, Copy, Paste, Clear

    /**
     *  Cut menu command
     */
    public void doCut()
    {
        if ( activeModulePanel != null )
            activeModulePanel.doCut();
    }


    /**
     *  Paste menu command
     */
    public void doPaste()
    {
        if ( activeModulePanel != null )
            activeModulePanel.doPaste();
    }


    /**
     *  Copy menu command
     */
    public void doCopy()
    {
        if ( activeModulePanel != null )
            activeModulePanel.doCopy();
    }


    /**
     *  Clear menu command
     */
    public void doClear()
    {
        if ( activeModulePanel != null )
            activeModulePanel.doClear();
    }


    //}}}

    /**
     *  Adds an AoIObject
     */
    public void addAoIObject()
    {
        if ( ( procedure.getScene().getNumObjects() > 0 ) && ( activeModulePanel != null ) )
        {
            addModule( new AoIObjectModule( procedure, getNewLocation() ) );
            checkMainEntry();
        }
    }


    /**
     *  Adds a Leaf module
     */
    public void addLeaf()
    {

        if ( activeModulePanel != null )
        {
            addModule( new LeafModule( procedure, getNewLocation() ) );
            checkMainEntry();
        }
    }


    /**
     *  Adds a Spline object
     */
    public void addSpline()
    {
        if ( activeModulePanel != null )
        {
            addModule( new SplineModule( procedure, getNewLocation() ) );
            checkMainEntry();
        }
    }


    /**
     *  Adds a Tube object
     */
    public void addTube()
    {

        if ( activeModulePanel != null )
        {
            addModule( new TubeModule( procedure, getNewLocation() ) );
            checkMainEntry();
        }
    }


    /**
     *  Adds a Value module
     */
    public void addValue()
    {
        if ( activeModulePanel != null )
        {
            addModule( new ConstantValueModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a Function module
     */
    public void addFunction()
    {
        if ( activeModulePanel != null )
        {
            addModule( new ValueFunctionModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a BinaryOperation module object
     */
    public void addBinaryOperation()
    {
        if ( activeModulePanel != null )
        {
            addModule( new BinaryOpModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a UnaryOperation module object
     */
    public void addUnaryOperation()
    {
        if ( activeModulePanel != null )
        {
            addModule( new UnaryOpModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a ScaleShift module
     */
    public void addScaleShift()
    {
        if ( activeModulePanel != null )
        {
            addModule( new ScaleShiftModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a Clip module
     */
    public void addClip()
    {
        if ( activeModulePanel != null )
        {
            addModule( new ClipModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a RandomValue module
     */
    public void addRandomValue()
    {
        if ( activeModulePanel != null )
        {
            addModule( new RandomValueModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a Coil module
     */
    public void addCoil()
    {
        if ( activeModulePanel != null )
        {
            addModule( new CoilModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a GoldenBall module
     */
    public void addGoldenBall()
    {
        if ( activeModulePanel != null )
        {
            addModule( new GoldenBallModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a Field module
     */
    public void addField()
    {

        if ( activeModulePanel != null )
        {
            addModule( new FieldModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a Top module
     */
    public void addTop()
    {
        if ( activeModulePanel != null )
        {
            addModule( new TopModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a RandomObject module
     */
    public void addRandomObject()
    {
        if ( activeModulePanel != null )
        {
            addModule( new RandomObjectModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a Distort module
     */
    public void addDistort()
    {
        if ( activeModulePanel != null )
        {
            addModule( new DistortModule( procedure, getNewLocation() ) );
        }
    }


    /**
     *  Adds a new module to the procedure
     *
     *@param  newModule  The module to add to the procedure
     */
    public void addModule( TapModule newModule )
    {
        if ( !init )
        {
            addUndoRecord();
        }

        procedure.addModule( newModule );
        procedure.setModified( true );
        for ( int i = 0; i < views.size(); ++i )
        {
            ( (TapView) views.elementAt( i ) ).addModule( newModule );
        }
    }


    /**
     *  Returns true if the procedure has a correct main entry
     */
    protected void checkMainEntry()
    {
        Vector modules = procedure.getModules();
        boolean hasMainEntry = false;

        for ( int i = 0; i < modules.size(); ++i )
        {
            hasMainEntry |= ( (TapModule) modules.elementAt( i ) ).isMainEntry();
        }

        if ( !hasMainEntry )
        {
            ( (TapModule) modules.elementAt( modules.size() - 1 ) ).setMainEntry( true );
            getHolder().validObject( true );
            repaint();
        }
        else
        {
            getHolder().validObject( true );
        }
    }


    /**
     *  Returns true if the procedure holds a valid object
     */
    protected void checkValidObject()
    {
        Vector modules = procedure.getModules();
        boolean hasMainEntry = false;

        for ( int i = 0; i < modules.size(); ++i )
        {
            hasMainEntry |= ( (TapModule) modules.elementAt( i ) ).isMainEntry();
        }

        getHolder().validObject( hasMainEntry );
    }


    /**
     *  Rename command
     */
    public void doRename()
    {
        if ( activeModulePanel != null )
            activeModulePanel.doRename();
    }


    /**
     *  Description of the Method
     */
    public void doLeftToRight()
    {
        if ( activeModulePanel != null )
            activeModulePanel.doLeftToRight();
    }


    /**
     *  Description of the Method
     */
    public void doTopToBottom()
    {
        if ( activeModulePanel != null )
            activeModulePanel.doTopToBottom();
    }


    /**
     *  Description of the Method
     */
    public void doRightToLeft()
    {
        if ( activeModulePanel != null )
            activeModulePanel.doRightToLeft();
    }


    /**
     *  Description of the Method
     */
    public void doBottomToTop()
    {
        if ( activeModulePanel != null )
            activeModulePanel.doBottomToTop();
    }


    /**
     *  Description of the Method
     */
    public void doSelectMainEntry()
    {
        if ( activeModulePanel != null )
            activeModulePanel.doSelectMainEntry();
    }


    /**
     *  Gets the newLocation attribute of the TapProcPanel object
     *
     *@return    The newLocation value
     */
    protected Point getNewLocation()
    {
        return activeModulePanel.getNewLocation();
    }


    /**
     *  register a new TapModulePanel
     *
     *@param  tv  Description of the Parameter
     */
    public void register( TapView tv )
    {
        views.add( tv );
        tv.initialize();
    }


    /**
     *  removes a TapModulePanel from the module panels list
     *
     *@param  tv  Description of the Parameter
     */
    public void removeView( TapView tv )
    {
        views.remove( tv );
    }



    /**
     *  Adds a new tab containing a module view
     */
    public void doAddTabbedModuleView()
    {
        views.add( activeModulePanel = new TapModulePanel( this ) );
        BScrollPane scrollpane = new TapModuleScrollPane( activeModulePanel );
        BorderContainer bc;
        add( bc = new BorderContainer(), TapBTranslate.text( "procedure" ) );
        bc.add( scrollpane, BorderContainer.CENTER );
        activeModulePanel.initialize();
        layoutChildren();
        procedure.notifyMinorChange();
    }


    /**
     *  Description of the Method
     */
    public void doAddTabbedParamView()
    {
        TapParametersPanel tpp;
        views.add( tpp = new TapParametersPanel( this ) );
        BorderContainer bc;
        add( bc = new BorderContainer(), TapBTranslate.text( "parameters" ) );
        bc.add( tpp, BorderContainer.CENTER );
        activeModulePanel = null;
        tpp.initialize();
        setSelectedTab( getChildCount() - 1 );
        layoutChildren();
        procedure.notifyMinorChange();
    }


    /**
     *  Description of the Method
     */
    public void doAddTabbedPreviewView()
    {
        PreviewTapView ptv;
        views.add( ptv = new PreviewTapView( this ) );
        BorderContainer bc;
        add( bc = new BorderContainer(), TapBTranslate.text( "preview" ) );
        bc.add( ptv, BorderContainer.CENTER );
        activeModulePanel = null;
        ptv.initialize();
        setSelectedTab( getChildCount() - 1 );
        layoutChildren();
        procedure.notifyMinorChange();
    }


    /**
     *  Description of the Method
     */
    public void doAddHSplitModuleView()
    {
        addView( new TapModulePanel( this ), true );
    }


    /**
     *  Description of the Method
     */
    public void doAddHSplitParamView()
    {
        addView( new TapParametersPanel( this ), true );
    }


    /**
     *  Description of the Method
     */
    public void doAddHSplitPreviewView()
    {
        addView( new PreviewTapView( this ), true );
    }


    /**
     *  Adds a new view in a split pane
     *
     *@param  view        The view to add
     *@param  horizontal  Whether the split pane is horizontal (true) or
     *      vertical (false).
     */
    public void addView( TapView view, boolean horizontal )
    {
        Widget newWidget;
        if ( view.needsScrollPane() )
        {
            ( (Widget) view ).addEventLink( KeyPressedEvent.class, this, "doKeyPressed" );
            ( (Widget) view ).addEventLink( KeyReleasedEvent.class, this, "doKeyReleased" );
            newWidget = (Widget) view.newScrollPane();
        }
        else
            newWidget = (Widget) view;
        newWidget.addEventLink( KeyPressedEvent.class, this, "doKeyPressed" );
        newWidget.addEventLink( KeyReleasedEvent.class, this, "doKeyReleased" );
        BorderContainer bc = (BorderContainer) getChild( getSelectedTab() );
        WidgetContainer wc = (WidgetContainer) bc.getChild( BorderContainer.CENTER );
        bc.remove( wc );
        BSplitPane curSplit = new BSplitPane( horizontal ? BSplitPane.HORIZONTAL : BSplitPane.VERTICAL );
        bc.add( curSplit, BorderContainer.CENTER );
        curSplit.add( wc, 0 );
        register( view );
        curSplit.add( newWidget, 1 );
        curSplit.setOneTouchExpandable( true );
        layoutChildren();
        curSplit.setDividerLocation( 0.5 );
        procedure.notifyMinorChange();
    }


    /**
     *  Description of the Method
     */
    public void doAddVSplitModuleView()
    {
        addView( new TapModulePanel( this ), false );
    }


    /**
     *  Description of the Method
     */
    public void doAddVSplitParamView()
    {
        addView( new TapParametersPanel( this ), false );
    }


    /**
     *  Description of the Method
     */
    public void doAddVSplitPreviewView()
    {
        addView( new PreviewTapView( this ), false );
    }


    /**
     *  Description of the Method
     */
    public void doRemoveTab()
    {
        if ( getChildCount() == 1 )
        {
            JOptionPane.showMessageDialog( null, TapDesignerTranslate.text( "cannotDeleteLastView" ), TapDesignerTranslate.text( "warning" ), JOptionPane.INFORMATION_MESSAGE );
        }
        else
        {
            remove( getSelectedTab() );
        }
    }


    /**
     *  Description of the Method
     */
    public void doRenameTab()
    {
        String inputValue = JOptionPane.showInputDialog( TapDesignerTranslate.text( "tabName" ), getTabName( getSelectedTab() ) );
        if ( inputValue != null && !inputValue.equals( "" ) )
        {
            setTabName( getSelectedTab(), inputValue );
            procedure.notifyMinorChange();
        }
    }


    /**
     *  Called by a view when minor changes have been brought to the procedure :
     *  new links, new names... This method allows other views to keep sync.
     *
     *@param  view  The calling view
     */
    public void minorViewSync( TapView view )
    {
        for ( int i = 0; i < views.size(); ++i )
        {
            if ( view != views.elementAt( i ) )
                ( (TapView) views.elementAt( i ) ).minorSync();
        }
        clearModulesState();
    }


    /**
     *  Description of the Method
     */
    public void clearModulesState()
    {
        Vector modules = procedure.getModules();

        for ( int i = 0; i < modules.size(); ++i )
        {
            ( (TapModule) modules.elementAt( i ) ).updateModuleWindow();
            ( (TapModule) modules.elementAt( i ) ).changed = false;
        }
    }


    /**
     *  Called by a view when major changes occurs to the modules list, like
     *  deletion. This method allows other views to keep sync.
     *
     *@param  view  The calling view
     */
    public void majorViewSync( TapView view )
    {
        for ( int i = 0; i < views.size(); ++i )
        {
            if ( view != views.elementAt( i ) )
            {
                ( (TapView) views.elementAt( i ) ).closeWindows();
                ( (TapView) views.elementAt( i ) ).initialize();
            }
        }
        layoutChildren();
    }


    /**
     *  Called by a view when a module have been added to that view. This method
     *  allows other views to keep sync.
     *
     *@param  view  Description of the Parameter
     */
    protected void syncModuleAddition( TapView view )
    {
        for ( int i = 0; i < views.size(); ++i )
        {
            if ( view != views.elementAt( i ) )
            {
                ( (TapView) views.elementAt( i ) ).syncModuleAddition();
            }
        }
    }


    /**
     *  Sets the activeModulePanel attribute of the TapProcPanel object
     *
     *@param  panel  The new activeModulePanel value
     */
    public void setActiveModulePanel( TapModulePanel panel )
    {
        activeModulePanel = panel;
        requestFocus();
    }


    /**
     *  Called each time the user selects a tab in the tabbed pane.
     */
    public void doSelectedTabChanged()
    {
        WidgetContainer wc = (WidgetContainer) getChild( getSelectedTab() );
        activeModulePanel = recursivelyFindModulePanel( wc );
        if ( activeModulePanel != null )
            activeModulePanel.doSelectionChanged();
    }


    /**
     *  Finds the first child module panel that belongs to a given widget
     *  container
     *
     *@param  wc  The parent widget container
     *@return     The first child TapModulePanel found
     */
    private TapModulePanel recursivelyFindModulePanel( WidgetContainer wc )
    {
        TapModulePanel panel;

        int n = wc.getChildCount();
        if ( n > 0 )
        {
            Object obj;
            Iterator iter = wc.getChildren().iterator();
            while ( iter.hasNext() )
            {
                obj = iter.next();
                if ( obj instanceof TapModulePanel )
                    return (TapModulePanel) obj;
                else if ( obj instanceof WidgetContainer )
                {
                    panel = recursivelyFindModulePanel( (WidgetContainer) obj );
                    if ( panel != null )
                        return panel;
                }
            }
        }
        return null;
    }



    /**
     *  Stores the current layout in the procedure
     */
    public void validateLayout()
    {
        ProcPanelLayout[] layouts = new ProcPanelLayout[getChildCount()];
        for ( int i = 0; i < getChildCount(); ++i )
        {
            BorderContainer bc = (BorderContainer) getChild( i );
            layouts[i] = new ProcPanelLayout( (WidgetContainer) bc.getChild( BorderContainer.CENTER ),
                    getTabName( i ) );
        }
        procedure.setProcPanelLayouts( layouts );
    }

}

