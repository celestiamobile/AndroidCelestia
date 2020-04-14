/*
 * SearchRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.common.CommonSection
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorRecyclerViewAdapter
import space.celestia.mobilecelestia.search.model.SearchResultItem

class SearchRecyclerViewAdapter(
    private val listener: SearchFragment.Listener?
) : SeparatorRecyclerViewAdapter() {

    override fun itemViewType(item: RecyclerViewItem): Int {
        return SEARCH_RESULT
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CommonTextViewHolder(parent)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is SearchResultItem && holder is CommonTextViewHolder) {
            holder.title.text = item.result
        }
    }

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is SearchResultItem) {
            listener?.onSearchItemSelected(item.result)
        }
    }

    fun updateSearchResults(results: List<String>) {
        updateSections(listOf(CommonSection(results.map { SearchResultItem(it) })))
    }

    private companion object {
        const val SEARCH_RESULT = 0
    }
}
