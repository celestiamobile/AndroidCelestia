package space.celestia.mobilecelestia.di

import android.content.Context
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
import space.celestia.celestia.Renderer
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.celestiafoundation.utils.FilePaths
import space.celestia.celestiafoundation.utils.versionName
import space.celestia.celestiaui.di.ApplicationId
import space.celestia.mobilecelestia.celestia.viewmodel.RendererSettings
import space.celestia.celestiaui.control.viewmodel.SessionSettings
import space.celestia.celestiaui.di.AppSettings
import space.celestia.celestiaui.di.AppSettingsNoBackup
import space.celestia.celestiaui.di.CoreSettings
import space.celestia.celestiaui.di.Platform
import space.celestia.celestiaui.favorite.viewmodel.FavoriteManager
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.EdgeInsets
import space.celestia.celestiaui.resource.model.AddonUpdateManager
import space.celestia.celestiaui.resource.model.FeatureFlags
import space.celestia.celestiaui.resource.model.FeatureFlagsManager
import space.celestia.celestiaui.resource.model.ResourceAPIService
import space.celestia.celestiaui.settings.viewmodel.SettingsEntryProvider
import space.celestia.celestiaui.utils.AppStatusReporter
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.mobilecelestia.BuildConfig
import space.celestia.celestiaui.pushnotification.UserAPIService
import space.celestia.mobilecelestia.settings.SettingsEntryProviderImpl
import java.util.concurrent.Executor
import javax.inject.Singleton

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
    fun provideFeatureFlagsManager(@ApplicationContext context: Context, resourceAPI: ResourceAPIService, @AppSettingsNoBackup appSettings: PreferenceManager, platform: Platform): FeatureFlagsManager {
        return FeatureFlagsManager(resourceAPI = resourceAPI, preferenceManager = appSettings, platform = platform.name, distribution = platform.flavor, version = context.versionName)
    }

    @Singleton
    @Provides
    fun provideFeatureFlags(featureFlagsManager: FeatureFlagsManager): FeatureFlags {
        return featureFlagsManager.get()
    }

    @Singleton
    @Provides
    fun provideAppCore(): AppCore {
        return AppCore()
    }

    @Singleton
    @Provides
    fun provideRenderer(appCore: AppCore): Renderer {
        val renderer = Renderer()
        renderer.setAppCore(appCore)
        return renderer
    }

    @Singleton
    @Provides
    fun provideRendererSettings(@AppSettings appSettings: PreferenceManager): RendererSettings {
        return RendererSettings(
            density = 0.0f,
            fontScale = 0.0f,
            safeAreaInsets = EdgeInsets(),
            frameRateOption = appSettings[PreferenceManager.PredefinedKey.FrameRateOption]?.toIntOrNull() ?: Renderer.FRAME_60FPS,
            enableFullResolution = appSettings[PreferenceManager.PredefinedKey.FullDPI] != "false", // default on
            enableMultisample = appSettings[PreferenceManager.PredefinedKey.MSAA] == "true",
            enableSRGBRendering = appSettings[PreferenceManager.PredefinedKey.SRGBRendering] == "true",
            pickSensitivity = appSettings[PreferenceManager.PredefinedKey.PickSensitivity]?.toFloatOrNull() ?: 10.0f
        )
    }

    @Singleton
    @Provides
    fun provideExecutor(renderer: Renderer): Executor {
        return CelestiaExecutor(renderer)
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
        if (appSettings[PreferenceManager.PredefinedKey.OnboardMessage] == "true") {
            appSettings.startEditing()
            settings.startEditing()
            appSettings[PreferenceManager.PredefinedKey.OnboardMessage] = null
            settings[PreferenceManager.PredefinedKey.OnboardMessage] = "true"
            appSettings.stopEditing()
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
        return SessionSettings(isGyroscopeSupported = true)
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
        return Platform(name = "android", flavor = BuildConfig.FLAVOR)
    }

    @Singleton
    @Provides
    fun provideSettingsEntryProvider(featureFlags: FeatureFlags): SettingsEntryProvider {
        return SettingsEntryProviderImpl(featureFlags)
    }

    @Singleton
    @Provides
    fun provideFavoriteManager(@ApplicationContext context: Context, appCore: AppCore): FavoriteManager {
        return FavoriteManager("${context.filesDir.absolutePath}/favorites.json", appCore)
    }
}
