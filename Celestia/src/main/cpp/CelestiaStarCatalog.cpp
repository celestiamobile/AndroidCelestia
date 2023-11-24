/*
 * CelestiaStarCatalog.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#include <celengine/stardb.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_StarCatalog_c_1getStarName(JNIEnv *env, jclass clazz, jlong ptr, jlong pointer) {
    auto d = reinterpret_cast<StarDatabase *>(ptr);
    return env->NewStringUTF(d->getStarName(*reinterpret_cast<Star *>(pointer), true).c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_StarCatalog_c_1getCount(JNIEnv *env, jclass clazz, jlong ptr) {
    auto d = reinterpret_cast<StarDatabase *>(ptr);
    return static_cast<jint>(d->size());
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_StarCatalog_c_1getStar(JNIEnv *env, jclass clazz, jlong ptr, jint index) {
    auto d = reinterpret_cast<StarDatabase *>(ptr);
    return reinterpret_cast<jlong>(d->getStar(index));
}