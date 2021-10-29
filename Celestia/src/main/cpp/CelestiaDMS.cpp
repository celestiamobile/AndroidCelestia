/*
 * CelestiaDMS.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#include <celengine/astro.h>

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_DMS_c_1getDegrees(JNIEnv *env, jobject thiz,
                                                          jdouble decimal) {
    int degrees, minutes;
    double seconds;
    astro::decimalToDegMinSec(decimal, degrees, minutes, seconds);
    return degrees;
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_DMS_c_1getMinutes(JNIEnv *env, jobject thiz,
                                                          jdouble decimal) {
    int degrees, minutes;
    double seconds;
    astro::decimalToDegMinSec(decimal, degrees, minutes, seconds);
    return minutes;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_DMS_c_1getSeconds(JNIEnv *env, jobject thiz,
                                                          jdouble decimal) {
    int degrees, minutes;
    double seconds;
    astro::decimalToDegMinSec(decimal, degrees, minutes, seconds);
    return seconds;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_DMS_c_1getDecimal(JNIEnv *env, jobject thiz,
                                                          jint degrees, jint minutes,
                                                          jdouble seconds) {
    return astro::degMinSecToDecimal(degrees, minutes, seconds);
}