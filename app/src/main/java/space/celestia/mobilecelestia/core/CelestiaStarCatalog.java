/*
 * CelestiaStarCatalog.java
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

public class CelestiaStarCatalog {
    protected long pointer;

    CelestiaStarCatalog(long ptr) {
        pointer = ptr;
    }

    @NonNull
    public String getStarName(CelestiaStar star) {
        return c_getStarName(pointer, star.pointer);
    }

    public int getCount() {
        return c_getCount(pointer);
    }

    public CelestiaStar getStar(int index) {
        return new CelestiaStar(c_getStar(pointer, index));
    }

    // C functions
    private static native String c_getStarName(long ptr, long pointer);
    private static native int c_getCount(long ptr);
    private static native long c_getStar(long ptr, int index);
}
