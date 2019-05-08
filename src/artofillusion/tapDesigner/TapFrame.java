/*
 *  This frame allows the user to design plants and trees made of several template objects. It is
 *  only a placeholder for a TapProcPanel in which all events are taken care of. Only TaPD object interaction with AoI scene
 *  is addressed here
 */
/*
 *  Copyright 2003 Francois Guillet
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
import artofillusion.animation.*;
import artofillusion.math.*;
import artofillusion.object.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

//}}}

/**
 *  This frame allows the user to design a Tree or a Plant object. Its function
 *  is to edit a TaPD procedure.
 *
 *@author     Francois Guillet
 *@created    14 mars 2004
 */
public class TapFrame extends BFrame implements TapProcPanelHolder
{
    //{{{ Variables
    private LayoutWindow window;
    private BMenuBar theMenuBar;
    private BMenu editMenu;
    private BMenu moduleMenu;
    private BMenu insertMenu;
    private BMenu sceneMenu;
    private BMenu optionsMenu;
    private BMenuItem aoiObjItem;
    private BMenuItem pasteAsObjectItem;
    private BMenuItem pasteAsPlantItem;
    private BMenuItem undoItem;
    private BMenuItem redoItem;
    private BMenuItem cutItem;
    private BMenuItem copyItem;
    private BMenuItem clearItem;
    private BMenuItem pasteItem;
    private BMenuItem renameItem;
    private BMenuItem mainEntryItem;
    private BMenuItem manageObjectsItem;
    private BMenuItem[] layoutItem = new BMenuItem[4];
    private BMenuItem[] plafItems;
    private BButton aoiObjButton;
    private BButton leafButton;
    private TapProcPanel procPanel;
    private BButton okButton;
    private BButton cancelButton;
    private AWTWidget toolbarWidget;
    private UIManager.LookAndFeelInfo[] plafInfo;
    private Vector visualModules;
    private Scene theScene;
    private ObjectInfo tapdObject;
    private ObjectInfo savedTapdObject;



    //{{{ TapFrame constructor
    /**
     *  Constructor for the TapFrame object
     *
     *@param  window        The LayoutWinodow that triggered the editing process
     *@param  editedObject  The object to edit
     */
    public TapFrame( LayoutWindow window, ObjectInfo editedObject )
    {
        super( "" );

        TapProcedure procedure = null;

        theScene = window.getScene();
        this.window = window;

        this.setTitle( TapBTranslate.text( "tapDesignerTitle" ) );

        if ( editedObject != null )
        {
            if ( editedObject.object instanceof TapDesignerObjectCollection )
            {
                savedTapdObject = editedObject;
                //System.out.println( "Object given by scene :" + editedObject );
                tapdObject = editedObject.duplicate();
                tapdObject.object = editedObject.object.duplicate();

                ( (TapDesignerObjectCollection) tapdObject.object ).getProcedure()
                        .setModified( false, (TapDesignerObjectCollection) tapdObject.object );

                TapDesignerObjectCollection collection = (TapDesignerObjectCollection) tapdObject.object;
                procedure = collection.getProcedure();
                procedure.setModified( false, (TapDesignerObjectCollection) tapdObject.object );
            }
        }
        else
        {
            procedure = importSelectedObjectsFromScene( true );
        }

        procedure.setWindow( window );
        initComponents( procedure );

        if ( editedObject != null )
        {
            okButton.setEnabled( true );
            procPanel.repaint();
            pasteAsObjectItem.setEnabled( true );
            pasteAsPlantItem.setEnabled( true );
        }

        addEventLink( WindowClosingEvent.class, this, "exitDesigner" );

        if ( procedure.getNumObjects() < 1 )
        {
            aoiObjButton.setEnabled( false );
            leafButton.setEnabled( false );
            aoiObjItem.setEnabled( false );
            manageObjectsItem.setEnabled( false );
        }
        else
        {
            leafButton.setEnabled( false );
            for ( int i = 0; i < procedure.getNumObjects(); ++i )
            {
                ObjectInfo info = procedure.getScene().getObject( i );
                if ( info.object instanceof SplineMesh )
                {
                    leafButton.setEnabled( true );
                    break;
                }
            }
        }
    }


