/*
 * CrashHandler.cpp
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include <jni.h>
#include <android/log.h>
#include <vector>
#include <dlfcn.h>

// THIS IS A WRAPPER AROUND SYSTEMFONT API
// THE FUNCTIONS IN THIS FILE SHOULD ONLY BE CALLED IN ANDROID Q OR LATER

// Handle to libandroid.so
static void *libHandle = nullptr;
// Functions needed so as to avoid include font headers
static void *(*AFontMatcher_create)() = nullptr;
static void (*AFontMatcher_setLocales)(void *, const char *) = nullptr;
static void (*AFontMatcher_setFamilyVariant)(void *, uint32_t) = nullptr;
static void (*AFontMatcher_setStyle)(void *, uint16_t, bool) = nullptr;
static void *(*AFontMatcher_match)(void *, const char *, uint16_t *, uint32_t, uint32_t *) = nullptr;
static void (*AFontMatcher_destroy)(void *) = nullptr;
static const char *(*AFont_getFontFilePath)(void *) = nullptr;
static size_t (*AFont_getCollectionIndex)(void *) = nullptr;
static void (*AFont_close)(void *) = nullptr;

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_utils_FontHelper_00024Matcher_c_1create(JNIEnv *env,
                                                                           jclass thiz) {
    if (!libHandle)
    {
        // Getting handle
        libHandle = dlopen("libandroid.so", RTLD_NOW);
        // Finding methods
        AFontMatcher_create = (void *(*)())dlsym(libHandle, "AFontMatcher_create");
        AFontMatcher_setLocales = (void (*)(void *, const char *))dlsym(libHandle, "AFontMatcher_setLocales");
        AFontMatcher_setFamilyVariant = (void (*)(void *, uint32_t))dlsym(libHandle, "AFontMatcher_setFamilyVariant");
        AFontMatcher_setStyle = (void (*)(void *, uint16_t, bool))dlsym(libHandle, "AFontMatcher_setStyle");
        AFontMatcher_match = (void *(*)(void *, const char *, uint16_t *, uint32_t, uint32_t *))dlsym(libHandle, "AFontMatcher_match");
        AFontMatcher_destroy = (void (*)(void *))dlsym(libHandle, "AFontMatcher_destroy");
        AFont_getFontFilePath = (const char *(*)(void *))dlsym(libHandle, "AFont_getFontFilePath");
        AFont_getCollectionIndex = (size_t (*)(void *))dlsym(libHandle, "AFont_getCollectionIndex");
        AFont_close = (void (*)(void *))dlsym(libHandle, "AFont_close");
    }
    return (jlong)AFontMatcher_create();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_utils_FontHelper_00024Matcher_c_1destroy(JNIEnv *env,
                                                                            jclass clazz,
                                                                            jlong ptr) {
    AFontMatcher_destroy((void *)ptr);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_utils_FontHelper_00024Font_c_1destroy(JNIEnv *env, jclass clazz,
                                                                         jlong ptr) {
    AFont_close((void *)ptr);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_utils_FontHelper_00024Font_c_1getFontFilePath(JNIEnv *env,
                                                                                 jclass clazz,
                                                                                 jlong ptr) {
    return env->NewStringUTF(AFont_getFontFilePath((void *)ptr));
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_utils_FontHelper_00024Font_c_1getCollectionIndex(JNIEnv *env,
                                                                                    jclass clazz,
                                                                                    jlong ptr) {
    return AFont_getCollectionIndex((void *)ptr);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_utils_FontHelper_00024Matcher_c_1setLocales(JNIEnv *env,
                                                                               jclass clazz,
                                                                               jlong ptr,
                                                                               jstring locales) {
    const char *str = env->GetStringUTFChars(locales, nullptr);
    AFontMatcher_setLocales((void *)ptr, str);
    env->ReleaseStringUTFChars(locales, str);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_utils_FontHelper_00024Matcher_c_1setFamilyVariant(JNIEnv *env,
                                                                                     jclass clazz,
                                                                                     jlong ptr,
                                                                                     jint family_variant) {
    AFontMatcher_setFamilyVariant((void *)ptr, family_variant);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_utils_FontHelper_00024Matcher_c_1setStyle(JNIEnv *env,
                                                                             jclass clazz,
                                                                             jlong ptr,
                                                                             jint weight,
                                                                             jboolean italic) {
    AFontMatcher_setStyle((void *)ptr, weight, italic);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_utils_FontHelper_00024Matcher_c_1match(JNIEnv *env, jclass clazz,
                                                                          jlong ptr,
                                                                          jstring family_name,
                                                                          jstring text) {
    const char *str1 = env->GetStringUTFChars(family_name, nullptr);
    const jchar *str2 = env->GetStringChars(text, nullptr);

    std::vector<uint16_t> utf16(str2, str2 + env->GetStringLength(text));

    void *resultFont = AFontMatcher_match((void *)ptr, str1, utf16.data(), utf16.size(), nullptr);

    env->ReleaseStringUTFChars(family_name, str1);
    env->ReleaseStringChars(text, str2);

    return (jlong)resultFont;
}