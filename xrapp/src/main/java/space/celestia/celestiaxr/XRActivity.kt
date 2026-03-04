package space.celestia.celestiaxr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import androidx.activity.ComponentActivity

/**
 * Hosts the OpenXR session and render loop.
 *
 * The Activity lifecycle maps directly to OpenXR session state transitions:
 *   onCreate  → xrCreateInstance / xrGetSystem
 *   onStart   → xrBeginSession (triggered by READY state)
 *   onStop    → xrEndSession   (triggered by STOPPING state)
 *   onDestroy → xrDestroySession / xrDestroyInstance
 *
 * All heavy OpenXR work happens on the native side via JNI.
 */
class XRActivity : ComponentActivity(), Choreographer.FrameCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "XRActivity created – initialising OpenXR")
        nativeInit()
    }

    override fun onStart() {
        super.onStart()
        nativeStart()
        Choreographer.getInstance().postFrameCallback(this)

        val panelIntent = Intent(this, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // After this call, the panel activity will be shown in overlay over the immersive activity running in the background.
        startActivity(panelIntent)
    }

    override fun onStop() {
        Choreographer.getInstance().removeFrameCallback(this)
        nativeStop()
        super.onStop()
    }

    override fun onDestroy() {
        nativeDestroy()
        super.onDestroy()
    }

    override fun doFrame(frameTimeNanos: Long) {
        nativeRenderFrame()
        Choreographer.getInstance().postFrameCallback(this)
    }

    // ── JNI interface ────────────────────────────────────────────────────────
    private external fun nativeInit()
    private external fun nativeStart()
    private external fun nativeRenderFrame()
    private external fun nativeStop()
    private external fun nativeDestroy()

    companion object {
        private const val TAG = "CelestiaXR"

        init {
            System.loadLibrary("celestia_native")
        }
    }
}
