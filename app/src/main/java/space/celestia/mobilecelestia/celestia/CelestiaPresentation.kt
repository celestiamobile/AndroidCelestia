package space.celestia.mobilecelestia.celestia

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.SurfaceHolder
import android.widget.FrameLayout
import space.celestia.celestia.Renderer
import space.celestia.mobilecelestia.R

class CelestiaPresentation(context: Context, display: Display, private val enableFullResolution: Boolean, val frameRateOption: Int, private val renderer: Renderer): Presentation(context, display) {
    private val scaleFactor: Float
        get() = if (enableFullResolution) 1.0f else (1.0f / density)
    private var density: Float = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.presentation_celestia)

        density = resources.displayMetrics.density
        setUpGLView(findViewById(R.id.celestia_gl_view))
    }

    private fun setUpGLView(container: FrameLayout) {
        val view = CelestiaView(this.context, scaleFactor)
        container.addView(view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        view.holder?.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                renderer.setPresentationSurfaceSize(width, height)
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                renderer.setPresentationSurface(holder.surface)
                renderer.setFrameRateOption(frameRateOption)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                renderer.setPresentationSurface(null)
            }
        })
    }
}