package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import space.celestia.mobilecelestia.R

@Composable
fun MultiLineTextRow(text: String, modifier: Modifier = Modifier, textColor: Color? = null) {
    Text(text = text, color = textColor ?: colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium), style = MaterialTheme.typography.bodyMedium, modifier = modifier.padding(
        horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
        vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical)
    ))
}