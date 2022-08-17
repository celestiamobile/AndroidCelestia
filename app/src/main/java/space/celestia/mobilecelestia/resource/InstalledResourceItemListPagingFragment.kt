package space.celestia.mobilecelestia.resource

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.resource.model.InstalledResourceItemListViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

@AndroidEntryPoint
class InstalledResourceItemListPagingFragment: AsyncListPagingFragment() {
    override val viewModel by viewModels<InstalledResourceItemListViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Installed", "")
    }

    companion object {
        fun newInstance() = InstalledResourceItemListPagingFragment()
    }
}