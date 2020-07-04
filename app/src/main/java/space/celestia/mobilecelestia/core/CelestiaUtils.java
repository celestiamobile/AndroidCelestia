/*
 * CelestiaUtils.java
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;

public class CelestiaUtils {
    public static native double getJulianDay(int era, int year, int month, int day, int hour, int minute, int second, int millisecond);
    public static native int[] getJulianDayComponents(double julianDay);

    public static native @NonNull CelestiaVector celToJ2000Ecliptic(@NonNull CelestiaVector cel);
    public static native @NonNull CelestiaVector eclipticToEquatorial(@NonNull CelestiaVector ecliptic);
    public static native @NonNull CelestiaVector equatorialToGalactic(@NonNull CelestiaVector equatorial);
    public static native @NonNull CelestiaVector rectToSpherical(@NonNull CelestiaVector rect);
}
