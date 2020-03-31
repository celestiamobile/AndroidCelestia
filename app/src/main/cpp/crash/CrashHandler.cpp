#include "client/linux/handler/exception_handler.h"
#include "client/linux/handler/minidump_descriptor.h"
#include <jni.h>
#include <android/log.h>

/*
 * Triggered automatically after an attempt to write a minidump file to the breakpad folder.
 */
static bool dumpCallback(const google_breakpad::MinidumpDescriptor &descriptor,
                  void *context,
                  bool succeeded) {

    // Allow system to log the native stack trace.
    __android_log_print(ANDROID_LOG_INFO, "CelestiaCrashReporter",
                        "Wrote breakpad minidump at %s succeeded=%d\n", descriptor.path(),
                        succeeded);
    return false;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_utils_CrashHandler_setupNativeCrashesListener(JNIEnv *env,
jclass clazz,
        jstring path) {
    const char *dumpPath = (char *) env->GetStringUTFChars(path, NULL);
    google_breakpad::MinidumpDescriptor descriptor(dumpPath);
    new google_breakpad::ExceptionHandler(descriptor, NULL, dumpCallback, NULL, true, -1);
    env->ReleaseStringUTFChars(path, dumpPath);
}