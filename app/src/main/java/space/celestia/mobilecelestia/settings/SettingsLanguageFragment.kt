// SettingsLanguageFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.celestiafoundation.utils.FilePaths
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.RadioButtonRow
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.di.AppSettings
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager
import java.io.File
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SettingsLanguageFragment : NavigationFragment.SubFragment() {
    @AppSettings
    @Inject
    lateinit var appSettings: PreferenceManager
    @Inject
    lateinit var defaultFilePaths: FilePaths

    private var availableLanguageCodes = listOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dataDirectory = appSettings[PreferenceManager.PredefinedKey.DataDirPath] ?: defaultFilePaths.dataDirectoryPath
        val localeDirectory = File("${dataDirectory}/locale")
        if (localeDirectory.exists()) {
            val languageCodes = ArrayList((localeDirectory.listFiles { file ->
                return@listFiles file.isDirectory
            } ?: arrayOf()).map { file -> file.name })
            availableLanguageCodes = languageCodes.sorted()
        }

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

        title = CelestiaString("Language", "Display language setting")
    }

    @Composable
    private fun MainScreen() {
        val substitutionList = mapOf(
            "zh_CN" to "zh-Hans",
            "zh_TW" to "zh-Hant"
        )

        fun getLocale(locale: String): Locale {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                val components = locale.split("_")
                if (components.size == 1)
                    return Locale.of(components[0])
                if (components.size == 2)
                    return Locale.of(components[0], components[1])
                return Locale.of(components[0], components[1], components[2])
            }
            val components = locale.split("_")
            if (components.size == 1) {
                @Suppress("DEPRECATION")
                return Locale(components[0])
            }
            if (components.size == 2) {
                @Suppress("DEPRECATION")
                return Locale(components[0], components[1])
            }
            @Suppress("DEPRECATION")
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
        var currentOverrideLanguage: String? by remember {
            mutableStateOf(currentOverrideLanguage())
        }
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        LazyColumn(modifier = Modifier.nestedScroll(nestedScrollInterop), contentPadding = WindowInsets.systemBars.asPaddingValues()) {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                TextRow(primaryText = CelestiaString("Current Language", "Current display language"), secondaryText = getLocalizedLanguageName(currentLanguage()))
            }

            if (availableLanguageCodes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                    Separator()
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                }
                items(items = availableLanguageCodes) {
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
                    Text(text = CelestiaString("Reset to Default", "Reset celestia.cfg, data directory location"))
                }
                Footer(text = CelestiaString("Configuration will take effect after a restart.", "Change requires a restart"))
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
            }
        }
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

    private fun currentOverrideLanguage(): String? {
        val overrideLocale = AppCompatDelegate.getApplicationLocales()
        if (overrideLocale.isEmpty)
            return null

        // If set from system picker it is possible that the override locale
        // is not exactly the one supported by Celestia, we check a full
        // match with language_region first and then language only
        val locale = Locale.forLanguageTag(overrideLocale.toLanguageTags())
        if (locale.country.isNotEmpty()) {
            val potential = "${locale.language}_${locale.country}"
            if (availableLanguageCodes.contains(potential))
                return potential
        }
        if (availableLanguageCodes.contains(locale.language))
            return locale.language
        return null
    }

    private fun currentLanguage(): String {
        return AppCore.getLanguage()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsLanguageFragment()
    }
}
