package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import space.celestia.mobilecelestia.R

@Composable
fun TextRow(primaryText: String, modifier: Modifier = Modifier, primaryTextColor: Color? = null, secondaryText: String? = null, accessoryIcon: ImageVector? = null, accessoryContentDescription: String = "", horizontalPadding: Dp = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal)) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(
        horizontal = horizontalPadding,
        vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical)
    )) {
        Column(modifier = Modifier.weight(1.0f), horizontalAlignment = Alignment.Start) {
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
        accessoryIcon?.let {
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.list_item_gap_horizontal)))
            Icon(imageVector = it, contentDescription = accessoryContentDescription, tint = colorResource(id = com.google.android.material.R.color.material_on_background_disabled))
        }
    }
}