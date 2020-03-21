package space.celestia.MobileCelestia.Favorite

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import space.celestia.MobileCelestia.Common.TitledFragment
import space.celestia.MobileCelestia.R

class FavoriteItemFragment : TitledFragment() {

    private var listener: Listener? = null
    private var favoriteItem: FavoriteBaseItem? = null

    override val title: String
        get() = favoriteItem!!.title

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            favoriteItem = it.getSerializable(ARG_ITEM) as? FavoriteBaseItem
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_item_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = FavoriteItemRecyclerViewAdapter(favoriteItem!!, listener)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FavoriteItemFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onFavoriteItemSelected(item: FavoriteBaseItem)
    }

    companion object {

        const val ARG_ITEM = "item"

        @JvmStatic
        fun newInstance(item: FavoriteBaseItem) =
            FavoriteItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}
