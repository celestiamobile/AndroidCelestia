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
import android.content.Intent
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import androidx.core.graphics.contains
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
import space.celestia.mobilecelestia.browser.*
import space.celestia.mobilecelestia.celestia.CelestiaFragment
import space.celestia.mobilecelestia.celestia.CelestiaView
import space.celestia.mobilecelestia.common.Cleanable
import space.celestia.mobilecelestia.common.PoppableFragment
import space.celestia.mobilecelestia.control.BottomControlFragment
import space.celestia.mobilecelestia.control.CameraControlAction
import space.celestia.mobilecelestia.control.CameraControlFragment
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.core.CelestiaBrowserItem
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
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
    AboutFragment.Listener,
    AppStatusReporter.Listener,
    CelestiaFragment.Listener,
    SettingsDataLocationFragment.Listener,
    SettingsCommonFragment.Listener,
    SettingsFontSelectionFragment.Listener,
    SettingsFontSelectionFragment.DataSource {

    private val preferenceManager by lazy { PreferenceManager(this, "celestia") }
    private val settingManager by lazy { PreferenceManager(this, "celestia_setting") }
    private val celestiaParentPath by lazy { this.filesDir.absolutePath }
    private var addonPath: String? = null
    private var extraScriptPath: String? = null

    private val core by lazy { CelestiaAppCore.shared() }
    private var currentSelection: CelestiaSelection? = null

    private val backStack: MutableList<Fragment> = ArrayList<Fragment>()

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

        findViewById<View>(R.id.overlay_container).setOnTouchListener { _, e ->
            if (e.actionMasked == MotionEvent.ACTION_UP) {
                hideOverlay()
                popLastFromBackStackAndShow()
            }
            return@setOnTouchListener true
        }

        findViewById<View>(R.id.interaction_filter).setOnTouchListener { v, e ->
            val point = PointF(e.x, e.y)
            val safeAreaParam = v.resources.displayMetrics.density * 16 // reserve 16 DP on each side for system gestures
            val safeArea = RectF(safeAreaParam, safeAreaParam, v.width - safeAreaParam, v.height - safeAreaParam)
            if (safeArea.contains(point)) {
                // Pass through
                return@setOnTouchListener false
            }
            // Excluded
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

    override fun onBackPressed() {
        val overlay = findViewById<ViewGroup>(R.id.overlay_container)
        if (overlay.visibility == View.VISIBLE) {
            val frag = supportFragmentManager.findFragmentById(R.id.normal_end_container) ?: return
            if (frag is PoppableFragment && frag.canPop()) {
                frag.popLast()
            } else {
                hideOverlay()
                popLastFromBackStackAndShow()
            }
        }
    }

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

        val ltr = resources.configuration.layoutDirection != View.LAYOUT_DIRECTION_RTL
        val safeInsetEnd = if (ltr) cutout.safeInsetRight else cutout.safeInsetLeft

        val endView = findViewById<View>(R.id.normal_end_container)
        val toolbarView = findViewById<View>(R.id.toolbar_end_container)
        val bottomView = findViewById<View>(R.id.toolbar_bottom_container)

        val endNotch = findViewById<View>(R.id.end_notch)
        val bottomNotch = findViewById<View>(R.id.bottom_notch)

        (endView.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.width = (300 * density).toInt() + safeInsetEnd
            endView.layoutParams = it
        }
        if (ltr)
            endView.setPadding(0, 0, safeInsetEnd, 0)
        else
            endView.setPadding(safeInsetEnd, 0, 0, 0)

        (toolbarView.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.width = (220 * density).toInt() + safeInsetEnd
            toolbarView.layoutParams = it
        }
        if (ltr)
            toolbarView.setPadding(0, 0, safeInsetEnd, 0)
        else
            toolbarView.setPadding(safeInsetEnd, 0, 0, 0)

        (bottomView.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.height = (60 * density).toInt() + cutout.safeInsetBottom
            bottomView.layoutParams = it
        }
        bottomView.setPadding(0, 0, 0, cutout.safeInsetBottom)

        (bottomNotch.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.height = cutout.safeInsetBottom
            bottomNotch.layoutParams = it
        }
        (endNotch.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.width = safeInsetEnd
            endNotch.layoutParams = it
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

            val customFont = preferenceManager[PreferenceManager.PredefinedKey.CustomFont]
            if (customFont != null) {
                try {
                    val json = JSONObject(customFont)
                    val path = json.getString(CUSTOM_FONT_PATH_KEY)
                    val index = json.getInt(CUSTOM_FONT_INDEX_KEY)
                    overrideFont = FontHelper.FontCompat(path, index)
                } catch (_: Exception) {}
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                availableSystemFonts = FontHelper.Matcher.getAvailableFonts().map { it }
            }

            System.loadLibrary("celestia")
            celestiaLibraryLoaded = true

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
                return@map Pair(ob.first.toString(), ob.second)
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
        showEndFragment(ToolbarFragment.newInstance(listOf()), R.id.toolbar_end_container)
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
            ToolbarAction.Home -> {
                CelestiaView.callOnRenderThread {
                    core.charEnter(CelestiaAction.Home.value)
                }
            }
        }
    }

    // Listeners...
    override fun onInfoActionSelected(action: InfoActionItem) {
        val selection = currentSelection ?: return
        when (action) {
            is InfoNormalActionItem -> {
                clearBackStack()
                core.simulation.selection = selection
                CelestiaView.callOnRenderThread { core.charEnter(action.item.value) }
            }
            is InfoSelectActionItem -> {
                clearBackStack()
                core.simulation.selection = selection
            }
            is InfoWebActionItem -> {
                clearBackStack()
                val url = selection.webInfoURL!!
                // show web info in browser
                openURL(url)
            }
            is SubsystemActionItem -> {
                addToBackStack()
                hideOverlay()
                val entry = selection.`object` ?: return
                val browserItem = CelestiaBrowserItem(core.simulation.universe.getNameForSelection(selection), null, entry, core.simulation.universe)
                showEndFragment(SubsystemBrowserFragment.newInstance(browserItem))
                return
            }
        }
    }

    override fun onSearchItemSelected(text: String) {
        val sel = core.simulation.findObject(text)
        if (sel.isEmpty) {
            showAlert(CelestiaString("Object not found", ""))
            return
        }
        addToBackStack()
        hideOverlay()
        showInfo(sel)
    }

    override fun onSearchItemSubmit(text: String) {
        onSearchItemSelected(text)
    }

    override fun onActionSelected(item: CelestiaAction) {
        CelestiaView.callOnRenderThread { core.charEnter(item.value) }
    }

    override fun onBrowserItemSelected(item: BrowserItem) {
        if (!item.isLeaf) {
            val frag = supportFragmentManager.findFragmentById(R.id.normal_end_container)
            if (frag is BrowserRootFragment) {
                frag.pushItem(item.item)
            }
        } else {
            val obj = item.item.`object`
            if (obj != null) {
                val selection = CelestiaSelection.create(obj)
                if (selection != null) {
                    clearBackStack()
                    hideOverlay()
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
        val frag = supportFragmentManager.findFragmentById(R.id.normal_end_container)
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
            val frag = supportFragmentManager.findFragmentById(R.id.normal_end_container)
            if (frag is FavoriteFragment) {
                frag.pushItem(item)
            }
        }
    }

    override fun deleteFavoriteItem(index: Int) {
        val frag = supportFragmentManager.findFragmentById(R.id.normal_end_container)
        if (frag is FavoriteFragment) {
            frag.remove(index)
        }
    }

    override fun renameFavoriteItem(item: MutableFavoriteBaseItem) {
        showTextInput(CelestiaString("Rename", ""), item.title) { text ->
            val frag = supportFragmentManager.findFragmentById(R.id.normal_end_container)
            if (frag is FavoriteFragment) {
                frag.rename(item, text)
            }
        }
    }

    override fun onMainSettingItemSelected(item: SettingsItem) {
        val frag = supportFragmentManager.findFragmentById(R.id.normal_end_container)
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
                val format = "yyyy/MM/dd HH:mm:ss"
                showTextInput(CelestiaString("Please enter the time in \"$format\" format.", "")) { input ->
                    val dateFormatter = SimpleDateFormat(format, Locale.US)
                    try {
                        val date = dateFormatter.parse(input)
                        if (date == null) {
                            showAlert(CelestiaString("Unrecognized time string.", ""))
                            return@showTextInput
                        }
                        CelestiaView.callOnRenderThread {
                            core.simulation.time = date.julianDay
                            runOnUiThread {
                                reloadSettings()
                            }
                        }
                    } catch (_: Exception) {
                        showAlert(CelestiaString("Unrecognized time string.", ""))
                    }
                }
            }
        }
    }

    override fun onAboutActionSelected(action: AboutAction) {
        when (action) {
            AboutAction.VisitOfficialWebsite -> {
                openURL("https://celestia.space")
            }
            AboutAction.VisitOfficialForum -> {
                openURL("https://celestia.space/forum")
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

    override fun onFontReset() {
        preferenceManager[PreferenceManager.PredefinedKey.CustomFont] = null
        overrideFont = null
        reloadSettings()
    }

    override fun onCustomFontProvided(font: FontHelper.FontCompat) {
        val json = JSONObject()
        json.put(CUSTOM_FONT_PATH_KEY, font.filePath)
        json.put(CUSTOM_FONT_INDEX_KEY, font.collectionIndex)
        preferenceManager[PreferenceManager.PredefinedKey.CustomFont] = json.toString()
        overrideFont = font
        reloadSettings()
    }

    override val currentFont: FontHelper.FontCompat?
        get() = if (overrideFont != null) overrideFont else defaultSystemFont

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

    private fun openURL(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun reloadSettings() {
        val frag = supportFragmentManager.findFragmentById(R.id.normal_end_container)
        if (frag is SettingsFragment) {
            frag.reload()
        }
    }

    private fun hideOverlay() {
        val overlay = findViewById<ViewGroup>(R.id.overlay_container)
        for (i in 0 until overlay.childCount) {
            val child = overlay.getChildAt(i)
            supportFragmentManager.findFragmentById(child.id)?.let {
                if (!backStack.contains(it) && it is Cleanable) {
                    it.cleanUp()
                }
                child.visibility = View.INVISIBLE
                supportFragmentManager.beginTransaction().hide(it).remove(it).commitAllowingStateLoss()
            }
        }
        overlay.visibility = View.INVISIBLE
        findViewById<View>(R.id.end_notch).visibility = View.INVISIBLE
        findViewById<View>(R.id.bottom_notch).visibility = View.INVISIBLE
    }

    private fun showInfo(selection: CelestiaSelection) {
        currentSelection = selection
        showEndFragment(
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
        showEndFragment(SearchFragment.newInstance())
    }

    private fun showBrowser() {
        showEndFragment(BrowserFragment.newInstance())
    }

    private fun showTimeControl() {
        showBottomFragment(
            BottomControlFragment.newInstance(
                listOf(
                    CelestiaAction.Slower,
                    CelestiaAction.PlayPause,
                    CelestiaAction.Faster,
                    CelestiaAction.Reverse
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
        showEndFragment(CameraControlFragment.newInstance())
    }

    private fun showHelp() {
        showEndFragment(HelpFragment.newInstance())
    }

    private fun showFavorite() {
        readFavorites()
        val scripts = CelestiaScript.getScriptsInDirectory("scripts", true)
        extraScriptPath?.let { path ->
            scripts.addAll(CelestiaScript.getScriptsInDirectory(path, true))
        }
        updateCurrentScripts(scripts)
        showEndFragment(FavoriteFragment.newInstance())
    }

    private fun showSettings() {
        showEndFragment(SettingsFragment.newInstance())
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

    private fun showEndFragment(fragment: Fragment, containerID: Int = R.id.normal_end_container) {
        findViewById<View>(R.id.overlay_container).visibility = View.VISIBLE
        findViewById<View>(containerID).visibility = View.VISIBLE
        findViewById<View>(R.id.end_notch).visibility = View.VISIBLE

        val ltr = resources.configuration.layoutDirection != View.LAYOUT_DIRECTION_RTL

        val ani1 = if (ltr) R.anim.enter_from_right else R.anim.enter_from_left
        val ani2 = if (ltr) R.anim.exit_to_left else R.anim.exit_to_right
        val ani3 = if (ltr) R.anim.enter_from_left else R.anim.enter_from_right
        val ani4 = if (ltr) R.anim.exit_to_right else R.anim.exit_to_left

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(ani1, ani2, ani3, ani4)
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

    private fun addToBackStack() {
        val frag = supportFragmentManager.findFragmentById(R.id.normal_end_container) ?: return
        backStack.add(frag)
    }

    private fun clearBackStack() {
        backStack.clear()
    }

    private fun popLastFromBackStackAndShow() {
        if (backStack.size == 0) return
        val frag = backStack.last()
        backStack.removeAt(backStack.size - 1)
        showEndFragment(frag)
    }

    companion object {
        private const val CURRENT_DATA_VERSION = "3"

        private const val CELESTIA_DATA_FOLDER_NAME = "CelestiaResources"
        private const val CELESTIA_CFG_NAME = "celestia.cfg"
        private const val CELESTIA_EXTRA_FOLDER_NAME = "CelestiaResources/extras"
        private const val CELESTIA_SCRIPT_FOLDER_NAME = "CelestiaResources/scripts"
        private const val CUSTOM_FONT_PATH_KEY = "path"
        private const val CUSTOM_FONT_INDEX_KEY = "index"

        private const val DATA_DIR_REQUEST = 1
        private const val CONFIG_FILE_REQUEST = 2

        private const val TAG = "MainActivity"

        private var firstInstance = true

        var customDataDirPath: String? = null
        var customConfigFilePath: String? = null

        var overrideFont: FontHelper.FontCompat? = null
        var defaultSystemFont: FontHelper.FontCompat? = null
        var defaultSystemBoldFont: FontHelper.FontCompat? = null
        var availableSystemFonts: List<FontHelper.FontCompat> = listOf()

        init {
            System.loadLibrary("nativecrashhandler")
        }
    }
}
