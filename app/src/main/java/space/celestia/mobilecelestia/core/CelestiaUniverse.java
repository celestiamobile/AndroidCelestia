/*
 * CelestiaUniverse.java
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

import java.util.Map;

public class CelestiaUniverse implements CelestiaBrowserItem.ChildrenProvider {
    protected long pointer;
    private CelestiaStarCatalog starCatalog;
    private CelestiaDSOCatalog dsoCatalog;

    CelestiaUniverse(long ptr) {
        pointer = ptr;
    }

    @NonNull
    public CelestiaStarCatalog getStarCatalog() {
        if (starCatalog == null)
            starCatalog = new CelestiaStarCatalog(c_getStarCatalog());
        return starCatalog;
    }

    @NonNull
    public CelestiaDSOCatalog getDSOCatalog() {
        if (dsoCatalog == null)
            dsoCatalog = new CelestiaDSOCatalog(c_getDSOCatalog());
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
            return CelestiaBrowserItem.fromJsonString(c_getChildrenForStar(obj.pointer), this);
        if (obj instanceof CelestiaBody)
            return CelestiaBrowserItem.fromJsonString(c_getChildrenForBody(obj.pointer), this);

        return null;
    }

    public @NonNull CelestiaSelection findObject(@NonNull String name) {
        return new CelestiaSelection(c_findObject(name));
    }

    // C functions
    private native long c_getStarCatalog();
    private native long c_getDSOCatalog();
    private native String c_getChildrenForStar(long pointer);
    private native String c_getChildrenForBody(long pointer);
    private native long c_findObject(String name);
}
