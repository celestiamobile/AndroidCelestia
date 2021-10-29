/*
 * CelestiaDSO.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaVector.h"
#include <celengine/deepskyobj.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_DSO_c_1getWebInfoURL(JNIEnv *env, jclass clazz, jlong pointer) {
    auto dso = (DeepSkyObject *)pointer;
    return env->NewStringUTF(dso->getInfoURL().c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_DSO_c_1getType(JNIEnv *env, jclass clazz, jlong pointer) {
    auto dso = (DeepSkyObject *)pointer;
    return env->NewStringUTF(dso->getType());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_DSO_c_1getPosition(JNIEnv *env, jclass clazz, jlong pointer) {
    auto dso = (DeepSkyObject *)pointer;
    return createVectorForVector3d(env, dso->getPosition());
}