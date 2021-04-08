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
        return new CelestiaSelection(c_getSelection(pointer));
    }

    public void setSelection(@NonNull CelestiaSelection selection) {
        c_setSelection(pointer, selection.pointer);
    }

    public @NonNull List<String> completionForText(@NonNull String text, int limit) {
        return c_completionForText(pointer, text, limit);
    }

    public @NonNull CelestiaSelection findObject(@NonNull String name) {
        return new CelestiaSelection(c_findObject(pointer, name));
    }

    public @NonNull
    CelestiaUniverse getUniverse() {
        if (universe == null)
            universe = new CelestiaUniverse(c_getUniverse(pointer));
        return universe;
    }

    public @NonNull
    CelestiaStarBrowser getStarBrowser(int kind) {
        return new CelestiaStarBrowser(c_getStarBrowser(pointer, kind));
    }

    public void reverseObserverOrientation() {
        c_reverseObserverOrientation(pointer);
    }

    protected CelestiaSimulation(long ptr) {
        pointer = ptr;
    }

    public double getTime() { return c_getTime(pointer); }
    public void setTime(double time) { c_setTime(pointer, time); }

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
        c_goToEclipse(pointer, eclipse.startTimeJulian, ref.pointer, target.pointer);
    }

    public @NonNull CelestiaObserver getActiveObserver() {
        return new CelestiaObserver(c_getActiveObserver(pointer));
    }

    public void goTo(@NonNull CelestiaDestination destination) {
        c_goToDestination(pointer, destination.target, destination.distance);
    }

    // C functions
    private static native long c_getSelection(long pointer);
    private static native void c_setSelection(long pointer, long ptr);
    private static native long c_getUniverse(long pointer);
    private static native List<String> c_completionForText(long pointer, String text, int limit);
    private static native long c_findObject(long pointer, String name);
    private static native long c_getStarBrowser(long pointer, int kind);
    private static native void c_reverseObserverOrientation(long pointer);
    private static native double c_getTime(long pointer);
    private static native void c_setTime(long pointer, double time);
    private static native void c_goToEclipse(long pointer, double time, long ref, long target);
    private static native long c_getActiveObserver(long pointer);
    private static native void c_goToDestination(long ptr, String target, double distance);
}
