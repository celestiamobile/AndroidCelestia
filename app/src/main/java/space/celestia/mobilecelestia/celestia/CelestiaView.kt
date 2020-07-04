/*
 * CelestiaView.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.celestia

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.PointF
import android.graphics.RectF
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.Log
import android.view.Choreographer
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import space.celestia.mobilecelestia.core.CelestiaAppCore
import java.util.*
import kotlin.collections.HashMap

@SuppressLint("ViewConstructor")
class CelestiaView(context: Context, val scaleFactor: Float) : GLSurfaceView(context), Choreographer.FrameCallback,
    ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {
    enum class InteractionMode {
        Object, Camera;

        val button: Int
            get() = when (this) {
                Camera -> CelestiaAppCore.MOUSE_BUTTON_LEFT
                Object -> CelestiaAppCore.MOUSE_BUTTON_RIGHT
            }
    }

    enum class ZoomMode {
        In, Out;

        val distance: Float
            get() = when (this) {
                In -> -1f
                Out -> 1f
            }
    }

    class Touch(p: PointF, t: Date) {

        var point: PointF = p
        var time: Date = t
        var action = false
    }

    var isReady = false

    private val threshHold: Int = 20 // thresh hold to start a one finger pan (milliseconds)
    private var touchLocations = HashMap<Int, Touch>()
    private var touchActive = false

    private var currentSpan: Float? = null

    var zoomMode: ZoomMode? = null

    private var internalInteractionMode = InteractionMode.Object

    fun setInteractionMode(interactionMode: InteractionMode) {
        queueEvent { internalInteractionMode = interactionMode }
    }

    override fun finalize() {
        Choreographer.getInstance().removeFrameCallback(this)

        super.finalize()
    }

    private val core by lazy { CelestiaAppCore.shared() }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) { return true }
        if (!isReady) { return true }

        if (!canInteract) {
            // Enable interaction back when last finger is lifted
            if (event.actionMasked == MotionEvent.ACTION_UP)
                canInteract = true
            return true
        }

        // Check first finger location before proceed
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            val point = PointF(event.x, event.y)

            val density = Resources.getSystem().displayMetrics.density

            var insetLeft = 16 * density
            var insetTop = 16 * density
            var insetRight = 16 * density
            var insetBottom = 16 * density

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                rootWindowInsets.displayCutout?.let {
                    insetLeft += it.safeInsetLeft
                    insetTop += it.safeInsetTop
                    insetRight += it.safeInsetRight
                    insetBottom += it.safeInsetBottom
                }
            }

            val interactionRect = RectF(insetLeft, insetTop, width - insetRight,  height - insetBottom)
            if (!interactionRect.contains(point.x, point.y)) {
                Log.d(TAG, "$interactionRect does not contain $point, interaction blocked")
                canInteract = false
                return true
            }
        }

        // First test if scaling is in progress
        scaleGestureDetector.onTouchEvent(event)
        if (isScaling) {
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                // Only mark scaling as ended when last finger is lifted
                isScaling = false
                canScroll = true
            }
            return true
        }

        if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            // Before detected as a scale, we might receiver ACTION_POINTER_DOWN, disable scrolling in advance
            canScroll = false
            return true
        }

        // Handle scroll and tap
        if (gestureDetector.onTouchEvent(event))
            return true

        if (event.actionMasked == MotionEvent.ACTION_UP && isScrolling) {
            // Last finger is lifted while scrolling
            Log.d(TAG, "on scroll end")

            stopScrolling()
            return true
        }

        // Other events
        Log.d(TAG, "unhandled event, ${event.actionMasked}")
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        holder.setFixedSize((width * scaleFactor).toInt(), (height * scaleFactor).toInt())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        sharedView = this
    }

    override fun onDetachedFromWindow() {
        sharedView = null

        super.onDetachedFromWindow()
    }

    override fun doFrame(p0: Long) {
        if (isReady) {
            requestRender()
        }

        val mode = zoomMode
        if (mode != null) {
            queueEvent { callZoom(mode.distance) }
        }
        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun callZoom(deltaY: Float) {
        if (internalInteractionMode == InteractionMode.Camera) {
            core.mouseMove(CelestiaAppCore.MOUSE_BUTTON_LEFT, PointF(0.0F, deltaY), CelestiaAppCore.SHIFT_KEY)
        } else {
            core.mouseWheel(deltaY, 0)
        }
    }

    private val scaleGestureDetector = ScaleGestureDetector(context, this)
    private val gestureDetector = GestureDetector(context, this)
    private var lastPoint: PointF? = null
    private val isScrolling: Boolean
        get() = lastPoint != null
    private var canScroll = true
    private var canInteract = true
    private var isScaling = false

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        val det = detector ?: return true

        if (isScrolling)
            stopScrolling()

        Log.d(TAG, "on scale begin")

        currentSpan = det.currentSpan
        isScaling = true

        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        Log.d(TAG, "on scale end")
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        val det = detector ?: return true
        val previousSpan = currentSpan ?: return true
        val currentSpan = det.currentSpan

        Log.d(TAG, "on scale")

        val delta = det.currentSpan / previousSpan
        // FIXME: 8 is a magic number
        val deltaY = (1 - delta) * previousSpan / 8 * scaleFactor

        Log.d(TAG, "Pinch with deltaY: $deltaY")

        callOnRenderThread {
            callZoom(deltaY)
        }

        this.currentSpan = currentSpan

        return true
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        val event = e ?: return true

        Log.d(TAG, "on single tap up")

        val point = PointF(event.x, event.y).scaleBy(scaleFactor)
        callOnRenderThread {
            core.mouseButtonDown(CelestiaAppCore.MOUSE_BUTTON_LEFT, point, 0)
            core.mouseButtonUp(CelestiaAppCore.MOUSE_BUTTON_LEFT, point, 0)
        }

        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d(TAG, "on fling")
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!canScroll) return false

        val event1 = e1 ?: return true
        val event2 = e2 ?: return true

        Log.d(TAG, "on scroll")

        val offset = PointF(-distanceX, -distanceY).scaleBy(scaleFactor)
        val originalPoint = PointF(event1.x, event1.y).scaleBy(scaleFactor)
        val newPoint = PointF(event2.x, event2.y).scaleBy(scaleFactor)

        val button = internalInteractionMode.button
        lastPoint = newPoint
        if (!isScrolling) {
            callOnRenderThread {
                core.mouseButtonDown(button, originalPoint, 0)
                core.mouseMove(button, offset, 0)
            }
        } else {
            callOnRenderThread {
                core.mouseMove(button, offset, 0)
            }
        }
        return true
    }

    override fun onShowPress(e: MotionEvent?) {}

    override fun onLongPress(e: MotionEvent?) {}

    override fun onDown(e: MotionEvent?): Boolean {
        Log.d(TAG, "on down")
        return true
    }

    private fun stopScrolling() {
        Log.d(TAG, "stop scrolling")

        val button = internalInteractionMode.button
        val lp = lastPoint!!

        callOnRenderThread {
            core.mouseButtonUp(button, lp, 0)
        }
        lastPoint = null
    }

    init {
        gestureDetector.setIsLongpressEnabled(false)
        Choreographer.getInstance().postFrameCallback(this)
    }

    companion object {
        private const val TAG = "CelestiaView"
        private var sharedView: CelestiaView? = null

        // Call on render thread to avoid concurrency issue.
        fun callOnRenderThread(block: () -> Unit) {
            sharedView?.queueEvent(block)
        }
    }
}

fun PointF.scaleBy(factor: Float): PointF {
    return PointF(x * factor, y * factor)
}

fun RectF.scaleBy(factor: Float): RectF {
    return RectF(left * factor, top * factor, right * factor, bottom * factor)
}
