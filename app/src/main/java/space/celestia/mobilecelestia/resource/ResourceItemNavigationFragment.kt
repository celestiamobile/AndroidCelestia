package space.celestia.mobilecelestia.resource

import android.os.Bundle
import androidx.core.os.BundleCompat
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.mobilecelestia.common.NavigationFragment
import java.lang.ref.WeakReference
import java.util.Date

class ResourceItemNavigationFragment: NavigationFragment() {
    private lateinit var item: ResourceItem
    private lateinit var lastUpdateDate: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            item = BundleCompat.getSerializable(savedInstanceState, ARG_ITEM, ResourceItem::class.java)!!
            lastUpdateDate = BundleCompat.getSerializable(savedInstanceState, ARG_UPDATED_DATE, Date::class.java)!!
        } else {
            item = BundleCompat.getSerializable(requireArguments(), ARG_ITEM, ResourceItem::class.java)!!
            lastUpdateDate = BundleCompat.getSerializable(requireArguments(), ARG_UPDATED_DATE, Date::class.java)!!
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(ARG_ITEM, item)
        outState.putSerializable(ARG_UPDATED_DATE, lastUpdateDate)
        super.onSaveInstanceState(outState)
    }

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        val fragment = ResourceItemFragment.newInstance(item, lastUpdateDate)
        val weakSelf = WeakReference(this)
        fragment.updateListener = object : ResourceItemFragment.UpdateListener {
            override fun onResourceItemUpdated(resourceItem: ResourceItem, updateDate: Date) {
                val self = weakSelf.get() ?: return
                self.item = resourceItem
                self.lastUpdateDate = updateDate
            }
        }
        return fragment
    }

    companion object {
        private const val ARG_ITEM = "item"
        private const val ARG_UPDATED_DATE = "date"

        @JvmStatic
        fun newInstance(item: ResourceItem, lastUpdateDate: Date) =
            ResourceItemNavigationFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                    putSerializable(ARG_UPDATED_DATE, lastUpdateDate)
                }
            }
    }
}