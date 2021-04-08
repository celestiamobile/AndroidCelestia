/*
 * CelestiaSelection.java
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CelestiaSelection {
    private final static int SELECTION_TYPE_NIL             = 0;
    private final static int SELECTION_TYPE_STAR            = 1;
    private final static int SELECTION_TYPE_BODY            = 2;
    private final static int SELECTION_TYPE_DEEP_SKY        = 3;
    private final static int SELECTION_TYPE_LOCATION        = 4;
    private final static int SELECTION_TYPE_GENERIC         = 5;

    protected long pointer;

    public CelestiaSelection(long ptr) {
        pointer = ptr;
    }

    @Nullable
    public static CelestiaSelection create(@NonNull CelestiaAstroObject object) {
        if (object instanceof CelestiaStar) {
            return new CelestiaSelection(c_createSelection(SELECTION_TYPE_STAR, object.pointer));
        } else if (object instanceof CelestiaBody) {
            return new CelestiaSelection(c_createSelection(SELECTION_TYPE_BODY, object.pointer));
        } else if (object instanceof CelestiaDSO) {
            return new CelestiaSelection(c_createSelection(SELECTION_TYPE_DEEP_SKY, object.pointer));
        } else if (object instanceof CelestiaLocation) {
            return new CelestiaSelection(c_createSelection(SELECTION_TYPE_LOCATION, object.pointer));
        }
        return null;
    }

    public boolean isEmpty() {
        return c_isEmpty(pointer);
    }

    @Nullable
    public CelestiaAstroObject getObject() {
        int type = c_getSelectionType(pointer);
        switch (type) {
            case SELECTION_TYPE_STAR:
                return new CelestiaStar(c_getSelectionPtr(pointer));
            case SELECTION_TYPE_LOCATION:
                return new CelestiaLocation(c_getSelectionPtr(pointer));
            case SELECTION_TYPE_DEEP_SKY:
                return new CelestiaDSO(c_getSelectionPtr(pointer));
            case SELECTION_TYPE_BODY:
                return new CelestiaBody(c_getSelectionPtr(pointer));
        }
        return null;
    }

    @Nullable
    public CelestiaStar getStar() {
        CelestiaAstroObject obj = getObject();
        if (obj instanceof CelestiaStar)
            return (CelestiaStar) obj;
        return null;
    }

    @Nullable
    public CelestiaLocation getLocation() {
        CelestiaAstroObject obj = getObject();
        if (obj instanceof CelestiaLocation)
            return (CelestiaLocation) obj;
        return null;
    }

    @Nullable
    public CelestiaDSO getDSO() {
        CelestiaAstroObject obj = getObject();
        if (obj instanceof CelestiaDSO)
            return (CelestiaDSO) obj;
        return null;
    }

    @Nullable
    public CelestiaBody getBody() {
        CelestiaAstroObject obj = getObject();
        if (obj instanceof CelestiaBody)
            return (CelestiaBody) obj;
        return null;
    }

    @Nullable
    public String getWebInfoURL() {
        CelestiaAstroObject object = getObject();
        if (object instanceof CelestiaBody)
            return ((CelestiaBody) object).getWebInfoURL();
        if (object instanceof CelestiaStar)
            return ((CelestiaStar) object).getWebInfoURL();
        if (object instanceof CelestiaDSO)
            return ((CelestiaDSO) object).getWebInfoURL();
        return null;
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
    private static native void c_destroy(long pointer);
    private static native long c_clone(long pointer);

    private static native long c_createSelection(int type, long pointer);
}
