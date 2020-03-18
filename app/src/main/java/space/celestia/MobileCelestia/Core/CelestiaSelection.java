package space.celestia.MobileCelestia.Core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CelestiaSelection {
    private final static int SELECTION_TYPE_NIL             = 0;
    private final static int SELECTION_TYPE_STAR            = 1;
    private final static int SELECTION_TYPE_BODY            = 2;
    private final static int SELECTION_TYPE_DEEP_SKY        = 3;
    private final static int SELECTION_TYPE_LOCATION        = 4;
    private final static int SELECTION_TYPE_GENERIC         = 5;

    protected long pointer;

    protected CelestiaSelection(long ptr) {
        pointer = ptr;
    }

    public CelestiaSelection(CelestiaAstroObject object) {
        if (object instanceof CelestiaStar) {
            pointer = c_createSelection(SELECTION_TYPE_STAR, object.pointer);
        } else if (object instanceof CelestiaBody) {
            pointer = c_createSelection(SELECTION_TYPE_BODY, object.pointer);
        } else if (object instanceof CelestiaDSO) {
            pointer = c_createSelection(SELECTION_TYPE_DEEP_SKY, object.pointer);
        } else if (object instanceof CelestiaLocation) {
            pointer = c_createSelection(SELECTION_TYPE_LOCATION, object.pointer);
        }
    }

    public boolean isEmpty() {
        return c_isEmpty();
    }

    @Nullable
    public CelestiaStar getStar() {
        if (c_getSelectionType() == SELECTION_TYPE_STAR)
            return new CelestiaStar(c_getSelectionPtr());
        return null;
    }

    @Nullable
    public CelestiaLocation getLocation() {
        if (c_getSelectionType() == SELECTION_TYPE_LOCATION)
            return new CelestiaLocation(c_getSelectionPtr());
        return null;
    }

    @Nullable
    public CelestiaDSO getDSO() {
        if (c_getSelectionType() == SELECTION_TYPE_DEEP_SKY)
            return new CelestiaDSO(c_getSelectionPtr());
        return null;
    }

    @Nullable
    public CelestiaBody getBody() {
        if (c_getSelectionType() == SELECTION_TYPE_BODY)
            return new CelestiaBody(c_getSelectionPtr());
        return null;
    }

    @NonNull
    public String getName() {
        return c_getName();
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }

    // C functions
    private native boolean c_isEmpty();
    private native int c_getSelectionType();
    private native long c_getSelectionPtr();
    private native String c_getName();
    private native void destroy();

    private static native long c_createSelection(int type, long pointer);
}
