/*
 * CelestiaAppCore.cpp
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaJNI.h"
#include <string>

#include <unistd.h>

#include <celestia/celestiacore.h>
#include <celengine/glsupport.h>
#include <celestia/helper.h>
#include <celutil/util.h>
#include <celutil/gettext.h>
#include <celestia/url.h>

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

// vector
jclass cvClz = nullptr;
jmethodID cv3InitMethodID = nullptr;
jmethodID cv4InitMethodID = nullptr;
jmethodID cvxMethodID = nullptr;
jmethodID cvyMethodID = nullptr;
jmethodID cvzMethodID = nullptr;
jmethodID cvwMethodID = nullptr;

// orbit
jclass coClz = nullptr;
jfieldID coPtrFieldID = nullptr;

// rotation model
jclass crmClz = nullptr;
jfieldID crmPtrFieldID = nullptr;

// universal coord
jclass cucClz = nullptr;
jfieldID cucPtrFieldID = nullptr;

extern "C" {
jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK)
        return JNI_ERR;

    // Find some commonly used classes and fields
    jclass cac = env->FindClass("space/celestia/mobilecelestia/core/CelestiaAppCore");
    cacClz = (jclass)env->NewGlobalRef(cac);
    cacPtrFieldID = env->GetFieldID(cacClz, "pointer", "J");

    jclass csi = env->FindClass("space/celestia/mobilecelestia/core/CelestiaSimulation");
    csiClz = (jclass)env->NewGlobalRef(csi);
    csiPtrFieldID = env->GetFieldID(csiClz, "pointer", "J");

    jclass cse = env->FindClass("space/celestia/mobilecelestia/core/CelestiaSelection");
    cseClz = (jclass)env->NewGlobalRef(cse);
    csePtrFieldID = env->GetFieldID(cseClz, "pointer", "J");

    jclass cao = env->FindClass("space/celestia/mobilecelestia/core/CelestiaAstroObject");
    caoClz = (jclass)env->NewGlobalRef(cao);
    caoPtrFieldID = env->GetFieldID(caoClz, "pointer", "J");

    jclass cun = env->FindClass("space/celestia/mobilecelestia/core/CelestiaUniverse");
    cunClz = (jclass)env->NewGlobalRef(cun);
    cunPtrFieldID = env->GetFieldID(cunClz, "pointer", "J");

    jclass csc = env->FindClass("space/celestia/mobilecelestia/core/CelestiaStarCatalog");
    cscClz = (jclass)env->NewGlobalRef(csc);
    cscPtrFieldID = env->GetFieldID(cscClz, "pointer", "J");

    jclass cdc = env->FindClass("space/celestia/mobilecelestia/core/CelestiaDSOCatalog");
    cdcClz = (jclass)env->NewGlobalRef(cdc);
    cdcPtrFieldID = env->GetFieldID(cdcClz, "pointer", "J");

    jclass cb = env->FindClass("space/celestia/mobilecelestia/core/CelestiaBody");
    cbClz = (jclass)env->NewGlobalRef(cb);
    cbiMethodID = env->GetMethodID(cbClz, "<init>", "(J)V");

    jclass cl = env->FindClass("space/celestia/mobilecelestia/core/CelestiaLocation");
    clClz = (jclass)env->NewGlobalRef(cl);
    cliMethodID = env->GetMethodID(clClz, "<init>", "(J)V");

    jclass cs = env->FindClass("space/celestia/mobilecelestia/core/CelestiaStar");
    csClz = (jclass)env->NewGlobalRef(cs);
    csiMethodID = env->GetMethodID(csClz, "<init>", "(J)V");

    jclass csb = env->FindClass("space/celestia/mobilecelestia/core/CelestiaStarBrowser");
    csbClz = (jclass)env->NewGlobalRef(csb);
    csbPtrFieldID = env->GetFieldID(csbClz, "pointer", "J");

    jclass cscript = env->FindClass("space/celestia/mobilecelestia/core/CelestiaScript");
    cscriptClz = (jclass)env->NewGlobalRef(cscript);
    cscriptiMethodID = env->GetMethodID(cscriptClz, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");

    jclass cv = env->FindClass("space/celestia/mobilecelestia/core/CelestiaVector");
    cvClz = (jclass)env->NewGlobalRef(cv);
    cv3InitMethodID = env->GetMethodID(cvClz, "<init>", "(DDD)V");
    cv4InitMethodID = env->GetMethodID(cvClz, "<init>", "(DDDD)V");
    cvxMethodID = env->GetMethodID(cvClz, "getX", "()D");
    cvyMethodID = env->GetMethodID(cvClz, "getY", "()D");
    cvzMethodID = env->GetMethodID(cvClz, "getZ", "()D");
    cvwMethodID = env->GetMethodID(cvClz, "getW", "()D");

    jclass co = env->FindClass("space/celestia/mobilecelestia/core/CelestiaOrbit");
    coClz = (jclass)env->NewGlobalRef(co);
    coPtrFieldID = env->GetFieldID(coClz, "pointer", "J");

    jclass crm = env->FindClass("space/celestia/mobilecelestia/core/CelestiaRotationModel");
    crmClz = (jclass)env->NewGlobalRef(crm);
    crmPtrFieldID = env->GetFieldID(crmClz, "pointer", "J");

    jclass cuc = env->FindClass("space/celestia/mobilecelestia/core/CelestiaUniversalCoord");
    cucClz = (jclass)env->NewGlobalRef(cuc);
    cucPtrFieldID = env->GetFieldID(cucClz, "pointer", "J");

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
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1initGL(JNIEnv *env, jclass clazz) {
    celestia::gl::init();
    return JNI_TRUE;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1init(JNIEnv *env, jobject thiz) {
    CelestiaCore *core = new CelestiaCore;
    env->SetLongField(thiz, cacPtrFieldID, (jlong)core);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1startRenderer(JNIEnv *env, jobject thiz) {
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
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1startSimulation(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jstring config_file_name,
                                                                           jobjectArray extra_directories,
                                                                           jobject wc) {
    jfieldID initFieldID = env->GetFieldID(cacClz, "initialized", "Z");
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
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1start__(JNIEnv *env, jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    core->start();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1start__D(JNIEnv *env, jobject thiz,
                                                                    jdouble seconds_since_epoch) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    core->start(seconds_since_epoch);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1draw(JNIEnv *env, jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    core->draw();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1tick(JNIEnv *env, jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    core->tick();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1resize(JNIEnv *env, jobject thiz, jint w,
                                                                  jint h) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    core->resize(w, h);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getSimulation(JNIEnv *env,
                                                                         jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);

    return (jlong)core->getSimulation();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1chdir(JNIEnv *env, jclass clazz,
                                                                 jstring path) {
    const char *c_str = env->GetStringUTFChars(path, nullptr);
    chdir(c_str);
    env->ReleaseStringUTFChars(path, c_str);
}

static int convert_modifier_to_celestia_modifier(jint buttons, jint modifiers)
{
    // TODO: other modifier
    int cModifiers = 0;
    if (buttons & 0x01)
        cModifiers |= CelestiaCore::LeftButton;
    if (buttons & 0x02)
        cModifiers |= CelestiaCore::MiddleButton;
    if (buttons & 0x04)
        cModifiers |= CelestiaCore::RightButton;
    if (modifiers & 0x08)
        cModifiers |= CelestiaCore::ShiftKey;
    if (modifiers & 0x10)
        cModifiers |= CelestiaCore::ControlKey;
    return cModifiers;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1mouseButtonUp(JNIEnv *env, jobject thiz,
                                                                         jint buttons, jfloat x,
                                                                         jfloat y, jint modifiers) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->mouseButtonUp(x, y, convert_modifier_to_celestia_modifier(buttons, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1mouseButtonDown(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jint buttons, jfloat x,
                                                                           jfloat y,
                                                                           jint modifiers) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->mouseButtonDown(x, y, convert_modifier_to_celestia_modifier(buttons, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1mouseMove(JNIEnv *env, jobject thiz,
                                                                     jint buttons, jfloat x,
                                                                     jfloat y, jint modifiers) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->mouseMove(x, y, convert_modifier_to_celestia_modifier(buttons, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1mouseWheel(JNIEnv *env, jobject thiz,
                                                                      jfloat motion,
                                                                      jint modifiers) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->mouseWheel(motion, convert_modifier_to_celestia_modifier(0, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1keyUp(JNIEnv *env, jobject thiz,
                                                                 jint input) {

    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->keyUp(input, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1keyDown(JNIEnv *env, jobject thiz,
                                                                   jint input) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->keyDown(input, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1charEnter(JNIEnv *env, jobject thiz,
                                                                     jint input) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->charEntered((char)input, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1runScript(JNIEnv *env, jobject thiz,
                                                                     jstring path) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    const char *str = env->GetStringUTFChars(path, nullptr);
    core->runScript(str);
    env->ReleaseStringUTFChars(path, str);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getCurrentURL(JNIEnv *env,
                                                                         jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    CelestiaState appState;
    appState.captureState(core);

    Url currentURL(appState, Url::CurrentVersion);
    return env->NewStringUTF(currentURL.getAsString().c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1goToURL(JNIEnv *env, jobject thiz,
                                                                   jstring url) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    const char *str = env->GetStringUTFChars(url, nullptr);
    core->goToUrl(str);
    env->ReleaseStringUTFChars(url, str);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setLocaleDirectoryPath(JNIEnv *env,
                                                                                  jclass clazz,
                                                                                  jstring path,
                                                                                  jstring locale) {
    // Set environment variable since NDK does not support locale
    const char *str = env->GetStringUTFChars(locale, nullptr);
    setenv("LANG", str, true);
    env->ReleaseStringUTFChars(locale, str);

    // Gettext integration
    setlocale(LC_ALL, "");
    setlocale(LC_NUMERIC, "C");
    str = env->GetStringUTFChars(path, nullptr);
    bindtextdomain("celestia", str);
    bind_textdomain_codeset("celestia", "UTF-8");
    bindtextdomain("celestia_constellations", str);
    bind_textdomain_codeset("celestia_constellations", "UTF-8");
    bindtextdomain("celestia_ui", str);
    bind_textdomain_codeset("celestia_ui", "UTF-8");
    textdomain("celestia");
    env->ReleaseStringUTFChars(path, str);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getLocalizedString(JNIEnv *env,
                                                                              jclass clazz,
                                                                              jstring string,
                                                                              jstring domain) {
    const char *c_str = env->GetStringUTFChars(string, nullptr);
    const char *c_dom = env->GetStringUTFChars(domain, nullptr);
    jstring localized = env->NewStringUTF(dgettext(c_dom, c_str));
    env->ReleaseStringUTFChars(string, c_str);
    env->ReleaseStringUTFChars(domain, c_dom);
    return localized;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getLocalizedFilename(JNIEnv *env,
                                                                                jclass clazz,
                                                                                jstring string) {
    const char *str = env->GetStringUTFChars(string, nullptr);
    jstring localized = env->NewStringUTF(LocaleFilename(str).string().c_str());
    env->ReleaseStringUTFChars(string, str);
    return localized;
}

static uint64_t bit_mask_value_update(jboolean value, uint64_t bit, uint64_t set) {
    uint64_t result = value ? ((bit & set) ? set : (set | bit)) : ((bit & set) ?  (set ^ bit) : set);
    return result;
}

#define RENDERMETHODS(flag) extern "C" JNIEXPORT jboolean JNICALL \
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getShow##flag (JNIEnv *env, jobject thiz) { \
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID); \
    return (jboolean)(((core->getRenderer()->getRenderFlags() & Renderer::Show##flag) == 0) ? JNI_FALSE : JNI_TRUE); \
} \
extern "C" \
JNIEXPORT void JNICALL \
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setShow##flag (JNIEnv *env, jobject thiz, \
                                                                        jboolean value) { \
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID); \
    core->getRenderer()->setRenderFlags(bit_mask_value_update(value, Renderer::Show##flag, core->getRenderer()->getRenderFlags())); \
} \

RENDERMETHODS(Stars)
RENDERMETHODS(Planets)
RENDERMETHODS(DwarfPlanets)
RENDERMETHODS(Moons)
RENDERMETHODS(MinorMoons)
RENDERMETHODS(Asteroids)
RENDERMETHODS(Comets)
RENDERMETHODS(Spacecrafts)
RENDERMETHODS(Galaxies)
RENDERMETHODS(Globulars)
RENDERMETHODS(Nebulae)
RENDERMETHODS(OpenClusters)
RENDERMETHODS(Diagrams)
RENDERMETHODS(Boundaries)
RENDERMETHODS(CloudMaps)
RENDERMETHODS(NightMaps)
RENDERMETHODS(Atmospheres)
RENDERMETHODS(CometTails)
RENDERMETHODS(PlanetRings)
RENDERMETHODS(Markers)
RENDERMETHODS(Orbits)
RENDERMETHODS(PartialTrajectories)
RENDERMETHODS(SmoothLines)
RENDERMETHODS(EclipseShadows)
RENDERMETHODS(RingShadows)
RENDERMETHODS(CloudShadows)
RENDERMETHODS(AutoMag)
RENDERMETHODS(CelestialSphere)
RENDERMETHODS(EclipticGrid)
RENDERMETHODS(HorizonGrid)
RENDERMETHODS(GalacticGrid)

#define LABELMETHODS(flag) extern "C" JNIEXPORT jboolean JNICALL \
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getShow##flag##Labels (JNIEnv *env, jobject thiz) { \
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID); \
    return (jboolean)(((core->getRenderer()->getLabelMode() & Renderer::flag##Labels) == 0) ? JNI_FALSE : JNI_TRUE); \
} \
extern "C" \
JNIEXPORT void JNICALL \
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setShow##flag##Labels (JNIEnv *env, jobject thiz, \
                                                                        jboolean value) { \
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID); \
    core->getRenderer()->setLabelMode((int)bit_mask_value_update(value, Renderer::flag##Labels, core->getRenderer()->getLabelMode())); \
} \

LABELMETHODS(Star)
LABELMETHODS(Planet)
LABELMETHODS(Moon)
LABELMETHODS(Constellation)
LABELMETHODS(Galaxy)
LABELMETHODS(Globular)
LABELMETHODS(Nebula)
LABELMETHODS(OpenCluster)
LABELMETHODS(Asteroid)
LABELMETHODS(Spacecraft)
LABELMETHODS(Location)
LABELMETHODS(Comet)
LABELMETHODS(DwarfPlanet)
LABELMETHODS(MinorMoon)

LABELMETHODS(I18nConstellation)

#define ORBITMETHODS(flag) extern "C" JNIEXPORT jboolean JNICALL \
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getShow##flag##Orbits (JNIEnv *env, jobject thiz) { \
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID); \
    return (jboolean)(((core->getRenderer()->getOrbitMask() & Body::flag) == 0) ? JNI_FALSE : JNI_TRUE); \
} \
extern "C" \
JNIEXPORT void JNICALL \
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setShow##flag##Orbits (JNIEnv *env, jobject thiz, \
                                                                        jboolean value) { \
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID); \
    core->getRenderer()->setOrbitMask((int)bit_mask_value_update(value, Body::flag, core->getRenderer()->getOrbitMask())); \
} \

ORBITMETHODS(Planet)
ORBITMETHODS(Moon)
ORBITMETHODS(Asteroid)
ORBITMETHODS(Spacecraft)
ORBITMETHODS(Comet)
ORBITMETHODS(Stellar)
ORBITMETHODS(DwarfPlanet)
ORBITMETHODS(MinorMoon)

#define FEATUREMETHODS(flag) extern "C" JNIEXPORT jboolean JNICALL \
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getShow##flag##Labels (JNIEnv *env, jobject thiz) { \
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID); \
    return (jboolean)(((core->getSimulation()->getObserver().getLocationFilter() & Location::flag) == 0) ? JNI_FALSE : JNI_TRUE); \
} \
extern "C" \
JNIEXPORT void JNICALL \
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setShow##flag##Labels (JNIEnv *env, jobject thiz, \
                                                                        jboolean value) { \
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID); \
    core->getSimulation()->getObserver().setLocationFilter((int)bit_mask_value_update(value, Location::flag, core->getSimulation()->getObserver().getLocationFilter())); \
} \

FEATUREMETHODS(City)
FEATUREMETHODS(Observatory)
FEATUREMETHODS(LandingSite)
FEATUREMETHODS(Crater)
FEATUREMETHODS(Vallis)
FEATUREMETHODS(Mons)
FEATUREMETHODS(Planum)
FEATUREMETHODS(Chasma)
FEATUREMETHODS(Patera)
FEATUREMETHODS(Mare)
FEATUREMETHODS(Rupes)
FEATUREMETHODS(Tessera)
FEATUREMETHODS(Regio)
FEATUREMETHODS(Chaos)
FEATUREMETHODS(Terra)
FEATUREMETHODS(Astrum)
FEATUREMETHODS(Corona)
FEATUREMETHODS(Dorsum)
FEATUREMETHODS(Fossa)
FEATUREMETHODS(Catena)
FEATUREMETHODS(Mensa)
FEATUREMETHODS(Rima)
FEATUREMETHODS(Undae)
FEATUREMETHODS(Reticulum)
FEATUREMETHODS(Planitia)
FEATUREMETHODS(Linea)
FEATUREMETHODS(Fluctus)
FEATUREMETHODS(Farrum)
FEATUREMETHODS(EruptiveCenter)
FEATUREMETHODS(Other)extern "C"

JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setResolution(JNIEnv *env, jobject thiz,
                                                                         jint value) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->getRenderer()->setResolution(value);
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getResolution(JNIEnv *env,
                                                                         jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    return core->getRenderer()->getResolution();
}

extern "C" JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setHudDetail(JNIEnv *env, jobject thiz,
                                                                         jint value) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->setHudDetail(value);
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getHudDetail(JNIEnv *env,
                                                                         jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    return core->getHudDetail();
}

extern "C" JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setTimeZone(JNIEnv *env, jobject thiz,
                                                                        jint value) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->setTimeZoneBias(0 == value ? 1 : 0);
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getTimeZone(JNIEnv *env,
                                                                        jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    return core->getTimeZoneBias() == 0 ? 1 : 0;
}

extern "C" JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setDateFormat(JNIEnv *env, jobject thiz,
                                                                       jint value) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->setDateFormat((astro::Date::Format)value);
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getDateFormat(JNIEnv *env,
                                                                       jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    return core->getDateFormat();
}

extern "C" JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setStarStyle(JNIEnv *env, jobject thiz,
                                                                         jint value) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->getRenderer()->setStarStyle((Renderer::StarStyle)value);
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getStarStyle(JNIEnv *env,
                                                                         jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    return core->getRenderer()->getStarStyle();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getRenderInfo(JNIEnv *env,
                                                                         jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    return env->NewStringUTF(Helper::getRenderInfo(core->getRenderer()).c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setAmbientLightLevel(JNIEnv *env,
                                                                                jobject thiz,
                                                                                jdouble ambient_light_level) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->getRenderer()->setAmbientLightLevel((float)ambient_light_level);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getAmbientLightLevel(JNIEnv *env,
                                                                                jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    return core->getRenderer()->getAmbientLightLevel();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setFaintestVisible(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jdouble faintest_visible) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    if ((core->getRenderer()->getRenderFlags() & Renderer::ShowAutoMag) == 0)
    {
        core->setFaintest((float)faintest_visible);
    }
    else
    {
        core->getRenderer()->setFaintestAM45deg((float)faintest_visible);
        core->setFaintestAutoMag();
    }
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getFaintestVisible(JNIEnv *env,
                                                                              jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    if ((core->getRenderer()->getRenderFlags() & Renderer::ShowAutoMag) == 0)
    {
        return core->getSimulation()->getFaintestVisible();
    }
    else
    {
        return core->getRenderer()->getFaintestAM45deg();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setGalaxyBrightness(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jdouble galaxy_brightness) {
    Galaxy::setLightGain((float)galaxy_brightness);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1getGalaxyBrightness(JNIEnv *env,
                                                                               jobject thiz) {
    return Galaxy::getLightGain();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1setMinimumFeatureSize(JNIEnv *env,
                                                                                 jobject thiz,
                                                                                 jdouble minimum_feature_size) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    core->getRenderer()->setMinimumFeatureSize((float)minimum_feature_size);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaAppCore_c_1geMinimumFeatureSize(JNIEnv *env,
                                                                                jobject thiz) {
    CelestiaCore *core = (CelestiaCore *)env->GetLongField(thiz, cacPtrFieldID);
    return core->getRenderer()->getMinimumFeatureSize();
}