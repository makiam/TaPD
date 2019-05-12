/*
 *  This class is responsible for the visual appearance and behavior
 *  of a tapmodule. It owns a module rather than extend it.
 */
/*
 *  Copyright (C) 2003 by Francois Guillet
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

import java.awt.*;
import javax.swing.*;
import buoy.event.*;
import buoy.widget.*;

/**
 *  Description of the Class
 *
 *@author     Fancois Guillet
 *@created    4 avril 2004
 */
public class TapVisualModule extends OverlayContainer
{
    private TapModulePanel modulePanel;
    /**
     *  Description of the Field
     */
    protected TapVisualModulePanel panel;
    /**
     *  Description of the Field
     */
    protected ButtonContainer buttonContainer;
    private TapModule module;
    private Rectangle[] inputPortsBounds;
    private Rectangle[] outputPortsBounds;
    private boolean reversibleSelect;
    private final static Color mainEntryColor = Color.green.darker();

    /**
     *  Description of the Field
     */
    public final static int LEFT = 0;
    /**
     *  Description of the Field
     */
    public final static int TOP = 1;
    /**
     *  Description of the Field
     */
    public final static int RIGHT = 2;
    /**
     *  Description of the Field
     */
    public final static int BOTTOM = 3;


    /**
     *  Constructor for the TapVisualModule object
     *
     *@param  modulePanel  Description of the Parameter
     *@param  module       Description of the Parameter
     */
    public TapVisualModule( TapModulePanel modulePanel, TapModule module )
    {
        super();
        this.modulePanel = modulePanel;
        this.module = module;
        panel = new TapVisualModulePanel();
        panel.setOpaque( true );
        buttonContainer = new ButtonContainer();
        buttonContainer.setOpaque( false );
        add( panel );
        add( buttonContainer );
        panel.setPreferredSize( buttonContainer.getPreferredSize() );
        layoutChildren();
        if ( module.isMainEntry() )
            panel.setBackground( Color.green.darker() );
        else
            panel.setBackground( module.getBackgroundColor() );
        if ( module.getNumInput() > 0 )
            inputPortsBounds = new Rectangle[module.getNumInput()];
        outputPortsBounds = new Rectangle[module.getNumOutput()];
        setPortsBounds();
        Color[] colors = null;
        if ( module.getNumInput() > 0 )
            colors = new Color[module.getNumInput()];
        for ( int i = 0; i < module.getNumInput(); ++i )
        {
            switch ( module.getInputNature( i ) )
            {
                case TapModule.NULL_PORT:
                    colors[i] = Color.red;
                    break;
                case TapModule.OBJECT_PORT:
                    colors[i] = Color.green;
                    /*
                     *  colors[i]=colors[i].darker();
                     */
                    break;
                case TapModule.VALUE_PORT:
                    colors[i] = Color.magenta;
                    /*
                     *  colors[i]=colors[i].brighter();
                     */
                    break;
            }
        }
        panel.setInputPortsColor( colors );
        colors = new Color[module.getNumOutput()];
        for ( int i = 0; i < module.getNumOutput(); ++i )
        {
            switch ( module.getOutputNature( i ) )
            {
                case TapModule.NULL_PORT:
                    colors[i] = Color.red;
                    break;
                case TapModule.OBJECT_PORT:
                    colors[i] = Color.green;
                    /*
                     *  colors[i]=colors[i].darker();
                     */
                    break;
                case TapModule.VALUE_PORT:
                    colors[i] = Color.magenta;
                    /*
                     *  colors[i]=colors[i].brighter();
                     */
                    break;
            }
        }
        panel.setOutputPortsColor( colors );
        addEventLink( ToolTipEvent.class, this, "doToolTip" );
        addEventLink( KeyPressedEvent.class, modulePanel, "doKeyPressed" );
        addEventLink( KeyReleasedEvent.class, modulePanel, "doKeyReleased" );
        buttonContainer.addEventLink( KeyPressedEvent.class, modulePanel, "doKeyPressed" );
        buttonContainer.addEventLink( KeyReleasedEvent.class, modulePanel, "doKeyReleased" );
        reversibleSelect = false;
        module.setVisualModule( this );
    }


