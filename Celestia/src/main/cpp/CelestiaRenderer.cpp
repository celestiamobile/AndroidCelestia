// CelestiaRenderer.cpp
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

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

struct CelestiaSurface
{
    ANativeWindow *window{ nullptr };
    EGLSurface surface{ EGL_NO_SURFACE };
    int windowWidth{ 0 };
    int windowHeight{ 0 };
};

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
    void setSurface(JNIEnv *env, jobject surface, bool presentation);
    void setSize(int width, int height, bool presentation);
    void setCorePointer(CelestiaCore *core);
    void makeContextCurrent();
    void setFrameRateOption(int frameRateOption);
    inline void setHasPendingTasks(bool h);

    // Offscreen rendering helpers
    void setupOffscreenBuffers();
    void cleanupOffscreenBuffers();
    void initQuadShader();
    void drawTextureToScreen(unsigned int texture);

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
    CelestiaSurface surface;
    CelestiaSurface presentationSurface;
    EGLContext context = EGL_NO_CONTEXT;
    EGLConfig config { nullptr };
    EGLint format {};
    int sampleCount { 0 };

    static JavaVM *jvm;
    static jmethodID flushTasksMethod;
    static jmethodID engineStartedMethod;

private:
    bool suspendedFlag = false;
    pthread_t threadId {};
    pthread_mutex_t msgMutex {};
    pthread_cond_t resumeCond {};

    int currentWindowWidth{ 0 };
    int currentWindowHeight{ 0 };
    bool hasPendingTasks{ false };

    // Offscreen rendering members
    unsigned int offscreenFbo{ 0 };
    unsigned int offscreenTexture{ 0 };
    unsigned int offscreenDepthRb{ 0 };
    int offscreenWidth{ 0 };
    int offscreenHeight{ 0 };

    // Quad shader members
    unsigned int quadProgram{ 0 };
    int avPosition{ -1 };
    int avTexCoord{ -1 };
    int usTexture{ -1 };
    unsigned int quadVbo{ 0 };

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

        const EGLint configCount = 64;
        EGLConfig configs[configCount];
        EGLint numConfigs;
        if (enableMultisample) {
            // Try to enable multisample but fallback if not available
            if (eglChooseConfig(display, multisampleAttribs, configs, configCount, &numConfigs)) {
                for (EGLint i = 0; i < numConfigs; ++i) {
                    if (eglGetConfigAttrib(display, configs[i], EGL_NATIVE_VISUAL_ID, &format)) {
                        config = configs[i];
                        EGLint numSamples;
                        if (eglGetConfigAttrib(display, config, EGL_SAMPLES, &numSamples) && numSamples > 1)
                            sampleCount = numSamples;
                        break;
                    } else {
                        LOG_ERROR("eglGetConfigAttrib() returned error %d", eglGetError());
                    }
                }
            } else {
                LOG_ERROR("eglChooseConfig() returned error %d", eglGetError());
            }
        }

        if (config == nullptr) {
            if (!eglChooseConfig(display, attribs, configs, configCount, &numConfigs)) {
                LOG_ERROR("eglChooseConfig() returned error %d", eglGetError());
                destroy();
                return false;
            }

            for (EGLint i = 0; i < numConfigs; ++i) {
                if (eglGetConfigAttrib(display, configs[i], EGL_NATIVE_VISUAL_ID, &format)) {
                    config = configs[i];
                    break;
                } else {
                    LOG_ERROR("eglGetConfigAttrib() returned error %d", eglGetError());
                }
            }
        }

        if (config == nullptr) {
            LOG_ERROR("No suitable EGLConfig found");
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

    if (surface.surface != EGL_NO_SURFACE) {
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroySurface(display, surface.surface);
        surface.surface = EGL_NO_SURFACE;
    }

    if (presentationSurface.surface != EGL_NO_SURFACE) {
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroySurface(display, presentationSurface.surface);
        presentationSurface.surface = EGL_NO_SURFACE;
    }

    if (surface.window != nullptr) {
        ANativeWindow_setBuffersGeometry(surface.window, 0, 0, format);
        if (!(surface.surface = eglCreateWindowSurface(display, config, surface.window, nullptr))) {
            LOG_ERROR("eglCreateWindowSurface() returned error %d", eglGetError());
            destroy();
            return false;
        }

        if (!eglMakeCurrent(display, surface.surface, surface.surface, context)) {
            LOG_ERROR("eglMakeCurrent() returned error %d", eglGetError());
            destroy();
            return false;
        }
    } else {
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    }

    if (presentationSurface.window != nullptr) {
        ANativeWindow_setBuffersGeometry(presentationSurface.window, 0, 0, format);
        if (!(presentationSurface.surface = eglCreateWindowSurface(display, config, presentationSurface.window, nullptr))) {
            LOG_ERROR("eglCreateWindowSurface() returned error %d", eglGetError());
            destroy();
            return false;
        }

        if (!eglMakeCurrent(display, presentationSurface.surface, presentationSurface.surface, context)) {
            LOG_ERROR("eglMakeCurrent() returned error %d", eglGetError());
            destroy();
            return false;
        }

        
    }
    return true;
}

