/*
 *  This represents a distort module .
 */
/*
 *  Copyright (C) 2003 by Francois Guillet
 *  Changes copyright (C) 2019 by Maksim Khramov
 *
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.tapDesigner;

import artofillusion.*;
import artofillusion.math.*;
import artofillusion.object.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import artofillusion.tapDesigner.TapModule.*;


/**
 *  Description of the Class
 *
 *@author     pims
 *@created    19 avril 2004
 */
public class DistortModule extends TapModule
{
    private DistortModule module;
    private static TapModule.ModuleTypeInfo typeInfo;
    TapDistortParameters smParms;
    boolean deliverDuplicates;


    /**
     *  Constructor for the DistortModule object
     *
     *@param  procedure  Description of the Parameter
     *@param  position   Description of the Parameter
     */
    public DistortModule( TapProcedure procedure, Point position )
    {
        super( procedure, TapDesignerTranslate.text( "distort" ), position );
        if ( typeInfo == null )
            typeInfo = new ModuleTypeInfo( TapBTranslate.text( "distortName" ), new ImageIcon( getClass().getResource( "/artofillusion/tapDesigner/icons/distort_tree.png" ) ) );

        setNumInput( 1 );
        setNumOutput( 1 );
        smParms = new TapDistortParameters();
        deliverDuplicates = true;
        setup();
    }


    /**
     *  Description of the Method
     */
    private void setup()
    {
        inputNature[0] = OBJECT_PORT;
        outputNature[0] = OBJECT_PORT;
        inputTooltips = new String[1];
        inputTooltips[0] = TapDesignerTranslate.text( "objectToDistort" );
        outputTooltips = new String[1];
        outputTooltips[0] = TapDesignerTranslate.text( "distortedObject" );
        setBackgroundColor( Color.orange.darker() );
        module = this;
    }


    /**
     *  Constructor for the DistortModule object
     *
     *@param  in                          Description of the Parameter
     *@param  theScene                    Description of the Parameter
     *@exception  IOException             Description of the Exception
     *@exception  InvalidObjectException  Description of the Exception
     */
    public DistortModule( DataInputStream in, Scene theScene )
        throws IOException, InvalidObjectException
    {
        super( in, theScene );

        short version = in.readShort();

        if ( ( version < 0 ) || ( version > 0 ) )
            throw new InvalidObjectException( "" );

        deliverDuplicates = in.readBoolean();
        smParms = new TapDistortParameters( in );
        setup();
    }


    /**
     *  Description of the Method
     *
     *@param  out              Description of the Parameter
     *@param  theScene         Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    @Override
    public void writeToFile( DataOutputStream out, Scene theScene )
        throws IOException
    {
        super.writeToFile( out, theScene );
        out.writeShort( 0 );
        out.writeBoolean( deliverDuplicates );
        smParms.writeToFile( out );
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public TapModule duplicate()
    {
        DistortModule module = new DistortModule( this.procedure, this.location );
        module.copyModule( (TapModule) this );
        module.deliverDuplicates = this.deliverDuplicates;
        module.smParms = this.smParms.duplicate();

        return (TapModule) module;
    }


    /**
     *  Description of the Method
     *
     *@param  parentFrame  Description of the Parameter
     */
    @Override
    public void edit( BFrame parentFrame )
    {
        super.edit( parentFrame );

        if ( isEditDialogOn )
            editDialog.toFront();
        else
        {
            editDialog = new DistortModuleDialog( (JFrame) parentFrame.getComponent() );
            isEditDialogOn = true;
        }
    }


