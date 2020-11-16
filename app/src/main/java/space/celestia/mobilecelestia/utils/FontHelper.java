/*
 * FontHelper.java
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Objects;

public class FontHelper {
    private static final String TAG = "FontHelper";

    public static class FontCompat {
        final public @NonNull File file;
        final public int collectionIndex;
        final public @Nullable String name;

        public @NonNull String getFilePath() {
            return file.getPath();
        }

        public FontCompat(@NonNull String filePath, int collectionIndex, @Nullable String name) {
            this.file = new File(filePath);
            this.collectionIndex = collectionIndex;
            this.name = name;
        }

        public FontCompat(@NonNull String filePath, int collectionIndex) {
            this(filePath, collectionIndex, null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FontCompat that = (FontCompat) o;
            return collectionIndex == that.collectionIndex &&
                    getFilePath().equals(that.getFilePath());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getFilePath(), collectionIndex);
        }
    }
}