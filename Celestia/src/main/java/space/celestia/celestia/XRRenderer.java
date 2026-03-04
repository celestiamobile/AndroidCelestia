// XRRenderer.java
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestia;

import android.app.Activity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class XRRenderer implements AutoCloseable {
    public interface Callback {
        void call();
    }

    private List<Callback> tasks = new ArrayList<>();
    private final Object taskLock = new Object();
    private final long pointer;
    private boolean closed = false;
    private boolean started = false;
    private EngineStartedListener engineStartedListener = null;

    public XRRenderer() {
        pointer = c_createNativeXRObject();
        c_initialize(pointer);
    }

    public void startConditionally(@NonNull Activity activity) {
        if (started) return;
        start(activity);
    }

    public void start(@NonNull Activity activity) {
        started = true;
        c_start(pointer, activity);
    }

    public void stop() {
        c_stop(pointer);
    }

    public void enqueueTask(@NonNull Callback task) {
        synchronized (taskLock) {
            int previousTaskCount = tasks.size();
            tasks.add(task);
            if (previousTaskCount == 0)
                c_setHasPendingTasks(pointer, true);
        }
    }

    protected void setCorePointer(long corePtr) {
        c_setCorePointer(pointer, corePtr);
    }

    public interface EngineStartedListener {
        boolean onEngineStarted();
    }

    public void setEngineStartedListener(EngineStartedListener engineStartedListener) {
        this.engineStartedListener = engineStartedListener;
    }

    private boolean engineStarted() {
        boolean result = false;
        if (engineStartedListener != null)
            result = engineStartedListener.onEngineStarted();
        engineStartedListener = null;
        return result;
    }

    @Override
    public void close() {
        if (!closed) {
            c_destroy(pointer);
            closed = true;
        }
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

    private static native long c_createNativeXRObject();
    private native void c_initialize(long pointer);
    private native void c_start(long pointer, Activity activity);
    private native void c_stop(long pointer);
    private native void c_destroy(long pointer);
    private native void c_setCorePointer(long pointer, long corePtr);
    private native void c_setHasPendingTasks(long pointer, boolean hasPendingTasks);
}
