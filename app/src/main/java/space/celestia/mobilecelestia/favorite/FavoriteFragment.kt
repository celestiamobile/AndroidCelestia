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
import androidx.appcompat.widget.Toolbar
import space.celestia.mobilecelestia.common.NavigationFragment

class FavoriteFragment : NavigationFragment(), Toolbar.OnMenuItemClickListener {
    private var listener: Listener? = null

    private val current: FavoriteItemFragment
        get() = requireNotNull(top) as FavoriteItemFragment

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return FavoriteItemFragment.newInstance(FavoriteRoot())
    }

    fun pushItem(item: FavoriteBaseItem) {
        val frag = FavoriteItemFragment.newInstance(item)
        pushFragment(frag)
    }

    fun add(item: FavoriteBaseItem) {
        (current.favoriteItem as MutableFavoriteBaseItem).append(item)
        current.reload()
    }

    fun remove(index: Int) {
        (current.favoriteItem as MutableFavoriteBaseItem).remove(index)
        current.reload()
    }

    fun move(fromIndex: Int, toIndex: Int) {
        (current.favoriteItem as MutableFavoriteBaseItem).move(fromIndex, toIndex)
        current.reload()
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
        listener?.saveFavorites()
        listener = null
    }

    interface Listener {
        fun saveFavorites()
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavoriteFragment()
    }
}
