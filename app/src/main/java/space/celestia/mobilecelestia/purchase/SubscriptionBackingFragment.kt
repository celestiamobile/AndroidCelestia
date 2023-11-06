package space.celestia.mobilecelestia.purchase

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.utils.CelestiaString
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
abstract class SubscriptionBackingFragment : NavigationFragment.SubFragment() {
    @Inject
    lateinit var purchaseManager: PurchaseManager

    private var listener: Listener? = null

    interface  Listener {
        fun requestOpenSubscriptionManagement()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (!purchaseManager.canUseInAppPurchase()) {
            ComposeView(requireContext()).apply {
                // Dispose of the Composition when the view's LifecycleOwner
                // is destroyed
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    Mdc3Theme {
                        EmptyHint(text = CelestiaString("This feature is not supported.", ""))
                    }
                }
            }
        } else if (purchaseManager.purchaseToken() == null) {
            val weakSelf = WeakReference(this)
            ComposeView(requireContext()).apply {
                // Dispose of the Composition when the view's LifecycleOwner
                // is destroyed
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    Mdc3Theme {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyHint(text = CelestiaString("This feature is only available to Celestia PLUS users.", ""), actionText = CelestiaString("Get Celestia PLUS", "")) {
                                weakSelf.get()?.listener?.requestOpenSubscriptionManagement()
                            }
                        }
                    }
                }
            }
        } else {
            createView(inflater, container, savedInstanceState)
        }
    }

    abstract fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SubscriptionBackingFragment.Listener")
        }
    }
}