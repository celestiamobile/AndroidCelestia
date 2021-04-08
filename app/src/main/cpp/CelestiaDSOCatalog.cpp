/*
 * CelestiaDSOCatalog.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#include <celengine/dsodb.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDSOCatalog_c_1getDSOName(JNIEnv *env, jclass clazz, jlong ptr, jlong pointer) {
    auto d = (DSODatabase *)ptr;
    return env->NewStringUTF(d->getDSOName((DeepSkyObject *)pointer, true).c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDSOCatalog_c_1getCount(JNIEnv *env, jclass clazz, jlong ptr) {
    auto d = (DSODatabase *)ptr;
    return d->size();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDSOCatalog_c_1getDSO(JNIEnv *env, jclass clazz, jlong ptr, jint index) {
    auto d = (DSODatabase *)ptr;
    return (jlong)d->getDSO(index);
}