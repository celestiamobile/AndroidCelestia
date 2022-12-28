package space.celestia.mobilecelestia.resource

import android.os.Bundle
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.resource.model.ResourceItem
import space.celestia.mobilecelestia.utils.getSerializableValue
import java.lang.ref.WeakReference
import java.util.*

class ResourceItemNavigationFragment: NavigationFragment() {
    private lateinit var item: ResourceItem
    private lateinit var language: String
    private lateinit var lastUpdateDate: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            item = savedInstanceState.getSerializableValue(ARG_ITEM, ResourceItem::class.java)!!
            lastUpdateDate = savedInstanceState.getSerializableValue(ARG_UPDATED_DATE, Date::class.java)!!
        } else {
            item = requireArguments().getSerializableValue(ARG_ITEM, ResourceItem::class.java)!!
            lastUpdateDate = requireArguments().getSerializableValue(ARG_UPDATED_DATE, Date::class.java)!!
        }
        language = requireArguments().getString(ARG_LANG, "en")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(ARG_ITEM, item)
        outState.putSerializable(ARG_UPDATED_DATE, lastUpdateDate)
        super.onSaveInstanceState(outState)
    }

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        val fragment = ResourceItemFragment.newInstance(item, language, lastUpdateDate)
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
        private const val ARG_LANG = "lang"
        private const val ARG_UPDATED_DATE = "date"

        @JvmStatic
        fun newInstance(item: ResourceItem, language: String, lastUpdateDate: Date) =
            ResourceItemNavigationFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                    putString(ARG_LANG, language)
                    putSerializable(ARG_UPDATED_DATE, lastUpdateDate)
                }
            }
    }
}