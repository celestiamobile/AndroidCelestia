#include <jni.h>
#include <android/keycodes.h>
#include <android/log.h>
#include <array>
#include <vector>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <cmath>

#include <celestia/celestiacore.h>
#include <celengine/perspectiveprojectionmode.h>
#include <celmath/frustum.h>

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <openxr/openxr.h>
#include <openxr/openxr_platform.h>

using namespace Eigen;

// ── Logging helpers ───────────────────────────────────────────────────────────
#define LOG_TAG "CelestiaNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define XR_CHECK(result, msg)                                              \
    do {                                                                   \
        XrResult _r = (result);                                            \
        if (XR_FAILED(_r)) {                                               \
            LOGE("OpenXR error in %s: result=%d", (msg), (int)(_r));       \
        }                                                                  \
    } while (0)

// ── Per-eye swapchain ─────────────────────────────────────────────────────────
struct EyeSwapchain {
    XrSwapchain                              handle      = XR_NULL_HANDLE;
    int32_t                                  width       = 0;
    int32_t                                  height      = 0;
    uint32_t                                 sampleCount = 1;
    std::vector<XrSwapchainImageOpenGLESKHR> images;

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

// ── Controller button table ───────────────────────────────────────────────────
struct ButtonDef {
    const char* actionName;
    const char* bindingPath;
    int         androidKeycode;
    bool        isFloat;   // true → float action thresholded at 0.5
};

static const ButtonDef kButtonDefs[] = {
    { "menu",                   "/user/hand/left/input/menu/click",           AKEYCODE_BUTTON_START,  false },
    { "button_a",               "/user/hand/right/input/a/click",             AKEYCODE_BUTTON_A,      false },
    { "button_b",               "/user/hand/right/input/b/click",             AKEYCODE_BUTTON_B,      false },
    { "button_x",               "/user/hand/left/input/x/click",              AKEYCODE_BUTTON_X,      false },
    { "button_y",               "/user/hand/left/input/y/click",              AKEYCODE_BUTTON_Y,      false },
    { "thumbstick_left_click",  "/user/hand/left/input/thumbstick/click",     AKEYCODE_BUTTON_THUMBL, false },
    { "thumbstick_right_click", "/user/hand/right/input/thumbstick/click",    AKEYCODE_BUTTON_THUMBR, false },
    { "left_trigger",           "/user/hand/left/input/trigger/value",        AKEYCODE_BUTTON_L2,     true  },
    { "right_trigger",          "/user/hand/right/input/trigger/value",       AKEYCODE_BUTTON_R2,     true  },
    { "left_grip",              "/user/hand/left/input/squeeze/value",        AKEYCODE_BUTTON_L1,     true  },
    { "right_grip",             "/user/hand/right/input/squeeze/value",       AKEYCODE_BUTTON_R1,     true  },
};
static constexpr int kButtonCount = static_cast<int>(sizeof(kButtonDefs) / sizeof(kButtonDefs[0]));

// ── Main OpenXR context ───────────────────────────────────────────────────────
struct CelestiaOpenXR {
    XrInstance   instance  = XR_NULL_HANDLE;
    XrSystemId   systemId  = XR_NULL_SYSTEM_ID;
    XrSession    session   = XR_NULL_HANDLE;
    XrSpace      appSpace  = XR_NULL_HANDLE;

    std::array<EyeSwapchain, 2> eyeSwapchains;

    EGLDisplay eglDisplay = EGL_NO_DISPLAY;
    EGLContext eglContext = EGL_NO_CONTEXT;
    EGLConfig  eglConfig  = nullptr;

    bool sessionRunning   = false;
    bool exitRequested    = false;
    bool enableMultisample = false;

    CelestiaCore* corePtr = nullptr;
    int32_t lastResizeWidth = 0;
    int32_t lastResizeHeight = 0;
    void setCorePointer(CelestiaCore* c) { corePtr = c; }

    std::thread renderThread;
    std::mutex stateMutex;
    std::condition_variable stateCv;
    bool active = false;
    bool resumed = false;
    bool hasPendingTasks = false;
    bool engineStartedCalled = false;
    inline void setHasPendingTasks(bool h) { hasPendingTasks = h; }

    JavaVM* jvm = nullptr;
    jobject activityGlobal = nullptr;
    jobject javaObject = nullptr;
    static jmethodID flushTasksMethod;
    static jmethodID engineStartedMethod;
    static jmethodID controllerButtonMethod;
    static jmethodID joystickAxisMethod;

    XrActionSet actionSet = XR_NULL_HANDLE;
    std::array<XrAction, kButtonCount> buttonActions = {};
    std::array<bool, kButtonCount>     buttonWasPressed = {};
    XrAction leftThumbstickAction  = XR_NULL_HANDLE;
    XrAction rightThumbstickAction = XR_NULL_HANDLE;

    bool init(JNIEnv* env, jobject activityObject);
    void resume();
    void start();
    void pause();
    void destroy();

    void renderLoop();
    void renderFrame(JNIEnv* env);
    bool pollEvents();

private:
    bool createInstance();
    bool acquireSystem();
    bool createEGLContext();
    bool createSession();
    bool createSwapchains();
    void createActions();

    void handleSessionStateChange(XrSessionState newState);
    void renderEye(int eyeIndex,
                   const XrSwapchainImageOpenGLESKHR& image,
                   const XrView& view,
                   XrCompositionLayerProjectionView& projView,
                   JNIEnv* env);
};

#ifndef GL_FRAMEBUFFER_SRGB
#define GL_FRAMEBUFFER_SRGB 0x8DB9
#endif

jmethodID CelestiaOpenXR::flushTasksMethod = nullptr;
jmethodID CelestiaOpenXR::engineStartedMethod = nullptr;
jmethodID CelestiaOpenXR::controllerButtonMethod = nullptr;
jmethodID CelestiaOpenXR::joystickAxisMethod = nullptr;

static void buildViewMatrix(const XrPosef& pose, float* result);

// ── Custom projection mode for asymmetric XR frustums ─────────────────────────
class CustomPerspectiveProjectionMode : public celestia::engine::PerspectiveProjectionMode
{
public:
    CustomPerspectiveProjectionMode(float left, float right, float top, float bottom, float nearZ, float farZ, float width, float height) :
        PerspectiveProjectionMode(width, height, 0, 0),
        left(left), right(right), top(top), bottom(bottom),
        nearZ(nearZ), farZ(std::isinf(farZ) ? maximumFarZ : std::min(farZ, maximumFarZ))
    {
    }

