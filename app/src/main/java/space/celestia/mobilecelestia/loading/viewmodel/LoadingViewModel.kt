package space.celestia.mobilecelestia.loading.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestiaui.utils.AppStatusReporter
import javax.inject.Inject


@HiltViewModel
class LoadingViewModel @Inject constructor(val appStatusReporter: AppStatusReporter) : ViewModel()