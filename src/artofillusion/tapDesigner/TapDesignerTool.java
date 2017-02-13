
/*
 *  Copyright 2003 Francois Guillet
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.tapDesigner;

import artofillusion.*;
import artofillusion.ui.*;
import java.util.*;
import artofillusion.tapDesigner.*;

/**
 *@author     pims
 *@created    20 juin 2004
 */

public class TapDesignerTool implements ModellingTool
{
    /**
     *  instance this tool,load it in memory
     */

    public TapDesignerTool() { }


    /**
     *  Get the text that appear as the menu item.
     *
     *@return    The name value
     */
    public String getName()
    {
        TapBTranslate.setLocale( ModellingApp.getPreferences().getLocale() );
        TapDesignerTranslate.setLocale( ModellingApp.getPreferences().getLocale() );
        return TapBTranslate.text( "tapDesignerTitle" );
        //return "Tree and Plant Designer";
    }


    /**
     *  See whether an appropriate object is selected and either display an
     *  error message, or bring up the array tool window.
     *
     *@param  window  Description of the Parameter
     */
    public void commandSelected( LayoutWindow window )
    {
        new TapFrame( window, null );
    }

}

