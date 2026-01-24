package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import space.celestia.mobilecelestia.R

@Composable
fun Footer(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(
        start = dimensionResource(id = R.dimen.section_footer_margin_horizontal),
        top = dimensionResource(id = R.dimen.section_footer_margin_top),
        end = dimensionResource(id = R.dimen.section_footer_margin_horizontal),
        bottom = dimensionResource(id = R.dimen.section_footer_margin_bottom)
    )) {
        Text(text = text, color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun FooterLink(text: String, linkText: String, link: String, action: (String) -> Unit, modifier: Modifier = Modifier) {
    val startIndex = text.indexOf(linkText)
    if (startIndex == -1)
        return

    val endIndex = startIndex + linkText.length
    val annotatedString = buildAnnotatedString {
        append(text)
        addStyle(
            style = SpanStyle(
                color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium),
                fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
                fontSize = MaterialTheme.typography.labelMedium.fontSize,
                fontWeight = MaterialTheme.typography.labelMedium.fontWeight,
                fontStyle = MaterialTheme.typography.labelMedium.fontStyle,
            ),
            start = 0,
            end = text.length
        )
        addStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
            ),
            start = startIndex,
            end = endIndex
        )
        addLink(LinkAnnotation.Clickable(tag = "URL", linkInteractionListener = { _ ->
            action(link)
        }), start = startIndex, end = endIndex)
    }
    Box(modifier = modifier.padding(
        start = dimensionResource(id = R.dimen.section_footer_margin_horizontal),
        top = dimensionResource(id = R.dimen.section_footer_margin_top),
        end = dimensionResource(id = R.dimen.section_footer_margin_horizontal),
        bottom = dimensionResource(id = R.dimen.section_footer_margin_bottom)
    )) {
        BasicText(annotatedString)
    }
}