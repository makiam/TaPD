/*
 *  This widget allows edition of module parameters. It may contain itself any number of splitpanes
 *  showing views or modules parameters
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
import javax.swing.tree.*;

import artofillusion.tapDesigner.TapModule.*;


//}}}

/**
 *  This widget container allows edition of module parameters.
 *
 *@author     François Guillet
 *@created    14 mars 2004
 */
public class TapParametersPanel extends BSplitPane implements TapView
{
    private TapProcPanel procPanel;
    private BScrollPane spl;
    private BScrollPane spr;
    private BTree tree;
    private ViewPopup viewPopup;
    private static ImageIcon constructionIcon;
    private BorderContainer editContent;
    private TreePath selectedNode;
    private int selectedModule;
    private int parameterSet;


    /**
     *  Constructor for the TapParametersPanel object
     *
     *@param  procPanel  the TapProcPanel that holds the parameter panel
     */
    public TapParametersPanel( TapProcPanel procPanel )
    {
        super();
        this.procPanel = procPanel;
        BScrollPane sp;
        add( spl = new BScrollPane( tree = new BTree() ), 0 );
        tree.setRootNodeShown( false );
        tree.setCellRenderer( new ParametersTreeRenderer() );
        editContent = new BorderContainer();
        LayoutInfo layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.BOTH, new Insets( 0, 0, 0, 0 ), new Dimension( 0, 0 ) );
        editContent.setDefaultLayout( layout );
        add( spr = new BScrollPane( editContent ), 1 );
        spr.setForceHeight( true );
        spr.setForceWidth( true );
        viewPopup = new ViewPopup( this );
        addEventLink( WidgetMouseEvent.class, this, "doShowViewPopup" );
        tree.addEventLink( WidgetMouseEvent.class, this, "doShowViewPopup" );
        tree.addEventLink( SelectionChangedEvent.class, this, "doSelectionChanged" );
        setOneTouchExpandable( true );
        setResizeWeight( 0 );
        if ( constructionIcon == null )
            constructionIcon = new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/atwork89.gif" ) );
        initialize();
    }


    /**
     *  Gets the procPanel to which the view belongs
     *
     *@return    The procPanel value
     */
    public TapProcPanel getProcPanel()
    {
        return procPanel;
    }


    /**
     *  Initializes the module parameter tree from the modules names
     */
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
         *  addModule( (TapModule) modules.elementAt( i ) );
         *  }
         *  resetToPreferredSizes();
         *  selectedNode = null;
         *  ignoreSelectionEvent = false;
         */
        syncModuleTree();
    }


    /**
     *  Adds a module to the tree
     *
     *@param  module  The module to add
     */
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
        if ( module.getNumEditWidgets() <= 1 )
            tn.setAllowsChildren( false );
        else
        {
            for ( int i = 0; i < module.getNumEditWidgets(); ++i )
            {
                tn = new DefaultMutableTreeNode( new ModuleTreeChild( module.getEditWidgetName( i ), i ) );
                tree.addNode( modulePath, tn );
                tn.setAllowsChildren( false );
            }
        }
        ( (DefaultTreeModel) tree.getModel() ).reload();
        setDividerLocation( getChild( 0 ).getPreferredSize().width );
        if ( selectedNode != null )
        {
            ( (JTree) tree.getComponent() ).collapsePath( selectedNode );
            tree.setNodeSelected( selectedNode, true );
            doSelectionChanged();
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
                    addModule( (TapModule) modules.elementAt( i ) );
            }
        }
        setDividerLocation( getChild( 0 ).getPreferredSize().width );
        if ( selectedNode != null )
        {
            ( (JTree) tree.getComponent() ).collapsePath( selectedNode );
            tree.setNodeSelected( selectedNode, true );
            doSelectionChanged();
        }
        //( (DefaultTreeModel) tree.getModel() ).reload();


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
    public void closeWindows()
    {
        if ( editContent.getChildCount() > 0 )
        {
            Widget w = editContent.getChild( BorderContainer.CENTER );
            if ( w instanceof EditWidget )
                ( (EditWidget) w ).doClose();
            editContent.remove( BorderContainer.CENTER );
        }
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
    public void syncModuleAddition()
    {
        syncModuleTree();
    }


    /**
     *  Synchronizes the view after minor edition : new link, link suppressed,
     *  module moved, module renamed...
     */
    public void minorSync()
    {
        if ( editContent.getChildCount() == 0 )
            return;
        Widget w = editContent.getChild( BorderContainer.CENTER );
        if ( w instanceof EditWidget )
            ( (EditWidget) w ).showValues( false );
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
    public BScrollPane newScrollPane()
    {
        return null;
    }


    /**
     *  No scroll pane needed, returns false.
     *
     *@return    Always false
     */
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
        TreePath tp = tree.getSelectedNode();
        if ( tp == null )
            return;
        selectedModule = -1;
        parameterSet = -1;
        Object obj = tp.getLastPathComponent();
        Object userObj = ( (DefaultMutableTreeNode) obj ).getUserObject();
        boolean successful = true;
        if ( userObj instanceof TapModule )
        {
            TapModule module = (TapModule) userObj;
            if ( module.getNumEditWidgets() == 0 )
            {
                if ( successful = getRidOfContent() )
                {
                    selectedModule = procPanel.getProcedure().getModuleIndex( module );
                    editContent.add( new BLabel( constructionIcon, BLabel.CENTER ), BorderContainer.CENTER );
                    layoutChildren();
                    repaint();
                }
            }
            else if ( module.getNumEditWidgets() == 1 )
            {
                if ( successful = getRidOfContent() )
                {
                    selectedModule = procPanel.getProcedure().getModuleIndex( module );
                    Widget editWidget = module.getEditWidget( 0,
                        new Runnable()
                        {
                            public void run()
                            {
                                doRunnableUpdate();
                            }
                        }, false );
                    editContent.add( editWidget, BorderContainer.CENTER );
                    layoutChildren();
                    repaint();
                }
            }
            else if ( module.getNumEditWidgets() > 1 )
            {
                if ( successful = getRidOfContent() )
                {
                    selectedModule = procPanel.getProcedure().getModuleIndex( module );
                    editContent.add( TapBTranslate.bLabel( "chooseModuleParameters" ), BorderContainer.CENTER );
                    layoutChildren();
                    repaint();
                }
            }
        }
        else if ( userObj instanceof ModuleTreeChild )
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) ( (DefaultMutableTreeNode) obj ).getParent();
            TapModule module = (TapModule) ( node.getUserObject() );
            if ( successful = getRidOfContent() )
            {
                selectedModule = procPanel.getProcedure().getModuleIndex( module );
                parameterSet = ( (ModuleTreeChild) userObj ).number;
                Widget editWidget = module.getEditWidget( ( (ModuleTreeChild) userObj ).number,
                    new Runnable()
                    {
                        public void run()
                        {
                            doRunnableUpdate();
                        }
                    }, false );
                editContent.add( editWidget, BorderContainer.CENTER );
                layoutChildren();
                repaint();
            }
        }
        else
        {
            if ( successful = getRidOfContent() )
            {
                layoutChildren();
                repaint();
            }
        }
        if ( successful )
            selectedNode = tp;
        else
        {
            tree.setNodeSelected( tp, false );
            if ( selectedNode != null )
                tree.setNodeSelected( selectedNode, true );
        }

    }


    /**
     *  Gets the ridOfContent attribute of the TapParametersPanel object
     *
     *@return    The ridOfContent value
     */
    private boolean getRidOfContent()
    {
        boolean dispose = true;
        if ( editContent.getChildCount() > 0 )
        {
            Iterator iter = editContent.getChildren().iterator();
            Object obj;
            while ( iter.hasNext() )
            {
                obj = iter.next();
                if ( obj instanceof EditWidgetBase )
                    dispose = dispose & ( (EditWidgetBase) obj ).doRevert();
            }
            if ( dispose )
                editContent.remove( BorderContainer.CENTER );
        }
        return dispose;
    }


    /**
     *  Description of the Method
     */
    private void doRunnableUpdate()
    {
        procPanel.minorViewSync( this );
        layoutChildren();
        repaint();
    }


    /**
     *  Sets the layoutData attribute of the TapView object
     *
     *@param  data  The new layoutData value
     */
    public void setLayoutData( ProcPanelLayoutData data )
    {
        if ( ( !( data instanceof ParametersLayoutData ) ) || ( data == null ) )
        {
            selectedModule = -1;
            parameterSet = 0;
            return;
        }
        else
        {
            selectedModule = ( (ParametersLayoutData) data ).moduleNumber;
            parameterSet = ( (ParametersLayoutData) data ).parameterSet;
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
                    if ( parameterSet < 0 )
                        tree.setNodeSelected( tp, true );
                    else
                    {
                        if ( parameterSet < tree.getChildNodeCount( tp ) )
                            tree.setNodeSelected( tree.getChildNode( tp, parameterSet ), true );
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
    public ProcPanelLayoutData getLayoutData()
    {
        return new ParametersLayoutData( selectedModule, parameterSet );
    }


    /**
     *  Data for restoring preview layouts
     *
     *@author     François Guillet
     *@created    15 août 2004
     */
    public static class ParametersLayoutData extends ProcPanelLayoutData
    {
        /**
         *  Description of the Field
         */
        protected int moduleNumber;
        /**
         *  Description of the Field
         */
        protected int parameterSet;


        /**
         *  Constructor for the ProcPanelLayoutData object
         *
         *@param  in                          Description of the Parameter
         *@exception  IOException             Description of the Exception
         *@exception  InvalidObjectException  Description of the Exception
         */
        public ParametersLayoutData( DataInputStream in )
            throws IOException, InvalidObjectException
        {
            super( in );
            short version = in.readShort();
            if ( ( version < 0 ) || ( version > 0 ) )
                throw new InvalidObjectException( "" );

            moduleNumber = in.readInt();
            parameterSet = in.readInt();
        }


        /**
         *  Constructor for the PreviewLayoutData object
         *
         *@param  moduleNumber  Description of the Parameter
         *@param  parameterSet  Description of the Parameter
         */
        protected ParametersLayoutData( int moduleNumber, int parameterSet )
        {
            this.moduleNumber = moduleNumber;
            this.parameterSet = parameterSet;
        }


        /**
         *  Description of the Method
         *
         *@param  out              Description of the Parameter
         *@exception  IOException  Description of the Exception
         */
        public void writeToFile( DataOutputStream out )
            throws IOException
        {
            out.writeShort( 0 );
            out.writeInt( moduleNumber );
            out.writeInt( parameterSet );
        }
    }
}

