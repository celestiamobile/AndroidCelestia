package space.celestia.mobilecelestia.resource

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import space.celestia.mobilecelestia.compose.Mdc3Theme

class SimpleWebFragment: Fragment() {
    private lateinit var uri: Uri
    private lateinit var matchingQueryKeys: List<String>
    private var filterURL = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = BundleCompat.getParcelable(requireArguments(), ARG_URI, Uri::class.java)!!
        matchingQueryKeys = requireArguments().getStringArrayList(ARG_MATCHING_QUERY_KEYS) ?: listOf()
        filterURL = requireArguments().getBoolean(ARG_FILTER_URL, true)
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
                    WebPage(uri = uri, filterURL = filterURL, matchingQueryKeys = matchingQueryKeys, paddingValues = WindowInsets.systemBars.asPaddingValues())
                }
            }
        }
    }

    companion object {
        const val ARG_URI = "uri"
        const val ARG_MATCHING_QUERY_KEYS = "query_keys"
        const val ARG_FILTER_URL = "filter_url"

        @JvmStatic
        fun newInstance(uri: Uri, matchingQueryKeys: List<String>, filterURL: Boolean) = SimpleWebFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_URI, uri)
                putStringArrayList(ARG_MATCHING_QUERY_KEYS, ArrayList(matchingQueryKeys))
                putBoolean(ARG_FILTER_URL, filterURL)
            }
        }
    }
}