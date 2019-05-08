/*
 *  Tailored BScrollPane
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

import buoy.widget.*;
import java.awt.*;



/**
 *  Tailored BScrollPane
 *
 *@author     francois guillet
 *@created    8 avril 2004
 */
public class TapModuleScrollPane extends BScrollPane
{
    /**
     *  Creates a new ProcPanelBScrollPane object
     *
     *@param  w  Widget contained in the scrollpane
     */
    public TapModuleScrollPane( Widget w )
    {
        super( w );
    }


    /**
     *  Overrides layoutChildren so that the modulePanel is at least as big as
     *  the viewport size
     */
    public void layoutChildren()
    {
        Rectangle bounds = getBounds();
        TapModulePanel childPanel = (TapModulePanel) getContent();
        Dimension size = childPanel.getRequiredSize();
        size.width = Math.max( size.width, bounds.width );
        size.height = Math.max( size.height, bounds.height );
        if ( size.width != 0 && size.height != 0 )
            childPanel.setPreferredSize( size );
        super.layoutChildren();
    }


    /**
     *  Gets the preferredSize attribute of the TapModuleScrollPane object
     *
     *@return    The preferredSize value
     */
    public Dimension getPreferredSize()
    {
        TapModulePanel childPanel = (TapModulePanel) getContent();
        return childPanel.getRequiredSize();
    }


    /**
     *  Gets the minimumSize attribute of the TapModuleScrollPane object
     *
     *@return    The minimumSize value
     */
    public Dimension getMinimumSize()
    {
        return new Dimension( 0, 0 );
    }
}

//}}}

