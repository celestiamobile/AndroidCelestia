#include "CelestiaJNI.h"
#include <celengine/body.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getName(JNIEnv *env, jobject thiz) {
    Body *body = (Body *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(body->getName(true).c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaBody_c_1getWebInfoURL(JNIEnv *env, jobject thiz) {
    Body *body = (Body *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(body->getInfoURL().c_str());
}