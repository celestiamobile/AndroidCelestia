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
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R

interface ViewItem

interface RecyclerViewItem : ViewItem {
    val clickable: Boolean
        get() = true
}

open class CommonSection(val items: List<RecyclerViewItem>,
                         val showSectionSeparator: Boolean = true,
                         val showRowSeparator: Boolean = true)

open class SeparatorRecyclerViewAdapter(private val separatorHeight: Int = 1,
                                        private val separatorLeft: Int = 16,
                                        private val separatorBackgroundColor: Int = R.color.colorSecondaryBackground,
                                        sections: List<CommonSection> = listOf(),
                                        private val fullSection: Boolean = true,
                                        private val showFirstAndLastSeparator: Boolean = true) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private class SeparatorItem(val full: Boolean) : ViewItem

    private var values: ArrayList<ViewItem> = arrayListOf()

    private val onClickListener: View.OnClickListener

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
            val view = SeparatorView(parent.context, 1, 0, separatorBackgroundColor)
            return SeparatorViewHolder(view)
        }
        if (viewType == SEPARATOR_1) {
            val view = SeparatorView(parent.context, separatorHeight, separatorLeft, separatorBackgroundColor)
            return SeparatorViewHolder(view)
        }
        return createVH(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = values[position]
        if (item is RecyclerViewItem) {
            bindVH(holder, item)
            if (item.clickable) {
                with(holder.itemView) {
                    tag = item
                    setOnClickListener(onClickListener)
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
            val showSectionSeparator = section.showSectionSeparator && section.items.count() > 0
            if (!prevSectionHasSep && showSectionSeparator) {
                data.add(SeparatorItem(fullSection))
            }
            for (j in 0 until section.items.count()) {
                data.add(section.items[j])
                // add separators to in between
                if (section.showRowSeparator && j != section.items.count() - 1) {
                    data.add(SeparatorItem(false))
                }
            }
            // add a separator to section bottom
            if (showSectionSeparator) {
                data.add(SeparatorItem(fullSection))
            }
            prevSectionHasSep = showSectionSeparator
        }
        if (!showFirstAndLastSeparator && data.size > 1) {
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

private class HeaderRecyclerViewItem(val title: String?): RecyclerViewItem
private class FooterRecyclerViewItem(val title: String?): RecyclerViewItem

fun List<CommonSectionV2>.transformed(): List<CommonSection> {
    val innerSections = ArrayList<CommonSection>()
    var lastSectionHasFooter = size == 0
    for (section in this) {
        if (section.header != null) {
            innerSections.add(
                CommonSection(listOf(HeaderRecyclerViewItem(section.header)),
                    showSectionSeparator = false, showRowSeparator = false
                ))
        }

        innerSections.add(section)

        if (section.footer != null) {
            innerSections.add(
                CommonSection(
                    listOf(FooterRecyclerViewItem(section.footer)),
                    showSectionSeparator = false, showRowSeparator = false
                )
            )
            lastSectionHasFooter = true
        }
    }
    if (!lastSectionHasFooter)
        innerSections.add(CommonSection(listOf(FooterRecyclerViewItem(null)), showRowSeparator = false ,showSectionSeparator = false))
    return innerSections
}

open class SeparatorHeaderRecyclerViewAdapter(sections: List<CommonSectionV2> = listOf()): SeparatorRecyclerViewAdapter(sections = sections.transformed()) {
    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is HeaderRecyclerViewItem)
            return HEADER
        if (item is FooterRecyclerViewItem)
            return FOOTER
        throw RuntimeException("$this must deal with item type $item")
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is HeaderViewHolder && item is HeaderRecyclerViewItem) {
            holder.textView.text = item.title
            return
        }
        if (holder is FooterViewHolder && item is FooterRecyclerViewItem) {
            holder.textView.text = item.title
            return
        }
        throw RuntimeException("$this must deal with item type $item")
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.common_section_header, parent, false)
            return HeaderViewHolder(view)
        }
        if (viewType == FOOTER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.common_section_footer, parent, false)
            return FooterViewHolder(view)
        }
        throw RuntimeException("$this must deal with item type $viewType")
    }

    fun updateSectionsWithHeader(sections: List<CommonSectionV2>) {
        updateSections(sections.transformed())
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView by lazy { itemView.findViewById(R.id.text) }
    }

    inner class FooterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView by lazy { itemView.findViewById(R.id.text) }
    }

    companion object {
        const val HEADER = 99997
        const val FOOTER = 99996
    }
}