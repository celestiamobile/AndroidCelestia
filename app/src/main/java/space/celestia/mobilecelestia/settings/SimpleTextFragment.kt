/*
 * SimpleTextFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Mdc3Theme

class SimpleTextFragment : NavigationFragment.SubFragment() {
    private var textTitle: String? = null
    private var textDetail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            textTitle = it.getString(ARG_TITLE, null)
            textDetail = it.getString(ARG_DETAIL, null)
        }
    }

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

    @Composable
    fun MainScreen() {
        val scroll = rememberScrollState(0)
        SelectionContainer(modifier = Modifier
            .verticalScroll(scroll)
            .fillMaxWidth()
            .padding(
                top = dimensionResource(id = R.dimen.common_page_medium_margin_vertical),
                bottom = dimensionResource(id = R.dimen.common_page_medium_margin_vertical),
                start = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                end = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
            )
        ) {
            Text(text = textDetail ?: "", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = textTitle ?: ""
    }

    companion object {
        const val ARG_TITLE = "title"
        const val ARG_DETAIL = "detail"

        @JvmStatic
        fun newInstance(title: String, detail: String) =
            SimpleTextFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_DETAIL, detail)
                }
            }
    }
}
