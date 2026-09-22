package space.celestia.mobilecelestia

import android.app.Application
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.di.AppSettings
import space.celestia.celestiaui.utils.PreferenceManager
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class CelestiaApplication: Application() {
    @AppSettings
    @Inject
    lateinit var appSettings: PreferenceManager

    private var isFlavorSetUp = false
    private var isSentrySetUp = false

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

        if (appSettings[PreferenceManager.PredefinedKey.PrivacyPolicyAccepted] == "true" ||
            Locale.getDefault().country != Locale.CHINA.country) {
            setUpSentryIfNeeded()
        }
    }

    fun setUpSentryIfNeeded() {
        if (isSentrySetUp) return
        setUpSentry()
        isSentrySetUp = true
    }
}