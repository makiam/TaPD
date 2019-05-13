
/*
 *  This module decorates the faces of a triangle mesh with objects
 *  This process can be used to populate a field with grass and plants.
 */
/*
 *  Copyright (C) 2004 by François Guillet
 *  Changes copyright (C) 2019 by Maksim Khramov
 *
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.tapDesigner;


/**
 *  A simple placeholder for module parameters edit widgets info
 *
 *@author     François Guillet
 *@created    12 ao�t 2004
 */
public class ModuleTreeChild
{
    /**
     *  Description of the Field
     */
    public String name;
    /**
     *  Description of the Field
     */
    public int number;


    /**
     *  Constructor for the ModuleTreeChild object
     *
     *@param  name    Description of the Parameter
     *@param  number  Description of the Parameter
     */
    public ModuleTreeChild( String name, int number )
    {
        this.name = name;
        this.number = number;
    }


    /**
     *  Gets a String representation of the object, i.e. its name.
     *
     *@return    The name of the ModuleTreeChild
     */
    @Override
    public String toString()
    {
        return name;
    }
}

