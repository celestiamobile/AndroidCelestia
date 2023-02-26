package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import space.celestia.mobilecelestia.R
import java.net.URL

@Composable
fun Footer(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(
        start = dimensionResource(id = R.dimen.section_footer_margin_horizontal),
        top = dimensionResource(id = R.dimen.section_footer_margin_top),
        end = dimensionResource(id = R.dimen.section_footer_margin_horizontal),
        bottom = dimensionResource(id = R.dimen.section_footer_margin_bottom)
    )) {
        Text(text = text, color = colorResource(id = R.color.material_on_background_emphasis_medium), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun FooterLink(text: String, action: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(
        start = dimensionResource(id = R.dimen.section_footer_margin_horizontal),
        top = dimensionResource(id = R.dimen.section_footer_margin_top),
        end = dimensionResource(id = R.dimen.section_footer_margin_horizontal),
        bottom = dimensionResource(id = R.dimen.section_footer_margin_bottom)
    )) {
        Text(text = text, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable {
            action()
        })
    }
}