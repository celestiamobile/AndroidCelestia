/*
 * AboutFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.intl.Locale
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.MultiLineTextRow
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.celestiafoundation.utils.AssetUtils
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.celestiafoundation.utils.versionCode
import space.celestia.celestiafoundation.utils.versionName

class AboutFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("About", "About Celstia...")
    }

    @Composable
    fun MainScreen() {
        val aboutSections by remember {
            mutableStateOf(createAboutItems())
        }
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        LazyColumn(modifier = Modifier.nestedScroll(nestedScrollInterop), contentPadding = WindowInsets.systemBars.asPaddingValues()) {
            for (index in aboutSections.indices) {
                val aboutSection = aboutSections[index]
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                }
                items(aboutSection) { item ->
                    when (item) {
                        is ActionItem -> {
                            TextRow(primaryText = item.title, primaryTextColor = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable {
                                listener?.onAboutURLSelected(item.url)
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
                    ICPCFooter()
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
            }
        }
    }

    @Composable
    private fun ICPCFooter() {
        Box(contentAlignment = Alignment.Center, modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical)
            )) {
            Text(text = "苏ICP备2023039249号-4A", color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium), style = MaterialTheme.typography.bodySmall, modifier = Modifier.clickable {
                listener?.onAboutURLSelected("https://beian.miit.gov.cn")
            })
        }
    }

    private fun createAboutItems(): List<List<AboutItem>> {
        val array = ArrayList<List<AboutItem>>()

        // Version
        val ctx = context
        var versionName = "Unknown"
        if (ctx != null)
            versionName = "${ctx.versionName}(${ctx.versionCode})"

        array.add(listOf(
            VersionItem(versionName)
        ))

        // Authors
        getInfo("CelestiaResources/AUTHORS", CelestiaString("Authors", "Authors for Celestia"))?.let {
            array.add(it)
        }

        // Translators
        getInfo("CelestiaResources/TRANSLATORS", CelestiaString("Translators", "Translators for Celestia"))?.let {
            array.add(it)
        }

        // Links
        array.add(
            listOf(
                ActionItem(CelestiaString("Development", "URL for Development wiki"),"https://celestia.mobi/help/development"),
                ActionItem(CelestiaString("Third Party Dependencies", "URL for Third Party Dependencies wiki"), "https://celestia.mobi/help/dependencies"),
                ActionItem(CelestiaString("Privacy Policy and Service Agreement", "Privacy Policy and Service Agreement"), "https://celestia.mobi/privacy")
            )
        )

        array.add(
            listOf(
                ActionItem(CelestiaString("Official Website", ""), "https://celestia.mobi"),
                ActionItem(CelestiaString("About Celestia", "System menu item"), "https://celestia.mobi/about")
            )
        )

        return array
    }

    private fun getInfo(assetPath: String, title: String): List<AboutItem>? {
        val ctx = context ?: return null
        try {
            val info = AssetUtils.readFileToText(ctx, assetPath)
            return listOf(
                TitleItem(title),
                DetailItem(info)
            )
        } catch (_: Exception) {}
        return null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement AboutFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onAboutURLSelected(url: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AboutFragment()
    }
}
