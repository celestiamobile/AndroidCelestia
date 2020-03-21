#include "CelestiaJNI.h"
#include <celengine/astro.h>
#include <celengine/observer.h>


extern "C"
JNIEXPORT jintArray JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaUtils_getJulianDayComponents(JNIEnv *env,
                                                                             jclass clazz,
                                                                             jdouble julian_day) {
    astro::Date astroDate(julian_day);
    jint date[8] = { 0 };

    int year = astroDate.year;
    int era = 1;
    if (year < 1)
    {
        era  = 0;
        year = 1 - year;
    }

    date[0] = era;
    date[1] = year;
    date[2] = astroDate.month;
    date[3] = astroDate.day;
    date[4] = astroDate.hour;
    date[5] = astroDate.minute;
    date[6] = (int)floor(astroDate.seconds);
    date[7] = (int)((astroDate.seconds - date[6]) * 1000);
    jintArray array = env->NewIntArray(8);
    env->SetIntArrayRegion(array, 0, 8, date);
    return array;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaUtils_getJulianDay(JNIEnv *env, jclass clazz,
                                                                   jint era, jint year, jint month,
                                                                   jint day, jint hour, jint minute,
                                                                   jint second, jint millisecond) {
    if (era < 1) year = 1 - year;
    astro::Date astroDate(year, month, day);
    astroDate.hour    = hour;
    astroDate.minute  = minute;
    astroDate.seconds = second;

    astroDate.seconds += millisecond / (double)1000;

    double jd = astro::UTCtoTDB(astroDate);
    return jd;
}