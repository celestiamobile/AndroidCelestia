/*
 * AboutFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.MultiLineTextRow
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.AssetUtils
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.versionCode
import space.celestia.mobilecelestia.utils.versionName

class AboutFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null

    private var bottomPadding = mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            bottomPadding.intValue = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            WindowInsetsCompat.CONSUMED
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("About", "")
    }

    @Composable
    fun MainScreen() {
        val aboutSections by remember {
            mutableStateOf(createAboutItems())
        }
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        LazyColumn(modifier = Modifier.nestedScroll(nestedScrollInterop)) {
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
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
                with(LocalDensity.current) {
                    Spacer(modifier = Modifier.height(bottomPadding.intValue.toDp()))
                }
            }
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
        getInfo("CelestiaResources/AUTHORS", CelestiaString("Authors", ""))?.let {
            array.add(it)
        }

        // Translators
        getInfo("CelestiaResources/TRANSLATORS", CelestiaString("Translators", ""))?.let {
            array.add(it)
        }

        // Links
        array.add(
            listOf(
                ActionItem(CelestiaString("Development", ""),"https://celestia.mobi/help/development"),
                ActionItem(CelestiaString("Third Party Dependencies", ""), "https://celestia.mobi/help/dependencies"),
                ActionItem(CelestiaString("Privacy Policy and Service Agreement", ""), "https://celestia.mobi/privacy")
            )
        )

        array.add(
            listOf(
                ActionItem(CelestiaString("Official Website", ""), "https://celestia.mobi"),
                ActionItem(CelestiaString("About Celestia", ""), "https://celestia.mobi/about")
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
