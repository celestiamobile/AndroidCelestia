package space.celestia.mobilecelestia.resource

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.resource.model.ResourceCategoryListViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

@AndroidEntryPoint
class ResourceCategoryListPagingFragment: AsyncListPagingFragment() {
    override val viewModel by viewModels<ResourceCategoryListViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            title = CelestiaString("Categories", "")
            rightNavigationBarItems = listOf(
                NavigationFragment.BarButtonItem(
                    MENU_ITEM_MANAGE_INSTALLED,
                    CelestiaString("Installed", "")
                )
            )
        }
    }

    override fun menuItemClicked(groupId: Int, id: Int): Boolean {
        when (id) {
            MENU_ITEM_MANAGE_INSTALLED -> {
                val fragment = InstalledResourceItemListPagingFragment.newInstance()
                (parentFragment as? NavigationFragment)?.pushFragment(fragment)
            } else -> {}
        }
        return true
    }

    companion object {
        const val MENU_ITEM_MANAGE_INSTALLED = 0
        fun newInstance(language: String) = ResourceCategoryListPagingFragment().apply {
            arguments = Bundle().apply {
                putString(ResourceCategoryListViewModel.ARG_LANG, language)
            }
        }
    }
}