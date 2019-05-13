/* Random number generator, the use of which is fairly striaghtforward. */

/* Copyright 2003 François Guillet
 *  Changes copyright (C) 2019 by Maksim Khramov
 *
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. 
*/

package artofillusion.tapDesigner;

import java.util.*;



/**
Random Number Generator
@author François Guillet
*/

class TapRandomGenerator
{	Random rand;
	//long currentSeed;
	
	public static final short UNIFORM = 0;
	public static final short GAUSSIAN = 1;
	
	public TapRandomGenerator(long seed)
	{
		 rand = new Random();
		 rand.setSeed(seed);
		 for (int i=0;i<10;++i) rand.nextDouble();
	}
	
	public double uniformDeviate()
	{	return rand.nextDouble();
	}
	
	public double uniformDeviate(double mean, double plusminus)
	{	double val = uniformDeviate();
		val = mean + (val-0.5)*2*plusminus;
		return val;
	}
	
	public long getSeed()
	{	return rand.nextLong();
	}
	
	public double getDistribution(double mean, double plusminus, short type)
	{	double val;
		switch (type)
		{	default :
			case UNIFORM :
				val = mean + (rand.nextDouble()-0.5)*2*plusminus;
				return val;
			case GAUSSIAN :
				val = rand.nextGaussian()*plusminus+mean;
				return val;
		}
	}
	
	public int integer(int n)
	{	return rand.nextInt(n);
	}
}
