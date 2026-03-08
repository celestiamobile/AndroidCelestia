package space.celestia.celestiaxr.di

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dagger.Module
import kotlinx.coroutines.flow.MutableSharedFlow
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import space.celestia.celestia.AppCore
import space.celestia.celestia.XRRenderer
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.celestiafoundation.utils.FilePaths
import space.celestia.celestiaui.control.viewmodel.SessionSettings
import space.celestia.celestiaui.di.AppSettings
import space.celestia.celestiaui.di.AppSettingsNoBackup
import space.celestia.celestiaui.di.ApplicationId
import space.celestia.celestiaui.di.CoreSettings
import space.celestia.celestiaui.di.Platform
import space.celestia.celestiaui.resource.model.AddonUpdateManager
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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideResourceAPI(): ResourceAPIService {
        class DateAdapter : JsonDeserializer<Date> {
            override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
            ): Date {
                try {
                    val seconds = json?.asDouble ?: return Date()
                    return Date((seconds * 1000.0).toLong())
                } catch(e: Throwable) {
                    throw JsonParseException(e)
                }
            }
        }

        val gson = GsonBuilder().registerTypeAdapter(Date::class.java, DateAdapter()).create()
        return Retrofit.Builder()
            .baseUrl("https://celestia.mobi/api/2/resource/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ResourceAPIService::class.java)
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
        appCore.setXRRenderer(renderer)
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
        return RenderSettings(enableMultisample = appSettings[PreferenceManager.PredefinedKey.MSAA] == "true", resolutionMultiplier = resolutionMultiplier)
    }
}