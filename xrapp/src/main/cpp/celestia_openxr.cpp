#include "celestia_openxr.h"

#include <cstring>
#include <string>
#include <vector>

// ── init ──────────────────────────────────────────────────────────────────────
bool CelestiaOpenXR::init(JavaVM* vm, jobject activityObject) {
    LOGI("CelestiaOpenXR::init");

    // xrInitializeLoaderKHR must be called before xrCreateInstance on Android.
    PFN_xrInitializeLoaderKHR xrInitializeLoaderKHR = nullptr;
    XrResult r = xrGetInstanceProcAddr(
        XR_NULL_HANDLE,
        "xrInitializeLoaderKHR",
        reinterpret_cast<PFN_xrVoidFunction*>(&xrInitializeLoaderKHR));

    if (XR_SUCCEEDED(r) && xrInitializeLoaderKHR != nullptr) {
        XrLoaderInitInfoAndroidKHR loaderInfo{XR_TYPE_LOADER_INIT_INFO_ANDROID_KHR};
        loaderInfo.applicationVM      = vm;
        loaderInfo.applicationContext = activityObject;
        XR_CHECK(
            xrInitializeLoaderKHR(reinterpret_cast<XrLoaderInitInfoBaseHeaderKHR*>(&loaderInfo)),
            "xrInitializeLoaderKHR");
    } else {
        LOGW("xrInitializeLoaderKHR not available – loader may not initialise correctly");
    }

    return createInstance(vm, activityObject) && acquireSystem();
}

// ── createInstance ────────────────────────────────────────────────────────────
bool CelestiaOpenXR::createInstance(JavaVM* vm, jobject activity) {
    const std::vector<const char*> extensions = {
        XR_KHR_ANDROID_CREATE_INSTANCE_EXTENSION_NAME,
        XR_KHR_OPENGL_ES_ENABLE_EXTENSION_NAME,
    };

    XrInstanceCreateInfoAndroidKHR androidInfo{XR_TYPE_INSTANCE_CREATE_INFO_ANDROID_KHR};
    androidInfo.applicationVM       = vm;
    androidInfo.applicationActivity = activity;

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
    LOGI("System: %s", props.systemName);
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

    // Create a 1×1 pbuffer surface so we can make the context current without a window
    const EGLint pbufferAttribs[] = {EGL_WIDTH, 1, EGL_HEIGHT, 1, EGL_NONE};
    EGLSurface pbuffer = eglCreatePbufferSurface(eglDisplay, eglConfig, pbufferAttribs);
    eglMakeCurrent(eglDisplay, pbuffer, pbuffer, eglContext);

    LOGI("EGL context created (OpenGL ES 3)");
    return true;
}

// ── createSession ─────────────────────────────────────────────────────────────
bool CelestiaOpenXR::createSession() {
    if (!createEGLContext()) return false;
    if (!initGraphics()) return false;

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

    LOGI("XrSession created successfully");
    return createSwapchains();
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
        sci.format      = GL_RGBA8;
        sci.sampleCount = vc.recommendedSwapchainSampleCount;
        sci.width       = vc.recommendedImageRectWidth;
        sci.height      = vc.recommendedImageRectHeight;
        sci.faceCount   = 1;
        sci.arraySize   = 1;
        sci.mipCount    = 1;

        EyeSwapchain& eye = eyeSwapchains[i];
        eye.width  = (int32_t)sci.width;
        eye.height = (int32_t)sci.height;

        XR_CHECK(xrCreateSwapchain(session, &sci, &eye.handle), "xrCreateSwapchain");

        uint32_t imgCount = 0;
        xrEnumerateSwapchainImages(eye.handle, 0, &imgCount, nullptr);
        eye.images.resize(imgCount, {XR_TYPE_SWAPCHAIN_IMAGE_OPENGL_ES_KHR});
        xrEnumerateSwapchainImages(eye.handle, imgCount, &imgCount,
            reinterpret_cast<XrSwapchainImageBaseHeader*>(eye.images.data()));

        LOGI("Swapchain[%u]: %ux%u  %u images", i, sci.width, sci.height, imgCount);
    }
    return true;
}

