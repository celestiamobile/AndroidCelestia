/*
 * CelestiaStar.java
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

public class CelestiaStar extends CelestiaAstroObject {
    CelestiaStar(long ptr) {
        super(ptr);
    }

    @Nullable
    String getWebInfoURL() {
        String web = c_getWebInfoURL();
        if (web.isEmpty())
            return null;
        return c_getWebInfoURL();
    }

    @NonNull
    public CelestiaUniversalCoord getPositionAtTime(double julianDay) {
        return new CelestiaUniversalCoord(c_getPositionAtTime(julianDay));
    }

    // C functions
    private native String c_getWebInfoURL();
    private native long c_getPositionAtTime(double julianDay);
}
