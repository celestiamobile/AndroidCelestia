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
Java_space_celestia_mobilecelestia_core_CelestiaStar_c_1getWebInfoURL(JNIEnv *env, jobject thiz) {
    Star *star = (Star *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(star->getInfoURL().c_str());
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaStar_c_1getPositionAtTime(JNIEnv *env, jobject thiz,
                                                                          jdouble julian_day) {
    Star *star = (Star *)env->GetLongField(thiz, caoPtrFieldID);
    return (jlong)new UniversalCoord(star->getPosition(julian_day));
}