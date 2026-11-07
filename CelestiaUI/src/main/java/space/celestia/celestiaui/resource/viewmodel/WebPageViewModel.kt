package space.celestia.celestiaui.resource.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestiaui.resource.model.ResourceAPIService
import javax.inject.Inject

@HiltViewModel
class WebPageViewModel @Inject constructor(
    val resourceAPI: ResourceAPIService
) : ViewModel() {
    // WebView history can exceed Binder limits; retain it only in memory.
    var savedWebViewState: Bundle? = null
}