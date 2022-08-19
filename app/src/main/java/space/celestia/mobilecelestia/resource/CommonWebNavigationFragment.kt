package space.celestia.mobilecelestia.resource

import android.net.Uri
import android.os.Bundle
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.utils.getParcelableValue

class CommonWebNavigationFragment: NavigationFragment() {
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = requireArguments().getParcelableValue(ARG_URI, Uri::class.java)!!
    }

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return CommonWebFragment.newInstance(uri)
    }

    override fun canPop(): Boolean {
        val currentTop = top
        if (currentTop is CommonWebFragment && currentTop.canGoBack()) {
            return true
        }
        return super.canPop()
    }

    override fun popLast() {
        val currentTop = top
        if (currentTop is CommonWebFragment && currentTop.canGoBack()) {
            currentTop.goBack()
            return
        }
        super.popLast()
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