    /**
     *  Description of the Method
     *
     *@param  ev  Description of the Parameter
     */
    public void doToolTip( ToolTipEvent ev )
    {
        new BToolTip( panel.getToolTipText( ev.getPoint() ) ).processEvent( ev );
    }


    /**
     *  Description of the Method
     */
    private void doEdit()
    {
        module.edit( modulePanel.getProcPanel().getFrame() );
    }


    /**
     *  Description of the Method
     *
     *@param  evt  Description of the Parameter
     */
    private void doPreview( CommandEvent evt )
    {
        module.showPreviewFrame( evt.getModifiers() );
    }


    /**
     *  Sets the location attribute of the TapVisualModule object
     *
     *@param  location  The new location value
     */
    public void setLocation( Point location )
    {
        module.setLocation( location );
    }


    /**
     *  Gets the location attribute of the TapVisualModule object
     *
     *@return    The location value
     */
    public Point getLocation()
    {
        return module.getLocation();
    }


    /**
     *  Gets the bounds attribute of the TapVisualModule object
     *
     *@return    The bounds value
     */
    @Override
    public Rectangle getBounds()
    {
        Rectangle rect = new Rectangle( buttonContainer.getPreferredSize() );
        Point p = module.getLocation();
        rect.x = p.x;
        rect.y = p.y;
        return rect;
    }


    /**
     *  Sets the decoration attribute of the TapVisualModule object
     *
     *@param  portDecoration  The new decoration value
     */
    public void setDecoration( int portDecoration )
    {
        module.setPortDecoration( portDecoration );
        setPortsBounds();
    }


    /**
     *  Sets the portsBounds attribute of the TapVisualModule object
     */
    public void setPortsBounds()
    {
        int one;
        int two;
        int three;
        int output;

        switch ( module.getPortDecoration() )
        {
            case TapModule.LEFT_TO_RIGHT:
                one = TOP;
                two = LEFT;
                three = BOTTOM;
                output = RIGHT;
                break;
            case TapModule.TOP_TO_BOTTOM:
                one = LEFT;
                two = TOP;
                three = RIGHT;
                output = BOTTOM;
                break;
            case TapModule.RIGHT_TO_LEFT:
                one = TOP;
                two = RIGHT;
                three = BOTTOM;
                output = LEFT;
                break;
            case TapModule.BOTTOM_TO_TOP:
                one = RIGHT;
                two = BOTTOM;
                three = LEFT;
                output = TOP;
                break;
            default:
                one = TOP;
                two = LEFT;
                three = BOTTOM;
                output = RIGHT;
                break;
        }
        if ( inputPortsBounds != null )
        {
            if ( inputPortsBounds.length == 1 )
            {
                setPortBounds( inputPortsBounds, 0, two, 0, 1 );
            }
            else if ( inputPortsBounds.length == 2 )
            {
                setPortBounds( inputPortsBounds, 0, one, 0, 1 );
                setPortBounds( inputPortsBounds, 1, three, 0, 1 );
            }
            else
            {
                int i;
                int k;
                int l;
                k = inputPortsBounds.length / 3;
                l = ( inputPortsBounds.length - 2 * k ) / 2;
                for ( i = 0; i < k; ++i )
                    setPortBounds( inputPortsBounds, i, one, i, k );
                for ( i = k; i < 2 * k + l; ++i )
                    setPortBounds( inputPortsBounds, i, two, i - k, k + l );
                for ( i = 2 * k + l; i < inputPortsBounds.length; ++i )
                    setPortBounds( inputPortsBounds, i, three, i - 2 * k - l, inputPortsBounds.length - 2 * k - l );

            }
        }
        for ( int i = 0; i < outputPortsBounds.length; ++i )
            setPortBounds( outputPortsBounds, i, output, i, outputPortsBounds.length );
        panel.setInputPortsBounds( inputPortsBounds );
        panel.setOutputPortsBounds( outputPortsBounds );
        panel.setInputPortsTooltips( module.getInputTooltips() );
        panel.setOutputPortsTooltips( module.getOutputTooltips() );

    }


