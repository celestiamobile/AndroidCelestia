package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;

public class CelestiaStarCatalog {
    protected long pointer;

    CelestiaStarCatalog(long ptr) {
        pointer = ptr;
    }

    @NonNull
    public String getStarName(CelestiaStar star) {
        return c_getStarName(star.pointer);
    }

    public int getCount() {
        return c_getCount();
    }

    public CelestiaStar getStar(int index) {
        return new CelestiaStar(c_getStar(index));
    }

    // C functions
    private native String c_getStarName(long pointer);
    private native int c_getCount();
    private native long c_getStar(int index);
}
