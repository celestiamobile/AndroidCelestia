/*
 * AboutRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.utils.CelestiaString

interface AboutItem : RecyclerViewItem

class VersionItem(val versionName: String) : AboutItem {
    val title: String
        get() = CelestiaString("Version", "")
    override val clickable: Boolean
        get() = false
}

class ActionItem(val title: String, val url: String) : AboutItem {
    override val clickable: Boolean
        get() = true
}

class TitleItem(val title: String) : AboutItem {
    override val clickable: Boolean
        get() = false
}

class DetailItem(val detail: String) : AboutItem {
    override val clickable: Boolean
        get() = false
}

class AboutRecyclerViewAdapter(
    values: List<List<AboutItem>>,
    private val listener: AboutFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(values.map { CommonSectionV2(it) }) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is ActionItem) {
            listener?.onAboutURLSelected(item.url)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is VersionItem)
            return VERSION_ITEM
        if (item is ActionItem)
            return ACTION_ITEM
        if (item is TitleItem)
            return TITLE_ITEM
        if (item is DetailItem)
            return DETAIL_ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VERSION_ITEM) {
            return CommonTextViewHolder(parent)
        }
        if (viewType == ACTION_ITEM) {
            val holder = CommonTextViewHolder(parent)
            holder.title.setTextColor(ResourcesCompat.getColor(parent.resources, R.color.colorThemeLabel, null))
            return holder
        }
        if (viewType == TITLE_ITEM) {
            return CommonTextViewHolder(parent)
        }
        if (viewType == DETAIL_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.common_multiline_text_item, parent, false)
            return MultilineViewHolder(view)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is CommonTextViewHolder) {
            when (item) {
                is VersionItem -> {
                    holder.title.text = item.title
                    holder.detail.text = item.versionName
                }
                is ActionItem -> {
                    holder.title.text = item.title
                }
                is TitleItem -> {
                    holder.title.text = item.title
                }
            }
            return
        }
        if (item is DetailItem && holder is MultilineViewHolder) {
            holder.textView.text = item.detail
            return
        }
        super.bindVH(holder, item)
    }

    inner class MultilineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
            get() = itemView.findViewById(R.id.text)
    }

    private companion object {
        const val VERSION_ITEM = 0
        const val ACTION_ITEM = 1
        const val TITLE_ITEM = 2
        const val DETAIL_ITEM = 3
    }
}