    //}}}

    //{{{ Components initialization
    /**
     *  Initialization of the frame widgets
     *
     *@param  procedure  TaPD procedure displayed in the frame
     */
    private void initComponents( TapProcedure procedure )
    {
        procPanel = new TapProcPanel( procedure, (TapProcPanelHolder) this );

        theMenuBar = new BMenuBar();
        editMenu = TapBTranslate.bMenu( "edit" );
        moduleMenu = TapBTranslate.bMenu( "module" );
        insertMenu = TapBTranslate.bMenu( "insert" );
        sceneMenu = TapBTranslate.bMenu( "scene" );
        optionsMenu = TapBTranslate.bMenu( "options" );

        BorderContainer content = new BorderContainer();
        BorderContainer frameContent = new BorderContainer();
        JToolBar toolbar = new JToolBar();
        aoiObjButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/aoi.png" ) ) );
        aoiObjButton.addEventLink( CommandEvent.class, procPanel, "addAoIObject" );
        aoiObjButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "aoiObjectTooltip" ) ) );
        aoiObjButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aoiObjButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aoiObjButton.getComponent() );

        leafButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/leaf.png" ) ) );
        leafButton.addEventLink( CommandEvent.class, procPanel, "addLeaf" );
        leafButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "leafTooltip" ) ) );
        leafButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        leafButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( leafButton.getComponent() );

        BButton aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/spline.png" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addSpline" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "splineTooltip" ) ) );
        toolbar.add( aButton.getComponent() );

        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/tube.png" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addTube" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "tubeTooltip" ) ) );
        toolbar.add( aButton.getComponent() );

        toolbar.addSeparator();

        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/coil.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "coilTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addCoil" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );
        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/golden.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "goldenTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addGoldenBall" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );

        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/field.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "fieldTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addField" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );

        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/top.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "topTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addTop" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );
        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/distort.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "distortTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addDistort" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );
        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/randobj.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "randObjTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addRandomObject" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );
        toolbar.addSeparator();
        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/value.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "valueTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addValue" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );
        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/function.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "functionTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addFunction" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );
        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/binary.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "binaryOpTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addBinaryOperation" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );
        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/unary.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "unaryOpTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addUnaryOperation" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );
        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/scale.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "scaleShiftTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addScaleShift" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );
        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/clip.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "clipTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addClip" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );
        aButton = new BButton( new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/randval.png" ) ) );
        aButton.addEventLink( ToolTipEvent.class, new BToolTip( TapBTranslate.text( "randomValueTooltip" ) ) );
        aButton.addEventLink( CommandEvent.class, procPanel, "addRandomValue" );
        aButton.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        aButton.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        toolbar.add( aButton.getComponent() );

        LayoutInfo layout = new LayoutInfo();
        layout.setAlignment( LayoutInfo.NORTHWEST );
        layout.setFill( LayoutInfo.HORIZONTAL );
        frameContent.add( toolbarWidget = new AWTWidget( toolbar ), BorderContainer.NORTH, layout );
        frameContent.add( content, BorderContainer.CENTER );
        setContent( frameContent );

        BOutline outline = new BOutline( procPanel, BorderFactory.createRaisedBevelBorder() );
        content.add( outline, BorderContainer.CENTER );
        RowContainer rowContainer = new RowContainer();
        okButton = TapBTranslate.bButton( "ok", this, "doOK" );
        okButton.setEnabled( false );
        layout = new LayoutInfo();
        layout.setInsets( new Insets( 5, 5, 5, 5 ) );
        layout.setFill( LayoutInfo.NONE );
        layout.setAlignment( LayoutInfo.CENTER );
        rowContainer.add( okButton, layout );
        cancelButton = TapBTranslate.bButton( "cancel", this, "doCancel" );
        rowContainer.add( cancelButton, layout );

        BorderContainer buttonContainer = new BorderContainer();
        buttonContainer.add( rowContainer, BorderContainer.EAST );

        toolbar.addSeparator();
        toolbar.add( okButton.getComponent() );
        toolbar.addSeparator();
        toolbar.add( cancelButton.getComponent() );
        toolbar.setBorderPainted( true );
        toolbarWidget.addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        toolbarWidget.addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );

        editMenu.add( undoItem = TapBTranslate.bMenuItem( "undo", procPanel, "doUndo" ) );
        editMenu.add( redoItem = TapBTranslate.bMenuItem( "redo", procPanel, "doRedo" ) );
        undoItem.setEnabled( false );
        redoItem.setEnabled( false );
        editMenu.addSeparator();
        editMenu.add( cutItem = TapBTranslate.bMenuItem( "cut", procPanel, "doCut" ) );
        editMenu.add( copyItem = TapBTranslate.bMenuItem( "copy", procPanel, "doCopy" ) );
        editMenu.add( pasteItem = TapBTranslate.bMenuItem( "paste", procPanel, "doPaste" ) );
        editMenu.add( clearItem = TapBTranslate.bMenuItem( "clear", procPanel, "doClear" ) );
        editMenu.addSeparator();
        editMenu.add( TapBTranslate.bMenuItem( "editUndoLevel", procPanel, "doEditUndoLevel" ) );
        editMenu.addSeparator();
        editMenu.add( TapBTranslate.bMenuItem( "quit", this, "exitDesigner" ) );
        theMenuBar.add( editMenu );

        moduleMenu.add( renameItem = TapBTranslate.bMenuItem( "rename", procPanel, "doRename" ) );
        moduleMenu.add( mainEntryItem = TapBTranslate.bMenuItem( "mainEntry", procPanel, "doSelectMainEntry" ) );
        moduleMenu.add( TapBTranslate.bMenuItem( "seed", procPanel, "doChooseSeed" ) );
        moduleMenu.addSeparator();

        BMenu tmp = TapBTranslate.bMenu( "layout" );
        tmp.add( layoutItem[0] = TapBTranslate.bMenuItem( "left_to_right", procPanel, "doLeftToRight" ) );
        tmp.add( layoutItem[1] = TapBTranslate.bMenuItem( "top_to_bottom", procPanel, "doTopToBottom" ) );
        tmp.add( layoutItem[2] = TapBTranslate.bMenuItem( "right_to_left", procPanel, "doRightToLeft" ) );
        tmp.add( layoutItem[3] = TapBTranslate.bMenuItem( "bottom_to_top", procPanel, "doBottomToTop" ) );
        moduleMenu.add( tmp );
        theMenuBar.add( moduleMenu );

        tmp = TapBTranslate.bMenu( "objectMenu" );
        tmp.add( aoiObjItem = TapBTranslate.bMenuItem( "object", procPanel, "addAoIObject" ) );
        tmp.add( aoiObjItem = TapBTranslate.bMenuItem( "leaf", procPanel, "addLeaf" ) );
        tmp.add( TapBTranslate.bMenuItem( "spline", procPanel, "addSpline" ) );
        tmp.add( TapBTranslate.bMenuItem( "tube", procPanel, "addTube" ) );
        insertMenu.add( tmp );
        tmp = TapBTranslate.bMenu( "valueMenu" );
        tmp.add( TapBTranslate.bMenuItem( "constantValue", procPanel, "addValue" ) );
        tmp.add( TapBTranslate.bMenuItem( "function", procPanel, "addFunction" ) );
        tmp.add( TapBTranslate.bMenuItem( "binaryOperation", procPanel, "addBinaryOperation" ) );
        tmp.add( TapBTranslate.bMenuItem( "unaryOperation", procPanel, "addUnaryOperation" ) );
        tmp.add( TapBTranslate.bMenuItem( "scaleShift", procPanel, "addScaleShift" ) );
        tmp.add( TapBTranslate.bMenuItem( "clip", procPanel, "addClip" ) );
        insertMenu.add( tmp );
        tmp = TapBTranslate.bMenu( "geometryMenu" );
        tmp.add( TapBTranslate.bMenuItem( "coil", procPanel, "addCoil" ) );
        tmp.add( TapBTranslate.bMenuItem( "goldenBall", procPanel, "addGoldenBall" ) );
        tmp.add( TapBTranslate.bMenuItem( "field", procPanel, "addField" ) );
        tmp.add( TapBTranslate.bMenuItem( "top", procPanel, "addTop" ) );
        tmp.add( TapBTranslate.bMenuItem( "distort", procPanel, "addDistort" ) );
        insertMenu.add( tmp );
        theMenuBar.add( insertMenu );

        sceneMenu.add( TapBTranslate.bMenuItem( "importFromScene", this, "importFromScene" ) );
        sceneMenu.add( TapBTranslate.bMenuItem( "importFromTaPDObject", this, "importFromTaPDObject" ) );
        sceneMenu.addSeparator();
        sceneMenu.add( pasteAsObjectItem = TapBTranslate.bMenuItem( "pasteAsObject", this, "pasteAsObjects" ) );
        pasteAsObjectItem.setEnabled( true );
        sceneMenu.add( pasteAsPlantItem = TapBTranslate.bMenuItem( "pasteAsPlant", this, "pasteAsPlantCommand" ) );
        pasteAsPlantItem.setEnabled( false );
        sceneMenu.addSeparator();
        sceneMenu.add( TapBTranslate.bMenuItem( "editViewLevel", this, "editViewLevel" ) );
        sceneMenu.add( TapBTranslate.bMenuItem( "editRenderingLevel", this, "editRenderingLevel" ) );
        sceneMenu.addSeparator();
        sceneMenu.add( TapBTranslate.bMenuItem( "textures", procPanel, "manageTextures" ) );
        sceneMenu.add( TapBTranslate.bMenuItem( "importTextures", procPanel, "importTextures" ) );
        sceneMenu.add( TapBTranslate.bMenuItem( "materials", procPanel, "manageMaterials" ) );
        sceneMenu.add( TapBTranslate.bMenuItem( "importMaterials", procPanel, "importMaterials" ) );
        sceneMenu.add( TapBTranslate.bMenuItem( "images", procPanel, "manageImages" ) );
        sceneMenu.add( manageObjectsItem = TapBTranslate.bMenuItem( "importedObjects", this, "manageObjects" ) );
        theMenuBar.add( sceneMenu );

        tmp = TapBTranslate.bMenu( "views" );
        BMenu tmp2 = TapBTranslate.bMenu( "addTabbedView" );
        tmp2.add( TapBTranslate.bMenuItem( "modulesView", procPanel, "doAddTabbedModuleView" ) );
        tmp2.add( TapBTranslate.bMenuItem( "parametersView", procPanel, "doAddTabbedParamView" ) );
        tmp2.add( TapBTranslate.bMenuItem( "previewView", procPanel, "doAddTabbedPreviewView" ) );
        tmp.add( tmp2 );
        tmp.addSeparator();
        tmp2 = TapBTranslate.bMenu( "addHSplit" );
        tmp2.add( TapBTranslate.bMenuItem( "modulesView", procPanel, "doAddHSplitModuleView" ) );
        tmp2.add( TapBTranslate.bMenuItem( "parametersView", procPanel, "doAddHSplitParamView" ) );
        tmp2.add( TapBTranslate.bMenuItem( "previewView", procPanel, "doAddHSplitPreviewView" ) );
        tmp.add( tmp2 );
        tmp2 = TapBTranslate.bMenu( "addVSplit" );
        tmp2.add( TapBTranslate.bMenuItem( "modulesView", procPanel, "doAddVSplitModuleView" ) );
        tmp2.add( TapBTranslate.bMenuItem( "parametersView", procPanel, "doAddVSplitParamView" ) );
        tmp2.add( TapBTranslate.bMenuItem( "previewView", procPanel, "doAddVSplitPreviewView" ) );
        tmp.add( tmp2 );
        tmp.addSeparator();
        tmp.add( TapBTranslate.bMenuItem( "removeTab", procPanel, "doRemoveTab" ) );
        tmp.add( TapBTranslate.bMenuItem( "renameTab", procPanel, "doRenameTab" ) );
        theMenuBar.add( tmp );

        setMenuBar( theMenuBar );
        pack();

        if ( procPanel.getProcedure().getBounds() == null )
        {
            Rectangle rect = window.getBounds();
            rect.width = ( new Double( rect.width * 0.7 ) ).intValue();
            rect.height = ( new Double( rect.height * 0.7 ) ).intValue();
            setBounds( rect );
        }
        else
            setBounds( procPanel.getProcedure().getBounds() );

        setVisible( true );
        layoutChildren();
        addEventLink( KeyPressedEvent.class, procPanel, "doKeyPressed" );
        addEventLink( KeyReleasedEvent.class, procPanel, "doKeyReleased" );
        addEventLink( WindowResizedEvent.class, this, "doResized" );
        requestFocus();
    }


    //}}}

    //{{{ Ok button clicked
    /**
     *  Description of the Method
     */
    private void doOK()
    {
        procPanel.validateLayout();
        procPanel.getProcedure().setBounds( getBounds() );
        if ( tapdObject == null )
        {
            tapdObject = pasteAsPlant( true );
        }
        else if ( procPanel.getProcedure().isModified() )
        {
            ObjectInfo info = pasteAsPlant( false );

            if ( info != null )
            {
                ( (TapDesignerObjectCollection) savedTapdObject.object ).copyObject( tapdObject.object, false );
                savedTapdObject.object.sceneChanged( savedTapdObject, theScene );
                theScene.objectModified( savedTapdObject.object );
                window.updateImage();
            }
        }

        exitDesigner();
    }


    //}}}

    //{{{ Cancel button clicked
    /**
     *  Description of the Method
     */
    private void doCancel()
    {
        exitDesigner();
    }


    //}}}

    //{{{ Paste as objects
    /**
     *  Description of the Method
     */
    public void pasteAsObjects()
    {
        TapProcedure procedure = procPanel.getProcedure();
        TapDesignerObjectCollection collection = procedure.getObject();
        ObjectInfo[] objects = collection.getAoIObjects();
        ArtOfIllusion.copyToClipboard( objects, procedure.getScene() );
        window.updateMenus();
    }


    //}}}

    //{{{ Paste as plant
    /**
     *  Description of the Method
     */
    public void pasteAsPlantCommand()
    {
        pasteAsPlant( true );
    }


    /**
     *  Description of the Method
     *
     *@param  duplicate  Description of the Parameter
     *@return            Description of the Return Value
     */
    public ObjectInfo pasteAsPlant( boolean duplicate )
    {
        int k = 1;
        boolean go_on = true;
        String objName = "";
        TapProcedure procedure = procPanel.getProcedure();

        while ( go_on )
        {
            objName = "TaPD " + k;
            go_on = false;

            for ( int i = 0; i < theScene.getNumObjects(); i++ )
            {
                ObjectInfo info = theScene.getObject( i );

                if ( info.name.equals( objName ) )
                {
                    go_on = true;
                }
            }

            ++k;
        }

        System.out.println( "Getting object ... :" + ( new Date() ).toString() );

        TapDesignerObjectCollection newCollection = procedure.getObject();
        System.out.println( "Object size : " + newCollection.size() );
        System.out.println( "Object received :" + ( new Date() ).toString() );

        if ( newCollection == null )
        {
            return null;
        }

        if ( duplicate )
        {
            ObjectInfo newObjectInfo = new ObjectInfo( newCollection, new CoordinateSystem(), objName );
            newObjectInfo.object.setTexture( theScene.getDefaultTexture(), theScene.getDefaultTexture().getDefaultMapping(newObjectInfo.object) );
            newObjectInfo.addTrack( new PositionTrack( newObjectInfo ), 0 );
            newObjectInfo.addTrack( new RotationTrack( newObjectInfo ), 1 );

            UndoRecord undo = new UndoRecord( window, false );
            window.addObject( newObjectInfo, undo );
            window.setUndoRecord( undo );
            window.setSelection( theScene.getNumObjects() - 1 );
            window.updateImage();

            return newObjectInfo;
        }
        else
        {
            tapdObject.object = newCollection;

            return tapdObject;
        }
    }


    //}}}

    //{{{ Resized Event
    /**
     *  Description of the Method
     */
    private void doResized()
    {
        procPanel.getProcedure().notifyMinorChange();
    }

    //}}}

    //{{{ Import TaPD procedure
    /**
     *  Description of the Method
     */
    public void importFromTaPDObject()
    {
        boolean added = false;
        TapProcedure procedure = procPanel.getProcedure();
        for ( int i = 0; i < theScene.getNumObjects(); i++ )
        {
            ObjectInfo info = theScene.getObject( i );

            if ( ( info.object instanceof TapDesignerObjectCollection ) && info.selected )
            {
                procedure.addModulesFromProcedure( ( (TapDesignerObjectCollection) info.object ).getProcedure() );
                procPanel.setNewProcedure( procedure );
                added = true;
            }
        }
        if ( !added )
            return;
        Scene theScene = procedure.getScene();
        for ( int i = 0; i < theScene.getNumObjects(); i++ )
        {
            ObjectInfo info = theScene.getObject( i );

            if ( info.object instanceof Object3D )
            {
                aoiObjButton.setEnabled( true );
                if ( info.object instanceof SplineMesh )
                    leafButton.setEnabled( true );
                aoiObjItem.setEnabled( true );
                manageObjectsItem.setEnabled( true );
            }
        }
    }


    //}}}
    //{{{ Import from scene
    /**
     *  Description of the Method
     */
    public void importFromScene()
    {
        importSelectedObjectsFromScene( false );
    }


    /**
     *  Description of the Method
     *
     *@param  init  Description of the Parameter
     *@return       Description of the Return Value
     */
    public TapProcedure importSelectedObjectsFromScene( boolean init )
    {
        ObjectInfo newInfo;
        double y;
        BoundingBox bounds = null;
        Vector object3DVector = new Vector();
        TapProcedure procedure = null;

        if ( !init )
        {
            procedure = procPanel.getProcedure();
        }

        for ( int i = 0; i < theScene.getNumObjects(); i++ )
        {
            ObjectInfo info = theScene.getObject( i );

            if ( ( info.object instanceof Object3D ) && info.selected )
            {
                newInfo = info.duplicate();
                newInfo.object = info.object.duplicate();
                bounds = info.getBounds();
                y = bounds.getSize().y;
                newInfo.coords.setOrigin( new Vec3( 0, y / 2, 0 ) );

                if ( init )
                {
                    object3DVector.addElement( newInfo );
                }
                else
                {
                    procedure.addObject( newInfo );
                    aoiObjButton.setEnabled( true );
                    if ( info.object instanceof SplineMesh )
                        leafButton.setEnabled( true );
                    aoiObjItem.setEnabled( true );
                    manageObjectsItem.setEnabled( true );
                }
            }
        }

        if ( init )
        {
            procedure = new TapProcedure( object3DVector );
        }

        return procedure;
    }


    //}}}

    //{{{ AoI object management
    /**
     *  Description of the Method
     */
    public void manageObjects()
    {
        procPanel.manageObjects();

        if ( procPanel.getProcedure().getNumObjects() == 0 )
        {
            aoiObjButton.setEnabled( false );
            leafButton.setEnabled( false );
            manageObjectsItem.setEnabled( false );
        }
        else
        {
            leafButton.setEnabled( false );
            for ( int i = 0; i < procPanel.getProcedure().getNumObjects(); ++i )
            {
                ObjectInfo info = procPanel.getProcedure().getScene().getObject( i );
                if ( info.object instanceof SplineMesh )
                {
                    leafButton.setEnabled( true );
                    break;
                }
            }
        }

    }


    //}}}

    //{{{ Edit view levels
    /**
     *  Description of the Method
     */
    public void editViewLevel()
    {
        procPanel.getProcedure().editViewLevel( this );
    }


    /**
     *  Description of the Method
     */
    public void editRenderingLevel()
    {
        procPanel.getProcedure().editRenderingLevel( this );
    }


    //}}}

    //{{{ Getters
    /**
     *  Gets the procedure attribute of the TapFrame object
     *
     *@return    The procedure value
     */
    public TapProcedure getProcedure()
    {
        return procPanel.getProcedure();
    }


    /**
     *  Gets the scene attribute of the TapFrame object
     *
     *@return    The scene value
     */
    public Scene getScene()
    {
        return theScene;
    }


    /**
     *  Gets the bFrame attribute of the TapFrame object
     *
     *@return    The bFrame value
     */
    @Override
    public BFrame getBFrame()
    {
        return (BFrame) this;
    }


    /**
     *  Sets the undoRedoFlags attribute of the TapFrame object
     *
     *@param  canUndo  The new undoRedoFlags value
     *@param  canRedo  The new undoRedoFlags value
     */
    @Override
    public void setUndoRedoFlags( boolean canUndo, boolean canRedo )
    {
        if ( undoItem != null )
        {
            undoItem.setEnabled( canUndo );
            redoItem.setEnabled( canRedo );
        }
    }


    //}}}

    //{{{ Called each time the selection in the proc panel changes
    /**
     *  Description of the Method
     *
     *@param  numSelected  Description of the Parameter
     */
    @Override
    public void selectionChanged( short numSelected )
    {
        switch ( numSelected )
        {
            default:
            case TapProcPanel.NO_SELECTION:
                cutItem.setEnabled( false );
                copyItem.setEnabled( false );
                clearItem.setEnabled( false );

                for ( int i = 0; i < 4; ++i )
                {
                    layoutItem[i].setEnabled( false );
                }

                renameItem.setEnabled( false );
                mainEntryItem.setEnabled( false );

                break;
            case TapProcPanel.SINGLE_SELECTION:
                cutItem.setEnabled( true );
                copyItem.setEnabled( true );
                clearItem.setEnabled( true );

                for ( int i = 0; i < 4; ++i )
                {
                    layoutItem[i].setEnabled( true );
                }

                renameItem.setEnabled( true );
                mainEntryItem.setEnabled( true );

                break;
            case TapProcPanel.MULTIPLE_SELECTION:
                cutItem.setEnabled( true );
                copyItem.setEnabled( true );
                clearItem.setEnabled( true );

                for ( int i = 0; i < 4; ++i )
                {
                    layoutItem[i].setEnabled( true );
                }

                renameItem.setEnabled( false );
                mainEntryItem.setEnabled( false );

                break;
        }
    }


    //}}}

    //{{{ Is the procedure a valid TaPD object ?
    /**
     *  Description of the Method
     *
     *@param  isValid  Description of the Parameter
     */
    @Override
    public void validObject( boolean isValid )
    {
        if ( isValid )
        {
            pasteAsObjectItem.setEnabled( true );
            pasteAsPlantItem.setEnabled( true );
            okButton.setEnabled( true );
        }
        else
        {
            pasteAsObjectItem.setEnabled( false );
            pasteAsPlantItem.setEnabled( false );
            okButton.setEnabled( false );
        }
    }


    //}}}

    //{{{ Quits the designer
    /**
     *  Quits the designer
     */
    public void exitDesigner()
    {
        procPanel.closeWindows();
        dispose();
    }

    //}}}

    //{{{ Themes selection

    // private void doPlafItem( CommandEvent evt )
    // {
    // for ( int i = 0; i < plafItems.length; ++i )
    // {
    // if ( plafItems[i] == evt.getWidget() )
    // {
    // try
    // {
    // UIManager.setLookAndFeel( plafInfo[i].getClassName() );
    // SwingUtilities.updateComponentTreeUI( this.getComponent() );
    // TaPDPreferences.getPreferences().setDefaultTheme( plafInfo[i].getName() );
    // }
    // catch ( Exception e )
    // {
    // e.printStackTrace();
    // }
    // }
    // }
    // }
    //}}}
}

