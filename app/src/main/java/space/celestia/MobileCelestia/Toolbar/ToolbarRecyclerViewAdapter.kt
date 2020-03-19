package space.celestia.MobileCelestia.Toolbar

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import space.celestia.MobileCelestia.R

import space.celestia.MobileCelestia.Toolbar.ToolbarFragment.Listener
import space.celestia.MobileCelestia.Toolbar.Model.ToolbarListItem
import space.celestia.MobileCelestia.Toolbar.Model.ToolbarActionItem

import kotlinx.android.synthetic.main.fragment_toolbar_action_item.view.*
import space.celestia.MobileCelestia.Common.SeparatorRecyclerViewAdapter

class ToolbarRecyclerViewAdapter(
    values: List<List<ToolbarListItem>>,
    private val listener: Listener?
) : SeparatorRecyclerViewAdapter(6, 32, values.map { RecyclerViewSection(it, true, false) }, false) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is ToolbarActionItem) {
            listener?.onToolbarActionSelected(item.action)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        return TOOLBAR_ACTION
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_toolbar_action_item, parent, false)
        return ActionViewHolder(view)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is ToolbarActionItem && holder is ActionViewHolder) {
            holder.contentView.text = item.title
            holder.imageView.setImageResource(item.image)
        }
    }

    inner class ActionViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val contentView: TextView = view.content
        val imageView: ImageView = view.image

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    companion object {
        const val TOOLBAR_ACTION = 0
    }
}
