package space.celestia.mobilecelestia.core;

import androidx.annotation.Nullable;

public class CelestiaPlanetarySystem {
    private final long pointer;

    protected CelestiaPlanetarySystem(long ptr) {
        pointer = ptr;
    }

    public @Nullable
    CelestiaBody getPrimaryObject() {
        long ptr = c_getPrimaryObject(pointer);
        if (ptr == 0)
            return null;
        return new CelestiaBody(ptr);
    }

    public @Nullable
    CelestiaStar getStar() {
        long ptr = c_getStar(pointer);
        if (ptr == 0)
            return null;
        return new CelestiaStar(ptr);
    }

    private native long c_getPrimaryObject(long ptr);
    private native long c_getStar(long ptr);
}