void CelestiaRenderer::destroy()
{
    LOG_INFO("Destroying context");

    cleanupOffscreenBuffers();

    if (context != EGL_NO_CONTEXT)
    {
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroyContext(display, context);
        if (surface.surface != EGL_NO_SURFACE)
            eglDestroySurface(display, surface.surface);
        if (presentationSurface.surface != EGL_NO_SURFACE)
            eglDestroySurface(display, presentationSurface.surface);
        eglTerminate(display);
    }
    display = EGL_NO_DISPLAY;
    surface.surface = EGL_NO_SURFACE;
    presentationSurface.surface = EGL_NO_SURFACE;
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
    pthread_cond_signal(&resumeCond);
    unlock();
}

void CelestiaRenderer::wait()
{
    while (suspendedFlag)
        pthread_cond_wait(&resumeCond, &msgMutex);
}

void CelestiaRenderer::setSurface(JNIEnv *env, jobject m_surface, bool presentation)
{
    lock();
    CelestiaSurface &s = presentation ? presentationSurface : surface;
    msg = CelestiaRenderer::MSG_WINDOW_SET;

    if (s.surface != EGL_NO_SURFACE)
    {
        if (!eglDestroySurface(display, s.surface))
            LOG_ERROR("eglDestroySurface() returned error %d", eglGetError());
        else
            s.surface = EGL_NO_SURFACE;
    }

    // Release current window
    if (s.window)
        ANativeWindow_release(s.window);

    if (m_surface)
        s.window = ANativeWindow_fromSurface(env, m_surface);
    else
        s.window = nullptr;
    if (s.window)
        SwappyGL_setWindow(s.window);
    unlock();
}

void CelestiaRenderer::setSize(int width, int height, bool presentation)
{
    lock();
    CelestiaSurface &s = presentation ? presentationSurface : surface;
    s.windowWidth = width;
    s.windowHeight = height;
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
    EGLSurface s = presentationSurface.surface != EGL_NO_SURFACE ? presentationSurface.surface : surface.surface;
    eglMakeCurrent(display, s, s, context);
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

static const char* QUAD_VS =
    "attribute vec2 a_Position;\n"
    "attribute vec2 a_TexCoord;\n"
    "varying vec2 v_TexCoord;\n"
    "void main() {\n"
    "    gl_Position = vec4(a_Position, 0.0, 1.0);\n"
    "    v_TexCoord = a_TexCoord;\n"
    "}\n";

static const char* QUAD_FS =
    "precision mediump float;\n"
    "varying vec2 v_TexCoord;\n"
    "uniform sampler2D u_Texture;\n"
    "void main() {\n"
    "    gl_FragColor = texture2D(u_Texture, v_TexCoord);\n"
    "}\n";

void CelestiaRenderer::initQuadShader() {
    if (quadProgram != 0) return;

    unsigned int vs = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vs, 1, &QUAD_VS, nullptr);
    glCompileShader(vs);

    unsigned int fs = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fs, 1, &QUAD_FS, nullptr);
    glCompileShader(fs);

    quadProgram = glCreateProgram();
    glAttachShader(quadProgram, vs);
    glAttachShader(quadProgram, fs);
    glLinkProgram(quadProgram);

    glDeleteShader(vs);
    glDeleteShader(fs);

    avPosition = glGetAttribLocation(quadProgram, "a_Position");
    avTexCoord = glGetAttribLocation(quadProgram, "a_TexCoord");
    usTexture = glGetUniformLocation(quadProgram, "u_Texture");

    // Quad covering full screen
    float vertices[] = {
        -1.0f, -1.0f, 0.0f, 0.0f,
         1.0f, -1.0f, 1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f, 1.0f,
         1.0f,  1.0f, 1.0f, 1.0f,
    };
    glGenBuffers(1, &quadVbo);
    glBindBuffer(GL_ARRAY_BUFFER, quadVbo);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
}

