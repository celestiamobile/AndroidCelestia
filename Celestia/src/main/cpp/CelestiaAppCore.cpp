/*
 * CelestiaAppCore.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include "CelestiaSelection.h"
#include <string>

#include <unistd.h>

#include <celestia/celestiacore.h>
#include <celengine/body.h>
#include <celengine/glsupport.h>
#include <celengine/location.h>
#include <celestia/configfile.h>
#include <celestia/helper.h>
#include <celestia/progressnotifier.h>
#include <celutil/flag.h>
#include <celutil/fsutils.h>
#include <celutil/gettext.h>
#include <celutil/localeutil.h>
#include <celestia/url.h>
#include <unicode/uloc.h>
#include <fmt/format.h>

#include <android/keycodes.h>

jclass stringClz = nullptr;

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

// destination
jclass cdClz = nullptr;
jmethodID cdInitMethodID = nullptr;

// selection
jclass selectionClz = nullptr;
jmethodID selectionGetObjectPointerMethodID = nullptr;
jmethodID selectionGetObjectTypeMethodID = nullptr;
jmethodID selectionInitMethodID = nullptr;

jclass completionClz = nullptr;
jmethodID completionInitMethodID = nullptr;

extern "C" {
jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK)
        return JNI_ERR;

    stringClz = (jclass)env->NewGlobalRef(env->FindClass("java/lang/String"));

    jclass cb = env->FindClass("space/celestia/celestia/Body");
    cbClz = (jclass)env->NewGlobalRef(cb);
    cbiMethodID = env->GetMethodID(cbClz, "<init>", "(J)V");

    jclass cl = env->FindClass("space/celestia/celestia/Location");
    clClz = (jclass)env->NewGlobalRef(cl);
    cliMethodID = env->GetMethodID(clClz, "<init>", "(J)V");

    jclass cs = env->FindClass("space/celestia/celestia/Star");
    csClz = (jclass)env->NewGlobalRef(cs);
    csiMethodID = env->GetMethodID(csClz, "<init>", "(J)V");

    jclass cscript = env->FindClass("space/celestia/celestia/Script");
    cscriptClz = (jclass)env->NewGlobalRef(cscript);
    cscriptiMethodID = env->GetMethodID(cscriptClz, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");

    jclass cv = env->FindClass("space/celestia/celestia/Vector");
    cvClz = (jclass)env->NewGlobalRef(cv);
    cv3InitMethodID = env->GetMethodID(cvClz, "<init>", "(DDD)V");
    cv4InitMethodID = env->GetMethodID(cvClz, "<init>", "(DDDD)V");
    cvxMethodID = env->GetMethodID(cvClz, "getX", "()D");
    cvyMethodID = env->GetMethodID(cvClz, "getY", "()D");
    cvzMethodID = env->GetMethodID(cvClz, "getZ", "()D");
    cvwMethodID = env->GetMethodID(cvClz, "getW", "()D");

    jclass cd = env->FindClass("space/celestia/celestia/Destination");
    cdClz = (jclass)env->NewGlobalRef(cd);
    cdInitMethodID = env->GetMethodID(cdClz, "<init>", "(Ljava/lang/String;Ljava/lang/String;DLjava/lang/String;)V");

    jclass al = env->FindClass("java/util/ArrayList");
    alClz = (jclass)env->NewGlobalRef(al);
    aliMethodID = env->GetMethodID(alClz, "<init>", "(I)V");
    alaMethodID = env->GetMethodID(alClz, "add", "(Ljava/lang/Object;)Z");

    jclass hm = env->FindClass("java/util/HashMap");
    hmClz = (jclass)env->NewGlobalRef(hm);
    hmiMethodID = env->GetMethodID(hmClz, "<init>", "()V");
    hmpMethodID = env->GetMethodID(hmClz, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    selectionClz = (jclass)env->NewGlobalRef(env->FindClass("space/celestia/celestia/Selection"));
    selectionGetObjectPointerMethodID = env->GetMethodID(selectionClz, "getObjectPointer", "()J");
    selectionGetObjectTypeMethodID = env->GetMethodID(selectionClz, "getObjectType", "()I");
    selectionInitMethodID = env->GetMethodID(selectionClz, "<init>", "(JI)V");

    completionClz = (jclass)env->NewGlobalRef(env->FindClass("space/celestia/celestia/Completion"));
    completionInitMethodID = env->GetMethodID(completionClz, "<init>", "(Ljava/lang/String;Lspace/celestia/celestia/Selection;)V");

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

    void update(const std::string& status) override
    {
        if (!object) { return; }

        const char *c_str = status.c_str();
        jstring str = env->NewStringUTF(c_str);
        env->CallVoidMethod(object, method, str);
        env->DeleteLocalRef(str);
    }

private:
    JNIEnv *env;
    jobject object;
    jmethodID method;
};

class AppCoreContextMenuHandler: public CelestiaCore::ContextMenuHandler
{
public:
    AppCoreContextMenuHandler(jobject object, jmethodID method) :
    CelestiaCore::ContextMenuHandler(),
    object(object),
    method(method) {};

    ~AppCoreContextMenuHandler() override
    {
        auto env = (JNIEnv *)pthread_getspecific(javaEnvKey);
        if (env && object) env->DeleteGlobalRef(object);
    }

    void requestContextMenu(float x, float y, Selection sel) override
    {
        auto env = (JNIEnv *)pthread_getspecific(javaEnvKey);
        if (!env) return;
        env->CallVoidMethod(object, method, (jfloat)x, (jfloat)y, selectionAsJavaSelection(env, sel));
    }

private:
    jobject object;
    jmethodID method;
};

class AppCoreFatalErrorHandler: public CelestiaCore::Alerter
{
public:
    AppCoreFatalErrorHandler(jobject object, jmethodID method) : CelestiaCore::Alerter(), object(object), method(method) {};

    ~AppCoreFatalErrorHandler() override
    {
        auto env = (JNIEnv *)pthread_getspecific(javaEnvKey);
        if (env && object) env->DeleteGlobalRef(object);
    }

    void fatalError(const std::string &message) override
    {
        auto env = (JNIEnv *)pthread_getspecific(javaEnvKey);
        if (!env) return;
        jstring str = env->NewStringUTF(message.c_str());
        env->CallVoidMethod(object, method, str);
        env->DeleteLocalRef(str);
    }

private:
    jobject object;
    jmethodID method;
};

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_AppCore_c_1initGL(JNIEnv *env, jclass clazz) {
    celestia::gl::init();
    celestia::gl::disableGeomShaders();
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_AppCore_c_1init(JNIEnv *env, jclass clazz) {
    auto core = new CelestiaCore;
    return (jlong)core;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setContextMenuHandler(JNIEnv *env, jobject thiz, jlong ptr) {
    auto core = (CelestiaCore *)ptr;
    static jmethodID contextMenuCallback = nullptr;
    if (!contextMenuCallback)
        contextMenuCallback = env->GetMethodID(env->GetObjectClass(thiz), "onRequestContextMenu",
                                               "(FFLspace/celestia/celestia/Selection;)V");
    core->setContextMenuHandler(new AppCoreContextMenuHandler(env->NewGlobalRef(thiz), contextMenuCallback));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setSystemAccessHandler(JNIEnv *env, jobject thiz, jlong ptr) {
    auto core = reinterpret_cast<CelestiaCore *>(ptr);
    static jmethodID systemAccessCallback = nullptr;
    if (!systemAccessCallback)
        systemAccessCallback = env->GetMethodID(env->GetObjectClass(thiz), "onSystemAccessRequested", "()I");
    jobject globalRef = env->NewGlobalRef(thiz);
    core->setScriptSystemAccessHandler([globalRef]{
        auto env = reinterpret_cast<JNIEnv *>(pthread_getspecific(javaEnvKey));
        if (!env) return CelestiaCore::ScriptSystemAccessPolicy::Ask;
        return static_cast<CelestiaCore::ScriptSystemAccessPolicy>(env->CallIntMethod(globalRef, systemAccessCallback));
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setFatalErrorHandler(JNIEnv *env, jobject thiz, jlong ptr) {
    auto core = reinterpret_cast<CelestiaCore *>(ptr);
    static jmethodID fatalErrorCallback = nullptr;
    if (!fatalErrorCallback)
        fatalErrorCallback = env->GetMethodID(env->GetObjectClass(thiz), "onFatalError",
                                               "(Ljava/lang/String;)V");
    core->setAlerter(new AppCoreFatalErrorHandler(env->NewGlobalRef(thiz), fatalErrorCallback));
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_AppCore_c_1startRenderer(JNIEnv *env, jclass clazz,
                                                                 jlong ptr) {
    auto core = (CelestiaCore *)ptr;

    if (!core->initRenderer())
        return JNI_FALSE;

    // start with default values
    constexpr auto DEFAULT_ORBIT_MASK = BodyClassification::Planet | BodyClassification::Moon | BodyClassification::Stellar;
    constexpr auto DEFAULT_LABEL_MODE = RenderLabels::I18nConstellationLabels | RenderLabels::LocationLabels;
    constexpr float DEFAULT_AMBIENT_LIGHT_LEVEL = 0.1f;
    constexpr float DEFAULT_VISUAL_MAGNITUDE = 8.0f;
    constexpr StarStyle DEFAULT_STAR_STYLE = StarStyle::FuzzyPointStars;
    constexpr ColorTableType DEFAULT_STARS_COLOR = ColorTableType::Blackbody_D65;
    constexpr auto DEFAULT_TEXTURE_RESOLUTION = TextureResolution::medres;
    constexpr float DEFAULT_TINT_SATURATION = 0.5f;

    core->getRenderer()->setRenderFlags(RenderFlags::DefaultRenderFlags);
    core->getRenderer()->setOrbitMask(DEFAULT_ORBIT_MASK);
    core->getRenderer()->setLabelMode(DEFAULT_LABEL_MODE);
    core->getRenderer()->setAmbientLightLevel(DEFAULT_AMBIENT_LIGHT_LEVEL);
    core->getRenderer()->setTintSaturation(DEFAULT_TINT_SATURATION);
    core->getRenderer()->setStarStyle(DEFAULT_STAR_STYLE);
    core->getRenderer()->setResolution(DEFAULT_TEXTURE_RESOLUTION);
    core->getRenderer()->setStarColorTable(DEFAULT_STARS_COLOR);

    core->getSimulation()->setFaintestVisible(DEFAULT_VISUAL_MAGNITUDE);

    core->getRenderer()->setSolarSystemMaxDistance((core->getConfig()->renderDetails.SolarSystemMaxDistance));
    core->getRenderer()->setShadowMapSize(core->getConfig()->renderDetails.ShadowMapSize);

    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_AppCore_c_1startSimulation(JNIEnv *env, jclass clazz,
                                                                   jlong ptr,
                                                                   jstring config_file_name,
                                                                   jobjectArray extra_directories,
                                                                   jobject wc) {
    auto core = (CelestiaCore *)ptr;

    jmethodID jWcMethod = nullptr;
    if (wc)
    {
        jclass jWcClz = env->GetObjectClass(wc);
        jWcMethod = env->GetMethodID(jWcClz, "onCelestiaProgress", "(Ljava/lang/String;)V");
    }

    AppCoreProgressWatcher watcher(env, wc, jWcMethod);
    std::vector<std::filesystem::path> extras;
    if (extra_directories != nullptr)
    {
        jsize number = env->GetArrayLength(extra_directories);
        for (jsize i = 0; i < number; i++)
        {
            auto str = (jstring)env->GetObjectArrayElement(extra_directories, i);
            const char *c_str = env->GetStringUTFChars(str, nullptr);
            extras.emplace_back(c_str);
            env->ReleaseStringUTFChars(str, c_str);
        }
    }
    std::string configFile;
    if (config_file_name != nullptr)
    {
        const char *c_str = env->GetStringUTFChars(config_file_name, nullptr);
        configFile = c_str;
        env->ReleaseStringUTFChars(config_file_name, c_str);
    }

    if (!core->initSimulation(configFile, extras, &watcher))
        return JNI_FALSE;

    return JNI_TRUE;
}


extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1start__J(JNIEnv *env, jclass clazz,
                                                            jlong ptr) {
    auto *core = (CelestiaCore *)ptr;

    core->start();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1start__JD(JNIEnv *env, jclass clazz,
                                                             jlong ptr,
                                                             jdouble seconds_since_epoch) {
    auto *core = (CelestiaCore *)ptr;

    core->start(seconds_since_epoch);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1draw(JNIEnv *env, jclass clazz,
                                                        jlong ptr) {
    ((CelestiaCore *)ptr)->draw();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1tick(JNIEnv *env, jclass clazz,
                                                        jlong ptr) {
    ((CelestiaCore *)ptr)->tick();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1resize(JNIEnv *env, jclass clazz,
                                                          jlong ptr, jint w,
                                                          jint h) {
    auto *core = (CelestiaCore *)ptr;

    core->resize(w, h);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setSafeAreaInsets(JNIEnv *env, jclass clazz,
                                                                     jlong ptr,
                                                                     jint left, jint top,
                                                                     jint right,
                                                                     jint bottom) {
    auto *core = (CelestiaCore *)ptr;

    core->setSafeAreaInsets(left, top, right, bottom);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setDPI(JNIEnv *env, jclass clazz,
                                                          jlong ptr,
                                                          jint dpi) {
    auto core = (CelestiaCore *)ptr;

    core->setScreenDpi(dpi);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_AppCore_c_1getSimulation(JNIEnv *env, jclass clazz,
                                                                 jlong ptr) {
    auto *core = (CelestiaCore *)ptr;

    return (jlong)core->getSimulation();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1chdir(JNIEnv *env, jclass clazz,
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
    if (modifiers & 0x40)
        cModifiers |= CelestiaCore::Touch;
    return cModifiers;
}

static int convert_key_code_to_celestia_key(int input, int key)
{
    int celestiaKey = 0;
    if (key >= AKEYCODE_NUMPAD_0 && key <= AKEYCODE_NUMPAD_9)
        celestiaKey = CelestiaCore::Key_NumPad0 + (key - AKEYCODE_NUMPAD_0);
    else if (key >= AKEYCODE_F1 && key <= AKEYCODE_F12)
        celestiaKey = CelestiaCore::Key_F1 + (key - AKEYCODE_F1);
    else
        switch(key)
        {
            case AKEYCODE_DPAD_UP:
                celestiaKey = CelestiaCore::Key_Up;
                break;
            case AKEYCODE_DPAD_DOWN:
                celestiaKey = CelestiaCore::Key_Down;
                break;
            case AKEYCODE_DPAD_LEFT:
                celestiaKey = CelestiaCore::Key_Left;
                break;
            case AKEYCODE_DPAD_RIGHT:
                celestiaKey = CelestiaCore::Key_Right;
                break;
            case AKEYCODE_PAGE_UP:
                celestiaKey = CelestiaCore::Key_PageUp;
                break;
            case AKEYCODE_PAGE_DOWN:
                celestiaKey = CelestiaCore::Key_PageDown;
                break;
            case AKEYCODE_MOVE_HOME:
                celestiaKey = CelestiaCore::Key_Home;
                break;
            case AKEYCODE_MOVE_END:
                celestiaKey = CelestiaCore::Key_End;
                break;
            case AKEYCODE_INSERT:
                celestiaKey = CelestiaCore::Key_Insert;
                break;
            default:
                if ((input < 128) && (input > 33))
                {
                    celestiaKey = (int) (input & 0x00FF);
                }
                break;
        }

    return celestiaKey;
}

static int convert_joystick_button(jint key)
{
    switch (key)
    {
        case AKEYCODE_BUTTON_L2:
            return CelestiaCore::JoyButton7;
        case AKEYCODE_BUTTON_R2:
            return CelestiaCore::JoyButton8;
        case AKEYCODE_BUTTON_A:
            return CelestiaCore::JoyButton1;
        case AKEYCODE_BUTTON_X:
            return CelestiaCore::JoyButton2;
        default:
            return -1;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1mouseButtonUp(JNIEnv *env, jclass clazz,
                                                                 jlong ptr,
                                                                 jint buttons, jfloat x,
                                                                 jfloat y, jint modifiers) {
    auto core = (CelestiaCore *)ptr;
    core->mouseButtonUp(x, y, convert_modifier_to_celestia_modifier(buttons, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1mouseButtonDown(JNIEnv *env, jclass clazz,
                                                                   jlong ptr,
                                                                   jint buttons, jfloat x,
                                                                   jfloat y,
                                                                   jint modifiers) {
    auto core = (CelestiaCore *)ptr;
    core->mouseButtonDown(x, y, convert_modifier_to_celestia_modifier(buttons, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1mouseMove(JNIEnv *env, jclass clazz,
                                                             jlong ptr,
                                                             jint buttons, jfloat x,
                                                             jfloat y, jint modifiers) {
    auto core = (CelestiaCore *)ptr;
    core->mouseMove(x, y, convert_modifier_to_celestia_modifier(buttons, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1mouseWheel(JNIEnv *env, jclass clazz,
                                                              jlong ptr,
                                                              jfloat motion,
                                                              jint modifiers) {
    auto core = (CelestiaCore *)ptr;
    core->mouseWheel(motion, convert_modifier_to_celestia_modifier(0, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1keyUpWithModifiers(JNIEnv *env,
                                                                      jclass clazz,
                                                                      jlong ptr, jint input,
                                                                      jint key, jint modifiers) {
    auto core = (CelestiaCore *)ptr;
    core->keyUp(convert_key_code_to_celestia_key(input, key), convert_modifier_to_celestia_modifier(0, modifiers));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1keyDownWithModifers(JNIEnv *env,
                                                                       jclass clazz,
                                                                       jlong ptr, jint input,
                                                                       jint key, jint modifiers) {
    auto core = (CelestiaCore *)ptr;
    int cModifiers = convert_modifier_to_celestia_modifier(0, modifiers);
    if (input < CelestiaCore::KeyCount)
        core->charEntered(static_cast<char>(input), cModifiers);
    core->keyDown(convert_key_code_to_celestia_key(input, key), cModifiers);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1keyUp(JNIEnv *env, jclass clazz,
                                                         jlong ptr,
                                                         jint input) {

    auto core = (CelestiaCore *)ptr;
    core->keyUp(input, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1keyDown(JNIEnv *env, jclass clazz,
                                                           jlong ptr,
                                                           jint input) {
    auto core = (CelestiaCore *)ptr;
    core->keyDown(input, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1charEnter(JNIEnv *env, jclass clazz,
                                                             jlong ptr,
                                                             jint input) {
    auto core = (CelestiaCore *)ptr;
    core->charEntered((char)input, 0);
}


extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1joystickButtonDown(JNIEnv *env, jclass clazz,
                                                                      jlong ptr,
                                                                      jint button) {
    auto core = (CelestiaCore *)ptr;
    int converted = convert_joystick_button(button);
    if (converted < 0) return;
    core->joystickButton(converted, true);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1joystickButtonUp(JNIEnv *env, jclass clazz,
                                                                    jlong ptr,
                                                                    jint button) {
    auto core = (CelestiaCore *)ptr;
    int converted = convert_joystick_button(button);
    if (converted < 0) return;
    core->joystickButton(converted, false);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1joystickAxis(JNIEnv *env, jclass clazz,
                                                                jlong ptr,
                                                                jint axis,
                                                                jfloat amount) {
    auto core = (CelestiaCore *)ptr;
    core->joystickAxis(static_cast<CelestiaCore::JoyAxis>(axis), amount);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1pinchUpdate(JNIEnv *env, jclass clazz, jlong ptr, jfloat x, jfloat y, jfloat scale, jboolean zoom_fov) {
    auto core = reinterpret_cast<CelestiaCore*>(ptr);
    core->pinchUpdate(static_cast<float>(x), static_cast<float>(y), static_cast<float>(scale), static_cast<bool>(zoom_fov));
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1runScript(JNIEnv *env, jclass clazz,
                                                             jlong ptr,
                                                             jstring path) {
    auto core = (CelestiaCore *)ptr;
    const char *str = env->GetStringUTFChars(path, nullptr);
    core->runScript(str, false);
    env->ReleaseStringUTFChars(path, str);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1runDemo(JNIEnv *env, jclass clazz, jlong ptr) {
    auto core = reinterpret_cast<CelestiaCore *>(ptr);
    const auto& demoScriptFile = core->getConfig()->paths.demoScriptFile;
    if (!demoScriptFile.empty()) {
        core->cancelScript();
        core->runScript(demoScriptFile);
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_AppCore_c_1getCurrentURL(JNIEnv *env, jclass clazz,
                                                                 jlong ptr) {
    auto core = (CelestiaCore *)ptr;
    CelestiaState appState(core);
    appState.captureState();

    Url currentURL(appState, Url::CurrentVersion);
    return env->NewStringUTF(currentURL.getAsString().c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1goToURL(JNIEnv *env, jclass clazz,
                                                           jlong ptr,
                                                           jstring url) {
    auto core = (CelestiaCore *)ptr;
    const char *str = env->GetStringUTFChars(url, nullptr);
    core->goToUrl(str);
    env->ReleaseStringUTFChars(url, str);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setLocaleDirectoryPath(JNIEnv *env,
                                                                          jclass clazz,
                                                                          jstring path,
                                                                          jstring locale,
                                                                          jstring country) {
    // Set environment variable since NDK does not support locale
    const char *str = env->GetStringUTFChars(locale, nullptr);
    const char *str_cont = env->GetStringUTFChars(country, nullptr);

    setenv("LANG", str, true);

    std::string uloc = str;
    bool shouldAppendCountryCode = false;
    UErrorCode status = U_ZERO_ERROR;
    if (uloc == "zh_CN")
    {
        uloc = "zh_Hans";
        shouldAppendCountryCode = true;
    }
    else if (uloc == "zh_TW")
    {
        uloc = "zh_Hant";
        shouldAppendCountryCode = true;
    }
    else
    {
        shouldAppendCountryCode = uloc.find('_') == std::string::npos;
    }
    if (shouldAppendCountryCode)
    {
        std::string code = str_cont;
        if (code.size() == 2)
        {
            std::string fullLoc = fmt::format("{}_{}", uloc, code);
            uloc_setDefault(fullLoc.c_str(), &status);
        }
        else
        {
            uloc_setDefault(str, &status);
        }
    }
    else
    {
        uloc_setDefault(str, &status);
    }

    env->ReleaseStringUTFChars(locale, str);
    env->ReleaseStringUTFChars(country, str_cont);
    str = env->GetStringUTFChars(path, nullptr);
    bindtextdomain("celestia", str);
    bind_textdomain_codeset("celestia", "UTF-8");
    bindtextdomain("celestia-data", str);
    bind_textdomain_codeset("celestia-data", "UTF-8");
    bindtextdomain("celestia_ui", str);
    bind_textdomain_codeset("celestia_ui", "UTF-8");
    textdomain("celestia");
    env->ReleaseStringUTFChars(path, str);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setUpLocale(JNIEnv *env, jclass clazz) {
    static bool isLocaleSet = false;
    if (isLocaleSet)
        return;

    isLocaleSet = true;
    CelestiaCore::initLocale();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_AppCore_c_1getLocalizedString(JNIEnv *env,
                                                                      jclass clazz,
                                                                      jstring string,
                                                                      jstring domain) {
    const char *c_str = env->GetStringUTFChars(string, nullptr);
    const char *c_dom = env->GetStringUTFChars(domain, nullptr);
    jstring localized;
    if (strlen(c_str) == 0)
        localized = env->NewStringUTF("");
    else
        localized = env->NewStringUTF(dgettext(c_dom, c_str));
    env->ReleaseStringUTFChars(string, c_str);
    env->ReleaseStringUTFChars(domain, c_dom);
    return localized;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_AppCore_c_1getLocalizedStringContext(JNIEnv *env,
                                                           jclass clazz,
                                                           jstring string,
                                                           jstring context,
                                                           jstring domain) {
    const char *c_str = env->GetStringUTFChars(string, nullptr);
    const char *c_dom = env->GetStringUTFChars(domain, nullptr);
    const char *c_con = env->GetStringUTFChars(context, nullptr);
    jstring localized;
    if (strlen(c_str) == 0)
    {
        localized = env->NewStringUTF("");
    }
    else
    {
        std::string auxStr = fmt::format("{}\004{}", c_con, c_str);
        const char *aux = auxStr.c_str();
        const char *translation = dgettext(c_dom, aux);
        if (translation == aux)
            localized = env->NewStringUTF(c_str);
        else
            localized = env->NewStringUTF(translation);
    }
    env->ReleaseStringUTFChars(string, c_str);
    env->ReleaseStringUTFChars(domain, c_dom);
    env->ReleaseStringUTFChars(context, c_con);
    return localized;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_AppCore_c_1getLocalizedFilename(JNIEnv *env,
                                                                        jclass clazz,
                                                                        jstring string) {
    using namespace celestia::util;
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
Java_space_celestia_celestia_AppCore_c_1getShow##flag (JNIEnv *env, jclass clazz, jlong pointer) { \
    auto core = (CelestiaCore *)pointer; \
    return static_cast<jboolean>(celestia::util::is_set(core->getRenderer()->getRenderFlags(), RenderFlags::Show##flag) ? JNI_TRUE : JNI_FALSE); \
} \
extern "C" \
JNIEXPORT void JNICALL \
Java_space_celestia_celestia_AppCore_c_1setShow##flag (JNIEnv *env, jclass clazz, jlong pointer, \
                                                                        jboolean value) { \
    auto core = (CelestiaCore *)pointer;                          \
    auto flags = core->getRenderer()->getRenderFlags();           \
    celestia::util::set_or_unset(flags, RenderFlags::Show##flag, static_cast<bool>(value)); \
    core->getRenderer()->setRenderFlags(flags); \
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
RENDERMETHODS(FadingOrbits)
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
RENDERMETHODS(Ecliptic)

#define LABELMETHODS(flag) extern "C" JNIEXPORT jboolean JNICALL \
Java_space_celestia_celestia_AppCore_c_1getShow##flag##Labels (JNIEnv *env, jclass clazz, jlong pointer) { \
    auto core = reinterpret_cast<CelestiaCore*>(pointer);        \
    return static_cast<jboolean>(celestia::util::is_set(core->getRenderer()->getLabelMode(), RenderLabels::flag##Labels) ? JNI_TRUE : JNI_FALSE); \
} \
extern "C" \
JNIEXPORT void JNICALL \
Java_space_celestia_celestia_AppCore_c_1setShow##flag##Labels (JNIEnv *env, jclass clazz, jlong pointer, \
                                                                        jboolean value) { \
    auto core = reinterpret_cast<CelestiaCore*>(pointer);        \
    auto flags = core->getRenderer()->getLabelMode();            \
    celestia::util::set_or_unset(flags, RenderLabels::flag##Labels, static_cast<bool>(value)); \
    core->getRenderer()->setLabelMode(flags); \
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
Java_space_celestia_celestia_AppCore_c_1getShow##flag##Orbits (JNIEnv *env, jclass clazz, jlong pointer) { \
    auto core = reinterpret_cast<CelestiaCore *>(pointer); \
    return celestia::util::is_set(core->getRenderer()->getOrbitMask(), BodyClassification::flag) ? JNI_TRUE : JNI_FALSE; \
} \
extern "C" \
JNIEXPORT void JNICALL \
Java_space_celestia_celestia_AppCore_c_1setShow##flag##Orbits (JNIEnv *env, jclass clazz, jlong pointer, \
                                                                        jboolean value) { \
    auto core = reinterpret_cast<CelestiaCore *>(pointer); \
    auto flags = core->getRenderer()->getOrbitMask();            \
    celestia::util::set_or_unset(flags, BodyClassification::flag, static_cast<bool>(value)); \
    core->getRenderer()->setOrbitMask(flags); \
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
Java_space_celestia_celestia_AppCore_c_1getShow##flag##Labels (JNIEnv *env, jclass clazz, jlong pointer) { \
    auto core = reinterpret_cast<CelestiaCore*>(pointer); \
    return static_cast<jboolean>(((core->getSimulation()->getObserver().getLocationFilter() & Location::flag) == 0) ? JNI_FALSE : JNI_TRUE); \
} \
extern "C" \
JNIEXPORT void JNICALL \
Java_space_celestia_celestia_AppCore_c_1setShow##flag##Labels (JNIEnv *env, jclass clazz, jlong pointer, \
                                                                        jboolean value) { \
    auto core = reinterpret_cast<CelestiaCore*>(pointer); \
    core->getSimulation()->getObserver().setLocationFilter(bit_mask_value_update(value, Location::flag, core->getSimulation()->getObserver().getLocationFilter())); \
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
FEATUREMETHODS(Other)

#define INTERACTIONMETHODS(flag) extern "C" JNIEXPORT jboolean JNICALL \
Java_space_celestia_celestia_AppCore_c_1getEnable##flag (JNIEnv *env, jclass clazz, jlong pointer) { \
    auto core = reinterpret_cast<CelestiaCore*>(pointer); \
    return celestia::util::is_set(core->getInteractionFlags(), CelestiaCore::InteractionFlags::flag) ? JNI_TRUE : JNI_FALSE; \
} \
extern "C" \
JNIEXPORT void JNICALL \
Java_space_celestia_celestia_AppCore_c_1setEnable##flag (JNIEnv *env, jclass clazz, jlong pointer, \
                                                                        jboolean value) { \
    auto core = reinterpret_cast<CelestiaCore*>(pointer); \
    auto flags = core->getInteractionFlags(); \
    celestia::util::set_or_unset(flags, CelestiaCore::InteractionFlags::flag, static_cast<bool>(value));                                                                      \
    core->setInteractionFlags(flags); \
} \

INTERACTIONMETHODS(ReverseWheel)
INTERACTIONMETHODS(RayBasedDragging)
INTERACTIONMETHODS(FocusZooming)

#define OBSERVERMETHODS(flag) extern "C" JNIEXPORT jboolean JNICALL \
Java_space_celestia_celestia_AppCore_c_1getEnable##flag (JNIEnv *env, jclass clazz, jlong pointer) { \
    auto core = reinterpret_cast<CelestiaCore*>(pointer); \
    return celestia::util::is_set(core->getObserverFlags(), celestia::engine::ObserverFlags::flag) ? JNI_TRUE : JNI_FALSE; \
} \
extern "C" \
JNIEXPORT void JNICALL \
Java_space_celestia_celestia_AppCore_c_1setEnable##flag (JNIEnv *env, jclass clazz, jlong pointer, \
                                                                        jboolean value) { \
    auto core = reinterpret_cast<CelestiaCore*>(pointer); \
    auto flags = core->getObserverFlags(); \
    celestia::util::set_or_unset(flags, celestia::engine::ObserverFlags::flag, static_cast<bool>(value));                                                                      \
    core->setObserverFlags(flags); \
}                                                                   \

OBSERVERMETHODS(AlignCameraToSurfaceOnLand)

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setResolution(JNIEnv *env, jclass clazz, jlong pointer,
                                                                 jint value) {
    auto core = (CelestiaCore *)pointer;
    core->getRenderer()->setResolution(static_cast<TextureResolution>(value));
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_AppCore_c_1getResolution(JNIEnv *env, jclass clazz, jlong pointer) {
    auto core = (CelestiaCore *)pointer;
    return static_cast<jint>(core->getRenderer()->getResolution());
}

extern "C" JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setHudDetail(JNIEnv *env, jclass clazz, jlong pointer, jint value) {
    auto core = (CelestiaCore *)pointer;
    core->setHudDetail(value);
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_AppCore_c_1getHudDetail(JNIEnv *env, jclass clazz, jlong pointer) {
    auto core = (CelestiaCore *)pointer;
    return core->getHudDetail();
}

extern "C" JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setMeasurementSystem(JNIEnv *env, jclass clazz, jlong pointer, jint value) {
    auto core = (CelestiaCore *)pointer;
    core->setMeasurementSystem(static_cast<celestia::MeasurementSystem>(value));
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_AppCore_c_1getMeasurementSystem(JNIEnv *env, jclass clazz, jlong pointer) {
    auto core = (CelestiaCore *)pointer;
    return (jint)core->getMeasurementSystem();
}


extern "C" JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setTemperatureScale(JNIEnv *env, jclass clazz, jlong pointer, jint value) {
    auto core = (CelestiaCore *)pointer;
    core->setTemperatureScale(static_cast<celestia::TemperatureScale>(value));
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_AppCore_c_1getTemperatureScale(JNIEnv *env, jclass clazz, jlong pointer) {
    auto core = (CelestiaCore *)pointer;
    return (jint)core->getTemperatureScale();
}

extern "C" JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setTimeZone(JNIEnv *env, jclass clazz, jlong pointer, jint value) {
    auto core = (CelestiaCore *)pointer;
    core->setTimeZoneBias(0 == value ? 1 : 0);
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_AppCore_c_1getTimeZone(JNIEnv *env, jclass clazz, jlong pointer) {
    auto core = (CelestiaCore *)pointer;
    return core->getTimeZoneBias() == 0 ? 1 : 0;
}

extern "C" JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setDateFormat(JNIEnv *env, jclass clazz, jlong pointer, jint value) {
    auto core = reinterpret_cast<CelestiaCore*>(pointer);
    core->setDateFormat(static_cast<celestia::astro::Date::Format>(value));
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_AppCore_c_1getDateFormat(JNIEnv *env, jclass clazz, jlong pointer) {
    auto core = (CelestiaCore *)pointer;
    return core->getDateFormat();
}

extern "C" JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setStarStyle(JNIEnv *env, jclass clazz, jlong pointer, jint value) {
    auto core = (CelestiaCore *)pointer;
    core->getRenderer()->setStarStyle(static_cast<StarStyle>(value));
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_AppCore_c_1getStarStyle(JNIEnv *env, jclass clazz, jlong pointer) {
    auto core = (CelestiaCore *)pointer;
    return static_cast<jint>(core->getRenderer()->getStarStyle());
}

extern "C" JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setStarColors(JNIEnv *env, jclass clazz, jlong pointer, jint value) {
    auto core = reinterpret_cast<CelestiaCore *>(pointer);
    core->getRenderer()->setStarColorTable(static_cast<ColorTableType>(value));
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_AppCore_c_1getStarColors(JNIEnv *env, jclass clazz, jlong pointer) {
    auto core = reinterpret_cast<CelestiaCore *>(pointer);
    return static_cast<jint>(core->getRenderer()->getStarColorTable());
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setTintSaturation(JNIEnv *env, jclass clazz, jlong pointer,
                                                          jfloat tint_saturation) {
    auto core = reinterpret_cast<CelestiaCore *>(pointer);
    core->getRenderer()->setTintSaturation(tint_saturation);
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_space_celestia_celestia_AppCore_c_1getTintSaturation(JNIEnv *env, jclass clazz,
                                                          jlong pointer) {
    auto core = reinterpret_cast<CelestiaCore *>(pointer);
    return core->getRenderer()->getTintSaturation();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_AppCore_c_1getRenderInfo(JNIEnv *env, jclass clazz, jlong ptr) {
    auto core = (CelestiaCore *)ptr;
    return env->NewStringUTF(Helper::getRenderInfo(core->getRenderer()).c_str());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_AppCore_c_1saveScreenshot(JNIEnv *env, jclass clazz, jlong ptr, jstring file_path, int image_type)
{
    auto core = (CelestiaCore *)ptr;
    const char *c_path = env->GetStringUTFChars(file_path, nullptr);
    bool result = core->saveScreenShot(c_path, (ContentType)image_type);
    env->ReleaseStringUTFChars(file_path, c_path);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setAmbientLightLevel(JNIEnv *env, jclass clazz, jlong pointer, jdouble ambient_light_level) {
    auto core = (CelestiaCore *)pointer;
    core->getRenderer()->setAmbientLightLevel((float)ambient_light_level);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_AppCore_c_1getAmbientLightLevel(JNIEnv *env, jclass clazz, jlong pointer) {
    auto core = (CelestiaCore *)pointer;
    return core->getRenderer()->getAmbientLightLevel();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setFaintestVisible(JNIEnv *env, jclass clazz, jlong pointer, jdouble faintest_visible) {
    auto core = (CelestiaCore *)pointer;
    if (!celestia::util::is_set(core->getRenderer()->getRenderFlags(), RenderFlags::ShowAutoMag))
    {
        core->setFaintest(static_cast<float>(faintest_visible));
    }
    else
    {
        core->getRenderer()->setFaintestAM45deg(static_cast<float>(faintest_visible));
        core->setFaintestAutoMag();
    }
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_AppCore_c_1getFaintestVisible(JNIEnv *env, jclass clazz, jlong pointer) {
    auto core = (CelestiaCore *)pointer;
    if (!celestia::util::is_set(core->getRenderer()->getRenderFlags(), RenderFlags::ShowAutoMag))
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
Java_space_celestia_celestia_AppCore_c_1setGalaxyBrightness(JNIEnv *env, jclass clazz, jlong pointer, jdouble galaxy_brightness) {
    Galaxy::setLightGain((float)galaxy_brightness);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_AppCore_c_1getGalaxyBrightness(JNIEnv *env, jclass clazz, jlong pointer) {
    return Galaxy::getLightGain();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setMinimumFeatureSize(JNIEnv *env, jclass clazz, jlong pointer, jdouble minimum_feature_size) {
    auto core = (CelestiaCore *)pointer;
    core->getRenderer()->setMinimumFeatureSize((float)minimum_feature_size);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_celestia_AppCore_c_1getMinimumFeatureSize(JNIEnv *env, jclass clazz, jlong pointer) {
    auto core = (CelestiaCore *)pointer;
    return core->getRenderer()->getMinimumFeatureSize();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setFont(JNIEnv *env, jclass clazz,
                                                           jlong ptr,
                                                           jstring font_path,
                                                           jint collection_index,
                                                           jint font_size) {
    auto core = (CelestiaCore *)ptr;
    const char *c_path = env->GetStringUTFChars(font_path, nullptr);
    core->setHudFont(c_path, collection_index, font_size);
    env->ReleaseStringUTFChars(font_path, c_path);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setTitleFont(JNIEnv *env, jclass clazz,
                                                                jlong ptr,
                                                                jstring font_path,
                                                                jint collection_index,
                                                                jint font_size) {
    auto core = (CelestiaCore *)ptr;
    const char *c_path = env->GetStringUTFChars(font_path, nullptr);
    core->setHudTitleFont(c_path, collection_index, font_size);
    env->ReleaseStringUTFChars(font_path, c_path);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setRendererFont(JNIEnv *env, jclass clazz,
                                                                   jlong ptr,
                                                                   jstring font_path,
                                                                   jint collection_index,
                                                                   jint font_size,
                                                                   jint font_style) {
    auto core = (CelestiaCore *)ptr;
    const char *c_path = env->GetStringUTFChars(font_path, nullptr);
    core->setRendererFont(c_path, collection_index, font_size, (Renderer::FontStyle)font_style);
    env->ReleaseStringUTFChars(font_path, c_path);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_AppCore_c_1getReferenceMarkEnabled(JNIEnv *env,
                                                                           jclass clazz,
                                                                           jlong ptr,
                                                                           jstring str) {
    auto core = (CelestiaCore *)ptr;
    const char *c_str = env->GetStringUTFChars(str, nullptr);
    bool enabled = core->referenceMarkEnabled(c_str);
    env->ReleaseStringUTFChars(str, c_str);

    return enabled ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1toggleReferenceMarkEnabled(JNIEnv *env,
                                                                              jclass clazz,
                                                                              jlong ptr,
                                                                              jstring str) {
    auto core = (CelestiaCore *)ptr;
    const char *c_str = env->GetStringUTFChars(str, nullptr);
    core->toggleReferenceMark(c_str);
    env->ReleaseStringUTFChars(str, c_str);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setPickTolerance(JNIEnv *env,
                                                                    jclass clazz, jlong ptr,
                                                                    jfloat pick_tolerance) {
    auto core = (CelestiaCore *)ptr;
    core->setPickTolerance((float)pick_tolerance);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_celestia_AppCore_c_1getLanguage(JNIEnv *env, jclass clazz) {
    const char *lang = dgettext("celestia", "LANGUAGE");
    if (strcmp(lang, "LANGUAGE") == 0)
        return env->NewStringUTF("en");
    return env->NewStringUTF(lang);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setScriptSystemAccessPolicy(JNIEnv *env, jclass clazz,
                                                                    jlong pointer,
                                                                    jint script_system_access_policy) {
    auto core = (CelestiaCore *)pointer;
    core->setScriptSystemAccessPolicy((CelestiaCore::ScriptSystemAccessPolicy)script_system_access_policy);
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_AppCore_c_1getScriptSystemAccessPolicy(JNIEnv *env, jclass clazz,
                                                                    jlong pointer) {
    auto core = (CelestiaCore *)pointer;
    return (jint)core->getScriptSystemAccessPolicy();
}


extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_AppCore_c_1getWidth(JNIEnv *env, jclass clazz, jlong ptr) {
    auto core = reinterpret_cast<CelestiaCore *>(ptr);
    auto [width, _] = core->getWindowDimension();
    return static_cast<jint>(width);
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_AppCore_c_1getHeight(JNIEnv *env, jclass clazz, jlong ptr) {
    auto core = reinterpret_cast<CelestiaCore *>(ptr);
    auto [_, height] = core->getWindowDimension();
    return static_cast<jint>(height);
}

extern "C"
JNIEXPORT jint JNICALL
Java_space_celestia_celestia_AppCore_c_1getLayoutDirection(JNIEnv *env, jclass clazz, jlong ptr) {
    auto core = reinterpret_cast<CelestiaCore *>(ptr);
    return static_cast<jint>(core->getLayoutDirection());
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_AppCore_c_1setLayoutDirection(JNIEnv *env, jclass clazz, jlong ptr,
                                                           jint layout_direction) {
    auto core = reinterpret_cast<CelestiaCore *>(ptr);
    core->setLayoutDirection(static_cast<celestia::LayoutDirection>(layout_direction));
}