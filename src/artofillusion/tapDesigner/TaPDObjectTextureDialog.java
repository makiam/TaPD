/* Copyright (C) 1999-2004 by Peter Eastman
 *  Changes copyright (C) 2019 by Maksim Khramov
 *
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tapDesigner;

import artofillusion.object.*;
import artofillusion.texture.*;
import artofillusion.ui.*;
import artofillusion.*;
import buoy.event.*;
import buoy.widget.*;

import java.awt.*;

/** This class implements the dialog box which is used to choose textures for objects. 
    It presents a list of all available textures from which the user can select one.
    If only one object is being editing, it also allows the user to edit the texture mapping
    for that object. */

public class TaPDObjectTextureDialog extends BDialog implements ListChangeListener
{
  private BFrame fr;
  private Scene sc;
  private ObjectInfo obj[], editObj;
  private BList texList, layerList;
  private BButton mapButton, addLayerButton, deleteLayerButton, moveUpButton, moveDownButton;
  private BButton newTextureButton, editTexturesButton;
  private BorderContainer content;
  private FormContainer listPanel, layerPanel, paramsPanel;
  private MaterialPreviewer preview;
  private BComboBox typeChoice, blendChoice, paramTypeChoice[];
  private Widget paramValueWidget[];
  private int fieldParamIndex[];
  private BScrollPane paramsScroller;
  private BLabel paramsLabel;
  private Runnable callback;
  private Texture oldTexture;
  private TextureMapping oldMapping;
  private LayeredTexture layeredTex;
  private LayeredMapping layeredMap;
  private ActionProcessor renderProcessor;
  
  private static final int CONSTANT_PARAM = 0;
  private static final int VERTEX_PARAM = 1;
  private static final int FACE_PARAM = 2;
  private static final int FACE_VERTEX_PARAM = 3;
  
  private static final String PARAM_TYPE_NAME[] = new String [] {
      Translate.text("Object"),
      Translate.text("Vertex"),
      Translate.text("Face"),
      Translate.text("Face-Vertex")
    };


