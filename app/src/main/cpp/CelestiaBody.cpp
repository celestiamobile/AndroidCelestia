/*
 * CelestiaBody.cpp
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
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
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getType(JNIEnv *env, jobject thiz) {
    Body *body = (Body *)env->GetLongField(thiz, caoPtrFieldID);
    return body->getClassification();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getName(JNIEnv *env, jobject thiz) {
    Body *body = (Body *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(body->getName(true).c_str());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1hasRings(JNIEnv *env, jobject thiz) {
    Body *body = (Body *)env->GetLongField(thiz, caoPtrFieldID);
    return (jboolean)(body->getRings() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1hasAtmosphere(JNIEnv *env, jobject thiz) {
    Body *body = (Body *)env->GetLongField(thiz, caoPtrFieldID);
    return (jboolean)(body->getAtmosphere() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1isEllipsoid(JNIEnv *env, jobject thiz) {
    Body *body = (Body *)env->GetLongField(thiz, caoPtrFieldID);
    return (jboolean)(body->isEllipsoid() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getRadius(JNIEnv *env, jobject thiz) {
    Body *body = (Body *)env->GetLongField(thiz, caoPtrFieldID);
    return body->getRadius();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getWebInfoURL(JNIEnv *env, jobject thiz) {
    Body *body = (Body *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(body->getInfoURL().c_str());
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getOrbitAtTime(JNIEnv *env, jobject thiz,
                                                                       jdouble julian_day) {
    Body *body = (Body *)env->GetLongField(thiz, caoPtrFieldID);
    return (jlong)(body->getOrbit(julian_day));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getRotationModelAtTime(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jdouble julian_day) {
    Body *body = (Body *)env->GetLongField(thiz, caoPtrFieldID);
    return (jlong)(body->getRotationModel(julian_day));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getPlanetarySystem(JNIEnv *env,
                                                                           jobject thiz) {
    Body *body = (Body *)env->GetLongField(thiz, caoPtrFieldID);
    return (jlong)(body->getSystem());
}