
/*
 *  EditWidgetDialog interface
 */
/*
 *  Copyright (C) 2004 by François Guillet
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.tapDesigner;


/**
 *  EditWidget interface. This interface is implemented by all the edit widgets
 *  used to edit module parameters
 *
 *@author     François Guillet
 *@created    14 aout 2004
 */

public interface EditWidgetDialog
{
    /**
     *  Display (reload) module values
     *
     *@param  force  Description of the Parameter
     */
    public void showValues( boolean force );
}

