/*
 * CelestiaScript.java
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.core;

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
