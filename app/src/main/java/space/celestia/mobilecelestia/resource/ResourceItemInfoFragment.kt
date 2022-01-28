/*
 * ResourceItemInfoFragment.kt
 *
 * Copyright (C) 2001-2022, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.resource.model.ResourceItem
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.GlideUrlCustomCacheKey
import java.text.DateFormat
import java.util.*

class ResourceItemInfoFragment: Fragment() {
    private lateinit var item: ResourceItem

    private var imageView: ImageView? = null
    private var descriptionLabel: TextView? = null
    private var authorsLabel: TextView? = null
    private var releaseDateLabel: TextView? = null

    private val formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        item = requireArguments().getSerializable(ARG_ITEM) as ResourceItem
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resource_item_info, container, false)
        val content = view.findViewById<TextView>(R.id.content)
        val footnote = view.findViewById<TextView>(R.id.footnote)
        val image = view.findViewById<ImageView>(R.id.image)
        val authors = view.findViewById<TextView>(R.id.authors)
        val releaseDate = view.findViewById<TextView>(R.id.publish_time)
        footnote.text = CelestiaString("Note: restarting Celestia is needed to use any new installed add-on.", "")
        this.descriptionLabel = content
        this.imageView = image
        this.authorsLabel = authors
        this.releaseDateLabel = releaseDate

        updateContents()

        return view
    }

    fun updateItem(item: ResourceItem) {
        this.item = item
        updateContents()
    }

    private fun updateContents() {
        val item = this.item ?: return
        descriptionLabel?.text = item.description

        val imageURL = item.image
        val itemID = item.id
        val imageView = this.imageView
        if (imageURL != null && imageView != null) {
            imageView.visibility = View.VISIBLE
            Glide.with(this).load(GlideUrlCustomCacheKey(imageURL, itemID)).into(imageView)
        } else {
            imageView?.visibility = View.GONE
        }

        val authors = item.authors
        if (authors != null && authors.isNotEmpty()) {
            authorsLabel?.visibility = View.VISIBLE
            authorsLabel?.text = CelestiaString("Authors: %s", "").format(authors.joinToString(", "))
        } else {
            authorsLabel?.visibility = View.GONE
        }

        val releaseDate = item.publishTime
        if (releaseDate != null) {
            releaseDateLabel?.visibility = View.VISIBLE
            releaseDateLabel?.text = CelestiaString("Release date: %s", "").format(formatter.format(releaseDate))
        } else {
            releaseDateLabel?.visibility = View.GONE
        }
    }

    companion object {
        private const val ARG_ITEM = "item"
        @JvmStatic
        fun newInstance(item: ResourceItem) =
            ResourceItemInfoFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}