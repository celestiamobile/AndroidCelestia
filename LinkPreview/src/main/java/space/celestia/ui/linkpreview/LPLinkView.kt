// LPLinkView.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.ui.linkpreview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import java.net.URL

@Composable
fun LPLinkView(data: LPLinkViewData, imageContentDescription: String, titleColor: Color, titleStyle: TextStyle, footerColor: Color, footerStyle: TextStyle, textSpacing: Dp, textPaddings: PaddingValues, favIconPadding: Dp, modifier: Modifier = Modifier) {
    val textContent: @Composable (modifier: Modifier) -> Unit = { textContentModifier ->
        LinkTextView(
            title = data.title,
            url = data.url,
            titleColor = titleColor,
            titleStyle = titleStyle,
            footerColor = footerColor,
            footerStyle = footerStyle,
            spacing = textSpacing,
            modifier = textContentModifier.padding(textPaddings)
        )
    }

    Card(modifier = modifier) {
        if (data.image == null) {
            textContent(Modifier)
        } else if (data.usesIcon) {
            Row(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)) {
                Image(bitmap = data.image.asImageBitmap(), contentDescription = imageContentDescription, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxHeight().aspectRatio(1.0f).padding(
                    start = favIconPadding,
                    top = favIconPadding,
                    bottom = favIconPadding
                ))
                textContent(Modifier.fillMaxHeight().weight(1.0f))
            }
        } else {
            Column {
                Image(bitmap = data.image.asImageBitmap(), contentDescription = imageContentDescription, modifier = Modifier.fillMaxWidth().aspectRatio(data.image.width.toFloat() / data.image.height.toFloat()))
                textContent(Modifier)
            }
        }
    }
}

@Composable
private fun LinkTextView(title: String, url: URL, titleColor: Color, titleStyle: TextStyle, footerColor: Color, footerStyle: TextStyle, spacing: Dp, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        Text(title, color = titleColor, style = titleStyle, textAlign = TextAlign.Start)
        Text(url.host, color = footerColor, style = footerStyle, textAlign = TextAlign.Start)
    }
}