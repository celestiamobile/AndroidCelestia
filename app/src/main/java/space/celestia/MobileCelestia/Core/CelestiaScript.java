package space.celestia.MobileCelestia.Core;

import androidx.annotation.NonNull;

import java.util.List;

public class CelestiaScript {
    public final String filename;
    public final String title;

    private CelestiaScript(String filename, String title) {
        this.filename = filename;
        this.title = title;
    }

    public static @NonNull
    List<CelestiaScript> getScriptsInDirectory(@NonNull String path, boolean deepScan) {
        return c_getScriptsInDirectory(path, deepScan);
    }

    private static native List<CelestiaScript> c_getScriptsInDirectory(String path, boolean deepScan);
}
