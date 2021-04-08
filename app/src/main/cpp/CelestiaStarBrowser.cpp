/*
 * CelestiaStarBrowser.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#include <celengine/starbrowser.h>

#define BROWSER_MAX_STAR_COUNT          100

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaStarBrowser_c_1destroy(JNIEnv *env, jclass clazz, jlong ptr) {
    auto browser = (StarBrowser *)ptr;
    delete browser;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaStarBrowser_c_1getStars(JNIEnv *env, jclass clazz, jlong ptr) {
    auto browser = (StarBrowser *)ptr;
    std::vector<const Star *> *stars = browser->listStars( BROWSER_MAX_STAR_COUNT );
    if (stars == nullptr)
        return env->NewObject(alClz, aliMethodID, 0);

    jobject arrayObject = env->NewObject(alClz, aliMethodID, (int)stars->size());
    for (int i = 0; i < stars->size(); i++) {
        Star *aStar = (Star *)(*stars)[i];
        jobject jstar = env->NewObject(csClz, csiMethodID, (jlong)aStar);
        env->CallBooleanMethod(arrayObject, alaMethodID, jstar);
    }

    delete stars;
    return arrayObject;
}