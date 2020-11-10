/*
 * FavoriteFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.favorite

import android.content.Context
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import space.celestia.mobilecelestia.common.NavigationFragment

class FavoriteFragment : NavigationFragment(), Toolbar.OnMenuItemClickListener {
    private var listener: Listener? = null

    private val currentFrag: FavoriteItemFragment
        get() = childFragmentManager.fragments.last() as FavoriteItemFragment

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return FavoriteItemFragment.newInstance(FavoriteRoot())
    }

    fun pushItem(item: FavoriteBaseItem) {
        val frag = FavoriteItemFragment.newInstance(item)
        pushFragment(frag)
    }

    fun add(item: FavoriteBaseItem) {
        val frag = currentFrag
        (frag.favoriteItem as MutableFavoriteBaseItem).append(item)
        frag.reload()
    }

    fun remove(index: Int) {
        val frag = currentFrag
        (frag.favoriteItem as MutableFavoriteBaseItem).remove(index)
        frag.reload()
    }

    fun rename(item: MutableFavoriteBaseItem, newName: String) {
        val frag = currentFrag
        item.rename(newName)
        frag.reload()
    }

    override fun menuItemClicked(id: Int): Boolean {
        when (id) {
            MENU_ITEM_ADD -> {
                listener?.addFavoriteItem(currentFrag.favoriteItem as MutableFavoriteBaseItem)
            } else -> {}
        }
        return true
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
        fun addFavoriteItem(item: MutableFavoriteBaseItem)
        fun saveFavorites()
    }

    companion object {
        fun newInstance() = FavoriteFragment()

        const val MENU_ITEM_ADD = 0
    }
}
