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

/**
 *  EditWidget interface. This interface is implemented by all the edit widgets
 *  used to edit module parameters
 *
 *@author     Francois Guillet
 *@created    14 mai 2004
 */

public interface EditWidget
{
    /**
     *  Display (reload) module values
     *
     *@param  force  Description of the Parameter
     */
    public void showValues( boolean force );


    /**
     *  Gets the Validate/OK button attribute of the EditWidget object Used to
     *  link an event to this button
     *
     *@return    The validate button value
     */
    public BButton getValidateButton();


    /**
     *  Gets the Revert/Cancel button attribute of the EditWidget object Used to
     *  link an event to this button
     *
     *@return    The revert button value
     */
    public BButton getRevertButton();


    /**
     *  Call this method before disposing an Edit Widget
     */
    public void doClose();

}