    std::tuple<float, float> getDefaultDepthRange() const override
    {
        return std::make_tuple(nearZ, farZ);
    }

    Matrix4f getProjectionMatrix(float nz, float fz, float) const override
    {
        float ratio = nz / nearZ;

        float l = ratio * left;
        float r = ratio * right;
        float t = ratio * top;
        float b = ratio * bottom;

        // https://registry.khronos.org/OpenGL-Refpages/gl2.1/xhtml/glFrustum.xml
        float A = (r + l) / (r - l);
        float B = (t + b) / (t - b);
        float C = -(fz + nz) / (fz - nz);
        float D = -2.0f * fz * nz / (fz - nz);

        Matrix4f m;

        m << 2.0f * nz / (r - l),                0.0f,     A, 0.0f,
             0.0f,                2.0f * nz / (t - b),     B, 0.0f,
             0.0f,                               0.0f,     C,    D,
             0.0f,                               0.0f, -1.0f, 0.0f;

        return m;
    }

    float getMinimumFOV() const override { return getFOV(1.0f); }
    float getMaximumFOV() const override { return getFOV(1.0f); }
    float getFOV(float zoom) const override
    {
        float a = top * top + nearZ * nearZ;
        float b = bottom * bottom + nearZ * nearZ;
        float c = (top - bottom) * (top - bottom);
        return std::acos((a + b - c) / (2.0f * std::sqrt(a * b)));
    }
    float getZoom(float fov) const override { return 1.0f; }
    celestia::math::Frustum getFrustum(float _nearZ, float _farZ, float zoom) const override {
        float ratio = _nearZ / nearZ;
        return celestia::math::Frustum(left * ratio, right * ratio, top * ratio, bottom * ratio, _nearZ, _farZ);
    }
    double getViewConeAngleMax(float zoom) const override
    {
        float a = left * left + top * top;
        float b = right * right + top * top;
        float c = left * left + bottom * bottom;
        float d = right * right + bottom * bottom;
        float maxValue = std::max({a, b, c, d});
        return static_cast<double>(nearZ) / std::sqrt(static_cast<double>(nearZ * nearZ + maxValue));
    }

    static constexpr float maximumFarZ = 1.0e9f;

    Vector3f getPickRay(float x, float y, float zoom) const override
    {
        auto invProj = getProjectionMatrix(nearZ, maximumFarZ, 1.0f).inverse();
        float aspectRatio = width / height;
        Vector4f clip(x / aspectRatio * 2.0f, y * 2.0f, -1.0, 1.0);
        return (invProj * clip).head<3>().normalized();
    }