void CelestiaRenderer::setupOffscreenBuffers() {
    int width = presentationSurface.windowWidth;
    int height = presentationSurface.windowHeight;

    if (width == 0 || height == 0) return;

    // Recreate buffers if size changed
    if (offscreenFbo != 0 && (offscreenWidth != width || offscreenHeight != height)) {
        cleanupOffscreenBuffers();
    }

    if (offscreenFbo == 0) {
        offscreenWidth = width;
        offscreenHeight = height;

        // Note: MSAA is disabled for offscreen rendering to maintain GLES2 compatibility.
        // MSAA is still used for direct window surface rendering (configured via EGLConfig).
        
        // Create regular FBO with texture and depth buffer
        glGenFramebuffers(1, &offscreenFbo);
        glGenTextures(1, &offscreenTexture);
        glGenRenderbuffers(1, &offscreenDepthRb);

        glBindTexture(GL_TEXTURE_2D, offscreenTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glBindRenderbuffer(GL_RENDERBUFFER, offscreenDepthRb);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height);

        glBindFramebuffer(GL_FRAMEBUFFER, offscreenFbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, offscreenTexture, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, offscreenDepthRb);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            LOG_ERROR("Framebuffer incomplete");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
}

void CelestiaRenderer::cleanupOffscreenBuffers() {
    if (offscreenFbo != 0) {
        glDeleteFramebuffers(1, &offscreenFbo);
        offscreenFbo = 0;
    }
    if (offscreenTexture != 0) {
        glDeleteTextures(1, &offscreenTexture);
        offscreenTexture = 0;
    }
    if (offscreenDepthRb != 0) {
        glDeleteRenderbuffers(1, &offscreenDepthRb);
        offscreenDepthRb = 0;
    }
    if (quadProgram != 0) {
        glDeleteProgram(quadProgram);
        quadProgram = 0;
    }
    if (quadVbo != 0) {
        glDeleteBuffers(1, &quadVbo);
        quadVbo = 0;
    }
    offscreenWidth = 0;
    offscreenHeight = 0;
}

void CelestiaRenderer::drawTextureToScreen(unsigned int texture) {
    if (quadProgram == 0) initQuadShader();

    glDisable(GL_DEPTH_TEST);
    glUseProgram(quadProgram);
    glBindBuffer(GL_ARRAY_BUFFER, quadVbo);

    glEnableVertexAttribArray(avPosition);
    glVertexAttribPointer(avPosition, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), nullptr);
    glEnableVertexAttribArray(avTexCoord);
    glVertexAttribPointer(avTexCoord, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)(2 * sizeof(float)));

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture);
    glUniform1i(usTexture, 0);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glDisableVertexAttribArray(avPosition);
    glDisableVertexAttribArray(avTexCoord);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glUseProgram(0);
    glEnable(GL_DEPTH_TEST);
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
        if (renderer->surface.surface != EGL_NO_SURFACE && !renderer->engineStartedCalled)
        {
            bool started = static_cast<bool>(newEnv->CallBooleanMethod(renderer->javaObject, CelestiaRenderer::engineStartedMethod, static_cast<jint>(renderer->sampleCount)));
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
        CelestiaSurface &s = renderer->presentationSurface.surface != EGL_NO_SURFACE ? renderer->presentationSurface : renderer->surface;
        int newWindowWidth = s.windowWidth;
        int newWindowHeight =s.windowHeight;
        if (renderer->engineStartedCalled && s.surface != EGL_NO_SURFACE && renderer->core)
            needsDrawn = true;
        renderer->unlock();

        if (renderer->engineStartedCalled && hasPendingTasks)
            newEnv->CallVoidMethod(renderer->javaObject, CelestiaRenderer::flushTasksMethod);

        if (needsDrawn)
        {
            bool hasBothSurfaces = (renderer->surface.surface != EGL_NO_SURFACE && 
                                   renderer->presentationSurface.surface != EGL_NO_SURFACE);

            // Cleanup offscreen resources if we transition from dual-surface to single-surface
            if (!hasBothSurfaces && renderer->offscreenFbo != 0) {
                renderer->cleanupOffscreenBuffers();
            }

            if (hasBothSurfaces) {
                // Both surfaces active: render to offscreen texture, then blit to both
                renderer->setupOffscreenBuffers();
                
                // Render to offscreen buffer
                glBindFramebuffer(GL_FRAMEBUFFER, renderer->offscreenFbo);
                glViewport(0, 0, renderer->presentationSurface.windowWidth, renderer->presentationSurface.windowHeight);
                renderer->resizeIfNeeded(renderer->presentationSurface.windowWidth, renderer->presentationSurface.windowHeight);
                renderer->tickAndDraw();
                
                glBindFramebuffer(GL_FRAMEBUFFER, 0);
                
                // Blit to main surface
                if (!eglMakeCurrent(renderer->display, renderer->surface.surface, renderer->surface.surface, renderer->context)) {
                    LOG_ERROR("eglMakeCurrent() for surface failed: %d", eglGetError());
                } else {
                    glViewport(0, 0, renderer->surface.windowWidth, renderer->surface.windowHeight);
                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                    renderer->drawTextureToScreen(renderer->offscreenTexture);
                    if (!SwappyGL_swap(renderer->display, renderer->surface.surface))
                        LOG_ERROR("SwappyGL_swap() for surface returned error %d", eglGetError());
                }
                
                // Blit to presentation surface
                if (!eglMakeCurrent(renderer->display, renderer->presentationSurface.surface, renderer->presentationSurface.surface, renderer->context)) {
                    LOG_ERROR("eglMakeCurrent() for presentationSurface failed: %d", eglGetError());
                } else {
                    glViewport(0, 0, renderer->presentationSurface.windowWidth, renderer->presentationSurface.windowHeight);
                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                    renderer->drawTextureToScreen(renderer->offscreenTexture);
                    if (!SwappyGL_swap(renderer->display, renderer->presentationSurface.surface))
                        LOG_ERROR("SwappyGL_swap() for presentationSurface returned error %d", eglGetError());
                }
            } else {
                // Single surface: render directly
                renderer->resizeIfNeeded(newWindowWidth, newWindowHeight);
                renderer->tickAndDraw();
                if (!SwappyGL_swap(renderer->display, s.surface))
                    LOG_ERROR("SwappyGL_swap() returned error %d", eglGetError());
            }
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
    CelestiaRenderer::engineStartedMethod = env->GetMethodID(clazz, "engineStarted", "(I)Z");
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
    renderer->enableMultisample = static_cast<bool>(enable_multisample);

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

    renderer->setSurface(env, surface, false);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1setPresentationSurface(JNIEnv *env, jobject thiz,
                                                                jlong ptr,
                                                                jobject surface) {
    auto renderer = (CelestiaRenderer *)ptr;

    renderer->setSurface(env, surface, true);
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

    renderer->setSize(width, height, false);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_celestia_Renderer_c_1setPresentationSurfaceSize(JNIEnv *env,
                                                                    jobject thiz,
                                                                    jlong ptr,
                                                                    jint width,
                                                                    jint height) {
    auto renderer = (CelestiaRenderer *)ptr;

    renderer->setSize(width, height, true);
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
    renderer->setHasPendingTasks(static_cast<bool>(has_pending_tasks));
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_space_celestia_celestia_Renderer_c_1getRenderingScaleX(JNIEnv *env, jobject thiz,
                                                             jlong pointer) {
    auto renderer = reinterpret_cast<CelestiaRenderer *>(pointer);
    // Return width ratio (presentationSurface / surface) if both surfaces exist, otherwise 1.0
    if (renderer->presentationSurface.surface != EGL_NO_SURFACE && 
        renderer->surface.surface != EGL_NO_SURFACE &&
        renderer->surface.windowWidth > 0) {
        return static_cast<jfloat>(renderer->presentationSurface.windowWidth) / 
               static_cast<jfloat>(renderer->surface.windowWidth);
    }
    return 1.0f;
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_space_celestia_celestia_Renderer_c_1getRenderingScaleY(JNIEnv *env, jobject thiz,
                                                             jlong pointer) {
    auto renderer = reinterpret_cast<CelestiaRenderer *>(pointer);
    // Return height ratio (presentationSurface / surface) if both surfaces exist, otherwise 1.0
    if (renderer->presentationSurface.surface != EGL_NO_SURFACE && 
        renderer->surface.surface != EGL_NO_SURFACE &&
        renderer->surface.windowHeight > 0) {
        return static_cast<jfloat>(renderer->presentationSurface.windowHeight) / 
               static_cast<jfloat>(renderer->surface.windowHeight);
    }
    return 1.0f;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_space_celestia_celestia_Renderer_c_1hasPresentationSurface(JNIEnv *env, jobject thiz,
                                                                 jlong pointer) {
    auto renderer = reinterpret_cast<CelestiaRenderer *>(pointer);
    return renderer->presentationSurface.surface != EGL_NO_SURFACE;
}