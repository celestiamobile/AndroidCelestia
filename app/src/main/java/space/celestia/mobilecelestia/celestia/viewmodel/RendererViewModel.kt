package space.celestia.mobilecelestia.celestia.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestia.Renderer
import space.celestia.celestiafoundation.utils.FilePaths
import space.celestia.celestiaui.di.AppSettings
import space.celestia.celestiaui.di.AppSettingsNoBackup
import space.celestia.celestiaui.purchase.PurchaseManager
import space.celestia.celestiaui.utils.AppStatusReporter
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.mobilecelestia.celestia.RendererSettings
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
class RendererViewModel @Inject constructor(
    val appCore: AppCore,
    val appStatusReporter: AppStatusReporter,
    val renderer: Renderer,
    val rendererSettings: RendererSettings,
    val executor: Executor,
    @param:AppSettings val appSettings: PreferenceManager,
    @param:AppSettingsNoBackup val appSettingsNoBackup: PreferenceManager,
    val purchaseManager: PurchaseManager,
    val defaultFilePaths: FilePaths
): ViewModel()