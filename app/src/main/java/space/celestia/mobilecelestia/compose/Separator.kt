package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import space.celestia.mobilecelestia.R

@Composable
fun Separator() {
    Row {
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.partial_separator_inset_start)))
        Divider(thickness = dimensionResource(id = R.dimen.default_separator_height))
    }
}