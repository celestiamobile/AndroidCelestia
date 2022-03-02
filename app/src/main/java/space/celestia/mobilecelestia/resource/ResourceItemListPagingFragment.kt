package space.celestia.mobilecelestia.resource

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.resource.model.ResourceCategory
import space.celestia.mobilecelestia.resource.model.ResourceItemListViewModel

@AndroidEntryPoint
class ResourceItemListPagingFragment: AsyncListPagingFragment() {
    override val viewModel by viewModels<ResourceItemListViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null)
            title = viewModel.category.name
    }

    companion object {
        fun newInstance(category: ResourceCategory) =
            ResourceItemListPagingFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ResourceItemListViewModel.ARG_CATEGORY, category)
                }
            }
    }
}