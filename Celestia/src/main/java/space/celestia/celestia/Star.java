// Star.java
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Star extends AstroObject {
    Star(long ptr) {
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
    public UniversalCoord getPositionAtTime(double julianDay) {
        return new UniversalCoord(c_getPositionAtTime(pointer, julianDay));
    }

    public String getSpectralType() {
        return c_getSpectralType(pointer);
    }

    // C functions
    private static native String c_getWebInfoURL(long pointer);
    private static native long c_getPositionAtTime(long pointer, double julianDay);
    private static native String c_getSpectralType(long pointer);
}
