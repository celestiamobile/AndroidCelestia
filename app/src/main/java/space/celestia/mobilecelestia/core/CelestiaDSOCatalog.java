package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;

public class CelestiaDSOCatalog {
    protected long pointer;

    protected CelestiaDSOCatalog(long ptr) {
        pointer = ptr;
    }

    @NonNull
    public String getDSOName(CelestiaDSO dso) {
        return c_getDSOName(dso.pointer);
    }

    public int getCount() {
        return c_getCount();
    }

    public CelestiaDSO getDSO(int index) {
        return new CelestiaDSO(c_getDSO(index));
    }

    // C functions
    private native String c_getDSOName(long pointer);
    private native int c_getCount();
    private native long c_getDSO(int index);
}
