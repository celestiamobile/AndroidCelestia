package space.celestia.MobileCelestia

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.MobileCelestia.Browser.BrowserCommonFragment
import space.celestia.MobileCelestia.Browser.BrowserFragment
import space.celestia.MobileCelestia.Browser.BrowserItem
import space.celestia.MobileCelestia.Control.BottomControlFragment
import space.celestia.MobileCelestia.Control.CameraControlAction
import space.celestia.MobileCelestia.Control.CameraControlFragment
import space.celestia.MobileCelestia.Core.CelestiaAppCore
import space.celestia.MobileCelestia.Core.CelestiaScript
import space.celestia.MobileCelestia.Core.CelestiaSelection
import space.celestia.MobileCelestia.Favorite.*
import space.celestia.MobileCelestia.Help.HelpAction
import space.celestia.MobileCelestia.Help.HelpFragment
import space.celestia.MobileCelestia.Info.InfoFragment
import space.celestia.MobileCelestia.Info.Model.*
import space.celestia.MobileCelestia.Loading.LoadingFragment
import space.celestia.MobileCelestia.Search.SearchFragment
import space.celestia.MobileCelestia.Settings.*
import space.celestia.MobileCelestia.Toolbar.ToolbarAction
import space.celestia.MobileCelestia.Toolbar.ToolbarFragment
import space.celestia.MobileCelestia.Utils.AssetUtils
import space.celestia.MobileCelestia.Utils.createDateFromJulianDay
import space.celestia.MobileCelestia.Utils.PreferenceManager
import space.celestia.MobileCelestia.Utils.julianDay
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(),
    ToolbarFragment.Listener,
    InfoFragment.Listener,
    SearchFragment.Listener,
    BottomControlFragment.Listener,
    BrowserCommonFragment.Listener,
    CameraControlFragment.Listener,
    HelpFragment.Listener,
    FavoriteItemFragment.Listener,
    SettingsItemFragment.Listener,
    SettingsMultiSelectionFragment.Listener,
    SettingsSingleSelectionFragment.Listener,
    SettingsCurrentTimeFragment.Listener,
    DatePickerDialog.OnDateSetListener {

    private val TAG = "MainActivity"

    private val celestiaFolderName = "CelestiaResources"
    private val celestiaCfgName = "celestia.cfg"

    // Fragments
    private val celestiaFragment = CelestiaFragment()
    private val loadingFragment = LoadingFragment()

    private val preferenceManager by lazy { PreferenceManager(this, "celestia") }
    private val celestiaParentPath by lazy { this.filesDir.absolutePath }

    private var core = CelestiaAppCore.shared()
    private var currentSelection: CelestiaSelection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) { return }

        // Add fragments
        supportFragmentManager
            .beginTransaction()
            .add(R.id.celestia_fragment_container, celestiaFragment)
            .commitAllowingStateLoss()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.loading_fragment_container, loadingFragment)
            .commitAllowingStateLoss()
        
        findViewById<View>(R.id.overlay_container).setOnTouchListener { _, _ ->
            hideOverlay()
            true
        }

        findViewById<ImageButton>(R.id.action_menu_button).setOnClickListener {
            showToolbar()
        }

        // Check if data is already copied
        if (preferenceManager[PreferenceManager.Preference.AssetCopied] == null) {
            copyAssets()
        } else {
            copyAssetSuccess()
        }
    }

    private fun copyAssets() {
        loadingFragment.update("Copying data...")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                AssetUtils.copyFileOrDir(this@MainActivity,celestiaFolderName, celestiaParentPath)
                preferenceManager[PreferenceManager.Preference.AssetCopied] = "true"
                withContext(Dispatchers.Main) {
                    copyAssetSuccess()
                }
            } catch (exp: IOException) {
                Log.e(TAG, "Copy data failed, ${exp.localizedMessage}")

                withContext(Dispatchers.Main) {
                    loadingFragment.update("Copying data failed...")
                }
            }
        }
    }

    private fun copyAssetSuccess() {
        celestiaFragment.requestLoadCelestia("$celestiaParentPath/$celestiaFolderName", "$celestiaParentPath/$celestiaFolderName/$celestiaCfgName", {
            GlobalScope.launch(Dispatchers.Main) {
                loadingFragment.update(it)
            }
        }, { success ->
            GlobalScope.launch(Dispatchers.Main) {
                if (success) {
                    supportFragmentManager.beginTransaction().remove(loadingFragment).commitAllowingStateLoss()
                } else {
                    loadingFragment.update("Loading Celestia failed...")
                }
            }
        })
    }

    private fun showToolbar() {
        // Show info action only when selection is not null
        currentSelection = core.simulation.selection
        var actions: List<List<ToolbarAction>> = listOf();
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
            else -> {
                // TODO: responds to other actions...
            }
        }
    }

    // Listeners...
    override fun onInfoActionSelected(action: InfoActionItem) {
        if (action is InfoNormalActionItem) {
            core.simulation.selection = currentSelection!!
            core.charEnter(action.item.value)
        } else if (action is InfoSelectActionItem) {
            core.simulation.selection = currentSelection!!
        }
    }

    override fun onSearchItemSelected(text: String) {
        val sel = core.simulation.findObject(text)
        if (sel.isEmpty) {
            // TODO: object not found
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
                currentSelection = CelestiaSelection(obj)
                showInfo(currentSelection!!)
            } else {
                // TODO: object not found
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

    override fun onFavoriteItemSelected(item: FavoriteBaseItem) {
        if (item.isLeaf) {
            if (item is FavoriteScriptItem) {
                core.runScript(item.script.filename)
            } else {
                // TODO: other items
            }
        } else {
            val frag = supportFragmentManager.findFragmentById(R.id.normal_right_container)
            if (frag is FavoriteFragment) {
                frag.pushItem(item)
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
        reloadSettings()
    }

    override fun onSingleSelectionSettingItemChange(field: String, value: Int) {
        val core = CelestiaAppCore.shared()
        core.setIntValueForField(field, value)
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

    fun reloadSettings() {
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
                InfoDescriptionItem(core.simulation.universe.nameForSelection(selection),
                    "Overview")
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
        updateCurrentScripts(CelestiaScript.getScriptsInDirectory("scripts", true))
        showRightFragment(FavoriteFragment.newInstance())
    }

    private fun showSettings() {
        showRightFragment(SettingsFragment.newInstance())
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
        init {
            System.loadLibrary("celestia")
        }
    }
}
