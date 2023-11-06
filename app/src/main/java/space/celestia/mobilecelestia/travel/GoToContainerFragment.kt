/*
 * GoToContainerFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.travel

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.utils.CelestiaString

class GoToContainerFragment : Fragment() {
    private var listener: Listener? = null
    private val goToData: GoToData
        get() = requireNotNull(_goToData)
    private var _goToData: GoToData? = null

    private val selection: Selection
        get() = requireNotNull(_selection)
    private var _selection: Selection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            _selection = BundleCompat.getParcelable(it, ARG_OBJECT, Selection::class.java)
            _goToData = BundleCompat.getSerializable(it, ARG_DATA, GoToData::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(topBar = {
            TopAppBar(title = {
                Text(text = CelestiaString("Go to Object", ""))
            }, scrollBehavior = scrollBehavior)
        }) { paddingValues ->
            GoToScreen(initialData = goToData, selection = selection, paddingValues = paddingValues, handler = { data, selection ->
                listener?.onGoToObject(data, selection)
            }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement GoToContainerFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onGoToObject(goToData: GoToData, selection: Selection)
    }

    companion object {
        private const val ARG_DATA = "data"
        private const val ARG_OBJECT = "object"

        @JvmStatic
        fun newInstance(goToData: GoToData, selection: Selection) =
            GoToContainerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_OBJECT, selection)
                    putSerializable(ARG_DATA, goToData)
                }
            }
    }
}
