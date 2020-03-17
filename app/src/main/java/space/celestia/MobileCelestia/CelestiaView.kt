package space.celestia.MobileCelestia

import android.content.Context
import android.content.res.Resources
import android.graphics.PointF
import android.graphics.RectF
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.Choreographer
import android.view.MotionEvent
import space.celestia.MobileCelestia.Core.CelestiaAppCore
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.hypot

class CelestiaView : GLSurfaceView, Choreographer.FrameCallback {
    class Touch {
        constructor(p: PointF, t: Date) {
            point = p
            time = t
        }

        var point: PointF
        var time: Date
        var action = false
    }

    public var isReady = false

    private val TAG = "CelestiaView"

    private val threshHold: Int = 20 // thresh hold to start a one finger pan (milliseconds)
    private var touchLocations = HashMap<Int, Touch>()
    private var touchActive = false

    constructor(context: Context) : super(context) {
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun finalize() {
        Choreographer.getInstance().removeFrameCallback(this)

        super.finalize()
    }

    private val core = CelestiaAppCore.shared()

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) { return true }
        if (!isReady) { return true }

        val ev = event!!
        val density = Resources.getSystem().displayMetrics.density

        val id = ev.actionIndex

        fun centerPoint(): PointF {
            var x = 0.toFloat()
            var y = 0.toFloat()
            for (kv in touchLocations) {
                x += kv.value.point.x
                y += kv.value.point.y
            }
            return PointF(x / touchLocations.size, y / touchLocations.size)
        }

        fun length(): Float {
            if (touchLocations.size != 2) { return 0.toFloat() }
            val locations = touchLocations.map { it.value }
            return hypot(
                abs(locations[0].point.x - locations[1].point.x),
                abs(locations[0].point.y - locations[1].point.y)
            )
        }

        when (ev.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                val point = PointF(
                    ev.getX(id) / density,
                    ev.getY(id) / density
                )

                // Avoid edge gesture
                val viewRect = RectF(0.toFloat(), 0.toFloat(), width / density,  height / density)
                viewRect.inset(16f, 16f)

                // we don't allow a third finger
                if (viewRect.contains(point.x, point.y) && touchLocations.count() < 2) {
                    Log.d(TAG, "Down $point")
                    if (touchLocations.size == 1) {
                        touchActive = true
                        // 1 finger to 2 fingers, check if should stop previous
                        val prev = touchLocations.values.map{ it }[0]
                        if (prev.action) {
                            // Stop 1 finger action
                            Log.d(TAG, "One finger action stopped")
                            core.mouseButtonUp(CelestiaAppCore.MOUSE_BUTTON_RIGHT, prev.point, 0)
                        }
                        // Start 2 finger action immediately
                        Log.d(TAG, "Two finger action started")
                        val p = PointF((prev.point.x + point.x) / 2, (prev.point.y + point.y) / 2)
                        core.mouseButtonDown(CelestiaAppCore.MOUSE_BUTTON_LEFT, p, 0)
                    }
                    touchLocations.put(id, Touch(point, Date()))
                }
            }
            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (touchActive) {
                    // Stop touch
                    val button = if (touchLocations.size == 2) CelestiaAppCore.MOUSE_BUTTON_LEFT else CelestiaAppCore.MOUSE_BUTTON_RIGHT
                    core.mouseButtonUp(button, centerPoint(), 0)
                    if (button == CelestiaAppCore.MOUSE_BUTTON_LEFT)
                        Log.d(TAG, "Two finger action stopped")
                    else
                        Log.d(TAG, "One finger action stopped")
                    touchActive = false
                }
                touchLocations.clear()
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchLocations.size == 2) {
                    // Two finger pan/pinch
                    val prevCenter = centerPoint()
                    val prevLength = length()

                    // Update all point locations
                    for (kv in touchLocations) {
                        val point = PointF(
                            ev.getX(kv.key) / density,
                            ev.getY(kv.key) / density
                        )
                        kv.value.point = point
                    }

                    // Calculate new values
                    val currCenter = centerPoint()
                    val currLength = length()

                    val offset = PointF(currCenter.x - prevCenter.x, currCenter.y - prevCenter.y)
                    Log.d(TAG, "Two finger move $offset")
                    core.mouseMove(CelestiaAppCore.MOUSE_BUTTON_LEFT, offset, 0)

                    if (prevLength > 0.2 && currLength > 0.2) {
                        val delta = currLength / prevLength
                        // FIXME: 8 is a magic number
                        val deltaY = (1 - delta) * prevLength / 8
                        Log.d(TAG, "Two finger pinch $deltaY")
                        core.mouseWheel(deltaY, 0)
                    }
                } else if (touchLocations.size == 1)  {
                    val point = PointF(
                        ev.x / density,
                        ev.y / density
                    )
                    val it = touchLocations.map { it.value }[0]
                    if (!it.action && Date().time - it.time.time > threshHold) {
                        it.action = true

                        touchActive = true

                        // Start one finger pan
                        Log.d(TAG, "One finger action started")
                        core.mouseButtonDown(CelestiaAppCore.MOUSE_BUTTON_RIGHT, it.point, 0)
                    }
                    if (it.action) {
                        val offset = PointF(point.x - it.point.x, point.y - it.point.y)

                        // One finger pan
                        Log.d(TAG, "One finger move $offset")
                        core.mouseMove(CelestiaAppCore.MOUSE_BUTTON_RIGHT, offset, 0)
                    }
                    it.point = point
                }
            }
        }
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val density = Resources.getSystem().displayMetrics.density
        holder.setFixedSize((width / density).toInt(), (height / density).toInt())
    }

    override fun doFrame(p0: Long) {
        if (isReady) {
            requestRender()
        }

        Choreographer.getInstance().postFrameCallback(this)
    }
}