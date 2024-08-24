package space.celestia.mobilecelestia.resource

import android.net.Uri
import android.os.Bundle
import androidx.core.os.BundleCompat
import space.celestia.mobilecelestia.common.NavigationFragment

class CommonWebNavigationFragment: NavigationFragment() {
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = BundleCompat.getParcelable(requireArguments(), ARG_URI, Uri::class.java)!!
    }

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return CommonWebFragment.newInstance(uri)
    }

    companion object {
        private const val ARG_URI = "uri"

        fun newInstance(uri: Uri) = CommonWebNavigationFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_URI, uri)
            }
        }
    }
}