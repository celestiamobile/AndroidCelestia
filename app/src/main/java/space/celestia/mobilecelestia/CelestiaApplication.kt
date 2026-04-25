package space.celestia.mobilecelestia

import android.app.Application
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import space.celestia.celestia.AppCore

@HiltAndroidApp
class CelestiaApplication: Application() {
    private var isFlavorSetUp = false

    override fun onCreate() {
        super.onCreate()

        System.loadLibrary("ziputils")
        System.loadLibrary("celestia")
        AppCore.setUpLocale()

        DynamicColors.applyToActivitiesIfAvailable(this)

        if (!isFlavorSetUp) {
            isFlavorSetUp = true
            setUpFlavor()
        }
    }
}