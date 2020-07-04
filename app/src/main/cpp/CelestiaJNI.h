/*
 * CelestiaJNI.h
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#ifndef CELESTIA_JNI_H
#define CELESTIA_JNI_H

#include <jni.h>

extern "C" {
extern jclass cacClz;
extern jfieldID cacPtrFieldID;
extern jclass csiClz;
extern jfieldID csiPtrFieldID;
extern jclass cseClz;
extern jfieldID csePtrFieldID;
extern jclass caoClz;
extern jfieldID caoPtrFieldID;
extern jclass cunClz;
extern jfieldID cunPtrFieldID;

extern jclass cscClz;
extern jfieldID cscPtrFieldID;
extern jclass cdcClz;
extern jfieldID cdcPtrFieldID;

extern jclass csbClz;
extern jfieldID csbPtrFieldID;

extern jclass cbClz;
extern jmethodID cbiMethodID;
extern jclass clClz;
extern jmethodID cliMethodID;
extern jclass csClz;
extern jmethodID csiMethodID;

extern jclass alClz;
extern jmethodID aliMethodID;
extern jmethodID alaMethodID;

extern jclass hmClz;
extern jmethodID hmiMethodID;
extern jmethodID hmpMethodID;

extern jclass cscriptClz;
extern jmethodID cscriptiMethodID;

// vector
extern jclass cvClz;
extern jmethodID cv3InitMethodID;
extern jmethodID cv4InitMethodID;
extern jmethodID cvxMethodID;
extern jmethodID cvyMethodID;
extern jmethodID cvzMethodID;
extern jmethodID cvwMethodID;

// orbit
extern jclass coClz;
extern jfieldID coPtrFieldID;

// rotation model
extern jclass crmClz;
extern jfieldID crmPtrFieldID;

// universal coord
extern jclass cucClz;
extern jfieldID cucPtrFieldID;
}

#endif //CELESTIA_JNI_H
