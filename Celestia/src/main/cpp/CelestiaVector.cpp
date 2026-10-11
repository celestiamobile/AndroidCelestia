// CelestiaVector.cpp
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

#include "CelestiaVector.h"

jobject createVectorForVector3d(JNIEnv *env, const Eigen::Vector3d &v)
{
    return env->NewObject(cvClz, cv3InitMethodID, static_cast<jdouble>(v.x()), static_cast<jdouble>(v.y()), static_cast<jdouble>(v.z()));
}

jobject createVectorForQuaterniond(JNIEnv *env, const Eigen::Quaterniond &v)
{
    return env->NewObject(cvClz, cv4InitMethodID, static_cast<jdouble>(v.x()), static_cast<jdouble>(v.y()), static_cast<jdouble>(v.z()), static_cast<jdouble>(v.w()));
}

Eigen::Vector3d vector3dFromObject(JNIEnv *env, jobject thiz)
{
    double x = env->CallDoubleMethod(thiz, cvxMethodID);
    double y = env->CallDoubleMethod(thiz, cvyMethodID);
    double z = env->CallDoubleMethod(thiz, cvzMethodID);
    return Eigen::Vector3d(x, y, z);
}