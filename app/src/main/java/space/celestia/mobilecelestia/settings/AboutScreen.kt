// AboutScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import space.celestia.celestiafoundation.utils.versionCode
import space.celestia.celestiafoundation.utils.versionName
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.FooterLink
import space.celestia.mobilecelestia.compose.Link

@Composable
fun AboutScreen(paddingValues: PaddingValues, linkClicked: (String, Boolean) -> Unit) {
    val context = LocalContext.current
    val nestedScrollInterop = rememberNestedScrollInteropConnection()
    val textModifier = Modifier.fillMaxWidth()
    val direction = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal) + paddingValues.calculateStartPadding(direction),
        top = dimensionResource(id = R.dimen.common_page_medium_gap_vertical) + paddingValues.calculateTopPadding(),
        end = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal) + paddingValues.calculateEndPadding(direction),
        bottom = dimensionResource(id = R.dimen.common_page_medium_gap_vertical) + paddingValues.calculateBottomPadding(),
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollInterop)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        Spacer(modifier = Modifier.weight(1.0f))

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.common_page_medium_margin_vertical)))
        Image(
            painter = painterResource(id = R.drawable.loading_icon),
            contentDescription = null,
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.app_icon_dimension))
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.common_page_medium_margin_vertical)))

        Spacer(modifier = Modifier.weight(1.0f))

        Text(
            "Celestia ${context.versionName} (${context.versionCode})",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            modifier = textModifier
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.common_page_medium_gap_vertical)))

        val links = arrayListOf(Link(text = "https://celestia.mobi", url = "https://celestia.mobi"))
        if (Locale.current.region == "CN") {
            links.add(Link(text = "苏ICP备2023039249号-4A", url = "https://beian.miit.gov.cn"))
        }
        FooterLink(text = links.joinToString(separator = " | ", transform = { it.text }), links = links, textAlign = TextAlign.Center, modifier = textModifier, action = { link ->
            linkClicked(link, link == links[0].url)
        })
    }
}
