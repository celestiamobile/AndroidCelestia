package space.celestia.mobilecelestia.resource

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.rememberWebViewState
import space.celestia.mobilecelestia.help.HelpAction
import space.celestia.mobilecelestia.help.HelpScreen
import space.celestia.mobilecelestia.resource.viewmodel.WebViewModel

class CommonWebFragment: Fragment() {
    private lateinit var uri: Uri
    private lateinit var matchingQueryKeys: List<String>
    private var filterURL = true
    private var listener: CommonWebNavigationFragment.Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = requireArguments()
        uri = BundleCompat.getParcelable(bundle, ARG_URI, Uri::class.java)!!
        matchingQueryKeys = requireArguments().getStringArrayList(ARG_MATCHING_QUERY_KEYS) ?: listOf()
        filterURL = bundle.getBoolean(ARG_FILTER_URL, true)
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

    @Composable
    private fun MainScreen() {
        val scope = rememberCoroutineScope()
        val viewModel: WebViewModel = hiltViewModel()
        val webViewState = rememberWebViewState(url = uri.toString(), matchingQueryKeys = matchingQueryKeys, filterURL = filterURL)
        SingleWebScreen(webViewState = webViewState, fallback = {
            HelpScreen(helpActionSelected = {
                when (it) {
                    HelpAction.RunDemo -> {
                        scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.runDemo() }
                    }
                }
            }, helpURLSelected = {
                listener?.onExternalWebLinkClicked(it)
            }, paddingValues = WindowInsets.systemBars.asPaddingValues())
        }, openSubscriptionPageHandler = {
            listener?.onOpenSubscriptionPage()
        }, openExternalWebLink = {
            listener?.onExternalWebLinkClicked(it)
        }, shareURLHandler = { title, url ->
            listener?.onShareURL(title, url)
        }, receivedACKHandler = {
            listener?.onReceivedACK(it)
        }, paddingValues = WindowInsets.systemBars.asPaddingValues(), modifier = Modifier.fillMaxSize())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CommonWebNavigationFragment.Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CommonWebNavigationFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        private const val ARG_URI = "uri"
        private const val ARG_MATCHING_QUERY_KEYS = "query_keys"
        private const val ARG_FILTER_URL = "filter_url"

        @JvmStatic
        fun newInstance(uri: Uri, matchingQueryKeys: List<String>? = null, filterURL: Boolean = false): CommonWebFragment {
            val fragment = CommonWebFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_URI, uri)
                    if (matchingQueryKeys != null)
                        putStringArrayList(ARG_MATCHING_QUERY_KEYS, ArrayList(matchingQueryKeys))
                    putBoolean(ARG_FILTER_URL, filterURL)
                }
            }
            return fragment
        }
    }
}