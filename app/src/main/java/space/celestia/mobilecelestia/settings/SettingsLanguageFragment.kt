/*
 * SettingsLanguageFragment.kt
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.RadioButtonRow
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString
import java.util.Locale

@AndroidEntryPoint
class SettingsLanguageFragment : NavigationFragment.SubFragment() {
    private var dataSource: DataSource? = null

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

        title = CelestiaString("Language", "")
    }

    @Composable
    private fun MainScreen() {
        val substitutionList = mapOf(
            "zh_CN" to "zh-Hans",
            "zh_TW" to "zh-Hant"
        )

        fun getLocale(locale: String): Locale {
            val components = locale.split("_")
            if (components.size == 1)
                return Locale(components[0])
            if (components.size == 2)
                return Locale(components[0], components[1])
            return Locale(components[0], components[1], components[2])
        }

        fun getLocalizedLanguageName(locale: String): String {
            val lang = substitutionList[locale] ?: locale
            val loc1 = Locale.forLanguageTag(lang)
            val name1 = loc1.getDisplayName(loc1)
            if (name1.isNotEmpty())
                return name1
            val loc2 = getLocale(lang)
            return loc2.getDisplayName(loc2)
        }

        val internalViewModifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
            )
        val languages = remember {
            dataSource?.availableLanguages() ?: listOf()
        }
        var currentOverrideLanguage: String? by remember {
            mutableStateOf(dataSource?.currentOverrideLanguage())
        }
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        LazyColumn(modifier = Modifier.nestedScroll(nestedScrollInterop)) {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                TextRow(primaryText = CelestiaString("Current Language", ""), secondaryText = getLocalizedLanguageName(dataSource?.currentLanguage() ?: "en"))
            }

            if (languages.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                    Separator()
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                }
                items(items = languages) {
                    RadioButtonRow(primaryText = getLocalizedLanguageName(it), selected = currentOverrideLanguage == it) {
                        currentOverrideLanguage = it
                        setOverrideLanguage(it)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                Separator()
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            }

            item {
                FilledTonalButton(modifier = internalViewModifier, onClick = {
                    currentOverrideLanguage = null
                    setOverrideLanguage(null)
                }) {
                    Text(text = CelestiaString("Reset to Default", ""))
                }
                Footer(text = CelestiaString("Configuration will take effect after a restart.", ""))
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
                with(LocalDensity.current) {
                    Spacer(modifier = Modifier.height(bottomPadding.intValue.toDp()))
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DataSource) {
            dataSource = context
        } else {
            throw RuntimeException("$context must implement SettingsLanguageFragment.Listener and SettingsLanguageFragment.DataSource")
        }
    }

    override fun onDetach() {
        super.onDetach()
        dataSource = null
    }

    private fun setOverrideLanguage(language: String?) {
        if (language == null) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(
                    language.uppercase(
                        Locale.US
                    ).replace("_", "-")
                )
            )
        }
    }

    interface DataSource {
        fun availableLanguages(): List<String>
        fun currentOverrideLanguage(): String?
        fun currentLanguage(): String
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsLanguageFragment()
    }
}
