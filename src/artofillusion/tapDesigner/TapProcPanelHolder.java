/* This interface specifies methods which should be implemented so that a component may use a TapProcPanel
(namely what a TapFrame should provide in the TaPD editor window case.*/

/* Copyright 2003 Francois Guillet

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. 
*/

package artofillusion.tapDesigner;
import buoy.widget.*;
import artofillusion.*;

public interface TapProcPanelHolder
{	
	public void selectionChanged(short numSelected);
	
	public void validObject(boolean isValid);
    
    public void setUndoRedoFlags(boolean canUndo, boolean canRedo);
	
	public BFrame getBFrame();
    
    public void requestFocus();
}
