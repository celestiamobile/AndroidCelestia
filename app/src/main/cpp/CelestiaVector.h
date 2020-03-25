#include "CelestiaJNI.h"
#include <Eigen/Geometry>

#ifndef CELESTIA_VECTOR_H
#define CELESTIA_VECTOR_H

jobject createVectorForVector3d(JNIEnv *env, const Eigen::Vector3d &v);
jobject createVectorForQuaterniond(JNIEnv *env, const Eigen::Quaterniond &v);

Eigen::Vector3d vector3dFromObject(JNIEnv *env, jobject thiz);

#endif
