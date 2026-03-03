package space.celestia.celestiaui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import space.celestia.celestiaui.R

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

data class Link(val text: String, val url: String)

@Composable
fun FooterLink(text: String, links: List<Link>, action: (String) -> Unit, modifier: Modifier = Modifier, textAlign: TextAlign? = null) {
    val annotatedString = buildAnnotatedString {
        append(text)
        addStyle(
            style = ParagraphStyle(textAlign = textAlign ?: TextAlign.Unspecified),
            start = 0,
            end = text.length
        )
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

        for (entry in links.withIndex()) {
            val startIndex = text.indexOf(entry.value.text)
            if (startIndex == -1)
                continue

            val endIndex = startIndex + entry.value.text.length
            addStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                ),
                start = startIndex,
                end = endIndex
            )

            addLink(LinkAnnotation.Clickable(tag = "URL${entry.index}", linkInteractionListener = { _ ->
                action(entry.value.url)
            }), start = startIndex, end = endIndex)
        }
    }

    BasicText(annotatedString, modifier = modifier)
}

@Composable
fun FooterLink(text: String, linkText: String, link: String, action: (String) -> Unit, modifier: Modifier = Modifier, textAlign: TextAlign? = null) {
    FooterLink(text = text, links = listOf(Link(text = linkText, url = link)), action = action, modifier = modifier, textAlign = textAlign)
}