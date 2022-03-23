package space.celestia.mobilecelestia.resource

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.resource.model.AsyncListItem
import space.celestia.mobilecelestia.resource.model.AsyncListPagingAdapter
import space.celestia.mobilecelestia.resource.model.AsyncListPagingViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

abstract class AsyncListPagingFragment: NavigationFragment.SubFragment() {
    abstract val viewModel: AsyncListPagingViewModel

    private var selectListener: Listener? = null

    interface Listener {
        fun onAsyncListPagingItemSelected(item: AsyncListItem)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loading_grouped_list, container, false)
        val loadingIndicator = view.findViewById<CircularProgressIndicator>(R.id.loading_indicator)
        val refresh = view.findViewById<TextView>(R.id.refresh)
        refresh.text = CelestiaString("Refresh", "")

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        val adapter = AsyncListPagingAdapter(selectListener)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.isVisible = false
        loadingIndicator.isVisible = true
        refresh.isVisible = false

        adapter.addLoadStateListener { states ->
            when (states.source.refresh) {
                is LoadState.NotLoading -> {
                    recyclerView.isVisible = true
                    loadingIndicator.isVisible = false
                    refresh.isVisible = false
                }
                is LoadState.Loading -> {
                    recyclerView.isVisible = false
                    loadingIndicator.isVisible = true
                    refresh.isVisible = false
                }
                is LoadState.Error -> {
                    recyclerView.isVisible = false
                    loadingIndicator.isVisible = false
                    refresh.isVisible = true
                }
            }
        }

        refresh.setOnClickListener {
            adapter.retry()
        }

        viewModel.items.observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            selectListener = context
        } else {
            throw RuntimeException("$context must implement AsyncListPagingFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        selectListener = null
    }

    inner class SpaceItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val density =  parent.resources.displayMetrics.density
            val spacing = (16 * density).toInt()
            val pos = parent.getChildLayoutPosition(view)
            outRect.left = spacing
            outRect.right = spacing
            outRect.top = if (pos == 0) spacing else spacing / 2
            outRect.bottom = if (pos + 1 == parent.adapter?.itemCount) spacing else spacing / 2
        }
    }
}