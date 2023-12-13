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
Java_space_celestia_celestia_StarBrowser_c_1destroy(JNIEnv *env, jclass clazz, jlong ptr) {
    auto browser = reinterpret_cast<celestia::engine::StarBrowser *>(ptr);
    delete browser;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_StarBrowser_c_1getStars(JNIEnv *env, jclass clazz, jlong ptr) {
    auto browser = reinterpret_cast<celestia::engine::StarBrowser *>(ptr);
    std::vector<celestia::engine::StarBrowserRecord> records;
    browser->populate(records);
    if (records.empty())
        return env->NewObject(alClz, aliMethodID, 0);

    jobject arrayObject = env->NewObject(alClz, aliMethodID, static_cast<jint>(records.size()));
    for (const auto &record : records)
     {
        jobject jstar = env->NewObject(csClz, csiMethodID, reinterpret_cast<jlong>(record.star));
        env->CallBooleanMethod(arrayObject, alaMethodID, jstar);
        env->DeleteLocalRef(jstar);
    }

    return arrayObject;
}