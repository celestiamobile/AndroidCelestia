package space.celestia.mobilecelestia.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import space.celestia.mobilecelestia.R

@Composable
fun TextRow(primaryText: String?, modifier: Modifier = Modifier, primaryTextColor: Color? = null, secondaryText: String? = null, @DrawableRes accessoryResource: Int = 0, accessoryContentDescription: String = "", horizontalPadding: Dp = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal)) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.defaultMinSize(minHeight = dimensionResource(id = if (secondaryText != null && primaryText != null) R.dimen.list_item_two_line_min_height else R.dimen.list_item_one_line_min_height)).padding(
        horizontal = horizontalPadding,
        vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical)
    )) {
        Column(modifier = Modifier.weight(1.0f), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_vertical))) {
            if (primaryText != null) {
                Text(
                    text = primaryText,
                    color = primaryTextColor ?: MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (secondaryText != null) {
                Text(
                    text = secondaryText,
                    color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (accessoryResource != 0) {
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.list_item_gap_horizontal)))
            Image(painter = painterResource(id = accessoryResource), contentDescription = accessoryContentDescription, colorFilter = ColorFilter.tint(
                colorResource(id = com.google.android.material.R.color.material_on_background_disabled)))
        }
    }
}