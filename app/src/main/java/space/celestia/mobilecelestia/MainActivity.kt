package space.celestia.mobilecelestia

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kaopiz.kprogresshud.KProgressHUD
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import space.celestia.mobilecelestia.browser.BrowserCommonFragment
import space.celestia.mobilecelestia.browser.BrowserFragment
import space.celestia.mobilecelestia.browser.BrowserItem
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
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(),
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
    AppStatusReporter.Listener {

    private val preferenceManager by lazy { PreferenceManager(this, "celestia") }
    private val settingManager by lazy { PreferenceManager(this, "celestia_setting") }
    private val celestiaParentPath by lazy { this.filesDir.absolutePath }
    private var addonPath: String? = null

    private val core by lazy { CelestiaAppCore.shared() }
    private var currentSelection: CelestiaSelection? = null

    private var readyForUriInput = false
    private var scriptOrURLPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCenter.start(
            application, "d1108985-aa25-4fb5-9269-31a70a87d28e",
            Analytics::class.java, Crashes::class.java
        )

        Crashes.getMinidumpDirectory().thenAccept { path ->
            if (path != null) {
                CrashHandler.setupNativeCrashesListener(path)
            }
        }

        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        AppStatusReporter.shared().register(this)

        setContentView(R.layout.activity_main)

        // Add fragments
        supportFragmentManager
            .beginTransaction()
            .add(R.id.loading_fragment_container, LoadingFragment.newInstance())
            .commitAllowingStateLoss()
        
        findViewById<View>(R.id.overlay_container).setOnTouchListener { _, _ ->
            hideOverlay()
            return@setOnTouchListener true
        }

        findViewById<ImageButton>(R.id.action_menu_button).setOnClickListener {
            showToolbar()
        }

        // Check if data is already copied
        if (preferenceManager[PreferenceManager.PredefinedKey.DataVersion] != CURRENT_DATA_VERSION) {
            // When version name does not match, copy the asset again
            copyAssets()
        } else {
            copyAssetSuccess()
        }

        handleIntent(intent)
    }

    override fun onDestroy() {
        AppStatusReporter.shared().unregister(this)

        super.onDestroy()
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
            readyForUriInput = true
            runScriptOrOpenURLIfNeededOnMainThread()
        }
    }

    override fun celestiaLoadingFailed() {
        AppStatusReporter.shared().updateStatus("Loading Celestia failed...")
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

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return

        if (uri.scheme == "content") {
            GlobalScope.launch(Dispatchers.IO) {
                handleContentURI(uri)
            }
        } else if (uri.scheme == "cel") {
            handleCelURI(uri)
        }
    }

    private fun handleContentURI(uri: Uri) {
        val itemName = uri.lastPathSegment ?: return

        // check file type
        if (!itemName.endsWith(".cel") && !itemName.endsWith(".celx")) {
            runOnUiThread {
                showAlert(CelestiaString("Wrong file type", ""))
            }
            return
        }

        val path = "${cacheDir.absolutePath}/$itemName"
        if (!FileUtils.copyUri(this, uri, path)) { return }

        requestRunScript(path)
    }

    private fun requestRunScript(path: String) {
        scriptOrURLPath = path
        if (readyForUriInput)
            runScriptOrOpenURLIfNeededOnMainThread()
    }

    private fun handleCelURI(uri: Uri) {
        val url = uri.toString()
        requestOpenURL(url)
    }

    private fun requestOpenURL(url: String) {
        scriptOrURLPath = url
        if (readyForUriInput)
            runScriptOrOpenURLIfNeededOnMainThread()
    }

    private fun runScriptOrOpenURLIfNeededOnMainThread() {
        runOnUiThread {
            runScriptOrOpenURLIfNeeded()
        }
    }

    private fun runScriptOrOpenURLIfNeeded() {
        val uri = scriptOrURLPath ?: return

        // Clear existing
        scriptOrURLPath = null

        val isURL = uri.startsWith("cel://")
        showAlert(CelestiaString(if (isURL) "Open URL?" else "Run Script?", "")) {
            if (isURL) {
                core.goToURL(uri)
            } else {
                core.runScript(uri)
            }
        }
    }

    private fun copyAssets() {
        AppStatusReporter.shared().updateStatus("Copying data...")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                AssetUtils.copyFileOrDir(this@MainActivity, CELESTIA_DATA_FOLDER_NAME, celestiaParentPath)
                preferenceManager[PreferenceManager.PredefinedKey.DataVersion] = CURRENT_DATA_VERSION
                withContext(Dispatchers.Main) {
                    copyAssetSuccess()
                }
            } catch (exp: IOException) {
                Log.e(TAG, "Copy data failed, ${exp.localizedMessage}")
                AppStatusReporter.shared().updateStatus("Copying data failed...")
            }
        }
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
        } catch (exp: Exception) {}
        return mapOf()
    }

    private fun copyAssetSuccess() {
        // Check permission to create folder
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            requestPermissionCompleted()
            return
        }
        // Requesting permission to create add-on folder
        val permission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permission, WRITE_EXTERNAL_STROAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != WRITE_EXTERNAL_STROAGE_REQUEST) { return }
        // No matter what the result for permission request, proceed
        requestPermissionCompleted()
    }

    private fun requestPermissionCompleted() {
        // Check permission to create folder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            createAddonFolderCompleted()
            return
        }

        // Permission granted, create add-on directory if not exits
        val folder = getExternalFilesDir(CELESTIA_EXTRA_FOLDER_NAME)
        if (folder != null && (folder.exists() || folder.mkdir())) {
            addonPath = folder.absolutePath
        }
        createAddonFolderCompleted()
    }

    private fun createAddonFolderCompleted() {
        // Load library
        AppStatusReporter.shared().updateStatus("Loading library...")
        GlobalScope.launch(Dispatchers.IO) {
            System.loadLibrary("celestia")
            withContext(Dispatchers.Main) {
                loadLibrarySuccess()
            }
        }
    }

    private fun loadLibrarySuccess() {
        // Add gl fragment
        val celestiaFragment = CelestiaFragment.newInstance("$celestiaParentPath/$CELESTIA_DATA_FOLDER_NAME", "$celestiaParentPath/$CELESTIA_DATA_FOLDER_NAME/$CELESTIA_CFG_NAME", addonPath)
        supportFragmentManager
            .beginTransaction()
            .add(R.id.celestia_fragment_container, celestiaFragment)
            .commitAllowingStateLoss()
    }

    private fun showToolbar() {
        // Show info action only when selection is not null
        currentSelection = core.simulation.selection
        var actions: List<List<ToolbarAction>> = listOf()
        if (!currentSelection!!.isEmpty) {
            actions = listOf(
                listOf(ToolbarAction.Celestia)
            )
        }

        showRightFragment(ToolbarFragment.newInstance(actions), R.id.toolbar_right_container)
    }

    override fun onToolbarActionSelected(action: ToolbarAction) {
        hideOverlay()
        when (action) {
            ToolbarAction.Celestia -> {
                showInfo(currentSelection!!)
            }
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
        val selection = currentSelection
        if (selection == null) {
            showAlert(CelestiaString("Object not found.", ""))
            return
        }

        when (action) {
            is InfoNormalActionItem -> {
                core.simulation.selection = selection
                core.charEnter(action.item.value)
            }
            is InfoSelectActionItem -> {
                core.simulation.selection = selection
            }
            is InfoWebActionItem -> {
                val url = selection.webInfoURL
                if (url == null) {
                    showAlert(CelestiaString("Cannot find URL", ""))
                    return
                }

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
        currentSelection = sel
        showInfo(sel)
    }

    override fun onActionSelected(item: CelestiaAction) {
        core.charEnter(item.value)
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
                    currentSelection = selection
                    showInfo(currentSelection!!)
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
        core.keyDown(action.value)
    }

    override fun onCameraActionStepperTouchUp(action: CameraControlAction) {
        core.keyUp(action.value)
    }

    override fun onHelpActionSelected(action: HelpAction) {
        core.charEnter(CelestiaAction.RunDemo.value)
    }

    override fun addFavoriteItem(item: MutableFavoriteBaseItem) {
        val frag = supportFragmentManager.findFragmentById(R.id.normal_right_container)
        if (frag is FavoriteFragment && item is FavoriteBookmarkItem) {
            val bookmark = core.currentBookmark
            if (bookmark == null) {
                showAlert(CelestiaString("Object not found", ""))
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
        } catch (exp: Exception) { }
        updateCurrentBookmarks(favorites)
    }

    override fun saveFavorites() {
        val favorites = getCurrentBookmarks()
        try {
            val myType = object : TypeToken<List<BookmarkNode>>() {}.type
            val str = Gson().toJson(favorites, myType)
            FileUtils.writeTextToFile(str, "${filesDir.absolutePath}/favorites.json")
        } catch (exp: Exception) { }
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

    override fun onCurrentTimeActionRequested(action: CurrentTimeAction) {
        when (action) {
            CurrentTimeAction.SetToCurrentTime -> {
                core.charEnter(CelestiaAction.CurrentTime.value)
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
    }

    private fun showInfo(selection: CelestiaSelection) {
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
        updateCurrentScripts(CelestiaScript.getScriptsInDirectory("scripts", true))
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
            val hud = KProgressHUD.create(this).setStyle(KProgressHUD.Style.SPIN_INDETERMINATE).show()

            val service = ShareAPI.shared.create(ShareAPIService::class.java)
            service.create(title, url, versionCode.toString()).commonHandler(URLCreationResponse::class.java, {
                hud.dismiss()
                ShareCompat.IntentBuilder
                    .from(this)
                    .setType("text/plain")
                    .setChooserTitle(name)
                    .setText(it.publicURL)
                    .startChooser()
            }, {
                hud.dismiss()
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
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            .add(containerID, fragment)
            .commitAllowingStateLoss()
    }

    private fun showBottomFragment(fragment: Fragment, containerID: Int = R.id.toolbar_bottom_container) {
        findViewById<View>(R.id.overlay_container).visibility = View.VISIBLE
        findViewById<View>(containerID).visibility = View.VISIBLE
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom)
            .add(containerID, fragment)
            .commitAllowingStateLoss()
    }

    companion object {
        private const val CURRENT_DATA_VERSION = "1"
        private const val WRITE_EXTERNAL_STROAGE_REQUEST = 1

        private const val CELESTIA_DATA_FOLDER_NAME = "CelestiaResources"
        private const val CELESTIA_CFG_NAME = "celestia.cfg"
        private const val CELESTIA_EXTRA_FOLDER_NAME = "CelestiaResources/extras"

        private const val TAG = "MainActivity"

        init {
            System.loadLibrary("nativecrashhandler")
        }
    }
}
