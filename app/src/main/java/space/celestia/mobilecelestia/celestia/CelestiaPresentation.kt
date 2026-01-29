package space.celestia.mobilecelestia.celestia

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.SurfaceHolder
import android.widget.FrameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestia.Renderer
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.EdgeInsets
import space.celestia.mobilecelestia.purchase.PurchaseManager
import space.celestia.mobilecelestia.utils.PreferenceManager

class CelestiaPresentation(
    context: Context,
    display: Display,
    private val rendererSettings: RendererSettings,
    private val appCore: AppCore,
    private val renderer: Renderer,
    private val executor: CelestiaExecutor,
    private val purchaseManager: PurchaseManager,
    private val appSettings: PreferenceManager
): Presentation(context, display) {
    private var density: Float = 1f
    private var fontScale: Float = 1f
    private val savedInsets = EdgeInsets()
    private val renderChanges: RenderChanges
        get() = RenderChanges(scaling = density != rendererSettings.density || fontScale != rendererSettings.fontScale, safeArea = savedInsets != rendererSettings.safeAreaInsets)

    private val presentationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.presentation_celestia)

        density = resources.displayMetrics.density
        fontScale = resources.configuration.fontScale

        setUpGLView(findViewById(R.id.celestia_gl_view))
    }

    private fun applyRenderChanges(changes: RenderChanges): RenderChanges {
        if (changes.scaling) {
            rendererSettings.density = density
            rendererSettings.fontScale = fontScale
        }

        if (changes.safeArea) {
            rendererSettings.safeAreaInsets = savedInsets
        }
        return changes
    }

    private fun updateContentScale(changes: RenderChanges) {
        if (!changes.scaling && !changes.safeArea) return

        renderer.makeContextCurrent()
        appCore.updateContentScale(rendererSettings, changes, purchaseManager, appSettings)
    }

    private fun setUpGLView(container: FrameLayout) {
        // Cannot use rendererSettings.scaleFactor here because it is before any updateContentScale call
        val view = CelestiaView(this.context, if (rendererSettings.enableFullResolution) 1.0f else (1.0f / density))
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
                val changes = applyRenderChanges(renderChanges)
                presentationScope.launch(executor.asCoroutineDispatcher()) {
                    updateContentScale(changes)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                renderer.setPresentationSurface(null)
            }
        })
    }

    override fun onStop() {
        super.onStop()
        presentationScope.cancel()
    }
}