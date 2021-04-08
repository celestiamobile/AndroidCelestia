/*
 * CelestiaOrbit.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaVector.h"
#include <celephem/orbit.h>

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1isPeriodic(JNIEnv *env, jclass clazz, jlong pointer) {
    auto p = (const Orbit *)pointer;
    return (jboolean)(p->isPeriodic() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1getPeriod(JNIEnv *env, jclass clazz, jlong pointer) {
    auto p = (const Orbit *)pointer;
    return p->getPeriod();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1getBoundingRadius(JNIEnv *env, jclass clazz, jlong pointer) {
    auto p = (const Orbit *)pointer;
    return p->getBoundingRadius();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1getValidBeginTime(JNIEnv *env, jclass clazz, jlong pointer) {
    auto p = (const Orbit *)pointer;
    double begin, end;
    p->getValidRange(begin, end);
    return begin;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1getValidEndTime(JNIEnv *env, jclass clazz, jlong pointer) {
    auto p = (const Orbit *)pointer;
    double begin, end;
    p->getValidRange(begin, end);
    return end;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1getVelocityAtTime(JNIEnv *env, jclass clazz, jlong pointer, jdouble julian_day) {
    auto p = (const Orbit *)pointer;
    const Eigen::Vector3d v = p->velocityAtTime(julian_day);
    return createVectorForVector3d(env, v);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1getPositionAtTime(JNIEnv *env, jclass clazz, jlong pointer, jdouble julian_day) {
    auto p = (const Orbit *)pointer;
    const Eigen::Vector3d v = p->positionAtTime(julian_day);
    return createVectorForVector3d(env, v);
}