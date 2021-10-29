/*
 * Selection.java
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
import androidx.annotation.Nullable;

public class Selection {
    private final static int SELECTION_TYPE_NIL             = 0;
    private final static int SELECTION_TYPE_STAR            = 1;
    private final static int SELECTION_TYPE_BODY            = 2;
    private final static int SELECTION_TYPE_DEEP_SKY        = 3;
    private final static int SELECTION_TYPE_LOCATION        = 4;
    private final static int SELECTION_TYPE_GENERIC         = 5;

    protected long pointer;

    public Selection(long ptr) {
        pointer = ptr;
    }

    @Nullable
    public static Selection create(@NonNull AstroObject object) {
        if (object instanceof Star) {
            return new Selection(c_createSelection(SELECTION_TYPE_STAR, object.pointer));
        } else if (object instanceof Body) {
            return new Selection(c_createSelection(SELECTION_TYPE_BODY, object.pointer));
        } else if (object instanceof DSO) {
            return new Selection(c_createSelection(SELECTION_TYPE_DEEP_SKY, object.pointer));
        } else if (object instanceof Location) {
            return new Selection(c_createSelection(SELECTION_TYPE_LOCATION, object.pointer));
        }
        return null;
    }

    public boolean isEmpty() {
        return c_isEmpty(pointer);
    }

    @Nullable
    public AstroObject getObject() {
        int type = c_getSelectionType(pointer);
        switch (type) {
            case SELECTION_TYPE_STAR:
                return new Star(c_getSelectionPtr(pointer));
            case SELECTION_TYPE_LOCATION:
                return new Location(c_getSelectionPtr(pointer));
            case SELECTION_TYPE_DEEP_SKY:
                return new DSO(c_getSelectionPtr(pointer));
            case SELECTION_TYPE_BODY:
                return new Body(c_getSelectionPtr(pointer));
        }
        return null;
    }

    @Nullable
    public Star getStar() {
        AstroObject obj = getObject();
        if (obj instanceof Star)
            return (Star) obj;
        return null;
    }

    @Nullable
    public Location getLocation() {
        AstroObject obj = getObject();
        if (obj instanceof Location)
            return (Location) obj;
        return null;
    }

    @Nullable
    public DSO getDSO() {
        AstroObject obj = getObject();
        if (obj instanceof DSO)
            return (DSO) obj;
        return null;
    }

    @Nullable
    public Body getBody() {
        AstroObject obj = getObject();
        if (obj instanceof Body)
            return (Body) obj;
        return null;
    }

    @Nullable
    public String getWebInfoURL() {
        AstroObject object = getObject();
        if (object instanceof Body)
            return ((Body) object).getWebInfoURL();
        if (object instanceof Star)
            return ((Star) object).getWebInfoURL();
        if (object instanceof DSO)
            return ((DSO) object).getWebInfoURL();
        return null;
    }

    public double getRadius() {
        return c_getRadius(pointer);
    }

    public long createCopy() {
        return c_clone(pointer);
    }

    @Override
    protected void finalize() throws Throwable {
        c_destroy(pointer);
        super.finalize();
    }

    // C functions
    private static native boolean c_isEmpty(long pointer);
    private static native int c_getSelectionType(long pointer);
    private static native long c_getSelectionPtr(long pointer);
    private static native String c_getName(long pointer);
    private static native double c_getRadius(long pointer);
    private static native void c_destroy(long pointer);
    private static native long c_clone(long pointer);

    private static native long c_createSelection(int type, long pointer);
}
