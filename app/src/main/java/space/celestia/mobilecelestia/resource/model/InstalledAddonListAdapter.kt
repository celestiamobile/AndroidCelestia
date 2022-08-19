package space.celestia.mobilecelestia.resource.model

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import java.lang.ref.WeakReference

class InstalledAddonListAdapter(val listener: WeakReference<Listener>): SeparatorHeaderRecyclerViewAdapter() {
    interface Listener {
        fun onItemSelected(item: ResourceItem)
    }

    internal class Item(val item: ResourceItem): RecyclerViewItem

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is Item) {
            listener.get()?.onItemSelected(item.item)
            return
        }
        super.onItemSelected(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM) {
            val holder = CommonTextViewHolder(parent)
            holder.detail.visibility = View.GONE
            holder.accessory.visibility = View.VISIBLE
            return holder
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is Item && holder is CommonTextViewHolder) {
            holder.title.text = item.item.name
            return
        }
        super.bindVH(holder, item)
    }

    fun update(items: List<ResourceItem>) {
        updateSectionsWithHeader(listOf(CommonSectionV2(items.map { Item(it) })))
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is Item)
            return ITEM
        return super.itemViewType(item)
    }

    private companion object {
        const val ITEM = 0
    }
}