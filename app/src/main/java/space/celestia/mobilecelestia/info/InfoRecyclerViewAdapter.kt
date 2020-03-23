package space.celestia.mobilecelestia.info

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import space.celestia.mobilecelestia.R

import space.celestia.mobilecelestia.info.InfoFragment.Listener

import kotlinx.android.synthetic.main.fragment_info_action_item.view.*
import kotlinx.android.synthetic.main.fragment_info_description_item.view.*
import space.celestia.mobilecelestia.info.model.InfoActionItem
import space.celestia.mobilecelestia.info.model.InfoDescriptionItem
import space.celestia.mobilecelestia.info.model.InfoItem

class InfoRecyclerViewAdapter(
    private val values: List<InfoItem>,
    private val listener: Listener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
        val tag = v.tag
        if (tag is InfoActionItem) {
            listener?.onInfoActionSelected(tag)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ACTION_ITEM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_info_action_item, parent, false)
            return ActionViewHolder(view)
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_info_description_item, parent, false)
        return DescriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = values[position]
        if (item is InfoActionItem && holder is ActionViewHolder) {
            holder.button.text = item.title

            with(holder.view) {
                tag = item
                setOnClickListener(onClickListener)
            }
        } else if (item is InfoDescriptionItem && holder is DescriptionViewHolder) {
            holder.contentView.text = item.overview
            holder.titleView.text = item.name
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = values[position]
        if (item is InfoActionItem) {
            return ACTION_ITEM
        }
        if (item is InfoDescriptionItem) {
            return DESCRIPTION_ITEM
        }
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int = values.size

    inner class ActionViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val button: TextView = view.button

        override fun toString(): String {
            return super.toString() + " '" + button.text + "'"
        }
    }

    inner class DescriptionViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val contentView: TextView = view.content
        val titleView: TextView = view.title

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    companion object {
        val ACTION_ITEM         = 0
        val DESCRIPTION_ITEM    = 1
    }
}
