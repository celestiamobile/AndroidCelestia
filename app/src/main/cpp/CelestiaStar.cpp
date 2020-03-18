#include "CelestiaJNI.h"
#include <celengine/star.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaStar_c_1getWebInfoURL(JNIEnv *env, jobject thiz) {
    Star *star = (Star *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(star->getInfoURL().c_str());
}