    Vector2f getRayIntersection(Vector3f pickRay, float zoom) const override
    {
        auto proj = getProjectionMatrix(nearZ, maximumFarZ, 1.0f);
        float coeff = -nearZ / pickRay.z();
        Vector4f point(coeff * pickRay.x(), coeff * pickRay.y(), -nearZ, 1.0f);
        Vector4f projected = proj * point;
        projected /= projected.w();
        float aspectRatio = width / height;
        return Vector2f(projected.x() * aspectRatio, projected.y());
    }

private:
    float left;
    float right;
    float bottom;
    float top;
    float nearZ;
    float farZ;
};

bool CelestiaOpenXR::init(JNIEnv* env, jobject activityObject) {
    LOGI("CelestiaOpenXR::init");
    if (activityGlobal) {
        env->DeleteGlobalRef(activityGlobal);
    }
    activityGlobal = env->NewGlobalRef(activityObject);
    exitRequested = false;

    // xrInitializeLoaderKHR MUST be called on the main thread
    PFN_xrInitializeLoaderKHR xrInitializeLoaderKHR = nullptr;
    XrResult r = xrGetInstanceProcAddr(
        XR_NULL_HANDLE,
        "xrInitializeLoaderKHR",
        reinterpret_cast<PFN_xrVoidFunction*>(&xrInitializeLoaderKHR));

    if (XR_SUCCEEDED(r) && xrInitializeLoaderKHR != nullptr) {
        XrLoaderInitInfoAndroidKHR loaderInfo{XR_TYPE_LOADER_INIT_INFO_ANDROID_KHR};
        loaderInfo.applicationVM      = jvm;
        loaderInfo.applicationContext = activityGlobal;
        XR_CHECK(
            xrInitializeLoaderKHR(reinterpret_cast<XrLoaderInitInfoBaseHeaderKHR*>(&loaderInfo)),
            "xrInitializeLoaderKHR");
    } else {
        LOGW("xrInitializeLoaderKHR not available – loader may not initialise correctly");
    }

    active = true;
    resumed = false;

    renderThread = std::thread(&CelestiaOpenXR::renderLoop, this);

    // Now mark as resumed so the render loop can proceed
    start();
    return true;
}

// ── renderLoop ────────────────────────────────────────────────────────────────
void CelestiaOpenXR::renderLoop() {
    // Attach current thread to JVM
    JNIEnv* env = nullptr;
    if (jvm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
        LOGE("Failed to attach render thread to JVM");
        return;
    }

    if (!createInstance()) {
        LOGE("CelestiaOpenXR::renderLoop - createInstance() failed");
        jvm->DetachCurrentThread();
        return;
    }

    if (!acquireSystem()) {
        LOGE("CelestiaOpenXR::renderLoop - acquireSystem() failed");
        jvm->DetachCurrentThread();
        return;
    }

    if (!createSession()) {
        LOGE("CelestiaOpenXR::renderLoop - createSession() failed");
        jvm->DetachCurrentThread();
        return;
    }

    // Render loop
    while (active && !exitRequested) {
        bool localResumed;
        {
            std::unique_lock<std::mutex> lock(stateMutex);
            localResumed = resumed;
        }

        if (!localResumed) {
            std::this_thread::sleep_for(std::chrono::milliseconds(20));
            continue;
        }

        if (!pollEvents()) {
            LOGI("CelestiaOpenXR::renderLoop - pollEvents returned false, breaking loop");
            break;
        }
        
        if (sessionRunning) {
            if (!engineStartedCalled) {
                jint sampleCount = enableMultisample ? (jint)eyeSwapchains[0].sampleCount : 1;
                bool started = static_cast<bool>(env->CallBooleanMethod(javaObject, CelestiaOpenXR::engineStartedMethod, sampleCount));
                if (!started)
                    break;
                engineStartedCalled = true;
            }
            renderFrame(env);
        } else {
            std::this_thread::sleep_for(std::chrono::milliseconds(20));
        }
    }

    destroy();

    // Detach from JVM
    jvm->DetachCurrentThread();
}

// ── createInstance ────────────────────────────────────────────────────────────
bool CelestiaOpenXR::createInstance() {
    const std::vector<const char*> extensions = {
        XR_KHR_ANDROID_CREATE_INSTANCE_EXTENSION_NAME,
        XR_KHR_OPENGL_ES_ENABLE_EXTENSION_NAME,
    };

    XrInstanceCreateInfoAndroidKHR androidInfo{XR_TYPE_INSTANCE_CREATE_INFO_ANDROID_KHR};
    androidInfo.applicationVM       = jvm;
    androidInfo.applicationActivity = activityGlobal;

    XrApplicationInfo appInfo{};
    strncpy(appInfo.applicationName, "Celestia",       XR_MAX_APPLICATION_NAME_SIZE - 1);
    strncpy(appInfo.engineName,      "CelestiaEngine", XR_MAX_ENGINE_NAME_SIZE      - 1);
    appInfo.apiVersion = XR_CURRENT_API_VERSION;

    XrInstanceCreateInfo createInfo{XR_TYPE_INSTANCE_CREATE_INFO};
    createInfo.next                  = &androidInfo;
    createInfo.applicationInfo       = appInfo;
    createInfo.enabledExtensionCount = static_cast<uint32_t>(extensions.size());
    createInfo.enabledExtensionNames = extensions.data();

    XrResult r = xrCreateInstance(&createInfo, &instance);
    if (XR_FAILED(r)) {
        LOGE("xrCreateInstance failed: %d", (int)r);
        return false;
    }

    // Log runtime name
    XrInstanceProperties props{XR_TYPE_INSTANCE_PROPERTIES};
    xrGetInstanceProperties(instance, &props);
    LOGI("OpenXR runtime: %s %u.%u.%u",
         props.runtimeName,
         XR_VERSION_MAJOR(props.runtimeVersion),
         XR_VERSION_MINOR(props.runtimeVersion),
         XR_VERSION_PATCH(props.runtimeVersion));

    return true;
}

// ── acquireSystem ─────────────────────────────────────────────────────────────
bool CelestiaOpenXR::acquireSystem() {
    XrSystemGetInfo info{XR_TYPE_SYSTEM_GET_INFO};
    info.formFactor = XR_FORM_FACTOR_HEAD_MOUNTED_DISPLAY;

    XrResult r = xrGetSystem(instance, &info, &systemId);
    if (XR_FAILED(r)) {
        LOGE("xrGetSystem failed: %d", (int)r);
        return false;
    }

    XrSystemProperties props{XR_TYPE_SYSTEM_PROPERTIES};
    xrGetSystemProperties(instance, systemId, &props);
    return true;
}

// ── createEGLContext ──────────────────────────────────────────────────────────
bool CelestiaOpenXR::createEGLContext() {
    eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (eglDisplay == EGL_NO_DISPLAY) {
        LOGE("eglGetDisplay failed");
        return false;
    }

    EGLint major, minor;
    if (!eglInitialize(eglDisplay, &major, &minor)) {
        LOGE("eglInitialize failed");
        return false;
    }

    // Config attributes for OpenGL ES 3
    const EGLint configAttribs[] = {
        EGL_RED_SIZE,     8,
        EGL_GREEN_SIZE,   8,
        EGL_BLUE_SIZE,    8,
        EGL_ALPHA_SIZE,   8,
        EGL_DEPTH_SIZE,   24,
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
        EGL_NONE,
    };

    EGLint numConfigs;
    if (!eglChooseConfig(eglDisplay, configAttribs, &eglConfig, 1, &numConfigs) || numConfigs == 0) {
        LOGE("eglChooseConfig failed");
        return false;
    }

    const EGLint contextAttribs[] = {
        EGL_CONTEXT_CLIENT_VERSION, 3,
        EGL_NONE,
    };
    eglContext = eglCreateContext(eglDisplay, eglConfig, EGL_NO_CONTEXT, contextAttribs);
    if (eglContext == EGL_NO_CONTEXT) {
        LOGE("eglCreateContext failed");
        return false;
    }

    // Make context current without a surface (Quest supports surfaceless contexts)
    eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, eglContext);

    return true;
}