    /**
     *  Sets the portBounds attribute of the TapVisualModule object
     *
     *@param  rectangles   The new portBounds value
     *@param  r            The new portBounds value
     *@param  orientation  The new portBounds value
     *@param  number       The new portBounds value
     *@param  total        The new portBounds value
     */
    public void setPortBounds( Rectangle[] rectangles, int r, int orientation, int number, int total )
    {
        int i;
        int x;
        int y;
        int width;
        int height;

        width = panel.getPreferredSize().width;
        height = panel.getPreferredSize().height;
        switch ( orientation )
        {
            case TOP:
                x = ( width / ( total + 1 ) ) * ( number + 1 );
                y = 0;
                rectangles[r] = new Rectangle( x - 3, y, 6, 5 );
                break;
            case BOTTOM:
                x = ( width / ( total + 1 ) ) * ( number + 1 );
                y = height;
                rectangles[r] = new Rectangle( x - 3, y - 6, 6, 5 );
                break;
            case LEFT:
                x = 0;
                y = ( height / ( total + 1 ) ) * ( number + 1 );
                rectangles[r] = new Rectangle( x, y - 3, 5, 6 );
                break;
            case RIGHT:
                x = width;
                y = ( height / ( total + 1 ) ) * ( number + 1 );
                rectangles[r] = new Rectangle( x - 6, y - 3, 5, 6 );
                break;
        }
    }


    /**
     *  Description of the Method
     *
     *@param  where  Description of the Parameter
     *@return        Description of the Return Value
     */
    public boolean inPorts( Point where )
    {
        int i;
        if ( inputPortsBounds != null )
            for ( i = 0; i < inputPortsBounds.length; ++i )
                if ( inputPortsBounds[i].contains( where ) )
                    return true;
        for ( i = 0; i < outputPortsBounds.length; ++i )
            if ( outputPortsBounds[i].contains( where ) )
                return true;
        return false;
    }


    /**
     *  Gets the clickedPortNature attribute of the TapVisualModule object
     *
     *@param  where  Description of the Parameter
     *@return        The clickedPortNature value
     */
    public int getClickedPortNature( Point where )
    {
        int i;
        if ( inputPortsBounds != null )
            for ( i = 0; i < inputPortsBounds.length; ++i )
                if ( inputPortsBounds[i].contains( where ) )
                    return module.getInputNature( i );
        for ( i = 0; i < outputPortsBounds.length; ++i )
            if ( outputPortsBounds[i].contains( where ) )
                return module.getOutputNature( i );
        return -1;
    }


    /**
     *  Gets the clickedPortIndex attribute of the TapVisualModule object
     *
     *@param  where  Description of the Parameter
     *@return        The clickedPortIndex value
     */
    public int getClickedPortIndex( Point where )
    {
        int i;
        if ( inputPortsBounds != null )
            for ( i = 0; i < inputPortsBounds.length; ++i )
                if ( inputPortsBounds[i].contains( where ) )
                    return i;
        for ( i = 0; i < outputPortsBounds.length; ++i )
            if ( outputPortsBounds[i].contains( where ) )
                return i;
        return -1;
    }


    /**
     *  Gets the clickedPortOutput attribute of the TapVisualModule object
     *
     *@param  where  Description of the Parameter
     *@return        The clickedPortOutput value
     */
    public boolean isClickedPortOutput( Point where )
    {
        int i;
        if ( inputPortsBounds != null )
            for ( i = 0; i < inputPortsBounds.length; ++i )
                if ( inputPortsBounds[i].contains( where ) )
                    return false;
        for ( i = 0; i < outputPortsBounds.length; ++i )
            if ( outputPortsBounds[i].contains( where ) )
                return true;
        return false;
    }


