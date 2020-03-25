package space.celestia.mobilecelestia.settings

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.createDateFromJulianDay

enum class CurrentTimeAction {
    PickDate, SetToCurrentTime;
}

interface CurrentTimeItem : RecyclerViewItem {
    val action: CurrentTimeAction
    val title: String
}

class DatePickerItem : CurrentTimeItem {
    override val action: CurrentTimeAction
        get() = CurrentTimeAction.PickDate

    override val title: String
        get() = CelestiaString("Select Time", "")
}

class SetToCurrentTimeItem : CurrentTimeItem {
    override val action: CurrentTimeAction
        get() = CurrentTimeAction.SetToCurrentTime

    override val title: String
        get() = CelestiaString("Set to Current Time", "")
}

private fun createSections(): List<CommonSectionV2> {
    val sections = ArrayList<CommonSectionV2>()
    sections.add(CommonSectionV2(listOf(DatePickerItem(), SetToCurrentTimeItem())))
    return sections
}

class SettingsCurrentTimeRecyclerViewAdapter(
    private val listener: SettingsCurrentTimeFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(createSections()) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is CurrentTimeItem) {
            listener?.onCurrentTimeActionRequested(item.action)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is SetToCurrentTimeItem || item is DatePickerItem)
            return SETTING_ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SETTING_ITEM) {
            return CommonTextViewHolder(parent)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is SetToCurrentTimeItem && holder is CommonTextViewHolder) {
            holder.title.text = item.title
            holder.detail.visibility = View.GONE
            return
        }
        if (item is DatePickerItem && holder is CommonTextViewHolder) {
            val core = CelestiaAppCore.shared()
            holder.title.text = item.title
            holder.detail.visibility = View.VISIBLE
            holder.detail.text = createDateFromJulianDay(core.simulation.time).toString()
            return
        }
        super.bindVH(holder, item)
    }

    fun reload() {
        updateSectionsWithHeader(createSections())
    }

    private companion object {
        const val SETTING_ITEM = 0
    }
}
