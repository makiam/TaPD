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
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

//}}}

/**
 *  Edit widget dialog base
 *
 *@author     Francois Guillet
 *@created    19 mai 2004
 */
public class MultipleEditWidgetDialogBase
         extends BFrame implements EditWidgetDialog
{
    private EditWidgetBase ew;
    private TapModule module;
    private BTree tree;
    private BSplitPane sp;
    private BButton dismissButton;
    private TreePath selectedNode;


    /**
     *  Constructor for the EditWidgetDialogBase object
     *
     *@param  parentFrame  Description of the Parameter
     *@param  module       Description of the Parameter
     */
    public MultipleEditWidgetDialogBase( BFrame parentFrame, TapModule module )
    {
        super( "" );
        this.module = module;
        setTitle( module.getName() );

        tree = new BTree();
        tree.setRootNodeShown( false );
        sp = new BSplitPane();
        sp.add( new BScrollPane( tree ), 0 );

        DefaultMutableTreeNode tn;
        for ( int i = 0; i < module.getNumEditWidgets(); ++i )
        {
            tn = new DefaultMutableTreeNode( new ModuleTreeChild( module.getEditWidgetName( i ), i ) );
            tree.addNode( tree.getRootNode(), tn );
            tn.setAllowsChildren( false );
        }
        selectedNode = tree.getChildNode( tree.getRootNode(), 0 );
        tree.setNodeSelected( selectedNode, true );
        sp.add( ew = (EditWidgetBase) module.getEditWidget( 0,
            new Runnable()
            {
                public void run()
                {
                    doRunnableUpdate();
                }
            }, false ), 1 );
        BorderContainer bc = new BorderContainer();
        LayoutInfo layout = new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.BOTH, new Insets( 3, 3, 3, 3 ), null );
        bc.setDefaultLayout( layout );
        bc.add( sp, BorderContainer.CENTER );

        dismissButton = TapBTranslate.bButton( "dismiss", this, "doDismiss" );
        bc.add( dismissButton, BorderContainer.SOUTH, new LayoutInfo( LayoutInfo.CENTER, LayoutInfo.NONE, new Insets( 3, 3, 3, 3 ), null ) );
        setContent( bc );
        pack();
        TapUtils.centerAndSizeWindow( this );
        sp.setDividerLocation( sp.getChild( 0 ).getPreferredSize().width );
        setVisible( true );
        addEventLink( WindowClosingEvent.class, this, "doDismiss" );
        tree.addEventLink( SelectionChangedEvent.class, this, "doSelectionChanged" );
        ew.showValues( true );
    }


    /**
     *  Called when the edit widget validates changes.
     */
    private void doRunnableUpdate()
    {
        module.getProcedure().getProcPanel().minorViewSync( null );
        layoutChildren();
        repaint();
    }


    /**
     *  Description of the Method
     */
    public void doDismiss()
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


    /**
     *  Description of the Method
     */
    private void doSelectionChanged()
    {
        TreePath tp = tree.getSelectedNode();
        if ( tp == null )
            return;
        Object obj = tp.getLastPathComponent();
        Object userObj = ( (DefaultMutableTreeNode) obj ).getUserObject();
        if ( userObj instanceof ModuleTreeChild )
        {
            if ( getRidOfContent() )
            {
                selectedNode = tp;
                ew = (EditWidgetBase) module.getEditWidget( ( (ModuleTreeChild) userObj ).number,
                    new Runnable()
                    {
                        public void run()
                        {
                            doRunnableUpdate();
                        }
                    }, false );
                sp.add( ew, 1 );
                sp.layoutChildren();
                sp.setDividerLocation( sp.getChild( 0 ).getPreferredSize().width );
                repaint();
            }
            else
            {
                tree.setNodeSelected( tp, false );
                tree.setNodeSelected( selectedNode, true );
            }
        }
    }


    /**
     *  Clears the right content of the split pane
     *
     *@return    The ridOfContent value
     */
    private boolean getRidOfContent()
    {
        if ( ew != null )
            return ew.doRevert();
        return true;
    }

}

