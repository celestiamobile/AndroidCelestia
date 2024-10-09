/*
 * Completion.java
 *
 * Copyright (C) 2024-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.celestia;

import androidx.annotation.NonNull;

public class Completion {
    @NonNull
    public final String name;
    @NonNull
    public final Selection selection;

    private Completion(@NonNull String name, @NonNull Selection selection) {
        this.name = name;
        this.selection = selection;
    }
}
