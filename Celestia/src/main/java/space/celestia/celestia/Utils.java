/*
 * Utils.java
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.celestia;

import androidx.annotation.NonNull;

public class Utils {
    public static native double getJulianDay(int era, int year, int month, int day, int hour, int minute, int second, int millisecond);
    public static native int[] getJulianDayComponents(double julianDay);

    public static native @NonNull
    Vector celToJ2000Ecliptic(@NonNull Vector cel);
    public static native @NonNull
    Vector eclipticToEquatorial(@NonNull Vector ecliptic);
    public static native @NonNull
    Vector equatorialToGalactic(@NonNull Vector equatorial);
    public static native @NonNull
    Vector rectToSpherical(@NonNull Vector rect);
    public static native double AUToKilometers(double au);
}
