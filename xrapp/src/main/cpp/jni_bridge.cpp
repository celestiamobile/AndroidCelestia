#include <jni.h>
#include <android/log.h>

#include "celestia_openxr.h"

// Singleton XR context shared across JNI calls
static CelestiaOpenXR* gXR = nullptr;

extern "C" {

JNIEXPORT void JNICALL
Java_space_celestia_celestiaxr_XRActivity_nativeInit(JNIEnv* env, jobject thiz) {
    LOGI("JNI: nativeInit");
    if (gXR == nullptr) {
        gXR = new CelestiaOpenXR();

        // Get the JavaVM so we can pass it to xrInitializeLoaderKHR
        JavaVM* vm = nullptr;
        env->GetJavaVM(&vm);

        // thiz is the XRActivity instance – keep a global ref so the VM doesn't
        // GC it before OpenXR is done with it
        jobject activityGlobal = env->NewGlobalRef(thiz);

        if (!gXR->init(vm, activityGlobal)) {
            LOGE("CelestiaOpenXR::init failed");
        }
    }
}

JNIEXPORT void JNICALL
Java_space_celestia_celestiaxr_XRActivity_nativeStart(JNIEnv* /*env*/, jobject /*thiz*/) {
    LOGI("JNI: nativeStart");
    if (gXR) gXR->start();
}


JNIEXPORT void JNICALL
Java_space_celestia_celestiaxr_XRActivity_nativeStop(JNIEnv* /*env*/, jobject /*thiz*/) {
    LOGI("JNI: nativeStop");
    if (gXR) gXR->stop();
}

JNIEXPORT void JNICALL
Java_space_celestia_celestiaxr_XRActivity_nativeDestroy(JNIEnv* /*env*/, jobject /*thiz*/) {
    LOGI("JNI: nativeDestroy");
    if (gXR) {
        gXR->destroy();
        delete gXR;
        gXR = nullptr;
    }
}

JNIEXPORT void JNICALL
Java_space_celestia_celestiaxr_XRActivity_nativeSetCorePointer(JNIEnv* env, jobject thiz, jlong corePtr) {
    if (gXR) {
        gXR->setCorePointer(reinterpret_cast<void*>(corePtr));
    }
}

} // extern "C"
