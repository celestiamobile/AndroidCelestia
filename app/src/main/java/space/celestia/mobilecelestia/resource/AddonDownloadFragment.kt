package space.celestia.mobilecelestia.resource

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import space.celestia.celestiaui.compose.Mdc3Theme
import space.celestia.celestiaui.resource.AddonDownload
import java.io.File

class AddonDownloadFragment : Fragment() {
    private var isLeaf: Boolean? = null
    private var categoryId: String? = null

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
            throw RuntimeException("$context must implement AddonDownloadFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isLeaf = if (it.containsKey(ARG_IS_LEAF)) it.getBoolean(ARG_IS_LEAF) else null
            categoryId = it.getString(ARG_CATEGORY_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    AddonDownload(
                        isLeaf = isLeaf,
                        categoryId = categoryId,
                        requestRunScript = { script ->
                            listener?.webRequestRunScript(script)
                        },
                        requestShareAddon = { name, id ->
                            listener?.webRequestShareAddon(name, id)
                        }
                    )
                }
            }
        }
    }

    companion object {
        private const val ARG_IS_LEAF = "is_leaf"
        private const val ARG_CATEGORY_ID = "category_id"

        fun newInstance() = AddonDownloadFragment()

        fun newInstance(isLeaf: Boolean, categoryId: String) = AddonDownloadFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_IS_LEAF, isLeaf)
                putString(ARG_CATEGORY_ID, categoryId)
            }
        }
    }
}
