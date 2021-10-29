/*
 * StarCatalog.java
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

public class StarCatalog {
    protected long pointer;

    StarCatalog(long ptr) {
        pointer = ptr;
    }

    @NonNull
    public String getStarName(Star star) {
        return c_getStarName(pointer, star.pointer);
    }

    public int getCount() {
        return c_getCount(pointer);
    }

    public Star getStar(int index) {
        return new Star(c_getStar(pointer, index));
    }

    // C functions
    private static native String c_getStarName(long ptr, long pointer);
    private static native int c_getCount(long ptr);
    private static native long c_getStar(long ptr, int index);
}
