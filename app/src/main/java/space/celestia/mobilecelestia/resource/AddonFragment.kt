package space.celestia.mobilecelestia.resource

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiaui.compose.Mdc3Theme
import space.celestia.celestiaui.resource.SingleAddonScreen
import java.io.File

class AddonFragment: Fragment() {
    private lateinit var item: ResourceItem

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
            throw RuntimeException("$context must implement AddonFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        item = if (savedInstanceState != null) {
            BundleCompat.getSerializable(savedInstanceState, ARG_ITEM, ResourceItem::class.java)!!
        } else {
            BundleCompat.getSerializable(requireArguments(), ARG_ITEM, ResourceItem::class.java)!!
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(ARG_ITEM, item)
        super.onSaveInstanceState(outState)
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
                    SingleAddonScreen(item = item, requestRunScript = { script ->
                        listener?.webRequestRunScript(script)
                    }, requestShareAddon = { name, id ->
                        listener?.webRequestShareAddon(name, id)
                    })
                }
            }
        }
    }

    companion object {
        private const val ARG_ITEM = "item"

        @JvmStatic
        fun newInstance(item: ResourceItem) =
            AddonFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}
