package space.celestia.celestiaxr

import android.app.Application
import com.google.android.material.color.DynamicColors

class CelestiaApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}