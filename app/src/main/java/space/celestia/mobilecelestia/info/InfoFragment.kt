/*
 * InfoFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.info

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.LayoutDirection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.celestia.AppCore
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.info.model.*
import space.celestia.mobilecelestia.utils.getOverviewForSelection
import space.celestia.ui.linkpreview.LPLinkMetadata
import space.celestia.ui.linkpreview.LPMetadataProvider
import java.net.MalformedURLException
import java.net.URL

class InfoFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null
    private lateinit var selection: Selection
    private var embeddedInNavigation = false
    private var linkMetadata: LPLinkMetadata? = null

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            selection = Selection(it.getLong(ARG_OBJECT_POINTER), it.getInt(ARG_OBJECT_TYPE))
            embeddedInNavigation = it.getBoolean(ARG_EMBEDDED_IN_NAVIGATION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        recyclerView = inflater.inflate(R.layout.fragment_info_list, container, false) as RecyclerView

        if (embeddedInNavigation)
            recyclerView.setBackgroundResource(R.color.colorBackground)

        reload()
        return recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null)
            title = ""
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement InfoFragment.nListener")
        }
    }

    override fun onDestroy() {
        selection.close()
        super.onDestroy()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onInfoActionSelected(action: InfoActionItem, item: Selection)
        fun onInfoLinkMetaDataClicked(url: URL)
    }

    private fun reload() {
        val core = AppCore.shared()
        val overview = core.getOverviewForSelection(selection)
        val name = core.simulation.universe.getNameForSelection(selection)
        val hasAltSurface = (selection.body?.alternateSurfaceNames?.size ?: 0) > 0

        var hasWebInfo = false
        var hideWebInfo = false
        val webInfoURL = selection.webInfoURL
        if (webInfoURL != null) {
            try {
                val url = URL(webInfoURL)
                hasWebInfo = true
                val fetcher = LPMetadataProvider()
                if (linkMetadata == null) {
                    hideWebInfo = true
                    fetcher.startFetchMetadataForURL(lifecycleScope, url) { metaData, _ ->
                        if (metaData == null) { return@startFetchMetadataForURL }
                        withContext(Dispatchers.Main) {
                            linkMetadata = metaData
                            reload()
                        }
                    }
                }
            } catch (ignored: MalformedURLException) {}
        }

        val metadata = linkMetadata

        val descriptionItem = InfoDescriptionItem(name, overview, hasWebInfo && !hideWebInfo, hasAltSurface)
        val manager = GridLayoutManager(context, 2)
        val firstSingleColumnItem = if (metadata == null) 1 else 2
        manager.spanSizeLookup = SizeLookup(firstSingleColumnItem)
        recyclerView.layoutManager = manager
        val actions = ArrayList<InfoItem>()
        actions.add(descriptionItem)
        if (metadata != null)
            actions.add(InfoMetadataItem(metadata))
        val otherActions = ArrayList(InfoActionItem.infoActions)
        if (descriptionItem.hasWebInfo)
            otherActions.add(InfoWebActionItem())
        if (descriptionItem.hasAlternateSurfaces)
            otherActions.add(AlternateSurfacesItem())
        otherActions.add(SubsystemActionItem())
        otherActions.add(MarkItem())
        actions.addAll(otherActions)
        recyclerView.adapter = InfoRecyclerViewAdapter(actions, selection, listener)
        while (recyclerView.getItemDecorationCount() > 0) {
            recyclerView.removeItemDecorationAt(0)
        }
        recyclerView.addItemDecoration(SpaceItemDecoration(firstSingleColumnItem))
    }

    inner class SizeLookup(private val firstSingleColumnItem: Int): GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            if (position < firstSingleColumnItem) { return 2 }
            return 1
        }
    }

    inner class SpaceItemDecoration(private val firstSingleColumnItem: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val density =  parent.resources.displayMetrics.density
            val isRTL = parent.resources.configuration.layoutDirection == LayoutDirection.RTL
            val spacing = (16 * density).toInt()

            val pos = parent.getChildLayoutPosition(view)
            if (pos < firstSingleColumnItem) {
                outRect.left = spacing
                outRect.right = spacing
                outRect.top = spacing
                outRect.bottom = if (pos == firstSingleColumnItem - 1) spacing else 0
            } else {
                outRect.top = 0
                outRect.bottom = spacing
                if ((pos - firstSingleColumnItem) % 2 == 1) {
                    outRect.left = if (isRTL) spacing else (spacing / 2)
                    outRect.right = if (isRTL) (spacing / 2) else spacing
                } else {
                    outRect.left = if (isRTL) (spacing / 2) else spacing
                    outRect.right = if (isRTL) spacing else (spacing / 2)
                }
            }
        }
    }

    companion object {
        const val ARG_OBJECT_POINTER = "object"
        const val ARG_OBJECT_TYPE = "type"
        const val ARG_EMBEDDED_IN_NAVIGATION = "embedded-in-navigation"

        @JvmStatic
        fun newInstance(selection: Selection, embeddedInNavigation: Boolean = false) =
            InfoFragment().apply {
                arguments = Bundle().apply {
                    this.putLong(ARG_OBJECT_POINTER, selection.selectionPointer)
                    this.putInt(ARG_OBJECT_TYPE, selection.type)
                    this.putBoolean(ARG_EMBEDDED_IN_NAVIGATION, embeddedInNavigation)
                }
            }
    }
}
