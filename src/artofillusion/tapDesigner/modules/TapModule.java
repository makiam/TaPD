/*
 *  This classe is abstract and implements the methods all kinds of modules must have.
 *  The subclasses represent specific kinds of modules.
 */
/*
 *  Copyright (C) 2003 by Francois Guillet
 *  Changes copyright (C) 2021 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package artofillusion.tapDesigner;

import artofillusion.*;
import artofillusion.animation.*;
import artofillusion.math.*;
import artofillusion.object.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import buoy.widget.*;


/**
 *  This class is abstract and implements the methods all kinds of modules must
 *  have. Some methods are abstract, some others implement a default behavior.
 *  The subclasses represent specific kinds of modules.
 *
 *@author     Fran�ois Guillet
 *@created    19 avril 2004
 */
public abstract class TapModule
{
    /**
     *  Description of the Field
     */
    protected TapVisualModule visualModule;
    /**
     *  Description of the Field
     */
    protected Vector modules;
    //the full module list
    /**
     *  Description of the Field
     */
    protected int[][] linkToIndex;
    //an array of module references this module is linked to (input)
    /**
     *  Description of the Field
     */
    protected int[][] inputPortLink;
    //an array of module outport references this module is linked to (input)
    /**
     *  Description of the Field
     */
    protected int numInput;
    //number of inputs or outputs
    /**
     *  Description of the Field
     */
    protected int numOutput;
    /**
     *  Description of the Field
     */
    protected int[] inputNature;
    //i.e. value or object
    /**
     *  Description of the Field
     */
    protected int[] outputNature;
    //same for output ports
    /**
     *  Description of the Field
     */
    protected String name;
    /**
     *  Description of the Field
     */
    protected Point location;
    private Color backgroundColor;
    /**
     *  Description of the Field
     */
    protected String[] inputTooltips;
    /**
     *  Description of the Field
     */
    protected String[] outputTooltips;
    private TapPreviewWindow fr;
    private boolean mainEntry;
    /**
     *  Description of the Field
     */
    protected boolean isPreviewOn;
    /**
     *  Description of the Field
     */
    protected boolean isEditDialogOn;
    /**
     *  Description of the Field
     */
    protected JFrame editDialog;
    //one of these two variables is used depending on using Swing or Buoy
    /**
     *  Description of the Field
     */
    protected BFrame editBDialog;
    /**
     *  Description of the Field
     */
    protected int portDecoration;
    //ports layout
    /**
     *  Description of the Field
     */
    protected TapProcedure procedure;
    /**
     *  used for partial (ctrl-click) previews
     */
    protected boolean stopHere;
    //used for partial (ctrl-click) previews
    /**
     */
    protected int modifiers;
    /**
     *  Description of the Field
     */
    protected boolean testMode;
    /**
     *  Description of the Field
     */
    protected boolean changed;
    final static int NULL_PORT = 0;
    final static int OBJECT_PORT = 1;
    final static int VALUE_PORT = 2;
    /**
     *  Left to right port decoration
     */
    public final static int LEFT_TO_RIGHT = 0;
    //ports layout
    /**
     *  Description of the Field
     */
    public final static int TOP_TO_BOTTOM = 1;
    /**
     *  Description of the Field
     */
    public final static int RIGHT_TO_LEFT = 2;
    /**
     *  Description of the Field
     */
    public final static int BOTTOM_TO_TOP = 3;


    /**
     *  Constructor for the TapModule object
     *
     *@param  procedure  The procedure to which the module belongs to
     *@param  name       The name of the module
     *@param  location   The graphical location of the module
     */
    public TapModule( TapProcedure procedure, String name, Point location )
    {
        this.procedure = procedure;
        this.name = name;
        this.modules = procedure.getModules();
        this.location = location;
        backgroundColor = Color.lightGray;
        linkToIndex = null;
        inputPortLink = null;
        inputTooltips = null;
        outputTooltips = null;
        fr = null;
        numInput = 0;
        numOutput = 0;
        isPreviewOn = false;
        isEditDialogOn = false;
        mainEntry = false;
        portDecoration = LEFT_TO_RIGHT;
        stopHere = false;
    }


