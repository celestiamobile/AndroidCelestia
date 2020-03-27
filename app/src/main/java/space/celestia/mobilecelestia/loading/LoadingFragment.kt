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
    private var currentText: String? = null

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
        if (currentText != null) {
            loadingLabel?.text = currentText
            currentText = null
        }
        return view
    }

    fun update(status: String) {
        if (loadingLabel == null) {
            currentText = status
        } else {
            loadingLabel?.text = status
        }
    }

    override fun celestiaLoadingProgress(status: String) {
        activity?.runOnUiThread {
            Log.d(TAG, "Loading $status")
            update(status)
        }
    }

    private companion object {
        const val TAG = "LoadingFragment"
    }

}
