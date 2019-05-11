package artofillusion.tapDesigner;

import buoy.event.*;
import buoy.widget.*;
import buoy.widget.BScrollPane.*;
import buoy.internal.*;
import buoy.xml.*;
import buoy.xml.delegate.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *  This customized BScrollPane keeps its contents centered if the contents size
 *  is smaller than the content view size. The modified code lies in
 *  layoutChildren(). See BScrollPane documentation for more info. The layout
 *  functionality might be integrated in Buoy 1.1
 *
 *@author     François Guillet
 *@created    10 mai 2004
 */

public class CenteredBScrollPane extends BScrollPane
{

    /**
     *  Constructor for the CenteredBScrollPane object
     */
    public CenteredBScrollPane()
    {
        super();
    }


    /**
     *  Constructor for the CenteredBScrollPane object
     *
     *@param  contentWidget  Description of the Parameter
     */
    public CenteredBScrollPane( Widget contentWidget )
    {
        super( contentWidget );
    }


    /**
     *  Constructor for the CenteredBScrollPane object
     *
     *@param  horizontalPolicy  Description of the Parameter
     *@param  verticalPolicy    Description of the Parameter
     */
    public CenteredBScrollPane( BScrollPane.ScrollbarPolicy horizontalPolicy, BScrollPane.ScrollbarPolicy verticalPolicy )
    {
        super( horizontalPolicy, verticalPolicy );
    }


    /**
     *  Create a new BScrollPane with the specified Widget as its content.
     *
     *@param  contentWidget     the Widget to use as the content of the
     *      BScrollPane
     *@param  horizontalPolicy  specifies when the horizontal scrollbar should
     *      be displayed. This should be equal to SCROLLBAR_ALWAYS,
     *      SCROLLBAR_AS_NEEDED, or SCROLLBAR_NEVER.
     *@param  verticalPolicy    specifies when the vertical scrollbar should be
     *      displayed. This should be equal to SCROLLBAR_ALWAYS,
     *      SCROLLBAR_AS_NEEDED, or SCROLLBAR_NEVER.
     */

    public CenteredBScrollPane( Widget contentWidget, BScrollPane.ScrollbarPolicy horizontalPolicy, BScrollPane.ScrollbarPolicy verticalPolicy )
    {
        super( contentWidget, horizontalPolicy, verticalPolicy );
    }


