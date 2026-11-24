package space.celestia.mobilecelestia.control

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import space.celestia.celestiaui.info.model.CelestiaContinuousAction
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.HoldIconButton

@Composable
fun PlaybackControls(
    actions: List<BottomControlAction>,
    overflowItems: List<OverflowItem>,
    onAction: (BottomControlAction) -> Unit,
    onContinuousActionDown: (CelestiaContinuousAction) -> Unit,
    onContinuousActionUp: (CelestiaContinuousAction) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var overflowExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) {
        if (!enabled) overflowExpanded = false
    }

    HorizontalFloatingToolbar(
        expanded = true,
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        modifier = modifier
    ) {
        for (action in actions) {
            key(action) {
                val icon: @Composable () -> Unit = {
                    action.imageID?.let { image ->
                        Icon(painterResource(image), contentDescription = action.contentDescription)
                    }
                }
                when (action) {
                    is ContinuousAction -> HoldIconButton(
                        onPress = { onContinuousActionDown(action.action) },
                        onRelease = { onContinuousActionUp(action.action) },
                        enabled = enabled,
                        content = icon
                    )
                    else -> IconButton(
                        onClick = { onAction(action) },
                        enabled = enabled,
                        content = icon
                    )
                }
            }
        }
        Box {
            IconButton(onClick = { overflowExpanded = true }, enabled = enabled) {
                Icon(
                    painterResource(R.drawable.bottom_toolbar_overflow),
                    contentDescription = CelestiaString("More actions", "Button to show more actions to in the bottom toolbar")
                )
            }
            DropdownMenu(
                expanded = overflowExpanded && enabled,
                onDismissRequest = { overflowExpanded = false }
            ) {
                for (item in overflowItems) {
                    DropdownMenuItem(
                        text = { Text(item.title) },
                        onClick = {
                            overflowExpanded = false
                            onAction(item.action)
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(CelestiaString("Close", "")) },
                    onClick = {
                        overflowExpanded = false
                        onClose()
                    }
                )
            }
        }
    }
}
