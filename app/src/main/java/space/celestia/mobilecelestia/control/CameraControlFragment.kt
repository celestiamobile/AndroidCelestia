package space.celestia.mobilecelestia.control

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R

enum class CameraControlAction(val value: Int) {
    Pitch0(32), Pitch1(26), Yaw0(28), Yaw1(30), Roll0(31), Roll1(33), Reverse(-1);
}

class CameraControlFragment : Fragment() {
    private var listener: Listener? = null

    private val toolbar by lazy { view!!.findViewById<Toolbar>(R.id.toolbar) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera_control_item_list, container, false)

        with(view.findViewById<RecyclerView>(R.id.list)) {
            layoutManager = LinearLayoutManager(context)
            adapter = CameraControlItemRecyclerViewAdapter(listener)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.title = "Camera Control"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CameraControlItemFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onCameraActionClicked(action: CameraControlAction)
        fun onCameraActionStepperTouchDown(action: CameraControlAction)
        fun onCameraActionStepperTouchUp(action: CameraControlAction)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CameraControlFragment()
    }
}
