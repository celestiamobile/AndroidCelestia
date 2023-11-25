/*
 * CelestiaSimulation.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaSelection.h"
#include <celengine/simulation.h>
#include <celengine/selection.h>
#include <celengine/starbrowser.h>
#include <celmath/geomutil.h>

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Simulation_c_1getSelection(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sim = reinterpret_cast<Simulation *>(pointer);
    Selection sel = sim->getSelection();
    return selectionAsJavaSelection(env, sel);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Simulation_c_1setSelection(JNIEnv *env, jclass clazz, jlong pointer,
                                                        jobject selection) {
    auto sim = reinterpret_cast<Simulation *>(pointer);
    sim->setSelection(javaSelectionAsSelection(env, selection));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Simulation_c_1getUniverse(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sim = reinterpret_cast<Simulation *>(pointer);
    return (jlong)sim->getUniverse();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Simulation_c_1completionForText(JNIEnv *env, jclass clazz, jlong pointer, jstring text, jint limit) {
    auto sim = reinterpret_cast<Simulation *>(pointer);
    const char *str = env->GetStringUTFChars(text, nullptr);
    std::vector<std::string> results;
    sim->getObjectCompletion(results, str, true);
    env->ReleaseStringUTFChars(text, str);
    jobject arrayObject = env->NewObject(alClz, aliMethodID, (int)results.size());
    int count = 0;
    for (const auto& result : results) {
        if (count > limit)
            break;
        jstring resultString = env->NewStringUTF(result.c_str());
        env->CallBooleanMethod(arrayObject, alaMethodID, resultString);
        env->DeleteLocalRef(resultString);
        count += 1;
    }
    return arrayObject;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_Simulation_c_1findObject(JNIEnv *env, jclass clazz, jlong pointer, jstring name) {
    auto sim = reinterpret_cast<Simulation *>(pointer);
    const char *str = env->GetStringUTFChars(name, nullptr);
    auto sel = sim->findObjectFromPath(str, true);
    env->ReleaseStringUTFChars(name, str);
    return selectionAsJavaSelection(env, sel);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Simulation_c_1getStarBrowser(JNIEnv *env, jclass clazz, jlong pointer, jint kind) {
    auto sim = reinterpret_cast<Simulation *>(pointer);
    return (jlong)new StarBrowser(sim, (int)kind);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Simulation_c_1reverseObserverOrientation(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sim = reinterpret_cast<Simulation *>(pointer);
    sim->reverseObserverOrientation();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_Simulation_c_1getTime(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sim = reinterpret_cast<Simulation *>(pointer);
    return sim->getTime();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Simulation_c_1setTime(JNIEnv *env, jclass clazz, jlong pointer, jdouble time) {
    auto sim = reinterpret_cast<Simulation *>(pointer);
    sim->setTime(time);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Simulation_c_1goToEclipse(JNIEnv *env, jclass clazz, jlong pointer, jdouble time, jobject ref, jobject target) {
    using namespace celestia::math;
    auto refSel = javaSelectionAsSelection(env, ref);
    auto targetSel = javaSelectionAsSelection(env, target);
    auto sim = reinterpret_cast<Simulation *>(pointer);
    sim->setTime(time);
    sim->setFrame(ObserverFrame::PhaseLock, targetSel, refSel);
    sim->update(0);
    double distance = targetSel.radius() * 4.0;
    sim->gotoLocation(UniversalCoord::Zero().offsetKm(Eigen::Vector3d::UnitX() * distance),
                      YRotation(-0.5 * celestia::numbers::pi) * XRotation(-0.5 * celestia::numbers::pi),
                      2.5);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Simulation_c_1getActiveObserver(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sim = reinterpret_cast<Simulation *>(pointer);
    return (jlong)sim->getActiveObserver();
}