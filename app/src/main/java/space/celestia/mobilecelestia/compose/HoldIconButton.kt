package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics

@Composable
fun HoldIconButton(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnRelease by rememberUpdatedState(onRelease)

    LaunchedEffect(interactionSource, enabled) {
        if (!enabled) return@LaunchedEffect
        var pressed = false
        try {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        pressed = true
                        currentOnPress()
                    }
                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                        if (pressed) {
                            pressed = false
                            currentOnRelease()
                        }
                    }
                    else -> {}
                }
            }
        } finally {
            if (pressed) currentOnRelease()
        }
    }

    IconButton(
        // Pointer and keyboard input use press interactions; accessibility clicks are instantaneous.
        onClick = {},
        interactionSource = interactionSource,
        enabled = enabled,
        modifier = modifier.semantics {
            if (enabled) {
                onClick {
                    currentOnPress()
                    currentOnRelease()
                    true
                }
            }
        },
        content = content
    )
}
