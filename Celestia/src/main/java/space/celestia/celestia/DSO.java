// DSO.java
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

public class DSO extends AstroObject {
    public static final int OBJECT_TYPE_GALAXY        = 0;
    public static final int OBJECT_TYPE_GLOBULAR      = 1;
    public static final int OBJECT_TYPE_NEBULA        = 2;
    public static final int OBJECT_TYPE_OPEN_CLUSTER  = 3;

    protected DSO(long ptr) {
        super(ptr);
    }

    @Nullable
    String getWebInfoURL() {
        String web = c_getWebInfoURL(pointer);
        if (web.isEmpty())
            return null;
        return c_getWebInfoURL(pointer);
    }

    public int getObjectType() {
        return c_getObjectType(pointer);
    }

    @NonNull
    public String getType() {
        return c_getType(pointer);
    }

    @NonNull
    public Vector getPosition() { return c_getPosition(pointer); }

    @NonNull
    public String getDescription() { return c_getDescription(pointer); }

    // C functions
    private static native String c_getWebInfoURL(long pointer);
    private static native String c_getType(long pointer);
    private static native int c_getObjectType(long pointer);
    private static native Vector c_getPosition(long pointer);
    private static native String c_getDescription(long pointer);
}
