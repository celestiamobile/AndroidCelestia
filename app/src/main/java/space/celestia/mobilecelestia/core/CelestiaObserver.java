/*
 * CelestiaObserver.java
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

public class CelestiaObserver {
    private final long pointer;

    protected CelestiaObserver(long ptr) {
        pointer = ptr;
    }

    public @NonNull
    String getDisplayedSurface() {
        return c_getDisplayedSurface(pointer);
    }

    public void setDisplayedSurface(@NonNull String displayedSurface) {
        c_setDisplayedSurface(pointer, displayedSurface);
    }

    private static native String c_getDisplayedSurface(long ptr);
    private static native void c_setDisplayedSurface(long ptr, String displayedSurface);
}
