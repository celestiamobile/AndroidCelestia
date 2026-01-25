// About.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.intl.Locale
import space.celestia.celestiafoundation.utils.AssetUtils
import space.celestia.celestiafoundation.utils.versionCode
import space.celestia.celestiafoundation.utils.versionName
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.MultiLineTextRow
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun About(paddingValues: PaddingValues, linkClicked: (String, Boolean) -> Unit) {
    val context = LocalContext.current
    val aboutSections by remember {
        mutableStateOf(createAboutItems(context))
    }
    val nestedScrollInterop = rememberNestedScrollInteropConnection()
    LazyColumn(modifier = Modifier.nestedScroll(nestedScrollInterop), contentPadding = paddingValues) {
        for (index in aboutSections.indices) {
            val aboutSection = aboutSections[index]
            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            }
            items(aboutSection) { item ->
                when (item) {
                    is ActionItem -> {
                        TextRow(primaryText = item.title, primaryTextColor = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable {
                            linkClicked(item.url, item.localizable)
                        })
                    }
                    is VersionItem -> {
                        TextRow(primaryText = CelestiaString("Version", ""), secondaryText = item.versionName)
                    }
                    is DetailItem -> {
                        MultiLineTextRow(text = item.detail)
                    }
                    is TitleItem -> {
                        TextRow(primaryText = item.title)
                    }
                }
            }
            item {
                if (index != aboutSections.size - 1) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                    Separator()
                }
            }
        }

        item {
            if (Locale.current.region == "CN") {
                ICPCFooter(linkClicked)
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
        }
    }
}

@Composable
private fun ICPCFooter(linkClicked: (String, Boolean) -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
            vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical)
        )) {
        Text(text = "苏ICP备2023039249号-4A", color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium), style = MaterialTheme.typography.bodySmall, modifier = Modifier.clickable {
            linkClicked("https://beian.miit.gov.cn", false)
        })
    }
}

private fun createAboutItems(context: Context): List<List<AboutItem>> {
    val array = ArrayList<List<AboutItem>>()

    // Version
    var versionName = "Unknown"
    versionName = "${context.versionName}(${context.versionCode})"

    array.add(listOf(
        VersionItem(versionName)
    ))

    // Authors
    getInfo(context, "CelestiaResources/AUTHORS", CelestiaString("Authors", "Authors for Celestia"))?.let {
        array.add(it)
    }

    // Translators
    getInfo(context, "CelestiaResources/TRANSLATORS", CelestiaString("Translators", "Translators for Celestia"))?.let {
        array.add(it)
    }

    // Links
    array.add(
        listOf(
            ActionItem(CelestiaString("Development", "URL for Development wiki"),"https://celestia.mobi/help/development", localizable = false),
            ActionItem(CelestiaString("Third Party Dependencies", "URL for Third Party Dependencies wiki"), "https://celestia.mobi/help/dependencies", localizable = true),
            ActionItem(CelestiaString("Privacy Policy and Service Agreement", "Privacy Policy and Service Agreement"), "https://celestia.mobi/privacy", localizable = true)
        )
    )

    array.add(
        listOf(
            ActionItem(CelestiaString("Official Website", ""), "https://celestia.mobi", localizable = true),
            ActionItem(CelestiaString("About Celestia", "System menu item"), "https://celestia.mobi/about", localizable = true)
        )
    )

    return array
}

private fun getInfo(context: Context, assetPath: String, title: String): List<AboutItem>? {
    try {
        val info = AssetUtils.readFileToText(context, assetPath)
        return listOf(
            TitleItem(title),
            DetailItem(info)
        )
    } catch (_: Exception) {}
    return null
}