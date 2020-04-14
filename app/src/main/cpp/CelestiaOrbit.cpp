/*
 * CelestiaOrbit.cpp
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
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
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1isPeriodic(JNIEnv *env, jobject thiz) {
    const Orbit *p = (const Orbit *)env->GetLongField(thiz, coPtrFieldID);
    return (jboolean)(p->isPeriodic() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1getPeriod(JNIEnv *env, jobject thiz) {
    const Orbit *p = (const Orbit *)env->GetLongField(thiz, coPtrFieldID);
    return p->getPeriod();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1getBoundingRadius(JNIEnv *env,
                                                                           jobject thiz) {
    const Orbit *p = (const Orbit *)env->GetLongField(thiz, coPtrFieldID);
    return p->getBoundingRadius();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1getValidBeginTime(JNIEnv *env,
                                                                           jobject thiz) {
    const Orbit *p = (const Orbit *)env->GetLongField(thiz, coPtrFieldID);
    double begin, end;
    p->getValidRange(begin, end);
    return begin;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1getValidEndTime(JNIEnv *env,
                                                                         jobject thiz) {
    const Orbit *p = (const Orbit *)env->GetLongField(thiz, coPtrFieldID);
    double begin, end;
    p->getValidRange(begin, end);
    return end;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1getVelocityAtTime(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jdouble julian_day) {
    const Orbit *p = (const Orbit *)env->GetLongField(thiz, coPtrFieldID);
    const Eigen::Vector3d v = p->velocityAtTime(julian_day);
    return createVectorForVector3d(env, v);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaOrbit_c_1getPositionAtTime(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jdouble julian_day) {
    const Orbit *p = (const Orbit *)env->GetLongField(thiz, coPtrFieldID);
    const Eigen::Vector3d v = p->positionAtTime(julian_day);
    return createVectorForVector3d(env, v);
}