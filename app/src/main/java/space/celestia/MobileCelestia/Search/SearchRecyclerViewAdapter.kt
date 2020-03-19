package space.celestia.MobileCelestia.Search

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import space.celestia.MobileCelestia.R

import kotlinx.android.synthetic.main.fragment_search_item.view.*
import space.celestia.MobileCelestia.Common.SeparatorRecyclerViewAdapter
import space.celestia.MobileCelestia.Search.Model.SearchResultItem

class SearchRecyclerViewAdapter(
    private val listener: SearchFragment.Listener?
) : SeparatorRecyclerViewAdapter() {

    override fun itemViewType(item: RecyclerViewItem): Int {
        return SEARCH_RESULT
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_search_item, parent, false)
        return ViewHolder(view)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is SearchResultItem && holder is ViewHolder) {
            holder.contentView.text = item.result
        }
    }

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is SearchResultItem) {
            listener?.onSearchItemSelected(item.result)
        }
    }

    fun updateSearchResults(results: List<String>) {
        updateSections(listOf(RecyclerViewSection(results.map { SearchResultItem(it) })))
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val contentView: TextView = view.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    private companion object {
        const val SEARCH_RESULT = 0
    }
}