    /**
     *  Gets the object attribute of the DistortModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The object value
     */
    @Override
    public TapDesignerObjectCollection getObject( int outputPort, long seed )
    {
        TapDesignerObjectCollection col = null;
        TapRandomGenerator gen = new TapRandomGenerator( seed );
        smParms.seed = gen.getSeed();

        BackModuleLink.BackLink bl;
        int j;

        /*
         *  if ( outputPort == -1 )
         *  {
         *  /preview wanted !
         *  stopHere = true;
         *  col = procedure.getTempObject();
         *  stopHere = false;
         *  return col;
         *  }
         *  else
         */
        if ( outputPort == -2 )
        {
            //preview up to

            BackModuleLink backLinks = procedure.getBackLink();
            bl = backLinks.findModule( this, 0 );
            col = null;

            if ( bl != null )
                col = bl.fromModule.getObject( bl.outputPort, gen.getSeed() );

            if ( col == null )
                return null;

            ObjectInfo mainObject = col.elementAt( 0 ).objectInfo;

            if ( mainObject.object instanceof TapObject )
                ( (TapObject) mainObject.object ).setShape( smParms );
            else if ( mainObject.object instanceof SplineMesh )
                TapUtils.distortSplineMesh( (SplineMesh) mainObject.object, smParms );

            TapDesignerObjectCollection tmpCollection = new TapDesignerObjectCollection( procedure );

            if ( inputPortLink[0] != null )
            {
                //modules linked to output will presumably decorate object

                for ( j = 0; j < inputPortLink[0].length; ++j )
                {
                    TapModule mod = (TapModule) modules.elementAt( linkToIndex[0][j] );
                    TapDesignerObjectCollection modCol = mod.getObject( col, inputPortLink[0][j], gen.getSeed() );

                    if ( modCol != null )
                        col.mergeCollection( modCol, 0 );
                }
            }

            return col;
        }
        else if ( ( outputPort == 0 ) || ( outputPort == -1 ) )
        {
            BackModuleLink backLinks = procedure.getBackLink();
            bl = backLinks.findModule( this, 0 );
            col = null;

            if ( bl != null )
                col = bl.fromModule.getObject( bl.outputPort, gen.getSeed() );

            if ( col == null )
                return null;

            ObjectInfo mainObject = col.elementAt( 0 ).objectInfo;

            if ( mainObject.object instanceof TapObject )
                ( (TapObject) mainObject.object ).setShape( smParms );
            else if ( mainObject.object instanceof SplineMesh )
                TapUtils.distortSplineMesh( (SplineMesh) mainObject.object, smParms );

            return col;
        }
        else
            return null;
    }


    /**
     *  Gets the object attribute of the DistortModule object
     *
     *@param  collection  Description of the Parameter
     *@param  inputPort   Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The object value
     */
    @Override
    public TapDesignerObjectCollection getObject( TapDesignerObjectCollection collection, int inputPort, long seed )
    {
        ObjectInfo anInfo;
        ObjectInfo mainObject;
        TapRandomGenerator gen = new TapRandomGenerator( seed );
        smParms.seed = gen.getSeed();

        BackModuleLink.BackLink bl;
        TapDesignerObjectCollection newCollection = null;
        int j;

        if ( inputPort == 0 )
        {
            mainObject = collection.elementAt( 0 ).objectInfo;

            if ( mainObject.object instanceof TapObject )
                ( (TapObject) mainObject.object ).setShape( smParms );
            else if ( mainObject.object instanceof SplineMesh )
                TapUtils.distortSplineMesh( (SplineMesh) mainObject.object, smParms );

            if ( ( inputPortLink[0] != null ) && ( !stopHere ) )
            {
                //modules linked to output will decorate object

                for ( j = 0; j < inputPortLink[0].length; ++j )
                {
                    TapModule mod = (TapModule) modules.elementAt( linkToIndex[0][j] );
                    TapDesignerObjectCollection modCol = mod.getObject( collection, inputPortLink[0][j], gen.getSeed() );

                    if ( modCol != null )
                    {
                        if ( newCollection != null )
                            newCollection.mergeCollection( modCol, 0 );
                        else
                            newCollection = modCol;
                    }
                }
            }

            return newCollection;
        }
        else
            return null;
    }


    /**
     *  Gets the value attribute of the DistortModule object
     *
     *@param  outputPort  Description of the Parameter
     *@param  var         Description of the Parameter
     *@param  seed        Description of the Parameter
     *@return             The value value
     */
    @Override
    public double getValue( int outputPort, double[] var, long seed )
    {
        return (double) 0.0;
    }


