package space.celestia.mobilecelestia.celestia

import space.celestia.mobilecelestia.common.EdgeInsets

class RendererSettings(var density: Float, var fontScale: Float, var safeAreaInsets: EdgeInsets, var frameRateOption: Int, val enableFullResolution: Boolean, val enableMultisample: Boolean, val pickSensitivity: Float) {
    val scaleFactor: Float
        get() = if (enableFullResolution) 1.0f else (1.0f / density)
}