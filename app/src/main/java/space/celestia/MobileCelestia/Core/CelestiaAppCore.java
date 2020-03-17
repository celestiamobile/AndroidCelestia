package space.celestia.MobileCelestia.Core;

import android.graphics.PointF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

import kotlin.UInt;

public class CelestiaAppCore {
    public static final int MOUSE_BUTTON_LEFT       = 0x1;
    public static final int MOUSE_BUTTON_MIDDLE     = 0x2;
    public static final int MOUSE_BUTTON_RIGHT      = 0x4;

    public interface ProgressWatcher {
        void onCelestiaProgress(@NonNull String progress);
    }

    private long pointer;
    private boolean intialized;
    private CelestiaSimulation simulation;

    // Singleton
    private static CelestiaAppCore shared;
    public static CelestiaAppCore shared() {
        if (shared == null)
            shared = new CelestiaAppCore();
        return shared;
    }

    public boolean isIntialized() {
        return intialized;
    }

    public CelestiaAppCore() {
        c_init();
        this.intialized = false;
        this.simulation = null;
    }

    public boolean startRenderer() {
        return c_startRenderer();
    }

    public boolean startSimulation(@Nullable String configFileName, @Nullable String[] extraDirectories, @Nullable ProgressWatcher watcher) {
        return c_startSimulation(configFileName, extraDirectories, watcher);
    }

    public void start() {
        c_start();
    }

    public void start(@NonNull Date date) {
        c_start((double)date.getTime() / 1000);
    }

    public void draw() {
        c_draw();
    }

    public void tick() {
        c_tick();
    }

    public void resize(int w, int h) {
        c_resize(w, h);
    }

    // Control
    public void mouseButtonUp(int buttons, PointF point, int modifiers) {
        c_mouseButtonUp(buttons, point.x, point.y, modifiers);
    }

    public void mouseButtonDown(int buttons, PointF point, int modifiers) {
        c_mouseButtonDown(buttons, point.x, point.y, modifiers);
    }

    public void mouseMove(int buttons, PointF offset, int modifiers) {
        c_mouseMove(buttons, offset.x, offset.y, modifiers);
    }

    public void mouseWheel(float motion, int modifiers) {
        c_mouseWheel(motion, modifiers);
    }

    public void keyUp(int input) {
        c_keyUp(input);
    }

    public void keyDown(int input) {
        c_keyDown(input);
    }

    public void charEnter(int input) {
        c_charEnter(input);
    }

    public static boolean initGL() {
        return c_initGL();
    }
    public static void chdir(String path) { c_chdir(path);}

    public CelestiaSimulation getSimulation() {
        if (simulation == null)
            simulation = new CelestiaSimulation(c_getSimulation());
        return simulation;
    }

    // C function
    private native void c_init();
    private native boolean c_startRenderer();
    private native boolean c_startSimulation(String configFileName, String[] extraDirectories, ProgressWatcher watcher);
    private native void c_start();
    private native void c_start(double secondsSinceEpoch);
    private native void c_draw();
    private native void c_tick();
    private native void c_resize(int w, int h);
    private native long c_getSimulation();

    // Control
    private native void c_mouseButtonUp(int buttons, float x, float y, int modifiers);
    private native void c_mouseButtonDown(int buttons, float x, float y, int modifiers);
    private native void c_mouseMove(int buttons, float x, float y, int modifiers);
    private native void c_mouseWheel(float motion, int modifiers);
    private native void c_keyUp(int input);
    private native void c_keyDown(int input);
    private native void c_charEnter(int input);

    private static native boolean c_initGL();
    private static native void c_chdir(String path);
}
