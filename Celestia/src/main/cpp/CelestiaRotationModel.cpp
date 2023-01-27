/*
 * CelestiaRotationModel.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaVector.h"
#include <celephem/rotation.h>

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_RotationModel_c_1isPeriodic(JNIEnv *env, jclass clazz, jlong pointer) {
    auto p = (const celestia::ephem::RotationModel *)pointer;
    return (jboolean)(p->isPeriodic() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_RotationModel_c_1getPeriod(JNIEnv *env, jclass clazz, jlong pointer) {
    auto p = (const celestia::ephem::RotationModel *)pointer;
    return p->getPeriod();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_RotationModel_c_1getValidBeginTime(JNIEnv *env, jclass clazz, jlong pointer) {
    auto p = (const celestia::ephem::RotationModel *)pointer;
    double begin, end;
    p->getValidRange(begin, end);
    return begin;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_RotationModel_c_1getValidEndTime(JNIEnv *env, jclass clazz, jlong pointer) {
    auto p = (const celestia::ephem::RotationModel *)pointer;
    double begin, end;
    p->getValidRange(begin, end);
    return begin;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_RotationModel_c_1getAngularVelocityAtTime__D(
        JNIEnv *env, jclass clazz, jlong pointer, jdouble julian_day) {
    auto p = (const celestia::ephem::RotationModel *)pointer;
    const Eigen::Vector3d v = p->angularVelocityAtTime(julian_day);
    return createVectorForVector3d(env, v);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_RotationModel_c_1getEquatorOrientationAtTime(
        JNIEnv *env, jclass clazz, jlong pointer, jdouble julian_day) {
    auto p = (const celestia::ephem::RotationModel *)pointer;
    const Eigen::Quaterniond v = p->equatorOrientationAtTime(julian_day);
    return createVectorForQuaterniond(env, v);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_RotationModel_c_1getSpinAtTime(JNIEnv *env, jclass clazz, jlong pointer, jdouble julian_day) {
    auto p = (const celestia::ephem::RotationModel *)pointer;
    const Eigen::Quaterniond v = p->spin(julian_day);
    return createVectorForQuaterniond(env, v);
}