// HelpFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.help

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.Fragment
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.utils.CelestiaString

enum class HelpAction {
    RunDemo;
}

class HelpFragment : Fragment() {
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement HelpFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @Composable
    private fun MainScreen() {
        val staticHelpDescriptionItems: List<DescriptionItem> by lazy { listOf(
            DescriptionItem(
                CelestiaString("Tap the mode button on the sidebar to switch between object mode and camera mode.", ""), R.drawable.tutorial_switch_mode),
            DescriptionItem(
                CelestiaString("In object mode, drag to rotate around an object.\n\nPinch to zoom in/out on an object.", ""), R.drawable.tutorial_mode_object),
            DescriptionItem(
                CelestiaString("In camera mode, drag to move field of view.\n\nPinch to zoom in/out field of view.", ""), R.drawable.tutorial_mode_camera)
        ) }
        val staticHelpURLItems: List<URLItem> by lazy { listOf(
            URLItem(CelestiaString("Mouse/Keyboard Controls", "Guide to control Celestia with a mouse/keyboard"), "celguide://guide?guide=BE1B5023-46B6-1F10-F15F-3B3F02F30300"),
            URLItem(CelestiaString("Use Add-ons and Scripts", "URL for Use Add-ons and Scripts wiki"), "celguide://guide?guide=D1A96BFA-00BB-0089-F361-10DD886C8A4F"),
            URLItem(CelestiaString("Scripts and URLs", "URL for Scripts and URLs wiki"), "celguide://guide?guide=A0AB3F01-E616-3C49-0934-0583D803E9D0")
        ) }
        val staticHelpActionItems: List<ActionItem> by lazy {
            listOf(
                ActionItem(CelestiaString("Run Demo", ""), HelpAction.RunDemo),
            )
        }
        val buttonModifier = Modifier.fillMaxWidth().padding(
            vertical = dimensionResource(id = R.dimen.list_text_gap_vertical),
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal)
        )
        LazyColumn(
            contentPadding = WindowInsets.systemBars.asPaddingValues(),
            modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection())
        ) {
            item {
                Text(text = CelestiaString("Welcome to Celestia", "Welcome message"), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                    end = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                    top = dimensionResource(id = R.dimen.common_page_medium_margin_vertical),
                    bottom = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
                ))
            }

            items(staticHelpDescriptionItems) {
                Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.tutorial_list_item_gap_horizontal)), modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal), vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical))) {
                    Icon(painter = painterResource(id = it.imageResourceID), contentDescription = "", modifier = Modifier.size(dimensionResource(id = R.dimen.tutorial_list_icon_dimension)), tint = MaterialTheme.colorScheme.onBackground)
                    Text(text = it.description, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
                }
            }

            items(staticHelpURLItems) {
                FilledTonalButton(modifier = buttonModifier, onClick = {
                    listener?.onHelpURLSelected(it.url)
                }) {
                    Text(text = it.title)
                }
            }

            items(staticHelpActionItems) {
                FilledTonalButton(modifier = buttonModifier, onClick = {
                    listener?.onHelpActionSelected(it.action)
                }) {
                    Text(text = it.title)
                }
            }
        }
    }

    interface Listener {
        fun onHelpActionSelected(action: HelpAction)
        fun onHelpURLSelected(url: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HelpFragment()
    }
}