    /**
     *  Description of the Method
     *
     *@param  modifiers  Description of the Parameter
     */
    @Override
    public void showPreviewFrame( int modifiers )
    {
        super.showPreviewFrame( modifiers );

        TapDesignerObjectCollection collection = null;
        procedure.initProcedure();

        if ( ( modifiers & ActionEvent.CTRL_MASK ) != 0 )
            collection = getObject( -2, procedure.getSeed() );
        else
            collection = getObject( -1, procedure.getSeed() );

        if ( collection != null )
            if ( collection.size() > 0 )
            {
                ObjectInfo newObjectInfo = new ObjectInfo( collection, new CoordinateSystem(), getName() );
                newObjectInfo.object.setTexture( procedure.getScene().getDefaultTexture(), procedure.getScene().getDefaultTexture().getDefaultMapping(newObjectInfo.object) );
                setupPreviewFrame( newObjectInfo );
            }
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public boolean acceptsMainEntry()
    {
        return false;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    @Override
    public boolean acceptsPreview()
    {
        return true;
    }


    /**
     *  Gets the moduleTypeInfo attribute of the DistortModule object
     *
     *@return    The moduleTypeInfo value
     */
    @Override
    public ModuleTypeInfo getModuleTypeInfo()
    {
        return typeInfo;
    }


    /**
     *  DistortModule editor window
     *
     *@author     Francois Guillet
     *@created    19 avril 2004
     */
    private class DistortModuleDialog
             extends JFrame
             implements ActionListener,
            DocumentListener,
            ChangeListener
    {
        private JButton okButton;
        private JButton applyButton;
        private JButton cancelButton;
        private JRadioButton curveDistGRB;
        private JRadioButton perpCurveDistGRB;
        private JRadioButton curveDistURB;
        private JRadioButton perpCurveDistURB;
        private boolean modified;
        private boolean setupFlag;
        private JTextField randomTiltTF;
        private JTextField randomTiltDivTF;
        private JTextField curveAngleTF;
        private JTextField curveAngleDistTF;
        private JTextField curveAngleBackTF;
        private JTextField sectionJitterTF;
        private JTextField perpCurveAngleTF;
        private JTextField perpCurveAngleDistTF;
        private JTextField perpCurveAngleBackTF;
        private JTextField curveRateTF;
        private JTextField perpCurveRateTF;
        private JTextField twistTurnsTF;
        private JTextField twistTurnsDistTF;
        private JRadioButton twistDistURB;
        private JRadioButton twistDistGRB;
        private ButtonGroup curveDistBG;
        private ButtonGroup perpCurveDistBG;
        private ButtonGroup twistDistBG;
        private NumberFormat format;
        private TapDistortParameters backParms;
        private JTextField leafCurveAngleTF;
        private JTextField leafCurveAngleDistTF;
        private JTextField leafCurveAngleBackTF;
        private JTextField leafCurveRateTF;
        private JTextField leafDepartureAngleTF;
        private JSlider leafRRatioSL;
        private ButtonGroup leafCurveDistBG;
        private JRadioButton leafCurveDistGRB;
        private JRadioButton leafCurveDistURB;
        private Container contentPane;


        /**
         *  Constructor for the DistortModuleDialog object
         *
         *@param  parentFrame  Description of the Parameter
         */
        public DistortModuleDialog( JFrame parentFrame )
        {
            setupFlag = true;
            format = NumberFormat.getInstance();
            backParms = smParms.duplicate();

            Container contentPane = this.getContentPane();
            curveDistBG = new ButtonGroup();
            perpCurveDistBG = new ButtonGroup();
            twistDistBG = new ButtonGroup();
            leafCurveDistBG = new ButtonGroup();

            curveDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            curveDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            perpCurveDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            perpCurveDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            twistDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            twistDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );
            leafCurveDistURB = TapDesignerTranslate.jRadioButton( "uniform", this );
            leafCurveDistGRB = TapDesignerTranslate.jRadioButton( "gaussian", this );

            curveDistBG.add( curveDistURB );
            curveDistBG.add( curveDistGRB );
            setRadioButton( smParms.curveAngleDistType, curveDistURB, curveDistGRB );
            perpCurveDistBG.add( perpCurveDistURB );
            perpCurveDistBG.add( perpCurveDistGRB );
            setRadioButton( smParms.perpCurveAngleDistType, perpCurveDistURB, perpCurveDistGRB );
            twistDistBG.add( twistDistURB );
            twistDistBG.add( twistDistGRB );
            setRadioButton( smParms.twistDistType, twistDistURB, twistDistGRB );
            leafCurveDistBG.add( leafCurveDistURB );
            leafCurveDistBG.add( leafCurveDistGRB );
            setRadioButton( smParms.leafCurveAngleDistType, leafCurveDistURB, leafCurveDistGRB );

            contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS ) );

            JPanel pp = new JPanel();
            pp.setLayout( new BoxLayout( pp, BoxLayout.Y_AXIS ) );

            JPanel p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "curvatureAngle" ) );
            p.add( curveAngleTF = new JTextField( format.format( smParms.curveAngle ) ) );
            curveAngleTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "pm" ) );
            p.add( curveAngleDistTF = new JTextField( format.format( smParms.curveAngleDist ) ) );
            curveAngleDistTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "backAngle" ) );
            p.add( curveAngleBackTF = new JTextField( format.format( smParms.curveAngleBack ) ) );
            curveAngleBackTF.setColumns( 4 );

            JPanel pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( curveDistURB );
            pRB.add( curveDistGRB );
            p.add( pRB );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "curvatureRate" ) );
            p.add( curveRateTF = new JTextField( format.format( smParms.curveRate ) ) );
            curveRateTF.setColumns( 4 );
            pp.add( p );

            TitledBorder border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapDesignerTranslate.text( "yCurveParameters" ) );
            pp.setBorder( border );
            contentPane.add( pp );

            pp = new JPanel();
            pp.setLayout( new BoxLayout( pp, BoxLayout.Y_AXIS ) );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "curvatureAngle" ) );
            p.add( perpCurveAngleTF = new JTextField( format.format( smParms.perpCurveAngle ) ) );
            perpCurveAngleTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "pm" ) );
            p.add( perpCurveAngleDistTF = new JTextField( format.format( smParms.perpCurveAngleDist ) ) );
            perpCurveAngleDistTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "backAngle" ) );
            p.add( perpCurveAngleBackTF = new JTextField( format.format( smParms.perpCurveAngleBack ) ) );
            perpCurveAngleBackTF.setColumns( 4 );
            pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( perpCurveDistURB );
            pRB.add( perpCurveDistGRB );
            p.add( pRB );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "curvatureRate" ) );
            p.add( perpCurveRateTF = new JTextField( format.format( smParms.perpCurveRate ) ) );
            perpCurveRateTF.setColumns( 4 );
            pp.add( p );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapDesignerTranslate.text( "rCurveParameters" ) );
            pp.setBorder( border );
            contentPane.add( pp );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "randomTilt" ) );
            p.add( randomTiltTF = new JTextField( format.format( smParms.randomTilt ) ) );
            randomTiltTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "randomTiltDiv" ) );
            p.add( randomTiltDivTF = new JTextField( format.format( smParms.randomTiltDiv ) ) );
            randomTiltDivTF.setColumns( 3 );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapDesignerTranslate.text( "randomTilt" ) );
            p.setBorder( border );
            contentPane.add( p );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "sectionJitter" ) );
            p.add( sectionJitterTF = new JTextField( format.format( smParms.sectionJitter ) ) );
            sectionJitterTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "twistTurns" ) );
            p.add( twistTurnsTF = new JTextField( format.format( smParms.twistTurns ) ) );
            twistTurnsTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "pm" ) );
            p.add( twistTurnsDistTF = new JTextField( format.format( smParms.twistTurnsDist ) ) );
            twistTurnsDistTF.setColumns( 4 );
            pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( twistDistURB );
            pRB.add( twistDistGRB );
            p.add( pRB );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapDesignerTranslate.text( "sectionParameters" ) );
            p.setBorder( border );
            contentPane.add( p );

            pp = new JPanel();
            pp.setLayout( new BoxLayout( pp, BoxLayout.Y_AXIS ) );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "leafCurvatureAngle" ) );
            p.add( leafCurveAngleTF = new JTextField( format.format( smParms.leafCurveAngle ) ) );
            leafCurveAngleTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "pm" ) );
            p.add( leafCurveAngleDistTF = new JTextField( format.format( smParms.leafCurveAngleDist ) ) );
            leafCurveAngleDistTF.setColumns( 4 );
            pRB = new JPanel();
            pRB.setLayout( new BoxLayout( pRB, BoxLayout.Y_AXIS ) );
            pRB.add( leafCurveDistURB );
            pRB.add( leafCurveDistGRB );
            p.add( pRB );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "backAngle" ) );
            p.add( leafCurveAngleBackTF = new JTextField( format.format( smParms.leafCurveAngleBack ) ) );
            leafCurveAngleBackTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "curvatureRate" ) );
            p.add( leafCurveRateTF = new JTextField( format.format( smParms.leafCurveRate ) ) );
            leafCurveRateTF.setColumns( 4 );
            pp.add( p );
            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( TapDesignerTranslate.jlabel( "leafDepartureAngle" ) );
            p.add( leafDepartureAngleTF = new JTextField( format.format( smParms.leafDepartureAngle ) ) );
            leafDepartureAngleTF.setColumns( 4 );
            p.add( TapDesignerTranslate.jlabel( "leafYCurvatureRatio" ) );
            p.add( leafRRatioSL = new JSlider( JSlider.HORIZONTAL, 0, 100, (int) ( smParms.leafRRatio * 100 ) ) );
            leafRRatioSL.setMajorTickSpacing( 100 );
            leafRRatioSL.setMinorTickSpacing( 10 );
            leafRRatioSL.setPaintTicks( true );

            Hashtable labelTable = new Hashtable();
            JLabel tmpLabel;
            labelTable.put( new Integer( 0 ), tmpLabel = TapDesignerTranslate.jlabel( "constant" ) );
            labelTable.put( new Integer( 100 ), tmpLabel = TapDesignerTranslate.jlabel( "normalized" ) );
            leafRRatioSL.setLabelTable( labelTable );
            leafRRatioSL.setPaintLabels( true );
            pp.add( p );
            border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.RAISED ), TapDesignerTranslate.text( "leafCurveParameters" ) );
            pp.setBorder( border );
            contentPane.add( pp );

            p = new JPanel();
            p.setLayout( new FlowLayout( FlowLayout.CENTER, 2, 2 ) );
            p.add( okButton = TapDesignerTranslate.jButton( "ok", this ) );
            p.add( applyButton = TapDesignerTranslate.jButton( "apply", this ) );
            p.add( cancelButton = TapDesignerTranslate.jButton( "cancel", this ) );
            contentPane.add( p );
            this.setTitle( TapDesignerTranslate.text( "distortModuleTitle", module.getName() ) );

            addWindowListener(
                new java.awt.event.WindowAdapter()
                {
                    @Override
                    public void windowClosing( java.awt.event.WindowEvent evt )
                    {
                        exitForm( evt );
                    }
                } );
            curveAngleTF.getDocument().addDocumentListener( this );
            curveAngleDistTF.getDocument().addDocumentListener( this );
            curveAngleBackTF.getDocument().addDocumentListener( this );
            sectionJitterTF.getDocument().addDocumentListener( this );
            perpCurveAngleTF.getDocument().addDocumentListener( this );
            perpCurveAngleDistTF.getDocument().addDocumentListener( this );
            perpCurveAngleBackTF.getDocument().addDocumentListener( this );
            perpCurveRateTF.getDocument().addDocumentListener( this );
            randomTiltTF.getDocument().addDocumentListener( this );
            randomTiltDivTF.getDocument().addDocumentListener( this );
            twistTurnsTF.getDocument().addDocumentListener( this );
            twistTurnsDistTF.getDocument().addDocumentListener( this );
            leafCurveAngleTF.getDocument().addDocumentListener( this );
            leafCurveAngleBackTF.getDocument().addDocumentListener( this );
            leafCurveAngleDistTF.getDocument().addDocumentListener( this );
            leafCurveRateTF.getDocument().addDocumentListener( this );
            leafDepartureAngleTF.getDocument().addDocumentListener( this );

            leafRRatioSL.addChangeListener( this );

            pack();
            setLocationRelativeTo( parentFrame );
            setResizable( false );
            setVisible( true );
            setupFlag = false;
            modified = false;
        }


        /**
         *  Sets the radioButton attribute of the DistortModuleDialog object
         *
         *@param  type  The new radioButton value
         *@param  rURB  The new radioButton value
         *@param  rGRB  The new radioButton value
         */
        private void setRadioButton( short type, JRadioButton rURB, JRadioButton rGRB )
        {
            switch ( type )
            {
                case TapRandomGenerator.UNIFORM:
                    rURB.setSelected( true );

                    break;
                case TapRandomGenerator.GAUSSIAN:
                    rGRB.setSelected( true );

                    break;
            }
        }


        /**
         *  Gets the radioType attribute of the DistortModuleDialog object
         *
         *@param  rURB  Description of the Parameter
         *@param  rGRB  Description of the Parameter
         *@return       The radioType value
         */
        private short getRadioType( JRadioButton rURB, JRadioButton rGRB )
        {
            if ( rURB.isSelected() )
                return TapRandomGenerator.UNIFORM;
            else
                return TapRandomGenerator.GAUSSIAN;
        }


        /**
         *  Description of the Method
         *
         *@param  evt  Description of the Parameter
         */
        private void exitForm( java.awt.event.WindowEvent evt )
        {
            doCancel();
        }


        /**
         *  Description of the Method
         *
         *@param  evt  Description of the Parameter
         */
        @Override
        public void actionPerformed( java.awt.event.ActionEvent evt )
        {
            String command = evt.getActionCommand();

            if ( command.equals( cancelButton.getActionCommand() ) )
                doCancel();
            else if ( command.equals( applyButton.getActionCommand() ) )
            {
                getValues();
                doApply();
            }
            else if ( command.equals( okButton.getActionCommand() ) )
            {
                if ( modified )
                {
                    //put back the module in initial state for undo record
                    getBackValues();
                    procedure.addUndoRecord();
                }

                getValues();
                doApply();
                editDialogClosed();
            }
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
        public void stateChanged( ChangeEvent e )
        {
            if ( setupFlag )
                return;
            modified = true;
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void doLiveCheck( DocumentEvent e )
        {
            modified = true;

            Document sourceDoc = e.getDocument();
            JTextField source = null;

            if ( sourceDoc == curveAngleTF.getDocument() )
                source = curveAngleTF;
            else if ( sourceDoc == curveAngleDistTF.getDocument() )
                source = curveAngleDistTF;
            else if ( sourceDoc == curveAngleBackTF.getDocument() )
                source = curveAngleBackTF;
            else if ( sourceDoc == sectionJitterTF.getDocument() )
                source = sectionJitterTF;
            else if ( sourceDoc == perpCurveAngleTF.getDocument() )
                source = perpCurveAngleTF;
            else if ( sourceDoc == perpCurveAngleDistTF.getDocument() )
                source = perpCurveAngleDistTF;
            else if ( sourceDoc == perpCurveAngleBackTF.getDocument() )
                source = perpCurveAngleBackTF;
            else if ( sourceDoc == curveRateTF.getDocument() )
                source = curveRateTF;
            else if ( sourceDoc == perpCurveRateTF.getDocument() )
                source = perpCurveRateTF;
            else if ( sourceDoc == randomTiltTF.getDocument() )
                source = randomTiltTF;
            else if ( sourceDoc == randomTiltDivTF.getDocument() )
                source = randomTiltDivTF;
            else if ( sourceDoc == twistTurnsTF.getDocument() )
                source = randomTiltTF;
            else if ( sourceDoc == twistTurnsDistTF.getDocument() )
                source = randomTiltDivTF;
            else if ( sourceDoc == leafCurveAngleTF.getDocument() )
                source = leafCurveAngleTF;
            else if ( sourceDoc == leafCurveAngleBackTF.getDocument() )
                source = leafCurveAngleBackTF;
            else if ( sourceDoc == leafCurveAngleDistTF.getDocument() )
                source = leafCurveAngleDistTF;
            else if ( sourceDoc == leafCurveRateTF.getDocument() )
                source = leafCurveRateTF;
            else if ( sourceDoc == leafDepartureAngleTF.getDocument() )
                source = leafDepartureAngleTF;

            if ( source == randomTiltDivTF )
            {
                try
                {
                    int dum = ( Integer.valueOf( source.getText().trim() ) ).intValue();
                    source.setForeground( Color.black );
                }
                catch ( NumberFormatException ex )
                {
                    source.setForeground( Color.red );
                }
            }
            else if ( source != null )
            {
                try
                {
                    double dum = Double.parseDouble( source.getText().trim().replace( ',', '.' ) );
                    source.setForeground( Color.black );
                }
                catch ( NumberFormatException ex )
                {
                    source.setForeground( Color.red );
                }
            }

        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
        public void changedUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
        public void insertUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        @Override
        public void removeUpdate( DocumentEvent e )
        {
            doLiveCheck( e );
        }


        /**
         *  Gets the values attribute of the DistortModuleDialog object
         */
        private void getValues()
        {
            try
            {
                smParms.curveAngle = Double.parseDouble( curveAngleTF.getText().trim().replace( ',', '.' ) );
                smParms.curveAngleDist = Double.parseDouble( curveAngleDistTF.getText().trim().replace( ',', '.' ) );
                smParms.curveAngleBack = Double.parseDouble( curveAngleBackTF.getText().trim().replace( ',', '.' ) );
                smParms.perpCurveAngle = Double.parseDouble( perpCurveAngleTF.getText().trim().replace( ',', '.' ) );
                smParms.perpCurveAngleDist = Double.parseDouble( perpCurveAngleDistTF.getText().trim().replace( ',', '.' ) );
                smParms.perpCurveAngleBack = Double.parseDouble( perpCurveAngleBackTF.getText().trim().replace( ',', '.' ) );
                smParms.curveRate = Double.parseDouble( curveRateTF.getText().trim().replace( ',', '.' ) );
                smParms.perpCurveRate = Double.parseDouble( perpCurveRateTF.getText().trim().replace( ',', '.' ) );
                smParms.sectionJitter = Double.parseDouble( sectionJitterTF.getText().trim().replace( ',', '.' ) );
                smParms.curveAngleDistType = getRadioType( curveDistURB, curveDistGRB );
                smParms.perpCurveAngleDistType = getRadioType( perpCurveDistURB, perpCurveDistGRB );
                smParms.randomTilt = Double.parseDouble( randomTiltTF.getText().trim().replace( ',', '.' ) );
                smParms.randomTiltDiv = ( Integer.valueOf( randomTiltDivTF.getText().trim() ) )
                        .intValue();
                smParms.twistTurns = Double.parseDouble( twistTurnsTF.getText().trim().replace( ',', '.' ) );
                smParms.twistTurnsDist = Double.parseDouble( twistTurnsDistTF.getText().trim().replace( ',', '.' ) );
                smParms.twistDistType = getRadioType( twistDistURB, twistDistGRB );
                smParms.leafCurveAngle = Double.parseDouble( leafCurveAngleTF.getText().trim().replace( ',', '.' ) );
                smParms.leafCurveAngleDist = Double.parseDouble( leafCurveAngleDistTF.getText().trim().replace( ',', '.' ) );
                smParms.leafCurveAngleBack = Double.parseDouble( leafCurveAngleBackTF.getText().trim().replace( ',', '.' ) );
                smParms.leafCurveRate = Double.parseDouble( leafCurveRateTF.getText().trim().replace( ',', '.' ) );
                smParms.leafDepartureAngle = Double.parseDouble( leafDepartureAngleTF.getText().trim().replace( ',', '.' ) );
                smParms.leafRRatio = (double) leafRRatioSL.getValue() / 100.0;
            }
            catch ( NumberFormatException e )
            {
                System.out.println( "Exception" );
                getBackValues();
            }
        }


        /**
         *  Gets the backValues attribute of the DistortModuleDialog object
         */
        private void getBackValues()
        {
            smParms = backParms.duplicate();
        }


        /**
         *  Description of the Method
         */
        private void doCancel()
        {
            if ( modified )
            {
                int r = JOptionPane.showConfirmDialog( this, TapDesignerTranslate.text( "parametersModified" ), TapDesignerTranslate.text( "warning" ), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION );

                if ( r == JOptionPane.YES_OPTION )
                    modified = false;

            }

            if ( !modified )
            {
                getBackValues();
                editDialogClosed();
            }
        }
    }
}

