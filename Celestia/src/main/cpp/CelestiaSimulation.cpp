// CelestiaSimulation.cpp
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

#include "CelestiaSelection.h"
#include <celengine/body.h>
#include <celengine/simulation.h>
#include <celengine/selection.h>
#include <celmath/geomutil.h>
#include <celmath/intersect.h>
#include <celmath/sphere.h>

namespace
{

Eigen::Vector3d findMaxEclipsePoint(const Eigen::Vector3d& toOcculter,
                                    const Eigen::Vector3d& toReceiver,
                                    double receiverRadius)
{
    double distance = 0.0;
    if (celestia::math::testIntersection(
            Eigen::ParametrizedLine<double, 3>(Eigen::Vector3d::Zero(), toOcculter),
            celestia::math::Sphered(toReceiver, receiverRadius),
            distance))
    {
        return toOcculter * distance - toReceiver;
    }

    const double t = toReceiver.dot(toOcculter) / toOcculter.squaredNorm();
    Eigen::Vector3d point = t * toOcculter - toReceiver;
    return point * (receiverRadius / point.norm());
}

}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Simulation_c_1getSelection(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sim = fromHandle<Simulation>(pointer);
    Selection sel = sim->getSelection();
    return selectionAsJavaSelection(env, sel);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Simulation_c_1setSelection(JNIEnv *env, jclass clazz, jlong pointer,
                                                        jobject selection) {
    auto sim = fromHandle<Simulation>(pointer);
    sim->setSelection(javaSelectionAsSelection(env, selection));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Simulation_c_1getUniverse(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sim = fromHandle<Simulation>(pointer);
    return toHandle(sim->getUniverse());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Simulation_c_1completionForText(JNIEnv *env, jclass clazz, jlong pointer, jstring text, jint limit) {
    auto sim = fromHandle<Simulation>(pointer);
    const char *str = env->GetStringUTFChars(text, nullptr);
    std::vector<celestia::engine::Completion> results;
    sim->getObjectCompletion(results, str, true);
    env->ReleaseStringUTFChars(text, str);
    jobject arrayObject = env->NewObject(alClz, aliMethodID, static_cast<int>(results.size()));
    int count = 0;
    for (const auto& result : results) {
        if (count > limit)
            break;

        auto selection = selectionAsJavaSelection(env, result.getSelection());
        auto name = env->NewStringUTF(result.getName().c_str());
        auto completion = env->NewObject(completionClz, completionInitMethodID, name, selection);
        env->DeleteLocalRef(selection);
        env->DeleteLocalRef(name);
        env->CallBooleanMethod(arrayObject, alaMethodID, completion);
        env->DeleteLocalRef(completion);
        count += 1;
    }
    return arrayObject;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Simulation_c_1findObject(JNIEnv *env, jclass clazz, jlong pointer, jstring name) {
    auto sim = fromHandle<Simulation>(pointer);
    const char *str = env->GetStringUTFChars(name, nullptr);
    auto sel = sim->findObjectFromPath(str, true);
    env->ReleaseStringUTFChars(name, str);
    return selectionAsJavaSelection(env, sel);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Simulation_c_1reverseObserverOrientation(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sim = fromHandle<Simulation>(pointer);
    sim->reverseObserverOrientation();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_Simulation_c_1getTime(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sim = fromHandle<Simulation>(pointer);
    return sim->getTime();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Simulation_c_1setTime(JNIEnv *env, jclass clazz, jlong pointer, jdouble time) {
    auto sim = fromHandle<Simulation>(pointer);
    sim->setTime(time);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Simulation_c_1performEclipseAction(
        JNIEnv *env, jclass clazz, jlong pointer, jdouble startTime, jdouble endTime,
        jlong occulterPointer, jlong receiverPointer, jint action) {
    auto sim = fromHandle<Simulation>(pointer);
    auto occulter = fromHandle<Body>(occulterPointer);
    auto receiver = fromHandle<Body>(receiverPointer);
    auto sun = receiver->getSystem()->getStar();
    if (sun == nullptr)
        return;

    const double midEclipseTime = (startTime + endTime) / 2.0;
    if (action == 0) {
        sim->setTime(midEclipseTime);
        return;
    }

    double now = sim->getTime();
    if (now < startTime || now > endTime)
        sim->setTime(midEclipseTime);
    now = sim->getTime();

    const Eigen::Vector3d toOcculter = occulter->getPosition(now).offsetFromKm(sun->getPosition(now));
    const Eigen::Vector3d toReceiver = receiver->getPosition(now).offsetFromKm(sun->getPosition(now));
    const Eigen::Vector3d receiverUp = receiver->getEclipticToBodyFixed(now).conjugate() * Eigen::Vector3d::UnitY();

    Body *frameBody = receiver;
    Eigen::Vector3d position;
    Eigen::Quaterniond orientation;

    switch (action) {
    case 1: {
        const Eigen::Vector3d eclipsePoint = findMaxEclipsePoint(toOcculter, toReceiver, receiver->getRadius());
        position = eclipsePoint * 4.0;
        orientation = celestia::math::LookAt<double>(position, eclipsePoint, receiverUp);
        break;
    }
    case 2: {
        const Eigen::Vector3d eclipsePoint = findMaxEclipsePoint(toOcculter, toReceiver, receiver->getRadius());
        position = eclipsePoint * 1.0001;
        orientation = celestia::math::LookAt<double>(eclipsePoint, -toReceiver, eclipsePoint.normalized());
        break;
    }
    case 3:
    case 4:
        frameBody = occulter;
        position = toOcculter.normalized() * occulter->getRadius() * (action == 3 ? 1.0001 : 20.0);
        orientation = celestia::math::LookAt<double>(position, toReceiver, receiverUp);
        break;
    default:
        return;
    }

    sim->setFrame(ObserverFrame::CoordinateSystem::Ecliptical, frameBody);
    sim->gotoLocation(UniversalCoord::Zero().offsetKm(position), orientation, 5.0);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Simulation_c_1getActiveObserver(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sim = fromHandle<Simulation>(pointer);
    return toHandle(sim->getActiveObserver());
}