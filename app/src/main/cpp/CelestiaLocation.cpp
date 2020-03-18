#include "CelestiaJNI.h"
#include <celengine/location.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaLocation_c_1getName(JNIEnv *env, jobject thiz) {
    Location *location = (Location *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(location->getName(true).c_str());
}