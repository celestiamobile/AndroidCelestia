#include "CelestiaJNI.h"
#include <string>

#include <unistd.h>

#include <celestia/celestiacore.h>
#include <celengine/gl.h>

jclass cacClz = nullptr;
jfieldID cacPtrFieldID = nullptr;
jclass csiClz = nullptr;
jfieldID csiPtrFieldID = nullptr;
jclass cseClz = nullptr;
jfieldID csePtrFieldID = nullptr;
jclass caoClz = nullptr;
jfieldID caoPtrFieldID = nullptr;
jclass cunClz = nullptr;
jfieldID cunPtrFieldID = nullptr;

jclass cscClz = nullptr;
jfieldID cscPtrFieldID = nullptr;
jclass cdcClz = nullptr;
jfieldID cdcPtrFieldID = nullptr;

jclass csbClz = nullptr;
jfieldID csbPtrFieldID = nullptr;

jclass cbiClz = nullptr;
jmethodID cbii1MethodID = nullptr;
jmethodID cbii2MethodID = nullptr;

jclass cbClz = nullptr;
jmethodID cbiMethodID = nullptr;
jclass clClz = nullptr;
jmethodID cliMethodID = nullptr;
jclass csClz = nullptr;
jmethodID csiMethodID = nullptr;

jclass alClz = nullptr;
jmethodID aliMethodID = nullptr;
jmethodID alaMethodID = nullptr;

jclass hmClz = nullptr;
jmethodID hmiMethodID = nullptr;
jmethodID hmpMethodID = nullptr;

jclass cscriptClz = nullptr;
jmethodID cscriptiMethodID = nullptr;

extern "C" {
jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK)
        return JNI_ERR;

    // Find some commonly used classes and fields
    jclass cac = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaAppCore");
    cacClz = (jclass)env->NewGlobalRef(cac);
    cacPtrFieldID = env->GetFieldID(cacClz, "pointer", "J");

    jclass csi = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaSimulation");
    csiClz = (jclass)env->NewGlobalRef(csi);
    csiPtrFieldID = env->GetFieldID(csiClz, "pointer", "J");

    jclass cse = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaSelection");
    cseClz = (jclass)env->NewGlobalRef(cse);
    csePtrFieldID = env->GetFieldID(cseClz, "pointer", "J");

    jclass cao = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaAstroObject");
    caoClz = (jclass)env->NewGlobalRef(cao);
    caoPtrFieldID = env->GetFieldID(caoClz, "pointer", "J");

    jclass cun = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaUniverse");
    cunClz = (jclass)env->NewGlobalRef(cun);
    cunPtrFieldID = env->GetFieldID(cunClz, "pointer", "J");

    jclass csc = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaStarCatalog");
    cscClz = (jclass)env->NewGlobalRef(csc);
    cscPtrFieldID = env->GetFieldID(cscClz, "pointer", "J");

    jclass cdc = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaDSOCatalog");
    cdcClz = (jclass)env->NewGlobalRef(cdc);
    cdcPtrFieldID = env->GetFieldID(cdcClz, "pointer", "J");

    jclass cb = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaBody");
    cbClz = (jclass)env->NewGlobalRef(cb);
    cbiMethodID = env->GetMethodID(cbClz, "<init>", "(J)V");

    jclass cl = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaLocation");
    clClz = (jclass)env->NewGlobalRef(cl);
    cliMethodID = env->GetMethodID(clClz, "<init>", "(J)V");

    jclass cs = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaStar");
    csClz = (jclass)env->NewGlobalRef(cs);
    csiMethodID = env->GetMethodID(csClz, "<init>", "(J)V");

    jclass csb = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaStarBrowser");
    csbClz = (jclass)env->NewGlobalRef(csb);
    csbPtrFieldID = env->GetFieldID(csbClz, "pointer", "J");

    jclass cbi = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaBrowserItem");
    cbiClz = (jclass)env->NewGlobalRef(cbi);
    cbii1MethodID = env->GetMethodID(cbiClz, "<init>", "(Ljava/lang/String;Lspace/celestia/MobileCelestia/Core/CelestiaAstroObject;Lspace/celestia/MobileCelestia/Core/CelestiaBrowserItem$ChildrenProvider;)V");
    cbii2MethodID = env->GetMethodID(cbiClz, "<init>", "(Ljava/lang/String;Ljava/util/Map;)V");

    jclass cscript = env->FindClass("space/celestia/MobileCelestia/Core/CelestiaScript");
    cscriptClz = (jclass)env->NewGlobalRef(cscript);
    cscriptiMethodID = env->GetMethodID(cscriptClz, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");

    jclass al = env->FindClass("java/util/ArrayList");
    alClz = (jclass)env->NewGlobalRef(al);
    aliMethodID = env->GetMethodID(alClz, "<init>", "(I)V");
    alaMethodID = env->GetMethodID(alClz, "add", "(Ljava/lang/Object;)Z");

    jclass hm = env->FindClass("java/util/HashMap");
    hmClz = (jclass)env->NewGlobalRef(hm);
    hmiMethodID = env->GetMethodID(hmClz, "<init>", "()V");
    hmpMethodID = env->GetMethodID(hmClz, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    return JNI_VERSION_1_6;
}
}

