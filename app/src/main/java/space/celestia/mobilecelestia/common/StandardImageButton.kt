package space.celestia.mobilecelestia.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

class StandardImageButton: androidx.appcompat.widget.AppCompatImageButton {

    constructor(context: Context, attrSet: AttributeSet) : super(context, attrSet) {
        background = null
    }

    constructor(context: Context) : super(context) {
        background = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    alpha = 0.3f
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    alpha = 1f
                }
            }
        }
        return super.onTouchEvent(event)
    }
}