package space.celestia.mobilecelestia.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun SimpleAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String? = null,
    showCancel: Boolean = false,
    dismissOnBackPressOrClickOutside: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = CelestiaString("OK", ""))
            }
        },
        dismissButton = if (showCancel) {{
            TextButton(onClick = onDismissRequest) {
                Text(text = CelestiaString("Cancel", ""))
            }
        }} else null,
        title = {
            Text(title)
        },
        text = if (text != null) { { Text(text) } } else null,
        properties = DialogProperties(dismissOnBackPress = dismissOnBackPressOrClickOutside, dismissOnClickOutside = dismissOnBackPressOrClickOutside)
    )
}