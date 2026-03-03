package space.celestia.mobilecelestia.di

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import space.celestia.celestia.AppCore
import space.celestia.celestia.Renderer
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.celestiafoundation.utils.FilePaths
import space.celestia.celestiaui.di.ApplicationId
import space.celestia.mobilecelestia.celestia.RendererSettings
import space.celestia.celestiaui.control.viewmodel.SessionSettings
import space.celestia.celestiaui.di.AppSettings
import space.celestia.celestiaui.di.AppSettingsNoBackup
import space.celestia.celestiaui.di.CoreSettings
import space.celestia.celestiaui.di.Flavor
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.EdgeInsets
import space.celestia.celestiaui.resource.model.AddonUpdateManager
import space.celestia.celestiaui.resource.model.ResourceAPIService
import space.celestia.celestiaui.settings.viewmodel.SettingsEntryProvider
import space.celestia.celestiaui.utils.AppStatusReporter
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.mobilecelestia.BuildConfig
import space.celestia.mobilecelestia.settings.SettingsEntryProviderImpl
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
    fun provideRenderer(): Renderer {
        return Renderer()
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
        return SessionSettings()
    }

    @Singleton
    @Provides
    @ApplicationId
    fun provideApplicationId(): String {
        return BuildConfig.APPLICATION_ID
    }

    @Singleton
    @Provides
    @Flavor
    fun provideFlavor(): String {
        return BuildConfig.FLAVOR
    }

    @Singleton
    @Provides
    fun provideSettingsEntryProvider(): SettingsEntryProvider {
        return SettingsEntryProviderImpl()
    }
}