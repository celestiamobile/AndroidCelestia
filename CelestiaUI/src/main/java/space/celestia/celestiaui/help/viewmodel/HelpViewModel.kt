package space.celestia.celestiaui.help.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestiaui.di.Flavor
import javax.inject.Inject

@HiltViewModel
class HelpViewModel @Inject constructor(@param:Flavor val flavor: String): ViewModel()