// ── Math Helpers ──────────────────────────────────────────────────────────────
static void buildProjectionMatrix(const XrFovf& fov, float nearZ, float farZ, float* result) {
    const float tanLeft = tanf(fov.angleLeft);
    const float tanRight = tanf(fov.angleRight);
    const float tanDown = tanf(fov.angleDown);
    const float tanUp = tanf(fov.angleUp);

    const float tanAngleWidth = tanRight - tanLeft;
    const float tanAngleHeight = tanUp - tanDown;

    result[0] = 2.0f / tanAngleWidth;
    result[4] = 0.0f;
    result[8] = (tanRight + tanLeft) / tanAngleWidth;
    result[12] = 0.0f;

    result[1] = 0.0f;
    result[5] = 2.0f / tanAngleHeight;
    result[9] = (tanUp + tanDown) / tanAngleHeight;
    result[13] = 0.0f;

    result[2] = 0.0f;
    result[6] = 0.0f;
    result[10] = -(farZ + nearZ) / (farZ - nearZ);
    result[14] = -(2.0f * farZ * nearZ) / (farZ - nearZ);

    result[3] = 0.0f;
    result[7] = 0.0f;
    result[11] = -1.0f;
    result[15] = 0.0f;
}

static void buildViewMatrix(const XrPosef& pose, float* result) {
    // Quaternion to 3x3 rotation
    const float x = pose.orientation.x;
    const float y = pose.orientation.y;
    const float z = pose.orientation.z;
    const float w = pose.orientation.w;
    
    // x, y, z are inverted for view matrix, so we invert translation and transpose rotation
    const float x2 = x + x, y2 = y + y, z2 = z + z;
    const float xx = x * x2,  xy = x * y2,  xz = x * z2;
    const float yy = y * y2,  yz = y * z2,  zz = z * z2;
    const float wx = w * x2,  wy = w * y2,  wz = w * z2;

    result[0] = 1.0f - (yy + zz);
    result[4] = xy - wz;
    result[8] = xz + wy;
    result[12] = 0.0f;

    result[1] = xy + wz;
    result[5] = 1.0f - (xx + zz);
    result[9] = yz - wx;
    result[13] = 0.0f;

    result[2] = xz - wy;
    result[6] = yz + wx;
    result[10] = 1.0f - (xx + yy);
    result[14] = 0.0f;

    // Translation
    const float tx = -pose.position.x;
    const float ty = -pose.position.y;
    const float tz = -pose.position.z;
    
    result[12] = tx * result[0] + ty * result[4] + tz * result[8];
    result[13] = tx * result[1] + ty * result[5] + tz * result[9];
    result[14] = tx * result[2] + ty * result[6] + tz * result[10];
    result[15] = 1.0f;
    
    result[3] = 0.0f;
    result[7] = 0.0f;
    result[11] = 0.0f;
}

static void multiplyMatrix(const float* a, const float* b, float* result) {
    for (int i = 0; i < 4; ++i) {
        for (int j = 0; j < 4; ++j) {
            result[i * 4 + j] = a[i * 4 + 0] * b[0 * 4 + j] +
                                a[i * 4 + 1] * b[1 * 4 + j] +
                                a[i * 4 + 2] * b[2 * 4 + j] +
                                a[i * 4 + 3] * b[3 * 4 + j];
        }
    }
}

