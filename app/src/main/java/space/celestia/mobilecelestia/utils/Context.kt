/*
 * Context.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build

val Context.packageInfo: PackageInfo?
    get() {
        val pm = packageManager
        val pn = packageName
        if (pm == null || pn == null)
            return null
        return pm.getPackageInfo(pn, 0)
    }

val Context.versionName: String
    get() {
        val pi = packageInfo ?: return "1.0"
        return pi.versionName
    }

val Context.versionCode: Long
    get() {
        val pi = packageInfo ?: return 1
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pi.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            pi.versionCode.toLong()
        }
    }