// ── createSession ─────────────────────────────────────────────────────────────
bool CelestiaOpenXR::createSession() {
    if (!createEGLContext()) return false;

    PFN_xrGetOpenGLESGraphicsRequirementsKHR pfnGetOpenGLESGraphicsRequirementsKHR = nullptr;
    XrResult rGetProc = xrGetInstanceProcAddr(
        instance,
        "xrGetOpenGLESGraphicsRequirementsKHR",
        reinterpret_cast<PFN_xrVoidFunction*>(&pfnGetOpenGLESGraphicsRequirementsKHR));

    if (XR_SUCCEEDED(rGetProc) && pfnGetOpenGLESGraphicsRequirementsKHR != nullptr) {
        XrGraphicsRequirementsOpenGLESKHR graphicsRequirements{XR_TYPE_GRAPHICS_REQUIREMENTS_OPENGL_ES_KHR};
        XrResult reqRes = pfnGetOpenGLESGraphicsRequirementsKHR(instance, systemId, &graphicsRequirements);
        if (XR_FAILED(reqRes)) {
            LOGE("xrGetOpenGLESGraphicsRequirementsKHR failed: %d", (int)reqRes);
            return false;
        }
    } else {
        LOGW("Failed to get xrGetOpenGLESGraphicsRequirementsKHR function pointer");
    }

    XrGraphicsBindingOpenGLESAndroidKHR gfxBinding{XR_TYPE_GRAPHICS_BINDING_OPENGL_ES_ANDROID_KHR};
    gfxBinding.display = eglDisplay;
    gfxBinding.config  = eglConfig;
    gfxBinding.context = eglContext;

    XrSessionCreateInfo sessionInfo{XR_TYPE_SESSION_CREATE_INFO};
    sessionInfo.next     = &gfxBinding;
    sessionInfo.systemId = systemId;

    XrResult r = xrCreateSession(instance, &sessionInfo, &session);
    if (XR_FAILED(r)) {
        LOGE("xrCreateSession failed: %d", (int)r);
        return false;
    }

    // Local reference space (origin at initial head position)
    XrReferenceSpaceCreateInfo spaceInfo{XR_TYPE_REFERENCE_SPACE_CREATE_INFO};
    spaceInfo.referenceSpaceType       = XR_REFERENCE_SPACE_TYPE_LOCAL;
    spaceInfo.poseInReferenceSpace     = {{0, 0, 0, 1}, {0, 0, 0}};  // identity
    XR_CHECK(xrCreateReferenceSpace(session, &spaceInfo, &appSpace), "xrCreateReferenceSpace");

    if (!createSwapchains()) return false;
    createActions();
    return true;
}

// ── createSwapchains ──────────────────────────────────────────────────────────
bool CelestiaOpenXR::createSwapchains() {
    // Query recommended render resolution
    uint32_t viewCount = 0;
    xrEnumerateViewConfigurationViews(instance, systemId,
        XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO, 0, &viewCount, nullptr);

    std::vector<XrViewConfigurationView> viewConfigs(viewCount, {XR_TYPE_VIEW_CONFIGURATION_VIEW});
    xrEnumerateViewConfigurationViews(instance, systemId,
        XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO,
        viewCount, &viewCount, viewConfigs.data());
    for (uint32_t i = 0; i < 2 && i < viewCount; ++i) {
        const auto& vc = viewConfigs[i];

        XrSwapchainCreateInfo sci{XR_TYPE_SWAPCHAIN_CREATE_INFO};
        sci.usageFlags  = XR_SWAPCHAIN_USAGE_COLOR_ATTACHMENT_BIT;
        sci.format      = GL_SRGB8_ALPHA8; // Workaround for https://communityforums.atmeta.com/discussions/dev-openxr/srgbrgb-giving-washed-outbright-image/957475
        sci.sampleCount = enableMultisample ? vc.recommendedSwapchainSampleCount : 1;
        sci.width       = vc.recommendedImageRectWidth;
        sci.height      = vc.recommendedImageRectHeight;
        sci.faceCount   = 1;
        sci.arraySize   = 1;
        sci.mipCount    = 1;

        EyeSwapchain& eye = eyeSwapchains[i];
        eye.width       = (int32_t)sci.width;
        eye.height      = (int32_t)sci.height;
        eye.sampleCount = sci.sampleCount;

        XR_CHECK(xrCreateSwapchain(session, &sci, &eye.handle), "xrCreateSwapchain");

        uint32_t imgCount = 0;
        xrEnumerateSwapchainImages(eye.handle, 0, &imgCount, nullptr);
        eye.images.resize(imgCount, {XR_TYPE_SWAPCHAIN_IMAGE_OPENGL_ES_KHR});
        xrEnumerateSwapchainImages(eye.handle, imgCount, &imgCount,
            reinterpret_cast<XrSwapchainImageBaseHeader*>(eye.images.data()));

        // Create a shared depth renderbuffer for this eye
        glGenRenderbuffers(1, &eye.depthRenderbuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, eye.depthRenderbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, eye.width, eye.height);

        // Create one FBO per swapchain image, each with color + depth attached
        eye.framebuffers.resize(imgCount);
        glGenFramebuffers((GLsizei)imgCount, eye.framebuffers.data());
        for (uint32_t j = 0; j < imgCount; ++j) {
            glBindFramebuffer(GL_FRAMEBUFFER, eye.framebuffers[j]);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                                   eye.images[j].image, 0);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                                      GL_RENDERBUFFER, eye.depthRenderbuffer);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    return true;
}

