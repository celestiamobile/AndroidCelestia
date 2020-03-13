#include <jni.h>
#include <string>

#include <celestia/celestiacore.h>
#include <celengine/gl.h>

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

extern "C" JNIEXPORT jstring JNICALL
Java_space_celestia_MobileCelestia_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

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
    jclass clz = env->GetObjectClass(thiz);
    jfieldID fieldID = env->GetFieldID(clz, "pointer", "J");
    env->SetLongField(thiz, fieldID, (jlong)core);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1startRenderer(JNIEnv *env, jobject thiz) {
    jclass clz = env->GetObjectClass(thiz);
    jfieldID fieldID = env->GetFieldID(clz, "pointer", "J");
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, fieldID);

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

    jclass clz = env->GetObjectClass(thiz);
    jfieldID fieldID = env->GetFieldID(clz, "pointer", "J");
    jfieldID initFieldID = env->GetFieldID(clz, "intialized", "Z");
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, fieldID);

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
    jclass clz = env->GetObjectClass(thiz);
    jfieldID fieldID = env->GetFieldID(clz, "pointer", "J");
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, fieldID);

    core->start();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1start__D(JNIEnv *env, jobject thiz,
                                                                    jdouble seconds_since_epoch) {
    jclass clz = env->GetObjectClass(thiz);
    jfieldID fieldID = env->GetFieldID(clz, "pointer", "J");
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, fieldID);

    core->start(seconds_since_epoch);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1draw(JNIEnv *env, jobject thiz) {
    jclass clz = env->GetObjectClass(thiz);
    jfieldID fieldID = env->GetFieldID(clz, "pointer", "J");
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, fieldID);

    core->draw();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1tick(JNIEnv *env, jobject thiz) {
    jclass clz = env->GetObjectClass(thiz);
    jfieldID fieldID = env->GetFieldID(clz, "pointer", "J");
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, fieldID);

    core->tick();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1resize(JNIEnv *env, jobject thiz, jint w,
                                                                  jint h) {
    jclass clz = env->GetObjectClass(thiz);
    jfieldID fieldID = env->GetFieldID(clz, "pointer", "J");
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, fieldID);

    core->resize(w, h);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_MobileCelestia_Core_CelestiaAppCore_c_1getSimulation(JNIEnv *env,
                                                                         jobject thiz) {
    jclass clz = env->GetObjectClass(thiz);
    jfieldID fieldID = env->GetFieldID(clz, "pointer", "J");
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, fieldID);

    return (jlong)core->getSimulation();
}