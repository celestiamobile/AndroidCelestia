/*
 * CelestiaObserver.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaSelection.h"
#include <celengine/observer.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_Observer_c_1getDisplayedSurface(JNIEnv *env,
                                                                        jclass clazz,
                                                                        jlong ptr) {
    return env->NewStringUTF(((Observer *)ptr)->getDisplayedSurface().c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Observer_c_1setDisplayedSurface(JNIEnv *env,
                                                                        jclass clazz,
                                                                        jlong ptr,
                                                                        jstring displayed_surface) {
    const char *str = env->GetStringUTFChars(displayed_surface, nullptr);
    ((Observer *)ptr)->setDisplayedSurface(str);
    env->ReleaseStringUTFChars(displayed_surface, str);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Observer_c_1setFrame(JNIEnv *env, jclass clazz, jlong ptr,
                                                  jint coordinate_system, jobject reference,
                                                  jobject target) {
    auto observer = reinterpret_cast<Observer *>(ptr);
    observer->setFrame(static_cast<ObserverFrame::CoordinateSystem>(coordinate_system), javaSelectionAsSelection(env, reference), javaSelectionAsSelection(env, target));
}