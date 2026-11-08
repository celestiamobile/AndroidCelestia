package space.celestia.celestiaui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

internal val LocalRetainedVisibility = compositionLocalOf { true }

@Composable
fun RetainedVisibility(visible: Boolean, content: @Composable () -> Unit) {
    val isVisible = visible && LocalRetainedVisibility.current
    CompositionLocalProvider(LocalRetainedVisibility provides isVisible) {
        Box(modifier = Modifier.layout { measurable, constraints ->
            if (isVisible) {
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            } else {
                // Keep composition and its last layout, without drawing or receiving input.
                layout(0, 0) {}
            }
        }) {
            content()
        }
    }
}
