/*
 * CelestiaPlanetarySystem.java
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.core;

import androidx.annotation.Nullable;

public class CelestiaPlanetarySystem {
    private final long pointer;

    protected CelestiaPlanetarySystem(long ptr) {
        pointer = ptr;
    }

    public @Nullable
    CelestiaBody getPrimaryObject() {
        long ptr = c_getPrimaryObject(pointer);
        if (ptr == 0)
            return null;
        return new CelestiaBody(ptr);
    }

    public @Nullable
    CelestiaStar getStar() {
        long ptr = c_getStar(pointer);
        if (ptr == 0)
            return null;
        return new CelestiaStar(ptr);
    }

    private native long c_getPrimaryObject(long ptr);
    private native long c_getStar(long ptr);
}
