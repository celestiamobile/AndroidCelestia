/*
 * CelestiaRenderer.cpp
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <epoxy/egl.h>
#include <swappy/swappyGL.h>
#include <swappy/swappyGL_extra.h>
#include <celestia/celestiacore.h>

#include <android/log.h>

#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOG_ERROR(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define LOG_TAG "Renderer"

#include "CelestiaJNI.h"

#define CELESTIA_RENDERER_FRAME_MAX             0
#define CELESTIA_RENDERER_FRAME_60FPS           1
#define CELESTIA_RENDERER_FRAME_30FPS           2
#define CELESTIA_RENDERER_FRAME_20FPS           3

pthread_key_t javaEnvKey;

class CelestiaRenderer
{
public:
    CelestiaRenderer() = default;
    ~CelestiaRenderer() = default;

    bool initialize();
    void destroy();
    inline void resizeIfNeeded(int windowWidth, int windowHeight);
    inline void tickAndDraw() const;
    void start();
    void stop();
    inline void lock();
    inline void unlock();
    void pause();
    void resume();
    inline void wait();
    void setSurface(JNIEnv *env, jobject surface);
    void setSize(int width, int height);
    void setCorePointer(CelestiaCore *core);
    void makeContextCurrent();
    void setFrameRateOption(int frameRateOption);
    inline void setHasPendingTasks(bool h);

    jobject javaObject = nullptr;

    CelestiaCore *core = nullptr;

    enum RenderThreadMessage {
        MSG_NONE = 0,
        MSG_WINDOW_SET,
        MSG_RENDER_LOOP_EXIT
    };

    enum RenderThreadMessage msg = MSG_NONE;

    bool enableMultisample = false;
    bool engineStartedCalled = false;

    EGLDisplay display = EGL_NO_DISPLAY;
    EGLSurface surface = EGL_NO_SURFACE;
    EGLContext context = EGL_NO_CONTEXT;
    EGLConfig config {};
    EGLint format {};

    static JavaVM *jvm;
    static jmethodID flushTasksMethod;
    static jmethodID engineStartedMethod;

private:
    bool suspendedFlag = false;
    pthread_t threadId {};
    pthread_mutex_t msgMutex {};
    pthread_cond_t resumeCond {};

    int windowWidth{ 0 };
    int windowHeight{ 0 };
    int currentWindowWidth{ 0 };
    int currentWindowHeight{ 0 };
    bool hasPendingTasks{ false };

    ANativeWindow *window = nullptr;

    static void *threadCallback(void *self);
};

JavaVM *CelestiaRenderer::jvm = nullptr;
jmethodID CelestiaRenderer::flushTasksMethod = nullptr;
jmethodID CelestiaRenderer::engineStartedMethod = nullptr;

bool CelestiaRenderer::initialize()
{
    if (context == EGL_NO_CONTEXT)
    {
        const EGLint multisampleAttribs[] = {
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL_BLUE_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_RED_SIZE, 8,
                EGL_DEPTH_SIZE, 16,
                EGL_SAMPLES, 4,
                EGL_SAMPLE_BUFFERS, 1,
                EGL_NONE
        };
        const EGLint attribs[] = {
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL_BLUE_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_RED_SIZE, 8,
                EGL_DEPTH_SIZE, 16,
                EGL_NONE
        };

        LOG_INFO("Initializing context");

        if ((display = eglGetDisplay(EGL_DEFAULT_DISPLAY)) == EGL_NO_DISPLAY) {
            LOG_ERROR("eglGetDisplay() returned error %d", eglGetError());
            return false;
        }

        if (!eglInitialize(display, nullptr, nullptr)) {
            LOG_ERROR("eglInitialize() returned error %d", eglGetError());
            destroy();
            return false;
        }

        EGLint numConfigs;
        if (enableMultisample) {
            // Try to enable multisample but fallback if not available
            if (!eglChooseConfig(display, multisampleAttribs, &config, 1, &numConfigs) && !eglChooseConfig(display, attribs, &config, 1, &numConfigs)) {
                LOG_ERROR("eglChooseConfig() returned error %d", eglGetError());
                destroy();
                return false;
            }
        } else {
            if (!eglChooseConfig(display, attribs, &config, 1, &numConfigs)) {
                LOG_ERROR("eglChooseConfig() returned error %d", eglGetError());
                destroy();
                return false;
            }
        }

        if (!eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format)) {
            LOG_ERROR("eglGetConfigAttrib() returned error %d", eglGetError());
            destroy();
            return false;
        }

        const EGLint contextAttributes[] = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
        };

        if (!(context = eglCreateContext(display, config, nullptr, contextAttributes))) {
            LOG_ERROR("eglCreateContext() returned error %d", eglGetError());
            destroy();
            return false;
        }
    }

    if (surface != EGL_NO_SURFACE) {
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroySurface(display, surface);
        surface = EGL_NO_SURFACE;
    }

    if (window) {
        ANativeWindow_setBuffersGeometry(window, 0, 0, format);
        if (!(surface = eglCreateWindowSurface(display, config, window, nullptr))) {
            LOG_ERROR("eglCreateWindowSurface() returned error %d", eglGetError());
            destroy();
            return false;
        }

        if (!eglMakeCurrent(display, surface, surface, context)) {
            LOG_ERROR("eglMakeCurrent() returned error %d", eglGetError());
            destroy();
            return false;
        }
    } else {
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    }
    return true;
}

void CelestiaRenderer::destroy()
{
    LOG_INFO("Destroying context");

    if (context != EGL_NO_CONTEXT)
    {
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroyContext(display, context);
        if (surface != EGL_NO_SURFACE)
            eglDestroySurface(display, surface);
        eglTerminate(display);
    }
    display = EGL_NO_DISPLAY;
    surface = EGL_NO_SURFACE;
    context = EGL_NO_CONTEXT;
    currentWindowWidth = 0;
    currentWindowHeight = 0;
}

void CelestiaRenderer::resizeIfNeeded(int newWindowWidth, int newWindowHeight)
{
    if (currentWindowHeight != newWindowHeight || currentWindowWidth != newWindowWidth)
    {
        core->resize(newWindowWidth, newWindowHeight);
        currentWindowWidth = newWindowWidth;
        currentWindowHeight = newWindowHeight;
    }
}

void CelestiaRenderer::tickAndDraw() const
{
    core->tick();
    core->draw();
}

void CelestiaRenderer::start()
{
    pthread_mutex_init(&msgMutex, nullptr);
    pthread_cond_init(&resumeCond, nullptr);
    pthread_create(&threadId, nullptr, threadCallback, this);
}

void CelestiaRenderer::stop()
{
    lock();
    msg = CelestiaRenderer::MSG_RENDER_LOOP_EXIT;
    unlock();

    pthread_join(threadId, nullptr);

    pthread_mutex_destroy(&msgMutex);
    pthread_cond_destroy(&resumeCond);
}

void CelestiaRenderer::lock()
{
    pthread_mutex_lock(&msgMutex);
}

void CelestiaRenderer::unlock()
{
    pthread_mutex_unlock(&msgMutex);
}

void CelestiaRenderer::pause()
{
    lock();
    suspendedFlag = true;
    unlock();
}

void CelestiaRenderer::resume()
{
    lock();
    suspendedFlag = false;
    pthread_cond_broadcast(&resumeCond);
    unlock();
}

void CelestiaRenderer::wait()
{
    while (suspendedFlag)
        pthread_cond_wait(&resumeCond, &msgMutex);
}

void CelestiaRenderer::setSurface(JNIEnv *env, jobject m_surface)
{
    lock();
    msg = CelestiaRenderer::MSG_WINDOW_SET;

    if (surface != EGL_NO_SURFACE)
    {
        if (!eglDestroySurface(display, surface))
            LOG_ERROR("eglDestroySurface() returned error %d", eglGetError());
        else
            surface = EGL_NO_SURFACE;
    }

    // Release current window
    if (window)
        ANativeWindow_release(window);

    if (m_surface)
        window = ANativeWindow_fromSurface(env, m_surface);
    else
        window = nullptr;
    if (window)
        SwappyGL_setWindow(window);
    unlock();
}

void CelestiaRenderer::setSize(int width, int height)
{
    lock();
    windowWidth = width;
    windowHeight = height;
    unlock();
}

void CelestiaRenderer::setCorePointer(CelestiaCore *m_core)
{
    lock();
    core = m_core;
    unlock();
}

void CelestiaRenderer::makeContextCurrent()
{
    eglMakeCurrent(display, surface, surface, context);
}

void CelestiaRenderer::setHasPendingTasks(bool h)
{
    lock();
    hasPendingTasks = h;
    unlock();
}

void CelestiaRenderer::setFrameRateOption(int frameRateOption)
{
    switch (frameRateOption)
    {
        case CELESTIA_RENDERER_FRAME_20FPS:
            SwappyGL_setSwapIntervalNS(SWAPPY_SWAP_20FPS);
            break;
        case CELESTIA_RENDERER_FRAME_30FPS:
            SwappyGL_setSwapIntervalNS(SWAPPY_SWAP_30FPS);
            break;
        case CELESTIA_RENDERER_FRAME_60FPS:
            SwappyGL_setSwapIntervalNS(SWAPPY_SWAP_60FPS);
            break;
        case CELESTIA_RENDERER_FRAME_MAX:
        default:
            SwappyGL_setSwapIntervalNS(SwappyGL_getRefreshPeriodNanos());
            break;
    }
}

void *CelestiaRenderer::threadCallback(void *self)
{
    auto renderer = (CelestiaRenderer *)self;

    // Create ENV for the native thread
    JNIEnv *newEnv;
    JavaVMAttachArgs args;
    args.version = JNI_VERSION_1_6;
    args.name = nullptr;
    args.group = nullptr;
    CelestiaRenderer::jvm->AttachCurrentThread(&newEnv, &args);

    pthread_key_create(&javaEnvKey, nullptr);
    pthread_setspecific(javaEnvKey, newEnv);

    bool renderingEnabled = true;

    while (renderingEnabled)
    {
        if (renderer->surface != EGL_NO_SURFACE && !renderer->engineStartedCalled)
        {
            bool started = newEnv->CallBooleanMethod(renderer->javaObject, CelestiaRenderer::engineStartedMethod) == JNI_TRUE;
            if (!started)
                break;
            renderer->engineStartedCalled = true;
        }

        renderer->lock();
        renderer->wait();

        switch (renderer->msg)
        {
            case CelestiaRenderer::MSG_WINDOW_SET:
                renderer->initialize();
                break;
            case CelestiaRenderer::MSG_RENDER_LOOP_EXIT:
                renderingEnabled = false;
                break;
            default:
                break;
        }
        renderer->msg = CelestiaRenderer::MSG_NONE;

        bool needsDrawn = false;
        bool hasPendingTasks = renderer->hasPendingTasks;
        int newWindowWidth = renderer->windowWidth;
        int newWindowHeight = renderer->windowHeight;
        if (renderer->engineStartedCalled && renderer->surface != EGL_NO_SURFACE && renderer->core)
            needsDrawn = true;
        renderer->unlock();

        if (renderer->engineStartedCalled && hasPendingTasks)
            newEnv->CallVoidMethod(renderer->javaObject, CelestiaRenderer::flushTasksMethod);

        if (needsDrawn)
        {
            renderer->resizeIfNeeded(newWindowWidth, newWindowHeight);
            renderer->tickAndDraw();
            if (!SwappyGL_swap(renderer->display, renderer->surface))
                LOG_ERROR("SwappyGL_swap() returned error %d", eglGetError());
        }
    }
    renderer->destroy();

    // Detach
    CelestiaRenderer::jvm->DetachCurrentThread();
    pthread_exit(nullptr);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1initialize(JNIEnv *env, jobject thiz, jlong ptr) {
    auto renderer = (CelestiaRenderer *)ptr;
    renderer->javaObject = env->NewGlobalRef(thiz);
    jclass clazz = env->GetObjectClass(thiz);
    CelestiaRenderer::engineStartedMethod = env->GetMethodID(clazz, "engineStarted", "()Z");
    CelestiaRenderer::flushTasksMethod = env->GetMethodID(clazz, "flushTasks", "()V");

    env->GetJavaVM(&CelestiaRenderer::jvm);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1destroy(JNIEnv *env, jobject thiz, jlong ptr) {

    auto renderer = (CelestiaRenderer *)ptr;
    env->DeleteGlobalRef(renderer->javaObject);
    delete renderer;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1start(JNIEnv *env, jobject thiz,
                                                          jlong ptr,
                                                          jobject activity,
                                                          jboolean enable_multisample) {
    SwappyGL_init(env, activity);
    // By default, Swappy will adjust the swap interval based on actual frame rendering time.
    SwappyGL_setAutoSwapInterval(false);

    LOG_INFO("Creating renderer thread");

    auto renderer = (CelestiaRenderer *)ptr;
    renderer->enableMultisample = enable_multisample == JNI_TRUE;

    renderer->start();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1stop(JNIEnv *env, jobject thiz, jlong ptr) {
    auto renderer = (CelestiaRenderer *)ptr;

    LOG_INFO("Stopping renderer thread");
    renderer->stop();
    LOG_INFO("Renderer thread stopped");

    SwappyGL_destroy();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1pause(JNIEnv *env, jobject thiz, jlong ptr) {
    auto renderer = (CelestiaRenderer *)ptr;

    renderer->pause();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1resume(JNIEnv *env, jobject thiz, jlong ptr) {
    auto renderer = (CelestiaRenderer *)ptr;

    renderer->resume();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1setSurface(JNIEnv *env, jobject thiz,
                                                               jlong ptr,
                                                               jobject surface) {
    auto renderer = (CelestiaRenderer *)ptr;

    renderer->setSurface(env, surface);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1setCorePointer(JNIEnv *env,
                                                                   jobject thiz,
                                                                   jlong ptr,
                                                                   jlong core_ptr) {
    auto renderer = (CelestiaRenderer *)ptr;

    renderer->setCorePointer((CelestiaCore *)core_ptr);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1setSurfaceSize(JNIEnv *env,
                                                                   jobject thiz,
                                                                   jlong ptr,
                                                                   jint width,
                                                                   jint height) {
    auto renderer = (CelestiaRenderer *)ptr;

    renderer->setSize(width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1makeContextCurrent(JNIEnv *env,
                                                                       jobject thiz,
                                                                       jlong ptr) {
    auto renderer = (CelestiaRenderer *)ptr;
    renderer->makeContextCurrent();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1setFrameRateOption(JNIEnv *env,
                                                                       jobject thiz,
                                                                       jlong ptr,
                                                                       jint frame_rate_option) {
    auto renderer = (CelestiaRenderer *)ptr;

    renderer->setFrameRateOption(frame_rate_option);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_Renderer_c_1createNativeRenderObject(JNIEnv *env,
                                                                             jclass clazz) {
    return (jlong)new CelestiaRenderer;
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1setHasPendingTasks(JNIEnv *env, jobject thiz,
                                                            jlong pointer,
                                                            jboolean has_pending_tasks) {
    auto renderer = reinterpret_cast<CelestiaRenderer *>(pointer);
    renderer->setHasPendingTasks(has_pending_tasks == JNI_TRUE);
}