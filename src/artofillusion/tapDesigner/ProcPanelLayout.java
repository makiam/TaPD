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


import artofillusion.*;
import buoy.widget.*;
import java.io.*;
import java.lang.reflect.*;



/**
 *  Description of the Class
 *
 *@author     Francois Guillet
 *@created    9 mai 2004
 */
public class ProcPanelLayout
{
    private int type;
    private ProcPanelLayout leftLayout;
    private ProcPanelLayout rightLayout;
    private String name;
    private ProcPanelLayoutData data;
    private double dividerLocation;
    private BSplitPane.Orientation orientation;

    /**
     *  Description of the Field
     */
    public final static int SPLITPANE = 0;
    /**
     *  Description of the Field
     */
    public final static int PARAMETERS_PANEL = 1;
    /**
     *  Description of the Field
     */
    public final static int MODULE_PANEL = 2;
    /**
     *  Description of the Field
     */
    public final static int PREVIEW_PANEL = 3;


    /**
     *  Constructor for the ProcPanelLayout object
     *
     *@param  wc  Description of the Parameter
     */
    public ProcPanelLayout( WidgetContainer wc )
    {
        construct( wc );
    }


    /**
     *  Constructor for the ProcPanelLayout object
     *
     *@param  wc    Description of the Parameter
     *@param  name  Description of the Parameter
     */
    public ProcPanelLayout( WidgetContainer wc, String name )
    {
        construct( wc );
        this.name = name;
    }


    /**
     *  Constructor for the construct object
     *
     *@param  wc  Description of the Parameter
     */
    private void construct( WidgetContainer wc )
    {
        orientation = BSplitPane.HORIZONTAL;
        name = "";
        if ( wc instanceof TapView )
            data = ( (TapView) wc ).getLayoutData();
        if ( wc instanceof BScrollPane )
            wc = (WidgetContainer) ( (BScrollPane) wc ).getContent();
        if ( wc instanceof TapModulePanel )
            type = MODULE_PANEL;
        else if ( wc instanceof TapParametersPanel )
            type = PARAMETERS_PANEL;
        else if ( wc instanceof PreviewTapView )
            type = PREVIEW_PANEL;
        else if ( wc instanceof BSplitPane )
        {
            type = SPLITPANE;
            BSplitPane sp = (BSplitPane) wc;
            orientation = sp.getOrientation();
            leftLayout = new ProcPanelLayout( (WidgetContainer) sp.getChild( 0 ) );
            rightLayout = new ProcPanelLayout( (WidgetContainer) sp.getChild( 1 ) );
            int loc = sp.getDividerLocation();
            int size = ( sp.getOrientation() == BSplitPane.HORIZONTAL ?
                    sp.getComponent().getSize().width : sp.getComponent().getSize().height );
            dividerLocation = 0.5;
            if ( size != 0 )
                dividerLocation = ( (double) loc ) / ( (double) size );
        }
        else
            System.out.println( "Warning : unknown widget type : " + wc );
    }


    /**
     *  Description of the Method
     *
     *@param  procPanel  Description of the Parameter
     *@return            Description of the Return Value
     */
    public Widget createWidget( TapProcPanel procPanel )
    {
        switch ( type )
        {
            case MODULE_PANEL:
                TapModulePanel tmp = new TapModulePanel( procPanel );
                procPanel.register( tmp );
                return tmp.newScrollPane();
            case PARAMETERS_PANEL:
                TapParametersPanel tpp = new TapParametersPanel( procPanel );
                tpp.setLayoutData( data );
                procPanel.register( tpp );
                return tpp;
            case PREVIEW_PANEL:
                PreviewTapView ptv = new PreviewTapView( procPanel );
                ptv.setLayoutData( data );
                procPanel.register( ptv );
                return ptv;
            case SPLITPANE:
                BSplitPane sp = new BSplitPane( orientation, leftLayout.createWidget( procPanel ),
                        rightLayout.createWidget( procPanel ) );
                sp.setOneTouchExpandable( true );
                sp.setResizeWeight( 0.5 );
                sp.setDividerLocation( dividerLocation );
                return sp;
        }
        return null;
    }


    /**
     *  Gets the name attribute of the ProcPanelLayout object
     *
     *@return    The name value
     */
    public String getName()
    {
        return name;
    }


    /**
     *  Description of the Method
     *
     *@param  out              Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void writeToFile( DataOutputStream out )
        throws IOException
    {
        out.writeShort( 1 );
        out.writeInt( type );
        out.writeUTF( name );
        if ( data != null )
        {
            out.writeBoolean( true );
            out.writeUTF( data.getClass().getName() );
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            data.writeToFile( new DataOutputStream( bos ) );
            byte[] bytes = bos.toByteArray();
            out.writeInt( bytes.length );
            out.write( bytes, 0, bytes.length );
        }
        else
            out.writeBoolean( false );
        if ( orientation == BSplitPane.HORIZONTAL )
            out.writeBoolean( true );
        else
            out.writeBoolean( false );
        out.writeDouble( dividerLocation );
        if ( leftLayout == null )
            out.writeBoolean( false );
        else
        {
            out.writeBoolean( true );
            leftLayout.writeToFile( out );
        }
        if ( rightLayout == null )
            out.writeBoolean( false );
        else
        {
            out.writeBoolean( true );
            rightLayout.writeToFile( out );
        }
    }


    /**
     *  Constructor for the ProcPanelLayout object
     *
     *@param  in                          Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public ProcPanelLayout( DataInputStream in )
        throws IOException, InvalidObjectException
    {
        boolean b;

        short version = in.readShort();
        if ( ( version < 0 ) || ( version > 1 ) )
        {
            throw new InvalidObjectException( "" );
        }
        type = in.readInt();
        name = in.readUTF();
        if ( version > 0 )
        {
            boolean hasData = in.readBoolean();
            if ( hasData )
            {
                String classname = in.readUTF();
                classname = classname.replaceFirst( ".tools.tapDesigner", ".tapDesigner" );
                int len = in.readInt();
                byte[] bytes = new byte[len];
                in.readFully( bytes );

                try
                {
                    Class cls = ModellingApp.getClass( classname );
                    if ( cls == null )
                        throw new IOException( "Unknown class: " + classname );

                    Constructor con = cls.getConstructor( new Class[]
                            {
                            DataInputStream.class
                            } );

                    data = (ProcPanelLayoutData) con.newInstance( new Object[]
                            {
                            new DataInputStream( new ByteArrayInputStream( bytes ) )
                            } );

                }
                catch ( InvocationTargetException ex )
                {
                    ex.getTargetException().printStackTrace();
                    throw new IOException();
                }
                catch ( Exception ex )
                {
                    ex.printStackTrace();
                    throw new IOException();
                }
            }
            else
                data = null;
        }
        b = in.readBoolean();
        if ( b )
            orientation = BSplitPane.HORIZONTAL;
        else
            orientation = BSplitPane.VERTICAL;
        dividerLocation = in.readDouble();
        b = in.readBoolean();
        if ( b )
            leftLayout = new ProcPanelLayout( in );
        b = in.readBoolean();
        if ( b )
            rightLayout = new ProcPanelLayout( in );
    }
}

