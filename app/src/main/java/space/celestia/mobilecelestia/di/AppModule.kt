package space.celestia.mobilecelestia.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideCelestiaLanguage(): String {
        val key = "LANGUAGE"
        val lang = AppCore.getLocalizedString(key, "celestia")
        return if (lang == key) "en" else lang
    }

    @Singleton
    @Provides
    fun provideResourceAPI(): ResourceAPIService {
        return ResourceAPI.shared.create(ResourceAPIService::class.java)
    }
}