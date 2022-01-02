/*
 * CelestiaDestination.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaSelection.h"
#import <celestia/destination.h>
#import <celestia/celestiacore.h>

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_celestia_AppCore_c_1getDestinations(JNIEnv *env,
                                                                   jobject clazz,
                                                                   jlong pointer) {
    auto core = (CelestiaCore *)pointer;
    const DestinationList *destionations = core->getDestinations();
    int count = destionations ? destionations->size() : 0;
    jobject array = env->NewObject(alClz, aliMethodID, count);
    for (int i = 0; i < count; ++i)
    {
        Destination *destination = destionations->at(i);
        jobject javaDestination = env->NewObject(
                cdClz, cdInitMethodID,
                env->NewStringUTF(destination->name.c_str()),
                env->NewStringUTF(destination->target.c_str()),
                destination->distance,
                env->NewStringUTF(destination->description.c_str())
        );
        env->CallBooleanMethod(array, alaMethodID, javaDestination);
    }
    return array;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Simulation_c_1goToDestination(JNIEnv *env,
                                                           jclass clazz,
                                                           jlong pointer,
                                                           jstring target,
                                                           jdouble distance) {
    auto sim = (Simulation *)pointer;
    const char *str = env->GetStringUTFChars(target, nullptr);
    Selection sel = sim->findObjectFromPath(str);
    if (!sel.empty())
    {
        sim->follow();
        sim->setSelection(sel);
        if (distance <= 0)
        {
            // Use the default distance
            sim->gotoSelection(5.0,
                               Eigen::Vector3f::UnitY(),
                               ObserverFrame::ObserverLocal);
        }
        else
        {
            sim->gotoSelection(5.0,
                               distance,
                               Eigen::Vector3f::UnitY(),
                               ObserverFrame::ObserverLocal);
        }
    }
    env->ReleaseStringUTFChars(target, str);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Simulation_c_1goToLocation(JNIEnv *env,
                                                        jclass clazz,
                                                        jlong pointer,
                                                        jobject selection,
                                                        jdouble distance,
                                                        jdouble duration) {
    auto sim = (Simulation *)pointer;
    sim->setSelection(javaSelectionAsSelection(env, selection));
    sim->geosynchronousFollow();
    sim->gotoSelection(duration, distance, Eigen::Vector3f(0, 1, 0), ObserverFrame::ObserverLocal);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Simulation_c_1goToLocationLongLat(JNIEnv *env,
                                                               jclass clazz,
                                                               jlong pointer,
                                                               jobject selection,
                                                               jfloat longitude,
                                                               jfloat latitude,
                                                               jdouble distance,
                                                               jdouble duration) {
    auto sim = (Simulation *)pointer;
    sim->setSelection(javaSelectionAsSelection(env, selection));
    sim->geosynchronousFollow();
    sim->gotoSelectionLongLat(duration, distance, longitude * (float)M_PI / 180.0f, latitude * (float)M_PI / 180.0f, Eigen::Vector3f(0, 1, 0));
}

