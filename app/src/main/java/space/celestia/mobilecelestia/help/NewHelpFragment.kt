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
            return create( { NewHelpFragment() }, URLHelper.buildInAppGuideURI("823FB82E-F660-BE54-F3E4-681F5BFD365D", language, false), listOf("guide"), null, true)
        }
    }
}