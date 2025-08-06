// Location.java
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestia;

import androidx.annotation.NonNull;

public class Location extends AstroObject {
    Location(long ptr) {
        super(ptr);
    }

    @NonNull
    public String getName() {
        return c_getName(pointer);
    }

    // C functions
    private static native String c_getName(long pointer);
}
