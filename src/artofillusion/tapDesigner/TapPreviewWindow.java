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

import artofillusion.animation.*;
import artofillusion.animation.distortion.*;
import artofillusion.image.*;
import artofillusion.material.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.script.*;
import artofillusion.texture.*;
import artofillusion.ui.*;
import artofillusion.*;
import bsh.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.Vector;
import buoy.event.*;
import buoy.widget.*;

//import artofillusion.tapDesigner.*;

/**
 *  .
 *
 *@author     François Guillet
 *@created    27 mai 2004
 */

public class TapPreviewWindow extends BFrame
{
    TapPreviewWidget widget;
    TapModule module;


    /**
     *  Create a new LayoutWindow for editing a Scene. Usually, you will not use
     *  this constructor directly. Instead, call ModellingApp.newWindow(Scene
     *  s).
     *
     *@param  s       Description of the Parameter
     *@param  module  Description of the Parameter
     */

    public TapPreviewWindow( Scene s, TapModule module )
    {
        super( TapBTranslate.text( "previewWindowTitle", module.getName() ) );
        this.module = module;
        setContent( widget = new TapPreviewWidget( s, module ) );
        pack();
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = ( new Double( screenDim.width * 0.7 ) ).intValue();
        int orw = ( new Double( screenDim.width * 0.15 ) ).intValue();
        int h = ( new Double( screenDim.height * 0.8 ) ).intValue();
        int orh = ( new Double( screenDim.width * 0.15 ) ).intValue();
        setBounds( new java.awt.Rectangle( orw, orh, w, h ) );
        setVisible( true );
        addEventLink( WindowClosingEvent.class, this, "doQuit" );
    }



    /**
     *  Gets the scene attribute of the TapPreviewWindow object
     *
     *@return    The scene value
     */
    public Scene getScene()
    {
        return widget.getScene();
    }


    /**
     *  Update the image displayed in this window.
     */

    public void updateImage()
    {
        widget.updateImage();
    }


    /**
     *  Description of the Method
     */
    private void doQuit()
    {
        module.previewWindowClosed();
        dispose();
    }

}

