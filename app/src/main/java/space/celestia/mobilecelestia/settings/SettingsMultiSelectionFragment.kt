package space.celestia.mobilecelestia.settings

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import space.celestia.mobilecelestia.R

class SettingsMultiSelectionFragment : SettingsBaseFragment() {

    private var item: SettingsMultiSelectionItem? = null

    private var listener: Listener? = null

    private val listAdapter by lazy { SettingsMultiSelectionRecyclerViewAdapter(item!!, listener) }

    override val title: String
        get() = item!!.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            item = it.getSerializable(ARG_ITEM) as? SettingsMultiSelectionItem
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_settings_multi_selection_list, container, false)

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
            throw RuntimeException("$context must implement SettingsMultiSelectionFragment.Listener")
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
        fun onMultiSelectionSettingItemChange(field: String, on: Boolean)
    }

    companion object {

        const val ARG_ITEM = "item"

        @JvmStatic
        fun newInstance(item: SettingsMultiSelectionItem) =
            SettingsMultiSelectionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}
