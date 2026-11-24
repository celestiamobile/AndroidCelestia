package space.celestia.celestiaui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import space.celestia.celestiaui.R

@Composable
fun Header(text: String, modifier: Modifier = Modifier, isFirst: Boolean = false) {
    Box(modifier = modifier.padding(
        start = dimensionResource(id = R.dimen.section_header_margin_horizontal),
        top = if (isFirst) 0.dp else dimensionResource(id = R.dimen.section_header_margin_top),
        end = dimensionResource(id = R.dimen.section_header_margin_horizontal),
        bottom = dimensionResource(id = R.dimen.section_header_margin_bottom)
    )) {
        Text(text = text, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
    }
}