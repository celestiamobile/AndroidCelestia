/*
 * Observer.java
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

public class Observer {
    public static final int COORDINATE_SYSTEM_UNIVERSAL = 0;
    public static final int COORDINATE_SYSTEM_ECLIPTICAL = 1;
    public static final int COORDINATE_SYSTEM_BODY_FIXED = 3;
    public static final int COORDINATE_SYSTEM_PHASE_LOCK = 5;
    public static final int COORDINATE_SYSTEM_CHASE = 6;

    protected final long pointer;

    protected Observer(long ptr) {
        pointer = ptr;
    }

    public @NonNull
    String getDisplayedSurface() {
        return c_getDisplayedSurface(pointer);
    }

    public void setDisplayedSurface(@NonNull String displayedSurface) {
        c_setDisplayedSurface(pointer, displayedSurface);
    }

    public void setFrame(int coordinateSystem, @NonNull Selection reference, @NonNull Selection target) {
        c_setFrame(pointer, coordinateSystem, reference, target);
    }

    public void rotate(float[] from, float[] to) {
        c_rotate(pointer, from, to);
    }

    private static native String c_getDisplayedSurface(long ptr);
    private static native void c_setDisplayedSurface(long ptr, String displayedSurface);
    private static native void c_setFrame(long ptr, int coordinateSystem, Selection reference, Selection target);
    private static native void c_rotate(long ptr, float[] from, float[] to);
}
