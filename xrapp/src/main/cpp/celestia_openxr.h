#pragma once

#include <jni.h>
#include <android/log.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
#include <array>
#include <vector>
#include <thread>
#include <mutex>
#include <condition_variable>

#include <openxr/openxr.h>
#include <openxr/openxr_platform.h>

// ── Logging helpers ───────────────────────────────────────────────────────────
#define LOG_TAG "CelestiaNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Check an XrResult and log on failure (non-fatal; caller decides what to do)
#define XR_CHECK(result, msg)                                              \
    do {                                                                   \
        XrResult _r = (result);                                            \
        if (XR_FAILED(_r)) {                                               \
            LOGE("OpenXR error in %s: result=%d", (msg), (int)(_r));       \
        }                                                                  \
    } while (0)

// ── Per-eye swapchain ─────────────────────────────────────────────────────────
struct EyeSwapchain {
    XrSwapchain                              handle    = XR_NULL_HANDLE;
    int32_t                                  width     = 0;
    int32_t                                  height    = 0;
    std::vector<XrSwapchainImageOpenGLESKHR> images;

    // Cached GL resources (one depth RB shared, one FBO per swapchain image)
    GLuint              depthRenderbuffer = 0;
    std::vector<GLuint> framebuffers;

    void destroyGLResources() {
        if (!framebuffers.empty()) {
            glDeleteFramebuffers((GLsizei)framebuffers.size(), framebuffers.data());
            framebuffers.clear();
        }
        if (depthRenderbuffer != 0) {
            glDeleteRenderbuffers(1, &depthRenderbuffer);
            depthRenderbuffer = 0;
        }
    }
};

// ── Main OpenXR context ───────────────────────────────────────────────────────
struct CelestiaOpenXR {

    // Core OpenXR handles
    XrInstance   instance  = XR_NULL_HANDLE;
    XrSystemId   systemId  = XR_NULL_SYSTEM_ID;
    XrSession    session   = XR_NULL_HANDLE;
    XrSpace      appSpace  = XR_NULL_HANDLE;   // LOCAL reference space

    // Stereo swapchains (index 0 = left, 1 = right)
    std::array<EyeSwapchain, 2> eyeSwapchains;

    // EGL context
    EGLDisplay eglDisplay = EGL_NO_DISPLAY;
    EGLContext eglContext = EGL_NO_CONTEXT;
    EGLConfig  eglConfig  = nullptr;
    EGLSurface pbufferSurface = EGL_NO_SURFACE;

    bool sessionRunning  = false;
    bool exitRequested   = false;

    // Pointer to CelestiaCore
    void* corePtr = nullptr;
    void setCorePointer(void* c) { corePtr = c; }
    
    // Background Threading
    std::thread renderThread;
    std::mutex stateMutex;
    std::condition_variable stateCv;
    bool active = false;
    bool resumed = false;
    bool celestiaInitialized = false;

    JavaVM* jvm = nullptr;
    jobject activityGlobal = nullptr;

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    bool init(JavaVM* vm, jobject activityObject);
    void start();
    void stop();
    void destroy();

    // ── Frame loop ────────────────────────────────────────────────────────────
    void renderLoop();
    void renderFrame();
    bool pollEvents();

private:
    bool createInstance();
    bool acquireSystem();
    bool createEGLContext();
    bool createSession();
    bool createSwapchains();

    void initCelestiaIfNeeded(JNIEnv* env);

    void handleSessionStateChange(XrSessionState newState);
    void renderEye(int eyeIndex,
                   const XrSwapchainImageOpenGLESKHR& image,
                   const XrView& view,
                   XrCompositionLayerProjectionView& projView);
};
