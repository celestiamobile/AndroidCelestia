package space.celestia.mobilecelestia.loading

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.utils.AppStatusReporter

class LoadingFragment : Fragment(), AppStatusReporter.Listener {

    private var loadingLabel: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppStatusReporter.shared().register(this)

        retainInstance = true
    }

    override fun onDestroy() {
        AppStatusReporter.shared().unregister(this)

        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_loading, container, false)
        loadingLabel = view.findViewById(R.id.loading_label)
        loadingLabel?.text = AppStatusReporter.shared().currentStatusString
        return view
    }

    override fun celestiaLoadingProgress(status: String) {
        activity?.runOnUiThread {
            Log.d(TAG, "Loading $status")
            loadingLabel?.text = status
        }
    }

    override fun celestiaLoadingSucceeded() {}
    override fun celestiaLoadingFailed() {}

    companion object {
        private const val TAG = "LoadingFragment"

        fun newInstance() = LoadingFragment()
    }

}
