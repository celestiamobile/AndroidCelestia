/*
 * CelestiaScript.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#import <celestia/scriptmenu.h>

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Script_c_1getScriptsInDirectory(JNIEnv *env,
                                                                                jclass clazz,
                                                                                jstring path,
                                                                                jboolean deep_scan) {
    const char *str = env->GetStringUTFChars(path, nullptr);
    auto results = ScanScriptsDirectory(str, deep_scan != JNI_FALSE);
    env->ReleaseStringUTFChars(path, str);

    jobject arrayObj = env->NewObject(alClz, aliMethodID, static_cast<jint>(results.size()));

    for (const auto& result : results)
    {
        jstring filename = env->NewStringUTF(result.filename.c_str());
        jstring title = env->NewStringUTF(result.title.c_str());
        jobject jscript = env->NewObject(cscriptClz, cscriptiMethodID, filename, title);
        env->CallBooleanMethod(arrayObj, alaMethodID, jscript);
        env->DeleteLocalRef(filename);
        env->DeleteLocalRef(title);
        env->DeleteLocalRef(jscript);
    }

    return arrayObj;
}