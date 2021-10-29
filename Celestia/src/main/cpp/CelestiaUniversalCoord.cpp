/*
 * CelestiaUniversalCoord.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaVector.h"
#include <celengine/univcoord.h>

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_UniversalCoord_c_1destroy(JNIEnv *env, jclass clazz, jlong pointer) {
    auto ptr = (UniversalCoord *)pointer;
    delete ptr;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_UniversalCoord_c_1getZero(JNIEnv *env,
                                                                  jclass clazz) {
    return (jlong)new UniversalCoord(UniversalCoord::Zero());
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_UniversalCoord_c_1distanceFrom(JNIEnv *env,
                                                                       jclass clazz,
                                                                       jlong ptr1,
                                                                       jlong ptr2) {
    UniversalCoord *u1 = (UniversalCoord *)ptr1;
    UniversalCoord *u2 = (UniversalCoord *)ptr2;
    return u1->distanceFromKm(*u2);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_UniversalCoord_c_1differenceFrom(JNIEnv *env,
                                                                         jclass clazz,
                                                                         jlong ptr1,
                                                                         jlong ptr2) {
    UniversalCoord *u1 = (UniversalCoord *)ptr1;
    UniversalCoord *u2 = (UniversalCoord *)ptr2;
    return (jlong)new UniversalCoord(u1->difference(*u2));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_UniversalCoord_c_1offsetFrom(JNIEnv *env,
                                                                     jclass clazz,
                                                                     jlong ptr1,
                                                                     jlong ptr2) {
    UniversalCoord *u1 = (UniversalCoord *)ptr1;
    UniversalCoord *u2 = (UniversalCoord *)ptr2;
    Eigen::Vector3d offset = u1->offsetFromKm(*u2);
    return createVectorForVector3d(env, offset);
}