    /**
     *  Layout the child Widgets. This may be invoked whenever something has
     *  changed (the size of this WidgetContainer, the preferred size of one of
     *  its children, etc.) that causes the layout to no longer be correct. If a
     *  child is itself a WidgetContainer, its layoutChildren() method will be
     *  called in turn. Original code by Peter Eastman for Buoy
     */
    public void layoutChildren()
    {

        int topMargin = 0;
        int leftMargin = 0;
        int bottomMargin = 0;
        int rightMargin = 0;
        Widget rowHeader = getRowHeader();
        Widget colHeader = getColHeader();
        BScrollBar hscroll = getHorizontalScrollBar();
        BScrollBar vscroll = getVerticalScrollBar();
        BScrollPane.ScrollbarPolicy hPolicy = getHorizontalScrollbarPolicy();
        BScrollPane.ScrollbarPolicy vPolicy = getVerticalScrollbarPolicy();
        boolean forceWidth = getForceWidth();
        boolean forceHeight = getForceHeight();
        WidgetContainerPanel panel = (WidgetContainerPanel) getComponent();
        JViewport rowHeaderPort = (JViewport) panel.getComponent( 1 );
        JViewport colHeaderPort = (JViewport) panel.getComponent( 2 );
        JViewport contentPort = (JViewport) panel.getComponent( 0 );

        Widget content = getContent();
        Dimension colHeaderSize = ( colHeader == null ? null : colHeader.getPreferredSize() );
        Dimension rowHeaderSize = ( rowHeader == null ? null : rowHeader.getPreferredSize() );
        Dimension hScrollSize = hscroll.getPreferredSize();
        Dimension vScrollSize = vscroll.getPreferredSize();
        Dimension contentSize = ( content == null ? new Dimension() : content.getPreferredSize() );
        Rectangle bounds = getBounds();

        // Find the margins.

        if ( colHeaderSize != null )
            topMargin = colHeaderSize.height;
        if ( rowHeaderSize != null )
            leftMargin = rowHeaderSize.width;
        boolean hasHScroll = ( hPolicy == SCROLLBAR_ALWAYS || ( hPolicy == SCROLLBAR_AS_NEEDED && bounds.width - leftMargin - vScrollSize.width < contentSize.width ) );
        boolean hasVScroll = ( vPolicy == SCROLLBAR_ALWAYS || ( vPolicy == SCROLLBAR_AS_NEEDED && bounds.height - topMargin - hScrollSize.height < contentSize.height ) );
        if ( hasHScroll )
            bottomMargin = hScrollSize.height;
        if ( hasVScroll )
            rightMargin = vScrollSize.width;
        if ( hPolicy == SCROLLBAR_AS_NEEDED && vPolicy == SCROLLBAR_AS_NEEDED && bounds.width - leftMargin >= contentSize.width && bounds.height - topMargin >= contentSize.height )
        {
            hasHScroll = false;
            hasVScroll = false;
            bottomMargin = 0;
            rightMargin = 0;
        }
        Rectangle viewBounds = new Rectangle( leftMargin, topMargin, bounds.width - leftMargin - rightMargin, bounds.height - topMargin - bottomMargin );

        // Set the size of the headers.

        colHeaderPort.setBounds( new Rectangle( leftMargin, 0, viewBounds.width, topMargin ) );
        if ( colHeader != null )
        {
            Dimension size = new Dimension( colHeaderSize );
            if ( forceWidth )
            {
                if ( size.width < viewBounds.width || hPolicy == SCROLLBAR_NEVER )
                    size.width = viewBounds.width;
            }
            colHeader.getComponent().setSize( size );
        }
        rowHeaderPort.setBounds( new Rectangle( 0, topMargin, leftMargin, viewBounds.height ) );
        if ( rowHeader != null )
        {
            Dimension size = new Dimension( rowHeaderSize );

            if ( forceHeight )
            {
                if ( size.height < viewBounds.height || vPolicy == SCROLLBAR_NEVER )
                    size.height = viewBounds.height;
            }

            rowHeader.getComponent().setSize( size );
        }

        // Set the size of the content Widget.

        contentPort.setBounds( viewBounds );
        if ( content != null )
        {
            Dimension size = new Dimension( contentSize );
            if ( forceWidth )
            {
                if ( size.width < viewBounds.width || hPolicy == SCROLLBAR_NEVER )
                    size.width = viewBounds.width;
            }
            if ( forceHeight )
            {
                if ( size.height < viewBounds.height || vPolicy == SCROLLBAR_NEVER )
                    size.height = viewBounds.height;
            }
            content.getComponent().setSize( size );
        }

        // Set up the scrollbars.

        hscroll.getComponent().setBounds( new Rectangle( leftMargin, viewBounds.y + viewBounds.height, viewBounds.width, bottomMargin ) );
        if ( content == null )
            hscroll.setEnabled( false );
        else
        {
            hscroll.setEnabled( true );
            int width = content.getComponent().getWidth();
            hscroll.setMaximum( width );
            if ( hscroll.getValue() + viewBounds.width > width )
                hscroll.setValue( width - viewBounds.width );
        }
        hscroll.setExtent( viewBounds.width );
        vscroll.getComponent().setBounds( new Rectangle( viewBounds.x + viewBounds.width, topMargin, rightMargin, viewBounds.height ) );
        if ( content == null )
            vscroll.setEnabled( false );
        else
        {
            vscroll.setEnabled( true );
            int height = content.getComponent().getHeight();
            vscroll.setMaximum( height );
            if ( vscroll.getValue() + viewBounds.height > height )
                vscroll.setValue( height - viewBounds.height );
        }
        vscroll.setExtent( viewBounds.height );

        // Layout any child containers.

        if ( content instanceof WidgetContainer )
            ( (WidgetContainer) content ).layoutChildren();
        if ( colHeader instanceof WidgetContainer )
            ( (WidgetContainer) colHeader ).layoutChildren();
        if ( rowHeader instanceof WidgetContainer )
            ( (WidgetContainer) rowHeader ).layoutChildren();
    }

}

