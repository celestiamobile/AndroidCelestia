/*
 * Renderer.java
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.celestia;

import android.app.Activity;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Renderer implements AutoCloseable {
    public static int FRAME_MAX = 0;
    public static int FRAME_60FPS = 1;
    public static int FRAME_30FPS = 2;
    public static int FRAME_20FPS = 3;

    public interface Callback {
        void call();
    }

    private List<Callback> tasks = new ArrayList<>();
    private final Object taskLock = new Object();
    private final long pointer;
    private boolean started = false;
    private boolean closed = false;

    private EngineStartedListener engineStartedListener = null;

    @Override
    public void close() throws Exception {
        if (!closed) {
            c_destroy(pointer);
            closed = true;
        }
    }

    public Renderer() {
        pointer = c_createNativeRenderObject();
        c_initialize(pointer);
    }

    public void makeContextCurrent() {
        c_makeContextCurrent(pointer);
    }
    public void setFrameRateOption(int frameRateOption) {
        c_setFrameRateOption(pointer, frameRateOption);
    }

    public void enqueueTask(@NonNull Callback task) {
        synchronized (taskLock) {
            int previousTaskCount = tasks.size();
            tasks.add(task);
            if (previousTaskCount == 0)
                c_setHasPendingTasks(pointer, true);
        }
    }

    public void startConditionally(@NonNull Activity activity, boolean enableMultisample) {
        if (started) return;
        start(activity, enableMultisample);
    }

    public void start(@NonNull Activity activity, boolean enableMultisample) {
        started = true;
        c_start(pointer, activity, enableMultisample);
    }

    public void stop() {
        c_stop(pointer);
    }

    public void pause() {
        c_pause(pointer);
    }

    public void resume() {
        c_resume(pointer);
    }

    public void setSurface(@Nullable Surface surface) {
        c_setSurface(pointer, surface);
    }

    protected void setCorePointer(long ptr) {
        c_setCorePointer(pointer, ptr);
    }

    public void setSurfaceSize(int width, int height) {
        c_setSurfaceSize(pointer, width, height);
    }

    public interface EngineStartedListener {
        boolean onEngineStarted(int samples);
    }

    public void setEngineStartedListener(EngineStartedListener engineStartedListener) {
        this.engineStartedListener = engineStartedListener;
    }

    private boolean engineStarted(int samples) {
        boolean result = false;
        if (engineStartedListener != null)
            result = engineStartedListener.onEngineStarted(samples);
        engineStartedListener = null;
        return result;
    }

    private void flushTasks() {
        ArrayList<Callback> taskCopy;
        synchronized (taskLock) {
            if (tasks.isEmpty()) return;
            taskCopy = new ArrayList<>(tasks);
            tasks = new ArrayList<>();
            c_setHasPendingTasks(pointer, false);
        }
        for (Callback task : taskCopy)
            task.call();
    }

    private static native long c_createNativeRenderObject();
    private native void c_initialize(long pointer);
    private native void c_destroy(long pointer);
    private native void c_start(long pointer, Activity activity, boolean enableMultisample);
    private native void c_stop(long pointer);
    private native void c_pause(long pointer);
    private native void c_resume(long pointer);
    private native void c_setSurface(long pointer, Surface surface);
    private native void c_setCorePointer(long pointer, long corePtr);
    private native void c_setSurfaceSize(long pointer, int width, int height);
    private native void c_makeContextCurrent(long pointer);
    private native void c_setFrameRateOption(long pointer, int frameRateOption);
    private native void c_setHasPendingTasks(long pointer, boolean hasPendingTasks);
}
