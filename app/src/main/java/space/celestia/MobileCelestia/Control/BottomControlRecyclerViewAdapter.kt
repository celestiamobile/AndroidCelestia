package space.celestia.MobileCelestia.Control

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import space.celestia.MobileCelestia.R

import space.celestia.MobileCelestia.Control.BottomControlFragment.Listener

import kotlinx.android.synthetic.main.fragment_bottom_control_item.view.*

class BottomControlRecyclerViewAdapter(
    private val values: List<CelestiaActionItem>,
    private val listener: Listener?
) : RecyclerView.Adapter<BottomControlRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as CelestiaActionItem
            listener?.onActionSelected(item.action)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_bottom_control_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        holder.imageView.setImageResource(item.image)

        with(holder.view) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.image
    }
}
