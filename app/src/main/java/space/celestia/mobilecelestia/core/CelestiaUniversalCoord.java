package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;

public class CelestiaUniversalCoord {
    private long pointer;
    protected CelestiaUniversalCoord(long ptr) { pointer = ptr; }

    private static CelestiaUniversalCoord zero = null;

    @NonNull
    public static CelestiaUniversalCoord getZero() {
        if (zero == null)
            zero = new CelestiaUniversalCoord(c_getZero());
        return zero;
    }

    public double distanceFrom(@NonNull CelestiaUniversalCoord otherCoord) {
        return c_distanceFrom(pointer, otherCoord.pointer);
    }

    @NonNull
    public CelestiaUniversalCoord differenceFrom(@NonNull CelestiaUniversalCoord otherCoord) {
        return new CelestiaUniversalCoord(c_differenceFrom(pointer, otherCoord.pointer));
    }

    @NonNull
    public CelestiaVector offsetFrom(@NonNull CelestiaUniversalCoord otherCoord) {
        return c_offsetFrom(pointer, otherCoord.pointer);
    }

    @Override
    protected void finalize() throws Throwable {
        c_destroy();
        super.finalize();
    }

    // C functions
    private static native long c_getZero();

    private static native double c_distanceFrom(long ptr1, long ptr2);
    private static native long c_differenceFrom(long ptr1, long ptr2);
    private static native CelestiaVector c_offsetFrom(long ptr1, long ptr2);

    private native void c_destroy();
}
