package space.celestia.mobilecelestia.purchase

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
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
            val view = inflater.inflate(R.layout.layout_empty_hint, container, false)
            val hint = view.findViewById<TextView>(R.id.hint)
            hint.text = CelestiaString("This feature is not supported.", "")
            view
        } else if (purchaseManager.purchaseToken() == null) {
            val view = inflater.inflate(R.layout.layout_empty_hint, container, false)
            val hint = view.findViewById<TextView>(R.id.hint)
            val button = view.findViewById<Button>(R.id.action_button)
            button.visibility = View.VISIBLE
            button.text = CelestiaString("Get Celestia PLUS", "")
            hint.text = CelestiaString("This feature is only available to Celestia PLUS users.", "")
            val weakSelf = WeakReference(this)
            button.setOnClickListener {
                weakSelf.get()?.listener?.requestOpenSubscriptionManagement()
            }
            view
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