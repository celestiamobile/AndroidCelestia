#include "CelestiaJNI.h"
#include <celengine/stardb.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaStarCatalog_c_1getStarName(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong pointer) {
    StarDatabase *d = (StarDatabase *)env->GetLongField(thiz, cscPtrFieldID);
    return env->NewStringUTF(d->getStarName(*(Star *)pointer, true).c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaStarCatalog_c_1getCount(JNIEnv *env, jobject thiz) {
    StarDatabase *d = (StarDatabase *)env->GetLongField(thiz, cscPtrFieldID);
    return d->size();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaStarCatalog_c_1getStar(JNIEnv *env, jobject thiz,
                                                                       jint index) {
    StarDatabase *d = (StarDatabase *)env->GetLongField(thiz, cscPtrFieldID);
    return (jlong)d->getStar(index);
}