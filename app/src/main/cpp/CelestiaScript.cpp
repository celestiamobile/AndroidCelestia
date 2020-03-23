#include "CelestiaJNI.h"
#import <celestia/scriptmenu.h>

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaScript_c_1getScriptsInDirectory(JNIEnv *env,
                                                                                jclass clazz,
                                                                                jstring path,
                                                                                jboolean deep_scan) {
    const char *str = env->GetStringUTFChars(path, nullptr);
    std::vector<ScriptMenuItem> *results = ScanScriptsDirectory(str, deep_scan != JNI_FALSE);
    env->ReleaseStringUTFChars(path, str);

    jobject arrayObj = env->NewObject(alClz, aliMethodID, (int)results->size());

    for (int i = 0; i < results->size(); i++) {
        auto result = (*results)[i];

        jobject jscript = env->NewObject(cscriptClz, cscriptiMethodID,
                                         env->NewStringUTF(result.filename.c_str()),
                                         env->NewStringUTF(result.title.c_str())
        );
        env->CallBooleanMethod(arrayObj, alaMethodID, jscript);
    }

    delete results;
    return arrayObj;
}