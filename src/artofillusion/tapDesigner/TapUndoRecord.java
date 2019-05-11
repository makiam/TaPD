/* Undo record buffer to proceed to undo/redo operations */

/* Copyright (C) 2004 by François Guillet
 *  Changes copyright (C) 2019 by Maksim Khramov
 *
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.tapDesigner;


public class TapUndoRecord
{
    TapProcedure[]     procedures;
    int                recordSize;
    int                index;
    TapProcedure       tmpProcedure;
    boolean            recordAdded;
    private final TapProcPanelHolder holder;

    public TapUndoRecord(TapProcPanelHolder holder, int recordSize)
    {
        this.holder = holder;
        setRecordSize(recordSize);
    }

    public void addRecord(TapProcedure procedure)
    {
        recordAdded = true;
        ++index;

        if (index == recordSize)
        {
            tmpProcedure = procedures[0];

            for (int i = 1; i < recordSize; ++i)
                procedures[i - 1] = procedures[i];

            --index;

        }
        else
            tmpProcedure = null;

        procedures[index] = procedure;
        
        procedure.setModified(true);

        for (int i = index + 1; i < recordSize; ++i)
            procedures[i] = null;

        holder.setUndoRedoFlags(true, false);

    }

    public void cancelLastRecord()
    {
        if (tmpProcedure == null)
        {
            --index;

            return;
        }

        for (int i = recordSize - 1; i > 0; --i)
            procedures[i] = procedures[i - 1];

        procedures[0] = tmpProcedure;
    }

    public TapProcedure getUndoRecord(TapProcedure procedure)
    {
        if (index < 0)
            return null;

        if (recordAdded)
        {
            addRecord(procedure);
            recordAdded = false;
            index -= 2;
        }
        else
            index -= 1;

        holder.setUndoRedoFlags(index >= 0, true);

        return procedures[index + 1];
    }

    public TapProcedure getRedoRecord()
    {
        if (index >= recordSize - 2)
            return null;

        if (procedures[index + 2] == null)
            return null;

        ++index;

        boolean canRedo = false;

        if (index < recordSize - 2)
            if (procedures[index + 2] != null)
                canRedo = true;

        holder.setUndoRedoFlags(true, canRedo);

        return procedures[index + 1];
    }

    public int getRecordSize()
    {
        return recordSize;
    }
    
    public void setRecordSize(int recordSize)
     {   this.recordSize = recordSize;
         procedures = new TapProcedure[recordSize];
         index = -1;
         recordAdded = false;
         holder.setUndoRedoFlags(false, false);
     }
}
