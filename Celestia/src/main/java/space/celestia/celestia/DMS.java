// DMS.java
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestia;

public class DMS {
    private double decimal;

    public DMS(int degrees, int minutes, double seconds) {
        decimal = c_getDecimal(degrees, minutes, seconds);
    }

    public DMS(double decimal) {
        this.decimal = decimal;
    }

    public int getDegrees() {
        return c_getDegrees(decimal);
    }

    public int getMinutes() {
        return c_getMinutes(decimal);
    }

    public double getSeconds() {
        return c_getSeconds(decimal);
    }

    public int getHMSHours() {
        return c_getHMSHours(decimal);
    }

    public int getHMSMinutes() {
        return c_getHMSMinutes(decimal);
    }

    public double getHMSSeconds() {
        return c_getHMSSeconds(decimal);
    }

    private static native int c_getDegrees(double decimal);
    private static native int c_getMinutes(double decimal);
    private static native double c_getSeconds(double decimal);
    private static native int c_getHMSHours(double decimal);
    private static native int c_getHMSMinutes(double decimal);
    private static native double c_getHMSSeconds(double decimal);
    private static native double c_getDecimal(int degrees, int minutes, double seconds);
}
