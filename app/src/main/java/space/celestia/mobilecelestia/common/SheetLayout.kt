/*
 * SheetLayout.kt
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.LayoutDirection
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.customview.widget.ViewDragHelper
import kotlin.math.max
import kotlin.math.min

class SheetLayout(context: Context, attrs: AttributeSet): ViewGroup(context, attrs) {
    private val dragHelper: ViewDragHelper

    private var capturedView: View? = null
    private var capturedViewY: Int? = null

    var edgeInsets: EdgeInsets = EdgeInsets()

    init {
        val callback = object: ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return true
            }

            override fun onViewPositionChanged(
                changedView: View,
                left: Int,
                top: Int,
                dx: Int,
                dy: Int
            ) {
                capturedView = changedView
                capturedViewY = top
            }

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                return left - dx
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                return min(max(top, max(((1 - sheetMaxHeightRatio) * height).toInt(), edgeInsets.top)), (height - sheetHandleHeight * resources.displayMetrics.density - edgeInsets.bottom).toInt())
            }
        }
        dragHelper = ViewDragHelper.create(this, callback)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null)
            return dragHelper.shouldInterceptTouchEvent(ev)
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null)
            dragHelper.processTouchEvent(event)
        return dragHelper.viewDragState != ViewDragHelper.STATE_IDLE
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        if (visibility != VISIBLE) {
            capturedView = null
            capturedViewY = null
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        capturedView = null
        capturedViewY = null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val containerWidth = resolveSizeAndState(0, widthMeasureSpec, 0)
        val containerHeight = resolveSizeAndState(0, heightMeasureSpec, 0)
        val isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val density = resources.displayMetrics.density

        for (i in 0 until childCount) {
            val child = getChildAt(i)

            if (child.visibility == GONE)
                continue

            val childWidth = if (isLandScape) calculateChildWidth(containerWidth, density) else (containerWidth - edgeInsets.left - edgeInsets.right)
            val childHeight = min(containerHeight - edgeInsets.top, (containerHeight * sheetMaxHeightRatio).toInt())
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY))
        }
        setMeasuredDimension(containerWidth, containerHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        val density = resources.displayMetrics.density

        val containerWidth = right - left
        val containerHeight = bottom - top

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE)
                continue

            var y = ((1 - sheetMaxHeightRatio) * containerHeight).toInt()
            if (child == capturedView) {
                var preservedY = capturedViewY
                if (preservedY != null) {
                    y = preservedY
                }
            }

            var sheetHeight = min(containerHeight - edgeInsets.top, (containerHeight * sheetMaxHeightRatio).toInt())
            if (!isLandScape) {
                child.layout(edgeInsets.left, y, containerWidth - edgeInsets.right, y + sheetHeight)
            } else {
                val width = calculateChildWidth(containerWidth, density)
                if (isRTL) {
                    child.layout((containerWidth - edgeInsets.right - width - sheetPaddingLandscapeDp * density).toInt(), y, (containerWidth - edgeInsets.right - sheetPaddingLandscapeDp * density).toInt(), y + sheetHeight)
                } else {
                    child.layout((sheetPaddingLandscapeDp * density + edgeInsets.left).toInt(), y, (width + sheetPaddingLandscapeDp * density + edgeInsets.left).toInt(), y + sheetHeight)
                }
            }
        }
    }

    private fun calculateChildWidth(containerWidth: Int, density: Float): Int {
        var widthUpperBound = containerWidth - edgeInsets.left - edgeInsets.right - sheetPaddingLandscapeDp * 2 * density
        widthUpperBound = min(widthUpperBound, containerWidth * sheetMaxWidthRatio)
        val widthLowerBound = min(widthUpperBound, containerWidth * sheetMinWidthRatio)
        return max(widthLowerBound, min(widthUpperBound, sheetStandardWidthDp * density)).toInt()
    }

    private companion object {
        const val TAG = "SheetLayout"
        const val sheetPaddingLandscapeDp = 16
        const val sheetStandardWidthDp = 320
        const val sheetMinWidthRatio = 0.3f
        const val sheetMaxWidthRatio = 0.5f
        const val sheetMaxHeightRatio = 0.9
        const val sheetHandleHeight = 30
    }
}
