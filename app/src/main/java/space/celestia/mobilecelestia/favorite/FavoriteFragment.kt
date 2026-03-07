// FavoriteFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.favorite

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import space.celestia.celestiaui.compose.Mdc3Theme
import space.celestia.celestiaui.favorite.FavoriteBookmarkItem
import space.celestia.celestiaui.favorite.FavoriteContainer
import space.celestia.celestiaui.favorite.FavoriteScriptItem
import space.celestia.celestiaui.favorite.MutableFavoriteBaseItem

class FavoriteFragment : Fragment() {
    interface Listener {
        fun saveFavorites()
        fun shareFavoriteItem(item: MutableFavoriteBaseItem)
        fun openFavoriteBookmark(item: FavoriteBookmarkItem)
        fun openFavoriteScript(item: FavoriteScriptItem)
    }

    private var listener: Listener? = null

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
                    FavoriteContainer(shareRequested = { item ->
                        listener?.shareFavoriteItem(item)
                    }, openBookmarkRequested = { item ->
                        listener?.openFavoriteBookmark(item)
                    }, openScriptRequested = { item ->
                        listener?.openFavoriteScript(item)
                    }, saveFavorites = {
                        listener?.saveFavorites()
                    })
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FavoriteFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavoriteFragment()
    }
}
