package space.celestia.celestiaxr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.lifecycleScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import space.celestia.celestia.AppCore
import space.celestia.celestia.XRRenderer
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.celestiafoundation.utils.AssetUtils
import space.celestia.celestiafoundation.utils.FilePaths
import space.celestia.celestiafoundation.utils.deleteRecursively
import space.celestia.celestiaui.control.viewmodel.JoystickAction
import space.celestia.celestiaui.control.viewmodel.JoystickHandler
import space.celestia.celestiaui.di.AppSettings
import space.celestia.celestiaui.di.AppSettingsNoBackup
import space.celestia.celestiaui.di.CoreSettings
import space.celestia.celestiaui.settings.viewmodel.CustomFont
import space.celestia.celestiaui.settings.viewmodel.SettingsKey
import space.celestia.celestiaui.utils.AppStatusReporter
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.celestiaxr.di.AlertMessage
import space.celestia.celestiaxr.di.PanelState
import space.celestia.celestiaxr.home.HomeActivity
import space.celestia.celestiaxr.settings.RenderSettings
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.Locale
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class XRActivity : ComponentActivity() {

    @Inject
    lateinit var appCore: AppCore

    @Inject
    lateinit var xrRenderer: XRRenderer

    @Inject
    lateinit var executor: Executor

    @Inject
    lateinit var defaultFilePaths: FilePaths

    @AlertMessage
    @Inject
    lateinit var alertMessage: MutableState<String?>

    @PanelState
    @Inject
    lateinit var panelState: MutableState<Boolean>

    private var panelStateObservation: Job? = null

    lateinit var appStatusReporter: AppStatusReporter

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppStatusInterface {
        fun getAppStatusReporter(): AppStatusReporter
    }

    @AppSettingsNoBackup
    @Inject
    lateinit var appSettingsNoBackup: PreferenceManager

    @AppSettings
    @Inject
    lateinit var appSettings: PreferenceManager

    @CoreSettings
    @Inject
    lateinit var coreSettings: PreferenceManager

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var renderSettings: RenderSettings

    private val celestiaConfigFilePath: String
        get() = appSettingsNoBackup[PreferenceManager.PredefinedKey.ConfigFilePath] ?: defaultFilePaths.configFilePath

    private val celestiaDataDirPath: String
        get() = appSettingsNoBackup[PreferenceManager.PredefinedKey.DataDirPath] ?: defaultFilePaths.dataDirectoryPath

    private val fontDirPath: String
        get() = defaultFilePaths.fontDirectoryPath

    private class Fonts(val regular: CustomFont, val bold: CustomFont)

    override fun onCreate(savedInstanceState: Bundle?) {
        val factory = EntryPointAccessors.fromApplication(this, AppStatusInterface::class.java)
        appStatusReporter = factory.getAppStatusReporter()
        val currentState = appStatusReporter.state
        val savedState = if (currentState == AppStatusReporter.State.NONE) null else savedInstanceState

        super.onCreate(savedState)

        val weakSelf = WeakReference(this)
        appStatusReporter.register(object: AppStatusReporter.Listener {
            override fun celestiaLoadingProgress(status: String) {}

            override fun celestiaLoadingStateChanged(newState: AppStatusReporter.State) {
                val self = weakSelf.get() ?: return
                when (newState) {
                    AppStatusReporter.State.FINISHED -> {
                        self.celestiaLoadingFinishedAsync()
                    }
                    AppStatusReporter.State.LOADING_SUCCESS -> {
                        self.celestiaLoadingSucceeded()
                    }
                    AppStatusReporter.State.EXTERNAL_LOADING_FAILURE, AppStatusReporter.State.LOADING_FAILURE -> {
                        self.celestiaLoadingFailed()
                    }
                    else -> {}
                }
            }
        })

        if (currentState == AppStatusReporter.State.LOADING_FAILURE || currentState == AppStatusReporter.State.EXTERNAL_LOADING_FAILURE) {
            celestiaLoadingFailed()
            return
        }

        when (currentState) {
            AppStatusReporter.State.NONE, AppStatusReporter.State.EXTERNAL_LOADING -> {
                loadExternalConfig()
            }
            AppStatusReporter.State.LOADING -> {
                // Do nothing
            }
            AppStatusReporter.State.LOADING_SUCCESS -> {
                celestiaLoadingSucceeded()
            }
            AppStatusReporter.State.FINISHED -> {
                celestiaLoadingFinished()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        xrRenderer.resume()
        openHomePanelIfNeeded()
    }

    override fun onStop() {
        xrRenderer.pause()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

        panelStateObservation?.cancel()
        exitProcess(0)
    }

    private fun copyAssetIfNeeded() {
        appStatusReporter.updateStatus(CelestiaString("Copying data…", "Copying default data from APK"))
        if (appSettingsNoBackup[PreferenceManager.PredefinedKey.DataVersion] != CURRENT_DATA_VERSION) {
            // When version name does not match, copy the asset again
            copyAssetsAndRemoveOldAssets()
        }
    }

    @Throws(IOException::class)
    private fun copyAssetsAndRemoveOldAssets() {
        try {
            File(defaultFilePaths.dataDirectoryPath).deleteRecursively()
            File(defaultFilePaths.fontDirectoryPath).deleteRecursively()
        } catch (_: Exception) {}
        AssetUtils.copyFileOrDir(this@XRActivity, FilePaths.CELESTIA_DATA_FOLDER_NAME, defaultFilePaths.parentDirectoryPath)
        AssetUtils.copyFileOrDir(this@XRActivity, FilePaths.CELESTIA_FONT_FOLDER_NAME, defaultFilePaths.parentDirectoryPath)
        appSettingsNoBackup[PreferenceManager.PredefinedKey.DataVersion] = CURRENT_DATA_VERSION
    }

    private fun loadExternalConfig() {
        appStatusReporter.updateState(AppStatusReporter.State.EXTERNAL_LOADING)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                copyAssetIfNeeded()
                if (!isActive) return@launch
                createAddonFolder()
                if (!isActive) return@launch
                loadConfig()
                if (!isActive) return@launch
                withContext(Dispatchers.Main) {
                    loadConfigSuccess()
                }
            } catch (error: Throwable) {
                withContext(Dispatchers.Main) {
                    appStatusReporter.updateState(AppStatusReporter.State.EXTERNAL_LOADING_FAILURE)
                    loadConfigFailed(error)
                }
            }
        }
    }

    private fun loadConfig() {
        availableInstalledFonts = mapOf(
            "ja" to Fonts(
                CustomFont("$fontDirPath/NotoSansCJK-Regular.ttc", 0),
                CustomFont("$fontDirPath/NotoSansCJK-Bold.ttc", 0)
            ),
            "ka" to Fonts(
                CustomFont("$fontDirPath/NotoSansGeorgian-Regular.ttf", 0),
                CustomFont("$fontDirPath/NotoSansGeorgian-Bold.ttf", 0)
            ),
            "ko" to Fonts(
                CustomFont("$fontDirPath/NotoSansCJK-Regular.ttc", 1),
                CustomFont("$fontDirPath/NotoSansCJK-Bold.ttc", 1)
            ),
            "zh_CN" to Fonts(
                CustomFont("$fontDirPath/NotoSansCJK-Regular.ttc", 2),
                CustomFont("$fontDirPath/NotoSansCJK-Bold.ttc", 2)
            ),
            "zh_TW" to Fonts(
                CustomFont("$fontDirPath/NotoSansCJK-Regular.ttc", 3),
                CustomFont("$fontDirPath/NotoSansCJK-Bold.ttc", 3)
            ),
            "ar" to Fonts(
                CustomFont("$fontDirPath/NotoSansArabic-Regular.ttf", 0),
                CustomFont("$fontDirPath/NotoSansArabic-Bold.ttf", 0)
            )
        )
        defaultInstalledFont = Fonts(
            CustomFont("$fontDirPath/NotoSans-Regular.ttf", 0),
            CustomFont("$fontDirPath/NotoSans-Bold.ttf", 0)
        )

         language = getString(R.string.celestia_language)
    }

    private fun loadConfigSuccess() {
        xrRenderer.setEngineStartedListener { _, resolutionMultiplier ->
            return@setEngineStartedListener initCelestia(resolutionMultiplier)
        }
        xrRenderer.setControllerButtonListener { button, up ->
            when (val action = JoystickHandler.joystickButtonKeyAction(button, appSettings)) {
                is JoystickAction.Key.Celestia -> {
                    action.invoke(appCore, up)
                }
                JoystickAction.Key.None -> {}
                JoystickAction.Key.ShowMenu -> {
                    if (up) {
                        lifecycleScope.launch {
                            openHomePanelIfNeeded()
                        }
                    }
                }
            }
        }
        xrRenderer.setJoystickAxisListener { axis, value ->
            val shouldInvertX = appSettings[PreferenceManager.PredefinedKey.ControllerInvertX] == "true"
            val shouldInvertY = appSettings[PreferenceManager.PredefinedKey.ControllerInvertY] == "true"
            when (axis) {
                AppCore.JOYSTICK_AXIS_X -> {
                    if (appSettings[PreferenceManager.PredefinedKey.ControllerEnableLeftThumbstick] != "false")
                        appCore.joystickAxis(axis, if (shouldInvertX) -value else value)
                }
                AppCore.JOYSTICK_AXIS_Y -> {
                    if (appSettings[PreferenceManager.PredefinedKey.ControllerEnableLeftThumbstick] != "false")
                        appCore.joystickAxis(axis, if (shouldInvertY) value else -value)
                }
                AppCore.JOYSTICK_AXIS_RX -> {
                    if (appSettings[PreferenceManager.PredefinedKey.ControllerEnableRightThumbstick] != "false")
                        appCore.joystickAxis(axis, if (shouldInvertX) -value else value)
                }
                AppCore.JOYSTICK_AXIS_RY -> {
                    if (appSettings[PreferenceManager.PredefinedKey.ControllerEnableRightThumbstick] != "false")
                        appCore.joystickAxis(axis, if (shouldInvertY) value else -value)
                }
            }
        }
        val appFont = (availableInstalledFonts[language] ?: defaultInstalledFont)?.bold
        xrRenderer.startConditionally(this, renderSettings.enableMultisample, renderSettings.resolutionMultiplier, appFont?.path, appFont?.ttcIndex ?: 0)
    }

    private fun openHomePanelIfNeeded() {
        if (!panelState.value) {
            if (isDestroyed || isFinishing) return

            val panelIntent = Intent(this@XRActivity, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(panelIntent)
        }
    }

    private fun loadConfigFailed(error: Throwable) {
        Log.e(TAG, "Initialization failed, $error")
        alertMessage.value = error.message ?: CelestiaString("Unknown error", "")
    }

    private fun celestiaLoadingFailed() {
        appStatusReporter.updateStatus(CelestiaString("Loading Celestia failed…", "Celestia loading failed"))
    }

    private fun celestiaLoadingSucceeded() = lifecycleScope.launch(executor.asCoroutineDispatcher()) {
        readSettings()
        appStatusReporter.updateState(AppStatusReporter.State.FINISHED)
    }

    private fun celestiaLoadingFinishedAsync() {
        lifecycleScope.launch {
            celestiaLoadingFinished()
        }
    }

    private fun readDefaultSetting(): Map<String, Any> {
        try {
            val jsonFileContent = AssetUtils.readFileToText(this, "defaults.json")
            val json = JSONObject(jsonFileContent)
            val map = HashMap<String, Any>()
            for (key in json.keys()) {
                map[key] = json[key]
            }
            return map
        } catch (_: Throwable) {}
        return mapOf()
    }

    private fun readSettings() {
        val map = readDefaultSetting()
        val bools = HashMap<String, Boolean>()
        val ints = HashMap<String, Int>()
        val doubles = HashMap<String, Double>()

        fun getDefaultInt(key: String): Int? {
            val value = map[key]
            if (value is Int) {
                return value
            }
            return null
        }

        fun getDefaultDouble(key: String): Double? {
            val value = map[key]
            if (value is Double) {
                return value
            }
            return null
        }

        fun getDefaultBool(key: String): Boolean? {
            val value = getDefaultInt(key)
            if (value != null) {
                if (value == 1) { return true }
                if (value == 0) { return false }
            }
            return null
        }

        fun getCustomInt(key: String): Int? {
            val value = coreSettings[PreferenceManager.CustomKey(key)] ?: return null
            return try {
                value.toInt()
            } catch (_: NumberFormatException) {
                null
            }
        }

        fun getCustomBool(key: String): Boolean? {
            val value = getCustomInt(key)
            if (value != null) {
                if (value == 1) { return true }
                if (value == 0) { return false }
            }
            return null
        }

        fun getCustomDouble(key: String): Double? {
            val value = coreSettings[PreferenceManager.CustomKey(key)] ?: return null
            return try {
                value.toDouble()
            } catch (_: NumberFormatException) {
                null
            }
        }

        for (key in SettingsKey.allBooleanCases) {
            val def = getDefaultBool(key.valueString)
            if (def != null) {
                bools[key.valueString] = def
            }
            val cus = getCustomBool(key.valueString)
            if (cus != null)
                bools[key.valueString] = cus
        }

        for (key in SettingsKey.allIntCases) {
            val def = getDefaultInt(key.valueString)
            if (def != null) {
                ints[key.valueString] = def
            }
            val cus = getCustomInt(key.valueString)
            if (cus != null)
                ints[key.valueString] = cus
        }

        for (key in SettingsKey.allDoubleCases) {
            val def = getDefaultDouble(key.valueString)
            if (def != null) {
                doubles[key.valueString] = def
            }
            val cus = getCustomDouble(key.valueString)
            if (cus != null)
                doubles[key.valueString] = cus
        }

        for ((key, value) in bools) {
            appCore.setBooleanValueForField(key, value)
        }

        for ((key, value) in ints) {
            appCore.setIntValueForField(key, value)
        }

        for ((key, value) in doubles) {
            appCore.setDoubleValueForField(key, value)
        }
    }

    private fun celestiaLoadingFinished() {
        resourceManager.addonDirectory = addonPaths.firstOrNull()
        resourceManager.scriptDirectory = extraScriptPaths.firstOrNull()

        panelStateObservation = lifecycleScope.launch {
            snapshotFlow { panelState.value }.collect { visible ->
                withContext(executor.asCoroutineDispatcher()) {
                    if (visible) {
                        xrRenderer.clearOverlayMessage()
                    } else {
                        xrRenderer.showOverlayMessage("Tap the menu button to open overlay")
                    }
                }
            }
        }
    }

    private fun createAddonFolder() {
        val dataAddonDir = getExternalFilesDir(CELESTIA_EXTRA_FOLDER_NAME)
        val dataScriptDir = getExternalFilesDir(CELESTIA_SCRIPT_FOLDER_NAME)

        val mediaAddonDir: File?
        val mediaScriptDir: File?
        val mediaDir = externalMediaDirs.firstOrNull()
        if (mediaDir != null) {
            mediaAddonDir = File(mediaDir, CELESTIA_EXTRA_FOLDER_NAME)
            mediaScriptDir = File(mediaDir, CELESTIA_SCRIPT_FOLDER_NAME)
        } else {
            mediaAddonDir = null
            mediaScriptDir = null
        }

        val addonDirs = if (appSettings[PreferenceManager.PredefinedKey.UseMediaDirForAddons] == "true") listOf(mediaAddonDir, dataAddonDir) else listOf(dataAddonDir, mediaAddonDir)
        val scriptDirs = if (appSettings[PreferenceManager.PredefinedKey.UseMediaDirForAddons] == "true") listOf(mediaScriptDir, dataScriptDir) else listOf(dataScriptDir, mediaScriptDir)
        addonPaths = createDirectoriesIfNeeded(addonDirs.mapNotNull { it })
        extraScriptPaths = createDirectoriesIfNeeded(scriptDirs.mapNotNull { it })
    }

    private fun createDirectoriesIfNeeded(dirs: List<File>): List<String> {
        val availablePaths = ArrayList<String>()
        for (dir in dirs) {
            try {
                if (dir.exists() || dir.mkdirs()) {
                    availablePaths.add(dir.absolutePath)
                }
            } catch (_: Throwable) {}
        }
        return availablePaths
    }

    private fun initCelestia(resolutionMultiplier: Float): Boolean {
        appStatusReporter.updateState(AppStatusReporter.State.LOADING)

        val data = celestiaDataDirPath
        val cfg = celestiaConfigFilePath
        val addonDirs = addonPaths.toTypedArray()

        AppCore.initGL()
        AppCore.chdir(data)

        val countryCode = Locale.getDefault().country
        // Set up locale
        AppCore.setLocaleDirectoryPath("$data/locale", language, countryCode)

        if (!appCore.startSimulation(cfg, addonDirs, appStatusReporter)) {
            val fallbackConfigPath = defaultFilePaths.configFilePath
            val fallbackDataPath = defaultFilePaths.dataDirectoryPath
            if (fallbackConfigPath != cfg || fallbackDataPath != data) {
                lifecycleScope.launch {
                    alertMessage.value = CelestiaString("Error loading data, fallback to original configuration.", "")
                }
                AppCore.chdir(fallbackDataPath)
                AppCore.setLocaleDirectoryPath("$fallbackDataPath/locale", language, countryCode)
                if (!appCore.startSimulation(fallbackConfigPath, addonDirs, appStatusReporter)) {
                    appStatusReporter.updateState(AppStatusReporter.State.LOADING_FAILURE)
                    return false
                }
            } else {
                appStatusReporter.updateState(AppStatusReporter.State.LOADING_FAILURE)
                return false
            }
        }

        if (!appCore.startRenderer()) {
            appStatusReporter.updateState(AppStatusReporter.State.LOADING_FAILURE)
            return false
        }

        appCore.screenDPI = (appCore.screenDPI * resolutionMultiplier).toInt()

        val preferredInstalledFont = availableInstalledFonts[language] ?: defaultInstalledFont
        val normalFont = preferredInstalledFont?.regular
        val boldFont = preferredInstalledFont?.bold
        if (normalFont != null) {
            appCore.setFont(normalFont.path, normalFont.ttcIndex, 9)
            appCore.setRendererFont(
                normalFont.path,
                normalFont.ttcIndex,
                9,
                AppCore.RENDER_FONT_STYLE_NORMAL
            )
        }
        if (boldFont != null) {
            appCore.setTitleFont(
                boldFont.path,
                boldFont.ttcIndex,
                15
            )
            appCore.setRendererFont(
                boldFont.path,
                boldFont.ttcIndex,
                15,
                AppCore.RENDER_FONT_STYLE_LARGE
            )
        }

        appCore.hudDetail = 0
        appCore.setHudMessagesEnabled(false)
        appCore.disableSelectionPointer()
        appCore.setHudOverlayImageEnabled(false)

        appCore.tick()
        appCore.start()

        appStatusReporter.updateState(AppStatusReporter.State.LOADING_SUCCESS)
        return true
    }

    companion object {
        private const val CURRENT_DATA_VERSION = "133"
        // 133 1.9.10, Localization update data update (ab865c0979679cafdd962d2d726e819acbc26cb0)

        private const val CELESTIA_ROOT_FOLDER_NAME = "CelestiaResources"
        private const val CELESTIA_EXTRA_FOLDER_NAME = "${CELESTIA_ROOT_FOLDER_NAME}/extras"
        private const val CELESTIA_SCRIPT_FOLDER_NAME = "${CELESTIA_ROOT_FOLDER_NAME}/scripts"

        private var addonPaths: List<String> = listOf()
        private var extraScriptPaths: List<String> = listOf()
        private var language: String = "en"

        private var availableInstalledFonts: Map<String, Fonts> = mapOf()
        private var defaultInstalledFont: Fonts? = null

        private const val TAG = "XRActivity"

        init {
            System.loadLibrary("ziputils")
            System.loadLibrary("celestia")
            AppCore.setUpLocale()
        }
    }
}
