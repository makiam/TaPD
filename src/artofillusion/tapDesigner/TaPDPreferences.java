/* Copyright (C) 2002-2004 by Peter Eastman, adaptation to the TaPD (C) 2004 François Guillet

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tapDesigner;

//{{{ imports
import java.io.*;
import java.util.*; //}}}

/** This class keeps track of TaPD user preferences. */

public final class TaPDPreferences
{
  private static Properties properties;
  private static String themeName;
  private static TaPDPreferences prefs = new TaPDPreferences();
  
  private TaPDPreferences()
  {
      loadPreferences();
  }
  
  public static TaPDPreferences getPreferences()
  { 
      if (prefs == null) prefs = new TaPDPreferences();
      return prefs;
  }

  /** Load the preferences from disk. */
  
  public static void loadPreferences()
  {
    properties = new Properties();
    initDefaultPreferences();
    File f = new File(System.getProperty("user.home"), ".tapdprefs");
    if (!f.exists())
        return;
    try
      {
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        properties.load(in);
        in.close();
      }
    catch (IOException ex)
      {
        ex.printStackTrace();
      }
    parsePreferences();
  }
  
  /** Save any changed preferences to disk. */
  
  public static void savePreferences()
  {
    File f = new File(System.getProperty("user.home"), ".tapdprefs");
    try
      {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        properties.save(out, "TaPD Preferences File");
        out.close();
      }
    catch (IOException ex)
      {
        ex.printStackTrace();
      }
  }
  
  /** Initialize internal variables to reasonable defaults. */
  
  private static void initDefaultPreferences()
  {
      themeName = "default";
  }
  
  /** Parse the properties loaded from the preferences file. */
  
  private static void parsePreferences()
  {
    themeName = properties.getProperty("TaPDTheme");
    if (themeName == null)  themeName = "default";
  }
  
  /** Parse an integer valued property. */
  
  private static int parseIntProperty(String name, int defaultVal)
  {
    try
      {
        return Integer.parseInt(properties.getProperty(name));
      }
    catch (Exception ex)
      {
        return defaultVal;
      }
  }
  
  /** Parse a double valued property. */
  
  private static double parseDoubleProperty(String name, double defaultVal)
  {
    try
      {
        return new Double(properties.getProperty(name)).doubleValue();
      }
    catch (Exception ex)
      {
        return defaultVal;
      }
  }
  
  public static void setDefaultTheme(String tn)
  {
    themeName = tn;
    properties.put("TaPDTheme", tn);
    savePreferences();
  }
  
  public static String getDefaultTheme()
  {
    return themeName;
  }
}