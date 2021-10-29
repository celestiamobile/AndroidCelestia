/*
 * CelestiaInteraction.kt
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
import android.os.Build
import android.util.Log
import android.view.*
import space.celestia.mobilecelestia.core.AppCore

class CelestiaInteraction(context: Context): View.OnTouchListener, View.OnKeyListener, View.OnGenericMotionListener, ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {
    protected val core by lazy { AppCore.shared() }

    enum class InteractionMode {
        Object, Camera;

        val button: Int
            get() = when (this) {
                Camera -> AppCore.MOUSE_BUTTON_LEFT
                Object -> AppCore.MOUSE_BUTTON_RIGHT
            }

        val next: InteractionMode
            get() = when (this) {
                Camera -> Object
                Object -> Camera
            }
    }

    enum class ZoomMode {
        In, Out;

        val distance: Float
            get() = when (this) {
                In -> -1.5f
                Out -> 1.5f
            }
    }

    private val scaleGestureDetector = ScaleGestureDetector(context, this)
    protected val gestureDetector = GestureDetector(context, this)

    var isReady = false
    var zoomMode: ZoomMode? = null
    var scaleFactor: Float = 1f
    var density: Float = 1f

    private var currentSpan: Float? = null
    private var internalInteractionMode = InteractionMode.Object

    private var lastPoint: PointF? = null
    private val isScrolling: Boolean
        get() = lastPoint != null
    private var canScroll = true
    private var canInteract = true
    private var isScaling = false

    private var isRightMouseButtonClicked = false
    private var lastMouseButton = AppCore.MOUSE_BUTTON_LEFT

    fun setInteractionMode(interactionMode: InteractionMode) {
        CelestiaView.callOnRenderThread {
            internalInteractionMode = interactionMode
        }
    }

    fun callZoom() {
        val mode = zoomMode ?: return
        callZoom(mode.distance)
    }

    private fun callZoom(deltaY: Float) {
        if (internalInteractionMode == InteractionMode.Camera) {
            core.mouseMove(AppCore.MOUSE_BUTTON_LEFT, PointF(0.0F, deltaY), AppCore.SHIFT_KEY)
        } else {
            core.mouseWheel(deltaY, 0)
        }
    }

    override fun onGenericMotion(v: View?, event: MotionEvent?): Boolean {
        if (event == null || v == null) { return true }
        if (!isReady) { return true }

        if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
            && event.action == MotionEvent.ACTION_MOVE) {
            // Process the movements starting from the
            // earliest historical position in the batch
            (0 until event.historySize).forEach { i ->
                // Process the event at historical position i
                processJoystickInput(event, i)
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput(event, -1)
            return true
        } else if (event.source == InputDevice.SOURCE_MOUSE && event.action == MotionEvent.ACTION_SCROLL) {
            val y = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
            CelestiaView.callOnRenderThread {
                core.mouseWheel(-y * scaleFactor, event.keyModifier())
            }
            return true
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null || v == null) { return true }
        if (!isReady) { return true }

        if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_POINTER_DOWN)
            isRightMouseButtonClicked = event.isButtonPressed(MotionEvent.BUTTON_SECONDARY)

        fun completion() {
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_POINTER_UP)
                isRightMouseButtonClicked = event.isButtonPressed(MotionEvent.BUTTON_SECONDARY)
        }

        if (!canInteract) {
            // Enable interaction back when last finger is lifted
            if (event.actionMasked == MotionEvent.ACTION_UP)
                canInteract = true
            completion()
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
                v.rootWindowInsets.displayCutout?.let {
                    insetLeft += it.safeInsetLeft
                    insetTop += it.safeInsetTop
                    insetRight += it.safeInsetRight
                    insetBottom += it.safeInsetBottom
                }
            }

            val interactionRect = RectF(insetLeft, insetTop, v.width - insetRight,  v.height - insetBottom)
            if (!interactionRect.contains(point.x, point.y)) {
                Log.d(TAG, "$interactionRect does not contain $point, interaction blocked")
                canInteract = false
                completion()
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
            completion()
            return true
        }

        if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            // Before detected as a scale, we might receiver ACTION_POINTER_DOWN, disable scrolling in advance
            canScroll = false
            completion()
            return true
        }

        // Handle scroll and tap
        if (gestureDetector.onTouchEvent(event)) {
            completion()
            return true
        }

        if (event.actionMasked == MotionEvent.ACTION_UP && isScrolling) {
            // Last finger is lifted while scrolling
            Log.d(TAG, "on scroll end")

            stopScrolling()
            completion()
            return true
        }

        completion()

        // Other events
        Log.d(TAG, "unhandled event, ${event.actionMasked}")
        return true
    }

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
        val deltaY = (1 - delta) * previousSpan / density / 8

        Log.d(TAG, "Pinch with deltaY: $deltaY")

        CelestiaView.callOnRenderThread {
            callZoom(deltaY)
        }

        this.currentSpan = currentSpan

        return true
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        val event = e ?: return true

        Log.d(TAG, "on single tap up")

        var button = AppCore.MOUSE_BUTTON_LEFT
        if (event.isAltPressed() || isRightMouseButtonClicked) {
            button = AppCore.MOUSE_BUTTON_RIGHT
        }

        val point = PointF(event.x, event.y).scaleBy(scaleFactor)
        CelestiaView.callOnRenderThread {
            core.mouseButtonDown(button, point, 0)
            core.mouseButtonUp(button, point, 0)
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

        // When left mouse button is clicked with only CTRL, it is detected with right mouse button with no modifier
        // we need to suppprt CTRL key for other dragging events, so we detect if ALT is pressed, if ALT is also
        // pressed, do not use the alternate behavior
        val useAltenateButton = (event2.isAltPressed() xor isRightMouseButtonClicked) and !(event2.isAltPressed() && event2.isCtrlPressed())
        val button = (if (useAltenateButton) internalInteractionMode.next else internalInteractionMode).button

        val offset = PointF(-distanceX, -distanceY).scaleBy(scaleFactor)
        val originalPoint = PointF(event1.x, event1.y).scaleBy(scaleFactor)
        val newPoint = PointF(event2.x, event2.y).scaleBy(scaleFactor)

        if (!isScrolling) {
            CelestiaView.callOnRenderThread {
                lastMouseButton = button
                core.mouseButtonDown(button, originalPoint, event2.keyModifier())
                core.mouseMove(button, offset, event2.keyModifier())
            }
        } else {
            CelestiaView.callOnRenderThread {
                core.mouseMove(button, offset, event2.keyModifier())
            }
        }
        lastPoint = newPoint
        return true
    }

    override fun onShowPress(e: MotionEvent?) {}

    override fun onLongPress(e: MotionEvent?) {
        if (e == null) return
        if (e.source == InputDevice.SOURCE_MOUSE) return // Mouse long press detected, ignore

        // Bring up the context menu
        val point = PointF(e.x, e.y).scaleBy(scaleFactor)
        val button = AppCore.MOUSE_BUTTON_RIGHT
        CelestiaView.callOnRenderThread {
            core.mouseButtonDown(button, point, 0)
            core.mouseButtonUp(button, point, 0)
        }
    }

    override fun onDown(e: MotionEvent?): Boolean {
        Log.d(TAG, "on down")
        return true
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (v == null || event == null) return false
        if (!isReady) return false

        if (event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) {
            val key = event.keyCode
            if (event.action == KeyEvent.ACTION_UP) {
                CelestiaView.callOnRenderThread {
                    core.joystickButtonUp(key)
                }
                return true
            } else if (event.action == KeyEvent.ACTION_DOWN) {
                CelestiaView.callOnRenderThread {
                    core.joystickButtonDown(key)
                }
                return true
            }
            return false
        }

        if (event.action == KeyEvent.ACTION_UP)
            return onKeyUp(keyCode, event)
        else if (event.action == KeyEvent.ACTION_DOWN)
            return onKeyDown(keyCode, event)
        return false
    }

    private fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (!isReady) return false

        var input = event.unicodeChar
        if ((event.metaState and KeyEvent.META_CTRL_ON) != 0) {
            if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z)
                input = (keyCode - KeyEvent.KEYCODE_A) + 1
        }
        if (keyCode == KeyEvent.KEYCODE_ESCAPE)
            input = 27
        else if (keyCode == KeyEvent.KEYCODE_FORWARD_DEL)
            input = 127
        else if (keyCode == KeyEvent.KEYCODE_DEL)
            input = 8

        CelestiaView.callOnRenderThread {
            core.keyDown(input, keyCode, event.keyModifier())
        }
        return true
    }

    private fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (!isReady) return false
        CelestiaView.callOnRenderThread {
            core.keyUp(event.unicodeChar, keyCode, 0)
        }
        return true
    }

    private fun stopScrolling() {
        Log.d(TAG, "stop scrolling")
        val lp = lastPoint!!

        CelestiaView.callOnRenderThread {
            core.mouseButtonUp(lastMouseButton, lp, 0)
        }
        lastPoint = null
    }

    private fun getCenteredAxis(
        event: MotionEvent,
        device: InputDevice,
        axis: Int,
        historyPos: Int
    ): Float {
        val range: InputDevice.MotionRange? = device.getMotionRange(axis, event.source)

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        range?.apply {
            val value: Float = if (historyPos < 0) {
                event.getAxisValue(axis)
            } else {
                event.getHistoricalAxisValue(axis, historyPos)
            }

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value
            }
        }
        return 0f
    }

    private fun processJoystickInput(event: MotionEvent, historyPos: Int) {

        val inputDevice = event.device

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        var x: Float = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_X, historyPos)
        if (x == 0f) {
            x = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_HAT_X, historyPos)
        }
        if (x == 0f) {
            x = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Z, historyPos)
        }

        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        var y: Float = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Y, historyPos)
        if (y == 0f) {
            y = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_HAT_Y, historyPos)
        }
        if (y == 0f) {
            y = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_RZ, historyPos)
        }

        CelestiaView.callOnRenderThread {
            core.joystickAxis(AppCore.JOYSTICK_AXIS_X, x)
            core.joystickAxis(AppCore.JOYSTICK_AXIS_Y, -y)
        }
    }

    companion object {
        private const val TAG = "CelestiaView"
    }
}

private fun KeyEvent.keyModifier(): Int {
    var modifier = 0
    if ((metaState and KeyEvent.META_CTRL_ON) != 0)
        modifier = modifier or AppCore.CONTROL_KEY
    if ((metaState and KeyEvent.META_SHIFT_ON) != 0)
        modifier = modifier or AppCore.SHIFT_KEY
    return modifier
}

private fun MotionEvent.keyModifier(): Int {
    var modifier = 0
    if ((metaState and KeyEvent.META_CTRL_ON) != 0)
        modifier = modifier or AppCore.CONTROL_KEY
    if ((metaState and KeyEvent.META_SHIFT_ON) != 0)
        modifier = modifier or AppCore.SHIFT_KEY
    return modifier
}

private fun MotionEvent.isAltPressed(): Boolean {
    return metaState and KeyEvent.META_ALT_ON != 0
}

private fun MotionEvent.isCtrlPressed(): Boolean {
    return metaState and KeyEvent.META_CTRL_ON != 0
}