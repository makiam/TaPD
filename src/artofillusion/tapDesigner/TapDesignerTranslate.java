/*
 *  Copyright (C) 2003 by Francois Guillet
 *  Copyright (C) 2003 by Peter Eastman for original Translate.java code
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.tapDesigner;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.*;
import javax.swing.event.*;
import artofillusion.ui.*;
import artofillusion.*;
import java.io.*;

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
        /*
         *  File dir = new File(ModellingApp.PLUGIN_DIRECTORY);
         *  if (dir.exists())
         *  {	String[] files = dir.list();
         *  for (int i = 0; i < files.length; i++)
         *  if (files[i].startsWith("TaPD"))
         *  {	ZipFile zf = null;
         *  try
         *  {
         *  zf = new ZipFile(new File(ModellingApp.PLUGIN_DIRECTORY, files[i]));
         *  }
         *  catch (IOException ex)
         *  {
         *  continue;  // Not a zip file.
         *  }
         *  if (zf!=null)
         *  {	JarClassLoader jcl = new JarClassLoader(zf);
         *  resources = ResourceBundle.getBundle("tapdesigner", locale, jcl);
         *  }
         *  }
         *  }
         *  else
         *  {	System.out.println("Dir does not exist");
         *  }
         */
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
     *  Get a Menu whose text is given by the property "menu.(name)".
     *
     *@param  name  Description of the Parameter
     *@return       Description of the Return Value
     */

    public static JMenu jMenu( String name )
    {
        try
        {
            return new JMenu( resources.getString( "menu." + name ) );
        }
        catch ( MissingResourceException ex )
        {
            return new JMenu( name );
        }
    }


    /**
     *  Get a JMenuItem whose text is given by the property "menu.(name)". If
     *  listener is not null, it will be added to the MenuItem as an
     *  ActionListener, and the menu item's action command will be set to
     *  (name). This also checks for a property called "menu.shortcut.(name)",
     *  and if it is found, sets the menu shortcut accordingly.
     *
     *@param  name      Description of the Parameter
     *@param  listener  Description of the Parameter
     *@return           Description of the Return Value
     */

    public static JMenuItem jMenuItem( String name, ActionListener listener )
    {
        String command = name;
        try
        {
            command = resources.getString( "menu." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        JMenuItem item = new JMenuItem( command );
        try
        {
            String shortcut = resources.getString( "menu." + name + ".shortcut" );
            if ( shortcut.length() > 1 && shortcut.charAt( 0 ) == '^' )
                item.setAccelerator( KeyStroke.getKeyStroke( "ctrl " + shortcut.charAt( 1 ) ) );
            else if ( shortcut.length() > 0 )
                item.setAccelerator( KeyStroke.getKeyStroke( "ctrl " + shortcut.charAt( 0 ) ) );
        }
        catch ( MissingResourceException ex )
        {
        }
        if ( listener != null )
        {
            item.setActionCommand( name );
            item.addActionListener( listener );
        }
        return item;
    }


    /**
     *  Get a MenuItem whose text is given by the property "menu.(name)". If
     *  listener is not null, it will be added to the MenuItem as an
     *  ActionListener, and the menu item's action command will be set to
     *  (name). This form of the method allows you to explicitly specify a menu
     *  shortcut, rather than using the one given in the properties file.
     *
     *@param  name      Description of the Parameter
     *@param  listener  Description of the Parameter
     *@return           Description of the Return Value
     */

    public static MenuItem menuItem( String name, ActionListener listener )
    {
        String command = name;
        try
        {
            command = resources.getString( "menu." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        MenuItem item = new MenuItem( command );
        try
        {
            String shortcut = resources.getString( "menu." + name + ".shortcut" );
            if ( shortcut.length() > 1 && shortcut.charAt( 0 ) == '^' )
                item.setShortcut( new MenuShortcut( (int) shortcut.charAt( 1 ), true ) );
            else if ( shortcut.length() > 0 )
                item.setShortcut( new MenuShortcut( (int) shortcut.charAt( 0 ) ) );
        }
        catch ( MissingResourceException ex )
        {
        }
        if ( listener != null )
        {
            item.setActionCommand( name );
            item.addActionListener( listener );
        }
        return item;
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
     *  Get a Button whose text is given by the property "button.(name)". If
     *  listener is not null, it will be added to the Button as an
     *  ActionListener, and the menu item's action command will be set to
     *  (name).
     *
     *@param  name      Description of the Parameter
     *@param  listener  Description of the Parameter
     *@return           Description of the Return Value
     */

    public static JButton jButton( String name, ActionListener listener )
    {
        return jButton( name, null, listener );
    }


    /**
     *  Get a Button whose text is given by the property "button.(name)", with a
     *  suffix appended to it. If listener is not null, it will be added to the
     *  Button as an ActionListener, and the menu item's action command will be
     *  set to (name).
     *
     *@param  name      Description of the Parameter
     *@param  suffix    Description of the Parameter
     *@param  listener  Description of the Parameter
     *@return           Description of the Return Value
     */

    public static JButton jButton( String name, String suffix, ActionListener listener )
    {
        String command = name;
        try
        {
            command = resources.getString( "button." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        if ( suffix != null )
            command += suffix;
        JButton b = new JButton( command );
        if ( listener != null )
        {
            b.setActionCommand( name );
            b.addActionListener( listener );
        }
        return b;
    }


    /**
     *  Get a RadioButton whose text is given by the property "radio.(name)". If
     *  listener is not null, it will be added to the Button as an
     *  ActionListener, and the menu item's action command will be set to
     *  (name).
     *
     *@param  name      Description of the Parameter
     *@param  listener  Description of the Parameter
     *@return           Description of the Return Value
     */

    public static JRadioButton jRadioButton( String name, ChangeListener listener )
    {
        return jRadioButton( name, null, listener );
    }


    /**
     *  Get a RadioButton whose text is given by the property "radio.(name)",
     *  with a suffix appended to it. If listener is not null, it will be added
     *  to the Button as an ActionListener, and the menu item's action command
     *  will be set to (name).
     *
     *@param  name      Description of the Parameter
     *@param  suffix    Description of the Parameter
     *@param  listener  Description of the Parameter
     *@return           Description of the Return Value
     */

    public static JRadioButton jRadioButton( String name, String suffix, ChangeListener listener )
    {
        String command = name;
        try
        {
            command = resources.getString( "radio." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        if ( suffix != null )
            command += suffix;
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
    public static Button button( String name, ActionListener listener )
    {
        return button( name, null, listener );
    }


    /**
     *  Description of the Method
     *
     *@param  name      Description of the Parameter
     *@param  suffix    Description of the Parameter
     *@param  listener  Description of the Parameter
     *@return           Description of the Return Value
     */
    public static Button button( String name, String suffix, ActionListener listener )
    {
        String command = name;
        try
        {
            command = resources.getString( "button." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        if ( suffix != null )
            command += suffix;
        Button b = new Button( command );
        if ( listener != null )
        {
            b.setActionCommand( name );
            b.addActionListener( listener );
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
        return jCheckBox( name, null, listener );
    }


    /**
     *  Description of the Method
     *
     *@param  name      Description of the Parameter
     *@param  suffix    Description of the Parameter
     *@param  listener  Description of the Parameter
     *@return           Description of the Return Value
     */
    public static JCheckBox jCheckBox( String name, String suffix, ChangeListener listener )
    {
        String command = name;
        try
        {
            command = resources.getString( "checkbox." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        if ( suffix != null )
            command += suffix;
        JCheckBox b = new JCheckBox( command );
        if ( listener != null )
        {
            b.setActionCommand( name );
            b.addChangeListener( listener );
        }
        return b;
    }


    /**
     *  Get a Label whose text is given by the property "name". If the property
     *  is not found, this simply uses name.
     *
     *@param  name  Description of the Parameter
     *@return       Description of the Return Value
     */

    public static JLabel jlabel( String name )
    {
        return jlabel( name, null );
    }


    /**
     *  Get a Label whose text is given by the property "name", with a suffix
     *  appended to it. If the property is not found, this simply uses name.
     *
     *@param  name    Description of the Parameter
     *@param  suffix  Description of the Parameter
     *@return         Description of the Return Value
     */

    public static JLabel jlabel( String name, String suffix )
    {
        try
        {
            name = resources.getString( "label." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        if ( suffix != null )
            name += suffix;
        return new JLabel( name );
    }


    /**
     *  Get the text given by the property "name". If the property is not found,
     *  this simply returns name.
     *
     *@param  name  Description of the Parameter
     *@return       Description of the Return Value
     */

    public static String text( String name )
    {
        try
        {
            return resources.getString( "text." + name );
        }
        catch ( MissingResourceException ex )
        {
            return name;
        }
    }


    /**
     *  Get the text given by the property "name". If the property is not found,
     *  this simply uses name. Any occurrance of the pattern "{0}" in the text
     *  string will be replaced with the string representation of arg1.
     *
     *@param  name  Description of the Parameter
     *@param  arg1  Description of the Parameter
     *@return       Description of the Return Value
     */

    public static String text( String name, Object arg1 )
    {
        String pattern = name;
        try
        {
            pattern = resources.getString( "text." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        return MessageFormat.format( pattern, new Object[]{arg1} );
    }


    /**
     *  Get the text given by the property "name". If the property is not found,
     *  this simply uses name. Any occurrances of the patterns "{0}" and "{1}"
     *  in the text string will be replaced with the strings representations of
     *  arg1 and arg2, respectively.
     *
     *@param  name  Description of the Parameter
     *@param  arg1  Description of the Parameter
     *@param  arg2  Description of the Parameter
     *@return       Description of the Return Value
     */

    public static String text( String name, Object arg1, Object arg2 )
    {
        String pattern = name;
        try
        {
            pattern = resources.getString( "text" + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        return MessageFormat.format( pattern, new Object[]{arg1, arg2} );
    }


    /**
     *  Get the text given by the property "name". If the property is not found,
     *  this simply uses name. That string and the args array are then passed to
     *  MessageFormat.format() so that any variable fields can be replaced with
     *  the correct values.
     *
     *@param  name  Description of the Parameter
     *@param  args  Description of the Parameter
     *@return       Description of the Return Value
     */

    public static String text( String name, Object args[] )
    {
        String pattern = name;
        try
        {
            pattern = resources.getString( "text." + name );
        }
        catch ( MissingResourceException ex )
        {
        }
        return MessageFormat.format( pattern, args );
    }
}