// ── createActions ─────────────────────────────────────────────────────────────
void CelestiaOpenXR::createActions() {
    XrActionSetCreateInfo actionSetInfo{XR_TYPE_ACTION_SET_CREATE_INFO};
    strncpy(actionSetInfo.actionSetName, "celestia", XR_MAX_ACTION_SET_NAME_SIZE - 1);
    strncpy(actionSetInfo.localizedActionSetName, "Celestia", XR_MAX_LOCALIZED_ACTION_SET_NAME_SIZE - 1);
    actionSetInfo.priority = 0;
    if (XR_FAILED(xrCreateActionSet(instance, &actionSetInfo, &actionSet))) {
        LOGE("xrCreateActionSet failed");
        return;
    }

    // Create one action per button entry
    std::vector<XrActionSuggestedBinding> bindings;
    bindings.reserve(kButtonCount + 2);

    for (int i = 0; i < kButtonCount; ++i) {
        const ButtonDef& def = kButtonDefs[i];
        XrActionCreateInfo actionInfo{XR_TYPE_ACTION_CREATE_INFO};
        actionInfo.actionType = def.isFloat ? XR_ACTION_TYPE_FLOAT_INPUT : XR_ACTION_TYPE_BOOLEAN_INPUT;
        strncpy(actionInfo.actionName, def.actionName, XR_MAX_ACTION_NAME_SIZE - 1);
        strncpy(actionInfo.localizedActionName, def.actionName, XR_MAX_LOCALIZED_ACTION_NAME_SIZE - 1);

        if (XR_FAILED(xrCreateAction(actionSet, &actionInfo, &buttonActions[i]))) {
            LOGE("xrCreateAction failed for %s", def.actionName);
            continue;
        }

        XrPath bindingPath = XR_NULL_PATH;
        XR_CHECK(xrStringToPath(instance, def.bindingPath, &bindingPath), def.bindingPath);
        bindings.push_back({buttonActions[i], bindingPath});
    }

    // Thumbstick vector2f actions
    struct { const char* name; const char* path; XrAction* action; } thumbsticks[] = {
        { "left_thumbstick",  "/user/hand/left/input/thumbstick",  &leftThumbstickAction  },
        { "right_thumbstick", "/user/hand/right/input/thumbstick", &rightThumbstickAction },
    };
    for (auto& t : thumbsticks) {
        XrActionCreateInfo actionInfo{XR_TYPE_ACTION_CREATE_INFO};
        actionInfo.actionType = XR_ACTION_TYPE_VECTOR2F_INPUT;
        strncpy(actionInfo.actionName, t.name, XR_MAX_ACTION_NAME_SIZE - 1);
        strncpy(actionInfo.localizedActionName, t.name, XR_MAX_LOCALIZED_ACTION_NAME_SIZE - 1);
        if (XR_FAILED(xrCreateAction(actionSet, &actionInfo, t.action))) {
            LOGE("xrCreateAction failed for %s", t.name);
            continue;
        }
        XrPath bindingPath = XR_NULL_PATH;
        XR_CHECK(xrStringToPath(instance, t.path, &bindingPath), t.path);
        bindings.push_back({*t.action, bindingPath});
    }

    XrPath oculusProfile = XR_NULL_PATH;
    XR_CHECK(xrStringToPath(instance, "/interaction_profiles/oculus/touch_controller", &oculusProfile),
             "xrStringToPath oculus profile");

    XrInteractionProfileSuggestedBinding suggested{XR_TYPE_INTERACTION_PROFILE_SUGGESTED_BINDING};
    suggested.interactionProfile     = oculusProfile;
    suggested.suggestedBindings      = bindings.data();
    suggested.countSuggestedBindings = static_cast<uint32_t>(bindings.size());
    XR_CHECK(xrSuggestInteractionProfileBindings(instance, &suggested),
             "xrSuggestInteractionProfileBindings");

    XrSessionActionSetsAttachInfo attachInfo{XR_TYPE_SESSION_ACTION_SETS_ATTACH_INFO};
    attachInfo.actionSets      = &actionSet;
    attachInfo.countActionSets = 1;
    XR_CHECK(xrAttachSessionActionSets(session, &attachInfo), "xrAttachSessionActionSets");
}

static void buildViewMatrix(const XrPosef& pose, float* result) {
    // pose is Camera-to-World. We need World-to-Camera (View Matrix) matching visionOS simd_inverse
    const float x = pose.orientation.x;
    const float y = pose.orientation.y;
    const float z = pose.orientation.z;
    const float w = pose.orientation.w;

    const float x2 = x + x, y2 = y + y, z2 = z + z;
    const float xx = x * x2,  xy = x * y2,  xz = x * z2;
    const float yy = y * y2,  yz = y * z2,  zz = z * z2;
    const float wx = w * x2,  wy = w * y2,  wz = w * z2;

    // Camera-to-World rotation matrix
    float r00 = 1.0f - (yy + zz);
    float r01 = xy - wz;
    float r02 = xz + wy;

    float r10 = xy + wz;
    float r11 = 1.0f - (xx + zz);
    float r12 = yz - wx;

    float r20 = xz - wy;
    float r21 = yz + wx;
    float r22 = 1.0f - (xx + yy);

    // Camera-to-World translation
    float tx = pose.position.x;
    float ty = pose.position.y;
    float tz = pose.position.z;

    // To get World-to-Camera (inverse of Camera-to-World), we transpose the rotation
    // and multiply the negatively translated position by the transposed rotation.
    // Transposed rotation:
    result[0] = r00;  result[4] = r10;  result[8] = r20;   result[12] = 0.0f;
    result[1] = r01;  result[5] = r11;  result[9] = r21;   result[13] = 0.0f;
    result[2] = r02;  result[6] = r12;  result[10] = r22;  result[14] = 0.0f;

    // Inverse translation: -R^T * T
    result[12] = -(r00 * tx + r10 * ty + r20 * tz);
    result[13] = -(r01 * tx + r11 * ty + r21 * tz);
    result[14] = -(r02 * tx + r12 * ty + r22 * tz);

    result[3] = 0.0f;
    result[7] = 0.0f;
    result[11] = 0.0f;
    result[15] = 1.0f;
}

// ── start / stop / resume / destroy ──────────────────────────────────────────
void CelestiaOpenXR::start() {
    std::unique_lock<std::mutex> lock(stateMutex);
    resumed = true;
    stateCv.notify_all();
}

void CelestiaOpenXR::pause() {
    std::unique_lock<std::mutex> lock(stateMutex);
    resumed = false;
}

void CelestiaOpenXR::resume() {
    LOGI("CelestiaOpenXR::resume");
    start();
}

