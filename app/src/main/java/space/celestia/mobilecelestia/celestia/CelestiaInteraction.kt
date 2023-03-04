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
import androidx.core.view.GestureDetectorCompat
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.utils.PreferenceManager
import kotlin.math.abs

class CelestiaInteraction(context: Context, private val appCore: AppCore, private val executor: CelestiaExecutor, interactionMode: InteractionMode, private val appSettings: PreferenceManager, private val canAcceptKeyEvents: () -> Boolean, private val showMenu: () -> Unit): View.OnTouchListener, View.OnKeyListener, View.OnGenericMotionListener, ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {
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

        companion object {
            fun fromButton(button: Int): InteractionMode {
                return when (button) {
                    AppCore.MOUSE_BUTTON_LEFT -> Camera
                    AppCore.MOUSE_BUTTON_RIGHT -> Object
                    else -> Object
                }
            }
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
    private val gestureDetector = GestureDetectorCompat(context, this)

    var isReady = false
    var zoomMode: ZoomMode? = null
    var scaleFactor: Float = 1f
    var density: Float = 1f
    private var isLeftTriggerPressed = false
    private var isRightTriggerPressed = false

    private var currentSpan: Float? = null
    private var internalInteractionMode = interactionMode

    private var lastPoint: PointF? = null
    private val isScrolling: Boolean
        get() = lastPoint != null
    private var canScroll = true
    private var canInteract = true
    private var isScaling = false

    private var isRightMouseButtonClicked = false
    private var lastMouseButton = AppCore.MOUSE_BUTTON_LEFT

    fun setInteractionMode(interactionMode: InteractionMode) {
        executor.execute {
            internalInteractionMode = interactionMode
        }
    }

    fun callZoom() {
        val mode = zoomMode ?: return
        callZoom(mode.distance)
    }

    private fun callZoom(deltaY: Float) {
        if (internalInteractionMode == InteractionMode.Camera) {
            appCore.mouseMove(AppCore.MOUSE_BUTTON_LEFT, PointF(0.0F, deltaY), AppCore.SHIFT_KEY)
        } else {
            appCore.mouseWheel(deltaY, 0)
        }
    }

    override fun onGenericMotion(v: View?, event: MotionEvent?): Boolean {
        if (event == null || v == null) { return true }
        if (!isReady) { return true }

        if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
            && event.action == MotionEvent.ACTION_MOVE) {
            if (!canAcceptKeyEvents()) return false
            // Process the movements starting from the
            // earliest historical position in the batch
            (0 until event.historySize).forEach { i ->
                // Process the event at historical position i
                processJoystickInput(event, i)
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput(event, -1)

            // Need to handle LT/RT here as buttons
            val isLeftTriggerPressedNow = event.getAxisValue(MotionEvent.AXIS_LTRIGGER) > 0.5 || event.getAxisValue(MotionEvent.AXIS_BRAKE) > 0.5
            if (isLeftTriggerPressedNow != isLeftTriggerPressed) {
                isLeftTriggerPressed = isLeftTriggerPressedNow
                processJoystickButton(KeyEvent.KEYCODE_BUTTON_L2, !isLeftTriggerPressedNow)
            }

            val isRightTriggerPressedNow = event.getAxisValue(MotionEvent.AXIS_RTRIGGER) > 0.5 || event.getAxisValue(MotionEvent.AXIS_GAS) > 0.5
            if (isRightTriggerPressedNow != isRightTriggerPressed) {
                isRightTriggerPressed = isRightTriggerPressedNow
                processJoystickButton(KeyEvent.KEYCODE_BUTTON_R2, !isRightTriggerPressedNow)
            }

            return true
        } else if (event.source == InputDevice.SOURCE_MOUSE && event.action == MotionEvent.ACTION_SCROLL) {
            val y = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
            executor.execute {
                appCore.mouseWheel(-y * scaleFactor, event.keyModifier())
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
        try {
            // Listeners in Android T SDK do not have optional parameters, might
            // cause NullPointerException, ignore it
            scaleGestureDetector.onTouchEvent(event)
        } catch (ignored: Throwable) {}

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
        try {
            // Listeners in Android T SDK do not have optional parameters, might
            // cause NullPointerException, ignore it
            if (gestureDetector.onTouchEvent(event)) {
                completion()
                return true
            }
        } catch(ignored: Throwable) {
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

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        if (isScrolling)
            stopScrolling()

        Log.d(TAG, "on scale begin")

        currentSpan = detector.currentSpan
        isScaling = true

        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        Log.d(TAG, "on scale end")
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val previousSpan = currentSpan ?: return true
        val currentSpan = detector.currentSpan

        Log.d(TAG, "on scale")

        val delta = detector.currentSpan / previousSpan
        // FIXME: 8 is a magic number
        val deltaY = (1 - delta) * previousSpan / density / 8

        Log.d(TAG, "Pinch with deltaY: $deltaY")

        executor.execute {
            callZoom(deltaY)
        }

        this.currentSpan = currentSpan

        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.d(TAG, "on single tap up")

        var button = AppCore.MOUSE_BUTTON_LEFT
        if (e.isAltPressed() || isRightMouseButtonClicked) {
            button = AppCore.MOUSE_BUTTON_RIGHT
        }

        val point = PointF(e.x, e.y).scaleBy(scaleFactor)
        executor.execute {
            appCore.mouseButtonDown(button, point, 0)
            appCore.mouseButtonUp(button, point, 0)
        }

        return true
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d(TAG, "on fling")
        return false
    }

    override fun onScroll(
        event1: MotionEvent,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!canScroll) return false

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
            executor.execute {
                lastMouseButton = button
                appCore.mouseButtonDown(button, originalPoint, event2.keyModifier())
                appCore.mouseMove(button, offset, event2.keyModifier())
            }
        } else {
            executor.execute {
                appCore.mouseMove(button, offset, event2.keyModifier())
            }
        }
        lastPoint = newPoint
        return true
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onLongPress(e: MotionEvent) {
        if (e.source == InputDevice.SOURCE_MOUSE) return // Mouse long press detected, ignore

        // Bring up the context menu
        val point = PointF(e.x, e.y).scaleBy(scaleFactor)
        val button = AppCore.MOUSE_BUTTON_RIGHT
        executor.execute {
            appCore.mouseButtonDown(button, point, 0)
            appCore.mouseButtonUp(button, point, 0)
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
        Log.d(TAG, "on down")
        return true
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (v == null || event == null) return false
        if (!isReady) return false
        if (!canAcceptKeyEvents()) return false

        if (event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) {
            val key = event.keyCode
            if (event.action == KeyEvent.ACTION_UP) {
                processJoystickButton(key, true)
                return true
            } else if (event.action == KeyEvent.ACTION_DOWN) {
                processJoystickButton(key, false)
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

    private fun joystickButtonKeyAction(keyCode: Int): Int {
        return when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> { appSettings[PreferenceManager.PredefinedKey.ControllerRemapA]?.toIntOrNull() ?: GAME_CONTROLLER_BUTTON_ACTION_MOVE_SLOWER }
            KeyEvent.KEYCODE_BUTTON_B -> { appSettings[PreferenceManager.PredefinedKey.ControllerRemapB]?.toIntOrNull() ?: GAME_CONTROLLER_BUTTON_ACTION_NONE }
            KeyEvent.KEYCODE_BUTTON_X -> { appSettings[PreferenceManager.PredefinedKey.ControllerRemapX]?.toIntOrNull() ?: GAME_CONTROLLER_BUTTON_ACTION_MOVE_FASTER }
            KeyEvent.KEYCODE_BUTTON_Y -> { appSettings[PreferenceManager.PredefinedKey.ControllerRemapY]?.toIntOrNull() ?: GAME_CONTROLLER_BUTTON_ACTION_NONE }
            KeyEvent.KEYCODE_DPAD_UP -> { appSettings[PreferenceManager.PredefinedKey.ControllerRemapDpadUp]?.toIntOrNull() ?: GAME_CONTROLLER_BUTTON_ACTION_PITCH_UP }
            KeyEvent.KEYCODE_DPAD_DOWN -> { appSettings[PreferenceManager.PredefinedKey.ControllerRemapDpadDown]?.toIntOrNull() ?: GAME_CONTROLLER_BUTTON_ACTION_PITCH_DOWN }
            KeyEvent.KEYCODE_DPAD_LEFT -> { appSettings[PreferenceManager.PredefinedKey.ControllerRemapDpadLeft]?.toIntOrNull() ?: GAME_CONTROLLER_BUTTON_ACTION_YAW_LEFT }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { appSettings[PreferenceManager.PredefinedKey.ControllerRemapDpadRight]?.toIntOrNull() ?: GAME_CONTROLLER_BUTTON_ACTION_YAW_RIGHT }
            KeyEvent.KEYCODE_BUTTON_L1 -> { appSettings[PreferenceManager.PredefinedKey.ControllerRemapLB]?.toIntOrNull() ?: GAME_CONTROLLER_BUTTON_ACTION_NONE }
            KeyEvent.KEYCODE_BUTTON_L2 -> { appSettings[PreferenceManager.PredefinedKey.ControllerRemapLT]?.toIntOrNull() ?: GAME_CONTROLLER_BUTTON_ACTION_ROLL_LEFT }
            KeyEvent.KEYCODE_BUTTON_R1 -> { appSettings[PreferenceManager.PredefinedKey.ControllerRemapRB]?.toIntOrNull() ?: GAME_CONTROLLER_BUTTON_ACTION_NONE }
            KeyEvent.KEYCODE_BUTTON_R2 -> { appSettings[PreferenceManager.PredefinedKey.ControllerRemapRT]?.toIntOrNull() ?: GAME_CONTROLLER_BUTTON_ACTION_ROLL_RIGHT }
            KeyEvent.KEYCODE_BUTTON_START -> GAME_CONTROLLER_BUTTON_ACTION_SHOW_MENU
            else -> GAME_CONTROLLER_BUTTON_ACTION_NONE
        }
    }

    private fun processJoystickButton(keyCode: Int, up: Boolean) {
        when (joystickButtonKeyAction(keyCode)) {
            GAME_CONTROLLER_BUTTON_ACTION_MOVE_FASTER -> {
                executor.execute { if (up) appCore.joystickButtonUp(KeyEvent.KEYCODE_BUTTON_X) else appCore.joystickButtonDown(KeyEvent.KEYCODE_BUTTON_X)  }
            }
            GAME_CONTROLLER_BUTTON_ACTION_MOVE_SLOWER -> {
                executor.execute { if (up) appCore.joystickButtonUp(KeyEvent.KEYCODE_BUTTON_A) else appCore.joystickButtonDown(KeyEvent.KEYCODE_BUTTON_A)  }
            }
            GAME_CONTROLLER_BUTTON_ACTION_STOP_SPEED -> {
                if (up) {
                    executor.execute { appCore.charEnter(115) }
                }
            }
            GAME_CONTROLLER_BUTTON_ACTION_REVERSE_SPEED -> {
                if (up) {
                    executor.execute { appCore.charEnter(113) }
                }
            }
            GAME_CONTROLLER_BUTTON_ACTION_REVERSE_ORIENTATION -> {
                if (up) {
                    executor.execute { appCore.simulation.reverseObserverOrientation() }
                }
            }
            GAME_CONTROLLER_BUTTON_ACTION_PITCH_UP -> {
                executor.execute { if (up) appCore.keyUp(26) else appCore.keyDown(26)  }
            }
            GAME_CONTROLLER_BUTTON_ACTION_PITCH_DOWN -> {
                executor.execute { if (up) appCore.keyUp(32) else appCore.keyDown(32)  }
            }
            GAME_CONTROLLER_BUTTON_ACTION_YAW_LEFT -> {
                executor.execute { if (up) appCore.keyUp(28) else appCore.keyDown(28)  }
            }
            GAME_CONTROLLER_BUTTON_ACTION_YAW_RIGHT -> {
                executor.execute { if (up) appCore.keyUp(30) else appCore.keyDown(30)  }
            }
            GAME_CONTROLLER_BUTTON_ACTION_ROLL_LEFT -> {
                executor.execute { if (up) appCore.keyUp(31) else appCore.keyDown(31)  }
            }
            GAME_CONTROLLER_BUTTON_ACTION_ROLL_RIGHT -> {
                executor.execute { if (up) appCore.keyUp(33) else appCore.keyDown(33)  }
            }
            GAME_CONTROLLER_BUTTON_ACTION_TAP_CENTER -> {
                executor.execute {
                    val width = appCore.width
                    val height = appCore.height
                    val center = PointF(width.toFloat() / 2.0f, height.toFloat() / 2.0f)
                    if (up) appCore.mouseButtonUp(AppCore.MOUSE_BUTTON_LEFT, center, 0) else appCore.mouseButtonDown(AppCore.MOUSE_BUTTON_LEFT, center, 0)
                }
            }
            GAME_CONTROLLER_BUTTON_ACTION_GO_TO -> {
                if (up) {
                    executor.execute { appCore.charEnter(103) }
                }
            }
            GAME_CONTROLLER_BUTTON_ACTION_ESC -> {
                if (up) {
                    executor.execute { appCore.charEnter(27) }
                }
            }
            GAME_CONTROLLER_BUTTON_ACTION_SHOW_MENU -> { showMenu() }
            GAME_CONTROLLER_BUTTON_ACTION_NONE -> { }
            else -> { }
        }
    }

    private fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (!canAcceptKeyEvents()) return false

        var input = event.unicodeChar
        if ((event.metaState and KeyEvent.META_CTRL_ON) != 0) {
            if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z)
                input = (keyCode - KeyEvent.KEYCODE_A) + 1
        }
        when (keyCode) {
            KeyEvent.KEYCODE_ESCAPE -> input = 27
            KeyEvent.KEYCODE_FORWARD_DEL -> input = 127
            KeyEvent.KEYCODE_DEL -> input = 8
        }

        executor.execute {
            appCore.keyDown(input, keyCode, event.keyModifier())
        }
        return true
    }

    private fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        executor.execute {
            appCore.keyUp(event.unicodeChar, keyCode, 0)
        }
        return true
    }

    private fun stopScrolling() {
        Log.d(TAG, "stop scrolling")
        val lp = lastPoint!!

        executor.execute {
            appCore.mouseButtonUp(lastMouseButton, lp, 0)
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
            if (abs(value) > flat) {
                return value
            }
        }
        return 0f
    }

    private fun processJoystickInput(event: MotionEvent, historyPos: Int) {

        val inputDevice = event.device

        // Calculate the horizontal distance to move by
        // using the input value combined from these physical controls:
        // the left control stick, hat axis, or the right control stick.
        var x: Float = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_X, historyPos)
        x += getCenteredAxis(event, inputDevice, MotionEvent.AXIS_HAT_X, historyPos)
        x += getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Z, historyPos)

        // Calculate the vertical distance to move by
        // using the input value combined from these physical controls:
        // the left control stick, hat switch, or the right control stick.
        var y: Float = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Y, historyPos)
        y += getCenteredAxis(event, inputDevice, MotionEvent.AXIS_HAT_Y, historyPos)
        y += getCenteredAxis(event, inputDevice, MotionEvent.AXIS_RZ, historyPos)

        val shouldInvertX = appSettings[PreferenceManager.PredefinedKey.ControllerInvertX] == "true"
        val shouldInvertY = appSettings[PreferenceManager.PredefinedKey.ControllerInvertY] == "true"

        executor.execute {
            appCore.joystickAxis(AppCore.JOYSTICK_AXIS_X, if (shouldInvertX) -x else x)
            appCore.joystickAxis(AppCore.JOYSTICK_AXIS_Y, if (shouldInvertY) y else -y)
        }
    }