    /**
     *  Constructor for the TapModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public TapModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        short version;
        int tmp;
        version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        procedure = null;
        name = in.readUTF();
        mainEntry = in.readBoolean();
        location = new Point();
        location.x = in.readInt();
        location.y = in.readInt();

        int R = in.readInt();
        int G = in.readInt();
        int B = in.readInt();
        backgroundColor = new Color( R, G, B );
        portDecoration = in.readInt();
        numInput = in.readInt();
        numOutput = in.readInt();
        setNumInput( numInput );
        setNumOutput( numOutput );

        for ( int i = 0; i < numOutput; ++i )
        {
            tmp = in.readInt();

            if ( tmp > 0 )
            {
                linkToIndex[i] = new int[tmp];

                for ( int j = 0; j < tmp; ++j )
                    linkToIndex[i][j] = in.readInt();
            }

            tmp = in.readInt();

            if ( tmp > 0 )
            {
                inputPortLink[i] = new int[tmp];

                for ( int j = 0; j < tmp; ++j )
                    inputPortLink[i][j] = in.readInt();
            }
        }

        fr = null;
        isPreviewOn = false;
        isEditDialogOn = false;
        stopHere = false;
    }


    /**
     *  Description of the Method
     *
     *@param  out              Description of the Parameter
     *@param  theScene         Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void writeToFile( DataOutputStream out, Scene theScene )
        throws IOException
    {
        out.writeShort( 0 );
        out.writeUTF( name );
        out.writeBoolean( mainEntry );
        out.writeInt( location.x );
        out.writeInt( location.y );
        out.writeInt( backgroundColor.getRed() );
        out.writeInt( backgroundColor.getGreen() );
        out.writeInt( backgroundColor.getBlue() );
        out.writeInt( portDecoration );
        out.writeInt( numInput );
        out.writeInt( numOutput );

        for ( int i = 0; i < numOutput; ++i )
        {
            if ( linkToIndex[i] != null )
            {
                out.writeInt( linkToIndex[i].length );

                for ( int j = 0; j < linkToIndex[i].length; ++j )
                    out.writeInt( linkToIndex[i][j] );
            }
            else
                out.writeInt( 0 );

            if ( inputPortLink[i] != null )
            {
                out.writeInt( inputPortLink[i].length );

                for ( int j = 0; j < inputPortLink[i].length; ++j )
                    out.writeInt( inputPortLink[i][j] );
            }
            else
                out.writeInt( 0 );

        }

    }


    /*
     *  used to debug
     */
    /**
     *  Description of the Method
     */
    public void printSelf()
    {
        System.out.println( this );

        if ( inputPortLink[0] != null )
            for ( int i = 0; i < inputPortLink[0].length; ++i )
                System.out.println( "To : " + linkToIndex[0][i] + " Port: " + inputPortLink[0][i] );
    }


    /**
     *  Sets the modules attribute of the TapModule object
     *
     *@param  modules  The new modules value
     */
    public void setModules( Vector modules )
    {
        this.modules = modules;
    }


    /**
     *  Gets the modules attribute of the TapModule object
     *
     *@return    The modules value
     */
    public Vector getModules()
    {
        return modules;
    }


    /**
     *  Gets the procedure attribute of the TapModule object
     *
     *@return    The procedure value
     */
    public TapProcedure getProcedure()
    {
        return procedure;
    }


