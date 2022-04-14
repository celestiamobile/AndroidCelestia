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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
import javax.inject.Inject

@AndroidEntryPoint
class ResourceItemFragment : NavigationFragment.SubFragment(), ResourceManager.Listener {
    @Inject
    lateinit var resourceManager: ResourceManager
    @Inject
    lateinit var resourceAPI: ResourceAPIService

    private lateinit var language: String

    private lateinit var item: ResourceItem
    private var hasFetchedLatestData = false
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
        if (savedInstanceState != null) {
            item = savedInstanceState.getSerializable(ARG_ITEM) as ResourceItem
            hasFetchedLatestData = savedInstanceState.getBoolean(ARG_LATEST, false)
        } else {
            item = requireArguments().getSerializable(ARG_ITEM) as ResourceItem
            hasFetchedLatestData = false
        }
        language = requireArguments().getString(ARG_LANG, "en")
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
                .appendQueryParameter("lang", language)
                .appendQueryParameter("platform", "android")
                .appendQueryParameter("theme", "dark")
                .appendQueryParameter("titleVisibility", "visible")
                .build()
            replace(CommonWebFragment.newInstance(uri, listOf("item"), resourceManager.contextDirectory(item.id)), R.id.webview_container)
        }

        if (!hasFetchedLatestData) {
            // Fetch the latest item, this is needed as user might come
            // here from Installed where the URL might be incorrect
            lifecycleScope.launch {
                try {
                    val result = resourceAPI.item(language, item.id).commonHandler(ResourceItem::class.java, ResourceAPI.gson)
                    item = result
                    updateUI()
                } catch (ignored: Throwable) {}
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(ARG_ITEM, item)
        outState.putBoolean(ARG_LATEST, hasFetchedLatestData)
        super.onSaveInstanceState(outState)
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
                hasFetchedLatestData = true
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
        private const val ARG_LANG = "lang"
        private const val ARG_LATEST = "latest"

        @JvmStatic
        fun newInstance(item: ResourceItem, language: String) =
            ResourceItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                    putString(ARG_LANG, language)
                }
            }
    }
}