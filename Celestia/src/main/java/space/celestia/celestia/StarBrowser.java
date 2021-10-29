/*
 * StarBrowser.java
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.celestia;

import androidx.annotation.NonNull;

import java.util.List;

public class StarBrowser {
    public final static int KIND_NEAREST       = 0;
    public final static int KIND_BRIGHTEST     = 2;
    public final static int KIND_WITH_PLANETS  = 3;

    private long pointer;

    StarBrowser(long pointer) {
        this.pointer = pointer;
    }

    public @NonNull
    List<Star> getStars() {
        return c_getStars(pointer);
    }

    @Override
    protected void finalize() throws Throwable {
        c_destroy(pointer);
        super.finalize();
    }

    private static native void c_destroy(long ptr);
    private static native List<Star> c_getStars(long ptr);
}
