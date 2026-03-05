package space.celestia.mobilecelestia.help

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestiaui.compose.Mdc3Theme
import space.celestia.celestiaui.help.HelpAction
import space.celestia.celestiaui.help.NewHelpScreen

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
                    NewHelpScreen(linkClicked = {
                        listener?.helpLinkClicked(it)
                    }, actionSelected = {
                        listener?.helpActionSelected(it)
                    })
                }
            }
        }
    }

    companion object {
        fun newInstance() = NewHelpFragment()
    }
}
