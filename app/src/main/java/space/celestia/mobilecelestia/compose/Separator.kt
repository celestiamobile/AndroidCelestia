package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import space.celestia.mobilecelestia.R

@Composable
fun Separator(modifier: Modifier = Modifier, separatorStart: Dp = dimensionResource(id = R.dimen.partial_separator_inset_start)) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(separatorStart))
        HorizontalDivider(thickness = dimensionResource(id = R.dimen.default_separator_height))
    }
}