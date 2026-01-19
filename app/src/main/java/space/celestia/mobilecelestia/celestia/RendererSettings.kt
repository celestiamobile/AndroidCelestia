package space.celestia.mobilecelestia.celestia

class RendererSettings(var density: Float, var fontScale: Float, var frameRateOption: Int, val enableFullResolution: Boolean, val enableMultisample: Boolean) {
    val scaleFactor: Float
        get() = if (enableFullResolution) 1.0f else (1.0f / density)
}