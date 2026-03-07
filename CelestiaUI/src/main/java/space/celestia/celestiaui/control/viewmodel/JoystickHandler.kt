package space.celestia.celestiaui.control.viewmodel

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.utils.PreferenceManager
import java.util.concurrent.Executor
import kotlin.math.abs

class JoystickHandler(val appCore: AppCore, val executor: Executor, val appSettings: PreferenceManager) {
    private var isLeftTriggerPressed = false
    private var isRightTriggerPressed = false
    var showMenu: (() -> Unit)? = null

    fun onGenericMotion(event: MotionEvent): Boolean {
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
    }

    fun onKey(keyCode: Int, event: KeyEvent): Boolean {
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

    private fun processJoystickButton(keyCode: Int, up: Boolean) {
        when (val action = joystickButtonKeyAction(keyCode, appSettings)) {
            is JoystickAction.Key.Celestia -> {
                executor.execute {
                    action.invoke(appCore, up)
                }
            }
            JoystickAction.Key.None -> {}
            JoystickAction.Key.ShowMenu -> {
                if (up) {
                    showMenu?.invoke()
                }
            }
        }
    }

    private fun processJoystickInput(event: MotionEvent, historyPos: Int) {

        val inputDevice = event.device

        // Calculate the horizontal distance to move by
        // using the input value combined from these physical controls:
        // the left control stick and the right control stick.
        var xLeft = 0f
        var yLeft = 0f
        var xRight = 0f
        var yRight = 0f
        if (appSettings[PreferenceManager.PredefinedKey.ControllerEnableLeftThumbstick] != "false") {
            xLeft += getCenteredAxis(event, inputDevice, MotionEvent.AXIS_X, historyPos)
            yLeft += getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Y, historyPos)
        }

        // Calculate the vertical distance to move by
        // using the input value combined from these physical controls:
        // the left control stick and the right control stick.
        if (appSettings[PreferenceManager.PredefinedKey.ControllerEnableRightThumbstick] != "false") {
            xRight += getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Z, historyPos)
            yRight += getCenteredAxis(event, inputDevice, MotionEvent.AXIS_RZ, historyPos)
        }

        val shouldInvertX = appSettings[PreferenceManager.PredefinedKey.ControllerInvertX] == "true"
        val shouldInvertY = appSettings[PreferenceManager.PredefinedKey.ControllerInvertY] == "true"

        executor.execute {
            appCore.joystickAxis(AppCore.JOYSTICK_AXIS_X, if (shouldInvertX) -xLeft else xLeft)
            appCore.joystickAxis(AppCore.JOYSTICK_AXIS_Y, if (shouldInvertY) yLeft else -yLeft)
            appCore.joystickAxis(AppCore.JOYSTICK_AXIS_RX, if (shouldInvertX) -xRight else xRight)
            appCore.joystickAxis(AppCore.JOYSTICK_AXIS_RY, if (shouldInvertY) yRight else -yRight)
        }
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

    companion object {
        fun joystickButtonKeyAction(keyCode: Int, appSettings: PreferenceManager): JoystickAction {
            return when (keyCode) {
                KeyEvent.KEYCODE_BUTTON_A -> { JoystickAction.get(appSettings[PreferenceManager.PredefinedKey.ControllerRemapA]?.toIntOrNull(), JoystickAction.Key.Celestia.MoveSlower) }
                KeyEvent.KEYCODE_BUTTON_B -> { JoystickAction.get(appSettings[PreferenceManager.PredefinedKey.ControllerRemapB]?.toIntOrNull(), JoystickAction.Key.None) }
                KeyEvent.KEYCODE_BUTTON_X -> { JoystickAction.get(appSettings[PreferenceManager.PredefinedKey.ControllerRemapX]?.toIntOrNull(), JoystickAction.Key.Celestia.MoveFaster) }
                KeyEvent.KEYCODE_BUTTON_Y -> { JoystickAction.get(appSettings[PreferenceManager.PredefinedKey.ControllerRemapY]?.toIntOrNull(), JoystickAction.Key.None) }
                KeyEvent.KEYCODE_DPAD_UP -> { JoystickAction.get(appSettings[PreferenceManager.PredefinedKey.ControllerRemapDpadUp]?.toIntOrNull(), JoystickAction.Key.Celestia.PitchUp) }
                KeyEvent.KEYCODE_DPAD_DOWN -> { JoystickAction.get(appSettings[PreferenceManager.PredefinedKey.ControllerRemapDpadDown]?.toIntOrNull(), JoystickAction.Key.Celestia.PitchDown) }
                KeyEvent.KEYCODE_DPAD_LEFT -> { JoystickAction.get(appSettings[PreferenceManager.PredefinedKey.ControllerRemapDpadLeft]?.toIntOrNull(), JoystickAction.Key.Celestia.RollLeft) }
                KeyEvent.KEYCODE_DPAD_RIGHT -> { JoystickAction.get(appSettings[PreferenceManager.PredefinedKey.ControllerRemapDpadRight]?.toIntOrNull(), JoystickAction.Key.Celestia.RollRight) }
                KeyEvent.KEYCODE_BUTTON_L1 -> { JoystickAction.get(appSettings[PreferenceManager.PredefinedKey.ControllerRemapLB]?.toIntOrNull(), JoystickAction.Key.None) }
                KeyEvent.KEYCODE_BUTTON_L2 -> { JoystickAction.get(appSettings[PreferenceManager.PredefinedKey.ControllerRemapLT]?.toIntOrNull(), JoystickAction.Key.Celestia.RollLeft) }
                KeyEvent.KEYCODE_BUTTON_R1 -> { JoystickAction.get(appSettings[PreferenceManager.PredefinedKey.ControllerRemapRB]?.toIntOrNull(), JoystickAction.Key.None) }
                KeyEvent.KEYCODE_BUTTON_R2 -> { JoystickAction.get(appSettings[PreferenceManager.PredefinedKey.ControllerRemapRT]?.toIntOrNull(), JoystickAction.Key.Celestia.RollRight) }
                KeyEvent.KEYCODE_BUTTON_START -> JoystickAction.Key.ShowMenu
                else -> JoystickAction.Key.None
            }
        }
    }
}