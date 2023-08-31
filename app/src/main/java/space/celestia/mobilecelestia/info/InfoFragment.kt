/*
 * InfoFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.info

import android.content.Context
import android.os.Bundle
import android.util.LayoutDirection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.AppCore
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.LinkPreview
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.info.model.*
import space.celestia.mobilecelestia.utils.getOverviewForSelection
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class InfoFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null
    private lateinit var selection: Selection
    private var embeddedInNavigation = false

    @Inject
    lateinit var appCore: AppCore

    private lateinit var objectName: String
    private lateinit var overview: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            selection = Selection(it.getLong(ARG_OBJECT_POINTER), it.getInt(ARG_OBJECT_TYPE))
            embeddedInNavigation = it.getBoolean(ARG_EMBEDDED_IN_NAVIGATION, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        objectName = appCore.simulation.universe.getNameForSelection(selection)
        overview = appCore.getOverviewForSelection(selection)
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    Scaffold(topBar = {}) { paddingValues ->
                        MainScreen(systemPadding = paddingValues)
                    }
                }
            }
        }
    }

    @Composable
    private fun MainScreen(systemPadding: PaddingValues) {
        var isWebInfoLoaded by remember {
            mutableStateOf(false)
        }

        val rowModifier = Modifier.fillMaxWidth()

        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        val direction = if (isRTL) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
        val contentPadding = PaddingValues(
            start = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal) + systemPadding.calculateStartPadding(direction),
            top = dimensionResource(id = R.dimen.common_page_medium_margin_vertical) + systemPadding.calculateTopPadding(),
            end = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal) + systemPadding.calculateEndPadding(direction),
            bottom = dimensionResource(id = R.dimen.common_page_medium_margin_vertical) + systemPadding.calculateBottomPadding(),
        )

        LazyVerticalGrid(modifier = Modifier, contentPadding = contentPadding, columns = GridCells.Fixed(2), content = {
            val hasAltSurface = (selection.body?.alternateSurfaceNames?.size ?: 0) > 0
            val hasWebInfo = !selection.webInfoURL.isNullOrEmpty()

            item(span = { GridItemSpan(2) }) {
                Column {
                    if (!embeddedInNavigation) {
                        SelectionContainer {
                            Text(text = objectName, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineSmall, modifier = rowModifier.padding(bottom = dimensionResource(
                                id = R.dimen.common_page_medium_gap_vertical
                            )))
                        }
                    }
                    SelectionContainer {
                        Text(text = overview, color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium), style = MaterialTheme.typography.bodyLarge, modifier = rowModifier.padding(bottom = dimensionResource(
                            id = R.dimen.common_page_medium_gap_vertical
                        )))
                    }
                    val urlString = selection.webInfoURL
                    var url: URL? = null
                    try {
                        url = URL(urlString)
                    } catch (ignored: MalformedURLException) {}
                    if (hasWebInfo && url != null) {
                        LinkPreview(url = url, modifier = rowModifier.padding(bottom = dimensionResource(id = R.dimen.common_page_medium_gap_vertical)), loadResult = { loadResult ->
                            isWebInfoLoaded = loadResult
                        }, onClick = { finalURL ->
                            listener?.onInfoLinkMetaDataClicked(finalURL)
                        })
                    }
                }
            }

            val actions = ArrayList(InfoActionItem.infoActions)
            if (hasWebInfo && !isWebInfoLoaded)
                actions.add(InfoWebActionItem())
            if (hasAltSurface)
                actions.add(AlternateSurfacesItem())
            actions.add(SubsystemActionItem())
            actions.add(MarkItem())

            val count = actions.size
            items(actions.mapIndexed { index, infoActionItem -> Pair(index, infoActionItem) }) { item ->
                val horizontalButtonSpacing = R.dimen.common_page_medium_gap_horizontal
                val verticalButtonSpacing = R.dimen.common_page_button_gap_vertical
                val index = item.first
                FilledTonalButton(
                    modifier = Modifier.padding(
                        start = if (index % 2 == 0) 0.dp else dimensionResource(horizontalButtonSpacing) / 2,
                        top = if (index / 2 == 0) 0.dp else dimensionResource(verticalButtonSpacing) / 2,
                        end =  if (index % 2 == 1) 0.dp else dimensionResource(horizontalButtonSpacing) / 2,
                        bottom = if (index / 2 == (count - 1) / 2) 0.dp else dimensionResource(verticalButtonSpacing) / 2,
                    ), onClick = {
                    listener?.onInfoActionSelected(item.second, selection)
                }) {
                    Text(text = item.second.title)
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (embeddedInNavigation)
            title = objectName
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement InfoFragment.nListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onInfoActionSelected(action: InfoActionItem, item: Selection)
        fun onInfoLinkMetaDataClicked(url: URL)
    }

    companion object {
        const val ARG_OBJECT_POINTER = "object"
        const val ARG_OBJECT_TYPE = "type"
        const val ARG_EMBEDDED_IN_NAVIGATION = "embedded-in-navigation"

        @JvmStatic
        fun newInstance(selection: Selection, embeddedInNavigation: Boolean = false) =
            InfoFragment().apply {
                arguments = Bundle().apply {
                    this.putLong(ARG_OBJECT_POINTER, selection.objectPointer)
                    this.putInt(ARG_OBJECT_TYPE, selection.type)
                    this.putBoolean(ARG_EMBEDDED_IN_NAVIGATION, embeddedInNavigation)
                }
            }
    }
}
