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
import androidx.annotation.Nullable;

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

    public void startConditionally(@NonNull Activity activity, boolean enableMultisample, int resolutionMultiplier, @Nullable String fontPath, int ttcIndex) {
        if (started) return;
        start(activity, enableMultisample, resolutionMultiplier, fontPath, ttcIndex);
    }

    public void start(@NonNull Activity activity, boolean enableMultisample, int resolutionMultiplier, @Nullable String fontPath, int ttcIndex) {
        started = true;
        c_start(pointer, activity, enableMultisample, resolutionMultiplier, fontPath, ttcIndex);
    }

    public void resume() {
        c_resume(pointer);
    }

    public void pause() {
        c_pause(pointer);
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
        boolean onEngineStarted(int sampleCount, float resolutionMultiplier);
    }

    public void setEngineStartedListener(EngineStartedListener engineStartedListener) {
        this.engineStartedListener = engineStartedListener;
    }

    private boolean engineStarted(int sampleCount, float resolutionMultiplier) {
        boolean result = false;
        if (engineStartedListener != null)
            result = engineStartedListener.onEngineStarted(sampleCount, resolutionMultiplier);
        engineStartedListener = null;
        return result;
    }

    public interface ControllerButtonListener {
        void onControllerButton(int keyCode, boolean isReleased);
    }

    private ControllerButtonListener controllerButtonListener = null;

    public void setControllerButtonListener(ControllerButtonListener listener) {
        this.controllerButtonListener = listener;
    }

    private void controllerButton(int keyCode, boolean isReleased) {
        if (controllerButtonListener != null)
            controllerButtonListener.onControllerButton(keyCode, isReleased);
    }

    public interface JoystickAxisListener {
        void onJoystickAxis(int axis, float value);
    }

    private JoystickAxisListener joystickAxisListener = null;

    public void setJoystickAxisListener(JoystickAxisListener listener) {
        this.joystickAxisListener = listener;
    }

    private void joystickAxis(int axis, float value) {
        if (joystickAxisListener != null)
            joystickAxisListener.onJoystickAxis(axis, value);
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

    public void showOverlayMessage(@NonNull String message) {
        c_setOverlayMessage(pointer, message);
    }

    public void clearOverlayMessage() {
        c_setOverlayMessage(pointer, null);
    }

    private static native long c_createNativeXRObject();
    private native void c_initialize(long pointer);
    private native void c_start(long pointer, Activity activity, boolean enableMultisample, int resolutionMultiplier, String fontPath, int ttcIndex);
    private native void c_resume(long pointer);
    private native void c_pause(long pointer);
    private native void c_destroy(long pointer);
    private native void c_setCorePointer(long pointer, long corePtr);
    private native void c_setHasPendingTasks(long pointer, boolean hasPendingTasks);
    private native void c_setOverlayMessage(long pointer, String message);
}
