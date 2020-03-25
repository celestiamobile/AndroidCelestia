package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CelestiaBody extends CelestiaAstroObject {
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

    protected CelestiaBody(long ptr) {
        super(ptr);
    }

    public int getType() { return c_getType(); }

    @NonNull
    public String getName() {
        return c_getName();
    }

    public boolean hasRings() { return c_hasRings(); }
    public boolean hasAtmosphere() { return  c_hasAtmosphere(); }
    public boolean isEllipsoid() { return c_isEllipsoid(); }
    public float getRadius() { return c_getRadius(); }

    @Nullable
    String getWebInfoURL() {
        String web = c_getWebInfoURL();
        if (web.isEmpty())
            return null;
        return c_getWebInfoURL();
    }

    @NonNull
    public CelestiaOrbit getOrbitAtTime(double julianDay) {
        return new CelestiaOrbit(c_getOrbitAtTime(julianDay));
    }

    @NonNull
    public CelestiaRotationModel getRotationModelAtTime(double julianDay) {
        return new CelestiaRotationModel(c_getRotationModelAtTime(julianDay));
    }

    // C functions
    private native int c_getType();
    private native String c_getName();
    private native boolean c_hasRings();
    private native boolean c_hasAtmosphere();
    private native boolean c_isEllipsoid();
    private native float c_getRadius();

    private native String c_getWebInfoURL();

    private native long c_getOrbitAtTime(double julianDay);
    private native long c_getRotationModelAtTime(double julianDay);
}