  public TaPDObjectTextureDialog(BFrame parent, Scene theScene, ObjectInfo objects[])
  {
    super(parent, Translate.text("objectTextureTitle"), false);
    
    fr = parent;
    sc = theScene;
    obj = objects;
    renderProcessor = new ActionProcessor();
    editObj = obj[0].duplicate();
    editObj.object = editObj.object.duplicate();
    oldTexture = editObj.object.getTexture();
    oldMapping = editObj.object.getTextureMapping();
    if (oldTexture instanceof LayeredTexture)
    {
      layeredMap = (LayeredMapping) oldMapping;
      layeredTex = (LayeredTexture) oldTexture;;
    }
    else
    {
      layeredTex = new LayeredTexture(editObj.object);
      layeredMap = (LayeredMapping) layeredTex.getDefaultMapping(editObj.object);
    }

    // Add the title and combo box at the top.
    
    content = new BorderContainer();
    setContent(BOutline.createEmptyBorder(content, UIUtilities.getStandardDialogInsets()));
    FormContainer northPanel = new FormContainer(1, 2);
    String title;
    if (obj.length == 1)
      title = Translate.text("chooseTextureForSingle", obj[0].name);
    else
      title = Translate.text("chooseTextureForMultiple");
    northPanel.add(new BLabel(title), 0, 0);
    RowContainer typeRow = new RowContainer();
    typeRow.add(Translate.label("Type"));
    typeRow.add(typeChoice = new BComboBox(new String [] {
      Translate.text("simpleTexture"),
      Translate.text("layeredTexture")
    }));
    typeChoice.setSelectedIndex((oldTexture instanceof LayeredTexture) ? 1 : 0);
    typeChoice.addEventLink(ValueChangedEvent.class, this, "typeChanged");
    if (obj.length == 1)
      northPanel.add(typeRow, 0, 1);
    content.add(northPanel, BorderContainer.NORTH);
    
    // Create the list of textures and the buttons under it.

    texList = new BList();
    texList.setMultipleSelectionEnabled(false);
    buildList();
    texList.addEventLink(SelectionChangedEvent.class, this, "selectionChanged");
    texList.addEventLink(MouseClickedEvent.class, this, "textureClicked");
    listPanel = new FormContainer(new double [] {1.0}, new double [] {1.0, 0.0});
    listPanel.add(UIUtilities.createScrollingList(texList), 0, 0, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
    RowContainer texButtonRow = new RowContainer();
    texButtonRow.add(newTextureButton = Translate.button("newTexture", this, "doNewTexture"));
    texButtonRow.add(editTexturesButton = Translate.button("textures", this, "doEditTextures"));
    listPanel.add(texButtonRow, 0, 1);
    
    // Create the section of the window for layered textures.
    
    layerPanel = new FormContainer(new double [] {1.0, 1.0, 1.0}, new double [] {0.0, 0.0, 0.0, 0.0, 1.0});
    layerPanel.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets(2, 2, 2, 2), null));
    layerPanel.add(addLayerButton = Translate.button("add", " >>", this, "doAddLayer"), 0, 0);
    layerPanel.add(deleteLayerButton = Translate.button("delete", this, "doDeleteLayer"), 0, 1);
    layerPanel.add(moveUpButton = Translate.button("moveUp", this, "doMoveLayerUp"), 0, 2);
    layerPanel.add(moveDownButton = Translate.button("moveDown", this, "doMoveLayerDown"), 0, 3);
    layerList = new BList() {
      @Override
      public Dimension getPreferredSize()
      {
        return texList.getPreferredSize();
      }
    };
    layerList.setMultipleSelectionEnabled(false);
    layerList.addEventLink(SelectionChangedEvent.class, this, "selectionChanged");
    for (int i = 0; i < layeredMap.getLayers().length; i++)
      layerList.add((layeredMap.getLayers())[i].getName());
    layerPanel.add(UIUtilities.createScrollingList(layerList), 1, 0, 1, 4, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
    layerPanel.add(Translate.label("blendingMode"), 2, 0);
    blendChoice = new BComboBox(new String [] {
      Translate.text("blend"),
      Translate.text("overlay"),
      Translate.text("overlayBumpsAdd"),
    });
    blendChoice.addEventLink(ValueChangedEvent.class, this, "blendTypeChanged");
    layerPanel.add(blendChoice, 2, 1);
   
    // Create the material previewer.
    
    if (oldTexture instanceof LayeredTexture)
    {
      preview = new MaterialPreviewer(layeredTex, editObj.object.getMaterial(), 160, 160);
      preview.setTexture(layeredTex, layeredMap);
    }
    else
    {
      preview = new MaterialPreviewer(oldTexture, editObj.object.getMaterial(), 160, 160);
      preview.setTexture(oldTexture, oldMapping);
    }
    preview.setMaterial(editObj.object.getMaterial(), editObj.object.getMaterialMapping());
    double paramAvgVal[] = editObj.object.getAverageParameterValues();
    ParameterValue paramValue[] = new ParameterValue [paramAvgVal.length];
    for (int i = 0; i < paramValue.length; i++)
      paramValue[i] = new ConstantParameterValue(paramAvgVal[i]);
    preview.getObject().object.setParameterValues(paramValue);

    // Add the buttons at the bottom.

    RowContainer buttons = new RowContainer();
    content.add(buttons, BorderContainer.SOUTH, new LayoutInfo());
    buttons.add(mapButton = Translate.button("editMapping", this, "doEditMapping"));
    buttons.add(Translate.button("ok", this, "doOk"));
    buttons.add(Translate.button("cancel", this, "doCancel"));

    // Create the parameters panel.
    
    paramsPanel = new FormContainer(1, 2);
    paramsPanel.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
    paramsPanel.add(Translate.label("textureParameters"), 0, 0);
    paramsPanel.add(BOutline.createBevelBorder(paramsScroller = new BScrollPane(), false), 0, 1);
    paramsScroller.setPreferredViewSize(new Dimension(300, 80));
    buildParamList();

    // Show the dialog.

    if (oldTexture instanceof LayeredTexture)
      layoutLayered();
    else
      layoutSimple();
    pack();
    setResizable(false);
    addEventLink(WindowClosingEvent.class, this, "dispose");
    UIUtilities.centerWindow(this);
    updateComponents();
    sc.addTextureListener(this);
    setVisible(true);
  }

