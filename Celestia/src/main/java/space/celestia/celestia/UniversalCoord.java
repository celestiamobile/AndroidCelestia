/*
 * UniversalCoord.java
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

public class UniversalCoord implements AutoCloseable {
    private long pointer;
    protected UniversalCoord(long ptr) { pointer = ptr; }

    private static UniversalCoord zero = null;
    private boolean closed = false;

    @NonNull
    public static UniversalCoord getZero() {
        if (zero == null)
            zero = new UniversalCoord(c_getZero());
        return zero;
    }

    public double distanceFrom(@NonNull UniversalCoord otherCoord) {
        return c_distanceFrom(pointer, otherCoord.pointer);
    }

    @NonNull
    public Vector offsetFrom(@NonNull UniversalCoord otherCoord) {
        return c_offsetFrom(pointer, otherCoord.pointer);
    }

    @Override
    public void close() throws Exception {
        if (!closed) {
            c_destroy(pointer);
            closed = true;
        }
    }

    // C functions
    private static native long c_getZero();

    private static native double c_distanceFrom(long ptr1, long ptr2);
    private static native Vector c_offsetFrom(long ptr1, long ptr2);

    private static native void c_destroy(long pointer);
}
