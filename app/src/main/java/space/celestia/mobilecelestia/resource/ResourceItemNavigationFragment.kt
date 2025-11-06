package space.celestia.mobilecelestia.resource

import android.os.Bundle
import androidx.core.os.BundleCompat
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.mobilecelestia.common.NavigationFragment
import java.lang.ref.WeakReference

class ResourceItemNavigationFragment: NavigationFragment() {
    private lateinit var item: ResourceItem

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

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        val fragment = ResourceItemFragment.newInstance(item)
        val weakSelf = WeakReference(this)
        fragment.updateListener = object : ResourceItemFragment.UpdateListener {
            override fun onResourceItemUpdated(resourceItem: ResourceItem) {
                val self = weakSelf.get() ?: return
                self.item = resourceItem
            }
        }
        return fragment
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