// ── destroy ───────────────────────────────────────────────────────────────────
void CelestiaOpenXR::destroy() {
    LOGI("CelestiaOpenXR::destroy");

    {
        std::unique_lock<std::mutex> lock(stateMutex);
        exitRequested = true;
        resumed = true; // Wake up thread so it can exit
        active = false;
        stateCv.notify_all();
    }

    if (renderThread.joinable()) {
        renderThread.join();
    }

    for (auto& eye : eyeSwapchains) {
        eye.destroyGLResources();
        if (eye.handle != XR_NULL_HANDLE) {
            xrDestroySwapchain(eye.handle);
            eye.handle = XR_NULL_HANDLE;
        }
    }
    for (int i = 0; i < kButtonCount; ++i) {
        if (buttonActions[i] != XR_NULL_HANDLE) {
            xrDestroyAction(buttonActions[i]);
            buttonActions[i] = XR_NULL_HANDLE;
        }
    }
    if (leftThumbstickAction  != XR_NULL_HANDLE) { xrDestroyAction(leftThumbstickAction);  leftThumbstickAction  = XR_NULL_HANDLE; }
    if (rightThumbstickAction != XR_NULL_HANDLE) { xrDestroyAction(rightThumbstickAction); rightThumbstickAction = XR_NULL_HANDLE; }
    if (actionSet != XR_NULL_HANDLE) { xrDestroyActionSet(actionSet); actionSet = XR_NULL_HANDLE; }
    if (appSpace   != XR_NULL_HANDLE) { xrDestroySpace(appSpace);          appSpace    = XR_NULL_HANDLE; }
    if (session    != XR_NULL_HANDLE) { xrDestroySession(session);         session     = XR_NULL_HANDLE; }
    if (instance   != XR_NULL_HANDLE) { xrDestroyInstance(instance);       instance    = XR_NULL_HANDLE; }

    if (eglDisplay != EGL_NO_DISPLAY) {
        eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        if (eglContext != EGL_NO_CONTEXT) {
            eglDestroyContext(eglDisplay, eglContext);
        }
        eglTerminate(eglDisplay);
        eglDisplay = EGL_NO_DISPLAY;
        eglContext = EGL_NO_CONTEXT;
    }

    if (jvm) {
        JNIEnv* env = nullptr;
        if (jvm->GetEnv((void**)&env, JNI_VERSION_1_6) == JNI_OK) {
            if (activityGlobal) {
                env->DeleteGlobalRef(activityGlobal);
                activityGlobal = nullptr;
            }
            if (javaObject) {
                env->DeleteGlobalRef(javaObject);
                javaObject = nullptr;
            }
        }
    }

    LOGI("CelestiaOpenXR destroyed");
}

// ── pollEvents ────────────────────────────────────────────────────────────────
bool CelestiaOpenXR::pollEvents() {
    XrEventDataBuffer event{XR_TYPE_EVENT_DATA_BUFFER};

    while (xrPollEvent(instance, &event) == XR_SUCCESS) {
        switch (event.type) {
            case XR_TYPE_EVENT_DATA_SESSION_STATE_CHANGED: {
                const auto& e = reinterpret_cast<XrEventDataSessionStateChanged&>(event);
                handleSessionStateChange(e.state);
                break;
            }
            case XR_TYPE_EVENT_DATA_INSTANCE_LOSS_PENDING:
                LOGI("Instance loss pending – requesting exit");
                exitRequested = true;
                break;
            default:
                break;
        }
        event = {XR_TYPE_EVENT_DATA_BUFFER};
    }

    return !exitRequested;
}

// ── handleSessionStateChange ──────────────────────────────────────────────────
void CelestiaOpenXR::handleSessionStateChange(XrSessionState newState) {
    LOGI("Session state → %d", (int)newState);

    switch (newState) {
        case XR_SESSION_STATE_READY: {
            XrSessionBeginInfo beginInfo{XR_TYPE_SESSION_BEGIN_INFO};
            beginInfo.primaryViewConfigurationType = XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO;
            XR_CHECK(xrBeginSession(session, &beginInfo), "xrBeginSession");
            sessionRunning = true;
            LOGI("OpenXR session running");
            break;
        }
        case XR_SESSION_STATE_STOPPING:
            XR_CHECK(xrEndSession(session), "xrEndSession");
            sessionRunning = false;
            break;
        case XR_SESSION_STATE_EXITING:
        case XR_SESSION_STATE_LOSS_PENDING:
            exitRequested = true;
            break;
        default:
            break;
    }
}

