// Utils.java
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestia;

import androidx.annotation.NonNull;

import java.util.Date;

public class Utils {
    public static native double getJulianDay(long milliSecondsFromEpoch);
    public static native long getMilliSecondsFromEpochFromJulianDay(double julianDay);

    public static native @NonNull
    Vector celToJ2000Ecliptic(@NonNull Vector cel);
    public static native @NonNull
    Vector eclipticToEquatorial(@NonNull Vector ecliptic);
    public static native @NonNull
    Vector equatorialToGalactic(@NonNull Vector equatorial);
    public static native @NonNull
    Vector rectToSpherical(@NonNull Vector rect);
    public static native double AUToKilometers(double au);
    public static native double lightYearsToParsecs(double ly);
    public static native double parsecsToLightYears(double pc);
    public static native double lightYearsToKilometers(double ly);
    public static native double kilometersToLightYears(double km);
    public static native double lightYearsToAU(double ly);
    public static native double AUtoLightYears(double au);
    public static native double kilometersToAU(double km);
    public static native double AUtoKilometers(double au);
    public static native double microLightYearsToKilometers(double mly);
    public static native double kilometersToMicroLightYears(double km);
    public static native double microLightYearsToAU(double mly);
    public static native double AUtoMicroLightYears(double au);
    public static native double degFromRad(double rad);
    public static native float[] transformQuaternion(float[] q, float angleZ);

    public static Date createDateFromJulianDay(Double julianDay) {
        return new Date(getMilliSecondsFromEpochFromJulianDay(julianDay));
    }
}
