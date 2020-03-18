package space.celestia.MobileCelestia.Core;

import androidx.annotation.NonNull;

public class CelestiaSimulation {
    private long pointer;

    public @NonNull
    CelestiaSelection getSelection() {
        return new CelestiaSelection(c_getSelection());
    }

    public void setSelection(@NonNull CelestiaSelection selection) {
        c_setSelection(selection.pointer);
    }

    protected CelestiaSimulation(long ptr) {
        pointer = ptr;
    }

    // C functions
    private native long c_getSelection();
    private native void c_setSelection(long ptr);
}