class AppCoreProgressWatcher: public ProgressNotifier
{
public:
    AppCoreProgressWatcher(JNIEnv *env, jobject object, jmethodID method) :
    ProgressNotifier(),
    env(env),
    object(object),
    method(method) {};

    void update(const std::string& status)
    {
        if (!object) { return; }

        const char *c_str = status.c_str();
        jstring str = env->NewStringUTF(c_str);
        env->CallVoidMethod(object, method, str);
    }

private:
    JNIEnv *env;
    jobject object;
    jmethodID method;
};

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1initGL(JNIEnv *env, jclass clazz) {
    glInit();
    return JNI_TRUE;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1init(JNIEnv *env, jobject thiz) {
    CelestiaCore *core = new CelestiaCore;
    env->SetLongField(thiz, cacPtrFieldID, (jlong)core);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1startRenderer(JNIEnv *env, jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    if (!core->initRenderer())
        return JNI_FALSE;

    // start with default values
    const int DEFAULT_ORBIT_MASK = Body::Planet | Body::Moon | Body::Stellar;
    const int DEFAULT_LABEL_MODE = 2176;
    const float DEFAULT_AMBIENT_LIGHT_LEVEL = 0.1f;
    const float DEFAULT_VISUAL_MAGNITUDE = 8.0f;
    const Renderer::StarStyle DEFAULT_STAR_STYLE = Renderer::FuzzyPointStars;
    const ColorTableType DEFAULT_STARS_COLOR = ColorTable_Blackbody_D65;
    const unsigned int DEFAULT_TEXTURE_RESOLUTION = medres;

    core->getRenderer()->setRenderFlags(Renderer::DefaultRenderFlags);
    core->getRenderer()->setOrbitMask(DEFAULT_ORBIT_MASK);
    core->getRenderer()->setLabelMode(DEFAULT_LABEL_MODE);
    core->getRenderer()->setAmbientLightLevel(DEFAULT_AMBIENT_LIGHT_LEVEL);
    core->getRenderer()->setStarStyle(DEFAULT_STAR_STYLE);
    core->getRenderer()->setResolution(DEFAULT_TEXTURE_RESOLUTION);
    core->getRenderer()->setStarColorTable(GetStarColorTable(DEFAULT_STARS_COLOR));

    core->getSimulation()->setFaintestVisible(DEFAULT_VISUAL_MAGNITUDE);

    core->getRenderer()->setSolarSystemMaxDistance((core->getConfig()->SolarSystemMaxDistance));

    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1startSimulation(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jstring config_file_name,
                                                                           jobjectArray extra_directories,
                                                                           jobject wc) {
    jfieldID initFieldID = env->GetFieldID(cacClz, "intialized", "Z");
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    jmethodID jWcMethod = nullptr;
    if (wc)
    {
        jclass jWcClz = env->GetObjectClass(wc);
        jWcMethod = env->GetMethodID(jWcClz, "onCelestiaProgress", "(Ljava/lang/String;)V");
    }

    AppCoreProgressWatcher watcher(env, wc, jWcMethod);
    std::vector<fs::path> extras;
    if (extra_directories != nullptr)
    {
        jsize number = env->GetArrayLength(extra_directories);
        for (jsize i = 0; i < number; i++)
        {
            jstring str = (jstring)env->GetObjectArrayElement(extra_directories, i);
            const char *c_str = env->GetStringUTFChars(str, nullptr);
            extras.push_back(c_str);
            env->ReleaseStringUTFChars(str, c_str);
        }
    }
    std::string configFile = "";
    if (config_file_name != nullptr)
    {
        const char *c_str = env->GetStringUTFChars(config_file_name, nullptr);
        configFile = c_str;
        env->ReleaseStringUTFChars(config_file_name, c_str);
    }

    if (!core->initSimulation(configFile, extras, &watcher))
        return JNI_FALSE;

    env->SetBooleanField(thiz, initFieldID, JNI_TRUE);
    return JNI_TRUE;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1start__(JNIEnv *env, jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    core->start();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1start__D(JNIEnv *env, jobject thiz,
                                                                    jdouble seconds_since_epoch) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    core->start(seconds_since_epoch);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1draw(JNIEnv *env, jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    core->draw();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1tick(JNIEnv *env, jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    core->tick();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1resize(JNIEnv *env, jobject thiz, jint w,
                                                                  jint h) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    core->resize(w, h);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1getSimulation(JNIEnv *env,
                                                                         jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    return (jlong)core->getSimulation();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1chdir(JNIEnv *env, jclass clazz,
                                                                 jstring path) {
    const char *c_str = env->GetStringUTFChars(path, nullptr);
    chdir(c_str);
    env->ReleaseStringUTFChars(path, c_str);
}

static int convert_modifier_to_celestia_modifier(jint buttons, jint modifiers)
{
    // TODO: other modifier
    int cModifiers = 0;
    if (buttons & 1)
        cModifiers |= CelestiaCore::LeftButton;
    if (buttons & 2)
        cModifiers |= CelestiaCore::MiddleButton;
    if (buttons & 4)
        cModifiers |= CelestiaCore::RightButton;
    return cModifiers;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1mouseButtonUp(JNIEnv *env, jobject thiz,
                                                                         jint buttons, jfloat x,
                                                                         jfloat y, jint modifiers) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->mouseButtonUp(x, y, convert_modifier_to_celestia_modifier(buttons, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1mouseButtonDown(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jint buttons, jfloat x,
                                                                           jfloat y,
                                                                           jint modifiers) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->mouseButtonDown(x, y, convert_modifier_to_celestia_modifier(buttons, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1mouseMove(JNIEnv *env, jobject thiz,
                                                                     jint buttons, jfloat x,
                                                                     jfloat y, jint modifiers) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->mouseMove(x, y, convert_modifier_to_celestia_modifier(buttons, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1mouseWheel(JNIEnv *env, jobject thiz,
                                                                      jfloat motion,
                                                                      jint modifiers) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->mouseWheel(motion, convert_modifier_to_celestia_modifier(0, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1keyUp(JNIEnv *env, jobject thiz,
                                                                 jint input) {

    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->keyUp(input, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1keyDown(JNIEnv *env, jobject thiz,
                                                                   jint input) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->keyDown(input, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1charEnter(JNIEnv *env, jobject thiz,
                                                                     jint input) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->charEntered((char)input, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1runScript(JNIEnv *env, jobject thiz,
                                                                     jstring path) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    const char *str = env->GetStringUTFChars(path, nullptr);
    core->runScript(str);
    env->ReleaseStringUTFChars(path, str);
}