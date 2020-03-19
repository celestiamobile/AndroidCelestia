package space.celestia.MobileCelestia.Control

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import space.celestia.MobileCelestia.R

import space.celestia.MobileCelestia.Info.Model.CelestiaAction
import java.io.Serializable

fun CelestiaAction.imageName(): String? {
    return when (this) {
        CelestiaAction.Forward -> {
            "time_forward"
        }
        CelestiaAction.Backward -> {
            "time_backward"
        }
        CelestiaAction.PlayPause -> {
            "time_playpause"
        }
        CelestiaAction.CancelScript -> {
            "time_stop"
        }
        else -> {
            null
        }
    }
}

class CelestiaActionItem(val action: CelestiaAction, val image: Int) {}

class BottomControlFragment : Fragment() {

    private var listener: Listener? = null
    private var items: List<CelestiaAction>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            items = (it.getSerializable(ARG_ACTIONS) as? Wrapper)?.actions
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_control_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                val manager = LinearLayoutManager(context)
                manager.orientation = LinearLayoutManager.HORIZONTAL
                layoutManager = manager
                adapter = BottomControlRecyclerViewAdapter(
                    items!!.map { CelestiaActionItem(
                        it,
                        resources.getIdentifier(it.imageName(), "drawable", activity!!.packageName)
                ) }, listener)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement BottomControlFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onActionSelected(item: CelestiaAction)
    }

    private inner class Wrapper(val actions: List<CelestiaAction>) : Serializable {}

    companion object {

        const val ARG_ACTIONS = "action"

        @JvmStatic
        fun newInstance(items: List<CelestiaAction>) =
            BottomControlFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ACTIONS, Wrapper(items))
                }
            }
    }
}
