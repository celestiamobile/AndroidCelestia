/*
 * CelestiaVector.h
 *
 * Copyright (C) 2001-2022, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#pragma once

#include "CelestiaJNI.h"
#include <celengine/selection.h>

Selection javaSelectionAsSelection(JNIEnv *env, jobject javaSelection);
jobject selectionAsJavaSelection(JNIEnv *env, Selection const& sel);
