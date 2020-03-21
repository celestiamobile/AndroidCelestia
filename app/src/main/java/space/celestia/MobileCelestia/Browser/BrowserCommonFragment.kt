package space.celestia.MobileCelestia.Browser

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import space.celestia.MobileCelestia.Common.TitledFragment
import space.celestia.MobileCelestia.R

import space.celestia.MobileCelestia.Core.CelestiaAppCore
import space.celestia.MobileCelestia.Core.CelestiaBrowserItem
import java.io.Serializable

class BrowserCommonFragment : TitledFragment() {

    private var listener: Listener? = null
    var browserItem: CelestiaBrowserItem? = null

    override val title: String
        get() = browserItem!!.name!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            browserItem = (it.getSerializable(ARG_ITEM) as? Wrapper)?.item
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_browser_common_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = BrowserCommonRecyclerViewAdapter(browserItem!!, listener)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement BrowserCommonFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onBrowserItemSelected(item: BrowserItem)
    }

    inner class Wrapper(val item: CelestiaBrowserItem) : Serializable

    companion object {

        const val ARG_ITEM = "item"

        @JvmStatic
        fun newInstance(item: CelestiaBrowserItem) =
            BrowserCommonFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, Wrapper(item))
                }
            }
    }
}
