package space.celestia.mobilecelestia.settings

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.core.CelestiaAppCore

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
            ImageViewCompat.setImageTintList(holder.accessory, ColorStateList.valueOf(
                ResourcesCompat.getColor(parent.resources, R.color.colorThemeLabel, null)))
            return holder
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        val core = CelestiaAppCore.shared()
        val current = core.getIntValueForField(this.item.key)
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
