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
Java_space_celestia_celestia_Body_c_1getType(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = reinterpret_cast<Body *>(pointer);
    return static_cast<jint>(body->getClassification());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_Body_c_1getName(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return env->NewStringUTF(body->getName(true).c_str());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_Body_c_1hasRings(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return (jboolean)(body->getRings() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_Body_c_1hasAtmosphere(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return (jboolean)(body->getAtmosphere() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_Body_c_1isEllipsoid(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return (jboolean)(body->isEllipsoid() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_space_celestia_celestia_Body_c_1getRadius(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return body->getRadius();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_Body_c_1getWebInfoURL(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return env->NewStringUTF(body->getInfoURL().c_str());
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Body_c_1getOrbitAtTime(JNIEnv *env, jclass clazz, jlong pointer,
                                                                       jdouble julian_day) {
    auto body = (Body *)pointer;
    return (jlong)(body->getOrbit(julian_day));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Body_c_1getRotationModelAtTime(JNIEnv *env,
                                                                               jclass clazz, jlong pointer,
                                                                               jdouble julian_day) {
    auto body = (Body *)pointer;
    return (jlong)(body->getRotationModel(julian_day));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Body_c_1getPlanetarySystem(JNIEnv *env,
                                                                           jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return (jlong)(body->getSystem());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Body_c_1getAlternateSurfaceNames(JNIEnv *env,
                                                                                 jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    auto altSurfaces = body->getAlternateSurfaceNames();
    if (!altSurfaces.has_value() || altSurfaces->empty())
        return  nullptr;

    jobject arrayObject = env->NewObject(alClz, aliMethodID, static_cast<jint>(altSurfaces->size()));
    for (const auto &surface : *altSurfaces)
    {
        jstring name = env->NewStringUTF(surface.c_str());
        env->CallBooleanMethod(arrayObject, alaMethodID, name);
        env->DeleteLocalRef(name);
    }
    return arrayObject;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Body_c_1getTimeline(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = (Body *)pointer;
    return (jlong)body->getTimeline();
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_Timeline_c_1getPhaseCount(JNIEnv *env, jclass clazz, jlong pointer) {
    auto timeline = (Timeline *)pointer;
    return (jint)timeline->phaseCount();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Timeline_c_1getPhase(JNIEnv *env, jclass clazz, jint index,
                                                  jlong pointer) {
    auto timeline = (Timeline *)pointer;
    return (jlong)timeline->getPhase((unsigned int)index).get();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_Timeline_00024Phase_c_1getStartTime(JNIEnv *env, jclass clazz,
                                                                 jlong pointer) {
    auto phase = (TimelinePhase *)pointer;
    return (jdouble)phase->startTime();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_Timeline_00024Phase_c_1getEndTime(JNIEnv *env, jclass clazz,
                                                               jlong pointer) {
    auto phase = (TimelinePhase *)pointer;
    return (jdouble)phase->endTime();
}