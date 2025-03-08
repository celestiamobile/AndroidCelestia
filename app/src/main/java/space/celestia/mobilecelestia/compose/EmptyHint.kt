package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import space.celestia.mobilecelestia.R

@Composable
fun EmptyHint(text: String, modifier: Modifier = Modifier, actionText: String? = null, actionHandler: (() -> Unit)? = null) {
    Column(modifier = modifier.padding(horizontal = dimensionResource(R.dimen.common_page_medium_margin_horizontal), vertical = dimensionResource(R.dimen.common_page_medium_margin_vertical)), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.common_page_medium_gap_vertical))) {
        Text(text = text, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
        if (actionText != null) {
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