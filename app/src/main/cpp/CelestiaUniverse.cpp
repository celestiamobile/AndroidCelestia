#include "CelestiaJNI.h"
#include <celengine/universe.h>

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaUniverse_c_1getStarCatalog(JNIEnv *env,
                                                                           jobject thiz) {
    Universe *u = (Universe *)env->GetLongField(thiz, cunPtrFieldID);
    return (jlong)u->getStarCatalog();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaUniverse_c_1getDSOCatalog(JNIEnv *env,
                                                                          jobject thiz) {
    Universe *u = (Universe *)env->GetLongField(thiz, cunPtrFieldID);
    return (jlong)u->getDSOCatalog();
}