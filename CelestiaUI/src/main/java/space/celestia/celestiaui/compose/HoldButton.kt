package space.celestia.celestiaui.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics

@Composable
fun HoldButton(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onActivate: (() -> Unit)? = null,
    content: @Composable (Modifier, MutableInteractionSource) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnRelease by rememberUpdatedState(onRelease)
    val currentOnActivate by rememberUpdatedState(onActivate)

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

    content(
        modifier.semantics {
            // Pointer and keyboard input emit presses; accessibility activates the click action.
            if (enabled) {
                onClick {
                    val activate = currentOnActivate
                    if (activate != null) {
                        activate()
                    } else {
                        currentOnPress()
                        currentOnRelease()
                    }
                    true
                }
            }
        },
        interactionSource,
    )
}
