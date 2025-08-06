// CelestiaDSO.cpp
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

#include "CelestiaVector.h"
#include <celengine/deepskyobj.h>
#include <celengine/galaxy.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_DSO_c_1getWebInfoURL(JNIEnv *env, jclass clazz, jlong pointer) {
    auto dso = (DeepSkyObject *)pointer;
    return env->NewStringUTF(dso->getInfoURL().c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_DSO_c_1getType(JNIEnv *env, jclass clazz, jlong pointer) {
    auto dso = (DeepSkyObject *)pointer;
    return env->NewStringUTF(dso->getType());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_DSO_c_1getPosition(JNIEnv *env, jclass clazz, jlong pointer) {
    auto dso = (DeepSkyObject *)pointer;
    return createVectorForVector3d(env, dso->getPosition());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_DSO_c_1getDescription(JNIEnv *env, jclass clazz, jlong pointer) {
    auto dso = reinterpret_cast<DeepSkyObject *>(pointer);
    return env->NewStringUTF(dso->getDescription().c_str());
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_space_celestia_celestia_Galaxy_c_1getRadius(JNIEnv *env, jclass clazz, jlong pointer) {
    auto galaxy = reinterpret_cast<Galaxy *>(pointer);
    return galaxy->getRadius();
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_space_celestia_celestia_Galaxy_c_1getDetail(JNIEnv *env, jclass clazz, jlong pointer) {
    auto galaxy = reinterpret_cast<Galaxy *>(pointer);
    return galaxy->getDetail();
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_DSO_c_1getObjectType(JNIEnv *env, jclass clazz, jlong pointer) {
    auto dso = reinterpret_cast<DeepSkyObject *>(pointer);
    return static_cast<jint>(dso->getObjType());
}