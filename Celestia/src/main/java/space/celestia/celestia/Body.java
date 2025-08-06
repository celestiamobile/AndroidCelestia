// Body.java
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

import java.util.Collections;
import java.util.List;

public class Body extends AstroObject {
    public final static int BODY_TYPE_PLANET            = 0x00001;
    public final static int BODY_TYPE_MOON              = 0x00002;
    public final static int BODY_TYPE_ASTEROID          = 0x00004;
    public final static int BODY_TYPE_COMET             = 0x00008;
    public final static int BODY_TYPE_SPACECRAFT        = 0x00010;
    public final static int BODY_TYPE_INVISIBLE         = 0x00020;
    public final static int BODY_TYPE_BARYCENTER        = 0x00040; // Not used (invisible is used instead)
    public final static int BODY_TYPE_SMALL_BODY        = 0x00080; // Not used
    public final static int BODY_TYPE_DWARF_PLANET      = 0x00100;
    public final static int BODY_TYPE_STELLAR           = 0x00200; // only used for orbit mask
    public final static int BODY_TYPE_SURFACE_FEATURE   = 0x00400;
    public final static int BODY_TYPE_COMPONENT         = 0x00800;
    public final static int BODY_TYPE_MINOR_MOON        = 0x01000;
    public final static int BODY_TYPE_DIFFUSE           = 0x02000;
    public final static int BODY_TYPE_UNKNOWN           = 0x10000;

    protected Body(long ptr) {
        super(ptr);
    }

    public int getType() { return c_getType(pointer); }

    @NonNull
    public String getName() {
        return c_getName(pointer);
    }

    public boolean hasRings() { return c_hasRings(pointer); }
    public boolean hasAtmosphere() { return  c_hasAtmosphere(pointer); }
    public boolean isEllipsoid() { return c_isEllipsoid(pointer); }
    public float getRadius() { return c_getRadius(pointer); }

    @Nullable
    String getWebInfoURL() {
        String web = c_getWebInfoURL(pointer);
        if (web.isEmpty())
            return null;
        return c_getWebInfoURL(pointer);
    }

    @NonNull
    public Orbit getOrbitAtTime(double julianDay) {
        return new Orbit(c_getOrbitAtTime(pointer, julianDay));
    }

    @NonNull
    public RotationModel getRotationModelAtTime(double julianDay) {
        return new RotationModel(c_getRotationModelAtTime(pointer, julianDay));
    }

    @Nullable
    public PlanetarySystem getSystem() {
        long ptr = c_getPlanetarySystem(pointer);
        if (ptr == 0)
            return null;
        return new PlanetarySystem(ptr);
    }

    public @NonNull List<String> getAlternateSurfaceNames() {
        List<String> results = c_getAlternateSurfaceNames(pointer);
        if (results == null)
            return Collections.emptyList();
        return results;
    }

    public @NonNull
    Timeline getTimeline() {
        return new Timeline(c_getTimeline(pointer));
    }

    public boolean canBeUsedAsCockpit() {
        return c_canBeUsedAsCockpit(pointer);
    }

    // C functions
    private static native int c_getType(long pointer);
    private static native String c_getName(long pointer);
    private static native boolean c_hasRings(long pointer);
    private static native boolean c_hasAtmosphere(long pointer);
    private static native boolean c_isEllipsoid(long pointer);
    private static native float c_getRadius(long pointer);

    private static native String c_getWebInfoURL(long pointer);

    private static native long c_getOrbitAtTime(long pointer, double julianDay);
    private static native long c_getRotationModelAtTime(long pointer, double julianDay);
    private static native long c_getPlanetarySystem(long pointer);
    private static native List<String> c_getAlternateSurfaceNames(long pointer);

    private static native long c_getTimeline(long pointer);

    private static native boolean c_canBeUsedAsCockpit(long pointer);
}
