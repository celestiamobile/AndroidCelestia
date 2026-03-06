package space.celestia.celestiaui.control.viewmodel

import android.graphics.PointF
import android.view.KeyEvent
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.info.model.CelestiaAction
import space.celestia.celestiaui.info.model.perform

sealed class JoystickAction {
    sealed class Key(val key: Int): JoystickAction() {
        data object None: Key(0)
        sealed class Celestia(key: Int): Key(key) {
            data object MoveFaster: Celestia(1)
            data object MoveSlower: Celestia(2)
            data object StopSpeed: Celestia(3)
            data object ReverseSpeed: Celestia(4)
            data object ReverseOrientation: Celestia(5)
            data object TapCenter: Celestia(6)
            data object GoTo: Celestia(7)
            data object Esc: Celestia(8)
            data object PitchUp: Celestia(9)
            data object PitchDown: Celestia(10)
            data object YawLeft: Celestia(11)
            data object YawRight: Celestia(12)
            data object RollLeft: Celestia(13)
            data object RollRight: Celestia(14)

            fun invoke(appCore: AppCore, up: Boolean) {
                when (this) {
                    MoveFaster -> {
                        if (up) appCore.joystickButtonUp(KeyEvent.KEYCODE_BUTTON_X) else appCore.joystickButtonDown(KeyEvent.KEYCODE_BUTTON_X)
                    }
                    MoveSlower -> {
                        if (up) appCore.joystickButtonUp(KeyEvent.KEYCODE_BUTTON_A) else appCore.joystickButtonDown(KeyEvent.KEYCODE_BUTTON_A)
                    }
                    StopSpeed -> {
                        if (up) {
                            appCore.perform(CelestiaAction.Stop) }

                    }
                    ReverseSpeed -> {
                        if (up) {
                            appCore.perform(CelestiaAction.ReverseSpeed)
                        }
                    }
                    ReverseOrientation -> {
                        if (up) {
                            appCore.simulation.reverseObserverOrientation()
                        }
                    }
                    PitchUp -> {
                        if (up) appCore.keyUp(26) else appCore.keyDown(26)
                    }
                    PitchDown -> {
                        if (up) appCore.keyUp(32) else appCore.keyDown(32)
                    }
                    YawLeft -> {
                        if (up) appCore.keyUp(28) else appCore.keyDown(28)
                    }
                    YawRight -> {
                        if (up) appCore.keyUp(30) else appCore.keyDown(30)
                    }
                    RollLeft -> {
                        if (up) appCore.keyUp(31) else appCore.keyDown(31)
                    }
                    RollRight -> {
                        if (up) appCore.keyUp(33) else appCore.keyDown(33)
                    }
                    TapCenter -> {
                        val width = appCore.width
                        val height = appCore.height
                        val center = PointF(width.toFloat() / 2.0f, height.toFloat() / 2.0f)
                        if (up) appCore.mouseButtonUp(AppCore.MOUSE_BUTTON_LEFT, center, 0) else appCore.mouseButtonDown(AppCore.MOUSE_BUTTON_LEFT, center, 0)
                    }
                    GoTo -> {
                        appCore.perform(CelestiaAction.GoTo)
                    }
                    Esc -> {
                        appCore.perform(CelestiaAction.CancelScript)
                    }
                }
            }
        }
        data object ShowMenu: Key(15)
    }

    companion object {
        fun get(value: Int?, default: JoystickAction): JoystickAction {
            return when (value) {
                0 -> Key.None
                1 -> Key.Celestia.MoveFaster
                2 -> Key.Celestia.MoveSlower
                3 -> Key.Celestia.StopSpeed
                4 -> Key.Celestia.ReverseSpeed
                5 -> Key.Celestia.ReverseOrientation
                6 -> Key.Celestia.TapCenter
                7 -> Key.Celestia.GoTo
                8 -> Key.Celestia.Esc
                9 -> Key.Celestia.PitchUp
                10 -> Key.Celestia.PitchDown
                11 -> Key.Celestia.YawLeft
                12 -> Key.Celestia.YawRight
                13 -> Key.Celestia.RollLeft
                14 -> Key.Celestia.RollRight
                15 -> Key.ShowMenu
                else -> default
            }
        }
    }
}