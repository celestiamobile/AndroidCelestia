package space.celestia.ui.slider

import androidx.annotation.RestrictTo

/**
 * Interface definition for a callback invoked when a slider's value is changed.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface OnChangeListener {
    /** Called when the value of the slider changes.  */
    fun onValueChange(timelineSlider: TimelineSlider, value: Float, fromUser: Boolean)
}