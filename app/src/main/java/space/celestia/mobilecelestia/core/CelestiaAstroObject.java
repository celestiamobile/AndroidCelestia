/*
 * CelestiaAstroObject.java
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.core;

import java.io.Serializable;

public class CelestiaAstroObject implements Serializable {
    protected long pointer;

    protected CelestiaAstroObject(long ptr) {
        pointer = ptr;
    }
}