// ── renderFrame ───────────────────────────────────────────────────────────────
void CelestiaOpenXR::renderFrame(JNIEnv* env) {
    if (!sessionRunning) return;

    // ── Wait ─────────────────────────────────────────────────────────────────
    XrFrameWaitInfo  waitInfo{XR_TYPE_FRAME_WAIT_INFO};
    XrFrameState     frameState{XR_TYPE_FRAME_STATE};
    XR_CHECK(xrWaitFrame(session, &waitInfo, &frameState), "xrWaitFrame");

    // ── Begin ─────────────────────────────────────────────────────────────────
    XrFrameBeginInfo beginInfo{XR_TYPE_FRAME_BEGIN_INFO};
    XR_CHECK(xrBeginFrame(session, &beginInfo), "xrBeginFrame");

    // ── Sync actions and fire button callbacks ────────────────────────────────
    if (actionSet != XR_NULL_HANDLE) {
        XrActiveActionSet activeSet{actionSet, XR_NULL_PATH};
        XrActionsSyncInfo syncInfo{XR_TYPE_ACTIONS_SYNC_INFO};
        syncInfo.activeActionSets      = &activeSet;
        syncInfo.countActiveActionSets = 1;
        XrResult syncResult = xrSyncActions(session, &syncInfo);

        if (syncResult == XR_SESSION_NOT_FOCUSED) {
            // Session lost focus (e.g. another activity is on top).
            // Synthesize releases for any buttons still considered held.
            for (int i = 0; i < kButtonCount; ++i) {
                if (buttonWasPressed[i]) {
                    env->CallVoidMethod(javaObject, CelestiaOpenXR::controllerButtonMethod,
                                        static_cast<jint>(kButtonDefs[i].androidKeycode),
                                        JNI_TRUE);
                    buttonWasPressed[i] = false;
                }
            }
            // Zero out thumbstick axes.
            env->CallVoidMethod(javaObject, CelestiaOpenXR::joystickAxisMethod, (jint)0, (jfloat)0.0f);
            env->CallVoidMethod(javaObject, CelestiaOpenXR::joystickAxisMethod, (jint)1, (jfloat)0.0f);
            env->CallVoidMethod(javaObject, CelestiaOpenXR::joystickAxisMethod, (jint)3, (jfloat)0.0f);
            env->CallVoidMethod(javaObject, CelestiaOpenXR::joystickAxisMethod, (jint)4, (jfloat)0.0f);
        } else if (!XR_FAILED(syncResult)) {
            // Session is focused – poll each button normally.
            XrActionStateGetInfo getInfo{XR_TYPE_ACTION_STATE_GET_INFO};
            for (int i = 0; i < kButtonCount; ++i) {
                if (buttonActions[i] == XR_NULL_HANDLE) continue;

                bool currentlyPressed = false;
                getInfo = {XR_TYPE_ACTION_STATE_GET_INFO};
                getInfo.action = buttonActions[i];

                if (kButtonDefs[i].isFloat) {
                    XrActionStateFloat floatState{XR_TYPE_ACTION_STATE_FLOAT};
                    if (XR_SUCCEEDED(xrGetActionStateFloat(session, &getInfo, &floatState)) && floatState.isActive)
                        currentlyPressed = floatState.currentState >= 0.5f;
                } else {
                    XrActionStateBoolean boolState{XR_TYPE_ACTION_STATE_BOOLEAN};
                    if (XR_SUCCEEDED(xrGetActionStateBoolean(session, &getInfo, &boolState)) && boolState.isActive)
                        currentlyPressed = boolState.currentState == XR_TRUE;
                }

                if (currentlyPressed != buttonWasPressed[i]) {
                    env->CallVoidMethod(javaObject, CelestiaOpenXR::controllerButtonMethod,
                                        static_cast<jint>(kButtonDefs[i].androidKeycode),
                                        static_cast<jboolean>(!currentlyPressed));
                    buttonWasPressed[i] = currentlyPressed;
                }
            }

            // Poll thumbstick axes (sent every frame; dead zone 0.1).
            auto pollThumbstick = [&](XrAction action, jint axisX, jint axisY) {
                if (action == XR_NULL_HANDLE) return;
                XrActionStateGetInfo gi{XR_TYPE_ACTION_STATE_GET_INFO};
                gi.action = action;
                XrActionStateVector2f state{XR_TYPE_ACTION_STATE_VECTOR2F};
                if (XR_FAILED(xrGetActionStateVector2f(session, &gi, &state))) return;
                float x = (state.isActive && std::abs(state.currentState.x) > 0.1f) ? state.currentState.x : 0.0f;
                float y = (state.isActive && std::abs(state.currentState.y) > 0.1f) ? state.currentState.y : 0.0f;
                env->CallVoidMethod(javaObject, CelestiaOpenXR::joystickAxisMethod, axisX, (jfloat)x);
                env->CallVoidMethod(javaObject, CelestiaOpenXR::joystickAxisMethod, axisY, (jfloat)y);
            };
            pollThumbstick(leftThumbstickAction,  0, 1);  // JOYSTICK_AXIS_X,  JOYSTICK_AXIS_Y
            pollThumbstick(rightThumbstickAction, 3, 4);  // JOYSTICK_AXIS_RX, JOYSTICK_AXIS_RY
        }
    }

    std::vector<XrCompositionLayerBaseHeader*> layers;
    XrCompositionLayerProjection               layerProj{XR_TYPE_COMPOSITION_LAYER_PROJECTION};
    std::array<XrCompositionLayerProjectionView, 2> projViews{};

    if (frameState.shouldRender) {
        // Locate eye views
        XrViewLocateInfo locateInfo{XR_TYPE_VIEW_LOCATE_INFO};
        locateInfo.viewConfigurationType = XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO;
        locateInfo.displayTime           = frameState.predictedDisplayTime;
        locateInfo.space                 = appSpace;

        XrViewState viewState{XR_TYPE_VIEW_STATE};
        std::array<XrView, 2> views{};
        views[0].type = views[1].type = XR_TYPE_VIEW;

        uint32_t viewCount = 2;
        XR_CHECK(xrLocateViews(session, &locateInfo, &viewState,
                               viewCount, &viewCount, views.data()), "xrLocateViews");

        for (int i = 0; i < 2; ++i) {
            renderEye(i, eyeSwapchains[i].images[0], views[i], projViews[i], env);
        }

        layerProj.space     = appSpace;
        layerProj.viewCount = 2;
        layerProj.views     = projViews.data();
        layers.push_back(reinterpret_cast<XrCompositionLayerBaseHeader*>(&layerProj));
    }

    // ── End ───────────────────────────────────────────────────────────────────
    XrFrameEndInfo endInfo{XR_TYPE_FRAME_END_INFO};
    endInfo.displayTime          = frameState.predictedDisplayTime;
    endInfo.environmentBlendMode = XR_ENVIRONMENT_BLEND_MODE_OPAQUE;
    endInfo.layerCount           = static_cast<uint32_t>(layers.size());
    endInfo.layers               = layers.data();
    XR_CHECK(xrEndFrame(session, &endInfo), "xrEndFrame");
}

