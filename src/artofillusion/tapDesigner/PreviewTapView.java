/*
 *  This TapView show full or partial previews
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

import artofillusion.*;
import artofillusion.object.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import artofillusion.tapDesigner.TapModule.*;



/**
 *  This widget container allows visualization of previews.
 *
 *@author     Francois Guillet
 *@created    14 ao�t 2004
 */
public class PreviewTapView extends BSplitPane implements TapView
{
    private TapProcPanel procPanel;
    private BScrollPane spl;
    private BTree tree;
    private ViewPopup viewPopup;
    private BorderContainer previewContent;
    private boolean upTo;
    private int selectedModule;


    /**
     *  Constructor for the TapParametersPanel object
     *
     *@param  procPanel  the TapProcPanel that holds the parameter panel
     */
    public PreviewTapView( TapProcPanel procPanel )
    {
        super();
        this.procPanel = procPanel;
        BScrollPane sp;
        add( spl = new BScrollPane( tree = new BTree( new DefaultMutableTreeNode( TapBTranslate.text( "fullPreview" ) ) ) ), 0 );
        tree.setRootNodeShown( true );
        tree.setCellRenderer( new ParametersTreeRenderer() );
        previewContent = new BorderContainer();
        LayoutInfo layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.BOTH, new Insets( 0, 0, 0, 0 ), new Dimension( 0, 0 ) );
        previewContent.setDefaultLayout( layout );
        add( previewContent, 1 );
        viewPopup = new ViewPopup( this );
        addEventLink( WidgetMouseEvent.class, this, "doShowViewPopup" );
        tree.addEventLink( WidgetMouseEvent.class, this, "doShowViewPopup" );
        tree.addEventLink( SelectionChangedEvent.class, this, "doSelectionChanged" );
        setOneTouchExpandable( true );
        setResizeWeight( 0 );
        initialize();
    }


    /**
     *  Gets the procPanel to which the view belongs
     *
     *@return    The procPanel value
     */
    @Override
    public TapProcPanel getProcPanel()
    {
        return procPanel;
    }


    /**
     *  Initializes the module parameter tree from the modules names
     */
    @Override
    public void initialize()
    {
        /*
         *  Vector modules = procPanel.getProcedure().getModules();
         *  if ( modules == null )
         *  return;
         *  int count = tree.getChildNodeCount( tree.getRootNode() );
         *  if ( count > 0 )
         *  {
         *  for ( int j = count - 1; j >= 0; --j )
         *  {
         *  tree.removeNode( tree.getChildNode( tree.getRootNode(), j ) );
         *  }
         *  }
         *  TapModule mod = null;
         *  for ( int i = 0; i < modules.size(); ++i )
         *  {
         *  TapModule module = (TapModule) modules.elementAt( i );
         *  if ( module.acceptsPreview() )
         *  addModule( module );
         *  }
         *  resetToPreferredSizes();
         */
        syncModuleTree();
    }


    /**
     *  Adds a module to the tree
     *
     *@param  module  The module to add
     */
    @Override
    public void addModule( TapModule module )
    {
        //check for classname
        int count = tree.getChildNodeCount( tree.getRootNode() );
        TreePath selectedNode = tree.getSelectedNode();
        TreePath classTreePath = null;
        for ( int j = count - 1; j >= 0; --j )
        {
            TreePath tp = tree.getChildNode( tree.getRootNode(), j );
            String className = ( (ModuleTypeInfo) ( (DefaultMutableTreeNode) tp.getLastPathComponent() ).getUserObject() ).toString();
            if ( className.equals( module.getModuleTypeInfo().toString() ) )
                classTreePath = tp;
        }
        if ( classTreePath == null )
            classTreePath = tree.addNode( tree.getRootNode(), new DefaultMutableTreeNode( module.getModuleTypeInfo() ) );
        DefaultMutableTreeNode tn;
        TreePath modulePath = tree.addNode( classTreePath, tn = new DefaultMutableTreeNode( module ) );
        if ( module instanceof ObjectModule )
            tn.setAllowsChildren( false );
        else
        {
            tn = new DefaultMutableTreeNode( TapBTranslate.text( "downTo" ) );
            tree.addNode( modulePath, tn );
            tn.setAllowsChildren( false );
            tn = new DefaultMutableTreeNode( TapBTranslate.text( "upTo" ) );
            tree.addNode( modulePath, tn );
            tn.setAllowsChildren( false );
        }
        ( (DefaultTreeModel) tree.getModel() ).reload();
        setDividerLocation( getChild( 0 ).getPreferredSize().width );
        if ( selectedNode != null )
        {
            ( (JTree) tree.getComponent() ).collapsePath( selectedNode );
            tree.setNodeSelected( selectedNode, true );
            doSelectionChanged( false );
        }

    }


    /**
     *  Synchronizes the module tree with the module list
     */
    private void syncModuleTree()
    {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        TreePath selectedNode = tree.getSelectedNode();
        Vector modules = procPanel.getProcedure().getModules();
        int count = tree.getChildNodeCount( tree.getRootNode() );
        if ( count > 0 )
        {
            for ( int j = count - 1; j >= 0; --j )
            {
                TreePath tp = tree.getChildNode( tree.getRootNode(), j );
                TreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
                int count2 = tree.getChildNodeCount( tp );
                for ( int i = count2 - 1; i >= 0; --i )
                {
                    boolean delete = true;
                    TreePath tp2 = tree.getChildNode( tp, i );
                    Object mod = tp2.getLastPathComponent();
                    model.nodeChanged( (DefaultMutableTreeNode) mod );
                    mod = ( (DefaultMutableTreeNode) mod ).getUserObject();
                    if ( modules != null )
                        for ( int k = 0; k < modules.size(); ++k )
                        {
                            if ( modules.elementAt( k ) == mod )
                                delete = false;
                        }
                    if ( delete )
                        tree.removeNode( tp2 );
                    count2 = tree.getChildNodeCount( tp );
                    if ( count2 == 0 )
                        tree.removeNode( tp );
                }
            }
        }

        if ( modules == null )
            return;
        count = getNumModules();
        if ( count < modules.size() )
        {
            for ( int i = 0; i < modules.size(); ++i )
            {
                boolean add = true;
                int nodeCount = tree.getChildNodeCount( tree.getRootNode() );
                for ( int j = nodeCount - 1; j >= 0; --j )
                {
                    TreePath tp = tree.getChildNode( tree.getRootNode(), j );
                    int count2 = tree.getChildNodeCount( tp );
                    for ( int k = count2 - 1; k >= 0; --k )
                    {
                        TreePath tp2 = tree.getChildNode( tp, k );
                        Object mod = tp2.getLastPathComponent();
                        mod = ( (DefaultMutableTreeNode) mod ).getUserObject();
                        if ( modules.elementAt( i ) == mod )
                            add = false;
                    }
                }
                if ( add )
                {
                    TapModule module = (TapModule) modules.elementAt( i );
                    if ( module.acceptsPreview() )
                        addModule( module );
                }
            }
        }
        setDividerLocation( getChild( 0 ).getPreferredSize().width );
        if ( selectedNode != null )
        {
            ( (JTree) tree.getComponent() ).collapsePath( selectedNode );
            tree.setNodeSelected( selectedNode, true );
            doSelectionChanged( false );
        }

    }


    /**
     *  Gets the number of modules attached to the tree
     *
     *@return    The number of modules
     */
    private int getNumModules()
    {
        int count = tree.getChildNodeCount( tree.getRootNode() );
        if ( count == 0 )
            return 0;
        else
        {
            int num = 0;
            for ( int j = count - 1; j >= 0; --j )
            {
                TreePath tp = tree.getChildNode( tree.getRootNode(), j );
                num += tree.getChildNodeCount( tp );
            }
            return num;
        }
    }


    /**
     *  Closes any window previously created by this widget
     */
    @Override
    public void closeWindows()
    {
        if ( previewContent.getChildCount() > 0 )
            previewContent.remove( BorderContainer.CENTER );
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
     *  Synchronizes the view with the procedure after a module has been added
     */
    @Override
    public void syncModuleAddition()
    {
        syncModuleTree();
    }


    /**
     *  Synchronizes the view after minor edition : new link, link suppressed,
     *  module moved, module renamed...
     */
    @Override
    public void minorSync()
    {
        if ( previewContent.getChildCount() == 0 )
            return;
        Widget w = previewContent.getChild( BorderContainer.CENTER );
        if ( w instanceof TapPreviewWidget )
        {
            TapPreviewWidget previewWidget = (TapPreviewWidget) w;
            Scene oldScene = previewWidget.getScene();
            Scene previewScene = previewWidget.getModule().getPreviewScene( upTo );
            if ( previewScene == null )
            {
                previewContent.remove( BorderContainer.CENTER );
                return;
            }
            ObjectInfo newObj = previewScene.getObject( 2 );
            ObjectInfo oldObj = oldScene.getObject( 2 );
            ( (TapDesignerObjectCollection) oldObj.object ).copyObject( (TapDesignerObjectCollection) newObj.object, false );
            oldObj.object.sceneChanged( oldObj, oldScene );
            oldScene.objectModified( oldObj.object );
            previewWidget.updateImage();
        }
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

        Vector modules = procPanel.getProcedure().getModules();
        int count = tree.getChildNodeCount( tree.getRootNode() );
        if ( count > 0 )
        {
            for ( int j = count - 1; j >= 0; --j )
            {
                TreePath tp = tree.getChildNode( tree.getRootNode(), j );
                TreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
                int count2 = tree.getChildNodeCount( tp );
                for ( int i = count2 - 1; i >= 0; --i )
                {
                    TreePath tp2 = tree.getChildNode( tp, i );
                    Object mod = tp2.getLastPathComponent();
                    model.nodeChanged( (DefaultMutableTreeNode) mod );
                }
            }
        }
        repaint();
    }


    /**
     *  Only here for compatibility with TapView interface. No scrollpane is
     *  needed.
     *
     *@return    Always null
     */
    @Override
    public BScrollPane newScrollPane()
    {
        return null;
    }


    /**
     *  No scroll pane needed, returns false.
     *
     *@return    Always false
     */
    @Override
    public boolean needsScrollPane()
    {
        return false;
    }


    /**
     *  Description of the Method
     */
    public void doSelectionChanged()
    {
        doSelectionChanged( true );
    }


    /**
     *  This method is called upon a selection change in the modules tree
     *
     *@param  userEvent  Description of the Parameter
     */
    public void doSelectionChanged( boolean userEvent )
    {
        if ( userEvent )
            procPanel.getProcedure().notifyMinorChange();
        previewContent.remove( BorderContainer.CENTER );
        selectedModule = -1;
        TreePath tp = tree.getSelectedNode();
        if ( tp == null )
            return;
        Object obj = tp.getLastPathComponent();
        Object userObj = ( (DefaultMutableTreeNode) obj ).getUserObject();
        if ( userObj instanceof TapModule )
        {
            TapModule module = (TapModule) ( userObj );
            selectedModule = procPanel.getProcedure().getModuleIndex( module );
            upTo = false;
            if ( module.isMainEntry() )
                upTo = true;
            Scene previewScene = module.getPreviewScene( false );
            if ( previewScene != null )
            {
                Widget previewWidget = new TapPreviewWidget( previewScene, module );
                previewContent.add( previewWidget, BorderContainer.CENTER );
            }
            layoutChildren();
            repaint();
        }
        else
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) ( (DefaultMutableTreeNode) obj ).getParent();
            if ( node != null )
            {
                if ( node.getUserObject() instanceof TapModule )
                {
                    TapModule module = (TapModule) ( node.getUserObject() );
                    selectedModule = procPanel.getProcedure().getModuleIndex( module );
                    upTo = ( (String) userObj ).equals( TapBTranslate.text( "upTo" ) );
                    Scene previewScene = module.getPreviewScene( upTo );
                    if ( previewScene != null )
                    {
                        Widget previewWidget = new TapPreviewWidget( previewScene, module );
                        previewContent.add( previewWidget, BorderContainer.CENTER );

                    }
                    layoutChildren();
                    repaint();
                }
                else
                {
                    previewContent.remove( BorderContainer.CENTER );
                    layoutChildren();
                    repaint();
                }
            }
            else
            {
                Vector modules = procPanel.getProcedure().getModules();
                if ( modules == null )
                    return;
                TapModule mod = null;
                for ( int i = 0; i < modules.size(); ++i )
                {
                    if ( ( (TapModule) modules.elementAt( i ) ).isMainEntry() )
                    {
                        TapModule module = (TapModule) (TapModule) modules.elementAt( i );
                        upTo = false;
                        Scene previewScene = module.getPreviewScene( upTo );
                        if ( previewScene != null )
                        {
                            Widget previewWidget = new TapPreviewWidget( previewScene, module );
                            previewContent.add( previewWidget, BorderContainer.CENTER );

                        }
                        layoutChildren();
                        repaint();
                    }
                }
            }
        }

    }


    /**
     *  Sets the layoutData attribute of the TapView object
     *
     *@param  data  The new layoutData value
     */
    @Override
    public void setLayoutData( ProcPanelLayoutData data )
    {
        if ( ( !( data instanceof PreviewLayoutData ) ) || ( data == null ) )
        {
            selectedModule = -1;
            upTo = false;
            return;
        }
        else
        {
            selectedModule = ( (PreviewLayoutData) data ).moduleNumber;
            upTo = ( (PreviewLayoutData) data ).upTo;
            if ( selectedModule == -1 )
            {
                tree.setNodeSelected( tree.getRootNode(), true );
                doSelectionChanged( false );
            }
            Vector modules = procPanel.getProcedure().getModules();
            if ( selectedModule < 0 || selectedModule >= modules.size() )
            {
                tree.setNodeSelected( tree.getRootNode(), true );
                doSelectionChanged( false );
                return;
            }

            TapModule module = (TapModule) modules.elementAt( selectedModule );
            int count = tree.getChildNodeCount( tree.getRootNode() );
            TreePath classTreePath = null;
            for ( int j = count - 1; j >= 0; --j )
            {
                TreePath tp = tree.getChildNode( tree.getRootNode(), j );
                String className = ( (ModuleTypeInfo) ( (DefaultMutableTreeNode) tp.getLastPathComponent() ).getUserObject() ).toString();
                if ( className.equals( module.getModuleTypeInfo().toString() ) )
                    classTreePath = tp;
            }
            if ( classTreePath == null )
                return;
            count = tree.getChildNodeCount( classTreePath );
            for ( int j = count - 1; j >= 0; --j )
            {
                TreePath tp = tree.getChildNode( classTreePath, j );
                Object obj = tp.getLastPathComponent();
                Object userObj = ( (DefaultMutableTreeNode) obj ).getUserObject();
                if ( userObj == module )
                {
                    if ( module instanceof ObjectModule )
                        tree.setNodeSelected( tp, true );
                    else
                    {
                        if ( upTo )
                            tree.setNodeSelected( tree.getChildNode( tp, 1 ), true );
                        else
                            tree.setNodeSelected( tree.getChildNode( tp, 0 ), true );
                    }
                    doSelectionChanged( false );
                }
            }
        }
    }


    /**
     *  Gets the layoutData attribute of the TapView object
     *
     *@return    The layoutData value
     */
    @Override
    public ProcPanelLayoutData getLayoutData()
    {
        return new PreviewLayoutData( selectedModule, upTo );
    }


    /**
     *@author     Fran�ois Guillet
     *@created    15 ao�t 2004
     */
    public static class PreviewLayoutData extends ProcPanelLayoutData
    {
        /**
         *  Description of the Field
         */
        protected int moduleNumber;
        /**
         *  Description of the Field
         */
        protected boolean upTo;


        /**
         *  Constructor for the ProcPanelLayoutData object
         *
         *@param  in                          Description of the Parameter
         *@exception  IOException             Description of the Exception
         *@exception  InvalidObjectException  Description of the Exception
         */
        public PreviewLayoutData( DataInputStream in )
            throws IOException, InvalidObjectException
        {
            super( in );
            short version = in.readShort();
            if ( ( version < 0 ) || ( version > 0 ) )
                throw new InvalidObjectException( "" );

            upTo = in.readBoolean();
            moduleNumber = in.readInt();
        }


        /**
         *  Constructor for the PreviewLayoutData object
         *
         *@param  moduleNumber  Description of the Parameter
         *@param  upTo          Description of the Parameter
         */
        protected PreviewLayoutData( int moduleNumber, boolean upTo )
        {
            this.moduleNumber = moduleNumber;
            this.upTo = upTo;
        }


        /**
         *  Description of the Method
         *
         *@param  out              Description of the Parameter
         *@exception  IOException  Description of the Exception
         */
        @Override
        public void writeToFile( DataOutputStream out )
            throws IOException
        {
            out.writeShort( 0 );
            out.writeBoolean( upTo );
            out.writeInt( moduleNumber );
        }
    }

}

