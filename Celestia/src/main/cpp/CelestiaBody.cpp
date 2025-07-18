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
#include <celengine/timeline.h>
#include <celengine/timelinephase.h>

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_Body_c_1getType(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = reinterpret_cast<Body *>(pointer);
    return static_cast<jint>(body->getClassification());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_Body_c_1getName(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = reinterpret_cast<Body *>(pointer);
    return env->NewStringUTF(body->getName(true).c_str());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_Body_c_1hasRings(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = reinterpret_cast<Body *>(pointer);
    return GetBodyFeaturesManager()->getRings(body) ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_Body_c_1hasAtmosphere(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = reinterpret_cast<Body *>(pointer);
    return GetBodyFeaturesManager()->getAtmosphere(body) ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_Body_c_1isEllipsoid(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = reinterpret_cast<Body *>(pointer);
    return static_cast<jboolean>(body->isEllipsoid() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_space_celestia_celestia_Body_c_1getRadius(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = reinterpret_cast<Body *>(pointer);
    return static_cast<jfloat>(body->getRadius());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_Body_c_1getWebInfoURL(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = reinterpret_cast<Body *>(pointer);
    return env->NewStringUTF(body->getInfoURL().c_str());
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Body_c_1getOrbitAtTime(JNIEnv *env, jclass clazz, jlong pointer,
                                                                       jdouble julian_day) {
    auto body = reinterpret_cast<Body *>(pointer);
    return reinterpret_cast<jlong>(body->getOrbit(julian_day));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Body_c_1getRotationModelAtTime(JNIEnv *env,
                                                                               jclass clazz, jlong pointer,
                                                                               jdouble julian_day) {
    auto body = reinterpret_cast<Body *>(pointer);
    return reinterpret_cast<jlong>(body->getRotationModel(julian_day));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Body_c_1getPlanetarySystem(JNIEnv *env,
                                                                           jclass clazz, jlong pointer) {
    auto body = reinterpret_cast<Body *>(pointer);
    return reinterpret_cast<jlong>(body->getSystem());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Body_c_1getAlternateSurfaceNames(JNIEnv *env,
                                                                                 jclass clazz, jlong pointer) {
    auto body = reinterpret_cast<Body *>(pointer);
    auto altSurfaces = GetBodyFeaturesManager()->getAlternateSurfaceNames(body);
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
    auto body = reinterpret_cast<Body *>(pointer);
    return reinterpret_cast<jlong>(body->getTimeline());
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_Timeline_c_1getPhaseCount(JNIEnv *env, jclass clazz, jlong pointer) {
    auto timeline = reinterpret_cast<Timeline *>(pointer);
    return static_cast<jint>(timeline->phaseCount());
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Timeline_c_1getPhase(JNIEnv *env, jclass clazz, jint index,
                                                  jlong pointer) {
    auto timeline = reinterpret_cast<Timeline *>(pointer);
    return reinterpret_cast<jlong>(timeline->getPhase(static_cast<unsigned int>(index)).get());
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_Timeline_00024Phase_c_1getStartTime(JNIEnv *env, jclass clazz,
                                                                 jlong pointer) {
    auto phase = reinterpret_cast<TimelinePhase *>(pointer);
    return static_cast<jdouble>(phase->startTime());
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_Timeline_00024Phase_c_1getEndTime(JNIEnv *env, jclass clazz,
                                                               jlong pointer) {
    auto phase = reinterpret_cast<TimelinePhase *>(pointer);
    return static_cast<jdouble>(phase->endTime());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_Body_c_1canBeUsedAsCockpit(JNIEnv *env, jclass clazz, jlong pointer) {
    auto body = reinterpret_cast<Body *>(pointer);
    return GetBodyFeaturesManager()->canBeUsedAsCockpit(body) ? JNI_TRUE : JNI_FALSE;
}