  @Override
  public void dispose()
  {
    sc.removeTextureListener(this);
    renderProcessor.stopProcessing();
    super.dispose();
  }
  
  /** Set a callback to be executed when the window is closed. */
  
  public void setCallback(Runnable cb)
  {
    callback = cb;
  }
  
  /** Layout the content panel for a simple texture. */
  
  private void layoutSimple()
  {
    content.remove(BorderContainer.CENTER);
    FormContainer center = new FormContainer(2, 2);
    center.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
    center.add(listPanel, 0, 0);
    center.add(preview, 1, 0, new LayoutInfo());
    center.add(paramsPanel, 0, 1, 2, 1);
    content.add(center, BorderContainer.CENTER);
  }
  
  /** Layout the content panel for a layered texture. */
  
  private void layoutLayered()
  {
    content.remove(BorderContainer.CENTER);
    FormContainer center = new FormContainer(new double [] {1.0, 1.0, 0.0}, new double [] {1.0, 1.0});
    center.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
    center.add(listPanel, 0, 0);
    center.add(layerPanel, 1, 0, 2, 1);
    center.add(paramsPanel, 0, 1, 2, 1);
    center.add(preview, 2, 1, new LayoutInfo());
    content.add(center, BorderContainer.CENTER);
  }
  
  /** Build the list of all available textures. */
  
  private void buildList()
  {
    texList.removeAll();
    for (int i = 0; i < sc.getNumTextures(); i++)
    {
      texList.add((sc.getTexture(i)).getName());
      if (editObj.object.getTexture() == sc.getTexture(i))
        texList.setSelected(i, true);
    }
  }
  
  /** Build the list of texture parameters. */
  
