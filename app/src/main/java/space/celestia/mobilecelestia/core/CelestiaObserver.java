package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CelestiaObserver {
    private final long pointer;

    protected CelestiaObserver(long ptr) {
        pointer = ptr;
    }

    public @NonNull
    String getDisplayedSurface() {
        return c_getDisplayedSurface(pointer);
    }

    public void setDisplayedSurface(@NonNull String displayedSurface) {
        c_setDisplayedSurface(pointer, displayedSurface);
    }

    private static native String c_getDisplayedSurface(long ptr);
    private static native void c_setDisplayedSurface(long ptr, String displayedSurface);
}
