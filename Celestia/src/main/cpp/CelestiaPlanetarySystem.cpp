// CelestiaPlanetarySystem.cpp
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

#include "CelestiaJNI.h"
#include <celengine/body.h>

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_PlanetarySystem_c_1getPrimaryObject(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong ptr) {
    return (jlong)((PlanetarySystem *)ptr)->getPrimaryBody();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_PlanetarySystem_c_1getStar(JNIEnv *env,
                                                                   jobject thiz,
                                                                   jlong ptr) {
    return (jlong)((PlanetarySystem *)ptr)->getStar();
}