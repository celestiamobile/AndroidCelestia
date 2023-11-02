package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import space.celestia.mobilecelestia.R

@Composable
fun EmptyHint(text: String, modifier: Modifier = Modifier, actionText: String? = null, actionHandler: (() -> Unit)? = null) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = text, textAlign = TextAlign.Center)
        if (actionText != null) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.common_page_small_gap_vertical)))
            FilledTonalButton(onClick = {
                if (actionHandler != null) {
                    actionHandler()
                }
            }, enabled = actionHandler != null) {
                Text(text = actionText)
            }
        }
    }
}