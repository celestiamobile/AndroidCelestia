/*
 * CelestiaEclipseFinder.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#include <json.hpp>
#include <celestia/eclipsefinder.h>

class EclipseSeacherWatcher: public EclipseFinderWatcher
{
public:
    EclipseSeacherWatcher(Body *body) : EclipseFinderWatcher(), aborted(false), finder(new EclipseFinder(body, this))
    {
    }

    Status eclipseFinderProgressUpdate(double t) override
    {
        return aborted ? AbortOperation : ContinueOperation;
    };

    ~EclipseSeacherWatcher() override
    {
        delete finder;
    }

    EclipseFinder *getFinder() { return finder; }
    void abort() { aborted = true; }
private:
    bool aborted;
    EclipseFinder *finder;
};

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_EclipseFinder_c_1createWithBody(JNIEnv *env,
                                                                        jclass clazz,
                                                                        jlong ptr) {
    return (long)new EclipseSeacherWatcher((Body *)ptr);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_EclipseFinder_c_1finalized(JNIEnv *env,
                                                                   jclass clazz,
                                                                   jlong ptr) {
    delete (EclipseSeacherWatcher *)ptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_EclipseFinder_c_1abort(JNIEnv *env, jclass clazz,
                                                               jlong ptr) {
    ((EclipseSeacherWatcher *)ptr)->abort();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_EclipseFinder_c_1search(JNIEnv *env, jclass clazz,
                                                                jlong ptr, jint kind,
                                                                jdouble start_time_julian,
                                                                jdouble end_time_julian) {
    EclipseFinder *finder = ((EclipseSeacherWatcher *)ptr)->getFinder();
    std::vector<Eclipse> results;
    finder->findEclipses(start_time_julian, end_time_julian, kind, results);

    using json = nlohmann::json;
    json j = json::array();
    for (auto & result : results) {
        json eclipse;
        eclipse["occulter"] = (jlong)result.occulter;
        eclipse["receiver"] = (jlong)result.receiver;
        eclipse["startTime"] = result.startTime;
        eclipse["endTime"] = result.endTime;
        j.push_back(eclipse);
    }
    return env->NewStringUTF(j.dump(-1, ' ', false, nlohmann::detail::error_handler_t::replace).c_str());
}