// ── Graphics Init ─────────────────────────────────────────────────────────────
bool CelestiaOpenXR::initGraphics() {
    // Shaders
    const char* vShaderStr =
        "#version 300 es\n"
        "in vec3 VertexPos;\n"
        "in vec3 VertexColor;\n"
        "out vec3 Color;\n"
        "uniform mat4 ModelViewProj;\n"
        "void main() {\n"
        "   Color = VertexColor;\n"
        "   gl_Position = ModelViewProj * vec4(VertexPos, 1.0);\n"
        "}\n";

    const char* fShaderStr =
        "#version 300 es\n"
        "precision mediump float;\n"
        "in vec3 Color;\n"
        "out vec4 FragColor;\n"
        "void main() {\n"
        "   FragColor = vec4(Color, 1.0);\n"
        "}\n";

    auto compileShader = [](GLenum type, const char* src) -> GLuint {
        GLuint shader = glCreateShader(type);
        glShaderSource(shader, 1, &src, nullptr);
        glCompileShader(shader);
        GLint compiled;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen > 1) {
                std::vector<char> infoLog(infoLen);
                glGetShaderInfoLog(shader, infoLen, nullptr, infoLog.data());
                LOGE("Error compiling shader:\n%s", infoLog.data());
            }
            glDeleteShader(shader);
            return 0;
        }
        return shader;
    };

    GLuint vertexShader = compileShader(GL_VERTEX_SHADER, vShaderStr);
    GLuint fragmentShader = compileShader(GL_FRAGMENT_SHADER, fShaderStr);

    shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertexShader);
    glAttachShader(shaderProgram, fragmentShader);
    glLinkProgram(shaderProgram);

    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);

    GLint linked;
    glGetProgramiv(shaderProgram, GL_LINK_STATUS, &linked);
    if (!linked) {
        LOGE("Error linking shader program");
        return false;
    }

    modelViewProjLoc = glGetUniformLocation(shaderProgram, "ModelViewProj");
    GLint posAttrib = glGetAttribLocation(shaderProgram, "VertexPos");
    GLint colAttrib = glGetAttribLocation(shaderProgram, "VertexColor");

    // Geometry: a simple colored triangle directly in front
    const float vertices[] = {
        // x, y, z, r, g, b
         0.0f,  0.5f, -2.0f,  1.0f, 0.0f, 0.0f,
        -0.5f, -0.5f, -2.0f,  0.0f, 1.0f, 0.0f,
         0.5f, -0.5f, -2.0f,  0.0f, 0.0f, 1.0f,
    };

    glGenVertexArrays(1, &vao);
    glBindVertexArray(vao);

    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

    glEnableVertexAttribArray(posAttrib);
    glVertexAttribPointer(posAttrib, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)0);

    glEnableVertexAttribArray(colAttrib);
    glVertexAttribPointer(colAttrib, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)(3 * sizeof(float)));

    glBindVertexArray(0);

    LOGI("Graphics initialized (Shaders + VAO)");
    return true;
}

void CelestiaOpenXR::destroyGraphics() {
    if (vao) { glDeleteVertexArrays(1, &vao); vao = 0; }
    if (vbo) { glDeleteBuffers(1, &vbo); vbo = 0; }
    if (shaderProgram) { glDeleteProgram(shaderProgram); shaderProgram = 0; }
}

// ── start / stop ──────────────────────────────────────────────────────────────
void CelestiaOpenXR::start() {
    LOGI("CelestiaOpenXR::start");
    createSession();
}

void CelestiaOpenXR::stop() {
    LOGI("CelestiaOpenXR::stop");
    sessionRunning = false;
}

