package space.celestia.MobileCelestia.Core;

import androidx.annotation.NonNull;

import java.util.List;

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

    public @NonNull List<String> completionForText(@NonNull String text) {
        return c_completionForText(text);
    }

    public @NonNull CelestiaSelection findObject(@NonNull String name) {
        return new CelestiaSelection(c_findObject(name));
    }

    public @NonNull
    CelestiaUniverse getUniverse() {
        if (universe == null)
            universe = new CelestiaUniverse(c_getUniverse());
        return universe;
    }

    public @NonNull
    CelestiaStarBrowser getStarBrowser(int kind) {
        return new CelestiaStarBrowser(c_getStarBrowser(kind));
    }

    public void reverseObserverOrientation() {
        c_reverseObserverOrientation();
    }

    protected CelestiaSimulation(long ptr) {
        pointer = ptr;
    }

    public double getTime() { return c_getTime(); }
    public void setTime(double time) { c_setTime(time); }

    // C functions
    private native long c_getSelection();
    private native void c_setSelection(long ptr);
    private native long c_getUniverse();
    private native List<String> c_completionForText(String text);
    private native long c_findObject(String name);
    private native long c_getStarBrowser(int kind);
    private native void c_reverseObserverOrientation();
    private native double c_getTime();
    private native void c_setTime(double time);
}
