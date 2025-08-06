// CelestiaDMS.cpp
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

#include "CelestiaJNI.h"
#include <celastro/astro.h>

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_DMS_c_1getDegrees(JNIEnv *env, jclass clazz, jdouble decimal) {
    int degrees;
    int minutes;
    double seconds;
    celestia::astro::decimalToDegMinSec(static_cast<double>(decimal), degrees, minutes, seconds);
    return static_cast<jint>(degrees);
}
extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_DMS_c_1getMinutes(JNIEnv *env, jclass clazz, jdouble decimal) {
    int degrees;
    int minutes;
    double seconds;
    celestia::astro::decimalToDegMinSec(static_cast<double>(decimal), degrees, minutes, seconds);
    return static_cast<jint>(minutes);
}
extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_DMS_c_1getSeconds(JNIEnv *env, jclass clazz, jdouble decimal) {
    int degrees;
    int minutes;
    double seconds;
    celestia::astro::decimalToDegMinSec(static_cast<double>(decimal), degrees, minutes, seconds);
    return static_cast<jdouble>(seconds);
}
extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_DMS_c_1getHMSHours(JNIEnv *env, jclass clazz, jdouble decimal) {
    int hours;
    int minutes;
    double seconds;
    celestia::astro::decimalToHourMinSec(static_cast<double>(decimal), hours, minutes, seconds);
    return static_cast<jint>(hours);
}
extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_DMS_c_1getHMSMinutes(JNIEnv *env, jclass clazz, jdouble decimal) {
    int hours;
    int minutes;
    double seconds;
    celestia::astro::decimalToHourMinSec(static_cast<double>(decimal), hours, minutes, seconds);
    return static_cast<jint>(minutes);
}
extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_DMS_c_1getHMSSeconds(JNIEnv *env, jclass clazz, jdouble decimal) {
    int hours;
    int minutes;
    double seconds;
    celestia::astro::decimalToHourMinSec(static_cast<double>(decimal), hours, minutes, seconds);
    return static_cast<jdouble>(seconds);
}
extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_DMS_c_1getDecimal(JNIEnv *env, jclass clazz, jint degrees,
                                               jint minutes, jdouble seconds) {
    return static_cast<jdouble>(celestia::astro::degMinSecToDecimal(static_cast<int>(degrees), static_cast<int>(minutes), static_cast<double >(seconds)));
}