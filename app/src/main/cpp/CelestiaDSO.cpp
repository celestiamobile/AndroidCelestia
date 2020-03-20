#include "CelestiaJNI.h"
#include <celengine/deepskyobj.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaDSO_c_1getWebInfoURL(JNIEnv *env, jobject thiz) {
    DeepSkyObject *dso = (DeepSkyObject *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(dso->getInfoURL().c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaDSO_c_1getType(JNIEnv *env, jobject thiz) {
    DeepSkyObject *dso = (DeepSkyObject *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(dso->getType());
}