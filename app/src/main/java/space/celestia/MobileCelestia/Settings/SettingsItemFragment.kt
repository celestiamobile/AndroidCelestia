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

class SettingsItemFragment : TitledFragment() {

    var listener: Listener? = null

    override val title: String
        get() = "Settings"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings_item_main_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = SettingsItemRecyclerViewAdapter(listener)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SettingsItemMainFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onMainSettingItemSelected(item: SettingsItem)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsItemFragment()
    }
}
