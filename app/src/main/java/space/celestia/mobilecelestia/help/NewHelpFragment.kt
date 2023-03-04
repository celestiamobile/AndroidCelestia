package space.celestia.mobilecelestia.help

import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.resource.CommonWebFragment
import space.celestia.mobilecelestia.utils.URLHelper

@AndroidEntryPoint
class NewHelpFragment: CommonWebFragment() {
    override fun createFallbackFragment(): Fragment {
        return HelpFragment.newInstance()
    }

    companion object {
        fun newInstance(language: String): NewHelpFragment {
            return create( { NewHelpFragment() }, URLHelper.buildInAppGuideShortURI("/help/welcome", language, false), listOf("guide"), null, true)
        }
    }
}