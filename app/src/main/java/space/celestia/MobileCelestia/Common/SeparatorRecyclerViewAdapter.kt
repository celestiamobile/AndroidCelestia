package space.celestia.MobileCelestia.Common

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.lang.RuntimeException

public interface ViewItem {}

public interface RecyclerViewItem : ViewItem {}

open class CommonSection(val items: List<RecyclerViewItem>,
                         val showSectionSeparator: Boolean = true,
                         val showRowSeparator: Boolean = true) {}

open class SeparatorRecyclerViewAdapter(private val separatorHeight: Int = 1,
                                        private val separatorLeft: Int = 16,
                                        sections: List<CommonSection> = listOf(),
                                        private val fullSection: Boolean = true) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private class SeparatorItem(val full: Boolean) : ViewItem {}

    private var values: List<ViewItem> = listOf()

    private val onClickListener: View.OnClickListener

    init {
        updateSections(sections)
        onClickListener = View.OnClickListener { v ->
            val tag = v.tag
            if (tag is RecyclerViewItem) {
                onItemSelected(tag)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = values[position]
        if (item is SeparatorItem) {
            if (item.full)
                return SEPARATOR_0
            return SEPARATOR_1
        }
        if (item is RecyclerViewItem) {
            return itemViewType(item)
        }
        throw RuntimeException("$this must deal with item type $item")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SEPARATOR_0) {
            val view = SeparatorView(parent.context, 1, 0)
            return SeparatorViewHolder(view)
        }
        if (viewType == SEPARATOR_1) {
            val view = SeparatorView(parent.context, separatorHeight, separatorLeft)
            return SeparatorViewHolder(view)
        }
        return createVH(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = values[position]
        if (item is RecyclerViewItem) {
            bindVH(holder, item)
            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }
    }

    override fun getItemCount(): Int = values.size

    open fun onItemSelected(item: RecyclerViewItem) {}

    open fun itemViewType(item: RecyclerViewItem): Int {
        throw RuntimeException("$this must deal with item type $item")
    }

    open fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        throw RuntimeException("$this must deal with item type $item")
    }

    open fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        throw RuntimeException("$this must deal with item type $viewType")
    }

    fun updateSections(sections: List<CommonSection>) {
        val data = ArrayList<ViewItem>()
        var prevSectionHasSep = false
        for (section in sections) {
            // add a separator to section top
            var showSectionSeparator = section.showSectionSeparator && section.items.count() > 0
            if (!prevSectionHasSep && showSectionSeparator) {
                data.add(SeparatorItem(fullSection))
            }
            for (i in 0 until section.items.count()) {
                data.add(section.items[i])
                // add separators to in between
                if (section.showRowSeparator && i != section.items.count() - 1) {
                    data.add(SeparatorItem(false))
                }
            }
            // add a separator to section bottom
            if (showSectionSeparator) {
                data.add(SeparatorItem(fullSection))
            }
            prevSectionHasSep = showSectionSeparator
        }
        values = data
    }

    inner class SeparatorViewHolder(val view: View) : RecyclerView.ViewHolder(view) {}

    companion object {
        const val SEPARATOR_0 = 99998
        const val SEPARATOR_1 = 99999
    }
}

public class CommonSectionV2(items: List<RecyclerViewItem>, val title: String? = null) : CommonSection(items)  {}

private class HeaderRecyclerViewItem(val title: String?): RecyclerViewItem {}

public fun List<CommonSectionV2>.transformed(): List<CommonSection> {
    val innerSections = ArrayList<CommonSection>()
    for (section in this) {
        innerSections.add(
            CommonSection(listOf(HeaderRecyclerViewItem(section.title)),
                false, false))
        innerSections.add(section)
    }
    return innerSections
}

open class SeparatorHeaderRecyclerViewAdapter(sections: List<CommonSectionV2> = listOf()): SeparatorRecyclerViewAdapter(1, 16, sections.transformed()) {
    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is HeaderRecyclerViewItem)
            return HEADER;
        throw RuntimeException("$this must deal with item type $item")
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is HeaderViewHolder && item is HeaderRecyclerViewItem) {
            holder.view.textView.text = item.title
            return
        }
        throw RuntimeException("$this must deal with item type $item")
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HEADER)
            return HeaderViewHolder(SectionHeaderView(parent.context))
        throw RuntimeException("$this must deal with item type $viewType")
    }

    fun updateSectionsWithHeader(sections: List<CommonSectionV2>) {
        updateSections(sections.transformed())
    }

    inner class HeaderViewHolder(val view: SectionHeaderView) : RecyclerView.ViewHolder(view) {}

    companion object {
        const val HEADER = 99997
    }
}