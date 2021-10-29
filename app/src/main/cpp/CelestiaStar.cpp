/*
 * CelestiaStar.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#include <celengine/star.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_Star_c_1getWebInfoURL(JNIEnv *env, jclass clazz, jlong pointer) {
    auto star = (Star *)pointer;
    return env->NewStringUTF(star->getInfoURL().c_str());
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_Star_c_1getPositionAtTime(JNIEnv *env, jclass clazz, jlong pointer,
                                                                          jdouble julian_day) {
    auto star = (Star *)pointer;
    return (jlong)new UniversalCoord(star->getPosition(julian_day));
}