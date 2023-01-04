package space.celestia.mobilecelestia.control

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestia.Renderer
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.StandardImageButton
import space.celestia.ui.slider.OnChangeListener
import space.celestia.ui.slider.TimelineSlider
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TimelineControlFragment: Fragment() {
    interface Listener {
        fun onTimelineControlHide()
    }

    @Inject
    lateinit var appCore: AppCore
    @Inject
    lateinit var renderer: Renderer

    private var listener: Listener? = null

    private var startTime = 0.0
    private var endTime = 0.0
    private var tickTimes = DoubleArray(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            @Suppress("UNCHECKED_CAST")
            startTime = it.getDouble(ARG_START_TIME)
            endTime = it.getDouble(ARG_END_TIME)
            tickTimes = it.getDoubleArray(ARG_TICK_TIMES)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timeline_control, container, false)
        val hideButton = view.findViewById<StandardImageButton>(R.id.hide_button)
        val timelineSlider = view.findViewById<TimelineSlider>(R.id.timeline_slider)
        timelineSlider.valueFrom = startTime.toFloat()
        timelineSlider.valueTo = endTime.toFloat()
        timelineSlider.value = startTime.toFloat()
        timelineSlider.ticks = ArrayList(tickTimes.map { it.toFloat() })
        val weakSelf = WeakReference(this)
        hideButton.setOnClickListener {
            val self = weakSelf.get() ?: return@setOnClickListener
            self.listener?.onTimelineControlHide()
        }
        renderer.enqueueTask {
            val self = weakSelf.get() ?: return@enqueueTask
            val time = self.appCore.simulation.time
            self.lifecycleScope.launch {
                timelineSlider.value = time.toFloat()
            }
        }
        timelineSlider.addOnChangeListener(object: OnChangeListener {
            override fun onValueChange(timelineSlider: TimelineSlider, value: Float, fromUser: Boolean) {
                val self = weakSelf.get() ?: return
                self.renderer.enqueueTask {
                    self.appCore.simulation.time = value.toDouble()
                }
            }
        })
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement TimelineControlFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        private const val ARG_START_TIME = "start_time"
        private const val ARG_END_TIME = "end_time"
        private const val ARG_TICK_TIMES = "tick_times"

        fun newInstance(startTime: Double, endTime: Double, tickTimes: DoubleArray): TimelineControlFragment {
            return TimelineControlFragment().apply {
                arguments = Bundle().apply {
                    putDouble(ARG_START_TIME, startTime)
                    putDouble(ARG_END_TIME, endTime)
                    putDoubleArray(ARG_TICK_TIMES, tickTimes)
                }
            }
        }
    }
}