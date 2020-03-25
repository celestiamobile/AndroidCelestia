#include "CelestiaJNI.h"
#include <celengine/astro.h>

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDMS_c_1getDegrees(JNIEnv *env, jobject thiz,
                                                                  jdouble decimal) {
    int degrees, minutes;
    double seconds;
    astro::decimalToDegMinSec(decimal, degrees, minutes, seconds);
    return degrees;
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDMS_c_1getMinutes(JNIEnv *env, jobject thiz,
                                                                  jdouble decimal) {
    int degrees, minutes;
    double seconds;
    astro::decimalToDegMinSec(decimal, degrees, minutes, seconds);
    return minutes;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDMS_c_1getSeconds(JNIEnv *env, jobject thiz,
                                                                  jdouble decimal) {
    int degrees, minutes;
    double seconds;
    astro::decimalToDegMinSec(decimal, degrees, minutes, seconds);
    return seconds;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaDMS_c_1getDecimal(JNIEnv *env, jobject thiz,
                                                                  jint degrees, jint minutes,
                                                                  jdouble seconds) {
    return astro::degMinSecToDecimal(degrees, minutes, seconds);
}