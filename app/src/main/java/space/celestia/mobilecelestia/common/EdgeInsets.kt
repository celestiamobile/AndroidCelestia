/*
 * EdgeInsets.kt
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import android.content.res.Configuration
import androidx.core.view.DisplayCutoutCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max

class EdgeInsets(val left: Int = 0, val top: Int = 0, val right: Int = 0, val bottom: Int = 0) {
    constructor(insets: WindowInsetsCompat?) : this(insets?.displayCutout)

    constructor(cutout: DisplayCutoutCompat?) : this(
        cutout?.safeInsetLeft ?: 0,
        cutout?.safeInsetTop ?: 0,
        cutout?.safeInsetRight ?: 0,
        cutout?.safeInsetBottom ?: 0)

    constructor(edgeInsets: EdgeInsets, roundedCorners: RoundedCorners, configuration: Configuration) : this(
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) edgeInsets.left else max(edgeInsets.left, max(roundedCorners.topLeft, roundedCorners.bottomLeft)),
        if (configuration.orientation != Configuration.ORIENTATION_PORTRAIT) edgeInsets.top else max(edgeInsets.top, max(roundedCorners.topLeft, roundedCorners.topRight)),
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) edgeInsets.right else max(edgeInsets.right, max(roundedCorners.topRight, roundedCorners.bottomRight)),
        if (configuration.orientation != Configuration.ORIENTATION_PORTRAIT) edgeInsets.bottom else max(edgeInsets.bottom, max(roundedCorners.bottomLeft, roundedCorners.bottomRight))
    )
}