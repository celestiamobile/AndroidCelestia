package space.celestia.celestiaxr

import android.app.Application
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import space.celestia.celestia.AppCore

@HiltAndroidApp
class CelestiaApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)
        System.loadLibrary("ziputils")
        System.loadLibrary("celestia")
        AppCore.setUpLocale()
    }
}