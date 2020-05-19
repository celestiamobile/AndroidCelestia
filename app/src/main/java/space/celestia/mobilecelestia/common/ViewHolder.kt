/*
 * ViewHolder.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.common_text_list_item.view.*
import space.celestia.mobilecelestia.R

interface BaseTextItemHolder {
    val title: TextView
    val accessory: ImageView
}

class CommonTextViewHolder(parent: ViewGroup):
    RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_text_list_item, parent, false)), BaseTextItemHolder {
    override val title = itemView.title
    override var accessory = itemView.accessory
    val detail = itemView.detail

    fun configure(title: String?, detail: String? = null, accessory: Drawable? = null) {
        this.title.text = title
        this.detail.text = detail
        this.accessory.setImageDrawable(accessory)
    }
}