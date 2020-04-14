/*
 * CelestiaDSOCatalog.cpp
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#include <celengine/dsodb.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDSOCatalog_c_1getDSOName(JNIEnv *env, jobject thiz,
                                                                         jlong pointer) {
    DSODatabase *d = (DSODatabase *)env->GetLongField(thiz, cdcPtrFieldID);
    return env->NewStringUTF(d->getDSOName((DeepSkyObject *)pointer, true).c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDSOCatalog_c_1getCount(JNIEnv *env, jobject thiz) {
    DSODatabase *d = (DSODatabase *)env->GetLongField(thiz, cdcPtrFieldID);
    return d->size();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDSOCatalog_c_1getDSO(JNIEnv *env, jobject thiz,
                                                                     jint index) {
    DSODatabase *d = (DSODatabase *)env->GetLongField(thiz, cdcPtrFieldID);
    return (jlong)d->getDSO(index);
}