package space.celestia.mobilecelestia.loading

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import space.celestia.mobilecelestia.R

class LoadingFragment : Fragment() {

    private var loadingLabel: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_loading, container, false)
        loadingLabel = view.findViewById(R.id.loading_label)
        return view
    }

    public fun update(status: String) {
        loadingLabel?.text = status
    }

}
