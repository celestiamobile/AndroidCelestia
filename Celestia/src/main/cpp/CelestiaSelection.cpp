/*
 * CelestiaSelection.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaSelection.h"
#include <string>
#include <celengine/body.h>
#include <celengine/star.h>
#include <celengine/deepskyobj.h>
#include <celengine/location.h>

Selection javaSelectionAsSelection(JNIEnv *env, jobject javaSelection)
{
    jint type = env->CallIntMethod(javaSelection, selectionGetObjectTypeMethodID);
    jlong pointer = env->CallLongMethod(javaSelection, selectionGetObjectPointerMethodID);
    switch (static_cast<SelectionType>(type))
    {
        case SelectionType::Body:
            return { reinterpret_cast<Body*>(pointer) };
        case SelectionType::Star:
            return { reinterpret_cast<Star*>(pointer) };
        case SelectionType::DeepSky:
            return { reinterpret_cast<DeepSkyObject*>(pointer) };
        case SelectionType::Location:
            return { reinterpret_cast<Location*>(pointer) };
        case SelectionType::None:
        default:
            return {};
    }
}

jobject selectionAsJavaSelection(JNIEnv *env, Selection const& sel)
{
    void *pointer = nullptr;
    switch (sel.getType())
    {
        case SelectionType::Body:
            pointer = sel.body();
            break;
        case SelectionType::Star:
            pointer = sel.star();
            break;
        case SelectionType::DeepSky:
            pointer = sel.deepsky();
            break;
        case SelectionType::Location:
            pointer = sel.location();
            break;
        case SelectionType::None:
        default:
            break;
    }
    return env->NewObject(selectionClz, selectionInitMethodID, reinterpret_cast<jlong>(pointer), static_cast<jint>(sel.getType()));
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_Selection_c_1getRadius(JNIEnv *env, jobject thiz) {
    return static_cast<jdouble>(javaSelectionAsSelection(env, thiz).radius());
}