    companion object {
        private const val TAG = "CelestiaView"

        const val GAME_CONTROLLER_BUTTON_ACTION_NONE = 0
        const val GAME_CONTROLLER_BUTTON_ACTION_MOVE_FASTER = 1
        const val GAME_CONTROLLER_BUTTON_ACTION_MOVE_SLOWER = 2
        const val GAME_CONTROLLER_BUTTON_ACTION_STOP_SPEED = 3
        const val GAME_CONTROLLER_BUTTON_ACTION_REVERSE_SPEED = 4
        const val GAME_CONTROLLER_BUTTON_ACTION_REVERSE_ORIENTATION = 5
        const val GAME_CONTROLLER_BUTTON_ACTION_TAP_CENTER = 6
        const val GAME_CONTROLLER_BUTTON_ACTION_GO_TO = 7
        const val GAME_CONTROLLER_BUTTON_ACTION_ESC = 8
        const val GAME_CONTROLLER_BUTTON_ACTION_PITCH_UP = 9
        const val GAME_CONTROLLER_BUTTON_ACTION_PITCH_DOWN = 10
        const val GAME_CONTROLLER_BUTTON_ACTION_YAW_LEFT = 11
        const val GAME_CONTROLLER_BUTTON_ACTION_YAW_RIGHT = 12
        const val GAME_CONTROLLER_BUTTON_ACTION_ROLL_LEFT = 13
        const val GAME_CONTROLLER_BUTTON_ACTION_ROLL_RIGHT = 14
        const val GAME_CONTROLLER_BUTTON_ACTION_SHOW_MENU = 15
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