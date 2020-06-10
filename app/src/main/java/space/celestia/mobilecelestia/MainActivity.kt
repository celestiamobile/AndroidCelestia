/*
 * MainActivity.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.DisplayCutout
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import space.celestia.mobilecelestia.browser.BrowserCommonFragment
import space.celestia.mobilecelestia.browser.BrowserFragment
import space.celestia.mobilecelestia.browser.BrowserItem
import space.celestia.mobilecelestia.celestia.CelestiaFragment
import space.celestia.mobilecelestia.celestia.CelestiaView
import space.celestia.mobilecelestia.control.BottomControlFragment
import space.celestia.mobilecelestia.control.CameraControlAction
import space.celestia.mobilecelestia.control.CameraControlFragment
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.core.CelestiaScript
import space.celestia.mobilecelestia.core.CelestiaSelection
import space.celestia.mobilecelestia.favorite.*
import space.celestia.mobilecelestia.help.HelpAction
import space.celestia.mobilecelestia.help.HelpFragment
import space.celestia.mobilecelestia.info.InfoFragment
import space.celestia.mobilecelestia.info.model.*
import space.celestia.mobilecelestia.loading.LoadingFragment
import space.celestia.mobilecelestia.search.SearchFragment
import space.celestia.mobilecelestia.settings.*
import space.celestia.mobilecelestia.share.ShareAPI
import space.celestia.mobilecelestia.share.ShareAPIService
import space.celestia.mobilecelestia.share.URLCreationResponse
import space.celestia.mobilecelestia.share.commonHandler
import space.celestia.mobilecelestia.toolbar.ToolbarAction
import space.celestia.mobilecelestia.toolbar.ToolbarFragment
import space.celestia.mobilecelestia.utils.*
import java.io.IOException
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(R.layout.activity_main),
    ToolbarFragment.Listener,
    InfoFragment.Listener,
    SearchFragment.Listener,
    BottomControlFragment.Listener,
    BrowserCommonFragment.Listener,
    CameraControlFragment.Listener,
    HelpFragment.Listener,
    FavoriteFragment.Listener,
    FavoriteItemFragment.Listener,
    SettingsItemFragment.Listener,
    SettingsMultiSelectionFragment.Listener,
    SettingsSingleSelectionFragment.Listener,
    SettingsCurrentTimeFragment.Listener,
    DatePickerDialog.OnDateSetListener,
    AboutFragment.Listener,
    AppStatusReporter.Listener,
    CelestiaFragment.Listener,
    SettingsDataLocationFragment.Listener,
    SettingsCommonFragment.Listener {

    private val preferenceManager by lazy { PreferenceManager(this, "celestia") }
    private val settingManager by lazy { PreferenceManager(this, "celestia_setting") }
    private val celestiaParentPath by lazy { this.filesDir.absolutePath }
    private var addonPath: String? = null
    private var extraScriptPath: String? = null

    private val core by lazy { CelestiaAppCore.shared() }
    private var currentSelection: CelestiaSelection? = null

    private var readyForInteraction = false
    private var scriptOrURLPath: String? = null

    private val celestiaConfigFilePath: String
        get() {
            val custom = customConfigFilePath
            if (custom != null)
                return custom
            return "$celestiaParentPath/$CELESTIA_DATA_FOLDER_NAME/$CELESTIA_CFG_NAME"
        }

    private val celestiaDataDirPath: String
        get() {
            val custom = customDataDirPath
            if (custom != null)
                return custom
            return "$celestiaParentPath/$CELESTIA_DATA_FOLDER_NAME"
        }

    @SuppressLint("CheckResult", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        // We don't need to recover when we get killed
        super.onCreate(null)

        if (!AppCenter.isConfigured()) {
            AppCenter.start(
                application, "d1108985-aa25-4fb5-9269-31a70a87d28e",
                Analytics::class.java, Crashes::class.java
            )

            Crashes.getMinidumpDirectory().thenAccept { path ->
                if (path != null) {
                    CrashHandler.setupNativeCrashesListener(path)
                }
            }
        }

        // Handle notch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        AppStatusReporter.shared().register(this)

        // Handle notch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val rootView = findViewById<View>(android.R.id.content).rootView
            rootView.setOnApplyWindowInsetsListener { _, insets ->
                insets.displayCutout?.let {
                    applyCutout(it)
                }
                return@setOnApplyWindowInsetsListener insets
            }
        }

        // Add fragments
        supportFragmentManager
            .beginTransaction()
            .add(R.id.loading_fragment_container, LoadingFragment.newInstance())
            .commitAllowingStateLoss()
        
        findViewById<View>(R.id.overlay_container).setOnTouchListener { _, _ ->
            hideOverlay()
            return@setOnTouchListener true
        }

        if (!firstInstance) {
            // TODO: handle recreation of Main Activity
            AppStatusReporter.shared().updateStatus(CelestiaString("Please restart Celestia", ""))
            return
        }

        firstInstance = false
        createCopyAssetObservable()
            .concatWith(createPermissionObservable())
            .concatWith(createLoadConfigObservable())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ status ->
                AppStatusReporter.shared().updateStatus(status)
            }, { error ->
                Log.e(TAG, "Initialization failed, $error")
                showError(error)
            }, {
                loadConfigSuccess()
            })

        handleIntent(intent)
    }

    override fun onDestroy() {
        AppStatusReporter.shared().unregister(this)

        super.onDestroy()
    }

    override fun onBackPressed() {}

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables sticky immersive mode.
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun celestiaLoadingProgress(status: String) {}

    override fun celestiaLoadingSucceeded() {
        runOnUiThread {
            // hide the loading container
            findViewById<View>(R.id.loading_fragment_container).visibility = View.GONE

            // apply setting
            readSettings()

            // show onboard
            showWelcomeIfNeeded()

            // open url/script if present
            readyForInteraction = true
            runScriptOrOpenURLIfNeeded()
        }
    }

    override fun celestiaLoadingFailed() {
        AppStatusReporter.shared().updateStatus(CelestiaString("Loading Celestia failed…", ""))
        if (customDataDirPath != null || customConfigFilePath != null) {
            runOnUiThread {
                removeCelestiaFragment()
                showAlert(CelestiaString("Error loading data, fallback to original configuration.", "")) {
                    // Fallback to default
                    setConfigFilePath(null)
                    setDataDirectoryPath(null)
                    loadConfigSuccess()
                }
            }
        } else {
            runOnUiThread {
                removeCelestiaFragment()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val rootView = findViewById<View>(android.R.id.content).rootView
            rootView.rootWindowInsets.displayCutout?.let {
                applyCutout(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun applyCutout(cutout: DisplayCutout) {
        val density = resources.displayMetrics.density

        val rightView = findViewById<View>(R.id.normal_right_container)
        val toolbarView = findViewById<View>(R.id.toolbar_right_container)
        val bottomView = findViewById<View>(R.id.toolbar_bottom_container)

        val rightNotch = findViewById<View>(R.id.right_notch)
        val bottomNotch = findViewById<View>(R.id.bottom_notch)

        (rightView.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.width = (300 * density).toInt() + cutout.safeInsetRight
            rightView.layoutParams = it
        }
        rightView.setPadding(0, 0, cutout.safeInsetRight, 0)

        (toolbarView.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.width = (220 * density).toInt() + cutout.safeInsetRight
            toolbarView.layoutParams = it
        }
        toolbarView.setPadding(0, 0, cutout.safeInsetRight, 0)

        (bottomView.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.height = (60 * density).toInt() + cutout.safeInsetBottom
            bottomView.layoutParams = it
        }
        bottomView.setPadding(0, 0, 0, cutout.safeInsetBottom)

        (bottomNotch.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.height = cutout.safeInsetBottom
            bottomNotch.layoutParams = it
        }
        (rightNotch.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.width = cutout.safeInsetRight
            rightNotch.layoutParams = it
        }
    }

    private fun removeCelestiaFragment() {
        supportFragmentManager.findFragmentById(R.id.celestia_fragment_container)?.let {
            supportFragmentManager.beginTransaction().hide(it).remove(it).commitAllowingStateLoss()
        }
    }

    private fun createCopyAssetObservable(): Observable<String> {
        return Observable.create {
            it.onNext(CelestiaString("Copying data…", ""))
            if (preferenceManager[PreferenceManager.PredefinedKey.DataVersion] != CURRENT_DATA_VERSION) {
                // When version name does not match, copy the asset again
                copyAssets()
            }
            it.onComplete()
        }
    }

    private fun createPermissionObservable(): Observable<String> {
        return RxPermissions(this).request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE).compose {
            it.buffer(1).flatMap { result ->
                if (result.size >= 1 && result.first()) {
                    createAddonFolder()
                }
                Observable.just(CelestiaString("Requesting permission finished", ""))
            }
        }
    }

    private fun createLoadConfigObservable(): Observable<String> {
        return Observable.create {
            it.onNext(CelestiaString("Loading configuration…", ""))

            System.loadLibrary("celestia")
            celestiaLibraryLoaded = true

            CelestiaAppCore.initGL()

            // Read custom paths here
            customConfigFilePath = preferenceManager[PreferenceManager.PredefinedKey.ConfigFilePath]
            customDataDirPath = preferenceManager[PreferenceManager.PredefinedKey.DataDirPath]

            it.onComplete()
        }
    }

    private fun showWelcomeIfNeeded() {
        if (preferenceManager[PreferenceManager.PredefinedKey.OnboardMessage] != "true") {
            preferenceManager[PreferenceManager.PredefinedKey.OnboardMessage] = "true"
            showHelp()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    @SuppressLint("CheckResult")
    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return

        Toast.makeText(this, CelestiaString("Opening external file or URL…", ""), Toast.LENGTH_SHORT).show()
        Observable.just(data)
            .map { uri ->
                if (uri.scheme == "content") {
                    return@map Pair(uri, true)
                } else if (uri.scheme == "cel") {
                    return@map Pair(uri, false)
                }
                throw RuntimeException("Unknown URI scheme ${uri.scheme}")
            }
            .map { ob ->
                if (ob.second) {
                    // Content scheme, copy the resource to a temporary directory

                    val itemName = ob.first.lastPathSegment
                        ?: throw RuntimeException("A filename needed to be present for ${ob.first.path}")

                    // Check file type
                    if (!itemName.endsWith(".cel") && !itemName.endsWith(".celx")) {
                        throw RuntimeException("Celestia does not know how to open $itemName")
                    }

                    // Copy to temporary directory
                    val path = "${cacheDir.absolutePath}/$itemName"
                    if (!FileUtils.copyUri(this, ob.first, path)) {
                        throw RuntimeException("Failed to open $itemName")
                    }
                    return@map Pair(path, ob.second)
                }
                return@map Pair(ob.toString(), ob.second)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                if (result.second) {
                    requestRunScript(result.first)
                } else {
                    requestOpenURL(result.first)
                }
            }, { error ->
                Log.e(TAG, "Handle URI failed, $error")
                showError(error)
            })
    }

    private fun requestRunScript(path: String) {
        scriptOrURLPath = path
        if (readyForInteraction)
            runScriptOrOpenURLIfNeeded()
    }

    private fun requestOpenURL(url: String) {
        scriptOrURLPath = url
        if (readyForInteraction)
            runScriptOrOpenURLIfNeeded()
    }

    private fun runScriptOrOpenURLIfNeeded() {
        val uri = scriptOrURLPath ?: return

        // Clear existing
        scriptOrURLPath = null

        val isURL = uri.startsWith("cel://")
        showAlert(if (isURL) CelestiaString("Open URL?", "") else CelestiaString("Run Script?", "")) {
            if (isURL) {
                core.goToURL(uri)
            } else {
                core.runScript(uri)
            }
        }
    }

    @Throws(IOException::class)
    private fun copyAssets() {
        AssetUtils.copyFileOrDir(this@MainActivity, CELESTIA_DATA_FOLDER_NAME, celestiaParentPath)
        preferenceManager[PreferenceManager.PredefinedKey.DataVersion] = CURRENT_DATA_VERSION
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
            val value = settingManager[PreferenceManager.CustomKey(key)] ?: return null
            return try {
                value.toInt()
            } catch (exp: NumberFormatException) {
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
            val value = settingManager[PreferenceManager.CustomKey(key)] ?: return null
            return try {
                value.toDouble()
            } catch (exp: NumberFormatException) {
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
            core.setBooleanValueForField(key, value)
        }

        for ((key, value) in ints) {
            core.setIntValueForField(key, value)
        }

        for ((key, value) in doubles) {
            core.setDoubleValueForField(key, value)
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
        } catch (ignored: Throwable) {}
        return mapOf()
    }

    private fun createAddonFolder() {
        try {
            var folder = getExternalFilesDir(CELESTIA_EXTRA_FOLDER_NAME)
            if (folder != null && (folder.exists() || folder.mkdir())) {
                addonPath = folder.absolutePath
            }
            folder = getExternalFilesDir(CELESTIA_SCRIPT_FOLDER_NAME)
            if (folder != null && (folder.exists() || folder.mkdir())) {
                extraScriptPath = folder.absolutePath
            }
        } catch (ignored: Throwable) {}
    }

    private fun loadConfigSuccess() {
        // Add gl fragment
        val celestiaFragment = CelestiaFragment.newInstance(
            celestiaDataDirPath,
            celestiaConfigFilePath,
            addonPath,
            preferenceManager[PreferenceManager.PredefinedKey.MSAA] == "true",
            preferenceManager[PreferenceManager.PredefinedKey.FullDPI] == "true"
        )
        supportFragmentManager
            .beginTransaction()
            .add(R.id.celestia_fragment_container, celestiaFragment)
            .commitAllowingStateLoss()
    }

    private fun showToolbar() {
        showRightFragment(ToolbarFragment.newInstance(listOf()), R.id.toolbar_right_container)
    }

    override fun onToolbarActionSelected(action: ToolbarAction) {
        hideOverlay()
        when (action) {
            ToolbarAction.Search -> {
                showSearch()
            }
            ToolbarAction.Time -> {
                showTimeControl()
            }
            ToolbarAction.Script -> {
                showScriptControl()
            }
            ToolbarAction.Browse -> {
                showBrowser()
            }
            ToolbarAction.Camera -> {
                showCameraControl()
            }
            ToolbarAction.Help -> {
                showHelp()
            }
            ToolbarAction.Favorite -> {
                showFavorite()
            }
            ToolbarAction.Setting -> {
                showSettings()
            }
            ToolbarAction.Share -> {
                showShare()
            }
        }
    }

    // Listeners...
    override fun onInfoActionSelected(action: InfoActionItem) {
        val selection = currentSelection ?: return
        when (action) {
            is InfoNormalActionItem -> {
                core.simulation.selection = selection
                CelestiaView.callOnRenderThread { core.charEnter(action.item.value) }
            }
            is InfoSelectActionItem -> {
                core.simulation.selection = selection
            }
            is InfoWebActionItem -> {
                val url = selection.webInfoURL!!
                // show web info in browser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        }
    }

    override fun onSearchItemSelected(text: String) {
        val sel = core.simulation.findObject(text)
        if (sel.isEmpty) {
            showAlert(CelestiaString("Object not found", ""))
            return
        }
        hideOverlay()
        showInfo(sel)
    }

    override fun onActionSelected(item: CelestiaAction) {
        CelestiaView.callOnRenderThread { core.charEnter(item.value) }
    }

    override fun onBrowserItemSelected(item: BrowserItem) {
        if (!item.isLeaf) {
            val frag = supportFragmentManager.findFragmentById(R.id.normal_right_container)
            if (frag is BrowserFragment) {
                frag.pushItem(item.item)
            }
        } else {
            val obj = item.item.`object`
            if (obj != null) {
                val selection = CelestiaSelection.create(obj)
                if (selection != null) {
                    showInfo(selection)
                } else {
                    showAlert(CelestiaString("Object not found", ""))
                }
            } else {
                showAlert(CelestiaString("Object not found", ""))
            }
        }
    }

    override fun onCameraActionClicked(action: CameraControlAction) {
        core.simulation.reverseObserverOrientation()
    }

    override fun onCameraActionStepperTouchDown(action: CameraControlAction) {
        CelestiaView.callOnRenderThread { core.keyDown(action.value) }
    }

    override fun onCameraActionStepperTouchUp(action: CameraControlAction) {
        CelestiaView.callOnRenderThread { core.keyUp(action.value) }
    }

    override fun onHelpActionSelected(action: HelpAction) {
        CelestiaView.callOnRenderThread { core.charEnter(CelestiaAction.RunDemo.value) }
    }

    override fun addFavoriteItem(item: MutableFavoriteBaseItem) {
        val frag = supportFragmentManager.findFragmentById(R.id.normal_right_container)
        if (frag is FavoriteFragment && item is FavoriteBookmarkItem) {
            val bookmark = core.currentBookmark
            if (bookmark == null) {
                showAlert(CelestiaString("Cannot add object", ""))
                return
            }
            frag.add(FavoriteBookmarkItem(bookmark))
        }
    }

    private fun readFavorites() {
        var favorites = arrayListOf<BookmarkNode>()
        try {
            val myType = object : TypeToken<List<BookmarkNode>>() {}.type
            val str = FileUtils.readFileToText("${filesDir.absolutePath}/favorites.json")
            val decoded = Gson().fromJson<ArrayList<BookmarkNode>>(str, myType)
            favorites = decoded
        } catch (ignored: Throwable) { }
        updateCurrentBookmarks(favorites)
    }

    override fun saveFavorites() {
        val favorites = getCurrentBookmarks()
        try {
            val myType = object : TypeToken<List<BookmarkNode>>() {}.type
            val str = Gson().toJson(favorites, myType)
            FileUtils.writeTextToFile(str, "${filesDir.absolutePath}/favorites.json")
        } catch (ignored: Throwable) { }
    }

    override fun onFavoriteItemSelected(item: FavoriteBaseItem) {
        if (item.isLeaf) {
            if (item is FavoriteScriptItem) {
                core.runScript(item.script.filename)
            } else if (item is FavoriteBookmarkItem) {
                core.goToURL(item.bookmark.url)
            }
        } else {
            val frag = supportFragmentManager.findFragmentById(R.id.normal_right_container)
            if (frag is FavoriteFragment) {
                frag.pushItem(item)
            }
        }
    }

    override fun deleteFavoriteItem(index: Int) {
        val frag = supportFragmentManager.findFragmentById(R.id.normal_right_container)
        if (frag is FavoriteFragment) {
            frag.remove(index)
        }
    }

    override fun renameFavoriteItem(item: MutableFavoriteBaseItem) {
        showTextInput(CelestiaString("Rename", ""), item.title) { text ->
            val frag = supportFragmentManager.findFragmentById(R.id.normal_right_container)
            if (frag is FavoriteFragment) {
                frag.rename(item, text)
            }
        }
    }

    override fun onMainSettingItemSelected(item: SettingsItem) {
        val frag = supportFragmentManager.findFragmentById(R.id.normal_right_container)
        if (frag is SettingsFragment) {
            frag.pushMainSettingItem(item)
        }
    }

    override fun onMultiSelectionSettingItemChange(field: String, on: Boolean) {
        val core = CelestiaAppCore.shared()
        core.setBooleanValueForField(field, on)
        settingManager[PreferenceManager.CustomKey(field)] = if (on) "1" else "0"
        reloadSettings()
    }

    override fun onSingleSelectionSettingItemChange(field: String, value: Int) {
        val core = CelestiaAppCore.shared()
        core.setIntValueForField(field, value)
        settingManager[PreferenceManager.CustomKey(field)] = value.toString()
        reloadSettings()
    }

    override fun onCommonSettingSliderItemChange(field: String, value: Double) {
        val core = CelestiaAppCore.shared()
        core.setDoubleValueForField(field, value)
        settingManager[PreferenceManager.CustomKey(field)] = value.toString()
        reloadSettings()
    }

    override fun onCommonSettingActionItemSelected(action: Int) {
        CelestiaView.callOnRenderThread { core.charEnter(action) }
    }

    override fun commonSettingPreferenceSwitchState(key: PreferenceManager.PredefinedKey): Boolean {
        return preferenceManager[key] == "true"
    }

    override fun onCommonSettingPreferenceSwitchStateChanged(
        key: PreferenceManager.PredefinedKey,
        value: Boolean
    ) {
        preferenceManager[key] = if (value) "true" else "false"
    }

    override fun onCurrentTimeActionRequested(action: CurrentTimeAction) {
        when (action) {
            CurrentTimeAction.SetToCurrentTime -> {
                CelestiaView.callOnRenderThread { core.charEnter(CelestiaAction.CurrentTime.value) }
                reloadSettings()
            }
            CurrentTimeAction.PickDate -> {
                val current = createDateFromJulianDay(core.simulation.time)
                val ca = Calendar.getInstance()
                ca.time = current
                val year = ca.get(Calendar.YEAR)
                val month = ca.get(Calendar.MONTH)
                val day = ca.get(Calendar.DAY_OF_MONTH)
                val dialog = DatePickerDialog(this, this, year, month, day)
                dialog.show()
            }
        }
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val current = createDateFromJulianDay(core.simulation.time)
        val ca = Calendar.getInstance()
        ca.time = current
        val h = ca.get(Calendar.HOUR_OF_DAY)
        val m = ca.get(Calendar.MINUTE)

        val dialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            val nca = Calendar.getInstance()
            nca.set(Calendar.YEAR, year)
            nca.set(Calendar.MONTH, month)
            nca.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            nca.set(Calendar.HOUR_OF_DAY, hourOfDay)
            nca.set(Calendar.MINUTE, minute)
            core.simulation.time = nca.time.julianDay
            reloadSettings()
        }, h, m, true)
        dialog.show()
    }

    override fun onAboutActionSelected(action: AboutAction) {
        when (action) {
            AboutAction.VisitOfficialWebsite -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://celestia.space"))
                startActivity(intent)
            }
            AboutAction.VisitOfficialForum -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://celestia.space/forum"))
                startActivity(intent)
            }
        }
    }

    override fun onDataLocationNeedReset() {
        setConfigFilePath(null)
        setDataDirectoryPath(null)
        reloadSettings()
    }

    override fun onDataLocationRequested(dataType: DataType) {
        if (!RxPermissions(this).isGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // No permission to read
            return
        }
        when (dataType) {
            DataType.Config -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "*/*"
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
                startActivityForResult(intent, CONFIG_FILE_REQUEST)
            }
            DataType.DataDirectory -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
                startActivityForResult(intent, DATA_DIR_REQUEST)
            }
        }
    }

    private fun setDataDirectoryPath(path: String?) {
        preferenceManager[PreferenceManager.PredefinedKey.DataDirPath] = path
        customDataDirPath = path
    }

    private fun setConfigFilePath(path: String?) {
        preferenceManager[PreferenceManager.PredefinedKey.ConfigFilePath] = path
        customConfigFilePath = path
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = data?.data ?: return
        if (requestCode == CONFIG_FILE_REQUEST) {
            val path = RealPathUtils.getRealPath(this, uri)
            setConfigFilePath(path)
            reloadSettings()
        } else if (requestCode == DATA_DIR_REQUEST) {
            val path = RealPathUtils.getRealPath(this, DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri)))
            setDataDirectoryPath(path)
            reloadSettings()
        }
    }

    override fun celestiaFragmentDidRequestActionMenu() {
        showToolbar()
    }

    override fun celestiaFragmentDidRequestObjectInfo() {
        val selection = core.simulation.selection
        if (selection.isEmpty) { return }

        showInfo(selection)
    }

    private fun reloadSettings() {
        val frag = supportFragmentManager.findFragmentById(R.id.normal_right_container)
        if (frag is SettingsFragment) {
            frag.reload()
        }
    }

    private fun hideOverlay() {
        val overlay = findViewById<ViewGroup>(R.id.overlay_container)
        for (i in 0 until overlay.childCount) {
            val child = overlay.getChildAt(i)
            supportFragmentManager.findFragmentById(child.id)?.let {
                child.visibility = View.INVISIBLE
                supportFragmentManager.beginTransaction().hide(it).remove(it).commitAllowingStateLoss()
            }
        }
        overlay.visibility = View.INVISIBLE
        findViewById<View>(R.id.right_notch).visibility = View.INVISIBLE
        findViewById<View>(R.id.bottom_notch).visibility = View.INVISIBLE
    }

    private fun showInfo(selection: CelestiaSelection) {
        currentSelection = selection
        showRightFragment(
            InfoFragment.newInstance(
                InfoDescriptionItem(
                    core.simulation.universe.getNameForSelection(selection),
                    core.getOverviewForSelection(selection),
                    selection.webInfoURL != null
                )
            )
        )
    }

    private fun showSearch() {
        showRightFragment(SearchFragment.newInstance())
    }

    private fun showBrowser() {
        showRightFragment(BrowserFragment.newInstance())
    }

    private fun showTimeControl() {
        showBottomFragment(
            BottomControlFragment.newInstance(
                listOf(
                    CelestiaAction.Backward,
                    CelestiaAction.PlayPause,
                    CelestiaAction.Forward
                )))
    }

    private fun showScriptControl() {
        showBottomFragment(
            BottomControlFragment.newInstance(
                listOf(
                    CelestiaAction.PlayPause,
                    CelestiaAction.CancelScript
                )))
    }

    private fun showCameraControl() {
        showRightFragment(CameraControlFragment.newInstance())
    }

    private fun showHelp() {
        showRightFragment(HelpFragment.newInstance())
    }

    private fun showFavorite() {
        readFavorites()
        val scripts = CelestiaScript.getScriptsInDirectory("scripts", true)
        extraScriptPath?.let { path ->
            scripts.addAll(CelestiaScript.getScriptsInDirectory(path, true))
        }
        updateCurrentScripts(scripts)
        showRightFragment(FavoriteFragment.newInstance())
    }

    private fun showSettings() {
        showRightFragment(SettingsFragment.newInstance())
    }

    private fun showShare() {
        val orig = core.currentURL
        val bytes = orig.toByteArray()
        val url = android.util.Base64.encodeToString(bytes, android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP)
        val sel = core.simulation.selection
        val name = core.simulation.universe.getNameForSelection(sel)

        showTextInput(CelestiaString("Share", ""), name) { title ->
            Toast.makeText(this, CelestiaString("Generating sharing link…", ""), Toast.LENGTH_SHORT).show()
            val service = ShareAPI.shared.create(ShareAPIService::class.java)
            service.create(title, url, versionCode.toString()).commonHandler(URLCreationResponse::class.java, {
                ShareCompat.IntentBuilder
                    .from(this)
                    .setType("text/plain")
                    .setChooserTitle(name)
                    .setText(it.publicURL)
                    .startChooser()
            }, {
                showShareError()
            })
        }
    }

    private fun showShareError() {
        showAlert(CelestiaString("Cannot share URL", ""))
    }

    private fun showRightFragment(fragment: Fragment, containerID: Int = R.id.normal_right_container) {
        findViewById<View>(R.id.overlay_container).visibility = View.VISIBLE
        findViewById<View>(containerID).visibility = View.VISIBLE
        findViewById<View>(R.id.right_notch).visibility = View.VISIBLE
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            .add(containerID, fragment)
            .commitAllowingStateLoss()
    }

    private fun showBottomFragment(fragment: Fragment, containerID: Int = R.id.toolbar_bottom_container) {
        findViewById<View>(R.id.overlay_container).visibility = View.VISIBLE
        findViewById<View>(containerID).visibility = View.VISIBLE
        findViewById<View>(R.id.bottom_notch).visibility = View.VISIBLE
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom)
            .add(containerID, fragment)
            .commitAllowingStateLoss()
    }

    companion object {
        private const val CURRENT_DATA_VERSION = "2"

        private const val CELESTIA_DATA_FOLDER_NAME = "CelestiaResources"
        private const val CELESTIA_CFG_NAME = "celestia.cfg"
        private const val CELESTIA_EXTRA_FOLDER_NAME = "CelestiaResources/extras"
        private const val CELESTIA_SCRIPT_FOLDER_NAME = "CelestiaResources/scripts"

        private const val DATA_DIR_REQUEST = 1
        private const val CONFIG_FILE_REQUEST = 2

        private const val TAG = "MainActivity"

        private var firstInstance = true

        var customDataDirPath: String? = null
        var customConfigFilePath: String? = null

        init {
            System.loadLibrary("nativecrashhandler")
        }
    }
}
