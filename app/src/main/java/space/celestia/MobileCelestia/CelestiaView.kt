package space.celestia.MobileCelestia

import android.content.Context
import android.content.res.Resources
import android.opengl.GLSurfaceView

class CelestiaView(context: Context) : GLSurfaceView(context) {
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val density = Resources.getSystem().displayMetrics.density
        holder.setFixedSize((width / density).toInt(), (height / density).toInt())
    }
}