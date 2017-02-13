/*
 *  Copyright (C) 2004 by Francois Guillet
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.tapDesigner;

import java.io.*;
import java.util.*;

/**
 *  Abstract class for storing TapViews parameters.
 *
 *@author     François Guillet
 *@created    15 août 2004
 */
public abstract class ProcPanelLayoutData
{
    /**
     *  Constructor for the ProcPanelLayoutData object
     *
     *@param  in               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public ProcPanelLayoutData( DataInputStream in )
        throws IOException { }


    /**
     *  Constructor for the ProcPanelLayoutData object
     */
    public ProcPanelLayoutData() { }


    /**
     *  Description of the Method
     *
     *@param  out              Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public abstract void writeToFile( DataOutputStream out )
        throws IOException;
}

