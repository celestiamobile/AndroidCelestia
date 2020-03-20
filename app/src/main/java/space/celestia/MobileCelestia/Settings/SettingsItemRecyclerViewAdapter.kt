package space.celestia.MobileCelestia.Settings

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import space.celestia.MobileCelestia.Common.CommonTextViewHolder
import space.celestia.MobileCelestia.Common.RecyclerViewItem
import space.celestia.MobileCelestia.Settings.SettingsItemFragment.Listener
import space.celestia.MobileCelestia.Common.SeparatorHeaderRecyclerViewAdapter

class SettingsItemRecyclerViewAdapter(
    private val listener: Listener?
) : SeparatorHeaderRecyclerViewAdapter(mainSettingSections) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is SettingsItem)
            listener?.onMainSettingItemSelected(item)
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is SettingsItem)
            return SETTING_ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SETTING_ITEM)
            return CommonTextViewHolder(parent)
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is SettingsItem && holder is CommonTextViewHolder) {
            holder.title.text = item.name
            holder.accessory.visibility = View.VISIBLE
            return
        }
        super.bindVH(holder, item)
    }

    private companion object {
        const val SETTING_ITEM = 0
    }
}
