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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

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

    public static Date createDateFromJulianDay(Double julianDay) {
        int[] compos = Utils.getJulianDayComponents(julianDay);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeZone(TimeZone.getTimeZone("GMT"));
        gc.set(GregorianCalendar.ERA, compos[0]);
        gc.set(GregorianCalendar.YEAR, compos[1]);
        gc.set(GregorianCalendar.MONTH, compos[2] - 1);
        gc.set(GregorianCalendar.DAY_OF_MONTH, compos[3]);
        gc.set(GregorianCalendar.HOUR_OF_DAY, compos[4]);
        gc.set(GregorianCalendar.MINUTE, compos[5]);
        gc.set(GregorianCalendar.SECOND, compos[6]);
        gc.set(GregorianCalendar.MILLISECOND, compos[7]);
        return gc.getTime();
    }
}
