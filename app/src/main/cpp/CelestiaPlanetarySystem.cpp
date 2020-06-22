/*
 * CelestiaLocation.cpp
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#include <celengine/body.h>

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaPlanetarySystem_c_1getPrimaryObject(JNIEnv *env,
                                                                                    jobject thiz,
                                                                                    jlong ptr) {
    return (jlong)((PlanetarySystem *)ptr)->getPrimaryBody();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaPlanetarySystem_c_1getStar(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong ptr) {
    return (jlong)((PlanetarySystem *)ptr)->getStar();
}