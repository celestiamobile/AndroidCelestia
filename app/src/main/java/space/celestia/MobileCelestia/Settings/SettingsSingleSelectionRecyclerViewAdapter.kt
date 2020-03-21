package space.celestia.MobileCelestia.Settings

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView

import space.celestia.MobileCelestia.Common.CommonSectionV2
import space.celestia.MobileCelestia.Common.CommonTextViewHolder
import space.celestia.MobileCelestia.Common.RecyclerViewItem
import space.celestia.MobileCelestia.Common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.MobileCelestia.Core.CelestiaAppCore
import space.celestia.MobileCelestia.R

fun SettingsSingleSelectionItem.createSections(): List<CommonSectionV2> {
    val sections = ArrayList<CommonSectionV2>()
    sections.add(CommonSectionV2(selections))
    return sections
}

class SettingsSingleSelectionRecyclerViewAdapter(
    val item: SettingsSingleSelectionItem,
    private val listener: SettingsSingleSelectionFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(item.createSections()) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is SettingsSingleSelectionItem.Selection) {
            val core = CelestiaAppCore.shared()
            listener?.onSingleSelectionSettingItemChange(this.item.key, item.value)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is SettingsSingleSelectionItem.Selection)
            return SETTING_ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SETTING_ITEM) {
            val holder = CommonTextViewHolder(parent)
            holder.accessory.setImageResource(R.drawable.ic_check)
            return holder
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        val core = CelestiaAppCore.shared()
        val current = core.getIntValueForPield(this.item.key)
        if (item is SettingsSingleSelectionItem.Selection && holder is CommonTextViewHolder) {
            holder.title.text = item.name
            holder.accessory.visibility = if (current == item.value) View.VISIBLE else View.INVISIBLE
            return
        }
        super.bindVH(holder, item)
    }

    fun reload() {
        updateSectionsWithHeader(item.createSections())
    }

    private companion object {
        const val SETTING_ITEM = 0
    }
}
