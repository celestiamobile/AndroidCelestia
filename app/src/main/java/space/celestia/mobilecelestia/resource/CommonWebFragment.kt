package space.celestia.mobilecelestia.resource

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.resource.model.ResourceItem
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.commonHandler
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CommonWebFragment: NavigationFragment.SubFragment(), CelestiaJavascriptInterface.MessageHandler {
    private lateinit var uri: Uri
    private lateinit var matchingQueryKeys: List<String>
    private var filterURL = true
    private var contextDirectory: File? = null

    private var listener: Listener? = null
    private var webView: WebView? = null
    private var webViewState: Bundle? = null
    private var initialLoadFinished = false

    @Inject
    lateinit var resourceAPI: ResourceAPIService

    interface Listener {
        fun onExternalWebLinkClicked(url: String)
        fun onRunScript(type: String, content: String, name: String?, location: String?, contextDirectory: File?)
        fun onShareURL(title: String, url: String)
        fun onReceivedACK(id: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = requireArguments().getParcelable(ARG_URI)!!
        matchingQueryKeys = requireArguments().getStringArrayList(ARG_MATCHING_QUERY_KEYS) ?: listOf()
        contextDirectory = requireArguments().getSerializable(ARG_CONTEXT_DIRECTORY) as? File
        filterURL = requireArguments().getBoolean(ARG_FILTER_URL, true)
        if (savedInstanceState != null) {
            initialLoadFinished = savedInstanceState.getBoolean(ARG_INITIAL_LOAD_FINISHED, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            val view = inflater.inflate(R.layout.fragment_common_web, container, false)
            configureWebView(view, savedInstanceState)
            view
        } catch (ignored: Throwable) {
            val view = inflater.inflate(R.layout.layout_empty_hint, container, false)
            val hint = view.findViewById<TextView>(R.id.hint)
            hint.text = CelestiaString("WebView is not available.", "")
            view
        }
    }

    private fun configureWebView(view: View, savedInstanceState: Bundle?) {
        val loadingIndicator = view.findViewById<CircularProgressIndicator>(R.id.loading_indicator)
        val forwardButton = view.findViewById<Button>(R.id.forward_button)
        val backwardButton = view.findViewById<Button>(R.id.backward_button)
        val navigationContainer = view.findViewById<LinearLayout>(R.id.webview_navigation_container)

        val webView = view.findViewById<WebView>(R.id.webview)
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.isHorizontalScrollBarEnabled = false

        val webSettings = webView.settings
        @SuppressLint("SetJavaScriptEnabled")
        webSettings.javaScriptEnabled = true

        val weakSelf = WeakReference(this)

        val defaultUri = uri
        val shouldFilterURL = filterURL
        val queryKeys = matchingQueryKeys
        webView.addJavascriptInterface(CelestiaJavascriptInterface(this), "AndroidCelestia")
        webView.webViewClient = object: WebViewClient() {
            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return shouldOverrideUrl(request?.url.toString())
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return shouldOverrideUrl(url)
            }

            fun isURLAllowed(uri: Uri): Boolean {
                if (!shouldFilterURL) {
                    return true
                }
                if (uri.host != defaultUri.host || uri.path != defaultUri.path) {
                    return false
                }
                for (key in queryKeys) {
                    if (uri.getQueryParameter(key) != defaultUri.getQueryParameter(key)) {
                        return false
                    }
                }
                return true
            }

            fun shouldOverrideUrl(url: String?): Boolean {
                if (url == null) return true
                val uri = Uri.parse(url)
                if (isURLAllowed(uri))
                    return false
                weakSelf.get()?.listener?.onExternalWebLinkClicked(url)
                return true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                val wv = view ?: return
                weakSelf.get()?.initialLoadFinished = true
                loadingIndicator.isVisible = false
                navigationContainer.visibility = if (wv.canGoBack() || wv.canGoForward()) View.VISIBLE else View.GONE
                forwardButton.isEnabled = wv.canGoForward()
                backwardButton.isEnabled = wv.canGoBack()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                val self = weakSelf.get() ?: return
                if ((self.parentFragment as? NavigationFragment)?.top == self) {
                    self.title = view?.title ?: ""
                }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                val self = weakSelf.get() ?: return
                if ((self.parentFragment as? NavigationFragment)?.top == self) {
                    self.title = title ?: ""
                }
            }
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webSettings, WebSettingsCompat.FORCE_DARK_ON)
        }

        this.webView = webView

        forwardButton.setOnClickListener {
            weakSelf.get()?.webView?.goForward()
        }

        backwardButton.setOnClickListener {
            weakSelf.get()?.webView?.goBack()
        }

        val savedState = webViewState ?: savedInstanceState
        if (savedState != null) {
            webView.restoreState(savedState)
            if (initialLoadFinished) {
                loadingIndicator.isVisible = false
                navigationContainer.visibility = if (webView.canGoBack() || webView.canGoForward()) View.VISIBLE else View.GONE
                forwardButton.isEnabled = webView.canGoForward()
                backwardButton.isEnabled = webView.canGoBack()
            }
        } else {
            webView.loadUrl(uri.toString())
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CommonWebFragment.Listener")
        }
    }

    override fun onDestroyView() {
        val webView = this.webView
        if (webView != null) {
            val bundle = Bundle().apply {
                webView.saveState(this)
            }
            webViewState = bundle
        }
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView?.saveState(outState)
        outState.putBoolean(ARG_INITIAL_LOAD_FINISHED, initialLoadFinished)
        super.onSaveInstanceState(outState)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun runScript(type: String, content: String, scriptName: String?, scriptLocation: String?) {
        listener?.onRunScript(type, content, scriptName, scriptLocation, contextDirectory)
    }

    override fun shareURL(title: String, url: String) {
        listener?.onShareURL(title, url)
    }

    override fun receivedACK(id: String) {
        listener?.onReceivedACK(id)
    }

    override fun openAddonNext(id: String) {
        val lang = AppCore.getLanguage()
        lifecycleScope.launch {
            try {
                val result = resourceAPI.item(lang, id).commonHandler(ResourceItem::class.java, ResourceAPI.gson)
                val frag = parentFragment
                if (frag is NavigationFragment) {
                    frag.pushFragment(ResourceItemFragment.newInstance(result, lang, Date()))
                }
            } catch (ignored: Throwable) {}
        }
    }

    fun canGoBack(): Boolean {
        return webView?.canGoBack() ?: false
    }

    fun goBack() {
        webView?.goBack()
    }

    companion object {
        private const val ARG_URI = "uri"
        private const val ARG_MATCHING_QUERY_KEYS = "query_keys"
        private const val ARG_CONTEXT_DIRECTORY = "context_directory"
        private const val ARG_FILTER_URL = "filter_url"
        private const val ARG_INITIAL_LOAD_FINISHED = "initial_load_finished"

        fun newInstance(uri: Uri, matchingQueryKeys: List<String>, contextDirectory: File? = null) = CommonWebFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_URI, uri)
                putSerializable(ARG_CONTEXT_DIRECTORY, contextDirectory)
                putStringArrayList(ARG_MATCHING_QUERY_KEYS, ArrayList(matchingQueryKeys))
                putBoolean(ARG_FILTER_URL, true)
            }
        }

        fun newInstance(uri: Uri) = CommonWebFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_URI, uri)
                putBoolean(ARG_FILTER_URL, false)
            }
        }
    }
}