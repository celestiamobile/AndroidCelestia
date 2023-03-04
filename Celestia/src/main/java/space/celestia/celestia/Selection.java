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

import androidx.annotation.Nullable;

public class Selection {
    private final static int SELECTION_TYPE_NIL             = 0;
    private final static int SELECTION_TYPE_STAR            = 1;
    private final static int SELECTION_TYPE_BODY            = 2;
    private final static int SELECTION_TYPE_DEEP_SKY        = 3;
    private final static int SELECTION_TYPE_LOCATION        = 4;
    private final static int SELECTION_TYPE_GENERIC         = 5;

    final public @Nullable AstroObject object;
    final public int type;

    public Selection() {
        object = null;
        type = SELECTION_TYPE_NIL;
    }

    public Selection(long objectPointer, int type) {
        switch (type)
        {
            case SELECTION_TYPE_STAR:
                object = new Star(objectPointer);
                break;
            case SELECTION_TYPE_BODY:
                object = new Body(objectPointer);
                break;
            case SELECTION_TYPE_DEEP_SKY:
                if (DSOCatalog.c_isDSOGalaxy(objectPointer)) {
                    object = new Galaxy(objectPointer);
                } else {
                    object = new DSO(objectPointer);
                }
                break;
            case SELECTION_TYPE_LOCATION:
                object = new Location(objectPointer);
                break;
            case SELECTION_TYPE_GENERIC:
                object = new AstroObject(objectPointer);
                break;
            case SELECTION_TYPE_NIL:
            default:
                object = null;
                break;
        }
        this.type = type;
    }

    protected Selection(@Nullable AstroObject object, int type) {
        this.object = object;
        this.type = type;
    }

    public Selection(@Nullable AstroObject object) {
        this(object, typeForObject(object));
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

    public boolean isEmpty() {
        return type == SELECTION_TYPE_NIL;
    }

    @Nullable
    public Star getStar() {
        if (object instanceof Star)
            return (Star) object;
        return null;
    }

    @Nullable
    public Location getLocation() {
        if (object instanceof Location)
            return (Location) object;
        return null;
    }

    @Nullable
    public DSO getDSO() {
        if (object instanceof DSO)
            return (DSO) object;
        return null;
    }

    @Nullable
    public Body getBody() {
        if (object instanceof Body)
            return (Body) object;
        return null;
    }

    @Nullable
    public String getWebInfoURL() {
        if (object instanceof Body)
            return ((Body) object).getWebInfoURL();
        if (object instanceof Star)
            return ((Star) object).getWebInfoURL();
        if (object instanceof DSO)
            return ((DSO) object).getWebInfoURL();
        return null;
    }

    public double getRadius() {
        return c_getRadius();
    }

    // Expose helper functions to JNI
    public long getObjectPointer() {
        return object != null ? object.pointer : 0;
    }

    private int getObjectType() {
        return type;
    }

    // C functions
    private native double c_getRadius();
}
