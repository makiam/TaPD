/* This class adds some information to the the traditional ObjectInfo. For some practical
reasons, it owns an ObjectInfo rather than is an ObjectInfo. */
/* Copyright (C) 2003 by François Guillet
 *  Changes copyright (C) 2019 by Maksim Khramov
 *
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.tapDesigner;


import artofillusion.object.*;



/** TapObjectInfo stores information
specific to the TapDesigner construction. At the moment, this concerns only the
decoration level. */
public class TapObjectInfo
{
    protected int     decorationLevel;
    protected double  sizeR;
    protected double  sizeY;
    protected String  name;
    public ObjectInfo objectInfo;

    public TapObjectInfo(ObjectInfo info)
    {
        objectInfo = info.duplicate();
        decorationLevel = 0;
        sizeR = 1.0;
        sizeY = 1.0;
    }

    public void setDecorationLevel(int level)
    {
        decorationLevel = level;
    }

    public int getDecorationLevel()
    {
        return decorationLevel;
    }

    public void setSize(double r, double y)
    {
        sizeR = r;
        sizeY = y;
    }

    public double getSizeR()
    {
        return sizeR;
    }

    public double getSizeY()
    {
        return sizeY;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}