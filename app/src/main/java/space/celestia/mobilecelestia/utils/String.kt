/*
 * String.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.utils

import space.celestia.celestia.AppCore

@Suppress("FunctionName")
fun CelestiaString(key: String, @Suppress("UNUSED_PARAMETER") comment: String): String {
    return AppCore.getLocalizedString(key)
}