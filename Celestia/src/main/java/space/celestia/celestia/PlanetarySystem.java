// PlanetarySystem.java
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestia;

import androidx.annotation.Nullable;

public class PlanetarySystem {
    private final long pointer;

    protected PlanetarySystem(long ptr) {
        pointer = ptr;
    }

    public @Nullable
    Body getPrimaryObject() {
        long ptr = c_getPrimaryObject(pointer);
        if (ptr == 0)
            return null;
        return new Body(ptr);
    }

    public @Nullable
    Star getStar() {
        long ptr = c_getStar(pointer);
        if (ptr == 0)
            return null;
        return new Star(ptr);
    }

    private native long c_getPrimaryObject(long ptr);
    private native long c_getStar(long ptr);
}
