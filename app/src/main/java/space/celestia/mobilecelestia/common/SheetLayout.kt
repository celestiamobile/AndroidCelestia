// SheetLayout.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class SheetDetent { Hidden, PartiallyExpanded, Expanded }

@Composable
fun SheetLayout(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val containerSize = LocalWindowInfo.current.containerSize
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val fullHeightPx = constraints.maxHeight.toFloat()
        val isWideScreen = with(density) { containerSize.width.toDp() } > SHEET_MAX_FULL_WIDTH_DP.dp

        val cutoutTop = with(density) { WindowInsets.displayCutout.getTop(this).toFloat() }
        val cutoutBottom = with(density) { WindowInsets.displayCutout.getBottom(this).toFloat() }
        val safeHeightPx = fullHeightPx - cutoutTop - cutoutBottom
        val sheetHeightPx = cutoutBottom + safeHeightPx * SHEET_MAX_HEIGHT_RATIO

        val expandedOffsetPx = fullHeightPx - sheetHeightPx
        val partialOffsetPx = fullHeightPx * 0.5f
        val hiddenOffsetPx = fullHeightPx

        val state = remember {
            AnchoredDraggableState(initialValue = if (visible) SheetDetent.Expanded else SheetDetent.Hidden)
        }

        SideEffect {
            if (fullHeightPx > 0) {
                state.updateAnchors(
                    newAnchors = DraggableAnchors {
                        SheetDetent.Hidden at hiddenOffsetPx
                        SheetDetent.PartiallyExpanded at partialOffsetPx
                        SheetDetent.Expanded at expandedOffsetPx
                    },
                    newTarget = if (visible) state.currentValue else SheetDetent.Hidden
                )
            }
        }

        LaunchedEffect(visible) {
            state.animateTo(if (visible) SheetDetent.Expanded else SheetDetent.Hidden)
        }

        val currentOnDismiss by rememberUpdatedState(onDismiss)
        LaunchedEffect(Unit) {
            var previousValue = state.currentValue
            snapshotFlow { state.currentValue }
                .collect { value ->
                    if (value == SheetDetent.Hidden && previousValue != SheetDetent.Hidden) {
                        currentOnDismiss()
                    }
                    previousValue = value
                }
        }

        val nestedScrollConnection = remember(state) {
            SheetNestedScrollConnection(state)
        }

        val sheetOffset by remember {
            derivedStateOf { state.offset }
        }
        val isSheetVisible = !sheetOffset.isNaN() && sheetOffset < fullHeightPx

        if (isSheetVisible) {
            // Sheet
            val sheetAlignment = if (isWideScreen) {
                if (layoutDirection == LayoutDirection.Rtl) Alignment.BottomEnd else Alignment.BottomStart
            } else {
                Alignment.BottomCenter
            }

            val safeStart = with(density) {
                val insets = WindowInsets.safeDrawing
                if (layoutDirection == LayoutDirection.Rtl) insets.getRight(this, layoutDirection).toDp()
                else insets.getLeft(this, layoutDirection).toDp()
            }

            val sheetWidthModifier = if (isWideScreen) {
                val containerWidthDp = with(density) { containerSize.width.toDp() }
                val widthDp = if (containerWidthDp > SHEET_WIDE_THRESHOLD_DP.dp) SHEET_WIDE_WIDTH_DP.dp else SHEET_NARROW_WIDTH_DP.dp
                Modifier.padding(start = safeStart + SHEET_LANDSCAPE_PADDING_DP.dp).width(widthDp)
            } else {
                Modifier.fillMaxWidth()
            }

            val yOffset = (sheetOffset - expandedOffsetPx).roundToInt().coerceAtLeast(0)

            Surface(
                modifier = Modifier
                    .align(sheetAlignment)
                    .then(sheetWidthModifier)
                    .height(with(density) { sheetHeightPx.toDp() })
                    .offset { IntOffset(0, yOffset) },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shadowElevation = 1.dp,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .anchoredDraggable(state, Orientation.Vertical),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                    Box(modifier = Modifier.weight(1f).nestedScroll(nestedScrollConnection)) {
                        content()
                    }
                }
            }
        }
    }
}

private class SheetNestedScrollConnection(
    private val state: AnchoredDraggableState<SheetDetent>
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y
        return if (delta < 0 && source == NestedScrollSource.UserInput) {
            Offset(0f, state.dispatchRawDelta(delta))
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        return if (source == NestedScrollSource.UserInput) {
            Offset(0f, state.dispatchRawDelta(available.y))
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val toFling = available.y
        return if (toFling < 0 && state.requireOffset() > state.anchors.minPosition()) {
            state.animateTo(state.targetValue)
            available
        } else {
            Velocity.Zero
        }
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        state.animateTo(state.targetValue)
        return available
    }
}

const val SHEET_MAX_FULL_WIDTH_DP = 600

private const val SHEET_MAX_HEIGHT_RATIO = 0.9f
private const val SHEET_LANDSCAPE_PADDING_DP = 16
private const val SHEET_WIDE_THRESHOLD_DP = 1024
private const val SHEET_WIDE_WIDTH_DP = 393
private const val SHEET_NARROW_WIDTH_DP = 320
