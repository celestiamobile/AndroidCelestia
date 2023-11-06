/*
 * SettingsItemFragment.kt
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.purchase.PurchaseManager
import space.celestia.mobilecelestia.utils.CelestiaString
import javax.inject.Inject

@AndroidEntryPoint
class SettingsItemFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null

    @Inject
    lateinit var purchaseManager: PurchaseManager

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

        title = CelestiaString("Settings", "")
    }

    @Composable
    private fun MainScreen() {
        val sections = if (purchaseManager.canUseInAppPurchase()) mainSettingSectionsBeforePlus + celestiaPlusSettingSection + mainSettingSectionsAfterPlus else mainSettingSectionsBeforePlus + mainSettingSectionsAfterPlus
        LazyColumn(modifier = Modifier
            .nestedScroll(rememberNestedScrollInteropConnection()), contentPadding = WindowInsets.systemBars.asPaddingValues()) {
            for (index in sections.indices) {
                val section = sections[index]
                item {
                    val header = section.header
                    if (header.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                    } else {
                        Header(text = header)
                    }
                }
                items(section.items) { item ->
                    if (item is SettingsItem)
                        TextRow(primaryText = item.name, modifier = Modifier.clickable {
                            listener?.onMainSettingItemSelected(item)
                        })
                }
                item {
                    val footer = section.footer
                    if (!footer.isNullOrEmpty()) {
                        Footer(text = footer)
                    }
                    if (index == sections.size - 1) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
                    } else {
                        val nextHeader = sections[index + 1].header
                        if (nextHeader.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                            if (footer.isNullOrEmpty()) {
                                Separator()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SettingsItemFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onMainSettingItemSelected(item: SettingsItem)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsItemFragment()
    }
}