    /**
     *  Gets the inputPortLocation attribute of the TapVisualModule object
     *
     *@param  portIndex  Description of the Parameter
     *@return            The inputPortLocation value
     */
    public Point getInputPortLocation( int portIndex )
    {
        return new Point( inputPortsBounds[portIndex].x + inputPortsBounds[portIndex].getSize().width / 2,
                inputPortsBounds[portIndex].y + inputPortsBounds[portIndex].getSize().height / 2 );
    }


    /**
     *  Gets the outputPortLocation attribute of the TapVisualModule object
     *
     *@param  portIndex  Description of the Parameter
     *@return            The outputPortLocation value
     */
    public Point getOutputPortLocation( int portIndex )
    {
        return new Point( outputPortsBounds[portIndex].x + outputPortsBounds[portIndex].getSize().width / 2,
                outputPortsBounds[portIndex].y + outputPortsBounds[portIndex].getSize().height / 2 );
    }


    /**
     *  Gets the inputPortLinkLocation attribute of the TapVisualModule object
     *
     *@param  portIndex  Description of the Parameter
     *@return            The inputPortLinkLocation value
     */
    public Point getInputPortLinkLocation( int portIndex )
    {
        if ( inputPortsBounds[portIndex].x == 0 )
        {
            return new Point( inputPortsBounds[portIndex].x, inputPortsBounds[portIndex].y + inputPortsBounds[portIndex].getSize().height / 2 );
        }
        else if ( inputPortsBounds[portIndex].x + inputPortsBounds[portIndex].getSize().width == getComponent().getSize().width - 1 )
        {
            return new Point( inputPortsBounds[portIndex].x + inputPortsBounds[portIndex].getSize().width,
                    inputPortsBounds[portIndex].y + inputPortsBounds[portIndex].getSize().height / 2 );
        }
        else if ( inputPortsBounds[portIndex].y == 0 )
        {
            return new Point( inputPortsBounds[portIndex].x + inputPortsBounds[portIndex].getSize().width / 2,
                    inputPortsBounds[portIndex].y );
        }
        else
        {
            return new Point( inputPortsBounds[portIndex].x + inputPortsBounds[portIndex].getSize().width / 2,
                    inputPortsBounds[portIndex].y + inputPortsBounds[portIndex].getSize().height );
        }
    }


    /**
     *  Gets the outputPortLinkLocation attribute of the TapVisualModule object
     *
     *@param  portIndex  Description of the Parameter
     *@return            The outputPortLinkLocation value
     */
    public Point getOutputPortLinkLocation( int portIndex )
    {
        if ( outputPortsBounds[portIndex].x == 0 )
        {
            return new Point( outputPortsBounds[portIndex].x, outputPortsBounds[portIndex].y + outputPortsBounds[portIndex].getSize().height / 2 );
        }
        else if ( outputPortsBounds[portIndex].x + outputPortsBounds[portIndex].getSize().width == getComponent().getSize().width - 1 )
        {
            return new Point( outputPortsBounds[portIndex].x + outputPortsBounds[portIndex].getSize().width,
                    outputPortsBounds[portIndex].y + outputPortsBounds[portIndex].getSize().height / 2 );
        }
        else if ( outputPortsBounds[portIndex].y == 0 )
        {
            return new Point( outputPortsBounds[portIndex].x + outputPortsBounds[portIndex].getSize().width / 2,
                    outputPortsBounds[portIndex].y );
        }
        else
        {
            return new Point( outputPortsBounds[portIndex].x + outputPortsBounds[portIndex].getSize().width / 2,
                    outputPortsBounds[portIndex].y + outputPortsBounds[portIndex].getSize().height );
        }
    }


    /**
     *  Sets the selected attribute of the TapVisualModule object
     *
     *@param  selected  The new selected value
     */
    public void setSelected( boolean selected )
    {
        panel.setSelected( selected );
    }


