/*
 * SeparatorRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDivider
import space.celestia.mobilecelestia.R

interface ViewItem

interface RecyclerViewItem : ViewItem {
    val clickable: Boolean
        get() = true
}

open class CommonSection(val items: List<RecyclerViewItem>,
                         val showSectionSeparator: Boolean = true,
                         val showRowSeparator: Boolean = true,
                         val showTopSectionSeparatorOnly: Boolean = false)

open class SeparatorRecyclerViewAdapter(@DimenRes private val separatorInsetStartResource: Int = 0,
                                        @DimenRes private val separatorContainerHeightResource: Int = 0,
                                        @DrawableRes private val separatorBackgroundResource: Int = R.drawable.background,
                                        sections: List<CommonSection> = listOf(),
                                        private val fullSection: Boolean = true,
                                        private val showFirstAndLastSeparator: Boolean = true,
                                        private val showSeparators: Boolean = true) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private class SeparatorItem(val full: Boolean) : ViewItem

    private var values: ArrayList<ViewItem> = arrayListOf()

    private val onClickListener: View.OnClickListener

    open val isItemClickable: Boolean
        get() = true

    init {
        updateSections(sections)
        onClickListener = View.OnClickListener { v ->
            val tag = v.tag
            if (tag is RecyclerViewItem) {
                onItemSelected(tag)
            }
        }
    }

    fun swapItem(index1: Int, index2: Int): Boolean {
        val item1 = values[index1]
        val item2 = values[index2]
        if (item1 is RecyclerViewItem && item2 is RecyclerViewItem && swapItem(item1, item2)) {
            values[index1] = item2
            values[index2] = item1
            notifyItemMoved(index1, index2)
            return true
        }
        return false
    }

    open fun swapItem(item1: RecyclerViewItem, item2: RecyclerViewItem): Boolean {
        return false
    }

    override fun getItemViewType(position: Int): Int {
        val item = values[position]
        if (item is SeparatorItem) {
            if (item.full)
                return SEPARATOR_0
            return SEPARATOR_1
        }
        if (item is RecyclerViewItem) {
            return itemViewType(item)
        }
        throw RuntimeException("$this must deal with item type $item")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SEPARATOR_0) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_separator, parent, false)
            val separator = view.findViewById<MaterialDivider>(R.id.separator_view)
            separator.setDividerInsetStartResource(R.dimen.partial_separator_inset_start)
            view.setBackgroundResource(separatorBackgroundResource)
            view.updateLayoutParams<ViewGroup.LayoutParams> {
                height = if (separatorContainerHeightResource != 0) parent.resources.getDimensionPixelSize(separatorContainerHeightResource) else ViewGroup.LayoutParams.WRAP_CONTENT
            }
            return SeparatorViewHolder(view)
        }
        if (viewType == SEPARATOR_1) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_separator, parent, false)
            val separator = view.findViewById<MaterialDivider>(R.id.separator_view)
            separator.setDividerInsetStartResource(if (separatorInsetStartResource != 0) separatorInsetStartResource else R.dimen.partial_separator_inset_start)
            view.updateLayoutParams<ViewGroup.LayoutParams> {
                height = if (separatorContainerHeightResource != 0) parent.resources.getDimensionPixelSize(separatorContainerHeightResource) else ViewGroup.LayoutParams.WRAP_CONTENT
            }
            view.setBackgroundResource(separatorBackgroundResource)
            return SeparatorViewHolder(view)
        }
        return createVH(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = values[position]
        if (item is RecyclerViewItem) {
            bindVH(holder, item)
            if (item.clickable && isItemClickable) {
                with(holder.itemView) {
                    tag = item
                    isClickable = true
                    setOnClickListener(onClickListener)
                }
            } else {
                with(holder.itemView) {
                    setOnClickListener(null)
                    isClickable = false
                }
            }
        }
    }

    override fun getItemCount(): Int = values.size

    open fun onItemSelected(item: RecyclerViewItem) {}

    open fun itemViewType(item: RecyclerViewItem): Int {
        throw RuntimeException("$this must deal with item type $item")
    }

    open fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        throw RuntimeException("$this must deal with item type $item")
    }

    open fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        throw RuntimeException("$this must deal with item type $viewType")
    }

    fun updateSections(sections: List<CommonSection>) {
        val data = ArrayList<ViewItem>()
        var prevSectionHasSep = false
        for (i in 0 until sections.size) {
            val section = sections[i]
            // add a separator to section top
            val showSectionSeparator = showSeparators && section.showSectionSeparator && section.items.count() > 0
            if (!prevSectionHasSep && showSectionSeparator) {
                data.add(SeparatorItem(fullSection))
            }
            for (j in 0 until section.items.count()) {
                data.add(section.items[j])
                // add separators to in between
                if (showSeparators && section.showRowSeparator && j != section.items.count() - 1) {
                    data.add(SeparatorItem(false))
                }
            }
            // add a separator to section bottom
            if (showSectionSeparator && !section.showTopSectionSeparatorOnly) {
                data.add(SeparatorItem(fullSection))
                prevSectionHasSep = true
            } else {
                prevSectionHasSep = false
            }
        }
        if (showSeparators && !showFirstAndLastSeparator && data.size > 1) {
            if (data[0] is SeparatorItem) {
                data.removeAt(0)
            }
            if (data[data.size - 1] is SeparatorItem) {
                data.removeAt(data.size - 1)
            }
        }
        values = data
    }

    inner class SeparatorViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        const val SEPARATOR_0 = 99998
        const val SEPARATOR_1 = 99999
    }
}

class CommonSectionV2(items: List<RecyclerViewItem>, val header: String? = "", val footer: String? = null) : CommonSection(items)