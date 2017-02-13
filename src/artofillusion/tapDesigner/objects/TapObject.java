/* This interface specifies methods which should be implemented for special TapObject.
These methods allow to
-set global shape using distort parameters
-get position at a given height
-distort an object upon creating a branch (branching effect) */

/* Copyright 2003 Francois Guillet

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. 
*/
package artofillusion.tapDesigner;

import artofillusion.*;

import artofillusion.math.*;

import artofillusion.object.*;

import buoy.widget.*;


public interface TapObject
{
    /* distortion of a TaPD object*/
    public void setShape(TapDistortParameters parms);

    /* position along the 'Y axis'*/
    public Mat4 getPosition(double yPos, double angle, boolean rDisplace);

    /* branching effects */
    public Mat4 setCounterAction(double yPos, double angle, TapDistortParameters parms);

    /* scaling and distort at the same time to avoid multiple calls*/
    public void resizeAndDistort(Vec3 size, double sizeR, double sizeY, TapDistortParameters parms);

    /* calculating a mesh after each call can be time and memory consuming.
    call regenerateMesh() after all distortion processes are over (branching effects for example)*/
    public void regenerateMesh();

    /* returns a proper AoI object to paste into the scene */
    public Object3D getPlainAoIObject();
}
