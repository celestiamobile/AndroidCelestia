// CelestiaUtils.cpp
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

#include "CelestiaVector.h"
#include <celastro/date.h>
#include <celengine/observer.h>

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_Utils_getJulianDay(JNIEnv *env, jclass clazz,
                                                jlong milli_seconds_from_epoch) {
    static auto epoch = celestia::astro::Date(1970, 1, 1);
    return celestia::astro::UTCtoTDB(epoch + static_cast<double>(milli_seconds_from_epoch) / 86400.0 / 1000.0);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Utils_celToJ2000Ecliptic(JNIEnv *env, jclass clazz,
                                                                 jobject cel) {
    Eigen::Vector3d p = vector3dFromObject(env, cel);
    return createVectorForVector3d(env, Eigen::Vector3d(p.x(), -p.z(), p.y()));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Utils_eclipticToEquatorial(JNIEnv *env,
                                                                   jclass clazz,
                                                                   jobject ecliptic) {
    Eigen::Vector3d p = vector3dFromObject(env, ecliptic);
    return createVectorForVector3d(env, celestia::astro::eclipticToEquatorial(p));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Utils_equatorialToGalactic(JNIEnv *env,
                                                                   jclass clazz,
                                                                   jobject equatorial) {
    Eigen::Vector3d p = vector3dFromObject(env, equatorial);
    return createVectorForVector3d(env, celestia::astro::equatorialToGalactic(p));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Utils_rectToSpherical(JNIEnv *env, jclass clazz,
                                                              jobject rect) {
    Eigen::Vector3d v = vector3dFromObject(env, rect);
    double r = v.norm();
    double theta = atan2(v.y(), v.x());
    if (theta < 0)
        theta = theta + 2 * celestia::numbers::pi;
    double phi = asin(v.z() / r);

    return createVectorForVector3d(env, Eigen::Vector3d(theta, phi, r));
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_Utils_AUToKilometers(JNIEnv *env, jclass clazz, jdouble au) {
    return celestia::astro::AUtoKilometers(au);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Utils_getMilliSecondsFromEpochFromJulianDay(JNIEnv *env, jclass clazz,
                                                                         jdouble julian_day) {
    static auto epoch = celestia::astro::Date(1970, 1, 1);
    return static_cast<jlong>((celestia::astro::TDBtoUTC(julian_day) - epoch) * 86400.0 * 1000.0);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_Utils_degFromRad(JNIEnv *env, jclass clazz, jdouble rad) {
    return static_cast<jdouble>(celestia::math::radToDeg(static_cast<double>(rad)));
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_space_celestia_celestia_Utils_transformQuaternion(JNIEnv *env, jclass clazz, jfloatArray q,
                                                       jfloat angle_z) {
    if (angle_z == 0.0f)
        return q;
    float buffer[4];
    env->GetFloatArrayRegion(q, 0, 4, buffer);
    Eigen::Quaternionf quaternion(buffer);
    Eigen::Quaternionf zRotation(Eigen::AngleAxisf(angle_z, Eigen::Vector3f::UnitZ()));
    Eigen::Quaternionf transformed = zRotation * quaternion;
    jfloatArray result = env->NewFloatArray(4);
    float out[4] = { transformed.x(), transformed.y(), transformed.z(), transformed.w() };
    env->SetFloatArrayRegion(result, 0, 4, out);
    return result;
}