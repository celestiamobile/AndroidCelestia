package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;

public class CelestiaLocation extends CelestiaAstroObject {
    CelestiaLocation(long ptr) {
        super(ptr);
    }

    @NonNull
    public String getName() {
        return c_getName();
    }

    // C functions
    private native String c_getName();
}
