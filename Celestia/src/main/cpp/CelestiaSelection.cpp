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
    switch ((int)type)
    {
        case Selection::Type_Body:
            return Selection((Body *)pointer);
        case Selection::Type_Star:
            return Selection((Star *)pointer);
        case Selection::Type_DeepSky:
            return Selection((DeepSkyObject *)pointer);
        case Selection::Type_Location:
            return Selection((Location *)pointer);
        case Selection::Type_Generic:
            return Selection((AstroObject *)pointer);
        case Selection::Type_Nil:
        default:
            return Selection();
    }
}

jobject selectionAsJavaSelection(JNIEnv *env, Selection const& sel)
{
    return env->NewObject(selectionClz, selectionInitMethodID, (jlong)sel.object(), (jint)sel.getType());
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_Selection_c_1getRadius(JNIEnv *env, jobject thiz) {
    return (jdouble)javaSelectionAsSelection(env, thiz).radius();
}