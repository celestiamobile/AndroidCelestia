#include "CelestiaVector.h"

jobject createVectorForVector3d(JNIEnv *env, const Eigen::Vector3d &v)
{
    return env->NewObject(cvClz, cv3InitMethodID, (jdouble)v.x(), (jdouble)v.y(), (jdouble)v.z());
}

jobject createVectorForQuaterniond(JNIEnv *env, const Eigen::Quaterniond &v)
{
    return env->NewObject(cvClz, cv4InitMethodID, (jdouble)v.x(), (jdouble)v.y(), (jdouble)v.z(), (jdouble)v.w());
}

Eigen::Vector3d vector3dFromObject(JNIEnv *env, jobject thiz)
{
    double x = env->CallDoubleMethod(thiz, cvxMethodID);
    double y = env->CallDoubleMethod(thiz, cvyMethodID);
    double z = env->CallDoubleMethod(thiz, cvzMethodID);
    return Eigen::Vector3d(x, y, z);
}