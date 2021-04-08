/*
 * CelestiaBody.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#include <celengine/body.h>

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getType(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return body->getClassification();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getName(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return env->NewStringUTF(body->getName(true).c_str());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1hasRings(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return (jboolean)(body->getRings() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1hasAtmosphere(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return (jboolean)(body->getAtmosphere() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1isEllipsoid(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return (jboolean)(body->isEllipsoid() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getRadius(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return body->getRadius();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getWebInfoURL(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return env->NewStringUTF(body->getInfoURL().c_str());
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getOrbitAtTime(JNIEnv *env, jclass clazz, jlong pointer,
                                                                       jdouble julian_day) {
    auto body = (Body *)pointer;
    return (jlong)(body->getOrbit(julian_day));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getRotationModelAtTime(JNIEnv *env,
                                                                               jclass clazz, jlong pointer,
                                                                               jdouble julian_day) {
    auto body = (Body *)pointer;
    return (jlong)(body->getRotationModel(julian_day));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getPlanetarySystem(JNIEnv *env,
                                                                           jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return (jlong)(body->getSystem());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getAlternateSurfaceNames(JNIEnv *env,
                                                                                 jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    std::vector<std::string> *altSurfaces = body->getAlternateSurfaceNames();
    if (!altSurfaces || altSurfaces->empty())
        return  nullptr;

    jobject arrayObject = env->NewObject(alClz, aliMethodID, (int)altSurfaces->size());
    for (auto &altSurface : *altSurfaces)
        env->CallBooleanMethod(arrayObject, alaMethodID, env->NewStringUTF(altSurface.c_str()));
    return arrayObject;
}