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
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.LayoutDirection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import space.celestia.celestia.AppCore
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.info.model.*
import space.celestia.mobilecelestia.utils.getOverviewForSelection
import space.celestia.ui.linkpreview.LPLinkView
import space.celestia.ui.linkpreview.LPLinkViewData
import space.celestia.ui.linkpreview.LPMetadataProvider
import java.lang.ref.WeakReference
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class InfoFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null
    private lateinit var selection: Selection
    private var embeddedInNavigation = false
    private var linkData: LPLinkViewData? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var titleLabel: TextView
    private lateinit var contentLabel: TextView
    private lateinit var linkView: LPLinkView

    @Inject
    lateinit var appCore: AppCore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            selection = Selection(it.getLong(ARG_OBJECT_POINTER), it.getInt(ARG_OBJECT_TYPE))
            embeddedInNavigation = it.getBoolean(ARG_EMBEDDED_IN_NAVIGATION, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_info_list, container, false)

        recyclerView = view.findViewById(R.id.list)
        titleLabel = view.findViewById(R.id.title)
        contentLabel = view.findViewById(R.id.content)
        linkView = view.findViewById(R.id.link_preview)
        titleLabel.text = appCore.simulation.universe.getNameForSelection(selection)
        contentLabel.text = appCore.getOverviewForSelection(selection)

        reload(true)
        return view
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
        super.onDestroy()

        linkData?.image?.recycle()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onInfoActionSelected(action: InfoActionItem, item: Selection)
        fun onInfoLinkMetaDataClicked(url: URL)
    }

    private fun reload(fetchData: Boolean) {
        val hasAltSurface = (selection.body?.alternateSurfaceNames?.size ?: 0) > 0
        val webInfoURL = selection.webInfoURL
        var hasWebInfo = false
        var hideWebInfo = false
        val linkData = this.linkData
        if (linkData != null) {
            hasWebInfo = true
            hideWebInfo = true
            linkView.linkData = linkData
            linkView.visibility = View.VISIBLE
            val weakSelf = WeakReference(this)
            linkView.setOnClickListener {
                weakSelf.get()?.listener?.onInfoLinkMetaDataClicked(linkData.url)
            }
        } else if (webInfoURL != null) {
            linkView.linkData = null
            linkView.visibility = View.GONE
            try {
                val url = URL(webInfoURL)
                hasWebInfo = true
                if (fetchData) {
                    val weakSelf = WeakReference(this)
                    fetchMetadata(url, textOnlyDataReadyBlock = {
                        val self = weakSelf.get() ?: return@fetchMetadata
                        val oldData = self.linkData
                        self.linkData = it
                        self.reload(false)
                        oldData?.image?.recycle()
                    }, imageDataReadyBlock = {
                        val self = weakSelf.get() ?: return@fetchMetadata
                        val oldData = self.linkData
                        self.linkData = it
                        self.reload(false)
                        oldData?.image?.recycle()
                    })
                }
            }
            catch(ignored: MalformedURLException) {}
        } else {
            linkView.linkData = null
            linkView.visibility = View.GONE
        }
        val manager = GridLayoutManager(context, 2)
        manager.spanSizeLookup = SizeLookup()
        recyclerView.layoutManager = manager
        val actions = ArrayList<InfoItem>()
        val otherActions = ArrayList(InfoActionItem.infoActions)
        if (hasWebInfo && !hideWebInfo)
            otherActions.add(InfoWebActionItem())
        if (hasAltSurface)
            otherActions.add(AlternateSurfacesItem())
        otherActions.add(SubsystemActionItem())
        otherActions.add(MarkItem())
        actions.addAll(otherActions)
        recyclerView.adapter = InfoRecyclerViewAdapter(actions, selection, listener)
        while (recyclerView.itemDecorationCount > 0) {
            recyclerView.removeItemDecorationAt(0)
        }
        recyclerView.addItemDecoration(SpaceItemDecoration())
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun fetchMetadata(url: URL, textOnlyDataReadyBlock: (LPLinkViewData) -> Unit, imageDataReadyBlock: (LPLinkViewData) -> Unit) {
        val fetcher = LPMetadataProvider()
        fetcher.startFetchMetadataForURL(lifecycleScope, url) { metaData, _ ->
            if (metaData == null) { return@startFetchMetadataForURL }
            textOnlyDataReadyBlock(LPLinkViewData(metaData.url, metaData.title, null, true))
            val imageURL = metaData.imageURL ?: metaData.iconURL
            val usesIcon = metaData.imageURL == null
            val client = OkHttpClient()
            val image = withContext(Dispatchers.IO) {
                try {
                    val req = Request.Builder().url(imageURL).build()
                    val res = client.newCall(req).execute()
                    val stream = res.body?.byteStream() ?: return@withContext null
                    return@withContext BitmapFactory.decodeStream(stream)
                } catch(ignored: Throwable) {
                    return@withContext null
                }
            }
            if (image != null) {
                imageDataReadyBlock(LPLinkViewData(metaData.url, metaData.title, image, usesIcon))
            }
        }
    }

    inner class SizeLookup: GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return 1
        }
    }

    inner class SpaceItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val isRTL = parent.resources.configuration.layoutDirection == LayoutDirection.RTL
            val horizontalButtonSpacing = resources.getDimensionPixelOffset(R.dimen.common_page_medium_gap_horizontal)
            val verticalButtonSpacing = resources.getDimensionPixelOffset(R.dimen.common_page_button_gap_vertical)

            val pos = parent.getChildLayoutPosition(view)
            outRect.top = 0
            outRect.bottom = verticalButtonSpacing
            if (pos % 2 == 1) {
                outRect.left = if (isRTL) 0 else (horizontalButtonSpacing / 2)
                outRect.right = if (isRTL) (horizontalButtonSpacing / 2) else 0
            } else {
                outRect.left = if (isRTL) (horizontalButtonSpacing / 2) else 0
                outRect.right = if (isRTL) 0 else (horizontalButtonSpacing / 2)
            }
        }
    }

    companion object {
        const val ARG_OBJECT_POINTER = "object"
        const val ARG_OBJECT_TYPE = "type"
        const val ARG_EMBEDDED_IN_NAVIGATION = "embedded-in-navigation"
        const val ARG_HAS_BOTTOM_BAR = "has-bottom-bar"

        @JvmStatic
        fun newInstance(selection: Selection, embeddedInNavigation: Boolean = false) =
            InfoFragment().apply {
                arguments = Bundle().apply {
                    this.putLong(ARG_OBJECT_POINTER, selection.objectPointer)
                    this.putInt(ARG_OBJECT_TYPE, selection.type)
                    this.putBoolean(ARG_EMBEDDED_IN_NAVIGATION, embeddedInNavigation)
                }
            }
    }
}
