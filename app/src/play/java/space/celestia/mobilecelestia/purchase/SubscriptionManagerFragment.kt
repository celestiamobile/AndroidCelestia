package space.celestia.mobilecelestia.purchase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestiaui.compose.Mdc3Theme

@AndroidEntryPoint
class SubscriptionManagerFragment: Fragment() {
    private var preferredPlayOfferId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferredPlayOfferId = arguments?.getString(PREFERRED_PLAY_OFFER_ID_ARG, null)
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
                    SubscriptionManagerScreen(preferredPlayOfferId)
                }
            }
        }
    }

    companion object {
        private const val PREFERRED_PLAY_OFFER_ID_ARG = "preferred-play-offer-id"

        fun newInstance(preferredPlayOfferId: String?) = SubscriptionManagerFragment().apply {
            arguments = Bundle().apply {
                putString(PREFERRED_PLAY_OFFER_ID_ARG, preferredPlayOfferId)
            }
        }
    }
}