// ── destroy ───────────────────────────────────────────────────────────────────
void CelestiaOpenXR::destroy() {
    LOGI("CelestiaOpenXR::destroy");

    destroyGraphics();

    for (auto& eye : eyeSwapchains) {
        if (eye.handle != XR_NULL_HANDLE) {
            xrDestroySwapchain(eye.handle);
            eye.handle = XR_NULL_HANDLE;
        }
    }
    if (appSpace != XR_NULL_HANDLE) { xrDestroySpace(appSpace);     appSpace  = XR_NULL_HANDLE; }
    if (session  != XR_NULL_HANDLE) { xrDestroySession(session);    session   = XR_NULL_HANDLE; }
    if (instance != XR_NULL_HANDLE) { xrDestroyInstance(instance);  instance  = XR_NULL_HANDLE; }

    if (eglDisplay != EGL_NO_DISPLAY) {
        eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroyContext(eglDisplay, eglContext);
        eglTerminate(eglDisplay);
        eglDisplay = EGL_NO_DISPLAY;
        eglContext = EGL_NO_CONTEXT;
    }
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
void CelestiaOpenXR::renderFrame() {
    if (!sessionRunning) return;

    // ── Wait ─────────────────────────────────────────────────────────────────
    XrFrameWaitInfo  waitInfo{XR_TYPE_FRAME_WAIT_INFO};
    XrFrameState     frameState{XR_TYPE_FRAME_STATE};
    XR_CHECK(xrWaitFrame(session, &waitInfo, &frameState), "xrWaitFrame");

    // ── Begin ─────────────────────────────────────────────────────────────────
    XrFrameBeginInfo beginInfo{XR_TYPE_FRAME_BEGIN_INFO};
    XR_CHECK(xrBeginFrame(session, &beginInfo), "xrBeginFrame");

    std::vector<XrCompositionLayerBaseHeader*> layers;
    XrCompositionLayerProjection               layerProj{XR_TYPE_COMPOSITION_LAYER_PROJECTION};
    std::array<XrCompositionLayerProjectionView, 2> projViews;

    if (frameState.shouldRender) {
        // Locate eye views
        XrViewLocateInfo locateInfo{XR_TYPE_VIEW_LOCATE_INFO};
        locateInfo.viewConfigurationType = XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO;
        locateInfo.displayTime           = frameState.predictedDisplayTime;
        locateInfo.space                 = appSpace;

        XrViewState viewState{XR_TYPE_VIEW_STATE};
        std::array<XrView, 2> views;
        views[0].type = views[1].type = XR_TYPE_VIEW;

        uint32_t viewCount = 2;
        XR_CHECK(xrLocateViews(session, &locateInfo, &viewState,
                               viewCount, &viewCount, views.data()), "xrLocateViews");

        for (int i = 0; i < 2; ++i) {
            renderEye(i, eyeSwapchains[i].images[0], views[i], projViews[i]);
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
                                XrCompositionLayerProjectionView& projView) {
    const EyeSwapchain& eye = eyeSwapchains[eyeIndex];

    // Acquire swapchain image
    XrSwapchainImageAcquireInfo acquireInfo{XR_TYPE_SWAPCHAIN_IMAGE_ACQUIRE_INFO};
    uint32_t imageIndex = 0;
    XR_CHECK(xrAcquireSwapchainImage(eye.handle, &acquireInfo, &imageIndex),
             "xrAcquireSwapchainImage");

    XrSwapchainImageWaitInfo waitInfo{XR_TYPE_SWAPCHAIN_IMAGE_WAIT_INFO};
    waitInfo.timeout = XR_INFINITE_DURATION;
    XR_CHECK(xrWaitSwapchainImage(eye.handle, &waitInfo), "xrWaitSwapchainImage");

    // ── Render deep-space background ─────────────────────────────────────────
    // Bind the swapchain texture to an FBO and clear to a deep-space colour.
    GLuint fbo = 0;
    glGenFramebuffers(1, &fbo);
    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                           eye.images[imageIndex].image, 0);
    glViewport(0, 0, eye.width, eye.height);
    glClearColor(0.01f, 0.02f, 0.08f, 1.0f);   // deep-space blue-black
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Compute MVP matrix for the current eye
    float proj[16];
    buildProjectionMatrix(view.fov, 0.05f, 100.0f, proj);
    
    float viewMat[16];
    buildViewMatrix(view.pose, viewMat);
    
    float mvp[16];
    multiplyMatrix(proj, viewMat, mvp);

    // Draw our triangle
    glUseProgram(shaderProgram);
    glUniformMatrix4fv(modelViewProjLoc, 1, GL_FALSE, mvp);
    
    glBindVertexArray(vao);
    glDrawArrays(GL_TRIANGLES, 0, 3);
    glBindVertexArray(0);

    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glDeleteFramebuffers(1, &fbo);

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
