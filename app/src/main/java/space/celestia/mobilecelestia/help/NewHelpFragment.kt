package space.celestia.mobilecelestia.help

import android.content.Context
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
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.utils.URLHelper
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.rememberWebViewState
import space.celestia.mobilecelestia.resource.CommonWebNavigationFragment
import space.celestia.mobilecelestia.resource.SingleWebScreen
import space.celestia.mobilecelestia.resource.viewmodel.WebViewModel

class NewHelpFragment: Fragment() {
    private var listener: CommonWebNavigationFragment.Listener? = null

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

    @Composable
    private fun MainScreen() {
        val scope = rememberCoroutineScope()
        val viewModel: WebViewModel = hiltViewModel()
        val webViewState = rememberWebViewState(url = URLHelper.buildInAppGuideShortURI("/help/welcome", AppCore.getLanguage(), false).toString(), matchingQueryKeys = listOf("guide"), filterURL = true)
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

    companion object {
        fun newInstance() = NewHelpFragment()
    }
}