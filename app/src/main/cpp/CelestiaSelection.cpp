#include "CelestiaJNI.h"
#include <string>
#include <celengine/selection.h>
#include <celengine/body.h>
#include <celengine/star.h>
#include <celengine/deepskyobj.h>
#include <celengine/location.h>

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSelection_destroy(JNIEnv *env, jobject thiz) {
    Selection *sel = (Selection *)env->GetLongField(thiz, csePtrFieldID);
    delete sel;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSelection_c_1isEmpty(JNIEnv *env, jobject thiz) {
    Selection *sel = (Selection *)env->GetLongField(thiz, csePtrFieldID);
    return (jboolean)(sel->empty() ? JNI_TRUE : JNI_FALSE);
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSelection_c_1getSelectionType(JNIEnv *env,
                                                                              jobject thiz) {
    Selection *sel = (Selection *)env->GetLongField(thiz, csePtrFieldID);
    return sel->getType();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSelection_c_1getSelectionPtr(JNIEnv *env,
                                                                             jobject thiz) {
    Selection *sel = (Selection *)env->GetLongField(thiz, csePtrFieldID);
    switch (sel->getType())
    {
        case Selection::Type_Nil:
        case Selection::Type_Generic:
            return 0;
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
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSelection_c_1getName(JNIEnv *env, jobject thiz) {
    Selection *sel = (Selection *)env->GetLongField(thiz, csePtrFieldID);
    return env->NewStringUTF(sel->getName(true).c_str());
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSelection_c_1createSelection(JNIEnv *env,
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