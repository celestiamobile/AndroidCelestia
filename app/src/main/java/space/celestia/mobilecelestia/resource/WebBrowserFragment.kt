package space.celestia.mobilecelestia.resource

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import space.celestia.celestiaui.compose.Mdc3Theme
import space.celestia.celestiaui.resource.WebScreen
import java.io.File

class WebBrowserFragment: Fragment() {
    private lateinit var uri: Uri

    interface Listener {
        fun webRequestRunScript(script: File)
        fun webRequestShareAddon(name: String, id: String)
    }

    private var listener: Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement WebBrowserFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

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
                    WebScreen(uri, requestRunScript = { script ->
                        listener?.webRequestRunScript(script)
                    }, requestShareAddon = { name, id ->
                        listener?.webRequestShareAddon(name, id)
                    })
                }
            }
        }
    }

    companion object {
        private const val ARG_URI = "uri"

        fun newInstance(uri: Uri) = WebBrowserFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_URI, uri)
            }
        }
    }
}
