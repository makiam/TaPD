/*
 * JEditMetalTheme.java - Minor Metal L&F tweaks for jEdit
 * Copyright (C) 2001 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package artofillusion.tapDesigner;

import java.awt.*;
import javax.swing.plaf.metal.*;
import javax.swing.plaf.*;

public class TaPDMetalTheme extends DefaultMetalTheme
{
	public String getName()
	{
		return "TaPD Metal";
	}

	public ColorUIResource getSystemTextColor()
	{
		return super.getSystemTextColor();
	}

	public FontUIResource getControlTextFont()
	{
		if (primaryFont == null)
            primaryFont = new FontUIResource("Dialog",Font.PLAIN,12);
        return primaryFont;
	}

	public FontUIResource getSystemTextFont()
	{
		if (secondaryFont == null)
            secondaryFont = new FontUIResource("Dialog",Font.PLAIN,12);
        return secondaryFont;
	}

	public FontUIResource getUserTextFont()
	{
		if (secondaryFont == null)
            secondaryFont = new FontUIResource("Dialog",Font.PLAIN,12);
        return secondaryFont;
	}

	public FontUIResource getMenuTextFont()
	{
		if (primaryFont == null)
            primaryFont = new FontUIResource("Dialog",Font.PLAIN,12);
        return primaryFont;
	}

	public void propertiesChanged()
	{
		/* primaryFont = new FontUIResource(
			jEdit.getFontProperty("metal.primary.font",
			super.getControlTextFont()));
		secondaryFont = new FontUIResource(
			jEdit.getFontProperty("metal.secondary.font",
			super.getSystemTextFont())); */
	}

	// private members
	private FontUIResource primaryFont;
	private FontUIResource secondaryFont;
}
