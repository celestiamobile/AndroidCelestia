package space.celestia.MobileCelestia.Search

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

import space.celestia.MobileCelestia.Common.CommonSection
import space.celestia.MobileCelestia.Common.CommonTextViewHolder
import space.celestia.MobileCelestia.Common.RecyclerViewItem
import space.celestia.MobileCelestia.Common.SeparatorRecyclerViewAdapter
import space.celestia.MobileCelestia.Search.Model.SearchResultItem

class SearchRecyclerViewAdapter(
    private val listener: SearchFragment.Listener?
) : SeparatorRecyclerViewAdapter() {

    override fun itemViewType(item: RecyclerViewItem): Int {
        return SEARCH_RESULT
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CommonTextViewHolder(parent)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is SearchResultItem && holder is CommonTextViewHolder) {
            holder.title.text = item.result
        }
    }

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is SearchResultItem) {
            listener?.onSearchItemSelected(item.result)
        }
    }

    fun updateSearchResults(results: List<String>) {
        updateSections(listOf(CommonSection(results.map { SearchResultItem(it) })))
    }

    private companion object {
        const val SEARCH_RESULT = 0
    }
}
