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
import space.celestia.mobilecelestia.celestia.SessionSettings
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.resource.model.AddonUpdateManager
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.utils.AppStatusReporter
import space.celestia.mobilecelestia.utils.PreferenceManager
import java.lang.reflect.Type
import java.util.Date
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CoreSettings

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppSettings

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
    fun provideExecutor(renderer: Renderer): CelestiaExecutor {
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
    fun provideDefaultFilePaths(@ApplicationContext context: Context): FilePaths {
        return FilePaths(context)
    }

    @Singleton
    @Provides
    fun provideSessionSettings(): SessionSettings {
        return SessionSettings()
    }
}