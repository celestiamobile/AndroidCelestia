package space.celestia.mobilecelestia.resource

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.rememberSaveableWebViewState
import space.celestia.mobilecelestia.compose.rememberWebViewNavigator

class CommonWebNavigationFragment: Fragment() {
    interface Listener {
        fun onExternalWebLinkClicked(url: String)
        fun onShareURL(title: String, url: String)
        fun onOpenSubscriptionPage()
        fun onReceivedACK(id: String)
        fun onShareAddon(name: String, id: String)
    }

    private lateinit var uri: Uri
    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = BundleCompat.getParcelable(requireArguments(), ARG_URI, Uri::class.java)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CommonWebNavigationFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @Composable
    private fun MainScreen() {
        val navigator = rememberWebViewNavigator()
        val webViewState = rememberSaveableWebViewState()
        LaunchedEffect(navigator) {
            val bundle = webViewState.viewState
            if (bundle == null) {
                // This is the first time load, so load the home page.
                navigator.loadUrl(uri.toString())
            }
        }

        WebNavigationScreen(
            webViewState = webViewState,
            navigator = navigator,
            openSubscriptionPageHandler = {
                listener?.onOpenSubscriptionPage()
            },
            openExternalWebLink = {
                listener?.onExternalWebLinkClicked(it)
            },
            shareURLHandler = { title, url ->
                listener?.onShareURL(title, url)
            },
            receivedACKHandler = {
                listener?.onReceivedACK(it)
            },
            shareAddon = { title, id ->
                listener?.onShareAddon(title, id)
            }
        )
    }

    companion object {
        private const val ARG_URI = "uri"

        fun newInstance(uri: Uri) = CommonWebNavigationFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_URI, uri)
            }
        }
    }
}