package space.celestia.mobilecelestia.resource

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.mobilecelestia.compose.Mdc3Theme

class ResourceItemNavigationFragment: Fragment() {
    private lateinit var item: ResourceItem
    private var listener: ResourceFragment.Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        item = BundleCompat.getSerializable(requireArguments(), ARG_ITEM, ResourceItem::class.java)!!
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
        if (context is ResourceFragment.Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ResourceFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(topBar = {
            TopAppBar(title = {
                Text(text = item.name)
            }, actions = {
                IconButton(onClick = {
                    listener?.onShareAddon(item.name, item.id)
                }) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = "")
                }
            }, scrollBehavior = scrollBehavior)
        }) { paddingValues ->
            AddonScreen(
                addon = item,
                shareURLHandler = { title, url ->
                    listener?.onShareURL(title, url)
                },
                receivedACKHandler = {
                    listener?.onReceivedACK(it)
                },
                openSubscriptionPageHandler = {
                    listener?.onOpenSubscriptionPage()
                },
                openExternalWebLink = {
                    listener?.onExternalWebLinkClicked(it)
                },
                paddingValues = paddingValues,
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            )
        }
    }

    companion object {
        private const val ARG_ITEM = "item"

        @JvmStatic
        fun newInstance(item: ResourceItem) =
            ResourceItemNavigationFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}