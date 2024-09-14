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
    var useLandscapeLayout = false

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
                val maxHeight = ((height - edgeInsets.top - edgeInsets.bottom) * sheetMaxHeightRatio).toInt()
                return min(max(top, max(height - maxHeight - edgeInsets.bottom, edgeInsets.top)), (height - sheetHandleHeight * resources.displayMetrics.density - edgeInsets.bottom).toInt())
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
        val density = resources.displayMetrics.density
        val shouldNotOccupyFullWidth = useLandscapeLayout

        for (i in 0 until childCount) {
            val child = getChildAt(i)

            if (child.visibility == GONE)
                continue

            val childWidth = if (shouldNotOccupyFullWidth) calculateChildWidth(containerWidth, density) else (containerWidth - edgeInsets.left - edgeInsets.right)
            val childHeight = min(containerHeight - edgeInsets.top, edgeInsets.bottom + ((containerHeight - edgeInsets.top - edgeInsets.bottom) * sheetMaxHeightRatio).toInt())
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY))
        }
        setMeasuredDimension(containerWidth, containerHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        val density = resources.displayMetrics.density

        val containerWidth = right - left
        val containerHeight = bottom - top

        val shouldNotOccupyFullWidth = useLandscapeLayout

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE)
                continue

            val sheetHeight = min(containerHeight - edgeInsets.top, edgeInsets.bottom + ((containerHeight - edgeInsets.top - edgeInsets.bottom) * sheetMaxHeightRatio).toInt())

            var y = containerHeight - sheetHeight
            if (child == capturedView) {
                val preservedY = capturedViewY
                if (preservedY != null) {
                    y = preservedY
                }
            }

            if (!shouldNotOccupyFullWidth) {
                child.layout(edgeInsets.left, y, containerWidth - edgeInsets.right, y + sheetHeight)
            } else {
                val width = calculateChildWidth(containerWidth, density)
                if (isRTL) {
                    child.layout((containerWidth - edgeInsets.right - width - sheetPaddingNonFullWidthDp * density).toInt(), y, (containerWidth - edgeInsets.right - sheetPaddingNonFullWidthDp * density).toInt(), y + sheetHeight)
                } else {
                    child.layout((sheetPaddingNonFullWidthDp * density + edgeInsets.left).toInt(), y, (width + sheetPaddingNonFullWidthDp * density + edgeInsets.left).toInt(), y + sheetHeight)
                }
            }
        }
    }

    private fun calculateChildWidth(containerWidth: Int, density: Float): Int {
        var widthUpperBound = containerWidth - edgeInsets.left - edgeInsets.right - sheetPaddingNonFullWidthDp * 2 * density
        widthUpperBound = min(widthUpperBound, containerWidth * sheetMaxWidthRatio)
        val widthLowerBound = min(widthUpperBound, containerWidth * sheetMinWidthRatio)
        return max(widthLowerBound, min(widthUpperBound, sheetStandardWidthDp * density)).toInt()
    }

    companion object {
        private const val sheetPaddingNonFullWidthDp = 16
        private const val sheetStandardWidthDp = 320
        private const val sheetMinWidthRatio = 0.3f
        private const val sheetMaxWidthRatio = 0.5f
        private const val sheetMaxHeightRatio = 0.9
        private const val sheetHandleHeight = 30
        const val sheetMaxFullWidthDp = 600
    }
}
