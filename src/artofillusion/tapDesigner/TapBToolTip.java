/*
 *  Custom BToolTip
 */
/*
 *  Copyright 2004 François Guillet
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
import javax.swing.*;


/**
 *  Description of the Class
 *
 *@author     François Guillet
 *@created    9 ao�t 2004
 */
public class TapBToolTip extends BToolTip
{

    /**
     *  Constructor for the TapBToolTip object
     *
     *@param  c  Description of the Parameter
     */
    public TapBToolTip( Component c )
    {
        component = c;
    }


    /**
     *  Create a new TapBToolTip.
     *
     *@param  text  the text to display in the tool tip
     */

    public TapBToolTip( String text )
    {
        component = new JToolTip();
        ( (JToolTip) component ).setTipText( text );
    }


    /**
     *  Get the text to display on the tool tip.
     *
     *@return    The text value
     */

    @Override
    public String getText()
    {

        if ( component instanceof JToolTip )
            return ( (JToolTip) component ).getTipText();
        else
            return "";
    }


    /**
     *  Set the text to display on the tool tip.
     *
     *@param  text  The new text value
     */

    @Override
    public void setText( String text )
    {
        if ( component instanceof JToolTip )
            ( (JToolTip) component ).setTipText( text );
        invalidateSize();
    }


    /**
     *  Description of the Method
     *
     *@param  widget  Description of the Parameter
     *@param  where   Description of the Parameter
     */
    @Override
    public void show( Widget widget, Point where )
    {
        super.show( widget, where );
    }
}

