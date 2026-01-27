package space.celestia.mobilecelestia.help

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.utils.URLHelper
import space.celestia.mobilecelestia.BuildConfig
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.resource.WebPage

@AndroidEntryPoint
class NewHelpFragment: Fragment() {
    interface Listener {
        fun helpActionSelected(action: HelpAction)
        fun helpLinkClicked(link: String)
    }

    private var listener: Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement NewHelpFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
                    WebPage(
                        uri = URLHelper.buildInAppGuideShortURI("/help/welcome", AppCore.getLanguage(), flavor = BuildConfig.FLAVOR, shareable = false),
                        filterURL = true,
                        matchingQueryKeys = listOf("guide"),
                        paddingValues = WindowInsets.systemBars.asPaddingValues(),
                        fallbackContent = { modifier, paddingValues ->
                            HelpScreen(modifier = modifier, paddingValues = paddingValues, linkClicked = { link ->
                                listener?.helpLinkClicked(link)
                            }, actionSelected = { action ->
                                listener?.helpActionSelected(action)
                            })
                        }
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance() = NewHelpFragment()
    }
}