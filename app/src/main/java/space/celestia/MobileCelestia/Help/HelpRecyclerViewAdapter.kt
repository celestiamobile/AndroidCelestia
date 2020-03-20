package space.celestia.MobileCelestia.Help

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_help_action_item.view.*
import space.celestia.MobileCelestia.R

import space.celestia.MobileCelestia.Help.HelpFragment.Listener

import kotlinx.android.synthetic.main.fragment_help_description_item.view.*
import space.celestia.MobileCelestia.Common.CommonSection
import space.celestia.MobileCelestia.Common.RecyclerViewItem
import space.celestia.MobileCelestia.Common.SeparatorRecyclerViewAdapter

open class HelpItem : RecyclerViewItem {
    override val clickable: Boolean
        get() = false
}
class DescriptionItem(val description: String, val imageResourceID: Int) : HelpItem() {}
class ActionItem(val title: String, val action: HelpAction) : HelpItem() {}

class HelpRecyclerViewAdapter(
    values: List<List<HelpItem>>,
    private val listener: Listener?
) : SeparatorRecyclerViewAdapter(1, 0, values.map { CommonSection(it, false, false) }) {

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is ActionItem) {
            return ACTION
        }
        return DESCRIPTION
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ACTION) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_help_action_item, parent, false)
            return ActionViewHolder(view)
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_help_description_item, parent, false)
        return DescriptionViewHolder(view)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is ActionViewHolder && item is ActionItem) {
            holder.button.text = item.title
            holder.buttonContainer.setOnClickListener {
                listener?.onHelpActionSelected(item.action)
            }
        } else if (holder is DescriptionViewHolder && item is DescriptionItem) {
            holder.descriptionView.text = item.description
            holder.imageView.setImageResource(item.imageResourceID)
        }
    }

    inner class ActionViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val button: TextView = view.button
        val buttonContainer: View = view.button_container
    }

    inner class DescriptionViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val descriptionView: TextView = view.description
        var imageView: ImageView = view.image
    }

    private companion object {
        const val DESCRIPTION   = 0
        const val ACTION        = 1
    }
}
