/*
 * DestinationDetailFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import space.celestia.celestia.Destination
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.celestiafoundation.utils.getSerializableValue

class DestinationDetailFragment : NavigationFragment.SubFragment() {
    private var item: Destination? = null
    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            item = it.getSerializableValue(ARG_ITEM, Destination::class.java)
        }
    }
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
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = item?.name ?: ""
    }

    @Composable
    fun MainScreen() {
        val scroll = rememberScrollState(0)
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        Column(modifier = Modifier
            .nestedScroll(nestedScrollInterop)
            .padding(
                start = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                end = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
            ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            SelectionContainer(modifier = Modifier
                .verticalScroll(scroll)
                .weight(1.0f)
                .fillMaxWidth()
                .padding(
                    top = dimensionResource(id = R.dimen.common_page_medium_margin_vertical),
                )
            ) {
                Text(text = item?.description ?: "", color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium), style = MaterialTheme.typography.bodyLarge)
            }
            FilledTonalButton(modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
                    bottom = dimensionResource(id = R.dimen.common_page_medium_margin_vertical),
                ), onClick = {
                onButtonClick()
            }) {
                Text(text = CelestiaString("Go", "Go to an object"))
            }
        }
    }

    private fun onButtonClick() {
        val destination = item ?: return
        listener?.onGoToDestination(destination)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement DestinationDetailFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onGoToDestination(destination: Destination)
    }

    companion object {
        private const val ARG_ITEM = "item"
        @JvmStatic
        fun newInstance(destination: Destination) =
            DestinationDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, destination)
                }
            }
    }
}