  private void buildParamList()
  {
    // Find a list of all parameters, both for the entire texture and for the selected layer.
	  TextureParameter params[];
	    if (editObj.object.getTexture() instanceof LayeredTexture)
	    {
	      int index = layerList.getSelectedIndex();
	      if (index == -1)
	        params = new TextureParameter [0];
	      else
	        params = ((LayeredMapping) editObj.object.getTextureMapping()).getLayerParameters(index);
	    }
	    else
	      params = editObj.object.getParameters();
	    paramTypeChoice = new BComboBox [params.length];
	    paramValueWidget = new ValueSelector[params.length];
	    fieldParamIndex = new int [params.length];
	    FormContainer paramsContainer = new FormContainer(3, params.length);
	    paramsContainer.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(0, 0, 0, 5), null));
	    final TextureParameter texParam[] = editObj.object.getParameters();
	    final ParameterValue paramValue[] = editObj.object.getParameterValues();
	    for (int i = 0; i < params.length; i++)
	    {
	      if (params[i].type != TextureParameter.NORMAL_PARAMETER)
	        continue;
	      boolean perObject = true;
	      double val = params[i].defaultVal;
	      int j;
	      for (j = 0; j < texParam.length; j++)
	        if (params[i].equals(texParam[j]))
	        {
	          val = paramValue[j].getAverageValue();
	          perObject = (paramValue[j] instanceof ConstantParameterValue);
	          break;
	        }
	      final int whichParam = j, whichField = i;
	      fieldParamIndex[i] = j;
	      paramsContainer.add(new BLabel(params[i].name), 0, i);
	      paramsContainer.add(paramTypeChoice[i] = new BComboBox(), 1, i);
	      paramTypeChoice[i].add(PARAM_TYPE_NAME[0]);
	      if (editObj.object instanceof Mesh)
	        paramTypeChoice[i].add(PARAM_TYPE_NAME[1]);
	      if (editObj.object instanceof FacetedMesh)
	        paramTypeChoice[i].add(PARAM_TYPE_NAME[2]);
	      if (editObj.object instanceof FacetedMesh)
	        paramTypeChoice[i].add(PARAM_TYPE_NAME[3]);
	      if (whichParam < paramValue.length)
	        paramTypeChoice[i].setSelectedValue(PARAM_TYPE_NAME[parameterTypeCode(paramValue[whichParam].getClass())]);
	      paramTypeChoice[i].addEventLink(ValueChangedEvent.class, this, "paramTypeChanged");
	      paramsContainer.add(paramValueWidget[i] = new ValueSelector(val, params[i].minVal, params[i].maxVal, 0.005), 2, i);
	      paramValueWidget[i].setEnabled(perObject);
	      paramValueWidget[i].addEventLink(ValueChangedEvent.class, new Object() {
	        void processEvent(ValueChangedEvent ev)
	        {
	          if (whichParam >= paramValue.length)
	            return;
	          double value = ((ValueSelector)paramValueWidget[whichField]).getValue();
	          ParameterValue val = new ConstantParameterValue(value);
	          editObj.object.setParameterValue(texParam[whichParam], val);
	          preview.getObject().object.setParameterValue(texParam[whichParam], val);
	          renderPreview();
	        }
	      });
	    }
	    paramsScroller.setContent(paramsContainer);
	    UIUtilities.applyBackground(paramsContainer, null);
	    paramsScroller.layoutChildren();
  }
  
  private int parameterTypeCode(Class type)
  {
    if (type == VertexParameterValue.class)
      return VERTEX_PARAM;
    if (type == FaceParameterValue.class)
      return FACE_PARAM;
    if (type == FaceVertexParameterValue.class)
      return FACE_VERTEX_PARAM;
    return CONSTANT_PARAM;
  }
  
  private void textureClicked(MouseClickedEvent ev)
  {
    if (ev.getClickCount() == 2)
    {
      int which = texList.getSelectedIndex();
      Texture tex = sc.getTexture(which);
      tex.edit(fr, sc);
      sc.changeTexture(which);
      renderPreview();
    }
  }
  
  private void doEditMapping()
  {
    int index = layerList.getSelectedIndex();
    new TextureMappingDialog(fr, editObj.object, index);
    editObj.setTexture(editObj.object.getTexture(), editObj.object.getTextureMapping());
    preview.setTexture(editObj.object.getTexture(), editObj.object.getTextureMapping());
    renderPreview();
  }
  
  private void doNewTexture()
  {
    TexturesDialog.showNewTextureWindow(this, sc);
  }
  
  private void doEditTextures()
  {
    sc.showTexturesDialog(fr);
    buildList();
    renderPreview();
  }
  
  private void doAddLayer()
  {
    preview.cancelRendering();
    Texture tex = sc.getTexture(texList.getSelectedIndex());
    layeredMap.addLayer(tex);
    layerList.add(0, tex.getName());
    layerList.setSelected(0, true);
    resetParameters();
    updateComponents();
  }
  
  private void doDeleteLayer()
  {
    int index = layerList.getSelectedIndex();
    preview.cancelRendering();
    layeredMap.deleteLayer(index);
    layerList.remove(index);
    resetParameters();
    updateComponents();
  }
  
  private void doMoveLayerUp()
  {
    int index = layerList.getSelectedIndex();
    preview.cancelRendering();
    String label = (String) layerList.getItem(index);
    layeredMap.moveLayer(index, index-1);
    layerList.remove(index);
    layerList.add(index-1, label);
    layerList.setSelected(index-1, true);
    resetParameters();
    updateComponents();
  }

  private void doMoveLayerDown()
  {
    int index = layerList.getSelectedIndex();
    preview.cancelRendering();
    String label = (String) layerList.getItem(index);
    layeredMap.moveLayer(index, index+1);
    layerList.remove(index);
    layerList.add(index+1, label);
    layerList.setSelected(index+1, true);
    resetParameters();
    updateComponents();
  }
  
  private void doOk()
  {
    TextureParameter param[] = editObj.object.getParameters();
    ParameterValue paramValue[] = editObj.object.getParameterValues();
    for (int i = 0; i < obj.length; i++)
    {
      if (editObj.object.getTexture() instanceof LayeredTexture)
      {
        LayeredMapping m = (LayeredMapping) editObj.object.getTextureMapping().duplicate();
        obj[i].setTexture(new LayeredTexture(m), m);
      }
      else
        obj[i].setTexture(editObj.object.getTexture(), editObj.object.getTextureMapping().duplicate());
      for (int j = 0; j < param.length; j++)
        obj[i].object.setParameterValue(param[j], paramValue[j].duplicate());
    }
    obj[0].object.copyObject(editObj.object);
    if (fr instanceof LayoutWindow)
    {
      ((LayoutWindow) fr).updateImage();
      ((LayoutWindow) fr).getScore().tracksModified(false);
    }
    if (callback != null)
      callback.run();
    dispose();
  }
  
  private void doCancel()
  {
    dispose();
    if (callback != null)
      callback.run();
  }
  
  private void paramTypeChanged(ValueChangedEvent ev)
  {
    Widget src = ev.getWidget();
    for (int i = 0; i < paramTypeChoice.length; i++)
      if (src == paramTypeChoice[i])
      {
        // Change whether the parameter is set globally, per-vertex, per-face, etc.
        
        String type = (String) paramTypeChoice[i].getSelectedValue();
        int index = fieldParamIndex[i];
        if (paramValueWidget[i] != null)
          paramValueWidget[i].setEnabled(type == PARAM_TYPE_NAME[CONSTANT_PARAM]);
        TextureParameter param = editObj.object.getParameters()[index];
        Object3D realObject = editObj.object;
        while (realObject instanceof ObjectWrapper)
          realObject = ((ObjectWrapper) realObject).getWrappedObject();
        if (type == PARAM_TYPE_NAME[CONSTANT_PARAM])
          editObj.object.setParameterValue(param, new ConstantParameterValue(editObj.object.getParameterValues()[index].getAverageValue()));
        else if (type == PARAM_TYPE_NAME[VERTEX_PARAM])
          editObj.object.setParameterValue(param, new VertexParameterValue((Mesh)realObject, param));
        else if (type == PARAM_TYPE_NAME[FACE_PARAM])
          editObj.object.setParameterValue(param, new FaceParameterValue((FacetedMesh)realObject, param));
        else if (type == PARAM_TYPE_NAME[FACE_VERTEX_PARAM])
          editObj.object.setParameterValue(param, new FaceVertexParameterValue((FacetedMesh)realObject, param));
        renderPreview();
        return;
      }
  }
  
  private void typeChanged()
  {
    boolean layered = (typeChoice.getSelectedIndex() == 1);
    if (!layered)
    {
      texList.setSelected(0, true);
      Texture tex = sc.getDefaultTexture();
      editObj.setTexture(tex, tex.getDefaultMapping(editObj.object));
      updateComponents();
      layoutSimple();
      pack();
      UIUtilities.centerWindow(this);
      resetParameters();
    }
    else
    {
      preview.cancelRendering();
      editObj.setTexture(layeredTex, layeredMap);
      preview.setTexture(layeredTex, layeredMap);
      updateComponents();
      layoutLayered();
      pack();
      UIUtilities.centerWindow(this);
      resetParameters();
      return;
    }
  }
  
  private void blendTypeChanged()
  {
    layeredMap.setLayerMode(layerList.getSelectedIndex(), blendChoice.getSelectedIndex());
    renderPreview();
  }
  
  private void selectionChanged()
  {
    boolean layered = (typeChoice.getSelectedIndex() == 1);
    boolean anyselection;
    if (layered)
      anyselection = (layerList.getSelectedIndex() > -1);
    else
      anyselection = (texList.getSelectedIndex() > -1);
    if (!anyselection)
    {
      updateComponents();
      return;
    }
    Texture tex;
    if (layered)
      tex = layeredMap.getLayer(layerList.getSelectedIndex());
    else
      tex = sc.getTexture(texList.getSelectedIndex());
    if (!layered)
    {
      if (tex == oldTexture)
        editObj.setTexture(tex, oldMapping.duplicate());
      else
        editObj.setTexture(tex, tex.getDefaultMapping(editObj.object));
      preview.setTexture(tex, editObj.object.getTextureMapping());
    }
    renderPreview();
    updateComponents();
  }
  
  /* Utility routine.  This should be called whenever the list of texture parameters
     might have changed.  It ensures that all parameter lists are correctly updated,
     then re-renders the preview. */
  
  private void resetParameters()
  {
    preview.cancelRendering();
    editObj.setTexture(editObj.object.getTexture(), editObj.object.getTextureMapping());
    preview.getObject().setTexture(preview.getObject().object.getTexture(), preview.getObject().object.getTextureMapping());
    renderPreview();
  }

  // Update the status of various components.
  
  private void updateComponents()
  {
    boolean layered = (typeChoice.getSelectedIndex() == 1);
    boolean anyselection = (texList.getSelectedIndex() > -1);
    Texture tex;

    buildParamList();
    if (layered)
    {
      addLayerButton.setEnabled(anyselection);
      int index = layerList.getSelectedIndex();
      if (index == -1)
      {
        mapButton.setEnabled(false);
        deleteLayerButton.setEnabled(false);
        moveUpButton.setEnabled(false);
        moveDownButton.setEnabled(false);
        blendChoice.setEnabled(false);
        return;
      }
      tex = layeredMap.getLayer(index);
      mapButton.setEnabled(true);
      deleteLayerButton.setEnabled(true);
      moveUpButton.setEnabled(index > 0);
      moveDownButton.setEnabled(index < layeredMap.getLayers().length-1);
      blendChoice.setEnabled(true);
      blendChoice.setSelectedIndex(layeredMap.getLayerMode(index));
    }
    else if (anyselection)
    {
      mapButton.setEnabled(true);
      tex = sc.getTexture(texList.getSelectedIndex());
    }
    else
      mapButton.setEnabled(false);
  }
  
  /* ListChangeListener methods. */
  
  @Override
  public void itemAdded(int index, Object obj)
  {
    Texture tex = (Texture) obj;
    texList.add(index, tex.getName());
  }
  
  @Override
  public void itemRemoved(int index, Object obj)
  {
    Texture tex = (Texture) obj;
    
    texList.remove(index);
    if (editObj.object.getTextureMapping() instanceof LayeredMapping)
    {
      Texture layers[] = layeredMap.getLayers();
      for (int i = layers.length-1; i >= 0; i--)
        if (layers[i] == tex)
        {
          layeredMap.deleteLayer(i);
          layerList.remove(i);
        }
      renderPreview();
      updateComponents();
    }
    else if (editObj.object.getTexture() == tex)
    {
      editObj.setTexture(sc.getDefaultTexture(), sc.getDefaultTexture().getDefaultMapping(editObj.object));
      preview.setTexture(editObj.object.getTexture(), editObj.object.getTextureMapping());
      renderPreview();
      updateComponents();
    }
  }
  
  @Override
  public void itemChanged(int index, Object obj)
  {
    Texture tex = (Texture) obj;
    texList.replace(index, tex.getName());
  }
  
  /**
   * Rerender the preview image after the texture has changed.
   */
  
  private void renderPreview()
  {
    renderProcessor.addEvent(new Runnable()
    {
      @Override
      public void run()
      {
        preview.render();
      }
    });
  }
}