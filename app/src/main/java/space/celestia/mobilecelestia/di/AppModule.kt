package space.celestia.mobilecelestia.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import space.celestia.celestia.AppCore
import space.celestia.celestia.Renderer
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.resource.model.ResourceManager
import space.celestia.mobilecelestia.utils.AppStatusReporter
import javax.inject.Singleton

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
    fun provideAppStatusReporter(): AppStatusReporter {
        return AppStatusReporter()
    }
}