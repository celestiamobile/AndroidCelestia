#include "CelestiaJNI.h"
#include <celengine/dsodb.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaDSOCatalog_c_1getDSOName(JNIEnv *env, jobject thiz,
                                                                         jlong pointer) {
    DSODatabase *d = (DSODatabase *)env->GetLongField(thiz, cdcPtrFieldID);
    return env->NewStringUTF(d->getDSOName((DeepSkyObject *)pointer, true).c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaDSOCatalog_c_1getCount(JNIEnv *env, jobject thiz) {
    DSODatabase *d = (DSODatabase *)env->GetLongField(thiz, cdcPtrFieldID);
    return d->size();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaDSOCatalog_c_1getDSO(JNIEnv *env, jobject thiz,
                                                                     jint index) {
    DSODatabase *d = (DSODatabase *)env->GetLongField(thiz, cdcPtrFieldID);
    return (jlong)d->getDSO(index);
}