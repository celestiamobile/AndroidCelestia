package space.celestia.MobileCelestia.Settings

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import space.celestia.MobileCelestia.Common.TitledFragment
import space.celestia.MobileCelestia.R

import java.io.Serializable

class SettingsMultiSelectionFragment : TitledFragment() {

    private var item: SettingsMultiSelectionItem? = null

    private var listener: Listener? = null

    override val title: String
        get() = item!!.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            item = (it.getSerializable(ARG_ITEM) as Wrapper).item
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
                adapter = SettingsMultiSelectionRecyclerViewAdapter(item!!, listener)
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


    inner class Wrapper(val item: SettingsMultiSelectionItem) : Serializable {}

    interface Listener {
    }

    companion object {

        const val ARG_ITEM = "item"

        @JvmStatic
        fun newInstance(item: SettingsMultiSelectionItem) =
            SettingsMultiSelectionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, Wrapper(item))
                }
            }
    }
}
