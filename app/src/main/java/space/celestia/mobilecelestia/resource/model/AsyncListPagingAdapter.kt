package space.celestia.mobilecelestia.resource.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDivider
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.resource.AsyncListPagingFragment
import java.lang.ref.WeakReference

class AsyncListPagingAdapter(listener: AsyncListPagingFragment.Listener?): PagingDataAdapter<AsyncListPagingItem, RecyclerView.ViewHolder>(COMPARATOR) {
    private val listener = if (listener == null) null else WeakReference(listener)

    inner class SeparatorViewHolder(val view: MaterialDivider) : RecyclerView.ViewHolder(view)

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
        when (viewType) {
            TYPE_SEPARATOR -> {
                val view = MaterialDivider(parent.context)
                view.setDividerThicknessResource(R.dimen.default_separator_height)
                view.setDividerInsetStartResource(R.dimen.full_separator_inset_start)
                view.setDividerColorResource(R.color.colorSeparator)
                view.setBackgroundResource(R.color.colorSecondaryBackground)
                return SeparatorViewHolder(view)
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
                return CommonTextViewHolder(parent)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: return

        when (item) {
            is AsyncListPagingItem.Data -> {
                if (holder is CommonTextViewHolder) {
                    holder.title.text = item.data.name
                }
                holder.itemView.setOnClickListener {
                    listener?.get()?.onAsyncListPagingItemSelected(item.data)
                }
            }
            is AsyncListPagingItem.Separator -> {
                val viewHolder = holder as SeparatorViewHolder
                viewHolder.view.setDividerInsetStartResource(item.dividerInsetStartResource)
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
                    return oldItem.dividerInsetStartResource == newItem.dividerInsetStartResource
                } else if (oldItem is AsyncListPagingItem.Header && newItem is AsyncListPagingItem.Header) {
                    return true
                } else if (oldItem is AsyncListPagingItem.Footer && newItem is AsyncListPagingItem.Footer) {
                    return true
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