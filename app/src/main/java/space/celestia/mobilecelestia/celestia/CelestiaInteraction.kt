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
import androidx.annotation.RequiresApi
import androidx.core.view.GestureDetectorCompat
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.utils.PreferenceManager
import java.lang.ref.WeakReference
import kotlin.math.abs

class CelestiaInteraction(context: Context, private val appCore: AppCore, private val executor: CelestiaExecutor, interactionMode: InteractionMode, private val appSettings: PreferenceManager, private val canAcceptKeyEvents: () -> Boolean, private val showMenu: () -> Unit): View.OnTouchListener, View.OnKeyListener, View.OnGenericMotionListener, ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener, View.OnHoverListener {
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
    var pointerCaptureListener: Any? = null

    var isReady = false
    var zoomMode: ZoomMode? = null
    var scaleFactor: Float = 1f
    var density: Float = 1f
    private var isLeftTriggerPressed = false
    private var isRightTriggerPressed = false

    private val isContextMenuEnabled = appSettings[PreferenceManager.PredefinedKey.ContextMenu] != "false"

    private var internalInteractionMode = interactionMode

    private var lastPoint: PointF? = null
    private val isScrolling: Boolean
        get() = lastPoint != null
    private var canScroll = true
    private var canInteract = true
    private var isScaling = false

