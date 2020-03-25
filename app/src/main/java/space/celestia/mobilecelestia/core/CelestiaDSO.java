package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CelestiaDSO extends CelestiaAstroObject {
    protected CelestiaDSO(long ptr) {
        super(ptr);
    }

    @Nullable
    String getWebInfoURL() {
        String web = c_getWebInfoURL();
        if (web.isEmpty())
            return null;
        return c_getWebInfoURL();
    }

    @NonNull
    public String getType() {
        return c_getType();
    }

    @NonNull
    public CelestiaVector getPosition() { return c_getPosition(); }

    // C functions
    private native String c_getWebInfoURL();
    private native String c_getType();
    private native CelestiaVector c_getPosition();
}
