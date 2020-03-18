package space.celestia.MobileCelestia.Core;

import androidx.annotation.NonNull;

public class CelestiaStar extends CelestiaAstroObject {
    protected CelestiaStar(long ptr) {
        super(ptr);
    }

    @NonNull
    public String getWebInfoURL() {
        return c_getWebInfoURL();
    }

    // C functions
    private native String c_getWebInfoURL();
}
