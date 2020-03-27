package space.celestia.mobilecelestia.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import space.celestia.mobilecelestia.R

class LoadingFragment : Fragment() {

    private var loadingLabel: TextView? = null
    private var currentText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
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

}