    /**
     *  Sets the procedure attribute of the TapModule object
     *
     *@param  proc  The new procedure value
     */
    public void setProcedure( TapProcedure proc )
    {
        this.procedure = proc;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public abstract TapModule duplicate();


    /**
     *  Description of the Method
     *
     *@param  offset  Description of the Parameter
     *@return         Description of the Return Value
     */
    public TapModule duplicate( int offset )
    {
        return duplicate();
    }



    /*
     *  copy the content of module into this
     */
    /**
     *  Description of the Method
     *
     *@param  module  Description of the Parameter
     */
    public void copyModule( TapModule module )
    {
        this.numInput = module.numInput;
        this.numOutput = module.numOutput;
        this.name = module.name;
        this.portDecoration = module.portDecoration;
        linkToIndex = new int[numOutput][];
        inputPortLink = new int[numOutput][];

        for ( int i = 0; i < numOutput; ++i )
        {
            if ( module.linkToIndex[i] != null )
            {
                this.linkToIndex[i] = new int[module.linkToIndex[i].length];
                this.inputPortLink[i] = new int[module.inputPortLink[i].length];

                for ( int j = 0; j < module.linkToIndex[i].length; ++j )
                {
                    this.linkToIndex[i][j] = module.linkToIndex[i][j];
                    this.inputPortLink[i][j] = module.inputPortLink[i][j];
                }
            }
        }

        this.modules = module.modules;
    }


    /**
     *  Gets the location attribute of the TapModule object
     *
     *@return    The location value
     */
    public Point getLocation()
    {
        Point loc = new Point( location );
        return loc;
    }


    /**
     *  Sets the location attribute of the TapModule object
     *
     *@param  location  The new location value
     */
    public void setLocation( Point location )
    {
        this.location = location;
    }


    /**
     *  Gets the portDecoration attribute of the TapModule object
     *
     *@return    The portDecoration value
     */
    public int getPortDecoration()
    {
        return portDecoration;
    }


    /**
     *  Sets the portDecoration attribute of the TapModule object
     *
     *@param  portDecoration  The new portDecoration value
     */
    public void setPortDecoration( int portDecoration )
    {
        this.portDecoration = portDecoration;
    }


    /**
     *  Gets the name attribute of the TapModule object
     *
     *@return    The name value
     */
    public String getName()
    {
        return name;
    }


    /**
     *  Sets the name attribute of the TapModule object
     *
     *@param  name  The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }


    /**
     *  Sets the numOutput attribute of the TapModule object
     *
     *@param  numOutput  The new numOutput value
     */
    protected void setNumOutput( int numOutput )
    {
        this.numOutput = numOutput;
        linkToIndex = new int[numOutput][];
        inputPortLink = new int[numOutput][];
        outputNature = new int[numOutput];

        for ( int i = 0; i < numOutput; ++i )
        {
            linkToIndex[i] = null;
            inputPortLink[i] = null;
            outputNature[i] = NULL_PORT;
        }
    }


    /**
     *  Gets the numOutput attribute of the TapModule object
     *
     *@return    The numOutput value
     */
    public int getNumOutput()
    {
        return numOutput;
    }


    /**
     *  Sets the numInput attribute of the TapModule object
     *
     *@param  numInput  The new numInput value
     */
    protected void setNumInput( int numInput )
    {
        this.numInput = numInput;

        if ( numInput > 0 )
            inputNature = new int[numInput];

        for ( int i = 0; i < numInput; ++i )
            inputNature[i] = NULL_PORT;
    }


    /**
     *  Gets the numInput attribute of the TapModule object
     *
     *@return    The numInput value
     */
    public int getNumInput()
    {
        return numInput;
    }


    /*
     *  Get a list of the input ports for this module.
     */
    /**
     *  Gets the inputNature attribute of the TapModule object
     *
     *@return    The inputNature value
     */
    public int[] getInputNature()
    {
        return inputNature;
    }


    /**
     *  Gets the inputNature attribute of the TapModule object
     *
     *@param  i  Description of the Parameter
     *@return    The inputNature value
     */
    public int getInputNature( int i )
    {
        return inputNature[i];
    }


    /*
     *  Get a list of the output ports for this module.
     */
    /**
     *  Gets the outputNature attribute of the TapModule object
     *
     *@return    The outputNature value
     */
    public int[] getOutputNature()
    {
        return outputNature;
    }


    /**
     *  Gets the outputNature attribute of the TapModule object
     *
     *@param  i  Description of the Parameter
     *@return    The outputNature value
     */
    public int getOutputNature( int i )
    {
        return outputNature[i];
    }


    /*
     *  links the output port of this instance to the input port of another module
     */
    /**
     *  Sets the link attribute of the TapModule object
     *
     *@param  toModule    The new link value
     *@param  outputPort  The new link value
     *@param  inputPort   The new link value
     */
    public void setLink( int toModule, int outputPort, int inputPort )
    {
        boolean add_link = true;

        if ( linkToIndex[outputPort] != null )
        {
            for ( int i = 0; i < linkToIndex[outputPort].length; ++i )
                if ( ( linkToIndex[outputPort][i] == toModule ) && ( inputPortLink[outputPort][i] == inputPort ) )
                    add_link = false;
        }

        if ( add_link )
        {
            linkToIndex[outputPort] = TapUtils.increaseIntArray( linkToIndex[outputPort] );
            inputPortLink[outputPort] = TapUtils.increaseIntArray( inputPortLink[outputPort] );

            int l = linkToIndex[outputPort].length - 1;
            linkToIndex[outputPort][l] = toModule;
            inputPortLink[outputPort][l] = inputPort;
            ( (TapModule) modules.elementAt( toModule ) ).newInputLinkCreated();
            this.newOutputLinkCreated();
            procedure.setModified( true );
        }
    }


    /**
     *  Returns the index of the module to which the ith output port of this
     *  module is linked
     *
     *@param  outputPort  Description of the Parameter
     *@param  i           Description of the Parameter
     *@return             The linkToIndex value
     */
    public int getLinkToIndex( int outputPort, int i )
    {
        if ( linkToIndex == null )
            return -1;

        if ( linkToIndex[outputPort] == null )
            return -1;

        if ( i >= linkToIndex[outputPort].length )
            return -1;
        else
            return linkToIndex[outputPort][i];
    }


    /**
     *  Returns the index of the input port of the module to which the ith
     *  output port of this module is linked
     *
     *@param  outputPort  Output port index
     *@param  i           Index of this output port link
     *@return             The index of the module this link is linked to
     */
    public int getInputPortLink( int outputPort, int i )
    {
        if ( inputPortLink == null )
            return -1;

        if ( inputPortLink[outputPort] == null )
            return -1;

        if ( i >= inputPortLink[outputPort].length )
            return -1;
        else
            return inputPortLink[outputPort][i];
    }


    /**
     *  Returns the number of links a specified output port has
     *
     *@param  outputPort  Output port index
     *@return             The number of links
     */
    public int getNumLinks( int outputPort )
    {
        if ( inputPortLink == null )
            return -1;

        if ( inputPortLink[outputPort] == null )
            return -1;

        return inputPortLink[outputPort].length;
    }


    /**
     *  Deletes a link between this module and another module
     *
     *@param  outputPort  Output port index
     *@param  i           Index of this output port link to delete
     */
    public void deleteLink( int outputPort, int i )
    {
        linkToIndex[outputPort] = TapUtils.deleteIntArrayElement( linkToIndex[outputPort], i );
        inputPortLink[outputPort] = TapUtils.deleteIntArrayElement( inputPortLink[outputPort], i );
        procedure.setModified( true );
    }


    /**
     *  If the modules vector is modified, links reference table must be
     *  updated. the translationTable parameter is such that newindex =
     *  translationTable[index]. If newIndex == -1, then this module is going to
     *  be deleted, and the link must be destroyed.
     *
     *@param  translationTable  Translation table to apply
     */
    public void applyTranslation( int[] translationTable )
    {
        for ( int i = 0; i < numOutput; ++i )
            if ( linkToIndex[i] != null )
            {
                int ref = 0;
                boolean go_on = true;

                while ( go_on )
                {
                    if ( translationTable[linkToIndex[i][ref]] == -1 )
                    {
                        linkToIndex[i] = TapUtils.deleteIntArrayElement( linkToIndex[i], ref );
                        inputPortLink[i] = TapUtils.deleteIntArrayElement( inputPortLink[i], ref );
                    }
                    else
                    {
                        linkToIndex[i][ref] = translationTable[linkToIndex[i][ref]];
                        ++ref;
                    }

                    if ( linkToIndex[i] == null )
                        go_on = false;
                    else if ( ref == linkToIndex[i].length )
                        go_on = false;
                }
            }
    }


    /**
     *  Do some cleanup before deletion
     */
    public void prepareToBeDeleted()
    {
        for ( int i = 0; i < numOutput; ++i )
        {
            linkToIndex[i] = null;
            inputPortLink[i] = null;
        }

        linkToIndex = null;
        inputPortLink = null;

        if ( isEditDialogOn )
            editDialogClosed();
        if ( isPreviewOn )
            fr.dispose();
    }


    /**
     *  the remap method is used when ports are added or deleted dynamically,
     *  presumably because the file being read has been written by a former
     *  version and ports have been added since. remap must return the new index
     *  of the port. This method must be overridden by modules which actually
     *  remap the ports
     *
     *@param  inputPort  old input port value
     *@return            new input port value
     */
    public int remap( int inputPort )
    {
        return inputPort;
    }


    /**
     *  Even if a module has no remap to do, the link tables have to be
     *  remapped...
     */
    public void remapOutput()
    {
        for ( int i = 0; i < numOutput; ++i )
        {
            if ( linkToIndex[i] != null )
                for ( int j = 0; j < linkToIndex[i].length; ++j )
                    inputPortLink[i][j] = ( (TapModule) modules.elementAt( linkToIndex[i][j] ) ).remap( inputPortLink[i][j] );
        }
    }


    /**
     *  Sets the backgroundColor attribute of the TapModule object
     *
     *@param  backgroundColor  The new backgroundColor value
     */
    protected void setBackgroundColor( Color backgroundColor )
    {
        this.backgroundColor = backgroundColor;
    }


    /**
     *  Gets the backgroundColor attribute of the TapModule object
     *
     *@return    The backgroundColor value
     */
    public Color getBackgroundColor()
    {
        return backgroundColor;
    }


    /**
     *  Gets the inputTooltips attribute of the TapModule object
     *
     *@return    The inputTooltips value
     */
    public String[] getInputTooltips()
    {
        return inputTooltips;
    }


    /**
     *  Gets the outputTooltips attribute of the TapModule object
     *
     *@return    The outputTooltips value
     */
    public String[] getOutputTooltips()
    {
        return outputTooltips;
    }


    /**
     *  Returns the visual module associated to this module
     *
     *@return    The visual module
     */
    public TapVisualModule getVisualModule()
    {
        return visualModule;
    }


    /**
     *  Sets the visualModule attribute of the TapModule object
     *
     *@param  vmod  The new visualModule value
     */
    public void setVisualModule( TapVisualModule vmod )
    {
        visualModule = vmod;
    }


    /**
     *  Gets the mainEntry attribute of the TapModule object
     *
     *@return    The mainEntry value
     */
    public boolean isMainEntry()
    {
        return mainEntry;
    }


    /**
     *  Sets the mainEntry attribute of the TapModule object
     *
     *@param  mainEntry  The new mainEntry value
     */
    public void setMainEntry( boolean mainEntry )
    {
        this.mainEntry = mainEntry;
    }


    /*
     *  the following method is called every time a new input link to this module is created
     */
    /**
     *  Description of the Method
     */
    public void newInputLinkCreated()
    {
    }


    /*
     *  the following method is called every time a new ouput link from this module is created
     */
    /**
     *  Description of the Method
     */
    public void newOutputLinkCreated()
    {
    }


    /*
     *  Anything there is to do before calculating the TaPD object is done by overriding this method
     */
    /**
     *  Description of the Method
     */
    public void initGenerationProcess()
    {
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    public void edit( BFrame parentFrame )
    {

    }


    /**
     *  Description of the Method
     *
     *@param  modifiers  Description of the Parameter
     */
    public void showPreviewFrame( int modifiers )
    {
        this.modifiers = modifiers;
    }


    /**
     *  Gets the scene attribute of the TapModule object
     *
     *@param  object  Description of the Parameter
     *@return         The scene value
     */
    public Scene getScene( ObjectInfo object )
    {
        // set up a new temporary scene
        Scene previewScene = new Scene();
        CoordinateSystem coords = new CoordinateSystem( new Vec3( 0.0, 0.0,  Camera.DEFAULT_DISTANCE_TO_SCREEN ), new Vec3( 0.0, 0.0, -1.0 ), Vec3.vy() );
        ObjectInfo info;

        info = new ObjectInfo( new SceneCamera(), coords, "Camera 1" );
        info.addTrack( new PositionTrack( info ), 0 );
        info.addTrack( new RotationTrack( info ), 1 );

        previewScene.addObject( info, null );
        info = new ObjectInfo( new DirectionalLight( new RGBColor( 1.0f, 1.0f, 1.0f ), 0.8f ), coords.duplicate(), "Light 1" );
        info.addTrack( new PositionTrack( info ), 0 );
        info.addTrack( new RotationTrack( info ), 1 );
        previewScene.addObject( info, null );
        object = object.duplicate();
        previewScene.addObject( object, null );
        BoundingBox bounds = object.getBounds();
        Vec3 size = bounds.getSize();
        object.coords.setOrigin( new Vec3( 0, -size.y / 2, 0 ) );

        return previewScene;
    }


    /**
     *  Gets the previewScene attribute of the TapModule object
     *
     *@param  upTo  Description of the Parameter
     *@return       The previewScene value
     */
    public Scene getPreviewScene( boolean upTo )
    {
        int upSelecter = -1;
        if ( upTo )
            upSelecter = -2;
        TapDesignerObjectCollection collection = null;
        procedure.initProcedure();

        collection = getObject( upSelecter, procedure.getSeed() );
        if ( collection != null )
        {
            if ( collection.size() > 0 )
            {
                ObjectInfo newObjectInfo = new ObjectInfo( collection, new CoordinateSystem(), getName() );
                newObjectInfo.object.setTexture( procedure.getScene().getDefaultTexture(), procedure.getScene().getDefaultTexture().getDefaultMapping(newObjectInfo.object) );
                // set up a new temporary scene
                Scene previewScene = getScene( newObjectInfo );
                return previewScene;
            }
        }
        return null;
    }


    /**
     *  Description of the Method
     */
    public void updatePreviewFrame()
    {
        if ( isPreviewOn )
        {
            showPreviewFrame( modifiers );
        }
    }


    /**
     *  Gets the object attribute of the TapModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The object value
     */
    public TapDesignerObjectCollection getObject( int outputPort, long seed )
    {
        System.out.println( "getObject(int outputPort, long seed) called in TapModule. Should never happen !" );

        return null;
    }


    /**
     *  Gets the object attribute of the TapModule object
     *
     *@param  collection  Description of the Parameter
     *@param  inputPort   Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The object value
     */
    public TapDesignerObjectCollection getObject( TapDesignerObjectCollection collection, int inputPort, long seed )
    {
        System.out.println( "getObject(TapDesignerObjectCollection collection, int inputPort, long seed) called in TapModule. Should never happen !" );

        return null;
    }


    /*
     *  getValue returns a value the calculation of which is based on the values of the var array.
     *  A null var array means that a constant value is requested. A var array of length 1 means that
     *  the Y or R value is passed in the array, a length of 2 corresponds to Y and R.
     */
    /**
     *  Gets the value attribute of the TapModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  var         Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The value value
     */
    public double getValue( int outputPort, double[] var, long seed )
    {
        System.out.println( "getValue(int outputPort, double[] var, long seed) called in TapModule. Should never happen !" );

        return 0.0;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public abstract boolean acceptsMainEntry();


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public abstract boolean acceptsPreview();


    /**
     *  Description of the Method
     */
    public void doApply()
    {
        procedure.setModified( true );
        procedure.doLiveUpdate();
    }


    /**
     *  Description of the Method
     */
    public void closeAllWindows()
    {
        if ( isPreviewOn )
            fr.dispose();

        if ( isEditDialogOn )
        {
            if ( editDialog != null )
                editDialog.dispose();
            else
                editBDialog.dispose();
        }

        isPreviewOn = false;
        isEditDialogOn = false;
    }


    /**
     *  Description of the Method
     */
    public void previewWindowClosed()
    {
        isPreviewOn = false;
        fr = null;
    }


    /*
     *  even if opening an edit dialog must be done in each module, TaPModule takes care of closing it
     */
    /**
     *  Description of the Method
     */
    public void editDialogClosed()
    {
        isEditDialogOn = false;

        if ( editDialog != null )
        {
            editDialog.dispose();
            editDialog = null;
        }
        else
        {
            editBDialog.dispose();
            editBDialog = null;
        }

        setName( this.getName() );
        //in case the module changed name (constant value module, for example)
        procedure.doLiveUpdate();
    }


    /**
     *  This method is called when a module edit window has to reload values
     *  Empty method provided for compatibility.
     */
    public void updateModuleWindow()
    {
        if ( editBDialog != null )
            if ( editBDialog instanceof EditWidgetDialog )
            {
                ( (EditWidgetDialog) editBDialog ).showValues( true );
            }
    }


    /*
     *  creates a preview frame from an object
     */
    /**
     *  Description of the Method
     *
     *@param  anObject  Description of the Parameter
     */
    public void setupPreviewFrame( ObjectInfo anObject )
    {
        if ( isPreviewOn )
        {
            Scene previewScene = fr.getScene();
            ObjectInfo obj = previewScene.getObject( 2 );
            ( (TapDesignerObjectCollection) obj.object ).copyObject( anObject.object, false );
            obj.object.sceneChanged( obj, previewScene );
            previewScene.objectModified( obj.object );
            fr.updateImage();
            fr.toFront();
        }
        else
        {
            // set up a new temporary scene
            Scene previewScene = new Scene();
            CoordinateSystem coords = new CoordinateSystem( new Vec3( 0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN ), new Vec3( 0.0, 0.0, -1.0 ), Vec3.vy() );
            ObjectInfo info;

            info = new ObjectInfo( new SceneCamera(), coords, "Camera 1" );
            info.addTrack( new PositionTrack( info ), 0 );
            info.addTrack( new RotationTrack( info ), 1 );

            previewScene.addObject( info, null );
            info = new ObjectInfo( new DirectionalLight( new RGBColor( 1.0f, 1.0f, 1.0f ), 0.8f ), coords.duplicate(), "Light 1" );
            info.addTrack( new PositionTrack( info ), 0 );
            info.addTrack( new RotationTrack( info ), 1 );
            previewScene.addObject( info, null );
            anObject = anObject.duplicate();
            previewScene.addObject( anObject, null );

            BoundingBox bounds = anObject.getBounds();
            Vec3 size = bounds.getSize();
            anObject.coords.setOrigin( new Vec3( 0, -size.y / 2, 0 ) );
            //and call for a new preview window
            fr = new TapPreviewWindow( previewScene, this );
            isPreviewOn = true;
            fr.setVisible( true );
        }
    }


    /**
     *  String representation of a module
     *
     *@return    String representation of the module, equivalent to getName()
     */
    @Override
    public String toString()
    {
        return getName();
    }


    /**
     *  Returns the module type information (name & icon) for a module class
     *
     *@return    The module type info
     */
    public abstract ModuleTypeInfo getModuleTypeInfo();


    /**
     *  Gets the number of edit frames used by this module
     *
     *@return    The number of edit frames to take into account
     */
    public int getNumEditWidgets()
    {
        return 0;
    }


    /**
     *  Gets the edit frame referenced by index
     *
     *@param  index       The reference to the edit frame
     *@param  cb          The Runnable called when validating modifications
     *@param  standalone  Whether the widget is in standalone frame or embedded
     *@return             The edit frame widget
     */
    public Widget getEditWidget( int index, Runnable cb, boolean standalone )
    {
        return null;
    }


    /**
     *  Gets the name of the edit frame referenced by index
     *
     *@param  index  The reference to the edit frame
     *@return        The edit frame value
     */
    public String getEditWidgetName( int index )
    {
        return null;
    }



    /**
     *  A class that holds an icon and a name for each module class
     *
     *@author     Francois Guillet
     *@created    8 mai 2004
     */
    public class ModuleTypeInfo
    {
        private ImageIcon icon;
        private String name;


        /**
         *  Gets the icon attribute of the ModuleTypeInfo object
         *
         *@return    The icon value
         */
        public ImageIcon getIcon()
        {
            return icon;
        }


        /**
         *  Constructor for the toString object
         *
         *@return    Description of the Return Value
         */
        public String toString()
        {
            return name;
        }


        /**
         *  Constructor for the ModuleTypeInfo object
         *
         *@param  name  Name for the module class
         *@param  icon  Icon for the module class
         */
        public ModuleTypeInfo( String name, ImageIcon icon )
        {
            this.name = name;
            this.icon = icon;
        }
    }

}