    /**
     *  Gets the selected attribute of the TapVisualModule object
     *
     *@return    The selected value
     */
    public boolean getSelected()
    {
        return panel.getSelected();
    }


    /**
     *  Gets the width attribute of the TapVisualModule object
     *
     *@return    The width value
     */
    public int getWidth()
    {
        return getComponent().getSize().width;
    }


    /**
     *  Gets the height attribute of the TapVisualModule object
     *
     *@return    The height value
     */
    public int getHeight()
    {
        return getComponent().getSize().height;
    }


    /**
     *  Gets the module attribute of the TapVisualModule object
     *
     *@return    The module value
     */
    public TapModule getModule()
    {
        return module;
    }


    /**
     *  Gets the module panel attribute of the TapVisualModule object
     *
     *@return    The module panel value
     */
    public TapModulePanel getModulePanel()
    {
        return modulePanel;
    }


    /**
     *  Sets the reversibleSelect attribute of the TapVisualModule object
     *
     *@param  reversibleSelect  The new reversibleSelect value
     */
    public void setReversibleSelect( boolean reversibleSelect )
    {
        this.reversibleSelect = reversibleSelect;
    }


    /**
     *  Sets the reverseSelected attribute of the TapVisualModule object
     *
     *@param  selected  The new reverseSelected value
     */
    public void setReverseSelected( boolean selected )
    {
        if ( selected )
            panel.setSelected( true );
        else
        {
            if ( reversibleSelect )
                panel.setSelected( false );
            else
                panel.setSelected( true );
        }
    }


    /**
     *  Description of the Method
     *
     *@param  translationTable  Description of the Parameter
     */
    public void applyTranslation( int[] translationTable )
    {
        module.applyTranslation( translationTable );
    }


    /**
     *  Description of the Method
     */
    public void prepareToBeDeleted()
    {
        buttonContainer.button.removeEventLink( KeyPressedEvent.class, this );
        if ( buttonContainer.previewButton != null )
            buttonContainer.previewButton.removeEventLink( KeyPressedEvent.class, this );
        removeEventLink( ToolTipEvent.class, this );
        module.prepareToBeDeleted();
    }


    /**
     *  Sets the name attribute of the TapVisualModule object
     *
     *@param  newName  The new name value
     */
    @Override
    public void setName( String newName )
    {
        buttonContainer.button.setText( newName );
        module.setName( newName );
        buttonContainer.packButtons();
        panel.setPreferredSize( buttonContainer.getPreferredSize() );
        layoutChildren();
        setPortsBounds();
        modulePanel.buildModuleLinks();
        modulePanel.updateChildBounds( this );
        modulePanel.getComponent().repaint();
    }


    /**
     *  Gets the mainEntry attribute of the TapVisualModule object
     *
     *@return    The mainEntry value
     */
    public boolean isMainEntry()
    {
        return module.isMainEntry();
    }


    /**
     *  Description of the Class
     *
     *@author     francois
     *@created    23 avril 2004
     */
    private class TapVisualModulePanel extends CustomWidget
    {
        private Rectangle[] inputPortsBounds;
        private Rectangle[] outputPortsBounds;
        private Color[] inputPortsColor;
        private Color[] outputPortsColor;
        private String[] inputPortsTooltips;
        private String[] outputPortsTooltips;
        private boolean selected;
        private Color background;


