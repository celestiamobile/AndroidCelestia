#pragma once

#include <jni.h>
#include <android/log.h>
#include <EGL/egl.h>
#include <GLES3/gl3.h>
#include <array>
#include <vector>

// These defines must come before the OpenXR headers
#define XR_USE_PLATFORM_ANDROID
#define XR_USE_GRAPHICS_API_OPENGL_ES

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

    // EGL context created by the app before calling createSession()
    EGLDisplay eglDisplay = EGL_NO_DISPLAY;
    EGLContext eglContext = EGL_NO_CONTEXT;
    EGLConfig  eglConfig  = nullptr;

    bool sessionRunning  = false;
    bool exitRequested   = false;

    // OpenGL basic rendering state
    GLuint shaderProgram = 0;
    GLuint vao = 0;
    GLuint vbo = 0;
    GLint modelViewProjLoc = -1;

    // Pointer to CelestiaCore
    class CelestiaCore* core = nullptr;
    void setCorePointer(CelestiaCore* c) { core = c; }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    bool init(JavaVM* vm, jobject activityObject);
    void start();
    void stop();
    void destroy();

    // ── Frame loop ────────────────────────────────────────────────────────────
    // Returns false when the app should exit.
    bool pollEvents();
    void renderFrame();

private:
    bool createInstance(JavaVM* vm, jobject activity);
    bool acquireSystem();
    bool createEGLContext();
    bool createSession();
    bool createSwapchains();
    bool initGraphics();
    void destroyGraphics();

    void handleSessionStateChange(XrSessionState newState);
    void renderEye(int eyeIndex,
                   const XrSwapchainImageOpenGLESKHR& image,
                   const XrView& view,
                   XrCompositionLayerProjectionView& projView);
};
