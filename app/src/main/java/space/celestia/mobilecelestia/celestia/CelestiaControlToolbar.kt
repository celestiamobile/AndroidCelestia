package space.celestia.mobilecelestia.celestia

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import space.celestia.celestiaui.R
import space.celestia.celestiaui.settings.ToolbarAction
import space.celestia.mobilecelestia.compose.HoldIconButton

@Composable
fun CelestiaControlToolbar(
    actions: List<ToolbarAction>,
    interactionMode: CelestiaInteraction.InteractionMode,
    onInteractionModeChanged: (CelestiaInteraction.InteractionMode) -> Unit,
    onZoomStarted: (CelestiaInteraction.ZoomMode) -> Unit,
    onZoomEnded: (CelestiaInteraction.ZoomMode) -> Unit,
    onAction: (ToolbarAction) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    VerticalFloatingToolbar(expanded = true, modifier = modifier) {
        for (action in actions) {
            key(action) {
                val image = if (action == ToolbarAction.Mode) {
                    if (interactionMode == CelestiaInteraction.InteractionMode.Camera) {
                        R.drawable.control_mode_camera
                    } else {
                        R.drawable.control_mode_object
                    }
                } else {
                    action.imageResource
                }
                val icon: @Composable () -> Unit = {
                    Icon(
                        painter = painterResource(image),
                        contentDescription = action.title
                    )
                }
                when (action) {
                    ToolbarAction.Mode -> IconToggleButton(
                        checked = interactionMode == CelestiaInteraction.InteractionMode.Camera,
                        onCheckedChange = { cameraMode ->
                            onInteractionModeChanged(
                                if (cameraMode) CelestiaInteraction.InteractionMode.Camera else CelestiaInteraction.InteractionMode.Object
                            )
                        },
                        enabled = enabled,
                        colors = IconButtonDefaults.iconToggleButtonColors(
                            checkedContentColor = LocalContentColor.current
                        ),
                        content = icon
                    )
                    ToolbarAction.ZoomIn, ToolbarAction.ZoomOut -> {
                        val mode = if (action == ToolbarAction.ZoomIn) CelestiaInteraction.ZoomMode.In else CelestiaInteraction.ZoomMode.Out
                        HoldIconButton(
                            onPress = { onZoomStarted(mode) },
                            onRelease = { onZoomEnded(mode) },
                            enabled = enabled,
                            content = icon
                        )
                    }
                    else -> IconButton(
                        onClick = { onAction(action) },
                        enabled = enabled,
                        content = icon
                    )
                }
            }
        }
    }
}