        /**
         *  Constructor for the TapVisualModulePanel object
         */
        public TapVisualModulePanel()
        {
            super();
            selected = false;
            addEventLink( RepaintEvent.class, this, "doRepaint" );
            addEventLink( KeyPressedEvent.class, modulePanel, "doKeyPressed" );
            addEventLink( KeyReleasedEvent.class, modulePanel, "doKeyReleased" );
            background = Color.white;
        }


        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void doRepaint( RepaintEvent ev )
        {
            int i;
            int j;
            Polygon triangle = new Polygon();
            Graphics2D g = ev.getGraphics();
            //super.paint( g );
            Rectangle rect = new Rectangle( getComponent().getSize() );
            Color c = g.getColor();
            if ( module.isMainEntry() )
                g.setColor( mainEntryColor );
            else
                g.setColor( module.getBackgroundColor() );
            g.fill( rect );
            //Dimension size = getComponent().getSize();
            Dimension size = buttonContainer.getPreferredSize();
            if ( inputPortsBounds != null )
                for ( i = 0; i < inputPortsBounds.length; ++i )
                {
                    triangle.reset();
                    if ( inputPortsBounds[i].x == 0 )
                    {
                        triangle.addPoint( 0, inputPortsBounds[i].y );
                        triangle.addPoint( inputPortsBounds[i].getSize().width,
                                inputPortsBounds[i].y + inputPortsBounds[i].getSize().height / 2 );
                        triangle.addPoint( 0, inputPortsBounds[i].y + inputPortsBounds[i].getSize().height );
                    }
                    else if ( inputPortsBounds[i].x + inputPortsBounds[i].getSize().width == size.width - 1 )
                    {
                        triangle.addPoint( inputPortsBounds[i].x + inputPortsBounds[i].getSize().width,
                                inputPortsBounds[i].y );
                        triangle.addPoint( inputPortsBounds[i].x,
                                inputPortsBounds[i].y + inputPortsBounds[i].getSize().height / 2 );
                        triangle.addPoint( inputPortsBounds[i].x + inputPortsBounds[i].getSize().width,
                                inputPortsBounds[i].y + inputPortsBounds[i].getSize().height );
                    }
                    else if ( inputPortsBounds[i].y == 0 )
                    {
                        triangle.addPoint( inputPortsBounds[i].x,
                                inputPortsBounds[i].y );
                        triangle.addPoint( inputPortsBounds[i].x + inputPortsBounds[i].getSize().width / 2,
                                inputPortsBounds[i].y + inputPortsBounds[i].getSize().height );
                        triangle.addPoint( inputPortsBounds[i].x + inputPortsBounds[i].getSize().width,
                                inputPortsBounds[i].y );
                    }
                    else
                    {
                        triangle.addPoint( inputPortsBounds[i].x,
                                inputPortsBounds[i].y + inputPortsBounds[i].getSize().height );
                        triangle.addPoint( inputPortsBounds[i].x + inputPortsBounds[i].getSize().width / 2,
                                inputPortsBounds[i].y );
                        triangle.addPoint( inputPortsBounds[i].x + inputPortsBounds[i].getSize().width,
                                inputPortsBounds[i].y + inputPortsBounds[i].getSize().height );
                    }
                    g.setColor( inputPortsColor[i] );
                    g.drawPolygon( triangle );
                    g.fillPolygon( triangle );
                }
            for ( i = 0; i < outputPortsBounds.length; ++i )
            {
                triangle.reset();
                if ( outputPortsBounds[i].x == 0 )
                {
                    triangle.addPoint( outputPortsBounds[i].getSize().width, outputPortsBounds[i].y );
                    triangle.addPoint( 0,
                            outputPortsBounds[i].y + outputPortsBounds[i].getSize().height / 2 );
                    triangle.addPoint( outputPortsBounds[i].getSize().width, outputPortsBounds[i].y + outputPortsBounds[i].getSize().height );
                }
                else if ( outputPortsBounds[i].x + outputPortsBounds[i].getSize().width == size.width - 1 )
                {
                    triangle.addPoint( outputPortsBounds[i].x,
                            outputPortsBounds[i].y );
                    triangle.addPoint( outputPortsBounds[i].x + outputPortsBounds[i].getSize().width,
                            outputPortsBounds[i].y + outputPortsBounds[i].getSize().height / 2 );
                    triangle.addPoint( outputPortsBounds[i].x,
                            outputPortsBounds[i].y + outputPortsBounds[i].getSize().height );
                }
                else if ( outputPortsBounds[i].y == 0 )
                {
                    triangle.addPoint( outputPortsBounds[i].x,
                            outputPortsBounds[i].y + outputPortsBounds[i].getSize().height );
                    triangle.addPoint( outputPortsBounds[i].x + outputPortsBounds[i].getSize().width / 2,
                            outputPortsBounds[i].y );
                    triangle.addPoint( outputPortsBounds[i].x + outputPortsBounds[i].getSize().width,
                            outputPortsBounds[i].y + outputPortsBounds[i].getSize().height );
                }
                else
                {
                    triangle.addPoint( outputPortsBounds[i].x,
                            outputPortsBounds[i].y );
                    triangle.addPoint( outputPortsBounds[i].x + outputPortsBounds[i].getSize().width / 2,
                            outputPortsBounds[i].y + outputPortsBounds[i].getSize().height );
                    triangle.addPoint( outputPortsBounds[i].x + outputPortsBounds[i].getSize().width,
                            outputPortsBounds[i].y );
                }
                g.setColor( outputPortsColor[i] );
                g.drawPolygon( triangle );
                g.fillPolygon( triangle );
            }
            if ( selected )
            {
                g.setColor( Color.red );
                g.drawRect( 0, 0, size.width - 1, size.height - 1 );
            }
            g.setColor( c );
        }


