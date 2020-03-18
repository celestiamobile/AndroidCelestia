package space.celestia.MobileCelestia.Toolbar

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import space.celestia.MobileCelestia.R
import space.celestia.MobileCelestia.Toolbar.Model.ToolbarActionItem

import space.celestia.MobileCelestia.Toolbar.Model.ToolbarListItem
import space.celestia.MobileCelestia.Toolbar.Model.ToolbarSeparatorItem
import java.io.Serializable
import kotlin.collections.ArrayList

enum class ToolbarAction : Serializable {
    Celestia, Setting, Share, Search, Time, Script, Camera, Browse, Help, Favorite;

    val title: String
        get() {
            when (this) {
                Celestia -> {
                    return "Information"
                }
                Setting -> {
                    return "Setting"
                }
                Share -> {
                    return "Share"
                }
                Search -> {
                    return "Search"
                }
                Time -> {
                    return "Time Control"
                }
                Script -> {
                    return "Script Control"
                }
                Camera -> {
                    return "Camera Control"
                }
                Browse -> {
                    return "Browser"
                }
                Help -> {
                    return "Help"
                }
                Favorite -> {
                    return "Favorite"
                }
            }
        }

    val imageName: String
        get() {
            return "toolbar_" + toString().toLowerCase()
        }

    companion object {
        val persistentAction: List<List<ToolbarAction>>
            get() = listOf(
                listOf(Share, Search),
                listOf(Camera, Time, Script),
                listOf(Browse, Favorite),
                listOf(Help),
                listOf(Setting)
            )
    }
}

class ToolbarFragment : Fragment() {

    private var existingActions: List<List<ToolbarAction>> = ArrayList<List<ToolbarAction>>()
    private var listener: ToolbarListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val value = it.getSerializable(ARG_ACTION_WRAPPER)
            if (value is Wrapper) {
                existingActions = value.items
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_toolbar_list, container, false)

        val allItems = ArrayList(existingActions)
        allItems.addAll(ToolbarAction.persistentAction)

        val model = ArrayList<ToolbarListItem>()
        for (section in allItems) {
            for (item in section) {
                model.add(ToolbarActionItem(
                    item,
                    resources.getIdentifier(item.imageName,
                        "drawable",
                        activity!!.packageName
                    )))
            }
            model.add(ToolbarSeparatorItem())
        }

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = ToolbarRecyclerViewAdapter(model, listener)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ToolbarListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement ToolbarListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface ToolbarListFragmentInteractionListener {
        fun onToolbarActionSelected(action: ToolbarAction)
    }

    inner class Wrapper(val items: List<List<ToolbarAction>>) : Serializable {}

    companion object {

        const val ARG_ACTION_WRAPPER = "action-wrapper"

        @JvmStatic
        fun newInstance(actions: List<List<ToolbarAction>>) =
            ToolbarFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ACTION_WRAPPER, Wrapper(actions))
                }
            }
    }
}
