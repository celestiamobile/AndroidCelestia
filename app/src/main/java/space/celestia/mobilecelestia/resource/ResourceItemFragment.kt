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
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.common.replace
import space.celestia.mobilecelestia.resource.model.ResourceItem
import space.celestia.mobilecelestia.resource.model.ResourceManager
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.showAlert
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ResourceItemFragment : NavigationFragment.SubFragment(), ResourceManager.Listener {
    @Inject
    lateinit var celestiaLanguage: String

    @Inject
    lateinit var resourceManager: ResourceManager

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

        resourceManager.addListener(this)
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
            val baseURL = "https://celestia.mobi/resources/item"
            val uri = Uri.parse(baseURL)
                .buildUpon()
                .appendQueryParameter("item", item.id)
                .appendQueryParameter("lang", celestiaLanguage)
                .appendQueryParameter("environment", "app")
                .appendQueryParameter("theme", "dark")
                .appendQueryParameter("titleVisibility", "visible")
                .build()
            replace(CommonWebFragment.newInstance(uri), R.id.resource_item_container)
        }
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
        resourceManager.removeListener(this)

        super.onDestroy()
    }

    private fun onProgressViewClick() {
        val activity = this.activity ?: return

        val dm = resourceManager

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
        val dm = resourceManager
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

        @JvmStatic
        fun newInstance(item: ResourceItem) =
            ResourceItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}