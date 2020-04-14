/*
 * CelestiaVector.h
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#include <Eigen/Geometry>

#ifndef CELESTIA_VECTOR_H
#define CELESTIA_VECTOR_H

jobject createVectorForVector3d(JNIEnv *env, const Eigen::Vector3d &v);
jobject createVectorForQuaterniond(JNIEnv *env, const Eigen::Quaterniond &v);

Eigen::Vector3d vector3dFromObject(JNIEnv *env, jobject thiz);

#endif
