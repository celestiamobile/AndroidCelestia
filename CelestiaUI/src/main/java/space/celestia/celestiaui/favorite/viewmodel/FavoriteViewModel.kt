package space.celestia.celestiaui.favorite.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.favorite.FavoriteBaseItem
import space.celestia.celestiaui.favorite.FavoriteRoot
import java.util.concurrent.Executor
import javax.inject.Inject


sealed class Page {
    data class Item(val item: FavoriteBaseItem) : Page()
    data class Destination(val destination: space.celestia.celestia.Destination) : Page()
}

@HiltViewModel
class FavoriteViewModel @Inject constructor(val appCore: AppCore, val executor: Executor, val favoriteManager: FavoriteManager) : ViewModel() {
    val backStack = mutableStateListOf<Page>(Page.Item(FavoriteRoot()))
}