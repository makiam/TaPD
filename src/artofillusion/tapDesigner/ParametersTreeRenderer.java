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

/**
 *  The tree renderer for the modules tree
 *
 *@author     François Guillet
 *@created    8 mai 2004
 */
public class ParametersTreeRenderer extends DefaultTreeCellRenderer
{

    /**
     *  Constructor for the ParameterTreeRenderer object
     */
    public ParametersTreeRenderer() { }


    /**
     *  Gets a treeCellRendererComponent
     *
     *@param  tree      Description of the Parameter
     *@param  value     Description of the Parameter
     *@param  sel       Description of the Parameter
     *@param  expanded  Description of the Parameter
     *@param  leaf      Description of the Parameter
     *@param  row       Description of the Parameter
     *@param  hasFocus  Description of the Parameter
     *@return           The treeCellRendererComponent value
     */
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus )
    {

        super.getTreeCellRendererComponent(
                tree, value, sel,
                expanded, leaf, row,
                hasFocus );
        {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) value;
            if ( node.getUserObject() instanceof ModuleTypeInfo )
            {
                setIcon( ( (ModuleTypeInfo) node.getUserObject() ).getIcon() );
                //setToolTipText( "A tooltip text." );
            }
            else
            {
                super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus );
            }
            return this;
        }
    }

}

