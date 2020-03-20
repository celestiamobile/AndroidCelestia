#include "CelestiaJNI.h"
#include <celengine/starbrowser.h>

#define BROWSER_MAX_STAR_COUNT          100

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaStarBrowser_c_1destroy(JNIEnv *env, jobject thiz) {
    StarBrowser *browser = (StarBrowser *)env->GetLongField(thiz, csbPtrFieldID);
    delete browser;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaStarBrowser_c_1getStars(JNIEnv *env, jobject thiz) {
    StarBrowser *browser = (StarBrowser *)env->GetLongField(thiz, csbPtrFieldID);
    std::vector<const Star *> *stars = browser->listStars( BROWSER_MAX_STAR_COUNT );
    if (stars == nullptr)
        return env->NewObject(alClz, aliMethodID, 0);

    jobject arrayObject = env->NewObject(alClz, aliMethodID, (int)stars->size());
    for (int i = 0; i < stars->size(); i++) {
        Star *aStar = (Star *)(*stars)[i];
        jobject jstar = env->NewObject(csClz, csiMethodID, (jlong)aStar);
        env->CallBooleanMethod(arrayObject, alaMethodID, jstar);
    }

    delete stars;
    return arrayObject;
}