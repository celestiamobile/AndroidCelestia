package space.celestia.MobileCelestia.Common

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.common_text_list_item.view.*
import space.celestia.MobileCelestia.R

public class CommonTextViewHolder(parent: ViewGroup):
    RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_text_list_item, parent, false)) {
    public val title = itemView.title
    public val detail = itemView.detail
    public var accessory = itemView.accessory
}