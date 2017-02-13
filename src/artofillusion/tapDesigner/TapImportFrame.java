/* Material or textures import/export from/to AoI scene */

/* Copyright (C) 2004 by Francois Guillet

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.tapDesigner;

import artofillusion.*;

import artofillusion.image.*;

import artofillusion.material.*;

import artofillusion.texture.*;

import buoy.event.*;

import buoy.widget.*;

import java.awt.*;

import java.util.*;

import javax.swing.*;


public class TapImportFrame
    extends BDialog
{
    BList[]   lists;
    Scene[]   scenes;
    BButton[] importButtons;
    boolean   isMaterial; //if not material, then  texture.

    public TapImportFrame(BFrame parentFrame, Scene tapdScene, Scene aoiScene, boolean isMaterial)
    {
        super(parentFrame, isMaterial ? TapBTranslate.text("materialManagement") : TapBTranslate.text("textureManagement"), true);

        int i;

        this.isMaterial = isMaterial;

        scenes = new Scene[2];
        scenes[0] = tapdScene;
        scenes[1] = aoiScene;

        BorderContainer border = new BorderContainer();
        setContent(border);

        importButtons = new BButton[2];
        importButtons[0] = TapBTranslate.bButton(">", this, "doCopyToScene");
        importButtons[1] = TapBTranslate.bButton("<", this, "doCopyToScene");

        double[]      colWeight = { 1.0 };
        double[]      rowWeight = { 1.0, 0.0, 1.0, 0.0, 1.0 };
        FormContainer buttonsFc = new FormContainer(colWeight, rowWeight);
        LayoutInfo    layout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(5, 5, 5, 5), new Dimension(0, 0));
        buttonsFc.add(importButtons[0], 0, 1, layout);
        buttonsFc.add(importButtons[1], 0, 3, layout);

        lists = new BList[2];

        for (i = 0; i < 2; ++i)
        {
            lists[i] = new BList();
            setListContents(lists[i], isMaterial ? getMaterialNames(scenes[i]) : getTextureNames(scenes[i]));
        }

        BScrollPane[] scrollPanes = new BScrollPane[2];

        for (i = 0; i < 2; ++i)
            scrollPanes[i] = new BScrollPane(lists[i]);

        LayoutInfo    listsLayout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, new Insets(5, 5, 5, 5), new Dimension(0, 0));
        FormContainer fc = new FormContainer(3, 4);
        fc.add(TapBTranslate.bLabel("tapdScene"), 0, 0, layout);
        fc.add(TapBTranslate.bLabel("aoiScene"), 2, 0, layout);
        fc.add(scrollPanes[0], 0, 1, listsLayout);
        fc.add(buttonsFc, 1, 1, layout);
        fc.add(scrollPanes[1], 2, 1, listsLayout);

        BButton quitButton = TapBTranslate.bButton("quit", this, "doQuit");
        fc.add(quitButton, 1, 2, layout);
        border.add(fc, BorderContainer.CENTER);

        pack();
        ((JDialog)getComponent()).setLocationRelativeTo(parentFrame.getComponent());
        setVisible(true);

        addEventLink(WindowClosingEvent.class, this, "doQuit");
    }

    private void doQuit()
    {
        dispose();
    }

    private void doCopyToScene(CommandEvent evt)
    {
        int from = 1;
        int to = 0;

        if (evt.getWidget() == importButtons[0])
        {
            from = 0;
            to = 1;
        }

        if (!isMaterial)
            doCopyTexturesToScene(from, to);
        else
            doCopyMaterialsToScene(from, to);
    }

    private void doCopyTexturesToScene(int from, int to)
    {
        int[]  selection = null;
        int    i;
        int    j;
        Vector textures = new Vector();
        Vector images = new Vector();

        if (lists[from].getSelectedIndex() >= 0)
            selection = lists[from].getSelectedIndices();

        if (selection != null)
        {
            Texture tex;
            Texture dup;

            for (i = 0; i < selection.length; ++i)
            {
                tex = scenes[from].getTexture(selection[i]);

                if (tex != null)
                {
                    dup = tex.duplicate();
                    dup.setID(tex.getID());
                    textures.addElement(dup);

                }
            }

            boolean used;

            for (i = 0; i < scenes[from].getNumImages(); i++)
            {
                ImageMap map = scenes[from].getImage(i);
                used = false;

                for (j = 0; j < textures.size() && !used; j++)
                    used = ((Texture)textures.elementAt(j)).usesImage(map);

                if (used)
                    images.addElement(map);
            }

            Texture[] textureArray = new Texture[textures.size()];
            textures.copyInto(textureArray);

            ImageMap[] imagesArray = new ImageMap[images.size()];
            images.copyInto(imagesArray);

            for (i = 0; i < imagesArray.length; i++)
            {
                for (j = 0; j < scenes[to].getNumImages() && imagesArray[i].getID() != scenes[to]
                          .getImage(j).getID(); j++)
                    ;

                if (j == scenes[to].getNumImages())
                    scenes[to].addImage(imagesArray[i]);
            }

            for (i = 0; i < textureArray.length; i++)
            {
                for (j = 0; j < scenes[to].getNumTextures() && textureArray[i].getID() != scenes[to]
                          .getTexture(j).getID(); j++)
                    ;

                if (j == scenes[to].getNumTextures() && !textureArray[i].getName()
                               .equals(scenes[to].getTexture(0).getName()))
                {
                    tex = textureArray[i];
                    tex.setID(textureArray[i].getID());
                    scenes[to].addTexture(tex);
                }
                else
                    JOptionPane.showMessageDialog(getComponent(), TapBTranslate.text("textureAlreadyDefined"), TapBTranslate.text("textureImportError"), JOptionPane.ERROR_MESSAGE);
            }

            for (i = 0; i < 2; ++i)
            {
                setListContents(lists[i], getTextureNames(scenes[i]));
                lists[i].clearSelection();
            }
        }
    }

    private void doCopyMaterialsToScene(int from, int to)
    {
        int[]  selection = null;
        int    i;
        int    j;
        Vector materials = new Vector();
        Vector images = new Vector();

        if (lists[from].getSelectedIndex() >= 0)
            selection = lists[from].getSelectedIndices();

        if (selection != null)
        {
            Material mat;
            Material dup;

            for (i = 0; i < selection.length; ++i)
            {
                mat = scenes[from].getMaterial(selection[i]);

                if (mat != null)
                {
                    dup = mat.duplicate();
                    dup.setID(mat.getID());
                    materials.addElement(dup);

                }
            }

            boolean used;

            for (i = 0; i < scenes[from].getNumImages(); i++)
            {
                ImageMap map = scenes[from].getImage(i);
                used = false;

                for (j = 0; j < materials.size() && !used; j++)
                    used = ((Material)materials.elementAt(j)).usesImage(map);

                if (used)
                    images.addElement(map);
            }

            Material[] materialArray = new Material[materials.size()];
            materials.copyInto(materialArray);

            ImageMap[] imagesArray = new ImageMap[images.size()];
            images.copyInto(imagesArray);

            for (i = 0; i < imagesArray.length; i++)
            {
                for (j = 0; j < scenes[to].getNumImages() && imagesArray[i].getID() != scenes[to]
                          .getImage(j).getID(); j++)
                    ;

                if (j == scenes[to].getNumImages())
                    scenes[to].addImage(imagesArray[i]);
            }

            for (i = 0; i < materialArray.length; i++)
            {
                for (j = 0; j < scenes[to].getNumMaterials() && materialArray[i].getID() != scenes[to]
                          .getMaterial(j).getID(); j++)
                    ;

                if (j == scenes[to].getNumMaterials())
                {
                    mat = materialArray[i];
                    mat.setID(materialArray[i].getID());
                    scenes[to].addMaterial(mat);
                }
                else
                    JOptionPane.showMessageDialog(getComponent(), TapBTranslate.text("materialAlreadyDefined"), TapBTranslate.text("materialImportError"), JOptionPane.ERROR_MESSAGE);
            }

            for (i = 0; i < 2; ++i)
            {
                setListContents(lists[i], getMaterialNames(scenes[i]));
                lists[i].clearSelection();
            }
        }
    }

    private Object[] getTextureNames(Scene scene)
    {
        int      numTextures = scene.getNumTextures();
        Object[] names = new Object[numTextures];

        for (int i = 0; i < numTextures; ++i)
            names[i] = (Object)scene.getTexture(i).getName();

        return names;
    }

    private Object[] getMaterialNames(Scene scene)
    {
        int numMaterials = scene.getNumMaterials();

        if (numMaterials == 0)
            return null;

        Object[] names = new Object[numMaterials];

        for (int i = 0; i < numMaterials; ++i)
            names[i] = (Object)scene.getMaterial(i).getName();

        return names;
    }

    private void setListContents(BList list, Object[] objects)
    {
        if (objects != null)
            list.setContents(objects);
        else
            list.removeAll();
    }
}
