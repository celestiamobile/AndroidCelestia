package space.celestia.MobileCelestia

import android.app.Activity
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import space.celestia.MobileCelestia.Core.CelestiaAppCore
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CelestiaFragment : Fragment(), GLSurfaceView.Renderer {
    private val TAG = "CelestiaFragment"

    private var activity: Activity? = null

    // MARK: GL View
    private var glViewContainer: FrameLayout? = null
    private var glView: CelestiaView? = null

    // MARK: Celestia
    private var pathToLoad: String? = null
    private var core = CelestiaAppCore.shared()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_celestia, container, false);
        glViewContainer = view.findViewById<FrameLayout>(R.id.celestia_gl_view)
        if (pathToLoad != null) {
            setupGLView()
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity = context as? Activity
    }

    override fun onDetach() {
        super.onDetach()

        activity = null
    }

    private fun setupGLView() {
        if (activity == null) { return }
        if (glView != null) { return }

        glView = CelestiaView(activity!!)
        glView?.setEGLContextClientVersion(2)
        glView?.setRenderer(this)
        glViewContainer?.addView(glView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
    }

    fun requestLoadCelestia(path: String) {
        pathToLoad = path

        setupGLView()
    }

    private fun loadCelestia(path: String) {
        CelestiaAppCore.chdir(path)

        if (!core.startSimulation("$path/celestia.cfg", null)) {
            return
        }

        if (!core.startRenderer()) {
            return
        }

        core.tick()
        core.start()

        glView?.isUserInteractionEnabled = true

        Log.d(TAG, "Ready to display")
    }

    // Render
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        glView?.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        // Celestia initialization have to be called with an OpenGL context
        pathToLoad?.let {
            loadCelestia(it)
        }
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        Log.d(TAG, "Resize ot $p1 x $p2")
        core.resize(p1, p2)
    }

    override fun onDrawFrame(p0: GL10?) {
        core.draw()
        core.tick()
    }
}
