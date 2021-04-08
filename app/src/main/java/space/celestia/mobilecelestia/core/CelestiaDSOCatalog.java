/*
 * CelestiaDSOCatalog.java
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

public class CelestiaDSOCatalog {
    protected long pointer;

    CelestiaDSOCatalog(long ptr) {
        pointer = ptr;
    }

    @NonNull
    public String getDSOName(CelestiaDSO dso) {
        return c_getDSOName(pointer, dso.pointer);
    }

    public int getCount() {
        return c_getCount(pointer);
    }

    public CelestiaDSO getDSO(int index) {
        return new CelestiaDSO(c_getDSO(pointer, index));
    }

    // C functions
    private static native String c_getDSOName(long ptr, long pointer);
    private static native int c_getCount(long ptr);
    private static native long c_getDSO(long ptr, int index);
}
