package space.celestia.mobilecelestia.celestia;

import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

abstract class BaseConfigChooser
        implements GLSurfaceView.EGLConfigChooser {
    public BaseConfigChooser(int[] configSpec, int[] fallbackSpec) {
        mConfigSpec = filterConfigSpec(configSpec);
        mFallbackSpec = filterConfigSpec(fallbackSpec);
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        int[] num_config = new int[1];
        boolean useFallback = false;
        if (!egl.eglChooseConfig(display, mConfigSpec, null, 0,
                num_config)) {
            useFallback = true;
            if (!egl.eglChooseConfig(display, mFallbackSpec, null, 0,
                    num_config)) {
                throw new IllegalArgumentException("eglChooseConfig failed");
            }
        }


        int numConfigs = num_config[0];

        if (numConfigs <= 0) {
            throw new IllegalArgumentException(
                    "No configs match configSpec");
        }

        EGLConfig[] configs = new EGLConfig[numConfigs];
        if (!egl.eglChooseConfig(display, useFallback ? mFallbackSpec : mConfigSpec, configs, numConfigs,
                num_config)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }
        EGLConfig config = chooseConfig(egl, display, configs, useFallback);
        if (config == null) {
            throw new IllegalArgumentException("No config chosen");
        }
        return config;
    }

    abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                    EGLConfig[] configs, boolean useFallback);

    protected int[] mConfigSpec;
    protected int[] mFallbackSpec;

    private int[] filterConfigSpec(int[] configSpec) {
        /* We know none of the subclasses define EGL_RENDERABLE_TYPE.
         * And we know the configSpec is well formed.
         */
        int len = configSpec.length;
        int[] newConfigSpec = new int[len + 2];
        System.arraycopy(configSpec, 0, newConfigSpec, 0, len-1);
        newConfigSpec[len-1] = EGL10.EGL_RENDERABLE_TYPE;
        newConfigSpec[len] = EGL14.EGL_OPENGL_ES2_BIT;  /* EGL_OPENGL_ES2_BIT */
        newConfigSpec[len+1] = EGL10.EGL_NONE;
        return newConfigSpec;
    }
}

/**
 * Choose a configuration with exactly the specified r,g,b,a sizes,
 * and at least the specified depth and stencil sizes.
 */
class ComponentSizeChooser extends BaseConfigChooser {
    public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize,
                                int depthSize, int stencilSize, int sampleSize,
                                int fallbackDepthSize, int fallbackStencilSize, int fallbackSampleSize) {
        super(new int[] {
                        EGL10.EGL_RED_SIZE, redSize,
                        EGL10.EGL_GREEN_SIZE, greenSize,
                        EGL10.EGL_BLUE_SIZE, blueSize,
                        EGL10.EGL_ALPHA_SIZE, alphaSize,
                        EGL10.EGL_DEPTH_SIZE, depthSize,
                        EGL10.EGL_STENCIL_SIZE, stencilSize,
                        EGL10.EGL_SAMPLES, sampleSize,
                        EGL10.EGL_SAMPLE_BUFFERS, sampleSize > 1 ? 1 : 0,
                        EGL10.EGL_NONE},
                new int[] {
                        EGL10.EGL_RED_SIZE, redSize,
                        EGL10.EGL_GREEN_SIZE, greenSize,
                        EGL10.EGL_BLUE_SIZE, blueSize,
                        EGL10.EGL_ALPHA_SIZE, alphaSize,
                        EGL10.EGL_DEPTH_SIZE, fallbackDepthSize,
                        EGL10.EGL_STENCIL_SIZE, fallbackStencilSize,
                        EGL10.EGL_SAMPLES, fallbackSampleSize,
                        EGL10.EGL_SAMPLE_BUFFERS, fallbackSampleSize > 1 ? 1 : 0,
                        EGL10.EGL_NONE});
        mValue = new int[1];
        mRedSize = redSize;
        mGreenSize = greenSize;
        mBlueSize = blueSize;
        mAlphaSize = alphaSize;
        mDepthSize = depthSize;
        mStencilSize = stencilSize;
        mSampleSize = sampleSize;
        mFallbackDepthSize = fallbackDepthSize;
        mFallbackStencilSize = fallbackStencilSize;
        mFallbackSampleSize = fallbackSampleSize;
    }

    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                  EGLConfig[] configs, boolean useFallback) {
        for (EGLConfig config : configs) {
            int d = findConfigAttrib(egl, display, config,
                    EGL10.EGL_DEPTH_SIZE, 0);
            int s = findConfigAttrib(egl, display, config,
                    EGL10.EGL_STENCIL_SIZE, 0);
            int ss = findConfigAttrib(egl, display, config,
                    EGL10.EGL_SAMPLES, 1);
            if ((d >= (useFallback ? mFallbackDepthSize : mDepthSize)) &&
                    (s >= (useFallback ? mFallbackStencilSize : mStencilSize)) &&
                    (ss >= (useFallback ? mFallbackSampleSize : mSampleSize))) {
                int r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config,
                        EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config,
                        EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0);
                if ((r == mRedSize) && (g == mGreenSize)
                        && (b == mBlueSize) && (a == mAlphaSize)) {
                    return config;
                }
            }
        }
        return null;
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                 EGLConfig config, int attribute, int defaultValue) {

        if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0];
        }
        return defaultValue;
    }

    private int[] mValue;
    // Subclasses can adjust these values:
    protected int mRedSize;
    protected int mGreenSize;
    protected int mBlueSize;
    protected int mAlphaSize;
    protected int mDepthSize;
    protected int mStencilSize;
    protected int mSampleSize;
    protected int mFallbackDepthSize;
    protected int mFallbackStencilSize;
    protected int mFallbackSampleSize;
}

/**
 * This class will choose a RGB_888 surface with
 * or without a depth buffer.
 *
 */
class SimpleEGLConfigChooser extends ComponentSizeChooser {
    public SimpleEGLConfigChooser(int sampleSize, int fallbackSampleSize) {
        super(8, 8, 8, 0, 16, 0, sampleSize, 16, 0, fallbackSampleSize);
    }
}

public class CelestiaEGLChooser extends SimpleEGLConfigChooser {
    public CelestiaEGLChooser(boolean multisampleEnabled) {
        super(multisampleEnabled ? 4 : 0, 0);
    }
}
