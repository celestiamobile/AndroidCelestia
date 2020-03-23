package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;

public class CelestiaDSO extends CelestiaAstroObject {
    protected CelestiaDSO(long ptr) {
        super(ptr);
    }

    @NonNull
    public String getWebInfoURL() {
        return c_getWebInfoURL();
    }

    @NonNull
    public String getType() {
        return c_getType();
    }

    // C functions
    private native String c_getWebInfoURL();
    private native String c_getType();
}
