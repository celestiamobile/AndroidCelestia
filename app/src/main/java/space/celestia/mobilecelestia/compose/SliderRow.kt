package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import space.celestia.mobilecelestia.R

@Composable
fun SliderRow(primaryText: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier, primaryTextColor: Color? = null, secondaryText: String? = null) {
    Column(horizontalAlignment = Alignment.Start, modifier = modifier.defaultMinSize(minHeight = dimensionResource(id = if (secondaryText != null) R.dimen.list_slider_item_two_line_min_height else R.dimen.list_slider_item_one_line_min_height)).padding(
        horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
        vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical)
    )) {
        Text(text = primaryText, color = primaryTextColor ?: MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
        if (secondaryText != null) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_item_gap_vertical)))
            Text(
                text = secondaryText,
                color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_item_gap_vertical)))
        Slider(value = value, valueRange = valueRange, onValueChange = onValueChange)
    }
}