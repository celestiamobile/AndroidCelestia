/*
 * DSOCatalog.java
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

public class DSOCatalog {
    protected long pointer;

    DSOCatalog(long ptr) {
        pointer = ptr;
    }

    @NonNull
    public String getDSOName(DSO dso) {
        return c_getDSOName(pointer, dso.pointer);
    }

    public int getCount() {
        return c_getCount(pointer);
    }

    public DSO getDSO(int index) {
        long dsoPtr = c_getDSO(pointer, index);
        if (c_isDSOGalaxy(dsoPtr)) {
            return new Galaxy(dsoPtr);
        }
        return new DSO(c_getDSO(pointer, index));
    }

    // C functions
    private static native String c_getDSOName(long ptr, long pointer);
    private static native int c_getCount(long ptr);
    private static native long c_getDSO(long ptr, int index);
    static native boolean c_isDSOGalaxy(long ptr);
}
