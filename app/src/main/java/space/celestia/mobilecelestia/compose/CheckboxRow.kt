package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import space.celestia.mobilecelestia.R

@Composable
fun CheckboxRow(primaryText: String, modifier: Modifier = Modifier, secondaryText: String? = null, checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(
        start = dimensionResource(id = R.dimen.list_item_small_margin_horizontal),
        end = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal)
    )) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        val textModifier = if (onCheckedChange != null) {
            Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                onCheckedChange(!checked)
            }
        } else {
            Modifier
        }
        Column(modifier = Modifier.weight(1.0f).padding(
            vertical = dimensionResource(id = R.dimen.list_item_small_margin_vertical)
        ), horizontalAlignment = Alignment.Start) {
            Text(text = primaryText, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge, modifier = textModifier)
            if (secondaryText != null) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_item_gap_vertical)))
                Text(
                    text = secondaryText,
                    color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = textModifier
                )
            }
        }
    }
}