/*
 * Loading.kt
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
import androidx.core.content.res.ResourcesCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import space.celestia.mobilecelestia.R

fun createLoadingDrawable(context: Context): CircularProgressDrawable {
    val drawable = CircularProgressDrawable(context)
    val density = context.resources.displayMetrics.density
    drawable.centerRadius = 15f * density
    drawable.strokeWidth = 3f * density
    drawable.setColorSchemeColors(ResourcesCompat.getColor(context.resources, R.color.colorLabel, null))
    drawable.start()
    return drawable
}