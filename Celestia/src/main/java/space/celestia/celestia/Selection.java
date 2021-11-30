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

public class Selection implements AutoCloseable {
    private final static int SELECTION_TYPE_NIL             = 0;
    private final static int SELECTION_TYPE_STAR            = 1;
    private final static int SELECTION_TYPE_BODY            = 2;
    private final static int SELECTION_TYPE_DEEP_SKY        = 3;
    private final static int SELECTION_TYPE_LOCATION        = 4;
    private final static int SELECTION_TYPE_GENERIC         = 5;

    protected long pointer;
    private boolean closed = false;

    protected Selection(long ptr) {
        pointer = ptr;
    }

    public Selection(long objectPtr, int type) {
        this(c_createSelection(type, objectPtr));
    }

    @NonNull
    public Selection clone() {
        return new Selection(c_clone(pointer));
    }

    private static int typeForObject(AstroObject object) {
        if (object == null) {
            return SELECTION_TYPE_NIL;
        } else if (object instanceof Star) {
            return SELECTION_TYPE_STAR;
        } else if (object instanceof Body) {
            return SELECTION_TYPE_BODY;
        } else if (object instanceof Location) {
            return SELECTION_TYPE_LOCATION;
        } else if (object instanceof DSO) {
            return SELECTION_TYPE_DEEP_SKY;
        } else {
            return SELECTION_TYPE_GENERIC;
        }
    }

    public Selection (@NonNull AstroObject object) {
        this(object.pointer, typeForObject(object));
    }

    public boolean isEmpty() {
        return c_isEmpty(pointer);
    }

    @Nullable
    public AstroObject getObject() {
        int type = c_getSelectionType(pointer);
        switch (type) {
            case SELECTION_TYPE_STAR:
                return new Star(getSelectionPointer());
            case SELECTION_TYPE_LOCATION:
                return new Location(getSelectionPointer());
            case SELECTION_TYPE_DEEP_SKY:
                return new DSO(getSelectionPointer());
            case SELECTION_TYPE_BODY:
                return new Body(getSelectionPointer());
            case SELECTION_TYPE_GENERIC:
                return new AstroObject(getSelectionPointer());
        }
        return null;
    }

    public long getSelectionPointer() {
        return c_getSelectionPtr(pointer);
    }

    public int getType() {
        return c_getSelectionType(pointer);
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

    @Override
    public void close() throws Exception {
        if (!closed) {
            c_destroy(pointer);
            closed = true;
        }
    }

    // C functions
    private static native boolean c_isEmpty(long pointer);
    private static native int c_getSelectionType(long pointer);
    private static native long c_getSelectionPtr(long pointer);
    private static native double c_getRadius(long pointer);
    private static native void c_destroy(long pointer);
    private static native long c_clone(long pointer);

    private static native long c_createSelection(int type, long pointer);
}
