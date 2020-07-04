/*
 * CelestiaUtils.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaVector.h"
#include <celengine/astro.h>
#include <celengine/observer.h>

extern "C"
JNIEXPORT jintArray JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaUtils_getJulianDayComponents(JNIEnv *env,
                                                                             jclass clazz,
                                                                             jdouble julian_day) {
    astro::Date astroDate(julian_day);
    jint date[8] = { 0 };

    int year = astroDate.year;
    int era = 1;
    if (year < 1)
    {
        era  = 0;
        year = 1 - year;
    }

    date[0] = era;
    date[1] = year;
    date[2] = astroDate.month;
    date[3] = astroDate.day;
    date[4] = astroDate.hour;
    date[5] = astroDate.minute;
    date[6] = (int)floor(astroDate.seconds);
    date[7] = (int)((astroDate.seconds - date[6]) * 1000);
    jintArray array = env->NewIntArray(8);
    env->SetIntArrayRegion(array, 0, 8, date);
    return array;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaUtils_getJulianDay(JNIEnv *env, jclass clazz,
                                                                   jint era, jint year, jint month,
                                                                   jint day, jint hour, jint minute,
                                                                   jint second, jint millisecond) {
    if (era < 1) year = 1 - year;
    astro::Date astroDate(year, month, day);
    astroDate.hour    = hour;
    astroDate.minute  = minute;
    astroDate.seconds = second;

    astroDate.seconds += millisecond / (double)1000;

    double jd = astro::UTCtoTDB(astroDate);
    return jd;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaUtils_celToJ2000Ecliptic(JNIEnv *env, jclass clazz,
                                                                         jobject cel) {
    Eigen::Vector3d p = vector3dFromObject(env, cel);
    return createVectorForVector3d(env, Eigen::Vector3d(p.x(), -p.z(), p.y()));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaUtils_eclipticToEquatorial(JNIEnv *env,
                                                                           jclass clazz,
                                                                           jobject ecliptic) {
    Eigen::Vector3d p = vector3dFromObject(env, ecliptic);
    return createVectorForVector3d(env, astro::eclipticToEquatorial(p));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaUtils_equatorialToGalactic(JNIEnv *env,
                                                                           jclass clazz,
                                                                           jobject equatorial) {
    Eigen::Vector3d p = vector3dFromObject(env, equatorial);
    return createVectorForVector3d(env, astro::equatorialToGalactic(p));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaUtils_rectToSpherical(JNIEnv *env, jclass clazz,
                                                                      jobject rect) {
    Eigen::Vector3d v = vector3dFromObject(env, rect);
    double r = v.norm();
    double theta = atan2(v.y(), v.x());
    if (theta < 0)
        theta = theta + 2 * PI;
    double phi = asin(v.z() / r);

    return createVectorForVector3d(env, Eigen::Vector3d(theta, phi, r));
}