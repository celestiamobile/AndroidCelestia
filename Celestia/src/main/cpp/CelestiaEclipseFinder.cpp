// CelestiaEclipseFinder.cpp
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

#include "CelestiaJNI.h"
#include <json.hpp>
#include <celestia/eclipsefinder.h>
#include <memory>

class EclipseSeacherWatcher: public EclipseFinderWatcher
{
public:
    EclipseSeacherWatcher(Body *body) : EclipseFinderWatcher(), aborted(false), finder(std::make_unique<EclipseFinder>(body, this))
    {
    }

    Status eclipseFinderProgressUpdate(double t) override
    {
        return aborted ? Status::AbortOperation : Status::ContinueOperation;
    };

    EclipseFinder *getFinder() { return finder.get(); }
    void abort() { aborted = true; }
private:
    bool aborted;
    std::unique_ptr<EclipseFinder> finder;
};

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_EclipseFinder_c_1createWithBody(JNIEnv *env,
                                                                        jclass clazz,
                                                                        jlong ptr) {
    return toHandle(new EclipseSeacherWatcher(fromHandle<Body>(ptr)));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_EclipseFinder_c_1destroy(JNIEnv *env,
                                                      jclass clazz,
                                                      jlong ptr) {
    delete fromHandle<EclipseSeacherWatcher>(ptr);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_EclipseFinder_c_1abort(JNIEnv *env, jclass clazz,
                                                               jlong ptr) {
    fromHandle<EclipseSeacherWatcher>(ptr)->abort();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_EclipseFinder_c_1search(JNIEnv *env, jclass clazz,
                                                                jlong ptr, jint kind,
                                                                jdouble start_time_julian,
                                                                jdouble end_time_julian) {
    EclipseFinder *finder = fromHandle<EclipseSeacherWatcher>(ptr)->getFinder();
    std::vector<Eclipse> results;
    finder->findEclipses(start_time_julian, end_time_julian, static_cast<Eclipse::Type>(kind), results);

    using json = nlohmann::json;
    json j = json::array();
    for (const auto& result : results)
    {
        json eclipse;
        eclipse["occulter"] = toHandle(result.occulter);
        eclipse["receiver"] = toHandle(result.receiver);
        eclipse["startTime"] = result.startTime;
        eclipse["endTime"] = result.endTime;
        j.push_back(eclipse);
    }
    return env->NewStringUTF(j.dump(-1, ' ', false, nlohmann::detail::error_handler_t::replace).c_str());
}