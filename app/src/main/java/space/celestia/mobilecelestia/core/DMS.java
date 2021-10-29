/*
 * CelestiaDMS.java
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.core;

public class DMS {
    private int degrees;
    private int minutes;
    private double seconds;

    public DMS(int degrees, int minutes, double seconds) {
        this.degrees = degrees;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public DMS(double decimal) {
        this.degrees = c_getDegrees(decimal);
        this.minutes = c_getMinutes(decimal);
        this.seconds = c_getSeconds(decimal);
    }

    public int getDegrees() {
        return degrees;
    }

    public int getHours() {
        return getDegrees();
    }

    public int getMinutes() {
        return minutes;
    }

    public double getSeconds() {
        return seconds;
    }

    public double getDecimal() {
        return c_getDecimal(degrees, minutes, seconds);
    }

    private native int c_getDegrees(double decimal);
    private native int c_getMinutes(double decimal);
    private native double c_getSeconds(double decimal);
    private native double c_getDecimal(int degrees, int minutes, double seconds);
}
