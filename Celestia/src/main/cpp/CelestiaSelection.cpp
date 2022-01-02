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

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Selection_c_1destroy(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sel = (Selection *)pointer;
    delete sel;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Selection_c_1clone(JNIEnv *env, jclass clazz, jlong pointer) {
    return (jlong)new Selection(*(Selection *)pointer);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_Selection_c_1isEmpty(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sel = (Selection *)pointer;
    return (jboolean)(sel->empty() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_Selection_c_1getSelectionType(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sel = (Selection *)pointer;
    return sel->getType();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Selection_c_1getSelectionPtr(JNIEnv *env, jclass clazz, jlong pointer) {
    auto sel = (Selection *)pointer;
    switch (sel->getType())
    {
        case Selection::Type_Nil:
            return 0;
        case Selection::Type_Generic:
            return (jlong)sel->object();
        case Selection::Type_Body:
            return (jlong)sel->body();
        case Selection::Type_Star:
            return (jlong)sel->star();
        case Selection::Type_DeepSky:
            return (jlong)sel->deepsky();
        case Selection::Type_Location:
            return (jlong)sel->location();
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Selection_c_1createSelection(JNIEnv *env,
                                                                     jclass clazz,
                                                                     jint type,
                                                                     jlong pointer) {
    switch (type)
    {
        case Selection::Type_Body:
            return (jlong)new Selection((Body *)pointer);
        case Selection::Type_Star:
            return (jlong)new Selection((Star *)pointer);
        case Selection::Type_DeepSky:
            return (jlong)new Selection((DeepSkyObject *)pointer);
        case Selection::Type_Location:
            return (jlong)new Selection((Location *)pointer);
        case Selection::Type_Nil:
        case Selection::Type_Generic:
        default:
            return 0;
    }
}

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