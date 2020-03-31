package space.celestia.mobilecelestia.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.search.model.RxSearchObservable
import java.util.concurrent.TimeUnit

class SearchFragment : Fragment() {

    private var listener: Listener? = null
    private val listAdapter by lazy { SearchRecyclerViewAdapter(listener) }

    @SuppressLint("CheckResult")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_item_list, container, false)

        view.findViewById<View>(R.id.search_container).setOnTouchListener { _, _ -> true }
        val searchView = view.findViewById<SearchView>(R.id.search)
        RxSearchObservable.fromView(searchView)
            .debounce(300, TimeUnit.MILLISECONDS)
            .map {
                if (it.isEmpty()) { return@map listOf<String>() }
                val core = CelestiaAppCore.shared()
                return@map core.simulation.completionForText(it)
            }.distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                listAdapter.updateSearchResults(it)
                listAdapter.notifyDataSetChanged()
            }

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
            throw RuntimeException("$context must implement SearchFragment.Listener")
        }
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