// ── renderEye ─────────────────────────────────────────────────────────────────
void CelestiaOpenXR::renderEye(int eyeIndex,
                               const XrSwapchainImageOpenGLESKHR& image,
                               const XrView& view,
                               XrCompositionLayerProjectionView& projView,
                               JNIEnv* env) {
    const EyeSwapchain& eye = eyeSwapchains[eyeIndex];

    // Acquire swapchain image
    XrSwapchainImageAcquireInfo acquireInfo{XR_TYPE_SWAPCHAIN_IMAGE_ACQUIRE_INFO};
    uint32_t imageIndex = 0;
    XR_CHECK(xrAcquireSwapchainImage(eye.handle, &acquireInfo, &imageIndex),
             "xrAcquireSwapchainImage");

    XrSwapchainImageWaitInfo waitInfo{XR_TYPE_SWAPCHAIN_IMAGE_WAIT_INFO};
    waitInfo.timeout = XR_INFINITE_DURATION;
    XR_CHECK(xrWaitSwapchainImage(eye.handle, &waitInfo), "xrWaitSwapchainImage");

    // ── Render into the cached FBO ────────────────────────────────────────────
    glBindFramebuffer(GL_FRAMEBUFFER, eye.framebuffers[imageIndex]);
    glDisable(GL_FRAMEBUFFER_SRGB);

    if (corePtr) {
        auto* core = corePtr;

        float nearZ = 0.05f;
        float farZ = 10000.0f; // Celestia will adjust this, just a default

        float left   = nearZ * tanf(view.fov.angleLeft);
        float right  = nearZ * tanf(view.fov.angleRight);
        float bottom = nearZ * tanf(view.fov.angleDown);
        float top    = nearZ * tanf(view.fov.angleUp);

        float viewMat[16];
        buildViewMatrix(view.pose, viewMat);

        if (eyeIndex == 0) {
            if (hasPendingTasks)
                env->CallVoidMethod(javaObject, CelestiaOpenXR::flushTasksMethod);
        }

        // Set up the asymmetric projection for this eye
        core->getRenderer()->setProjectionMode(std::make_shared<CustomPerspectiveProjectionMode>(
            left, right, top, bottom, nearZ, farZ, static_cast<float>(eye.width), static_cast<float>(eye.height)));

        Matrix4f viewMatrix = Map<const Matrix4f>(viewMat);
        core->getRenderer()->setCameraTransform(viewMatrix.block<3, 3>(0, 0).cast<double>());

        if (eye.width != lastResizeWidth || eye.height != lastResizeHeight) {
            core->resize(eye.width, eye.height);
            lastResizeWidth = eye.width;
            lastResizeHeight = eye.height;
        }

        if (eyeIndex == 0) {
            core->tick();
        }

        core->draw();
    }

    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    // Release swapchain image
    XrSwapchainImageReleaseInfo releaseInfo{XR_TYPE_SWAPCHAIN_IMAGE_RELEASE_INFO};
    XR_CHECK(xrReleaseSwapchainImage(eye.handle, &releaseInfo), "xrReleaseSwapchainImage");

    // Fill projection view for the compositor
    projView = {XR_TYPE_COMPOSITION_LAYER_PROJECTION_VIEW};
    projView.pose    = view.pose;
    projView.fov     = view.fov;
    projView.subImage.swapchain              = eye.handle;
    projView.subImage.imageRect.offset       = {0, 0};
    projView.subImage.imageRect.extent       = {eye.width, eye.height};
    projView.subImage.imageArrayIndex        = 0;
}

// ── JNI bridge for XRRenderer.java ────────────────────────────────────────────
extern "C" {

JNIEXPORT jlong JNICALL
Java_space_celestia_celestia_XRRenderer_c_1createNativeXRObject(JNIEnv* /*env*/, jclass /*clazz*/) {
    LOGI("JNI: createNativeXRObject");
    return reinterpret_cast<jlong>(new CelestiaOpenXR());
}

JNIEXPORT void JNICALL
Java_space_celestia_celestia_XRRenderer_c_1initialize(JNIEnv* env, jobject thiz, jlong pointer) {
    LOGI("JNI: initialize");
    auto* xr = reinterpret_cast<CelestiaOpenXR*>(pointer);
    xr->javaObject = env->NewGlobalRef(thiz);
    env->GetJavaVM(&xr->jvm);

    jclass clazz = env->GetObjectClass(thiz);
    CelestiaOpenXR::flushTasksMethod = env->GetMethodID(clazz, "flushTasks", "()V");
    CelestiaOpenXR::engineStartedMethod = env->GetMethodID(clazz, "engineStarted", "(I)Z");
    CelestiaOpenXR::controllerButtonMethod = env->GetMethodID(clazz, "controllerButton", "(IZ)V");
    CelestiaOpenXR::joystickAxisMethod = env->GetMethodID(clazz, "joystickAxis", "(IF)V");
}

JNIEXPORT void JNICALL
Java_space_celestia_celestia_XRRenderer_c_1start(JNIEnv* env, jobject /*thiz*/, jlong pointer, jobject activity, jboolean enableMultisample) {
    LOGI("JNI: start");
    auto* xr = reinterpret_cast<CelestiaOpenXR*>(pointer);
    xr->enableMultisample = static_cast<bool>(enableMultisample);
    xr->init(env, activity);
}

JNIEXPORT void JNICALL
Java_space_celestia_celestia_XRRenderer_c_1resume(JNIEnv* /*env*/, jobject /*thiz*/, jlong pointer) {
    LOGI("JNI: resume");
    auto* xr = reinterpret_cast<CelestiaOpenXR*>(pointer);
    xr->resume();
}

JNIEXPORT void JNICALL
Java_space_celestia_celestia_XRRenderer_c_1pause(JNIEnv* /*env*/, jobject /*thiz*/, jlong pointer) {
    LOGI("JNI: pause");
    auto* xr = reinterpret_cast<CelestiaOpenXR*>(pointer);
    xr->pause();
}

JNIEXPORT void JNICALL
Java_space_celestia_celestia_XRRenderer_c_1destroy(JNIEnv* /*env*/, jobject /*thiz*/, jlong pointer) {
    LOGI("JNI: destroy");
    auto* xr = reinterpret_cast<CelestiaOpenXR*>(pointer);
    xr->destroy();
    delete xr;
}

JNIEXPORT void JNICALL
Java_space_celestia_celestia_XRRenderer_c_1setCorePointer(JNIEnv* /*env*/, jobject /*thiz*/, jlong pointer, jlong corePtr) {
    auto* xr = reinterpret_cast<CelestiaOpenXR*>(pointer);
    xr->setCorePointer(reinterpret_cast<CelestiaCore*>(corePtr));
}

JNIEXPORT void JNICALL
Java_space_celestia_celestia_XRRenderer_c_1setHasPendingTasks(JNIEnv* /*env*/, jobject /*thiz*/, jlong pointer, jboolean hasPendingTasks) {
    auto* xr = reinterpret_cast<CelestiaOpenXR*>(pointer);
    if (xr) {
        xr->setHasPendingTasks(static_cast<bool>(hasPendingTasks));
    }
}

} // extern "C"