        /**
         *  Sets the inputPortsBounds attribute of the TapVisualModulePanel
         *  object
         *
         *@param  inputPortsBounds  The new inputPortsBounds value
         */
        public void setInputPortsBounds( Rectangle[] inputPortsBounds )
        {
            this.inputPortsBounds = inputPortsBounds;
        }


        /**
         *  Sets the outputPortsBounds attribute of the TapVisualModulePanel
         *  object
         *
         *@param  outputPortsBounds  The new outputPortsBounds value
         */
        public void setOutputPortsBounds( Rectangle[] outputPortsBounds )
        {
            this.outputPortsBounds = outputPortsBounds;
        }


        /**
         *  Sets the inputPortsColor attribute of the TapVisualModulePanel
         *  object
         *
         *@param  inputPortsColor  The new inputPortsColor value
         */
        public void setInputPortsColor( Color[] inputPortsColor )
        {
            this.inputPortsColor = inputPortsColor;
        }


        /**
         *  Sets the outputPortsColor attribute of the TapVisualModulePanel
         *  object
         *
         *@param  outputPortsColor  The new outputPortsColor value
         */
        public void setOutputPortsColor( Color[] outputPortsColor )
        {
            this.outputPortsColor = outputPortsColor;
        }


        /**
         *  Gets the toolTipText attribute of the TapVisualModulePanel object
         *
         *@param  where  Description of the Parameter
         *@return        The toolTipText value
         */
        public String getToolTipText( Point where )
        {
            int i;
            if ( inputPortsBounds != null )
                for ( i = 0; i < inputPortsBounds.length; ++i )
                    if ( inputPortsBounds[i].contains( where ) )
                    {
                        if ( inputPortsTooltips != null )
                            return inputPortsTooltips[i];
                        else
                            return "No tooltip";
                    }
            for ( i = 0; i < outputPortsBounds.length; ++i )
                if ( outputPortsBounds[i].contains( where ) )
                {
                    if ( outputPortsTooltips != null )
                        return outputPortsTooltips[i];
                    else
                        return "No tooltip";
                }

            return TapBTranslate.text( "clickSelectMove" );
        }


        /**
         *  Sets the inputPortsTooltips attribute of the TapVisualModulePanel
         *  object
         *
         *@param  inputPortsTooltips  The new inputPortsTooltips value
         */
        public void setInputPortsTooltips( String[] inputPortsTooltips )
        {
            this.inputPortsTooltips = inputPortsTooltips;
        }


        /**
         *  Sets the outputPortsTooltips attribute of the TapVisualModulePanel
         *  object
         *
         *@param  outputPortsTooltips  The new outputPortsTooltips value
         */
        public void setOutputPortsTooltips( String[] outputPortsTooltips )
        {
            this.outputPortsTooltips = outputPortsTooltips;
        }


