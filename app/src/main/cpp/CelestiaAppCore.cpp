#include <jni.h>
#include <string>

#include <unistd.h>

#include <celestia/celestiacore.h>
#include <celengine/gl.h>

static jclass cacClz = nullptr;
static jfieldID cacPtrFieldID = nullptr;

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
    return JNI_VERSION_1_6;
}
}

class AppCoreProgressWatcher: public ProgressNotifier
{
public:
    AppCoreProgressWatcher() : ProgressNotifier() {};
    void update(const std::string& status)
    {
        // TODO: tell object to update UI
    }
private:
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
                                                                           jobjectArray extra_directories) {
    jfieldID initFieldID = env->GetFieldID(cacClz, "intialized", "Z");
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    AppCoreProgressWatcher watcher;
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