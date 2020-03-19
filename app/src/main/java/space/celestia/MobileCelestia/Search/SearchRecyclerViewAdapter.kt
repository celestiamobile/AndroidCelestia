package space.celestia.MobileCelestia.Search

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import space.celestia.MobileCelestia.R


import space.celestia.MobileCelestia.Search.SearchFragment.Listener

import kotlinx.android.synthetic.main.fragment_search_item.view.*
import space.celestia.MobileCelestia.Search.Model.SearchResultItem

class SearchRecyclerViewAdapter(
    private val listener: Listener?
) : RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder>() {

    private var values: List<SearchResultItem> = listOf()
    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val item = v.tag as SearchResultItem
            listener?.onSearchItemSelected(item.result)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_search_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.contentView.text = item.result

        with(holder.view) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount(): Int = values.size

    fun updateSearchResults(results: List<String>) {
        values = results.map { SearchResultItem(it) }
        notifyDataSetChanged()
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val contentView: TextView = view.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}
