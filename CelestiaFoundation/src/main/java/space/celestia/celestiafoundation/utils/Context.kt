// Context.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiafoundation.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

private val Context.packageInfo: PackageInfo?
    get() {
        val pm = packageManager
        val pn = packageName
        if (pm == null || pn == null)
            return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return pm.getPackageInfo(pn, PackageManager.PackageInfoFlags.of(PackageManager.GET_CONFIGURATIONS.toLong()))
        }
        @Suppress("DEPRECATION")
        return pm.getPackageInfo(pn, PackageManager.GET_CONFIGURATIONS)
    }

val Context.versionName: String
    get() {
        val pi = packageInfo ?: return "1.0"
        return pi.versionName ?: "1.0"
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