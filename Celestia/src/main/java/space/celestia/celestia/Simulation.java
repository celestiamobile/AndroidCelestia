/*
 * Simulation.java
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.celestia;

import androidx.annotation.NonNull;

import java.util.List;

public class Simulation {
    final private long pointer;
    private Universe universe;

    public @NonNull
    Selection getSelection() {
        return c_getSelection(pointer);
    }

    public void setSelection(@NonNull Selection selection) {
        c_setSelection(pointer, selection);
    }

    public @NonNull List<Completion> completionForText(@NonNull String text, int limit) {
        return c_completionForText(pointer, text, limit);
    }

    public @NonNull
    Selection findObject(@NonNull String name) {
        return c_findObject(pointer, name);
    }

    public @NonNull
    Universe getUniverse() {
        if (universe == null)
            universe = new Universe(c_getUniverse(pointer));
        return universe;
    }
    public void reverseObserverOrientation() {
        c_reverseObserverOrientation(pointer);
    }

    protected Simulation(long ptr) {
        pointer = ptr;
    }

    public double getTime() { return c_getTime(pointer); }
    public void setTime(double time) { c_setTime(pointer, time); }

    public void goToEclipse(EclipseFinder.Eclipse eclipse) {
        PlanetarySystem system = eclipse.receiver.getSystem();
        if (system == null)
             return;
        Star star = system.getStar();
        if (star == null)
            return;
        Selection target = new Selection(eclipse.receiver);
        Selection ref = new Selection(star);
        c_goToEclipse(pointer, eclipse.startTimeJulian, ref, target);
    }

    public @NonNull
    Observer getActiveObserver() {
        return new Observer(c_getActiveObserver(pointer));
    }

    public void goTo(@NonNull Destination destination) {
        c_goToDestination(pointer, destination.target, destination.distance);
    }

    public void goToLocation(@NonNull GoToLocation location) {
        double distance = location.selection.getRadius() * 5.0;
        if ((location.fieldMask & GoToLocation.FieldMaskDistance) != 0) {
            distance = location.distance;
        }
        if (((location.fieldMask & GoToLocation.FieldMaskLongitude) != 0) && ((location.fieldMask & GoToLocation.FieldMaskLatitude) != 0 )) {
            c_goToLocationLongLat(pointer, location.selection, location.longitude, location.latitude, distance, location.duration);
        } else {
            c_goToLocation(pointer, location.selection, distance, location.duration);
        }
    }

    // C functions
    private static native Selection c_getSelection(long pointer);
    private static native void c_setSelection(long pointer, Selection selection);
    private static native long c_getUniverse(long pointer);
    private static native List<Completion> c_completionForText(long pointer, String text, int limit);
    private static native Selection c_findObject(long pointer, String name);
    private static native void c_reverseObserverOrientation(long pointer);
    private static native double c_getTime(long pointer);
    private static native void c_setTime(long pointer, double time);
    private static native void c_goToEclipse(long pointer, double time, Selection ref, Selection target);
    private static native long c_getActiveObserver(long pointer);
    private static native void c_goToDestination(long ptr, String target, double distance);
    private static native void c_goToLocation(long pointer, Selection selection, double distance, double duration);
    private static native void c_goToLocationLongLat(long pointer, Selection selection, float longitude, float latitude, double distance, double duration);
}
