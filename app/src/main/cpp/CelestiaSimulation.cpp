#include "CelestiaJNI.h"
#include <celengine/simulation.h>

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaSimulation_c_1getSelection(JNIEnv *env,
                                                                           jobject thiz) {
    Simulation *sim = (Simulation *)env->GetLongField(thiz, csiPtrFieldID);
    Selection sel = sim->getSelection();
    return (jlong)new Selection(sel);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaSimulation_c_1setSelection(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong ptr) {
    Simulation *sim = (Simulation *)env->GetLongField(thiz, csiPtrFieldID);
    sim->setSelection(Selection(*(Selection *)ptr));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaSimulation_c_1getUniverse(JNIEnv *env,
                                                                          jobject thiz) {
    Simulation *sim = (Simulation *)env->GetLongField(thiz, csiPtrFieldID);
    return (jlong)sim->getUniverse();
}