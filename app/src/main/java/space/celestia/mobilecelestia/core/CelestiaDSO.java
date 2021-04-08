/*
 * CelestiaDSO.java
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
import androidx.annotation.Nullable;

public class CelestiaDSO extends CelestiaAstroObject {
    protected CelestiaDSO(long ptr) {
        super(ptr);
    }

    @Nullable
    String getWebInfoURL() {
        String web = c_getWebInfoURL(pointer);
        if (web.isEmpty())
            return null;
        return c_getWebInfoURL(pointer);
    }

    @NonNull
    public String getType() {
        return c_getType(pointer);
    }

    @NonNull
    public CelestiaVector getPosition() { return c_getPosition(pointer); }

    // C functions
    private static native String c_getWebInfoURL(long pointer);
    private static native String c_getType(long pointer);
    private static native CelestiaVector c_getPosition(long pointer);
}
