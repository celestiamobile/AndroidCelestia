/*
 * CelestiaLocation.java
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;

public class CelestiaLocation extends CelestiaAstroObject {
    CelestiaLocation(long ptr) {
        super(ptr);
    }

    @NonNull
    public String getName() {
        return c_getName();
    }

    // C functions
    private native String c_getName();
}
