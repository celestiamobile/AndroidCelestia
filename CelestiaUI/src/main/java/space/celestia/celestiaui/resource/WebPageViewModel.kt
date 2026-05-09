package space.celestia.celestiaui.resource

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestiaui.resource.model.FeatureFlags
import space.celestia.celestiaui.resource.model.ResourceAPIService
import javax.inject.Inject

@HiltViewModel
class WebPageViewModel @Inject constructor(
    val resourceAPI: ResourceAPIService,
    val featureFlags: FeatureFlags
) : ViewModel()
