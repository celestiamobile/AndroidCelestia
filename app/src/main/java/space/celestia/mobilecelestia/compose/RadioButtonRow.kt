package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import space.celestia.mobilecelestia.R

@Composable
fun RadioButtonRow(primaryText: String, modifier: Modifier = Modifier, secondaryText: String? = null, selected: Boolean, onClick: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(
        horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
        vertical = dimensionResource(id = R.dimen.list_item_small_margin_vertical)
    )) {
        RadioButton(selected = selected, onClick = onClick)
        val textModifier = if (onClick != null) {
            Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                onClick()
            }
        } else {
            Modifier
        }
        Column(modifier = Modifier.weight(1.0f), horizontalAlignment = Alignment.Start) {
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