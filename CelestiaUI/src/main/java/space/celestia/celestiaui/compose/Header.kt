package space.celestia.celestiaui.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import space.celestia.celestiaui.R

@Composable
fun Header(text: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(
        start = dimensionResource(id = R.dimen.section_header_margin_horizontal),
        top = if (action == null) dimensionResource(id = R.dimen.section_header_margin_top) else 0.dp,
        end = dimensionResource(id = R.dimen.section_header_margin_horizontal),
        bottom = if (action == null) dimensionResource(id = R.dimen.section_header_margin_bottom) else 0.dp
    )) {
        Text(
            text = text,
            modifier = if (action != null) Modifier.weight(1f) else Modifier,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
        )
        action?.invoke()
    }
}