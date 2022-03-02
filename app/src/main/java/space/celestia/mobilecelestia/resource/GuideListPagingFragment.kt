package space.celestia.mobilecelestia.resource

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.resource.model.GuideListViewModel

@AndroidEntryPoint
class GuideListPagingFragment: AsyncListPagingFragment() {
    override val viewModel by viewModels<GuideListViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null)
            title = viewModel.title
    }

    companion object {
        fun newInstance(type: String, title: String) =
            GuideListPagingFragment().apply {
                arguments = Bundle().apply {
                    putString(GuideListViewModel.ARG_TYPE, type)
                    putString(GuideListViewModel.ARG_TITLE, title)
                }
            }
    }
}