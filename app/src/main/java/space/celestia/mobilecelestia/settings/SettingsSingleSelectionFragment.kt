package space.celestia.mobilecelestia.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R

class SettingsSingleSelectionFragment : SettingsBaseFragment() {

    private var item: SettingsSingleSelectionItem? = null

    private var listener: Listener? = null

    private val listAdapter by lazy { SettingsSingleSelectionRecyclerViewAdapter(item!!, listener) }

    override val title: String
        get() = item!!.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            item = it.getSerializable(ARG_ITEM) as? SettingsSingleSelectionItem
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_settings_single_selection_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SettingsSingleSelectionFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun reload() {
        listAdapter.reload()
        listAdapter.notifyDataSetChanged()
    }

    interface Listener {
        fun onSingleSelectionSettingItemChange(field: String, value: Int)
    }

    companion object {

        const val ARG_ITEM = "item"

        @JvmStatic
        fun newInstance(item: SettingsSingleSelectionItem) =
            SettingsSingleSelectionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}
