package space.celestia.MobileCelestia.Settings

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import space.celestia.MobileCelestia.R

import java.io.Serializable

class SettingsCurrentTimeFragment : SettingsBaseFragment() {
    
    private var listener: Listener? = null

    private val listAdapter by lazy { SettingsCurrentTimeRecyclerViewAdapter(listener) }

    override val title: String
        get() = SettingsCurrentTimeItem().name

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_settings_current_time_list, container, false)

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
            throw RuntimeException("$context must implement SettingsCurrentTimeFragment.Listener")
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
        fun onCurrentTimeActionRequested(action: CurrentTimeAction)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsCurrentTimeFragment()
    }
}
