package space.celestia.mobilecelestia.compose

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import space.celestia.celestiaui.compose.HoldButton

@Composable
fun HoldIconButton(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    HoldButton(
        onPress = onPress,
        onRelease = onRelease,
        modifier = modifier,
        enabled = enabled,
    ) { buttonModifier, interactionSource ->
        IconButton(
            onClick = {},
            interactionSource = interactionSource,
            enabled = enabled,
            modifier = buttonModifier,
            content = content,
        )
    }
}
