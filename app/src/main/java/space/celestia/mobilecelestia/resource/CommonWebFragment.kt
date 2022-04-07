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
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.progressindicator.CircularProgressIndicator
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.File
import java.lang.ref.WeakReference

class CommonWebFragment: NavigationFragment.SubFragment(), CelestiaJavascriptInterface.MessageHandler {
    private lateinit var uri: Uri
    private lateinit var matchingQueryKeys: List<String>
    private var contextDirectory: File? = null

    private var listener: Listener? = null

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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            val view = inflater.inflate(R.layout.fragment_common_web, container, false)
            configureWebView(view)
            view
        } catch (ignored: Throwable) {
            val view = inflater.inflate(R.layout.layout_empty_hint, container, false)
            val hint = view.findViewById<TextView>(R.id.hint)
            hint.text = CelestiaString("WebView is not available.", "")
            view
        }
    }

    private fun configureWebView(view: View) {
        val loadingIndicator = view.findViewById<CircularProgressIndicator>(R.id.loading_indicator)
        val webView = view.findViewById<WebView>(R.id.webview)
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.isHorizontalScrollBarEnabled = false

        val webSettings = webView.settings
        @SuppressLint("SetJavaScriptEnabled")
        webSettings.javaScriptEnabled = true

        val weakSelf = WeakReference(this)

        val defaultUri = uri
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
                loadingIndicator.isVisible = false
            }
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webSettings, WebSettingsCompat.FORCE_DARK_ON)
        }

        webView.loadUrl(uri.toString())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CommonWebFragment.Listener")
        }
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

    companion object {
        private const val ARG_URI = "uri"
        private const val ARG_MATCHING_QUERY_KEYS = "query_keys"
        private const val ARG_CONTEXT_DIRECTORY = "context_directory"

        fun newInstance(uri: Uri, matchingQueryKeys: List<String>, contextDirectory: File? = null) = CommonWebFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_URI, uri)
                putSerializable(ARG_CONTEXT_DIRECTORY, contextDirectory)
                putStringArrayList(ARG_MATCHING_QUERY_KEYS, ArrayList(matchingQueryKeys))
            }
        }
    }
}