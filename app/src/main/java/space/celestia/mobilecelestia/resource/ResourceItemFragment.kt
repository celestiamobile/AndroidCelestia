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
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.common.replace
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.celestiafoundation.utils.URLHelper
import space.celestia.celestiafoundation.utils.commonHandler
import space.celestia.celestiafoundation.utils.getSerializableValue
import space.celestia.mobilecelestia.utils.*
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ResourceItemFragment : NavigationFragment.SubFragment(), ResourceManager.Listener {
    @Inject
    lateinit var resourceManager: ResourceManager
    @Inject
    lateinit var resourceAPI: ResourceAPIService

    private lateinit var language: String

    private lateinit var item: ResourceItem
    private lateinit var lastUpdateDate: Date
    private lateinit var statusButton: Button
    private lateinit var goToButton: Button
    private lateinit var progressIndicator: LinearProgressIndicator
    private var currentState: ResourceItemState = ResourceItemState.None

    private var listener: Listener? = null
    var updateListener: UpdateListener? = null

    interface Listener {
        fun objectExistsWithName(name: String): Boolean
        fun onGoToObject(name: String)
        fun onShareAddon(name: String, id: String)
        fun onRunScript(file: File)
    }

    interface UpdateListener {
        fun onResourceItemUpdated(resourceItem: ResourceItem, updateDate: Date)
    }

    enum class ResourceItemState {
        None, Downloading, Installed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            item = savedInstanceState.getSerializableValue(ARG_ITEM, ResourceItem::class.java)!!
            lastUpdateDate = savedInstanceState.getSerializableValue(ARG_UPDATED_DATE, Date::class.java)!!
        } else {
            item = requireArguments().getSerializableValue(ARG_ITEM, ResourceItem::class.java)!!
            lastUpdateDate = requireArguments().getSerializableValue(ARG_UPDATED_DATE, Date::class.java)!!
        }
        language = requireArguments().getString(ARG_LANG, "en")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resource_item, container, false)
        val contentContainer = view.findViewById<LinearLayout>(R.id.content_container)

        ViewCompat.setOnApplyWindowInsetsListener(contentContainer) { contentView, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            contentView.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.webview_container)) { _, _ ->
            // Consume the insets since we have content beneath it
            WindowInsetsCompat.CONSUMED
        }

        statusButton = view.findViewById(R.id.status_button)
        statusButton.setOnClickListener {
            onProgressViewClick()
        }
        goToButton = view.findViewById(R.id.go_to_button)
        goToButton.text = if (item.type == "script") CelestiaString("Run", "Run a script") else CelestiaString("Go", "Go to an object")
        goToButton.visibility = View.GONE
        progressIndicator = view.findViewById(R.id.progress_indicator)

        updateUI()

        resourceManager.addListener(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = item.name
        rightNavigationBarItems = listOf(NavigationFragment.BarButtonItem(SHARE_BAR_BUTTON_ID, null, R.drawable.ic_share))

        if (savedInstanceState == null) {
            val uri = URLHelper.buildInAppAddonURI(item.id, language)
            replace(CommonWebFragment.newInstance(uri, listOf("item"), resourceManager.contextDirectory(item)), R.id.webview_container)
        }

        if (Date().time - lastUpdateDate.time > UPDATE_INTERVAL_MILLISECONDS) {
            // Fetch the latest item, this is needed as user might come
            // here from Installed where the URL might be incorrect
            val weakSelf = WeakReference(this)
            val resourceAPI = this.resourceAPI
            val language = this.language
            val itemID = item.id
            lifecycleScope.launch {
                try {
                    val result = resourceAPI.item(language, itemID).commonHandler(ResourceItem::class.java, ResourceAPI.gson)
                    val self = weakSelf.get() ?: return@launch
                    self.item = result
                    self.lastUpdateDate = Date()
                    self.updateListener?.onResourceItemUpdated(self.item, self.lastUpdateDate)
                    self.title = self.item.name
                    self.updateUI()
                } catch (ignored: Throwable) {}
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(ARG_ITEM, item)
        outState.putSerializable(ARG_UPDATED_DATE, lastUpdateDate)
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

    override fun onDestroyView() {
        resourceManager.removeListener(this)

        super.onDestroyView()
    }

    override fun menuItemClicked(groupId: Int, id: Int): Boolean {
        when (id) {
            SHARE_BAR_BUTTON_ID -> {
                listener?.onShareAddon(item.name, item.id)
            } else -> {}
        }
        return true
    }

    private fun onProgressViewClick() {
        val activity = this.activity ?: return

        val dm = resourceManager

        // Already installed, offer an option for uninstalling
        if (dm.isInstalled(item)) {
            activity.showAlert(CelestiaString("Do you want to uninstall this add-on?", ""), handler = {
                var success = false
                try {
                    success = dm.uninstall(item)
                } catch (_: Exception) {}
                if (success) {
                    currentState = ResourceItemState.None
                } else {
                    activity.showAlert(CelestiaString("Unable to uninstall add-on.", ""))
                }
                updateUI()
            })
            return
        }

        // Already downloading, allow user to cancel
        if (dm.isDownloading(item.id)) {
            activity.showAlert(CelestiaString("Do you want to cancel this task?", "Prompt to ask to cancel downloading an add-on"), handler = {
                dm.cancel(item.id)
                currentState = ResourceItemState.None
                updateUI()
            })
            return
        }

        // Start downloading
        dm.download(item, File(activity.cacheDir, item.id))
        currentState = ResourceItemState.Downloading
        updateUI()
    }

    override fun onResourceFetchError(identifier: String, errorContext: ResourceManager.ErrorContext) {
        if (identifier != item.id) { return }

        currentState = ResourceItemState.None
        updateUI()
        val activity = this.activity ?: return

        val message = when (errorContext) {
            ResourceManager.ErrorContext.Cancelled -> null
            ResourceManager.ErrorContext.ZipError -> CelestiaString("Error unzipping add-on", "")
            ResourceManager.ErrorContext.Download -> CelestiaString("Error downloading add-on", "")
            is ResourceManager.ErrorContext.CreateDirectory -> CelestiaString("Error creating directory for add-on", "")
            is ResourceManager.ErrorContext.OpenFile -> CelestiaString("Error opening file for saving add-on", "")
            is ResourceManager.ErrorContext.WriteFile -> CelestiaString("Error writing data file for add-on", "")
        }
        if (message != null)
            activity.showAlert(title = CelestiaString("Failed to download or install this add-on.", ""), message = message)
    }

    override fun onFileDownloaded(identifier: String) {
        if (identifier != item.id) { return }

        updateUI()
    }

    override fun onFileUnzipped(identifier: String) {
        if (identifier != item.id) { return }

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
        if (dm.isInstalled(item))
            currentState = ResourceItemState.Installed
        if (dm.isDownloading(item.id))
            currentState = ResourceItemState.Downloading

        when (currentState) {
            ResourceItemState.None -> {
                progressIndicator.visibility = View.GONE
                progressIndicator.progress = 0
                statusButton.text = CelestiaString("Install", "Install an add-on")
            }
            ResourceItemState.Downloading -> {
                progressIndicator.visibility = View.VISIBLE
                statusButton.text = CelestiaString("Cancel", "")
            }
            ResourceItemState.Installed -> {
                progressIndicator.visibility = View.GONE
                progressIndicator.progress = 0
                statusButton.text = CelestiaString("Uninstall", "Uninstall an add-on")
            }
        }

        if (item.type == "script") {
            val mainScriptName = item.mainScriptName
            if (currentState == ResourceItemState.Installed && mainScriptName != null) {
                val scriptFile = File(resourceManager.contextDirectory(item), mainScriptName)
                if (scriptFile.exists()) {
                    goToButton.visibility = View.VISIBLE
                    goToButton.setOnClickListener {
                        listener?.onRunScript(scriptFile)
                    }
                } else {
                    goToButton.visibility = View.GONE
                }
            } else {
                goToButton.visibility = View.GONE
            }
        } else {
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
    }

    companion object {
        private const val ARG_ITEM = "item"
        private const val ARG_LANG = "lang"
        private const val ARG_UPDATED_DATE = "date"
        private const val UPDATE_INTERVAL_MILLISECONDS = 1800000L
        private const val SHARE_BAR_BUTTON_ID = 4214

        @JvmStatic
        fun newInstance(item: ResourceItem, language: String, lastUpdateDate: Date) =
            ResourceItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                    putString(ARG_LANG, language)
                    putSerializable(ARG_UPDATED_DATE, lastUpdateDate)
                }
            }
    }
}