    private var currentPressedMouseButton = 0
    private var scrollingMouseButton = AppCore.MOUSE_BUTTON_LEFT
    private var capturedPoint: PointF? = null
    private var lastMousePoint: PointF? = null
    private var isHoveredOn = false

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val weakSelf = WeakReference(this)
            pointerCaptureListener = PointerCaptureListener(
                handleMouseButtonPress = { view, event ->
                    weakSelf.get()?.handleMouseButtonPress(view, event, true)
                },
                handleMouseButtonRelease = { view, event ->
                    weakSelf.get()?.handleMouseButtonRelease(view, event, true)
                },
                handleMouseButtonMove = { _, event ->
                    val self = weakSelf.get() ?: return@PointerCaptureListener
                    if (self.currentPressedMouseButton != 0) {
                        val point = PointF(event.x, event.y).scaleBy(self.scaleFactor)
                        val current = self.currentPressedMouseButton
                        val modifier = event.keyModifier()
                        self.executor.execute {
                            // Pointer captured do not pass the TOUCH modifier
                            self.appCore.mouseMove(current, point, modifier)
                        }
                    }
                }
            )
        }
    }

    fun setInteractionMode(interactionMode: InteractionMode) {
        internalInteractionMode = interactionMode
    }

    fun callZoom() {
        val mode = zoomMode ?: return
        callZoom(mode.distance)
    }

    private fun callZoom(deltaY: Float) {
        val isCameraMode = internalInteractionMode == InteractionMode.Camera
        executor.execute {
            if (isCameraMode) {
                appCore.mouseMove(AppCore.MOUSE_BUTTON_LEFT, PointF(0.0F, deltaY), AppCore.SHIFT_KEY)
            } else {
                appCore.mouseWheel(deltaY, 0)
            }
        }
    }

    override fun onHover(v: View?, event: MotionEvent?): Boolean {
        if (v == null || event == null) return false
        if (event.actionMasked == MotionEvent.ACTION_HOVER_MOVE || event.actionMasked == MotionEvent.ACTION_HOVER_ENTER) {
            isHoveredOn = true
        } else if (event.actionMasked == MotionEvent.ACTION_HOVER_EXIT) {
            isHoveredOn = false
        }
        return true
    }

    // For BUTTON_RELEASE event
    private fun isButtonReleased(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_POINTER_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            return when (currentPressedMouseButton) {
                AppCore.MOUSE_BUTTON_LEFT -> {
                    !event.isButtonPressed(MotionEvent.BUTTON_PRIMARY)
                }
                AppCore.MOUSE_BUTTON_RIGHT -> {
                    !event.isButtonPressed(MotionEvent.BUTTON_SECONDARY)
                }
                AppCore.MOUSE_BUTTON_MIDDLE -> {
                    !event.isButtonPressed(MotionEvent.BUTTON_TERTIARY)
                }
                else -> { false }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && event.actionMasked == MotionEvent.ACTION_BUTTON_RELEASE) {
            return when (currentPressedMouseButton) {
                AppCore.MOUSE_BUTTON_LEFT -> {
                    event.actionButton == MotionEvent.BUTTON_PRIMARY
                }
                AppCore.MOUSE_BUTTON_RIGHT -> {
                    event.actionButton == MotionEvent.BUTTON_SECONDARY
                }
                AppCore.MOUSE_BUTTON_MIDDLE -> {
                    event.actionButton == MotionEvent.BUTTON_TERTIARY
                }
                else -> { false }
            }
        }
        return false
    }

    private fun pressedButton(event: MotionEvent): Int {
        if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            if (event.isButtonPressed(MotionEvent.BUTTON_PRIMARY)) {
                return AppCore.MOUSE_BUTTON_LEFT
            } else if (event.isButtonPressed(MotionEvent.BUTTON_SECONDARY)) {
                return AppCore.MOUSE_BUTTON_RIGHT
            } else if (event.isButtonPressed(MotionEvent.BUTTON_TERTIARY)) {
                return AppCore.MOUSE_BUTTON_MIDDLE
            }
            return 0
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && event.actionMasked == MotionEvent.ACTION_BUTTON_PRESS) {
            return when (event.actionButton) {
                MotionEvent.BUTTON_PRIMARY -> {
                    AppCore.MOUSE_BUTTON_LEFT
                }
                MotionEvent.BUTTON_SECONDARY -> {
                    AppCore.MOUSE_BUTTON_RIGHT
                }
                MotionEvent.BUTTON_TERTIARY -> {
                    AppCore.MOUSE_BUTTON_MIDDLE
                }
                else -> { 0 }
            }
        }
        return 0
    }

    @RequiresApi(Build.VERSION_CODES.O)
    class PointerCaptureListener(private val handleMouseButtonPress: (view: View, event: MotionEvent) -> Unit, private val handleMouseButtonRelease: (view: View, event: MotionEvent) -> Unit, private val handleMouseButtonMove: (view: View, event: MotionEvent) -> Unit): View.OnCapturedPointerListener {
        override fun onCapturedPointer(view: View?, event: MotionEvent?): Boolean {
            val e = event ?: return true
            val v = view ?: return true
            when (event.actionMasked) {
                MotionEvent.ACTION_BUTTON_PRESS -> {
                    handleMouseButtonPress(v, e)
                    return true
                }
                MotionEvent.ACTION_BUTTON_RELEASE -> {
                    handleMouseButtonRelease(v, e)
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    handleMouseButtonMove(v, e)
                }
            }
            return true
        }
    }

    private fun handleMouseButtonPress(view: View, event: MotionEvent, captured: Boolean): Boolean {
        val newButton = pressedButton(event)
        if (newButton != 0 && newButton != currentPressedMouseButton) {
            val modifier = event.keyModifier()
            val current = currentPressedMouseButton
            currentPressedMouseButton = newButton
            val point = (if (captured) capturedPoint else PointF(event.x, event.y).scaleBy(scaleFactor)) ?: PointF(0f, 0f)
            executor.execute {
                if (current != 0)
                    appCore.mouseButtonUp(current, point, modifier)
                appCore.mouseButtonDown(newButton, point, modifier)
            }
            lastMousePoint = point
            if (!captured && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && event.actionMasked == MotionEvent.ACTION_BUTTON_PRESS) {
                this.capturedPoint = point
                view.requestPointerCapture()
            }
            return true
        }
        return false
    }

    private fun handleMouseButtonRelease(view: View, event: MotionEvent, captured: Boolean): Boolean {
        if (isButtonReleased(event)) {
            val modifier = event.keyModifier()
            val current = currentPressedMouseButton
            currentPressedMouseButton = 0
            val point = (if (captured) capturedPoint else PointF(event.x, event.y).scaleBy(scaleFactor)) ?: PointF(0f, 0f)
            executor.execute {
                appCore.mouseButtonUp(current, point, modifier)
            }
            lastMousePoint = null
            if (captured && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && event.actionMasked == MotionEvent.ACTION_BUTTON_RELEASE) {
                capturedPoint = null
                view.releasePointerCapture()
            }
            return true
        }
        return false
    }

    override fun onGenericMotion(v: View?, event: MotionEvent?): Boolean {
        if (event == null || v == null) { return true }
        if (!isReady) { return true }

        if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
            && event.device != null
            && event.actionMasked == MotionEvent.ACTION_MOVE) {
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
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && (event.source and InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE) {
            if (isHoveredOn && event.actionMasked == MotionEvent.ACTION_SCROLL) {
                val y = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                executor.execute {
                    appCore.mouseWheel(-y * scaleFactor, event.keyModifier())
                }
                return true
            }/* else if (event.actionMasked == MotionEvent.ACTION_BUTTON_PRESS) {
                handleMouseButtonPress(v, event, false)
                return true
            } else if (event.actionMasked == MotionEvent.ACTION_BUTTON_RELEASE) {
                handleMouseButtonRelease(v, event, false)
                return true
            }*/
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null || v == null) { return true }
        if (!isReady) { return true }

        if ((event.source and InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE) {
            // Try to handle mouse logic but falls through if not handled
            var handled = false
            if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                handled = handleMouseButtonPress(v, event, false)
            } else if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_POINTER_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                handled = handleMouseButtonRelease(v, event, false)
            } else if (event.actionMasked == MotionEvent.ACTION_MOVE && currentPressedMouseButton != 0) {
                val lastPoint = lastMousePoint
                if (lastPoint != null) {
                    val current = PointF(event.x, event.y)
                    val offset = PointF(current.x - lastPoint.x, current.y - lastPoint.y).scaleBy(scaleFactor)
                    val button = currentPressedMouseButton
                    val modifier = event.keyModifier()
                    lastMousePoint = current
                    executor.execute {
                        // Mouse input but we do not support capturing
                        appCore.mouseMove(button, offset, modifier.or(AppCore.TOUCH))
                    }
                    handled = true
                }
            }
            if (handled)
                return true
        }

        if (!canInteract) {
            // Enable interaction back when last finger is lifted
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL)
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
                v.rootWindowInsets.displayCutout?.let {
                    insetLeft += it.safeInsetLeft
                    insetTop += it.safeInsetTop
                    insetRight += it.safeInsetRight
                    insetBottom += it.safeInsetBottom
                }
            }

            val interactionRect = RectF(insetLeft, insetTop, v.width - insetRight,  v.height - insetBottom)
            if (!interactionRect.contains(point.x, point.y)) {
                // interactionRect does not contain point, interaction blocked
                canInteract = false
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
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
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
        try {
            // Listeners in Android T SDK do not have optional parameters, might
            // cause NullPointerException, ignore it
            if (gestureDetector.onTouchEvent(event)) {
                return true
            }
        } catch(ignored: Throwable) {
            return true
        }

        if ((event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) && isScrolling) {
            // Last finger is lifted while scrolling
            stopScrolling()
            return true
        }

        // Other events
        Log.e(TAG, "unhandled event, ${event.actionMasked}")
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        if (isScrolling)
            stopScrolling()

        isScaling = true
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {}

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val focus = PointF(detector.focusX, detector.focusY).scaleBy(scaleFactor)
        val scale = detector.scaleFactor
        executor.execute {
            appCore.pinchUpdate(focus, scale)
        }

        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        val point = PointF(e.x, e.y).scaleBy(scaleFactor)
        executor.execute {
            appCore.mouseButtonDown(AppCore.MOUSE_BUTTON_LEFT, point, 0)
            appCore.mouseButtonUp(AppCore.MOUSE_BUTTON_LEFT, point, 0)
        }

        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    override fun onScroll(event1: MotionEvent?, event2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (!canScroll) return false

        if (event1 == null) {
            // https://developer.android.com/reference/android/view/GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent,%20android.view.MotionEvent,%20float,%20float)
            // A null event indicates an incomplete event stream or error state.
            if (isScrolling)
                stopScrolling()
            return true
        }

        val button = internalInteractionMode.button
        val offset = PointF(-distanceX, -distanceY).scaleBy(scaleFactor)
        val originalPoint = PointF(event1.x, event1.y).scaleBy(scaleFactor)
        val newPoint = PointF(event2.x, event2.y).scaleBy(scaleFactor)

        if (!isScrolling) {
            scrollingMouseButton = button
            executor.execute {
                appCore.mouseButtonDown(button, originalPoint, 0)
                appCore.mouseMove(button, offset, AppCore.TOUCH)
            }
        } else {
            executor.execute {
                appCore.mouseMove(button, offset, AppCore.TOUCH)
            }
        }
        lastPoint = newPoint
        return true
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onLongPress(e: MotionEvent) {
        if (!isContextMenuEnabled)
            return

        // Bring up the context menu
        val point = PointF(e.x, e.y).scaleBy(scaleFactor)
        val button = AppCore.MOUSE_BUTTON_RIGHT
        executor.execute {
            appCore.mouseButtonDown(button, point, 0)
            appCore.mouseButtonUp(button, point, 0)
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
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
        val lp = lastPoint ?: return

        executor.execute {
            appCore.mouseButtonUp(scrollingMouseButton, lp, 0)
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