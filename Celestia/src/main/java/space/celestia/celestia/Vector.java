/*
 * Vector.java
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.celestia;

public class Vector {
    private double[] array;

    public double getX() { return array[0]; }
    public double getY() { return array[1]; }
    public double getZ() { return array[2]; }
    public double getW() { return array[3]; }

    private Vector(double x, double y, double z, double w) {
        array = new double[] {x, y, z, w};
    }

    private Vector(double x, double y, double z) {
        array = new double[] {x, y, z};
    }
}
