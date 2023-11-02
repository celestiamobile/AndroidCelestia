package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import space.celestia.mobilecelestia.R

@Composable
fun SwitchRow(primaryText: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, primaryTextColor: Color? = null, secondaryText: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(
        horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
    )) {
        Column(modifier = Modifier.weight(1.0f).padding(
            vertical = dimensionResource(id = R.dimen.list_item_small_margin_vertical)
        ), horizontalAlignment = Alignment.Start) {
            Text(text = primaryText, color = primaryTextColor ?: MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
            if (secondaryText != null) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_item_gap_vertical)))
                Text(
                    text = secondaryText,
                    color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.list_item_gap_horizontal)))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}