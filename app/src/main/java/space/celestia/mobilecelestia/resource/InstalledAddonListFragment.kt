package space.celestia.mobilecelestia.resource

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.resource.model.InstalledAddonListAdapter
import space.celestia.mobilecelestia.resource.model.ResourceItem
import space.celestia.mobilecelestia.resource.model.ResourceManager
import space.celestia.mobilecelestia.utils.CelestiaString
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class InstalledAddonListFragment: NavigationFragment.SubFragment(), InstalledAddonListAdapter.Listener {
    @Inject
    lateinit var resourceManager: ResourceManager

    private var installedAddons = listOf<ResourceItem>()

    private lateinit var refreshButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingIndicator: CircularProgressIndicator
    private val adapter by lazy { InstalledAddonListAdapter(WeakReference(this)) }

    private var listener: Listener? = null

    interface Listener {
        fun onInstalledAddonSelected(addon: ResourceItem)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loading_grouped_list, container, false)
        recyclerView = view.findViewById(R.id.list)
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        refreshButton = view.findViewById(R.id.refresh)
        refreshButton.text = CelestiaString("Refresh", "")

        refreshButton.visibility = View.GONE
        adapter.update(installedAddons)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        if (installedAddons.isEmpty()) {
            loadingIndicator.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            loadingIndicator.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
        lifecycleScope.launch {
            val addons = withContext(Dispatchers.IO) {
                resourceManager.installedResourcesAsync()
            }
            loadingIndicator.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            installedAddons = addons
            adapter.update(installedAddons)
            adapter.notifyDataSetChanged()
        }
        return view
    }

    override fun onItemSelected(item: ResourceItem) {
        listener?.onInstalledAddonSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Installed", "")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement InstalledAddonListFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        fun newInstance() = InstalledAddonListFragment()
    }
}