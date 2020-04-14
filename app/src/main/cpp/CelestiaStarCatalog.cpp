/*
 * CelestiaStarCatalog.cpp
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#include <celengine/stardb.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaStarCatalog_c_1getStarName(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong pointer) {
    StarDatabase *d = (StarDatabase *)env->GetLongField(thiz, cscPtrFieldID);
    return env->NewStringUTF(d->getStarName(*(Star *)pointer, true).c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaStarCatalog_c_1getCount(JNIEnv *env, jobject thiz) {
    StarDatabase *d = (StarDatabase *)env->GetLongField(thiz, cscPtrFieldID);
    return d->size();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaStarCatalog_c_1getStar(JNIEnv *env, jobject thiz,
                                                                       jint index) {
    StarDatabase *d = (StarDatabase *)env->GetLongField(thiz, cscPtrFieldID);
    return (jlong)d->getStar(index);
}