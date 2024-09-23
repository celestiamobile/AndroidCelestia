package space.celestia.mobilecelestia

import android.app.Application
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CelestiaApplication: Application() {
    private var isFlavorSetUp = false

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        if (!isFlavorSetUp) {
            isFlavorSetUp = true
            setUpFlavor()
        }
    }
}