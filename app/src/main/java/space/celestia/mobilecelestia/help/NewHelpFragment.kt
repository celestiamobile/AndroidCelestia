package space.celestia.mobilecelestia.help

import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.utils.URLHelper
import space.celestia.mobilecelestia.resource.CommonWebFragment

@AndroidEntryPoint
class NewHelpFragment: CommonWebFragment() {
    override fun createFallbackFragment(): Fragment {
        return HelpFragment.newInstance()
    }

    companion object {
        fun newInstance(): NewHelpFragment {
            return create( { NewHelpFragment() }, URLHelper.buildInAppGuideShortURI("/help/welcome", AppCore.getLanguage(), false), listOf("guide"), null, true)
        }
    }
}