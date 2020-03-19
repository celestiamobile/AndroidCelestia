package space.celestia.MobileCelestia.Common

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.lang.RuntimeException

open class SeparatorRecyclerViewAdapter(private val separatorHeight: Int = 1,
                                        private val separatorLeft: Int = 16,
                                        sections: List<RecyclerViewSection> = listOf(),
                                        private val fullSection: Boolean = true) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    public interface BaseRecyclerViewItem {}
    private class SeparatorItem(val full: Boolean) : BaseRecyclerViewItem {}

    public interface RecyclerViewItem : BaseRecyclerViewItem {}

    public class RecyclerViewSection(val items: List<RecyclerViewItem>,
                                     val showSectionSeparator: Boolean = true,
                                     val showRowSeparator: Boolean = true) {}

    private var values: List<BaseRecyclerViewItem> = listOf()

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

    fun updateSections(sections: List<RecyclerViewSection>) {
        val data = ArrayList<BaseRecyclerViewItem>()
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
