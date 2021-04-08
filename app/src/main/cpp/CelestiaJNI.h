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
#include <pthread.h>

extern "C" {
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

// universal destination
extern jclass cdClz;
extern jmethodID cdInitMethodID;

extern pthread_key_t javaEnvKey;
}

#endif //CELESTIA_JNI_H
