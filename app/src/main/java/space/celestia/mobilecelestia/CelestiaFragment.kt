package space.celestia.mobilecelestia

import android.app.Activity
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import space.celestia.mobilecelestia.core.CelestiaAppCore
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CelestiaFragment : Fragment(), GLSurfaceView.Renderer, CelestiaAppCore.ProgressWatcher {
    private val TAG = "CelestiaFragment"

    private var activity: Activity? = null

    // MARK: GL View
    private var glViewContainer: FrameLayout? = null
    private var glView: CelestiaView? = null
    private var glViewSize: Size? = null
    private var isReady: Boolean = false

    // MARK: Celestia
    private var pathToLoad: String? = null
    private var cfgToLoad: String? = null
    private var core = CelestiaAppCore.shared()

    private var statusCallback: ((String) -> Unit)? = null
    private var resultCallback: ((Boolean) -> Unit)? = null

    private var listener: Listener? = null

    interface Listener {
        fun celestiaWillStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_celestia, container, false)
        glViewContainer = view.findViewById(R.id.celestia_gl_view)
        if (pathToLoad != null) {
            setupGLView()
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity = context as? Activity
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CelestiaFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()

        activity = null
        listener = null
    }

    private fun setupGLView() {
        if (activity == null) { return }
        if (glView != null) { return }

        glView = CelestiaView(activity!!)
        glView?.setEGLContextClientVersion(2)
        glView?.setRenderer(this)
        glViewContainer?.addView(glView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
    }

    fun requestLoadCelestia(path: String, cfgPath: String,  status: (String) -> Unit, result: (Boolean) -> Unit) {
        pathToLoad = path
        cfgToLoad = cfgPath

        statusCallback = status
        resultCallback = result

        setupGLView()
    }

    private fun loadCelestia(path: String, cfg: String) {
        CelestiaAppCore.chdir(path)

        CelestiaAppCore.setLocaleDirectoryPath("$path/locale", Locale.getDefault().toString())

        if (!core.startSimulation(cfg, null, this)) {
            resultCallback?.let { it(false) }
            return
        }

        if (!core.startRenderer()) {
            resultCallback?.let { it(false) }
            return
        }

        listener?.celestiaWillStart()

        glViewSize?.let {
            core.resize(it.width, it.height)
            glViewSize = null
        }

        core.tick()
        core.start()

        glView?.isReady = true
        isReady = true

        Log.d(TAG, "Ready to display")
        resultCallback?.let { it(true) }
    }

    // Render
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        glView?.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        glView?.queueEvent {
            // Celestia initialization have to be called with an OpenGL context
            if (pathToLoad != null && cfgToLoad != null) {
                loadCelestia(pathToLoad!!, cfgToLoad!!)
            }
        }
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        glViewSize = Size(p1, p2)
        Log.d(TAG, "Resize to $p1 x $p2")
        if (!isReady) { return }
        core.resize(p1, p2)
    }

    override fun onDrawFrame(p0: GL10?) {
        if (!isReady) { return }
        core.draw()
        core.tick()
    }

    // Progress
    override fun onCelestiaProgress(progress: String) {
        Log.d(TAG, "Loading $progress")
        statusCallback?.let {
            it(progress)
        }
    }
}
