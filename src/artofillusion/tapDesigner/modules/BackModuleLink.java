/* The BackModuleLink class is used to find which modules have their output ports
linked to a specific module. Used to find upstream path. */

/* Copyright (C) 2003 by François Guillet
 *  Changes copyright (C) 2019 by Maksim Khramov
 *
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.tapDesigner;


import java.util.*;


public class BackModuleLink
{
    private final List<TapModule> modules;
    private final int[][]            backLinks;
    private final int[][]            backOutputPorts;
    private final int[][]            backInputPorts;
    private final TapRandomGenerator gen;

    public BackModuleLink(List<TapModule> modules, long seed)
    {
        this.modules = modules;
        gen = new TapRandomGenerator(seed);
        backLinks = new int[modules.size()][];
        backOutputPorts = new int[modules.size()][];
        backInputPorts = new int[modules.size()][];

        for (int i = 0; i < modules.size(); ++i)
        {
            backLinks[i] = null;
            backOutputPorts[i] = null;
            backInputPorts[i] = null;
        }

        int numLinks;
        int numOutput;
        int inputPortLink;
        int i;
        int j;
        int k;
        int l;
        int index;

        for (i = 0; i < modules.size(); ++i)
        {
            TapModule mod = modules.get(i);
            numOutput = mod.getNumOutput();

            if (numOutput > 0)
                for (j = 0; j < numOutput; ++j)
                {
                    numLinks = mod.getNumLinks(j);

                    if (numLinks > 0)
                        for (k = 0; k < numLinks; ++k)
                        {
                            inputPortLink = mod.getInputPortLink(j, k);
                            index = mod.getLinkToIndex(j, k);

                            if (index >= 0 && inputPortLink >= 0)
                            {
                                backLinks[index] = TapUtils.increaseIntArray(backLinks[index]);
                                backOutputPorts[index] = TapUtils.increaseIntArray(backOutputPorts[index]);
                                backInputPorts[index] = TapUtils.increaseIntArray(backInputPorts[index]);
                                l = backLinks[index].length - 1;
                                backLinks[index][l] = i;
                                backOutputPorts[index][l] = j;
                                backInputPorts[index][l] = inputPortLink;
                            }
                        }
                }
        }
    }

    public List<BackLink> findAllModules(TapModule toModule, int inputPort)
    {   int    i;
        int    j;
        int    index;
        List<BackLink> bl = new Vector();
        index = modules.indexOf(toModule);

        for (i = 0; i < modules.size(); ++i)
            if (modules.get(i) == toModule)
                index = i;

        if (backLinks[index] != null)
            for (j = 0; j < backLinks[index].length; ++j)
            {
                if (backInputPorts[index][j] == inputPort)
                {
                    BackLink tmpbl = new BackLink(modules.get(backLinks[index][j]), backOutputPorts[index][j]);
                    bl.add(tmpbl);
                }

            }
        return bl;
    }
    
    public BackLink findModule(TapModule toModule, int inputPort)
    {
        List<BackLink> bl = findAllModules(toModule,inputPort);
        
        if (bl.size() <= 0)
            return null;
        else if (bl.size() == 1)
            return bl.get(0);
        else
            return bl.get(gen.integer(bl.size()));
    }

    public class BackLink
    {
        public TapModule fromModule;
        public int       outputPort;

        public BackLink(TapModule fromModule, int outputPort)
        {
            this.fromModule = fromModule;
            this.outputPort = outputPort;
        }
    }
}
