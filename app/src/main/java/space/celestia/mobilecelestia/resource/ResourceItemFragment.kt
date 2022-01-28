/*
 * ResourceItemFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.common.replace
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.resource.model.ResourceItem
import space.celestia.mobilecelestia.resource.model.ResourceManager
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.commonHandler
import space.celestia.mobilecelestia.utils.showAlert
import java.io.File

class ResourceItemFragment : NavigationFragment.SubFragment(), ResourceManager.Listener {
    private lateinit var item: ResourceItem
    private lateinit var statusButton: Button
    private lateinit var goToButton: Button
    private lateinit var progressIndicator: LinearProgressIndicator
    private var currentState: ResourceItemState = ResourceItemState.None

    private var listener: Listener? = null

    interface Listener {
        fun objectExistsWithName(name: String): Boolean
        fun onGoToObject(name: String)
        fun onShareAddon(name: String, id: String)
    }

    enum class ResourceItemState {
        None, Downloading, Installed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ResourceManager.shared.addListener(this)
        item = requireArguments().getSerializable(ARG_ITEM) as ResourceItem
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resource_item, container, false)

        statusButton = view.findViewById(R.id.status_button)
        statusButton.setOnClickListener {
            onProgressViewClick()
        }
        goToButton = view.findViewById(R.id.go_to_button)
        goToButton.text = CelestiaString("Go", "")
        goToButton.visibility = View.GONE
        progressIndicator = view.findViewById(R.id.progress_indicator)

        updateUI()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            val resourceItem = this.item

            title = resourceItem.name
            rightNavigationBarItems = listOf(
                NavigationFragment.BarButtonItem(SHARE_BUTTON_ID, CelestiaString("Share", ""), R.drawable.share_common_small_tint)
            )

            replace(ResourceItemInfoFragment.newInstance(resourceItem), R.id.resource_item_container)

            // Fetch the latest data from server since user might have come from `Installed`
            val lang = AppCore.getLocalizedString("LANGUAGE", "celestia")
            val service = ResourceAPI.shared.create(ResourceAPIService::class.java)
            lifecycleScope.launch {
                try {
                    val result = service.item(lang, resourceItem.id).commonHandler(ResourceItem::class.java, ResourceAPI.gson)
                    item = result
                    title = result.name
                    val fragment = childFragmentManager.findFragmentById(R.id.resource_item_container) as? ResourceItemInfoFragment
                    fragment?.updateItem(result)
                } catch (ignored: Throwable) {}
            }
        }
    }

    override fun menuItemClicked(groupId: Int, id: Int): Boolean {
        if (id == SHARE_BUTTON_ID) {
            listener?.onShareAddon(item.name, item.id)
            return true
        }
        return super.menuItemClicked(groupId, id)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ResourceItemFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroy() {
        ResourceManager.shared.removeListener(this)

        super.onDestroy()
    }

    private fun onProgressViewClick() {
        val activity = this.activity ?: return

        val dm = ResourceManager.shared

        // Already installed, offer an option for uninstalling
        if (dm.isInstalled(item.id)) {
            activity.showAlert(CelestiaString("Do you want to uninstall this add-on?", "")) {
                var success = false
                try {
                    success = dm.uninstall(item.id)
                } catch (e: Exception) {}
                if (success) {
                    currentState = ResourceItemState.None
                } else {
                    activity.showAlert(CelestiaString("Unable to uninstall add-on.", ""))
                }
                updateUI()
            }
            return
        }

        // Already downloading, allow user to cancel
        if (dm.isDownloading(item.id)) {
            activity.showAlert(CelestiaString("Do you want to cancel this task?", "")) {
                dm.cancel(item.id)
                currentState = ResourceItemState.None
                updateUI()
            }
            return
        }

        // Start downloading
        dm.download(item, File(activity.cacheDir, item.id))
        currentState = ResourceItemState.Downloading
        updateUI()
    }

    override fun onResourceFetchError(identifier: String) {
        currentState = ResourceItemState.None
        updateUI()
        activity?.showAlert(CelestiaString("Failed to download or install this add-on.", ""))
    }

    override fun onFileDownloaded(identifier: String) {
        updateUI()
    }

    override fun onFileUnzipped(identifier: String) {
        currentState = ResourceItemState.Installed
        updateUI()
    }

    override fun onProgressUpdate(identifier: String, progress: Float) {
        if (identifier != item.id) { return }

        currentState = ResourceItemState.Downloading
        progressIndicator.progress = (progress * 100).toInt()
        updateUI()
    }

    private fun updateUI() {
        // Ensure we are up to date with these cases
        val dm = ResourceManager.shared
        if (dm.isInstalled(item.id))
            currentState = ResourceItemState.Installed
        if (dm.isDownloading(item.id))
            currentState = ResourceItemState.Downloading

        when (currentState) {
            ResourceItemState.None -> {
                progressIndicator.visibility = View.GONE
                progressIndicator.progress = 0
                statusButton.text = CelestiaString("Install", "")
            }
            ResourceItemState.Downloading -> {
                progressIndicator.visibility = View.VISIBLE
                statusButton.text = CelestiaString("Cancel", "")
            }
            ResourceItemState.Installed -> {
                progressIndicator.visibility = View.GONE
                progressIndicator.progress = 0
                statusButton.text = CelestiaString("Uninstall", "")
            }
        }

        val objectName = item.objectName
        if (currentState == ResourceItemState.Installed && objectName != null && listener?.objectExistsWithName(objectName) == true) {
            goToButton.visibility = View.VISIBLE
            goToButton.setOnClickListener {
                listener?.onGoToObject(objectName)
            }
        } else {
            goToButton.visibility = View.GONE
        }
    }

    companion object {
        private const val ARG_ITEM = "item"
        private const val SHARE_BUTTON_ID = 142

        @JvmStatic
        fun newInstance(item: ResourceItem) =
            ResourceItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}