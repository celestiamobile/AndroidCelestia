package space.celestia.celestiaui.help.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.di.Flavor
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
class HelpViewModel @Inject constructor(@param:Flavor val flavor: String, val appCore: AppCore, val executor: Executor): ViewModel()