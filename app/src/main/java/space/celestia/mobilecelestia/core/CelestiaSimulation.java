/*
 * CelestiaSimulation.java
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;

import java.util.List;

public class CelestiaSimulation {
    private long pointer;
    private CelestiaUniverse universe;

    public @NonNull
    CelestiaSelection getSelection() {
        return new CelestiaSelection(c_getSelection());
    }

    public void setSelection(@NonNull CelestiaSelection selection) {
        c_setSelection(selection.pointer);
    }

    public @NonNull List<String> completionForText(@NonNull String text, int limit) {
        return c_completionForText(text, limit);
    }

    public @NonNull CelestiaSelection findObject(@NonNull String name) {
        return new CelestiaSelection(c_findObject(name));
    }

    public @NonNull
    CelestiaUniverse getUniverse() {
        if (universe == null)
            universe = new CelestiaUniverse(c_getUniverse());
        return universe;
    }

    public @NonNull
    CelestiaStarBrowser getStarBrowser(int kind) {
        return new CelestiaStarBrowser(c_getStarBrowser(kind));
    }

    public void reverseObserverOrientation() {
        c_reverseObserverOrientation();
    }

    protected CelestiaSimulation(long ptr) {
        pointer = ptr;
    }

    public double getTime() { return c_getTime(); }
    public void setTime(double time) { c_setTime(time); }

    public void goToEclipse(CelestiaEclipseFinder.Eclipse eclipse) {
        CelestiaPlanetarySystem system = eclipse.receiver.getSystem();
        if (system == null)
             return;
        CelestiaStar star = system.getStar();
        if (star == null)
            return;
        CelestiaSelection target = CelestiaSelection.create(eclipse.receiver);
        CelestiaSelection ref = CelestiaSelection.create(star);
        if (target == null || ref == null)
            return;
        c_goToEclipse(eclipse.startTimeJulian, ref.pointer, target.pointer);
    }

    public @NonNull CelestiaObserver getActiveObserver() {
        return new CelestiaObserver(c_getActiveObserver());
    }

    public void goTo(@NonNull CelestiaDestination destination) {
        c_goToDestination(pointer, destination.target, destination.distance);
    }

    // C functions
    private native long c_getSelection();
    private native void c_setSelection(long ptr);
    private native long c_getUniverse();
    private native List<String> c_completionForText(String text, int limit);
    private native long c_findObject(String name);
    private native long c_getStarBrowser(int kind);
    private native void c_reverseObserverOrientation();
    private native double c_getTime();
    private native void c_setTime(double time);
    private native void c_goToEclipse(double time, long ref, long target);
    private native long c_getActiveObserver();
    private static native void c_goToDestination(long ptr, String target, double distance);
}
