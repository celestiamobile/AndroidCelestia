package space.celestia.mobilecelestia.settings

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import kotlinx.android.synthetic.main.fragment_multiline_list_item.view.*
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.R

interface AboutItem : RecyclerViewItem {}

class VersionItem(val versionName: String) : AboutItem {
    val title: String
        get() = "Version"
    override val clickable: Boolean
        get() = false
}

val AboutAction.title: String
    get() {
        return when (this) {
            AboutAction.VisitOfficialWebsite -> {
                "Official Website"
            }
            AboutAction.VisitOfficialForum -> {
                "Support Forum"
            }
        }
    }

class ActionItem(val action: AboutAction) : AboutItem {
    val title: String
        get() = action.title

    override val clickable: Boolean
        get() = true
}

class TitleItem(val title: String) : AboutItem {
    override val clickable: Boolean
        get() = false
}

class DetailItem(val detail: String) : AboutItem {
    override val clickable: Boolean
        get() = false
}

class AboutRecyclerViewAdapter(
    private val values: List<List<AboutItem>>,
    private val listener: AboutFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(values.map { CommonSectionV2(it) }) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is ActionItem) {
            listener?.onAboutActionSelected(item.action)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is VersionItem)
            return VERSION_ITEM
        if (item is ActionItem)
            return ACTION_ITEM
        if (item is TitleItem)
            return TITLE_ITEM
        if (item is DetailItem)
            return DETAIL_ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VERSION_ITEM) {
            val holder = CommonTextViewHolder(parent)
            return holder
        }
        if (viewType == ACTION_ITEM) {
            val holder = CommonTextViewHolder(parent)
            holder.title.setTextColor(parent.resources.getColor(R.color.colorThemeLabel))
            return holder
        }
        if (viewType == TITLE_ITEM) {
            val holder = CommonTextViewHolder(parent)
            return holder
        }
        if (viewType == DETAIL_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_multiline_list_item, parent, false)
            val holder = MultilineViewHolder(view)
            return holder
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is CommonTextViewHolder) {
            if (item is VersionItem) {
                holder.title.text = item.title
                holder.detail.text = item.versionName
            } else if (item is ActionItem) {
                holder.title.text = item.title
            } else if (item is TitleItem) {
                holder.title.text = item.title
            }
            return
        }
        if (item is DetailItem && holder is MultilineViewHolder) {
            holder.textView.text = item.detail
            return
        }
        super.bindVH(holder, item)
    }

    inner class MultilineViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.text
    }

    private companion object {
        const val VERSION_ITEM = 0
        const val ACTION_ITEM = 1
        const val TITLE_ITEM = 2
        const val DETAIL_ITEM = 3
    }
}
