/*
 *  Copyright (C) 2003 by François Guillet
 *  Copyright (C) 2003 by Peter Eastman for original Translate.java code
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

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *  This class extends AoI Translate Class so that i) the tap Designer
 *  properties file is used instead of AoI properties file ii) Swing objects are
 *  returned
 *
 *@author     pims
 *@created    11 juillet 2004
 */

public class TapDesignerTranslate
{
    private static Locale locale = Locale.getDefault();
    private static ResourceBundle resources;


    /**
     *  Set the locale to be used for generating text.
     *
     *@param  l  The new locale value
     */

    public static void setLocale( Locale l )
    {
        locale = l;
        resources = ResourceBundle.getBundle( "tapdesigner", locale );
    }


    /**
     *  Get the locale currently used for generating text.
     *
     *@return    The locale value
     */

    public static Locale getLocale()
    {
        return locale;
    }


    /**
     *  Get a CheckboxMenuItem whose text is given by the property
     *  "menu.(name)". If listener is not null, it will be added to the MenuItem
     *  as an ItemListener. state specifies the initial state of the item.
     *
     *@param  name      Description of the Parameter
     *@param  listener  Description of the Parameter
     *@param  state     Description of the Parameter
     *@return           Description of the Return Value
     */

    public static JCheckBoxMenuItem jCheckBoxMenuItem( String name, ItemListener listener, boolean state )
    {
        String command = name;
        try
        {
            command = resources.getString( "menu." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        JCheckBoxMenuItem item = new JCheckBoxMenuItem( command, state );
        try
        {
            String shortcut = resources.getString( "menu." + name + ".shortcut" );
            if ( shortcut.length() > 1 && shortcut.charAt( 0 ) == '^' )
                item.setAccelerator( KeyStroke.getKeyStroke( shortcut.charAt( 1 ) ) );
            else if ( shortcut.length() > 0 )
                item.setAccelerator( KeyStroke.getKeyStroke( shortcut.charAt( 0 ) ) );
        }
        catch ( MissingResourceException ex )
        {
        }
        if ( listener != null )
            item.addItemListener( listener );
        return item;
    }


    /**
     *  Get a Button whose text is given by the property "button.(name)", with a
     *  suffix appended to it. If listener is not null, it will be added to the
     *  Button as an ActionListener, and the menu item's action command will be
     *  set to (name).
     *
     *@param  name      Description of the Parameter
     *@param  listener  Description of the Parameter
     *@return           Description of the Return Value
     */

    public static JButton jButton(String name, ActionListener listener )
    {
        String command = name;
        try
        {
            command = resources.getString( "button." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        JButton b = new JButton( command );
        if ( listener != null )
        {
            b.setActionCommand( name );
            b.addActionListener( listener );
        }
        return b;
    }


    /**
     *  Get a RadioButton whose text is given by the property "radio.(name)",
     *  with a suffix appended to it. If listener is not null, it will be added
     *  to the Button as an ActionListener, and the menu item's action command
     *  will be set to (name).
     *
     *@param  name      Description of the Parameter
     *@param  listener  Description of the Parameter
     *@return           Description of the Return Value
     */

    public static JRadioButton jRadioButton( String name, ChangeListener listener )
    {
        String command = name;
        try
        {
            command = resources.getString( "radio." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        JRadioButton b = new JRadioButton( command );
        if ( listener != null )
        {
            b.setActionCommand( name );
            b.addChangeListener( listener );
        }
        return b;
    }


    /**
     *  Description of the Method
     *
     *@param  name      Description of the Parameter
     *@param  listener  Description of the Parameter
     *@return           Description of the Return Value
     */
    public static JCheckBox jCheckBox( String name, ChangeListener listener )
    {
        String command = name;
        try
        {
            command = resources.getString( "checkbox." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        JCheckBox b = new JCheckBox( command );
        if ( listener != null )
        {
            b.setActionCommand( name );
            b.addChangeListener( listener );
        }
        return b;
    }

    /**
     *  Get a Label whose text is given by the property "name", with a suffix
     *  appended to it. If the property is not found, this simply uses name.
     *
     *@param  name    Description of the Parameter
     *@return         Description of the Return Value
     */

    public static JLabel jlabel(String name )
    {
        try
        {
            name = resources.getString( "label." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        return new JLabel( name );
    }


}

