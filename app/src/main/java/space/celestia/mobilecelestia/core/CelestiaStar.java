package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;

public class CelestiaStar extends CelestiaAstroObject {
    CelestiaStar(long ptr) {
        super(ptr);
    }

    @NonNull
    String getWebInfoURL() {
        return c_getWebInfoURL();
    }

    // C functions
    private native String c_getWebInfoURL();
}
