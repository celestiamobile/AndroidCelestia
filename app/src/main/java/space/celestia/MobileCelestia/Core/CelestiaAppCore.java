package space.celestia.MobileCelestia.Core;

import java.util.Date;
import java.util.List;

public class CelestiaAppCore {
    private long pointer;
    private boolean intialized;
    private CelestiaSimulation simulation;

    public CelestiaAppCore(long pointer) {
        c_init();
        this.intialized = false;
        this.simulation = null;
    }

    public boolean startRenderer() {
        return c_startRenderer();
    }

    public boolean startSimulation(String configFileName, String[] extraDirectories) {
        return c_startSimulation(configFileName, extraDirectories);
    }

    public void start() {
        c_start();
    }

    public void start(Date date) {
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

    public static boolean initGL() {
        return c_initGL();
    }

    public CelestiaSimulation getSimulation() {
        if (simulation == null)
            simulation = new CelestiaSimulation(c_getSimulation());
        return simulation;
    }

    // C function
    private native void c_init();
    private native boolean c_startRenderer();
    private native boolean c_startSimulation(String configFileName, String[] extraDirectories);
    private native void c_start();
    private native void c_start(double secondsSinceEpoch);
    private native void c_draw();
    private native void c_tick();
    private native void c_resize(int w, int h);
    private native long c_getSimulation();

    private static native boolean c_initGL();
}
