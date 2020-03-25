#include "CelestiaVector.h"
#include <celengine/deepskyobj.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDSO_c_1getWebInfoURL(JNIEnv *env, jobject thiz) {
    DeepSkyObject *dso = (DeepSkyObject *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(dso->getInfoURL().c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDSO_c_1getType(JNIEnv *env, jobject thiz) {
    DeepSkyObject *dso = (DeepSkyObject *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(dso->getType());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDSO_c_1getPosition(JNIEnv *env, jobject thiz) {
    DeepSkyObject *dso = (DeepSkyObject *)env->GetLongField(thiz, caoPtrFieldID);
    return createVectorForVector3d(env, dso->getPosition());
}