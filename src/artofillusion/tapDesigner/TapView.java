
/*
 *  interface implemented by all TaPD views.
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

import buoy.widget.*;

/**
 *  interface implemented by all TaPD views that appear in the proc panel.
 *
 *@author     Francois Guillet
 *@created    1 mai 2004
 */
public interface TapView
{
    /**
     *  Gets the procPanel to which the view belongs
     *
     *@return    The procPanel value
     */
    public TapProcPanel getProcPanel();


    /**
     *  Synchronizes the view with the procedure after a module has been added
     */
    public void syncModuleAddition();


    /**
     *  Synchronizes the view after minor edition : new link, link suppressed,
     *  module moved...
     */
    public void minorSync();


    /**
     *  initialize the view from the procedure
     */
    public void initialize();


    /**
     *  close all edit windows associated with the view
     */
    public void closeWindows();


    /**
     *  Adds a module to the view, if the view is concerned by module addition
     *
     *@param  newModule  The fnew module to display
     */
    public void addModule( TapModule newModule );


    /**
     *  Returns a scroll pane containing the view. Typically, this method should
     *  return new BScrollPane(this) if it returns true for needsScrollPane().
     *  If no scrollpane is needed, it may return null.
     *
     *@return    Description of the Return Value
     */
    public BScrollPane newScrollPane();


    /**
     *  Whether this view needs to be implemented in a scroll pane
     *
     *@return    Boolean
     */
    public boolean needsScrollPane();


    /**
     *  Sets the layoutData attribute of the TapView object
     *
     *@param  data  The new layoutData value
     */
    public void setLayoutData( ProcPanelLayoutData data );


    /**
     *  Gets the layoutData attribute of the TapView object
     *
     *@return    The layoutData value
     */
    public ProcPanelLayoutData getLayoutData();

}

