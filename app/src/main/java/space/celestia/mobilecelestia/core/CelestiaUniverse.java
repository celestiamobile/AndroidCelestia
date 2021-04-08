/*
 * CelestiaUniverse.java
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

import java.util.Map;

public class CelestiaUniverse implements CelestiaBrowserItem.ChildrenProvider {
    public static final int MARKER_DIAMOND          = 0;
    public static final int MARKER_TRIANGLE         = 1;
    public static final int MARKER_SQUARE           = 2;
    public static final int MARKER_FILLED_SQUARE    = 3;
    public static final int MARKER_PLUS             = 4;
    public static final int MARKER_X                = 5;
    public static final int MARKER_LEFT_ARROW       = 6;
    public static final int MARKER_RIGHT_ARROW      = 7;
    public static final int MARKER_UP_ARROW         = 8;
    public static final int MARKER_DOWN_ARROW       = 9;
    public static final int MARKER_CIRCLE           = 10;
    public static final int MARKER_DISK             = 11;
    public static final int MARKER_CROSSHAIR        = 12;
    public static final int MARKER_COUNT            = 13;

    protected long pointer;
    private CelestiaStarCatalog starCatalog;
    private CelestiaDSOCatalog dsoCatalog;

    CelestiaUniverse(long ptr) {
        pointer = ptr;
    }

    @NonNull
    public CelestiaStarCatalog getStarCatalog() {
        if (starCatalog == null)
            starCatalog = new CelestiaStarCatalog(c_getStarCatalog(pointer));
        return starCatalog;
    }

    @NonNull
    public CelestiaDSOCatalog getDSOCatalog() {
        if (dsoCatalog == null)
            dsoCatalog = new CelestiaDSOCatalog(c_getDSOCatalog(pointer));
        return dsoCatalog;
    }

    @NonNull
    public String getNameForSelection(CelestiaSelection selection) {
        CelestiaStar star = selection.getStar();
        if (star != null)
            return getStarCatalog().getStarName(star);
        CelestiaDSO dso = selection.getDSO();
        if (dso != null)
            return getDSOCatalog().getDSOName(dso);
        CelestiaBody body = selection.getBody();
        if (body != null)
            return body.getName();
        CelestiaLocation location = selection.getLocation();
        if (location != null)
            return location.getName();
        return "";
    }

    @Override
    public Map<String, CelestiaBrowserItem> childrenForItem(CelestiaBrowserItem item) {
        CelestiaAstroObject obj = item.getObject();

        if (obj == null) { return null; }
        if (obj instanceof CelestiaStar)
            return CelestiaBrowserItem.fromJsonString(c_getChildrenForStar(pointer, obj.pointer), this);
        if (obj instanceof CelestiaBody)
            return CelestiaBrowserItem.fromJsonString(c_getChildrenForBody(pointer, obj.pointer), this);

        return null;
    }

    public @NonNull CelestiaSelection findObject(@NonNull String name) {
        return new CelestiaSelection(c_findObject(pointer, name));
    }

    public void mark(@NonNull CelestiaSelection selection, int marker) {
        c_mark(pointer, selection.pointer, marker);
    }

    public void unmark(@NonNull CelestiaSelection selection) {
        c_unmark(pointer, selection.pointer);
    }

    public void unmarkAll() {
        c_unmarkAll(pointer);
    }

    // C functions
    private static native long c_getStarCatalog(long ptr);
    private static native long c_getDSOCatalog(long ptr);
    private static native String c_getChildrenForStar(long ptr, long pointer);
    private static native String c_getChildrenForBody(long ptr, long pointer);
    private static native long c_findObject(long ptr, String name);

    private static native void c_mark(long ptr, long selection, int marker);
    private static native void c_unmark(long ptr, long selection);
    private static native void c_unmarkAll(long ptr);
}
