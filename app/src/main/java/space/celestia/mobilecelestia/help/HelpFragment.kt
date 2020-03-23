package space.celestia.mobilecelestia.help

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R

enum class HelpAction {
    RunDemo;
}

private val staticHelpDescriptionItems: List<DescriptionItem> by lazy { listOf(
    DescriptionItem("Tap to select an object.", R.drawable.tutorial_gesture_tap),
    DescriptionItem("Drag with one finger to rotate around an object.", R.drawable.tutorial_gesture_one_finger_pan),
    DescriptionItem("Drag with two fingers to move around.", R.drawable.tutorial_gesture_two_finger_pan),
    DescriptionItem("Pinch to zoom in/out on an object.", R.drawable.tutorial_gesture_pinch)
) }
private val staticHelpActionItems: List<ActionItem> by lazy { listOf( ActionItem("Run Demo", HelpAction.RunDemo) ) }

class HelpFragment : Fragment() {

    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_help, container, false)

        // Set the adapter
        with(view.findViewById<RecyclerView>(R.id.list)) {
            layoutManager = LinearLayoutManager(context)
            adapter = HelpRecyclerViewAdapter(listOf(staticHelpDescriptionItems, staticHelpActionItems), listener)
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement HelpFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onHelpActionSelected(action: HelpAction)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HelpFragment()
    }
}
