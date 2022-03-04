package space.celestia.mobilecelestia.resource

import android.os.Bundle
import space.celestia.mobilecelestia.common.NavigationFragment

class GuideFragment: NavigationFragment() {
    private lateinit var type: String
    private lateinit var title: String
    private lateinit var language: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        type = requireArguments().getString(ARG_TYPE, "")
        title = requireArguments().getString(ARG_TITLE, "")
        language = requireArguments().getString(ARG_LANG, "en")
    }

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return GuideListPagingFragment.newInstance(type, title, language)
    }

    companion object {
        private const val ARG_TYPE = "type"
        private const val ARG_TITLE = "title"
        private const val ARG_LANG = "lang"

        fun newInstance(type: String, title: String, language: String) = GuideFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TYPE, type)
                putString(ARG_TITLE, title)
                putString(ARG_LANG, language)
            }
        }
    }
}