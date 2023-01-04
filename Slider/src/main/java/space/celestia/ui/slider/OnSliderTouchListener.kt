package space.celestia.ui.slider

import androidx.annotation.RestrictTo

/**
 * Interface definition for callbacks invoked when a slider's touch event is being started/stopped.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface OnSliderTouchListener {
    fun onStartTrackingTouch(timelineSlider: TimelineSlider)
    fun onStopTrackingTouch(timelineSlider: TimelineSlider)
}