package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import space.celestia.mobilecelestia.R

@Composable
fun RadioButtonRow(primaryText: String, modifier: Modifier = Modifier, secondaryText: String? = null, selected: Boolean, hideHorizontalPadding: Boolean = false, onClick: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.toggleable(selected, role = Role.RadioButton, onValueChange = { newValue ->
        onClick?.invoke()
    }).then(if (hideHorizontalPadding) Modifier else Modifier.padding(
        start = dimensionResource(id = R.dimen.list_item_small_margin_horizontal),
        end = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal)
    ))) {
        RadioButton(selected = selected, onClick = null, modifier = Modifier.minimumInteractiveComponentSize())
        Column(modifier = Modifier.weight(1.0f).padding(
            vertical = dimensionResource(id = R.dimen.list_item_small_margin_vertical)
        ), horizontalAlignment = Alignment.Start) {
            Text(text = primaryText, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
            if (secondaryText != null) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_item_gap_vertical)))
                Text(
                    text = secondaryText,
                    color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}