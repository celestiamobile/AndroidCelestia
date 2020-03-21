package space.celestia.MobileCelestia.Search

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import space.celestia.MobileCelestia.Core.CelestiaAppCore
import space.celestia.MobileCelestia.R

class SearchFragment : Fragment(), SearchView.Listener {

    private var listener: Listener? = null
    private val listAdapter by lazy { SearchRecyclerViewAdapter(listener) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_item_list, container, false)

        val searchView = view.findViewById<SearchView>(R.id.search)
        searchView.listener = this

        // Set the adapter
        with(view.findViewById<RecyclerView>(R.id.list)) {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SearchListFragmentInteractionListener")
        }
    }

    override fun onTextChanged(newText: String) {
        val core = CelestiaAppCore.shared()
        if (newText.isEmpty()) {
            listAdapter.updateSearchResults(listOf())
        } else {
            listAdapter.updateSearchResults(core.simulation.completionForText(newText))
        }
        listAdapter.notifyDataSetChanged()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onSearchItemSelected(text: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() = SearchFragment()
    }
}
