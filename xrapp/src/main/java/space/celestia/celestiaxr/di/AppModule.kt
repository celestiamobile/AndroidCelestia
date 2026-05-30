package space.celestia.celestiaxr.di

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import space.celestia.celestia.AppCore
import space.celestia.celestia.XRRenderer
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.celestiafoundation.utils.FilePaths
import space.celestia.celestiafoundation.utils.versionName
import space.celestia.celestiaui.control.viewmodel.SessionSettings
import space.celestia.celestiaui.di.AppSettings
import space.celestia.celestiaui.di.AppSettingsNoBackup
import space.celestia.celestiaui.di.ApplicationId
import space.celestia.celestiaui.di.CoreSettings
import space.celestia.celestiaui.di.Platform
import space.celestia.celestiaui.favorite.viewmodel.FavoriteManager
import space.celestia.celestiaui.pushnotification.UserAPIService
import space.celestia.celestiaui.resource.model.AddonUpdateManager
import space.celestia.celestiaui.resource.model.FeatureFlags
import space.celestia.celestiaui.resource.model.FeatureFlagsManager
import space.celestia.celestiaui.resource.model.ResourceAPIService
import space.celestia.celestiaui.settings.viewmodel.SettingsEntryProvider
import space.celestia.celestiaui.utils.AppStatusReporter
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.celestiaxr.BuildConfig
import space.celestia.celestiaxr.common.CelestiaExecutor
import space.celestia.celestiaxr.settings.RenderSettings
import space.celestia.celestiaxr.settings.SettingsEntryProviderImpl
import java.lang.reflect.Type
import java.util.Date
import java.util.concurrent.Executor
import javax.inject.Singleton
import kotlin.Boolean

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private val json = Json { ignoreUnknownKeys = true }

    @Singleton
    @Provides
    fun provideResourceAPI(): ResourceAPIService {
        return Retrofit.Builder()
            .baseUrl("https://celestia.mobi/api/2/resource/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ResourceAPIService::class.java)
    }

    @Singleton
    @Provides
    fun provideUserAPI(): UserAPIService {
        return Retrofit.Builder()
            .baseUrl("https://celestia.mobi/api/2/users/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UserAPIService::class.java)
    }

    @Singleton
    @Provides
    fun provideResourceManager(): ResourceManager {
        return ResourceManager()
    }

    @Singleton
    @Provides
    fun provideAddonUpdateManager(resourceAPI: ResourceAPIService, resourceManager: ResourceManager): AddonUpdateManager {
        return AddonUpdateManager(resourceManager = resourceManager, resourceAPI = resourceAPI)
    }

    @Singleton
    @Provides
    fun provideAppCore(): AppCore {
        return AppCore()
    }

    @Singleton
    @Provides
    fun provideXRRenderer(appCore: AppCore): XRRenderer {
        val renderer = XRRenderer()
        renderer.setAppCore(appCore)
        return renderer
    }

    @Singleton
    @Provides
    fun provideExecutor(xrRenderer: XRRenderer): Executor {
        return CelestiaExecutor(xrRenderer)
    }

    @Singleton
    @Provides
    fun provideAppStatusReporter(): AppStatusReporter {
        return AppStatusReporter()
    }
    @Singleton
    @Provides
    @CoreSettings
    fun provideCoreSettings(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context, "celestia_setting")
    }

    @Singleton
    @Provides
    @AppSettings
    fun provideAppSettings(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context, "celestia")
    }

    @Singleton
    @Provides
    @AppSettingsNoBackup
    fun provideAppSettingsNoBackup(@ApplicationContext context: Context, @AppSettings appSettings: PreferenceManager): PreferenceManager {
        val settings = PreferenceManager(context, "celestia_no_backup")
        // Migration
        if (settings[PreferenceManager.PredefinedKey.LocalSettingMigration] != "true") {
            settings.startEditing()
            settings[PreferenceManager.PredefinedKey.BoldFontIndex] = appSettings[PreferenceManager.PredefinedKey.BoldFontIndex]
            settings[PreferenceManager.PredefinedKey.BoldFontPath] = appSettings[PreferenceManager.PredefinedKey.BoldFontPath]
            settings[PreferenceManager.PredefinedKey.NormalFontIndex] = appSettings[PreferenceManager.PredefinedKey.NormalFontIndex]
            settings[PreferenceManager.PredefinedKey.NormalFontPath] = appSettings[PreferenceManager.PredefinedKey.NormalFontPath]
            settings[PreferenceManager.PredefinedKey.ConfigFilePath] = appSettings[PreferenceManager.PredefinedKey.ConfigFilePath]
            settings[PreferenceManager.PredefinedKey.DataDirPath] = appSettings[PreferenceManager.PredefinedKey.DataDirPath]
            settings[PreferenceManager.PredefinedKey.MigrationSourceDirectory] = appSettings[PreferenceManager.PredefinedKey.MigrationSourceDirectory]
            settings[PreferenceManager.PredefinedKey.MigrationTargetDirectory] = appSettings[PreferenceManager.PredefinedKey.MigrationTargetDirectory]
            settings[PreferenceManager.PredefinedKey.LocalSettingMigration] = "true"
            settings.stopEditing()
        }
        return settings
    }

    @Singleton
    @Provides
    fun provideDefaultFilePaths(@ApplicationContext context: Context): FilePaths {
        return FilePaths(context)
    }

    @Singleton
    @Provides
    fun provideSessionSettings(): SessionSettings {
        return SessionSettings(isGyroscopeSupported = false)
    }

    @Singleton
    @Provides
    @ApplicationId
    fun provideApplicationId(): String {
        return BuildConfig.APPLICATION_ID
    }

    @Singleton
    @Provides
    fun providePlatform(): Platform {
        return Platform(name = "quest", flavor = null)
    }

    @Singleton
    @Provides
    fun provideSettingsEntryProvider(): SettingsEntryProvider {
        return SettingsEntryProviderImpl()
    }

    @Singleton
    @Provides
    @AlertMessage
    fun provideAlertMessage() = mutableStateOf<String?>(null)

    @Singleton
    @Provides
    @PanelState
    fun providePanelState() = mutableStateOf(false)

    @Singleton
    @Provides
    fun providerRenderSettings(@AppSettings appSettings: PreferenceManager): RenderSettings {
        val resolutionMultiplierValue = appSettings[PreferenceManager.PredefinedKey.ResolutionMultiplier]?.toIntOrNull()
        val resolutionMultiplier = if (resolutionMultiplierValue != null && listOf(1, 2, 4).contains(resolutionMultiplierValue)) {
            resolutionMultiplierValue
        } else {
            1
        }
        return RenderSettings(enableMultisample = appSettings[PreferenceManager.PredefinedKey.MSAA] == "true", resolutionMultiplier = resolutionMultiplier, enableSRGBRendering = appSettings[PreferenceManager.PredefinedKey.SRGBRendering] == "true", enableMixedImmersion = appSettings[PreferenceManager.PredefinedKey.MixedImmersion] == "true")
    }

    @Singleton
    @Provides
    fun provideFavoriteManager(@ApplicationContext context: Context, appCore: AppCore): FavoriteManager {
        return FavoriteManager("${context.filesDir.absolutePath}/favorites.json", appCore)
    }

    @Singleton
    @Provides
    fun provideFeatureFlagsManager(@ApplicationContext context: Context, resourceAPI: ResourceAPIService, @AppSettingsNoBackup appSettings: PreferenceManager, platform: Platform): FeatureFlagsManager {
        return FeatureFlagsManager(resourceAPI = resourceAPI, preferenceManager = appSettings, platform = platform.name, distribution = null, version = context.versionName)
    }

    @Singleton
    @Provides
    fun provideFeatureFlags(featureFlagsManager: FeatureFlagsManager): FeatureFlags {
        return featureFlagsManager.get()
    }
}