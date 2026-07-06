// SafeAreaInsets.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.common

import android.os.Build
import android.view.RoundedCorner
import android.view.WindowManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun rememberSafeAreaInsets(): EdgeInsets {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current

    val hasRegularHorizontalSpace = with(density) { LocalWindowInfo.current.containerSize.width.toDp() } > SHEET_MAX_FULL_WIDTH_DP.dp

    val displayCutout = WindowInsets.displayCutout

    val left = max(0, displayCutout.getLeft(density, layoutDirection))
    val top = max(0, displayCutout.getTop(density))
    val right = max(0, displayCutout.getRight(density, layoutDirection))
    val bottom = max(0, displayCutout.getBottom(density))

    val roundedCorners = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val windowInsets = context.getSystemService(WindowManager::class.java).currentWindowMetrics.windowInsets
        RoundedCorners(
            windowInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)?.radius ?: 0,
            windowInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)?.radius ?: 0,
            windowInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)?.radius ?: 0,
            windowInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT)?.radius ?: 0
        )
    } else {
        RoundedCorners(0, 0, 0, 0)
    }

    return EdgeInsets(
        left = if (!hasRegularHorizontalSpace) left else max(left, max(roundedCorners.topLeft, roundedCorners.bottomLeft)),
        top = if (hasRegularHorizontalSpace) top else max(top, max(roundedCorners.topLeft, roundedCorners.topRight)),
        right = if (!hasRegularHorizontalSpace) right else max(right, max(roundedCorners.topRight, roundedCorners.bottomRight)),
        bottom = if (hasRegularHorizontalSpace) bottom else max(bottom, max(roundedCorners.bottomLeft, roundedCorners.bottomRight))
    )
}
