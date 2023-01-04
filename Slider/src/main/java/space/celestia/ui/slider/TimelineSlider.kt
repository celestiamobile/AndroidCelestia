package space.celestia.ui.slider

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.Paint.Style
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

public class TimelineSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = DEF_STYLE_RES
) : View(context, attrs, defStyleAttr) {
    private val changeListeners: ArrayList<OnChangeListener> = arrayListOf()
    private val touchListeners: ArrayList<OnSliderTouchListener> = arrayListOf()
    private val foregroundTrackPaint = Paint()
    private val backgroundTrackPaint = Paint()
    private val clippingPath = Path()
    private var touchDownX = 0f
    private var thumbIsPressed = false

    private var lastEvent: MotionEvent? = null

    private val scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

    public var valueFrom = 0f
        set(value) {
            field = value
            postInvalidate()
        }
    public var valueTo = 1f
        set(value) {
            field = value
            postInvalidate()
        }
    public var value = 0f
        set(value) {
            field = value
            dispatchOnChangedProgrammatically()
            postInvalidate()
        }
    public var ticks: ArrayList<Float> = arrayListOf()
        get() = ArrayList(field)
        set(value) {
            field = ArrayList(value.sorted())
            postInvalidate()
        }

    public var tickLength = 0
        set(value) {
            field = value
            postInvalidate()
        }

    private var trackColorBackground: ColorStateList? = null
    public var trackBackgroundTintList: ColorStateList
        get() = trackColorBackground!!
        set(value) {
            if (value == trackColorBackground) {
                return
            }
            trackColorBackground = value
            backgroundTrackPaint.color = getColorForState(value)
            invalidate()
        }

    private var trackColorForeground: ColorStateList? = null
    public var trackForegroundTintList: ColorStateList
        get() = trackColorForeground!!
        set(value) {
            if (value == trackColorForeground) {
                return
            }
            trackColorBackground = value
            foregroundTrackPaint.color = getColorForState(value)
            invalidate()
        }

    init {
        backgroundTrackPaint.style = Style.STROKE
        foregroundTrackPaint.style = Style.STROKE

        processAttributes(context, attrs, defStyleAttr)
    }

    private fun processAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.TimelineSlider, defStyleAttr, DEF_STYLE_RES)
        valueFrom = a.getFloat(R.styleable.TimelineSlider_android_valueFrom, 0.0f)
        valueTo = a.getFloat(R.styleable.TimelineSlider_android_valueTo, 1.0f)
        value = a.getFloat(R.styleable.TimelineSlider_android_value, valueFrom)

        val trackColorBackground = getColorStateList(context, a, R.styleable.TimelineSlider_trackColorBackground)
        trackBackgroundTintList = trackColorBackground
            ?: AppCompatResources.getColorStateList(
                context, R.color.m3_slider_inactive_track_color
            )
        val trackColorForeground = getColorStateList(context, a, R.styleable.TimelineSlider_trackColorForeground)
        trackForegroundTintList =  trackColorForeground
            ?: AppCompatResources.getColorStateList(
                context, R.color.m3_slider_active_track_color
            )

        tickLength = a.getDimensionPixelSize(R.styleable.TimelineSlider_tickLength, resources.getDimensionPixelSize(R.dimen.celestia_slider_tick_length))

        if (!a.getBoolean(R.styleable.TimelineSlider_android_enabled, true)) {
            isEnabled = false
        }
    }

    public fun addOnChangeListener(listener: OnChangeListener) {
        changeListeners.add(listener)
    }

    public fun removeOnChangeListener(listener: OnChangeListener) {
        changeListeners.remove(listener)
    }

    public fun clearOnChangeListeners() {
        changeListeners.clear()
    }

    public fun addOnSliderTouchListener(listener: OnSliderTouchListener) {
        touchListeners.add(listener)
    }

    public fun removeOnSliderTouchListener(listener: OnSliderTouchListener) {
        touchListeners.remove(listener)
    }

    public fun clearOnSliderTouchListeners() {
        touchListeners.clear()
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) { return }

        super.onDraw(canvas)
        canvas.clipPath(clippingPath)

        val yCenter = height / 2
        val isRTL = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
        drawBackgroundTrack(canvas, yCenter, isRTL)
        if (value > valueFrom) {
            drawForegroundTrack(canvas, yCenter, isRTL)
        }
    }

    private fun drawTrack(to: Float, yCenter: Int, isRTL: Boolean, canvas: Canvas, paint: Paint) {
        var currentX = if (isRTL) width.toFloat() else 0f
        for (tick in ticks) {
            if (tick > to) break
            val normalized = normalizeValue(tick)
            val nextX = if (isRTL) (1 - normalized) * width else normalized * width
            if (isRTL) {
                if (currentX > nextX + tickLength / 2) {
                    canvas.drawLine(currentX, yCenter.toFloat(), nextX + tickLength / 2, yCenter.toFloat(), paint)
                    currentX = nextX - tickLength / 2
                }
            } else {
                if (currentX < nextX - tickLength / 2) {
                    canvas.drawLine(currentX, yCenter.toFloat(), nextX - tickLength / 2, yCenter.toFloat(), paint)
                    currentX = nextX + tickLength / 2
                }
            }
        }

        val normalizedLast = normalizeValue(to)
        val lastX = if (isRTL) (1 - normalizedLast) * width else normalizedLast * width
        if (isRTL) {
            if (currentX > lastX + tickLength / 2) {
                canvas.drawLine(currentX, yCenter.toFloat(), lastX + tickLength / 2, yCenter.toFloat(), paint)
            }
        } else {
            if (currentX < lastX - tickLength / 2) {
                canvas.drawLine(currentX, yCenter.toFloat(), lastX - tickLength / 2, yCenter.toFloat(), paint)
            }
        }
    }

    private fun normalizeValue(value: Float): Float {
        if (valueTo == valueFrom) return 0f
        return (value - valueFrom) / (valueTo - valueFrom)
    }

    private fun drawBackgroundTrack(canvas: Canvas, yCenter: Int, isRTL: Boolean) {
        drawTrack(valueTo, yCenter, isRTL, canvas,  backgroundTrackPaint)
    }

    private fun drawForegroundTrack(canvas: Canvas, yCenter: Int, isRTL: Boolean) {
        drawTrack(value, yCenter, isRTL, canvas, foregroundTrackPaint)
    }

    internal class SliderState : BaseSavedState {
        var valueFrom = 0f
        var valueTo = 0f
        var value = 0f
        var ticks: ArrayList<Float> = arrayListOf()
        var hasFocus = false

        constructor(superState: Parcelable?) : super(superState)
        private constructor(source: Parcel) : super(source) {
            valueFrom = source.readFloat()
            valueTo = source.readFloat()
            value = source.readFloat()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                source.readList(ticks, Float::class.java.classLoader, Float::class.java)
            } else {
                @Suppress("DEPRECATION")
                source.readList(ticks, Float::class.java.classLoader)
            }
            hasFocus = source.createBooleanArray()!![0]
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeFloat(valueFrom)
            dest.writeFloat(valueTo)
            dest.writeFloat(value)
            dest.writeList(ticks)
            val booleans = BooleanArray(1)
            booleans[0] = hasFocus
            dest.writeBooleanArray(booleans)
        }

        companion object CREATOR : Parcelable.Creator<SliderState> {
            override fun createFromParcel(parcel: Parcel): SliderState {
                return SliderState(parcel)
            }

            override fun newArray(size: Int): Array<SliderState?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val sliderState = SliderState(superState)
        sliderState.valueFrom = valueFrom
        sliderState.valueTo = valueTo
        sliderState.value = value
        sliderState.ticks = ticks
        sliderState.hasFocus = hasFocus()
        return sliderState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val sliderState = state as SliderState
        super.onRestoreInstanceState(sliderState.superState)

        valueFrom = sliderState.valueFrom
        valueTo = sliderState.valueTo
        value = sliderState.value
        ticks = sliderState.ticks
        if (sliderState.hasFocus) {
            requestFocus()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        backgroundTrackPaint.strokeWidth = h.toFloat()
        foregroundTrackPaint.strokeWidth = h.toFloat()
        clippingPath.reset()
        clippingPath.addRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), h / 2f, h / 2f, Path.Direction.CW)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled || event == null)
            return false

        val x = event.x
        var touchPosition = x / width.toFloat()
        touchPosition = max(0f, touchPosition)
        touchPosition = min(1f, touchPosition)
        val isRTL = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
        val value = if (isRTL) ((1 - touchPosition) * (valueTo - valueFrom) + valueFrom) else (touchPosition * (valueTo - valueFrom) + valueFrom)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = x

                // If we're inside a vertical scrolling container,
                // we should start dragging in ACTION_MOVE
                if (!isPotentialVerticalScroll(event)) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    requestFocus()
                    thumbIsPressed = true
                    if (abs(value - this.value) >= THRESHOLD) {
                        this.value = value
                        dispatchOnChangedFromUser()
                    }
                    invalidate()
                    onStartTrackingTouch()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!thumbIsPressed) {
                    // Check if we're trying to scroll vertically instead of dragging this Slider
                    if (isPotentialVerticalScroll(event) && abs(x - touchDownX) < scaledTouchSlop) {
                        return false
                    }
                    parent.requestDisallowInterceptTouchEvent(true)
                    onStartTrackingTouch()
                }
                thumbIsPressed = true
                if (abs(value - this.value) >= THRESHOLD) {
                    this.value = value
                    dispatchOnChangedFromUser()
                }
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                thumbIsPressed = false
                val lastEvent = this.lastEvent
                // We need to handle a tap if the last event was down at the same point.
                if (lastEvent != null && lastEvent.actionMasked == MotionEvent.ACTION_DOWN && abs(
                        lastEvent.x - event.x
                    ) <= scaledTouchSlop && abs(lastEvent.y - event.y) <= scaledTouchSlop
                ) {
                    onStartTrackingTouch()
                }
                onStopTrackingTouch()
                if (abs(value - this.value) >= THRESHOLD) {
                    this.value = value
                    dispatchOnChangedFromUser()
                }
                invalidate()
            }
            else -> {}
        }

        // Set if the thumb is pressed. This will cause the ripple to be drawn.
        isPressed = thumbIsPressed

        lastEvent = MotionEvent.obtain(event)
        return true
    }

    private fun isInVerticalScrollingContainer(): Boolean {
        var p = parent
        while (p is ViewGroup) {
            val parent = p
            val canScrollVertically =
                parent.canScrollVertically(1) || parent.canScrollVertically(-1)
            if (canScrollVertically && parent.shouldDelayChildPressedState()) {
                return true
            }
            p = p.getParent()
        }
        return false
    }

    private fun isMouseEvent(event: MotionEvent): Boolean {
        return event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE
    }

    private fun isPotentialVerticalScroll(event: MotionEvent): Boolean {
        return !isMouseEvent(event) && isInVerticalScrollingContainer()
    }

    private fun dispatchOnChangedProgrammatically() {
        for (listener in changeListeners) {
            listener.onValueChange(this, value, false)
        }
    }

    private fun dispatchOnChangedFromUser() {
        for (listener in changeListeners) {
            listener.onValueChange(this, value, true)
        }
    }

    private fun onStartTrackingTouch() {
        for (listener in touchListeners) {
            listener.onStartTrackingTouch(this)
        }
    }

    private fun onStopTrackingTouch() {
        for (listener in touchListeners) {
            listener.onStopTrackingTouch(this)
        }
    }

    @ColorInt
    private fun getColorForState(colorStateList: ColorStateList): Int {
        return colorStateList.getColorForState(drawableState, colorStateList.defaultColor)
    }

    fun getColorStateList(
        context: Context, attributes: TypedArray, @StyleableRes index: Int
    ): ColorStateList? {
        if (attributes.hasValue(index)) {
            val resourceId = attributes.getResourceId(index, 0)
            if (resourceId != 0) {
                val value = AppCompatResources.getColorStateList(context, resourceId)
                if (value != null) {
                    return value
                }
            }
        }
        return attributes.getColorStateList(index)
    }

    companion object {
        private val DEF_STYLE_RES = R.style.Widget_Celestia_Slider
        private const val THRESHOLD = .0001f
    }
}