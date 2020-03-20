#ifndef CELESTIA_CELESTIAJNI_H
#define CELESTIA_CELESTIAJNI_H

#include <jni.h>

extern "C" {
extern jclass cacClz;
extern jfieldID cacPtrFieldID;
extern jclass csiClz;
extern jfieldID csiPtrFieldID;
extern jclass cseClz;
extern jfieldID csePtrFieldID;
extern jclass caoClz;
extern jfieldID caoPtrFieldID;
extern jclass cunClz;
extern jfieldID cunPtrFieldID;

extern jclass cscClz;
extern jfieldID cscPtrFieldID;
extern jclass cdcClz;
extern jfieldID cdcPtrFieldID;

extern jclass csbClz;
extern jfieldID csbPtrFieldID;

extern jclass cbiClz;
extern jmethodID cbii1MethodID;
extern jmethodID cbii2MethodID;

extern jclass cbClz;
extern jmethodID cbiMethodID;
extern jclass clClz;
extern jmethodID cliMethodID;
extern jclass csClz;
extern jmethodID csiMethodID;

extern jclass alClz;
extern jmethodID aliMethodID;
extern jmethodID alaMethodID;

extern jclass hmClz;
extern jmethodID hmiMethodID;
extern jmethodID hmpMethodID;

extern jclass cscriptClz;
extern jmethodID cscriptiMethodID;
}

#endif //CELESTIA_CELESTIAJNI_H
