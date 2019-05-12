
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
import java.util.Locale;

/**
 *@author     pims
 *@created    20 juin 2004
 */

public class TapDesignerTool implements ModellingTool
{
    /**
     *  Get the text that appear as the menu item.
     *
     *@return    The name value
     */
    @Override
    public String getName()
    {
        ApplicationPreferences apf = ArtOfIllusion.getPreferences();
        Locale locale = apf.getLocale();
        TapBTranslate.setLocale(locale);
        TapDesignerTranslate.setLocale(locale);
        return TapBTranslate.text( "tapDesignerTitle" );
    }


    /**
     *  See whether an appropriate object is selected and either display an
     *  error message, or bring up the Tap tool window.
     *
     *@param  window  Description of the Parameter
     */
    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void commandSelected( LayoutWindow window )
    {
        new TapFrame( window, null );
    }

}

