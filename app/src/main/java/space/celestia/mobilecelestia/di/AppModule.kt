package space.celestia.mobilecelestia.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import space.celestia.celestia.AppCore
import space.celestia.celestia.Renderer
import space.celestia.mobilecelestia.MainActivity
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.FilePaths
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.resource.model.ResourceManager
import space.celestia.mobilecelestia.utils.AppStatusReporter
import space.celestia.mobilecelestia.utils.PreferenceManager
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
        return Retrofit.Builder()
            .baseUrl("https://celestia.mobi/api/resource/")
            .addConverterFactory(GsonConverterFactory.create())
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
}