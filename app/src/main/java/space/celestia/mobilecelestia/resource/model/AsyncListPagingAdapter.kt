package space.celestia.mobilecelestia.resource.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.SeparatorView
import space.celestia.mobilecelestia.resource.AsyncListPagingFragment
import java.lang.ref.WeakReference

class AsyncListPagingAdapter(private val stylized: Boolean, listener: AsyncListPagingFragment.Listener?): PagingDataAdapter<AsyncListPagingItem, RecyclerView.ViewHolder>(COMPARATOR) {
    private val listener = if (listener == null) null else WeakReference(listener)

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
            get() = itemView.findViewById(R.id.resource_title)
        val image: ImageView
            get() = itemView.findViewById(R.id.resource_image)
    }

    inner class SeparatorViewHolder(val view: SeparatorView) : RecyclerView.ViewHolder(view)

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is AsyncListPagingItem.Data -> TYPE_ITEM
            is AsyncListPagingItem.Separator -> TYPE_SEPARATOR
            is AsyncListPagingItem.Header -> TYPE_HEADER
            is AsyncListPagingItem.Footer -> TYPE_FOOTER
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SEPARATOR -> {
                val view = SeparatorView(parent.context, 0.5f, 0.0f, R.color.colorSecondaryBackground)
                SeparatorViewHolder(view)
            }
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.common_section_header, parent, false)
                return HeaderViewHolder(view)
            }
            TYPE_FOOTER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.common_section_footer, parent, false)
                return FooterViewHolder(view)
            }
            else -> {
                if (stylized) {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.common_resource_item, parent, false)
                    ItemViewHolder(view)
                } else {
                    CommonTextViewHolder(parent)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: return

        when (item) {
            is AsyncListPagingItem.Data -> {
                if (holder is ItemViewHolder) {
                    holder.title.text = item.data.name
                    val imageURL = item.data.imageURL
                    if (imageURL != null) {
                        Glide.with(holder.image).load(imageURL).placeholder(R.drawable.resource_item_placeholder).into(holder.image)
                    } else {
                        Glide.with(holder.image).clear(holder.image)
                        holder.image.setImageResource(R.drawable.resource_item_placeholder)
                    }
                } else if (holder is CommonTextViewHolder) {
                    holder.title.text = item.data.name
                }
                holder.itemView.setOnClickListener {
                    listener?.get()?.onAsyncListPagingItemSelected(item.data)
                }
            }
            is AsyncListPagingItem.Separator -> {
                val viewHolder = holder as SeparatorViewHolder
                viewHolder.view.separatorInset = item.inset
            }
            else -> {}
        }
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView by lazy { itemView.findViewById(R.id.text) }
    }

    inner class FooterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView by lazy { itemView.findViewById(R.id.text) }
    }

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_SEPARATOR = 1
        private const val TYPE_HEADER = 2
        private const val TYPE_FOOTER = 3

        private val COMPARATOR = object : DiffUtil.ItemCallback<AsyncListPagingItem>() {
            override fun areItemsTheSame(
                oldItem: AsyncListPagingItem,
                newItem: AsyncListPagingItem
            ): Boolean {
                if (oldItem is AsyncListPagingItem.Data && newItem is AsyncListPagingItem.Data) {
                    return oldItem.data.id == newItem.data.id
                } else if (oldItem is AsyncListPagingItem.Separator && newItem is AsyncListPagingItem.Separator) {
                    return oldItem.inset == newItem.inset
                }
                return false
            }

            override fun areContentsTheSame(
                oldItem: AsyncListPagingItem,
                newItem: AsyncListPagingItem
            ): Boolean {
                return areItemsTheSame(oldItem, newItem)
            }
        }
    }
}