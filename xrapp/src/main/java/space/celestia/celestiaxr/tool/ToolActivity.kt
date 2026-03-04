package space.celestia.celestiaxr.tool

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.compose.Mdc3Theme
import space.celestia.celestiaui.favorite.FavoriteBookmarkItem
import space.celestia.celestiaui.info.model.perform
import space.celestia.celestiaui.resource.CommonWebFragment
import space.celestia.celestiaui.resource.model.ResourceAPIService
import space.celestia.celestiaui.tool.ToolScreen
import space.celestia.celestiaui.utils.AppURL
import space.celestia.celestiaui.utils.AppURLResult
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.showAlert
import space.celestia.celestiaxr.tool.viewmodel.ToolViewModel
import java.io.File
import java.util.UUID
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class ToolActivity : AppCompatActivity(), CommonWebFragment.Listener {
    @Inject
    lateinit var executor: Executor

    @Inject
    lateinit var appCore: AppCore

    @Inject
    lateinit var resourceAPI: ResourceAPIService

    private val viewModel: ToolViewModel by viewModels()

    companion object {
        const val EXTRA_TOOL = "extra_tool"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mdc3Theme {
                val scope = rememberCoroutineScope()
                ToolScreen(
                    backStack = viewModel.backStack,
                    linkClicked = { link, localized ->
                        openLink(link, localized)
                    },
                    requestRunScript = { script ->
                        scope.launch(executor.asCoroutineDispatcher()) {
                            appCore.runScript(script.absolutePath)
                        }
                    },
                    requestShareAddon = { name, id ->
                        shareAddon(name, id)
                    },
                    openScriptRequested = { file ->
                        scope.launch(executor.asCoroutineDispatcher()) {
                            appCore.runScript(file.script.filename)
                        }
                    },
                    openBookmarkRequested = { bookmark ->
                        scope.launch(executor.asCoroutineDispatcher()) {
                            appCore.goToURL(bookmark.bookmark.url)
                        }
                    },
                    shareRequested = { favorite ->
                        if (favorite is FavoriteBookmarkItem && favorite.isLeaf) {
                            shareURLDirect(favorite.bookmark.name, favorite.bookmark.url)
                        }
                    },
                    providePreferredDisplay = { null },
                    refreshRateChanged = { _  -> }
                )
            }
        }
    }

    override fun onExternalWebLinkClicked(url: String) {
        openURL(url)
    }

    override fun onRunScript(
        type: String,
        content: String,
        name: String?,
        location: String?,
        contextDirectory: File?
    ) {
        if (!listOf("cel", "celx").contains(type)) return
        val supportedScriptLocations = listOf("temp", "context")
        if (location != null && !supportedScriptLocations.contains(location)) return
        if (location == "context" && contextDirectory == null) return

        val scriptFile: File
        val scriptFileName = "${name ?: UUID.randomUUID()}.${type}"
        scriptFile = if (location == "context") {
            File(contextDirectory, scriptFileName)
        } else {
            File(cacheDir, scriptFileName)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                scriptFile.writeText(content)
                withContext(Dispatchers.Main) {
                    openAppURL(AppURL.Script(scriptFile.absolutePath))
                }
            } catch (_: Throwable) {}
        }
    }

    override fun onShareURL(title: String, url: String) {
        shareURLDirect(title, url)
    }

    override fun onReceivedACK(id: String) {
        // TODO: impl
    }

    override fun onRunDemo() {
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            appCore.runDemo()
        }
    }

    override fun onOpenSubscriptionPage(preferredPlayOfferId: String?) {}

    private fun shareAddon(name: String, id: String) {
        val baseURL = "https://celestia.mobi/resources/item"
        val uri = baseURL.toUri().buildUpon().appendQueryParameter("item", id).appendQueryParameter("lang", AppCore.getLanguage()).build()
        shareURLDirect(name, uri.toString())
    }

    private fun shareURLDirect(title: String, url: String) {
        var intent = ShareCompat.IntentBuilder(this)
            .setType("text/plain")
            .setText(url)
            .intent
        intent.putExtra(Intent.EXTRA_TITLE, title)
        intent = Intent.createChooser(intent, null)
        val ai = intent.resolveActivityInfo(packageManager, PackageManager.MATCH_DEFAULT_ONLY)
        if (ai != null && ai.exported)
            startActivity(intent)
        else
            showUnsupportedAction()
    }

    private fun showUnsupportedAction() {
        showAlert(CelestiaString("Unsupported action.", ""))
    }

    private fun openLink(link: String, localizable: Boolean) {
        var uri = link.toUri()
        if (localizable)
            uri = uri.buildUpon().appendQueryParameter("lang", AppCore.getLanguage()).build()
        openURI(uri)
    }

    private fun openURL(url: String) {
        openURI(url.toUri())
    }

    private fun openURI(uri: Uri) = lifecycleScope.launch {
        when (val urlResult = AppURL.fromUri(uri, this@ToolActivity)) {
            is AppURLResult.Success -> {
                val url = urlResult.url
                openAppURL(url)
            }
            is AppURLResult.Failure -> {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                val ai = intent.resolveActivityInfo(packageManager, PackageManager.MATCH_DEFAULT_ONLY)
                if (ai != null && ai.exported)
                    startActivity(intent)
                else
                    showUnsupportedAction()
            }
        }
    }

    private suspend fun openAppURL(url: AppURL) {
        when (url) {
            is AppURL.Script -> {
                withContext(executor.asCoroutineDispatcher()) {
                    appCore.runScript(url.path)
                }
            }
            is AppURL.CelURL -> {
                withContext(executor.asCoroutineDispatcher()) {
                    appCore.goToURL(url.url)
                }
            }
            is AppURL.Object -> {
                val selection = withContext(executor.asCoroutineDispatcher()) { appCore.simulation.findObject(url.path) }
                if (selection.isEmpty) return
                val action = url.action
                if (action != null) {
                    when (action) {
                        AppURL.Object.Action.Select -> {
                            appCore.simulation.selection = selection
                        }
                        is AppURL.Object.Action.SelectAnd -> {
                            appCore.simulation.selection = selection
                            appCore.perform(action.action)
                        }
                    }
                } else {
                    viewModel.backStack.add(Tool.Page.ObjectInfo(selection).page)
                }
            }
            is AppURL.Addon -> {
                try {
                    val item = resourceAPI.item(lang = AppCore.getLanguage(), item = url.id)
                    viewModel.backStack.add(Tool.Page.Addon(item).page)
                } catch (_: Throwable) {}
            }
            is AppURL.Article -> {
                viewModel.backStack.add(Tool.Page.Article(url.id).page)
            }
        }
    }
}
