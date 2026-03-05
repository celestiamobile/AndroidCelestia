package space.celestia.celestiaxr.tool

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.core.os.BundleCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.compose.Mdc3Theme
import space.celestia.celestiaui.favorite.FavoriteBookmarkItem
import space.celestia.celestiaui.info.model.perform
import space.celestia.celestiaui.resource.CommonWebFragment
import space.celestia.celestiaui.utils.AppURL
import space.celestia.celestiaui.utils.AppURLResult
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.showAlert
import java.io.File
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class ToolActivity : AppCompatActivity(), CommonWebFragment.Listener {
    @Inject
    lateinit var executor: Executor

    @Inject
    lateinit var appCore: AppCore

    companion object {
        const val EXTRA_TOOL = "extra_tool"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tool = intent.extras?.let { BundleCompat.getParcelable(it, EXTRA_TOOL, Tool.Page::class.java) }
        if (tool == null) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            Mdc3Theme {
                val scope = rememberCoroutineScope()
                ToolScreen(
                    page = tool,
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
                    requestRunFavoriteScript = { scriptPath ->
                        scope.launch(executor.asCoroutineDispatcher()) {
                            appCore.runScript(scriptPath)
                        }
                    },
                    requestOpenCelestiaURL = { url ->
                        scope.launch(executor.asCoroutineDispatcher()) {
                            appCore.goToURL(url)
                        }
                    },
                    requestShareFavorite = { favorite ->
                        if (favorite is FavoriteBookmarkItem && favorite.isLeaf) {
                            shareURLDirect(favorite.bookmark.name, favorite.bookmark.url)
                        }
                    }
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
        // TODO: impl
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
                    val intent = Intent(this, ToolActivity::class.java)
                    intent.putExtra(EXTRA_TOOL, Tool.Page.ObjectInfo(selection))
                    startActivity(intent)
                }
            }
            is AppURL.Addon -> {
                val intent = Intent(this, ToolActivity::class.java)
                intent.putExtra(EXTRA_TOOL, Tool.Page.Addon(url.id))
                startActivity(intent)
            }
            is AppURL.Article -> {
                val intent = Intent(this, ToolActivity::class.java)
                intent.putExtra(EXTRA_TOOL, Tool.Page.Article(url.id))
                startActivity(intent)
            }
        }
    }
}
