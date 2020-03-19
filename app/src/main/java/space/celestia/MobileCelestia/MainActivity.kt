package space.celestia.MobileCelestia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.MobileCelestia.Control.BottomControlFragment
import space.celestia.MobileCelestia.Core.CelestiaAppCore
import space.celestia.MobileCelestia.Core.CelestiaSelection
import space.celestia.MobileCelestia.Info.InfoFragment
import space.celestia.MobileCelestia.Info.Model.*
import space.celestia.MobileCelestia.Loading.LoadingFragment
import space.celestia.MobileCelestia.Search.SearchFragment
import space.celestia.MobileCelestia.Toolbar.ToolbarAction
import space.celestia.MobileCelestia.Toolbar.ToolbarFragment
import space.celestia.MobileCelestia.Utils.AssetUtils
import space.celestia.MobileCelestia.Utils.PreferenceManager
import java.io.IOException

class MainActivity : AppCompatActivity(),
    ToolbarFragment.Listener,
    InfoFragment.Listener,
    SearchFragment.Listener,
    BottomControlFragment.Listener {

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
