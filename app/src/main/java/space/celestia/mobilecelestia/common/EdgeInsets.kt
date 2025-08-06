// EdgeInsets.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.common

import androidx.core.graphics.Insets
import androidx.core.view.DisplayCutoutCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max

class EdgeInsets {
    val left: Int
    val top: Int
    val right: Int
    val bottom: Int

    constructor(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    constructor(cutout: DisplayCutoutCompat?) : this(
        cutout?.safeInsetLeft ?: 0,
        cutout?.safeInsetTop ?: 0,
        cutout?.safeInsetRight ?: 0,
        cutout?.safeInsetBottom ?: 0)

    constructor(insets: Insets?) : this(
        insets?.left ?: 0,
        insets?.top ?: 0,
        insets?.right ?: 0,
        insets?.bottom ?: 0)

    constructor(insets: WindowInsetsCompat?, roundedCorners: RoundedCorners, hasRegularHorizontalSpace: Boolean) {
        val displayCutout = EdgeInsets(insets?.displayCutout)
        val systemBarInsets = EdgeInsets(insets?.getInsets(WindowInsetsCompat.Type.systemBars()))
        val edgeInsets = EdgeInsets(max(displayCutout.left, systemBarInsets.left), max(displayCutout.top, systemBarInsets.top), max(displayCutout.right, systemBarInsets.right), max(displayCutout.bottom, systemBarInsets.bottom))

        left = if (!hasRegularHorizontalSpace) edgeInsets.left else max(edgeInsets.left, max(roundedCorners.topLeft, roundedCorners.bottomLeft))
        top = if (hasRegularHorizontalSpace) edgeInsets.top else max(edgeInsets.top, max(roundedCorners.topLeft, roundedCorners.topRight))
        right = if (!hasRegularHorizontalSpace) edgeInsets.right else max(edgeInsets.right, max(roundedCorners.topRight, roundedCorners.bottomRight))
        bottom = if (hasRegularHorizontalSpace) edgeInsets.bottom else max(edgeInsets.bottom, max(roundedCorners.bottomLeft, roundedCorners.bottomRight))
    }
}