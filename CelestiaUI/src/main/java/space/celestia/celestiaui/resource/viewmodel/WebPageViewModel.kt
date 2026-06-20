package space.celestia.celestiaui.resource.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestiaui.resource.model.ResourceAPIService
import javax.inject.Inject

@HiltViewModel
class WebPageViewModel @Inject constructor(
    val resourceAPI: ResourceAPIService
) : ViewModel()