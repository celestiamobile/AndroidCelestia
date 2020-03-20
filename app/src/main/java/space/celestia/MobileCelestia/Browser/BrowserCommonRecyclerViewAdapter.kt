package space.celestia.MobileCelestia.Browser

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

import space.celestia.MobileCelestia.Browser.BrowserCommonFragment.Listener

import space.celestia.MobileCelestia.Common.CommonSectionV2
import space.celestia.MobileCelestia.Common.CommonTextViewHolder
import space.celestia.MobileCelestia.Common.RecyclerViewItem
import space.celestia.MobileCelestia.Common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.MobileCelestia.Core.CelestiaBrowserItem

fun CelestiaBrowserItem.createSection(): List<CommonSectionV2> {
    val list = ArrayList<CommonSectionV2>()

    if (`object` != null) {
        val section = CommonSectionV2(listOf( BrowserItem(this, true) ))
        list.add(section)
    }
    list.add(CommonSectionV2(children.map { BrowserItem(it, it.children.size == 0) }))
    return list
}

class BrowserCommonRecyclerViewAdapter(
    private val item: CelestiaBrowserItem,
    private val listener: Listener?
) : SeparatorHeaderRecyclerViewAdapter(item.createSection()) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is BrowserItem) {
            listener?.onBrowserItemSelected(item)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is BrowserItem) {
            return BROWSER_ITEM
        }
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == BROWSER_ITEM) {
            return CommonTextViewHolder(parent)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is CommonTextViewHolder && item is BrowserItem) {
            holder.title.text = item.item.name
            if (item.isLeaf) {
                holder.accessory.visibility = View.GONE
            } else {
                holder.accessory.visibility = View.VISIBLE
            }
            return
        }
        super.bindVH(holder, item)
    }

    companion object {
        const val BROWSER_ITEM  = 0
    }
}
