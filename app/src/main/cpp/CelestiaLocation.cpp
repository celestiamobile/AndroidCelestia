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
#include <celengine/location.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaLocation_c_1getName(JNIEnv *env, jobject thiz) {
    Location *location = (Location *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(location->getName(true).c_str());
}