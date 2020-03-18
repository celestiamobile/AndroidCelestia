package space.celestia.MobileCelestia.Core;

import androidx.annotation.NonNull;

public class CelestiaSimulation {
    private long pointer;
    private CelestiaUniverse universe;

    public @NonNull
    CelestiaSelection getSelection() {
        return new CelestiaSelection(c_getSelection());
    }

    public void setSelection(@NonNull CelestiaSelection selection) {
        c_setSelection(selection.pointer);
    }

    public @NonNull
    CelestiaUniverse getUniverse() {
        if (universe == null)
            universe = new CelestiaUniverse(c_getUniverse());
        return universe;
    }

    protected CelestiaSimulation(long ptr) {
        pointer = ptr;
    }

    // C functions
    private native long c_getSelection();
    private native void c_setSelection(long ptr);
    private native long c_getUniverse();
}
