/*
 *  Copyright (C) 2004 by Francois Guillet. Some parts taken from LayoutWindow.java by
 *  Peter Eastman.
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
import artofillusion.object.*;
import artofillusion.ui.*;
import artofillusion.*;
import java.awt.*;
import javax.swing.BorderFactory;
import buoy.event.*;
import buoy.widget.*;


/**
 *  The LayoutWindow class represents the main window for creating and laying
 *  out scenes.
 *
 *@author     pims
 *@created    27 mai 2004
 */

public class TapPreviewWidget extends BorderContainer implements EditingWindow
{
    private SceneViewer theView;
    private Scene theScene;
    private TapModule module;
    private ToolPalette tools;
    private EditingTool defaultTool, currentTool;
    private BRadioButton displayItem[];


    /*
     *  @param  s       Description of the Parameter
     *  @param  module  Description of the Parameter
     *  @param  w       Description of the Parameter
     */
    /**
     *  Constructor for the TapPreviewWidget object
     *
     *@param  s       Description of the Parameter
     *@param  module  Description of the Parameter
     */
    public TapPreviewWidget( Scene s, TapModule module )
    {
        super();
        setCornersAreVertical( true );
        setBackground( ThemeManager.getAppBackgroundColor() );
        RowContainer row = new RowContainer();
        add( row, BorderContainer.NORTH );
        theScene = s;
        add( theView = new SceneViewer( s, row, this ), BorderContainer.CENTER );
        this.module = module;
        tools = new ToolPalette( 1, 2 );
        BorderContainer bc = new BorderContainer();
        bc.setDefaultLayout( new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 3, 3, 3, 3 ), new Dimension( 0, 0 ) ) );
        bc.add( tools, BorderContainer.NORTH );
        ColumnContainer cc = new ColumnContainer();
        cc.setDefaultLayout( new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets( 3, 3, 3, 3 ), new Dimension( 0, 0 ) ) );
        cc.add( TapBTranslate.bButton( "renderPreview", this, "doRenderPreview" ) );
        cc.add( TapBTranslate.bButton( "setCameraLocation", this, "doCameraLocation" ) );
        cc.add( TapBTranslate.bButton( "grids", this, "doGrids" ) );
        ColumnContainer subcc = new ColumnContainer();
        subcc.setDefaultLayout( new LayoutInfo( LayoutInfo.WEST, LayoutInfo.NONE, new Insets( 2, 2, 2, 2 ), new Dimension( 0, 0 ) ) );
        displayItem = new BRadioButton[4];

        RadioButtonGroup rbg = new RadioButtonGroup();
        subcc.add( displayItem[0] = TapBTranslate.bRadioButton( "wireframeDisplay", theView.getRenderMode() == ViewerCanvas.RENDER_WIREFRAME, rbg ) );
        displayItem[0].addEventLink( ValueChangedEvent.class, this, "displayModeCommand" );
        subcc.add( displayItem[1] = TapBTranslate.bRadioButton( "shadedDisplay", theView.getRenderMode() == ViewerCanvas.RENDER_FLAT, rbg ) );
        displayItem[1].addEventLink( ValueChangedEvent.class, this, "displayModeCommand" );
        subcc.add( displayItem[2] = TapBTranslate.bRadioButton( "smoothDisplay", theView.getRenderMode() == ViewerCanvas.RENDER_SMOOTH, rbg ) );
        displayItem[2].addEventLink( ValueChangedEvent.class, this, "displayModeCommand" );
        subcc.add( displayItem[3] = TapBTranslate.bRadioButton( "texturedDisplay", theView.getRenderMode() == ViewerCanvas.RENDER_TEXTURED, rbg ) );
        displayItem[3].addEventLink( ValueChangedEvent.class, this, "displayModeCommand" );
        cc.add( new BOutline( subcc, BorderFactory.createRaisedBevelBorder() ) );

        bc.add( cc, BorderContainer.SOUTH );

        add( bc, BorderContainer.WEST );
        EditingTool metaTool;
        EditingTool altTool;
        tools.addTool( metaTool = new MoveViewTool( this ) );
        tools.addTool( altTool = new RotateViewTool( this ) );
        ( (RotateViewTool) altTool ).setUseSelectionCenter( true );
        defaultTool = metaTool;
        tools.selectTool( defaultTool );
        theView.setTool( defaultTool );
        theView.setMetaTool( metaTool );
        theView.setAltTool( altTool );
        displayItem[0].setState( theView.getRenderMode() == ViewerCanvas.RENDER_WIREFRAME );
        displayItem[1].setState( theView.getRenderMode() == ViewerCanvas.RENDER_FLAT );
        displayItem[2].setState( theView.getRenderMode() == ViewerCanvas.RENDER_SMOOTH );
        displayItem[3].setState( theView.getRenderMode() == ViewerCanvas.RENDER_TEXTURED );

    }

    public void setModified()
    {

    }

    public ToolPalette getToolPalette()
    {
        return tools;
    }

    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    private void displayModeCommand( ValueChangedEvent ev )
    {
        Widget source = ev.getWidget();
        if ( source == displayItem[0] )
            theView.setRenderMode( ViewerCanvas.RENDER_WIREFRAME );
        else if ( source == displayItem[1] )
            theView.setRenderMode( ViewerCanvas.RENDER_FLAT );
        else if ( source == displayItem[2] )
            theView.setRenderMode( ViewerCanvas.RENDER_SMOOTH );
        else if ( source == displayItem[3] )
            theView.setRenderMode( ViewerCanvas.RENDER_TEXTURED );
        updateImage();
    }


    /**
     *  Description of the Method
     */
    public void doRenderPreview()
    {
        new RenderSetupDialog( TapUtils.getParentBFrame( this ), theScene );
    }


    /**
     *  Description of the Method
     */
    public void doGrids()
    {
        ValueField spaceField = new ValueField( theScene.getGridSpacing(), ValueField.POSITIVE );
        ValueField divField = new ValueField( theScene.getGridSubdivisions(), ValueField.POSITIVE + ValueField.INTEGER );
        BCheckBox showBox = new BCheckBox( Translate.text( "showGrid" ), theScene.getShowGrid() );
        BCheckBox snapBox = new BCheckBox( Translate.text( "snapToGrid" ), theScene.getSnapToGrid() );
        ComponentsDialog dlg = new ComponentsDialog( TapUtils.getParentBFrame( this ), Translate.text( "gridTitle" ),
                new Widget[]{spaceField, divField, showBox, snapBox},
                new String[]{Translate.text( "gridSpacing" ), Translate.text( "snapToSubdivisions" ), null, null} );
        if ( !dlg.clickedOk() )
            return;
        theScene.setGridSpacing( spaceField.getValue() );
        theScene.setGridSubdivisions( (int) divField.getValue() );
        theScene.setShowGrid( showBox.getState() );
        theScene.setSnapToGrid( snapBox.getState() );
        theView.setGrid( theScene.getGridSpacing(), theScene.getGridSubdivisions(), theScene.getShowGrid(), theScene.getSnapToGrid() );
        updateImage();
    }


    /**
     *  Description of the Method
     */
    private void doCameraLocation()
    {
        int i;
        int objRef;
        TransformDialog dlg;
        ObjectInfo obj;
        Vec3 orig;
        Vec3 size;
        double angles[];
        double values[];

        obj = null;
        objRef = 0;
        setUndoRecord( new UndoRecord( this, false ) );
        for ( i = 0; i < theScene.getNumObjects(); i++ )
        {
            if ( theScene.getObject( i ).object instanceof SceneCamera )
            {
                objRef = i;
                obj = theScene.getObject( objRef );
            }
        }
        if ( obj == null )
            return;
        orig = obj.coords.getOrigin();
        angles = obj.coords.getRotationAngles();
        size = obj.object.getBounds().getSize();
        dlg = new TransformDialog( TapUtils.getParentBFrame( this ), Translate.text( "objectLayoutTitle", theScene.getObject( objRef ).name ),
                new double[]{orig.x, orig.y, orig.z, angles[0], angles[1], angles[2],
                size.x, size.y, size.z}, false, false );
        if ( !dlg.clickedOk() )
            return;
        values = dlg.getValues();
        if ( !Double.isNaN( values[0] ) )
            orig.x = values[0];
        if ( !Double.isNaN( values[1] ) )
            orig.y = values[1];
        if ( !Double.isNaN( values[2] ) )
            orig.z = values[2];
        if ( !Double.isNaN( values[3] ) )
            angles[0] = values[3];
        if ( !Double.isNaN( values[4] ) )
            angles[1] = values[4];
        if ( !Double.isNaN( values[5] ) )
            angles[2] = values[5];
        if ( !Double.isNaN( values[6] ) )
            size.x = values[6];
        if ( !Double.isNaN( values[7] ) )
            size.y = values[7];
        if ( !Double.isNaN( values[8] ) )
            size.z = values[8];
        obj.coords.setOrigin( orig );
        obj.coords.setOrientation( angles[0], angles[1], angles[2] );
        obj.object.setSize( size.x, size.y, size.z );
        theScene.objectModified( obj.object );
        obj.object.sceneChanged( obj, theScene );
        updateImage();
    }


    /**
     *  Gets the scene attribute of the TapPreviewWindow object
     *
     *@return    The scene value
     */
    public Scene getScene()
    {
        return theScene;
    }


    /**
     *  Constructor for the getModule object
     *
     *@return    The module value
     */
    public TapModule getModule()
    {
        return module;
    }


    /**
     *  Set the currently selected EditingTool.
     *
     *@param  tool  The new tool value
     */

    public void setTool( EditingTool tool )
    {
        theView.setTool( tool );
        currentTool = tool;
    }


    /**
     *  Set the text to display at the bottom of the window.
     *
     *@param  text  The new helpText value
     */

    public void setHelpText( String text )
    {

    }


    /**
     *  Get the BFrame for this EditingWindow: either the EditingWindow itself
     *  if it is a BFrame, or its parent if it is a BDialog.
     *
     *@return    The frame value
     */

    public BFrame getFrame()
    {
        return TapUtils.getParentBFrame( this );
    }


    /**
     *  Update the image displayed in this window.
     */

    public void updateImage()
    {
        if ( ! theView.getComponent().isShowing() )
            return;
        theView.copyOrientationFromCamera();
        //theView.repaint();
        theView.updateImage();
        theView.repaint();
    }


    /**
     *  Update which menus are enabled.
     */

    public void updateMenus()
    {

    }


    /**
     *  Set the current UndoRecord for this EditingWindow.
     *
     *@param  command  The new undoRecord value
     */

    public void setUndoRecord( UndoRecord command )
    {

    }


    /**
     *  Get the ViewerCanvas in which editing is taking place. This may return
     *  null if there is no ViewerCanvas.
     *
     *@return    The view value
     */

    public ViewerCanvas getView()
    {
        return null;
    }


    /**
     *  Confirm whether this window should be closed (possibly by displaying a
     *  message to the user), and then close it. If the closing is canceled,
     *  this should return false.
     *
     *@return    Description of the Return Value
     */

    public boolean confirmClose()
    {
        return true;
    }

}

