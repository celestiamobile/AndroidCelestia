/*
 * FavoriteItemFragment.kt
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.utils.CelestiaString

class FavoriteItemFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null

    var favoriteItem: FavoriteBaseItem? = null
    private val itemHelper by lazy { ItemTouchHelper(FavoriteItemItemTouchCallback()) }
    private val listAdapter by lazy { FavoriteItemRecyclerViewAdapter(favoriteItem!!, listener, this.itemHelper) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            favoriteItem = it.getSerializable(ARG_ITEM) as? FavoriteBaseItem
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general_grouped_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
                itemHelper.attachToRecyclerView(this)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            title = favoriteItem?.title ?: ""
            var items: List<NavigationFragment.BarButtonItem> = listOf()
            if (favoriteItem is MutableFavoriteBaseItem) {
                items = listOf(
                    NavigationFragment.BarButtonItem(MENU_ITEM_ADD, CelestiaString("Add", ""), R.drawable.ic_add)
                )
            }
            rightNavigationBarItems = items
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FavoriteItemFragment.Listener")
        }
    }

    override fun menuItemClicked(groupId: Int, id: Int): Boolean {
        when (id) {
            MENU_ITEM_ADD -> {
                listener?.addFavoriteItem(favoriteItem as MutableFavoriteBaseItem)
            } else -> {}
        }
        return true
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun reload() {
        listAdapter.reload()
        listAdapter.notifyDataSetChanged()
    }

    interface Listener {
        fun onFavoriteItemSelected(item: FavoriteBaseItem)
        fun deleteFavoriteItem(index: Int)
        fun renameFavoriteItem(item: MutableFavoriteBaseItem)
        fun addFavoriteItem(item: MutableFavoriteBaseItem)
        fun shareFavoriteItem(item: MutableFavoriteBaseItem)
    }

    companion object {
        const val ARG_ITEM = "item"
        const val MENU_ITEM_ADD = 0

        @JvmStatic
        fun newInstance(item: FavoriteBaseItem) =
            FavoriteItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}
