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
        return c_getDSOName(dso.pointer);
    }

    public int getCount() {
        return c_getCount();
    }

    public CelestiaDSO getDSO(int index) {
        return new CelestiaDSO(c_getDSO(index));
    }

    // C functions
    private native String c_getDSOName(long pointer);
    private native int c_getCount();
    private native long c_getDSO(int index);
}
