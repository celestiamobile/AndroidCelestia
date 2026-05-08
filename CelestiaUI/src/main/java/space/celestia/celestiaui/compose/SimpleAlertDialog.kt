package space.celestia.celestiaui.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.dropUnlessResumed
import space.celestia.celestiaui.utils.CelestiaString

@Composable
fun SimpleAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String? = null,
    showCancel: Boolean = false,
    dismissOnBackPressOrClickOutside: Boolean = true,
    confirmButtonText: String? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = dropUnlessResumed { onConfirm() }) {
                Text(text = confirmButtonText ?: CelestiaString("OK", ""))
            }
        },
        dismissButton = if (showCancel) {{
            TextButton(onClick = dropUnlessResumed { onDismissRequest() }) {
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