/*
 * CelestiaStarBrowser.java
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

import java.util.List;

public class CelestiaStarBrowser {
    public final static int KIND_NEAREST       = 0;
    public final static int KIND_BRIGHTEST     = 2;
    public final static int KIND_WITH_PLANETS  = 3;

    private long pointer;

    CelestiaStarBrowser(long pointer) {
        this.pointer = pointer;
    }

    public @NonNull
    List<CelestiaStar> getStars() {
        return c_getStars();
    }

    @Override
    protected void finalize() throws Throwable {
        c_destroy();
        super.finalize();
    }

    private native void c_destroy();
    private native List<CelestiaStar> c_getStars();
}
