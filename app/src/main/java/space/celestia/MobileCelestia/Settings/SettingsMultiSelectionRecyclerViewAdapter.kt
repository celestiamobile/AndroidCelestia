package space.celestia.MobileCelestia.Settings

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_settings_multi_selection_master_item.view.*

import space.celestia.MobileCelestia.Common.CommonSectionV2
import space.celestia.MobileCelestia.Common.CommonTextViewHolder
import space.celestia.MobileCelestia.Common.RecyclerViewItem
import space.celestia.MobileCelestia.Common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.MobileCelestia.R
import java.util.zip.Inflater

fun SettingsMultiSelectionItem.createSections(): List<CommonSectionV2> {
    val sections = ArrayList<CommonSectionV2>()
    if (masterKey != null) {
        sections.add(CommonSectionV2(listOf(MasterSwitch(this))))
    }
    sections.add(CommonSectionV2(selections))
    return sections
}

class MasterSwitch(item: SettingsMultiSelectionItem) : RecyclerViewItem {
    val name = item.name
    val key = item.masterKey!!
}

class SettingsMultiSelectionRecyclerViewAdapter(
    item: SettingsMultiSelectionItem,
    private val listener: SettingsMultiSelectionFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(item.createSections()) {

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is SettingsMultiSelectionItem.Selection)
            return SETTING_ITEM
        if (item is MasterSwitch)
            return SETTING_MASTER
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SETTING_ITEM) {
            val holder = CommonTextViewHolder(parent)
            holder.accessory.setImageResource(R.drawable.ic_check)
            return holder
        }
        if (viewType == SETTING_MASTER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_settings_multi_selection_master_item, parent,false)
            return MasterViewHolder(view)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is SettingsMultiSelectionItem.Selection && holder is CommonTextViewHolder) {
            holder.title.text = item.name
            holder.accessory.visibility = View.VISIBLE
            return
        }
        if (item is MasterSwitch && holder is MasterViewHolder) {
            holder.title.text = item.name
            return
        }
        super.bindVH(holder, item)
    }

    inner class MasterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title
        val switch: Switch = view.accessory
    }

    private companion object {
        const val SETTING_ITEM = 0
        const val SETTING_MASTER = 1
    }
}
