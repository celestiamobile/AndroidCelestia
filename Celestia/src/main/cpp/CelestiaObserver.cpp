// CelestiaObserver.cpp
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

#include "CelestiaSelection.h"
#include <celengine/observer.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_Observer_c_1getDisplayedSurface(JNIEnv *env,
                                                                        jclass,
                                                                        jlong ptr) {
    return env->NewStringUTF(((Observer *)ptr)->getDisplayedSurface().c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Observer_c_1setDisplayedSurface(JNIEnv *env,
                                                                        jclass,
                                                                        jlong ptr,
                                                                        jstring displayed_surface) {
    const char *str = env->GetStringUTFChars(displayed_surface, nullptr);
    ((Observer *)ptr)->setDisplayedSurface(str);
    env->ReleaseStringUTFChars(displayed_surface, str);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Observer_c_1setFrame(JNIEnv *env, jclass, jlong ptr,
                                                  jint coordinate_system, jobject reference,
                                                  jobject target) {
    auto observer = reinterpret_cast<Observer *>(ptr);
    observer->setFrame(static_cast<ObserverFrame::CoordinateSystem>(coordinate_system), javaSelectionAsSelection(env, reference), javaSelectionAsSelection(env, target));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Observer_c_1rotate(JNIEnv *env, jclass clazz, jlong ptr,
                                                jfloatArray from, jfloatArray to) {
    auto observer = reinterpret_cast<Observer *>(ptr);
    float fromBuffer[4];
    float toBuffer[4];
    env->GetFloatArrayRegion(from, 0, 4, fromBuffer);
    env->GetFloatArrayRegion(to, 0, 4, toBuffer);
    Eigen::Quaternionf f(fromBuffer);
    Eigen::Quaternionf t(toBuffer);
    f.normalize();
    t.normalize();
    observer->rotate(t * f.conjugate());
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Observer_c_1applyQuaternion(JNIEnv *env, jclass clazz, jlong ptr,
                                                       jfloatArray current, jfloatArray previous) {
    float prevBuffer[4];
    float currBuffer[4];
    env->GetFloatArrayRegion(previous, 0, 4, prevBuffer);
    env->GetFloatArrayRegion(current, 0, 4, currBuffer);
    Eigen::Quaternionf prev(prevBuffer);
    Eigen::Quaternionf curr(currBuffer);
    auto observer = reinterpret_cast<Observer *>(ptr);
    observer->setOrientationTransform((curr * prev.inverse()).cast<double>() * observer->getOrientationTransform());
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Observer_c_1setCockpit(JNIEnv *env, jclass clazz, jlong ptr,
                                                    jobject selection) {
    auto observer = reinterpret_cast<Observer *>(ptr);
    observer->setCockpit(javaSelectionAsSelection(env, selection));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Observer_c_1getCockpit(JNIEnv *env, jclass clazz, jlong ptr) {
    auto observer = reinterpret_cast<Observer *>(ptr);
    return selectionAsJavaSelection(env, observer->getCockpit());
}