        /**
         *  Sets the selected attribute of the TapVisualModulePanel object
         *
         *@param  selected  The new selected value
         */
        public void setSelected( boolean selected )
        {
            this.selected = selected;
        }


        /**
         *  Gets the selected attribute of the TapVisualModulePanel object
         *
         *@return    The selected value
         */
        public boolean getSelected()
        {
            return selected;
        }


        /**
         *  Sets the background attribute of the TapVisualModulePanel object
         *
         *@param  color  The new background value
         */
        @Override
        public void setBackground( Color color )
        {
            background = color;
        }
    }


    /**
     *  Description of the Class
     *
     *@author     francois
     *@created    23 avril 2004
     */
    private class ButtonContainer extends ExplicitContainer
    {

        /**
         *  Description of the Field
         */
        protected BButton button, previewButton;
        private Dimension preferredSize;


        /**
         *  Constructor for the ButtonContainer object
         */
        public ButtonContainer()
        {
            super();
            preferredSize = new Dimension();
            button = TapBTranslate.bButton( module.getName(), this, "doButtonEdit" );
            button.setFont( new java.awt.Font( "Dialog", 0, 10 ) );
            button.addEventLink( KeyPressedEvent.class, modulePanel, "doKeyPressed" );
            button.addEventLink( KeyReleasedEvent.class, modulePanel, "doKeyReleased" );
            button.addEventLink( ToolTipEvent.class, this, "doButtonToolTip" );
            ( (JButton) button.getComponent() ).setMargin( new Insets( 3, 3, 3, 3 ) );
            add( button, new Rectangle( button.getPreferredSize() ) );

            if ( module.acceptsPreview() )
            {
                previewButton = TapBTranslate.bButton( "P", this, "doButtonPreview" );
                previewButton.setFont( new java.awt.Font( "Dialog", 0, 10 ) );
                ( (JButton) previewButton.getComponent() ).setMargin( new Insets( 3, 3, 3, 3 ) );
                previewButton.addEventLink( KeyPressedEvent.class, modulePanel, "doKeyPressed" );
                previewButton.addEventLink( KeyReleasedEvent.class, modulePanel, "doKeyReleased" );
                previewButton.addEventLink( ToolTipEvent.class, this, "doPreviewToolTip" );
                add( previewButton, new Rectangle( previewButton.getPreferredSize() ) );

            }
            packButtons();

        }


        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void doButtonToolTip( ToolTipEvent ev )
        {
            new BToolTip( TapBTranslate.text( "clickEnterParameters" ) ).processEvent( ev );
        }


        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void doPreviewToolTip( ToolTipEvent ev )
        {
            new BToolTip( TapBTranslate.text( "clickDisplayPreview" ) ).processEvent( ev );
        }


        /**
         *  Description of the Method
         */
        public void doButtonEdit()
        {
            doEdit();
        }


        /**
         *  Description of the Method
         *
         *@param  ev  Description of the Parameter
         */
        public void doButtonPreview( CommandEvent ev )
        {
            doPreview( ev );
        }


        /**
         *  Description of the Method
         */
        public void packButtons()
        {

            Dimension size = button.getPreferredSize();
            Rectangle rect = new Rectangle( size );
            rect.x = rect.y = 7;
            setChildBounds( button, rect );

            if ( module.acceptsPreview() )
            {
                Dimension prevSize = previewButton.getPreferredSize();
                rect = new Rectangle( prevSize );
                prevSize.height = size.height;
                rect.x = size.width + 8;
                rect.y = 7;
                setChildBounds( previewButton, rect );
            }
            preferredSize.width = rect.x + rect.width + 7;
            preferredSize.height = rect.height + 15;
        }


        /**
         *  Gets the preferredSize attribute of the ButtonContainer object
         *
         *@return    The preferredSize value
         */
        @Override
        public Dimension getPreferredSize()
        {
            return preferredSize;
        }

    }
}

