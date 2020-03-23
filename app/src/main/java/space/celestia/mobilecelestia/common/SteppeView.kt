package space.celestia.mobilecelestia.common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import space.celestia.mobilecelestia.R

class SteppeView(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {
    private val leftView by lazy { findViewById<View>(R.id.stepper_left) }
    private val rightView by lazy { findViewById<View>(R.id.stepper_right) }

    var listener: Listener? = null

    private val subViewListener: View.OnTouchListener = View.OnTouchListener { view, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                listener?.stepperTouchDown(this, view == leftView)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                listener?.stepperTouchUp(this, view == leftView)
            }
        }
        view.onTouchEvent(event)
    }

    init {
        View.inflate(context, R.layout.stepper_view, this)

        leftView.setOnTouchListener(subViewListener)
        rightView.setOnTouchListener(subViewListener)
    }

    interface Listener {
        fun stepperTouchDown(view: SteppeView, left: Boolean)
        fun stepperTouchUp(view: SteppeView, left: Boolean)
    }

    companion object {
        const val TAG = "StepperView"
    }
}