// Universe.java
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

import java.util.Map;

public class Universe implements BrowserItem.ChildrenProvider {
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
    private StarCatalog starCatalog;
    private DSOCatalog dsoCatalog;

    Universe(long ptr) {
        pointer = ptr;
    }

    @NonNull
    public StarCatalog getStarCatalog() {
        if (starCatalog == null)
            starCatalog = new StarCatalog(c_getStarCatalog(pointer));
        return starCatalog;
    }

    @NonNull
    public DSOCatalog getDSOCatalog() {
        if (dsoCatalog == null)
            dsoCatalog = new DSOCatalog(c_getDSOCatalog(pointer));
        return dsoCatalog;
    }

    public @NonNull
    StarBrowser getStarBrowser(int kind, @NonNull Observer observer) {
        return new StarBrowser(c_getStarBrowser(pointer, kind, observer.pointer));
    }

    @NonNull
    public String getNameForSelection(Selection selection) {
        Star star = selection.getStar();
        if (star != null)
            return getStarCatalog().getStarName(star);
        DSO dso = selection.getDSO();
        if (dso != null)
            return getDSOCatalog().getDSOName(dso);
        Body body = selection.getBody();
        if (body != null)
            return body.getName();
        Location location = selection.getLocation();
        if (location != null)
            return location.getName();
        return "";
    }

    @Override
    @Nullable
    public Map<String, BrowserItem> childrenForItem(@NonNull BrowserItem item) {
        AstroObject obj = item.getObject();

        if (obj == null) { return null; }
        if (obj instanceof Star)
            return BrowserItem.fromJsonString(c_getChildrenForStar(pointer, obj.pointer), this);
        if (obj instanceof Body)
            return BrowserItem.fromJsonString(c_getChildrenForBody(pointer, obj.pointer), this);

        return null;
    }

    public void mark(@NonNull Selection selection, int marker) {
        c_mark(pointer, selection, marker);
    }

    public void unmark(@NonNull Selection selection) {
        c_unmark(pointer, selection);
    }

    public void unmarkAll() {
        c_unmarkAll(pointer);
    }

    // C functions
    private static native long c_getStarCatalog(long ptr);
    private static native long c_getDSOCatalog(long ptr);
    private static native long c_getStarBrowser(long pointer, int kind, long observer);
    private static native String c_getChildrenForStar(long ptr, long pointer);
    private static native String c_getChildrenForBody(long ptr, long pointer);

    private static native void c_mark(long ptr, Selection selection, int marker);
    private static native void c_unmark(long ptr, Selection selection);
    private static native void c_unmarkAll(long ptr);
}
