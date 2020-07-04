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
Java_space_celestia_mobilecelestia_core_CelestiaRotationModel_c_1isPeriodic(JNIEnv *env,
                                                                            jobject thiz) {
    const RotationModel *p = (const RotationModel *)env->GetLongField(thiz, crmPtrFieldID);
    return (jboolean)(p->isPeriodic() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaRotationModel_c_1getPeriod(JNIEnv *env,
                                                                           jobject thiz) {
    const RotationModel *p = (const RotationModel *)env->GetLongField(thiz, crmPtrFieldID);
    return p->getPeriod();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaRotationModel_c_1getValidBeginTime(JNIEnv *env,
                                                                                   jobject thiz) {
    const RotationModel *p = (const RotationModel *)env->GetLongField(thiz, crmPtrFieldID);
    double begin, end;
    p->getValidRange(begin, end);
    return begin;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaRotationModel_c_1getValidEndTime(JNIEnv *env,
                                                                                 jobject thiz) {
    const RotationModel *p = (const RotationModel *)env->GetLongField(thiz, crmPtrFieldID);
    double begin, end;
    p->getValidRange(begin, end);
    return begin;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaRotationModel_c_1getAngularVelocityAtTime__D(
        JNIEnv *env, jobject thiz, jdouble julian_day) {
    const RotationModel *p = (const RotationModel *)env->GetLongField(thiz, crmPtrFieldID);
    const Eigen::Vector3d v = p->angularVelocityAtTime(julian_day);
    return createVectorForVector3d(env, v);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaRotationModel_c_1getEquatorOrientationAtTime(
        JNIEnv *env, jobject thiz, jdouble julian_day) {
    const RotationModel *p = (const RotationModel *)env->GetLongField(thiz, crmPtrFieldID);
    const Eigen::Quaterniond v = p->equatorOrientationAtTime(julian_day);
    return createVectorForQuaterniond(env, v);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaRotationModel_c_1getSpinAtTime(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jdouble julian_day) {
    const RotationModel *p = (const RotationModel *)env->GetLongField(thiz, crmPtrFieldID);
    const Eigen::Quaterniond v = p->spin(julian_day);
    return createVectorForQuaterniond(env, v);
}