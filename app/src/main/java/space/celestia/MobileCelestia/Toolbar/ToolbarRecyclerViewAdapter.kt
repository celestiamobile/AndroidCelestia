package space.celestia.MobileCelestia.Toolbar

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import space.celestia.MobileCelestia.R

import space.celestia.MobileCelestia.Toolbar.ToolbarFragment.ToolbarListFragmentInteractionListener
import space.celestia.MobileCelestia.Toolbar.Model.ToolbarListItem
import space.celestia.MobileCelestia.Toolbar.Model.ToolbarActionItem
import space.celestia.MobileCelestia.Toolbar.Model.ToolbarSeparatorItem

import kotlinx.android.synthetic.main.fragment_toolbar_action_item.view.*

class ToolbarRecyclerViewAdapter(
    private val values: List<ToolbarListItem>,
    private val listener: ToolbarListFragmentInteractionListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val tag = v.tag
            if (tag is ToolbarActionItem) {
                listener?.onToolbarActionSelected(tag.action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TOOLBAR_ACTION) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_toolbar_action_item, parent, false)
            return ActionViewHolder(view)
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_toolbar_separator_item, parent, false)
        return SeparatorViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        val item = values[position]
        if (item is ToolbarActionItem) {
            return TOOLBAR_ACTION
        }
        if (item is ToolbarSeparatorItem) {
            return TOOLBAR_SEPARATOR
        }
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = values[position]
        if (item is ToolbarActionItem && holder is ActionViewHolder) {
            holder.contentView.text = item.title
            holder.imageView.setImageResource(item.image)

            with(holder.view) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ActionViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val contentView: TextView = view.content
        val imageView: ImageView = view.image

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    inner class SeparatorViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    }

    companion object {
        val TOOLBAR_ACTION = 0
        val TOOLBAR_SEPARATOR = 1
    }
}
