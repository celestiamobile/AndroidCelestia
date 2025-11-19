// MainActivity.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.LayoutDirection
import android.util.Log
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.animation.addListener
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.Insets
import androidx.core.net.toUri
import androidx.core.os.BundleCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import space.celestia.celestia.AppCore
import space.celestia.celestia.Body
import space.celestia.celestia.BrowserItem
import space.celestia.celestia.Destination
import space.celestia.celestia.EclipseFinder
import space.celestia.celestia.GoToLocation
import space.celestia.celestia.Renderer
import space.celestia.celestia.Script
import space.celestia.celestia.Selection
import space.celestia.celestia.Universe
import space.celestia.celestiafoundation.favorite.BookmarkNode
import space.celestia.celestiafoundation.resource.model.GuideItem
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.celestiafoundation.utils.AssetUtils
import space.celestia.celestiafoundation.utils.FilePaths
import space.celestia.celestiafoundation.utils.FileUtils
import space.celestia.celestiafoundation.utils.URLHelper
import space.celestia.celestiafoundation.utils.deleteRecursively
import space.celestia.celestiafoundation.utils.showToast
import space.celestia.celestiafoundation.utils.versionCode
import space.celestia.celestiafoundation.utils.versionName
import space.celestia.mobilecelestia.browser.BrowserCommonFragment
import space.celestia.mobilecelestia.browser.BrowserFragment
import space.celestia.mobilecelestia.browser.BrowserPredefinedItem
import space.celestia.mobilecelestia.browser.BrowserRootFragment
import space.celestia.mobilecelestia.browser.BrowserUIItem
import space.celestia.mobilecelestia.browser.SubsystemBrowserFragment
import space.celestia.mobilecelestia.celestia.CelestiaFragment
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.EdgeInsets
import space.celestia.mobilecelestia.common.RoundedCorners
import space.celestia.mobilecelestia.common.SheetLayout
import space.celestia.mobilecelestia.control.BottomControlAction
import space.celestia.mobilecelestia.control.CameraControlAction
import space.celestia.mobilecelestia.control.CameraControlContainerFragment
import space.celestia.mobilecelestia.control.CameraControlFragment
import space.celestia.mobilecelestia.control.ContinuousAction
import space.celestia.mobilecelestia.control.CustomAction
import space.celestia.mobilecelestia.control.CustomActionType
import space.celestia.mobilecelestia.control.InstantAction
import space.celestia.mobilecelestia.control.ObserverModeFragment
import space.celestia.mobilecelestia.control.OverflowItem
import space.celestia.mobilecelestia.di.AppSettings
import space.celestia.mobilecelestia.di.CoreSettings
import space.celestia.mobilecelestia.eventfinder.EventFinderContainerFragment
import space.celestia.mobilecelestia.eventfinder.EventFinderInputFragment
import space.celestia.mobilecelestia.eventfinder.EventFinderResultFragment
import space.celestia.mobilecelestia.favorite.DestinationDetailFragment
import space.celestia.mobilecelestia.favorite.FavoriteBaseItem
import space.celestia.mobilecelestia.favorite.FavoriteBookmarkItem
import space.celestia.mobilecelestia.favorite.FavoriteDestinationItem
import space.celestia.mobilecelestia.favorite.FavoriteFragment
import space.celestia.mobilecelestia.favorite.FavoriteItemFragment
import space.celestia.mobilecelestia.favorite.FavoriteScriptItem
import space.celestia.mobilecelestia.favorite.MutableFavoriteBaseItem
import space.celestia.mobilecelestia.favorite.getCurrentBookmarks
import space.celestia.mobilecelestia.favorite.updateCurrentBookmarks
import space.celestia.mobilecelestia.favorite.updateCurrentDestinations
import space.celestia.mobilecelestia.favorite.updateCurrentScripts
import space.celestia.mobilecelestia.help.HelpAction
import space.celestia.mobilecelestia.help.HelpFragment
import space.celestia.mobilecelestia.help.NewHelpFragment
import space.celestia.mobilecelestia.info.InfoFragment
import space.celestia.mobilecelestia.info.model.AlternateSurfacesItem
import space.celestia.mobilecelestia.info.model.CelestiaAction
import space.celestia.mobilecelestia.info.model.CelestiaContinuosAction
import space.celestia.mobilecelestia.info.model.InfoActionItem
import space.celestia.mobilecelestia.info.model.InfoNormalActionItem
import space.celestia.mobilecelestia.info.model.InfoSelectActionItem
import space.celestia.mobilecelestia.info.model.InfoWebActionItem
import space.celestia.mobilecelestia.info.model.MarkItem
import space.celestia.mobilecelestia.info.model.SubsystemActionItem
import space.celestia.mobilecelestia.loading.LoadingFragment
import space.celestia.mobilecelestia.purchase.PurchaseManager
import space.celestia.mobilecelestia.purchase.SubscriptionBackingFragment
import space.celestia.mobilecelestia.resource.CommonWebFragment
import space.celestia.mobilecelestia.resource.CommonWebNavigationFragment
import space.celestia.mobilecelestia.resource.InstalledAddonListFragment
import space.celestia.mobilecelestia.resource.ResourceFragment
import space.celestia.mobilecelestia.resource.ResourceItemFragment
import space.celestia.mobilecelestia.resource.ResourceItemNavigationFragment
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.search.SearchFragment
import space.celestia.mobilecelestia.settings.AboutFragment
import space.celestia.mobilecelestia.settings.CustomFont
import space.celestia.mobilecelestia.settings.SettingsCurrentTimeNavigationFragment
import space.celestia.mobilecelestia.settings.SettingsFragment
import space.celestia.mobilecelestia.settings.SettingsItem
import space.celestia.mobilecelestia.settings.SettingsItemFragment
import space.celestia.mobilecelestia.settings.SettingsKey
import space.celestia.mobilecelestia.settings.SettingsRefreshRateFragment
import space.celestia.mobilecelestia.toolbar.ToolbarAction
import space.celestia.mobilecelestia.toolbar.ToolbarFragment
import space.celestia.mobilecelestia.travel.GoToContainerFragment
import space.celestia.mobilecelestia.travel.GoToInputFragment
import space.celestia.mobilecelestia.utils.AppStatusReporter
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager
import space.celestia.mobilecelestia.utils.currentBookmark
import space.celestia.mobilecelestia.utils.julianDay
import space.celestia.mobilecelestia.utils.showAlert
import space.celestia.mobilecelestia.utils.showError
import space.celestia.mobilecelestia.utils.showLoading
import space.celestia.mobilecelestia.utils.showOptions
import space.celestia.mobilecelestia.utils.showTextInput
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.URL
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main),
    ToolbarFragment.Listener,
    InfoFragment.Listener,
    SearchFragment.Listener,
    BrowserCommonFragment.Listener,
    CameraControlFragment.Listener,
    HelpFragment.Listener,
    FavoriteFragment.Listener,
    FavoriteItemFragment.Listener,
    SettingsItemFragment.Listener,
    AboutFragment.Listener,
    AppStatusReporter.Listener,
    CelestiaFragment.Listener,
    EventFinderInputFragment.Listener,
    EventFinderResultFragment.Listener,
    InstalledAddonListFragment.Listener,
    DestinationDetailFragment.Listener,
    GoToInputFragment.Listener,
    ResourceItemFragment.Listener,
    SettingsRefreshRateFragment.Listener,
    CommonWebFragment.Listener,
    ObserverModeFragment.Listener,
    SubscriptionBackingFragment.Listener {

    @AppSettings
    @Inject
    lateinit var appSettings: PreferenceManager

    @CoreSettings
    @Inject
    lateinit var coreSettings: PreferenceManager

    private val legacyCelestiaParentPath by lazy { this.filesDir.absolutePath }

    private val favoriteJsonFilePath by lazy { "${filesDir.absolutePath}/favorites.json" }

    @Inject
    lateinit var appCore: AppCore
    @Inject
    lateinit var resourceAPI: ResourceAPIService
    @Inject
    lateinit var resourceManager: ResourceManager
    @Inject
    lateinit var executor: CelestiaExecutor

    @Inject
    lateinit var purchaseManager: PurchaseManager

    private lateinit var appStatusReporter: AppStatusReporter

    private lateinit var drawerLayout: DrawerLayout

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppStatusInterface {
        fun getAppStatusReporter(): AppStatusReporter
    }

    private var interactionBlocked = false

    private var readyForInteraction = false
    private var scriptOrURLPath: String? = null
    private var addonToOpen: String? = null
    private var guideToOpen: String? = null

    @Inject
    lateinit var defaultFilePaths: FilePaths

    private val celestiaConfigFilePath: String
        get() = appSettings[PreferenceManager.PredefinedKey.ConfigFilePath] ?: defaultFilePaths.configFilePath

    private val celestiaDataDirPath: String
        get() = appSettings[PreferenceManager.PredefinedKey.DataDirPath] ?: defaultFilePaths.dataDirectoryPath

    private val fontDirPath: String
        get() = defaultFilePaths.fontDirectoryPath

    private var latestNewsID: String? = null

    private var initialURLCheckPerformed = false
    private var isAskingForExit = false

    private var onBackPressedCallback: OnBackPressedCallback? = null

    private var currentToolbarActions: List<BottomControlAction> = listOf()
    private var currentToolbarOverflowActions: List<OverflowItem> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        val factory = EntryPointAccessors.fromApplication(this, AppStatusInterface::class.java)
        appStatusReporter = factory.getAppStatusReporter()

        val currentState = appStatusReporter.state
        val savedState = if (currentState == AppStatusReporter.State.NONE) null else savedInstanceState

        super.onCreate(savedState)

        drawerLayout = findViewById(R.id.drawer_container)

        Log.d(TAG, "Creating MainActivity")

        // One time migration of language to system per app language support
        val language = appSettings[PreferenceManager.PredefinedKey.Language]
        if (language != null) {
            // Clear the stored language first
            appSettings[PreferenceManager.PredefinedKey.Language] = null
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(
                    language.replace("_", "-")
                )
            )
        }

        showPrivacyAlertIfNeeded()

        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        appStatusReporter.register(this)

        // Handle notch
        ViewCompat.setOnApplyWindowInsetsListener( findViewById<View>(android.R.id.content).rootView) { _, insets ->
            updateConfiguration(resources.configuration, insets)
            return@setOnApplyWindowInsetsListener insets
        }

        findViewById<View>(R.id.close_button).setOnClickListener {
            lifecycleScope.launch {
                hideOverlay(true)
            }
        }

        @Suppress("ClickableViewAccessibility")
        findViewById<View>(R.id.interaction_filter).setOnTouchListener { _, _ ->
            return@setOnTouchListener interactionBlocked
        }

        val weakSelf = WeakReference(this)
        drawerLayout.setDrawerLockMode(if (drawerLayout.isDrawerOpen(GravityCompat.END)) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        drawerLayout.addDrawerListener(object: DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                weakSelf.get()?.drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }

            override fun onDrawerClosed(drawerView: View) {
                weakSelf.get()?.drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })

        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer)) { _, insets ->
            val systemBarInsets = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
            val builder = WindowInsetsCompat.Builder(insets).setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(if (isRTL) systemBarInsets.left else 0 , systemBarInsets.top, if (isRTL) 0 else systemBarInsets.right, systemBarInsets.bottom))
            return@setOnApplyWindowInsetsListener builder.build()
        }

        val bottomSheetContainer = findViewById<FrameLayout>(R.id.bottom_sheet)
        ViewCompat.setOnApplyWindowInsetsListener(bottomSheetContainer) { _, insets ->
            val systemBarInsets = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
            val builder = WindowInsetsCompat.Builder(insets).setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, 0, 0, systemBarInsets.bottom))
            return@setOnApplyWindowInsetsListener builder.build()
        }
        findViewById<FrameLayout>(R.id.drawer).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        bottomSheetContainer.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        findViewById<FrameLayout>(R.id.toolbar_overlay).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        if (currentState == AppStatusReporter.State.LOADING_FAILURE || currentState == AppStatusReporter.State.EXTERNAL_LOADING_FAILURE) {
            celestiaLoadingFailed()
            return
        }

        if (savedState != null) {
            val toolbarVisible = savedState.getBoolean(TOOLBAR_VISIBLE_TAG, false)
            val bottomSheetVisible = savedState.getBoolean(BOTTOM_SHEET_VISIBLE_TAG, false)
            @Suppress("UNCHECKED_CAST")
            currentToolbarActions = BundleCompat.getSerializable(savedState, TOOLBAR_ACTIONS_TAG, ArrayList::class.java) as? List<BottomControlAction> ?: listOf()
            @Suppress("UNCHECKED_CAST")
            currentToolbarOverflowActions = BundleCompat.getSerializable(savedState, TOOLBAR_OVERFLOW_ACTIONS_TAG, ArrayList::class.java) as? List<OverflowItem> ?: listOf()

            initialURLCheckPerformed = savedState.getBoolean(ARG_INITIAL_URL_CHECK_PERFORMED, false)

            findViewById<View>(R.id.bottom_toolbar_container).visibility = if (toolbarVisible) View.VISIBLE else View.GONE

            findViewById<View>(R.id.bottom_sheet_overlay).visibility = if (bottomSheetVisible) View.VISIBLE else View.GONE
            findViewById<View>(R.id.bottom_sheet_card).visibility = if (bottomSheetVisible) View.VISIBLE else View.GONE

            if (currentToolbarActions.isNotEmpty() && toolbarVisible) {
                showToolbarActionsDirect(currentToolbarActions, currentToolbarOverflowActions)
            }
        }

        when (currentState) {
            AppStatusReporter.State.NONE, AppStatusReporter.State.EXTERNAL_LOADING -> {
                loadExternalConfig(savedState)
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
            else -> {}
        }

        val rootView = findViewById<View>(android.R.id.content).rootView
        updateConfiguration(resources.configuration, ViewCompat.getRootWindowInsets(rootView))

        handleIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(BOTTOM_SHEET_VISIBLE_TAG, findViewById<View>(R.id.bottom_sheet_overlay).isVisible)
        outState.putBoolean(TOOLBAR_VISIBLE_TAG, findViewById<View>(R.id.bottom_toolbar_container).isVisible)
        outState.putSerializable(TOOLBAR_ACTIONS_TAG, ArrayList<BottomControlAction>(currentToolbarActions))
        outState.putSerializable(TOOLBAR_OVERFLOW_ACTIONS_TAG, ArrayList<OverflowItem>(currentToolbarOverflowActions))
        outState.putBoolean(ARG_INITIAL_URL_CHECK_PERFORMED, initialURLCheckPerformed)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        appStatusReporter.unregister(this)
        onBackPressedCallback?.remove()
        onBackPressedCallback = null

        Log.d(TAG, "Destroying MainActivity")

        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val rootView = findViewById<View>(android.R.id.content).rootView
        updateConfiguration(newConfig, ViewCompat.getRootWindowInsets(rootView))
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun showPrivacyAlertIfNeeded() {
        if (appSettings[PreferenceManager.PredefinedKey.PrivacyPolicyAccepted] != "true" && Locale.getDefault().country == Locale.CHINA.country) {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setCancelable(false)
            builder.setTitle(R.string.privacy_policy_alert_title)
            builder.setMessage(R.string.privacy_policy_alert_detail)
            builder.setNeutralButton(R.string.privacy_policy_alert_show_policy_button_title) { _, _ ->
                val baseURL = "https://celestia.mobi/privacy"
                val uri = baseURL.toUri().buildUpon().appendQueryParameter("lang", "zh_CN").build()
                openURI(uri)
                showPrivacyAlertIfNeeded()
            }
            builder.setPositiveButton(R.string.privacy_policy_alert_accept_button_title) { _, _ ->
                appSettings[PreferenceManager.PredefinedKey.PrivacyPolicyAccepted] = "true"
            }
            builder.setNegativeButton(R.string.privacy_policy_alert_decline_button_title) { dialog, _ ->
                dialog.cancel()
                finishAndRemoveTask()
                exitProcess(0)
            }
            builder.show()
        }
    }

    private fun updateConfiguration(configuration: Configuration, windowInsets: WindowInsetsCompat?) {
        val isRTL = configuration.layoutDirection == LayoutDirection.RTL

        val hasRegularHorizontalSpace =  configuration.screenWidthDp > SheetLayout.sheetMaxFullWidthDp

        val safeInsets = EdgeInsets(
            windowInsets,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) RoundedCorners(windowInsets) else RoundedCorners(0, 0, 0, 0),
            hasRegularHorizontalSpace
        )

        val safeInsetEnd = if (isRTL) safeInsets.left else safeInsets.right

        val drawerParams = findViewById<View>(R.id.drawer).layoutParams
        drawerParams.width = resources.getDimensionPixelSize(R.dimen.toolbar_default_width) + safeInsetEnd

        val bottomSheetContainer = findViewById<SheetLayout>(R.id.bottom_sheet_overlay)
        bottomSheetContainer.edgeInsets = EdgeInsets(windowInsets?.systemWindowInsets)
        bottomSheetContainer.useLandscapeLayout = hasRegularHorizontalSpace
        bottomSheetContainer.requestLayout()
    }

    private fun loadExternalConfig(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.loading_fragment_container, LoadingFragment.newInstance())
                .commitAllowingStateLoss()
        }
        appStatusReporter.updateState(AppStatusReporter.State.EXTERNAL_LOADING)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                copyAssetIfNeeded()
                if (!isActive) return@launch
                val migrationResult = migrateData()
                if (migrationResult != null) {
                    appSettings[PreferenceManager.PredefinedKey.MigrationSourceDirectory] = null
                    appSettings[PreferenceManager.PredefinedKey.MigrationTargetDirectory] = null
                }
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

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, findViewById(R.id.main_container)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun celestiaLoadingProgress(status: String) {}

    override fun celestiaLoadingStateChanged(newState: AppStatusReporter.State) {
        when (newState) {
            AppStatusReporter.State.FINISHED -> {
                celestiaLoadingFinishedAsync()
            }
            AppStatusReporter.State.LOADING_SUCCESS -> {
                celestiaLoadingSucceeded()
            }
            AppStatusReporter.State.EXTERNAL_LOADING_FAILURE, AppStatusReporter.State.LOADING_FAILURE -> {
                celestiaLoadingFailed()
            }
            else -> {}
        }
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

    private fun celestiaLoadingFinished() {
        supportFragmentManager.findFragmentById(R.id.loading_fragment_container)?.let {
            supportFragmentManager.beginTransaction().hide(it).remove(it).commitAllowingStateLoss()
        }
        findViewById<View>(R.id.loading_fragment_container).visibility = View.GONE
        findViewById<AppCompatImageButton>(R.id.close_button).contentDescription = CelestiaString("Close", "")
        if (supportFragmentManager.findFragmentById(R.id.drawer) == null) {
            val actions = if (purchaseManager.canUseInAppPurchase()) listOf(listOf(ToolbarAction.CelestiaPlus)) else listOf()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.drawer, ToolbarFragment.newInstance(actions))
                .commitAllowingStateLoss()
        }

        if (onBackPressedCallback == null) {
            val weakSelf = WeakReference(this)
            val backPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val self = weakSelf.get() ?: return
                    val frag = self.supportFragmentManager.findFragmentById(R.id.bottom_sheet)
                    if (frag != null) {
                        self.lifecycleScope.launch {
                            self.hideOverlay(true)
                        }
                    } else if (!self.isAskingForExit) {
                        self.isAskingForExit = true
                        self.showAlert(
                            CelestiaString(
                                "Exit Celestia",
                                "Alert title for the exit triggered by back button"
                            ),
                            CelestiaString(
                                "Are you sure you want to exit?",
                                "Alert content for the exit triggered by back button"
                            ),
                            handler = {
                                self.isAskingForExit = false
                                self.finishAndRemoveTask()
                                exitProcess(0)
                            },
                            cancelHandler = {
                                self.isAskingForExit = false
                            })
                    }
                }
            }
            onBackPressedDispatcher.addCallback(backPressedCallback)
            onBackPressedCallback = backPressedCallback
        }

        resourceManager.addonDirectory = addonPaths.firstOrNull()
        resourceManager.scriptDirectory = extraScriptPaths.firstOrNull()
        readyForInteraction = true

        if (!initialURLCheckPerformed) {
            initialURLCheckPerformed = true
            openURLOrScriptOrGreeting()
        }
    }

    private fun celestiaLoadingFailed() {
        appStatusReporter.updateStatus(CelestiaString("Loading Celestia failed…", "Celestia loading failed"))
        lifecycleScope.launch {
            removeCelestiaFragment()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val rootView = findViewById<View>(android.R.id.content).rootView
        updateConfiguration(resources.configuration, ViewCompat.getRootWindowInsets(rootView))
    }

    private fun removeCelestiaFragment() {
        supportFragmentManager.findFragmentById(R.id.celestia_fragment_container)?.let {
            supportFragmentManager.beginTransaction().hide(it).remove(it).commitAllowingStateLoss()
        }
    }

    private fun copyAssetIfNeeded() {
        appStatusReporter.updateStatus(CelestiaString("Copying data…", "Copying default data from APK"))
        if (appSettings[PreferenceManager.PredefinedKey.DataVersion] != CURRENT_DATA_VERSION) {
            // When version name does not match, copy the asset again
            copyAssetsAndRemoveOldAssets()
        }
    }

    private fun loadConfig() {
        availableInstalledFonts = mapOf(
            "ja" to Pair(
                CustomFont("$fontDirPath/NotoSansCJK-Regular.ttc", 0),
                CustomFont("$fontDirPath/NotoSansCJK-Bold.ttc", 0)
            ),
            "ka" to Pair(
                CustomFont("$fontDirPath/NotoSansGeorgian-Regular.ttf", 0),
                CustomFont("$fontDirPath/NotoSansGeorgian-Bold.ttf", 0)
            ),
            "ko" to Pair(
                CustomFont("$fontDirPath/NotoSansCJK-Regular.ttc", 1),
                CustomFont("$fontDirPath/NotoSansCJK-Bold.ttc", 1)
            ),
            "zh_CN" to Pair(
                CustomFont("$fontDirPath/NotoSansCJK-Regular.ttc", 2),
                CustomFont("$fontDirPath/NotoSansCJK-Bold.ttc", 2)
            ),
            "zh_TW" to Pair(
                CustomFont("$fontDirPath/NotoSansCJK-Regular.ttc", 3),
                CustomFont("$fontDirPath/NotoSansCJK-Bold.ttc", 3)
            ),
            "ar" to Pair(
                CustomFont("$fontDirPath/NotoSansArabic-Regular.ttf", 0),
                CustomFont("$fontDirPath/NotoSansArabic-Bold.ttf", 0)
            )
        )
        defaultInstalledFont = Pair(
            CustomFont("$fontDirPath/NotoSans-Regular.ttf", 0),
            CustomFont("$fontDirPath/NotoSans-Bold.ttf", 0)
        )

        language = getString(R.string.celestia_language)

        enableMultisample = appSettings[PreferenceManager.PredefinedKey.MSAA] == "true"
        enableHiDPI =
            appSettings[PreferenceManager.PredefinedKey.FullDPI] != "false" // default on
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data ?: return

        showToast(CelestiaString("Opening external file or URL…", ""), Toast.LENGTH_SHORT)
        if (uri.scheme == "content") {
            handleContentURI(uri)
        } else if (uri.scheme == "cel") {
            requestOpenURL(uri.toString())
        } else if (uri.scheme == "https") {
            handleAppLink(uri)
        } else if (uri.scheme == "celaddon") {
            if (uri.host == "item") {
                val id = uri.getQueryParameter("item") ?: return
                requestOpenAddon(id)
            }
        } else if (uri.scheme == "celguide") {
            if (uri.host == "guide") {
                val id = uri.getQueryParameter("guide") ?: return
                requestOpenGuide(id)
            }
        } else {
            // Cannot handle this URI scheme
            showAlert("Unknown URI scheme ${uri.scheme}")
        }
    }

    private fun handleAppLink(uri: Uri) {
        val path = uri.path ?: return
        when (path) {
            "/resources/item" -> {
                val id = uri.getQueryParameter("item") ?: return
                requestOpenAddon(id)
            }
            "/resources/guide" -> {
                val guide = uri.getQueryParameter("guide") ?: return
                requestOpenGuide(guide)
            }
        }
    }

    private fun handleContentURI(uri: Uri) {
        // Content scheme, copy the resource to a temporary directory
        var itemName = uri.lastPathSegment
        // Check file name
        if (itemName == null) {
            showAlert(String.format(CelestiaString("A filename needed to be present for %s", ""), uri.path))
            return
        }
        val possibleFilUri = itemName.toUri()
        if (possibleFilUri.scheme == "file") {
            val possibleFileName = possibleFilUri.lastPathSegment
            if (possibleFileName != null) {
                itemName = possibleFileName
            }
        }
        // Check file type
        if (!itemName.endsWith(".cel") && !itemName.endsWith(".celx")) {
            showAlert(String.format(CelestiaString("Celestia does not know how to open %s", ""), itemName))
            return
        }
        itemName = itemName.substringAfter("/")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val path = "${cacheDir.absolutePath}/$itemName"
                if (!FileUtils.copyUri(this@MainActivity, uri, path)) {
                    throw RuntimeException("Failed to open $itemName")
                }
                withContext(Dispatchers.Main) {
                    requestRunScript(path)
                }
            } catch (error: Throwable) {
                withContext(Dispatchers.Main) {
                    showError(error)
                }
            }
        }
    }

    private fun requestRunScript(path: String) {
        scriptOrURLPath = path
        if (readyForInteraction)
            openURLOrScriptOrGreeting()
    }

    private fun requestOpenURL(url: String) {
        scriptOrURLPath = url
        if (readyForInteraction)
            openURLOrScriptOrGreeting()
    }

    private fun requestOpenAddon(addon: String) {
        addonToOpen = addon
        if (readyForInteraction)
            openURLOrScriptOrGreeting()
    }

    private fun requestOpenGuide(guide: String) {
        guideToOpen = guide
        if (readyForInteraction)
            openURLOrScriptOrGreeting()
    }

    private fun openCelestiaURL(uri: String) = lifecycleScope.launch(executor.asCoroutineDispatcher()) {
        val isURL = uri.startsWith("cel://")
        if (isURL) {
            appCore.goToURL(uri)
        } else {
            appCore.runScript(uri)
        }
    }

    private fun openURLOrScriptOrGreeting() {
        fun cleanup() {
            // Just clean up everything, only the first message gets presented
            scriptOrURLPath = null
            guideToOpen = null
            addonToOpen = null
        }

        if (appSettings[PreferenceManager.PredefinedKey.OnboardMessage] != "true") {
            appSettings[PreferenceManager.PredefinedKey.OnboardMessage] = "true"
            showHelp()
            cleanup()
            return
        }
        val scriptOrURL = scriptOrURLPath
        if (scriptOrURL != null) {
            val isURL = scriptOrURL.startsWith("cel://")
            showAlert(if (isURL) CelestiaString("Open URL?", "Request user consent to open a URL") else CelestiaString("Run script?", "Request user consent to run a script"), handler = {
                openCelestiaURL(scriptOrURL)
            })
            cleanup()
            return
        }
        val guide = guideToOpen
        val lang = AppCore.getLanguage()
        if (guide != null) {
            lifecycleScope.launch {
                val additionalQueryParameters = if (purchaseManager.canUseInAppPurchase()) mapOf("purchaseTokenAndroid" to (purchaseManager.purchaseToken() ?: "")) else null
                showBottomSheetFragment(CommonWebFragment.newInstance(URLHelper.buildInAppGuideURI(id = guide, language = lang, additionalQueryParameters = additionalQueryParameters), listOf("guide")))
            }
            cleanup()
            return
        }
        val addon = addonToOpen
        if (addon != null) {
            lifecycleScope.launch {
                try {
                    val result = resourceAPI.item(lang, addon)
                    showBottomSheetFragment(ResourceItemNavigationFragment.newInstance(result))
                } catch (ignored: Throwable) {}
            }
            cleanup()
            return
        }

        // Check news
        lifecycleScope.launch {
            try {
                val result = resourceAPI.latest("news", lang)
                if (appSettings[PreferenceManager.PredefinedKey.LastNewsID] == result.id) { return@launch }
                latestNewsID = result.id
                val additionalQueryParameters = if (purchaseManager.canUseInAppPurchase()) mapOf("purchaseTokenAndroid" to (purchaseManager.purchaseToken() ?: "")) else null
                showBottomSheetFragment(CommonWebFragment.newInstance(URLHelper.buildInAppGuideURI(id = result.id, language = lang, additionalQueryParameters = additionalQueryParameters), listOf("guide")))
            } catch (ignored: Throwable) {}
        }
    }

    @Throws(IOException::class)
    private fun copyAssetsAndRemoveOldAssets() {
        try {
            // Remove old ones, ignore any exception thrown
            File(legacyCelestiaParentPath, FilePaths.CELESTIA_DATA_FOLDER_NAME).deleteRecursively()
            File(legacyCelestiaParentPath, FilePaths.CELESTIA_FONT_FOLDER_NAME).deleteRecursively()
            File(defaultFilePaths.dataDirectoryPath).deleteRecursively()
            File(defaultFilePaths.fontDirectoryPath).deleteRecursively()
        } catch (ignored: Exception) {}
        AssetUtils.copyFileOrDir(this@MainActivity, FilePaths.CELESTIA_DATA_FOLDER_NAME, defaultFilePaths.parentDirectoryPath)
        AssetUtils.copyFileOrDir(this@MainActivity, FilePaths.CELESTIA_FONT_FOLDER_NAME, defaultFilePaths.parentDirectoryPath)
        appSettings[PreferenceManager.PredefinedKey.DataVersion] = CURRENT_DATA_VERSION
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
            val value = coreSettings[PreferenceManager.CustomKey(key)] ?: return null
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
            appCore.setBooleanValueForField(key, value)
        }

        for ((key, value) in ints) {
            appCore.setIntValueForField(key, value)
        }

        for ((key, value) in doubles) {
            appCore.setDoubleValueForField(key, value)
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

    private fun migrateData(): Boolean? {
        val sourcePath = appSettings[PreferenceManager.PredefinedKey.MigrationSourceDirectory]
        val targetPath = appSettings[PreferenceManager.PredefinedKey.MigrationTargetDirectory]
        if (sourcePath == null || targetPath == null)
            return null

        Log.i(TAG, "Perform data migration")

        val sourceDirectory = File(sourcePath, CELESTIA_ROOT_FOLDER_NAME)
        val targetDirectory = File(targetPath, CELESTIA_ROOT_FOLDER_NAME)
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory) {
            // No need to migrate
            Log.i(TAG, "Nothing needs to be migrated")
            return true
        }

        if (targetDirectory.exists()) {
            if (!targetDirectory.deleteRecursively()) {
                // We are in a bad state here since we cannot delete the target directory
                Log.e(TAG, "Unable to delete target directory")
                return false
            }
        } else {
            val parentDirectory = targetDirectory.parentFile
            if (parentDirectory != null && !parentDirectory.exists()) {
                // Create parent and other intermediate directories if needed
                if (!parentDirectory.mkdirs()) {
                    Log.e(TAG, "Unable to create parent or intermediate directory for the target directory")
                    return false
                }
            }
        }

        if (!sourceDirectory.copyRecursively(targetDirectory)) {
            Log.e(TAG, "Failed to copy data for migration")
            if (targetDirectory.exists()) {
                targetDirectory.deleteRecursively()
            }
            return false
        } else {
            // Successfully migrated, delete original
            Log.i(TAG, "Migration successful")
            sourceDirectory.deleteRecursively()
            return true
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
            } catch (ignored: Throwable) {}
        }
        return availablePaths
    }

    private fun loadConfigSuccess() {
        val customFrameRateOption =
            appSettings[PreferenceManager.PredefinedKey.FrameRateOption]?.toIntOrNull()
                ?: Renderer.FRAME_60FPS
        // Add gl fragment
        val celestiaFragment = CelestiaFragment.newInstance(
            celestiaDataDirPath,
            celestiaConfigFilePath,
            addonPaths,
            enableMultisample,
            enableHiDPI,
            customFrameRateOption,
            language
        )
        supportFragmentManager
            .beginTransaction()
            .add(R.id.celestia_fragment_container, celestiaFragment)
            .commitAllowingStateLoss()
    }

    private fun loadConfigFailed(error: Throwable) {
        Log.e(TAG, "Initialization failed, $error")
        showError(error)
    }

    private fun showToolbar() = lifecycleScope.launch {
        hideOverlay(true)
        drawerLayout.openDrawer(GravityCompat.END, true)
    }

    override fun onToolbarActionSelected(action: ToolbarAction) {
        executeToolbarAction(action)
    }

    private fun executeToolbarAction(action: ToolbarAction) {
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
                lifecycleScope.launch {
                    hideOverlay(true)
                    showShare()
                }
            }
            ToolbarAction.Home -> {
                lifecycleScope.launch {
                    hideOverlay(true)
                    withContext(executor.asCoroutineDispatcher()) {
                        appCore.charEnter(CelestiaAction.Home.value)
                    }
                }
            }
            ToolbarAction.Event -> {
                showEventFinder()
            }
            ToolbarAction.Exit -> {
                moveTaskToBack(true)
            }
            ToolbarAction.Addons -> {
                showInstalledAddons()
            }
            ToolbarAction.Paperplane -> {
                showGoTo()
            }
            ToolbarAction.Speedometer -> {
                showSpeedControl()
            }
            ToolbarAction.NewsArchive -> {
                val baseURL = "https://celestia.mobi/news"
                val uri = baseURL.toUri().buildUpon().appendQueryParameter("lang", AppCore.getLanguage()).build()
                lifecycleScope.launch {
                    hideOverlay(true)
                    openURL(uri.toString())
                }
            }
            ToolbarAction.Download -> {
                openAddonDownload()
            }
            ToolbarAction.Feedback -> {
                lifecycleScope.launch {
                    hideOverlay(true)
                    showSendFeedback()
                }
            }
            ToolbarAction.CelestiaPlus -> {
                showInAppPurchase()
            }
        }
    }

    override fun requestOpenSubscriptionManagement() {
        showInAppPurchase()
    }

    private fun showInAppPurchase() = lifecycleScope.launch {
        val fragment = purchaseManager.createInAppPurchaseFragment() ?: return@launch
        showBottomSheetFragment(fragment)
    }

    // Listeners...
    override fun onInfoActionSelected(action: InfoActionItem, item: Selection) {
        when (action) {
            is InfoNormalActionItem -> {
                lifecycleScope.launch(executor.asCoroutineDispatcher()) {
                    appCore.simulation.selection = item
                    appCore.charEnter(action.item.value)
                }
            }
            is InfoSelectActionItem -> {
                lifecycleScope.launch(executor.asCoroutineDispatcher()) {
                    appCore.simulation.selection = item
                }
            }
            is InfoWebActionItem -> {
                val url = item.webInfoURL
                if (url != null)
                    openURL(url)
            }
            is SubsystemActionItem -> {
                val entry = item.`object` ?: return
                lifecycleScope.launch {
                    val name = withContext(executor.asCoroutineDispatcher()) {
                        appCore.simulation.universe.getNameForSelection(item)
                    }
                    val browserItem = BrowserItem(name, null, entry, appCore.simulation.universe)
                    showBottomSheetFragment(SubsystemBrowserFragment.newInstance(browserItem))
                }
            }
            is AlternateSurfacesItem -> {
                val alternateSurfaces = item.body?.alternateSurfaceNames ?: return
                val surfaces = ArrayList<String>()
                surfaces.add(CelestiaString("Default", ""))
                surfaces.addAll(alternateSurfaces)
                showOptions(CelestiaString("Alternate Surfaces", "Alternative textures to display"), surfaces.toTypedArray()) { index ->
                    lifecycleScope.launch(executor.asCoroutineDispatcher()) {
                        if (index == 0)
                            appCore.simulation.activeObserver.displayedSurface = ""
                        else
                            appCore.simulation.activeObserver.displayedSurface = alternateSurfaces[index - 1]
                    }
                }
            }
            is MarkItem -> {
                val markers = CelestiaFragment.getAvailableMarkers()
                showOptions(CelestiaString("Mark", "Mark an object"), markers.toTypedArray()) { newIndex ->
                    lifecycleScope.launch(executor.asCoroutineDispatcher()) {
                        if (newIndex >= Universe.MARKER_COUNT) {
                            appCore.simulation.universe.unmark(item)
                        } else {
                            appCore.simulation.universe.mark(item, newIndex)
                            appCore.showMarkers = true
                        }
                    }
                }
            }
            else -> {
            }
        }
    }

    override fun onInfoLinkMetaDataClicked(url: URL) {
        openURL(url.toString())
    }

    override fun onRunScript(file: File) {
        openCelestiaURL(file.absolutePath)
    }

    override fun onRunScript(type: String, content: String, name: String?, location: String?, contextDirectory: File?) {
        if (!supportedScriptTypes.contains(type)) return
        val supportedScriptLocations = listOf("temp", "context")
        if (location != null && !supportedScriptLocations.contains(location)) return
        if (location == "context" && contextDirectory == null) return

        val scriptFile: File
        val scriptFileName = "${name ?: UUID.randomUUID()}.${type}"
        scriptFile = if (location == "context") {
            File(contextDirectory, scriptFileName)
        } else {
            File(cacheDir, scriptFileName)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                scriptFile.writeText(content)
                withContext(Dispatchers.Main) {
                    openCelestiaURL(scriptFile.absolutePath)
                }
            } catch (ignored: Throwable) {}
        }
    }

    override fun onReceivedACK(id: String) {
        if (id == latestNewsID) {
            appSettings[PreferenceManager.PredefinedKey.LastNewsID] = id
        }
    }

    override fun onExternalWebLinkClicked(url: String) {
        openURL(url)
    }

    override fun onShareURL(title: String, url: String) {
        shareURLDirect(title, url)
    }

    override fun onRunDemo() {
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            appCore.runDemo()
        }
    }

    override fun onOpenSubscriptionPage() {
        showInAppPurchase()
    }

    override fun onObjectNotFound() {
        showAlert(CelestiaString("Object not found", ""))
    }

    private fun onInstantActionSelected(item: CelestiaAction) {
        lifecycleScope.launch(executor.asCoroutineDispatcher()) { appCore.charEnter(item.value) }
    }

    private fun onContinuousActionUp(item: CelestiaContinuosAction) {
        lifecycleScope.launch(executor.asCoroutineDispatcher()) { appCore.keyUp(item.value) }
    }

    private fun onContinuousActionDown(item: CelestiaContinuosAction) {
        lifecycleScope.launch(executor.asCoroutineDispatcher()) { appCore.keyDown(item.value) }
    }

    private fun onCustomAction(type: CustomActionType) {
        when (type) {
            CustomActionType.ShowTimeSettings -> {
                lifecycleScope.launch {
                    showBottomSheetFragment(SettingsCurrentTimeNavigationFragment.newInstance())
                }
            }
        }
    }

    override fun objectExistsWithName(name: String): Boolean {
        return !appCore.simulation.findObject(name).isEmpty
    }

    override fun onGoToObject(name: String) {
        hideKeyboard()

        val sel = appCore.simulation.findObject(name)
        if (sel.isEmpty) {
            showAlert(CelestiaString("Object not found", ""))
            return
        }
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            appCore.simulation.selection = sel
            appCore.charEnter(CelestiaAction.GoTo.value)
        }
    }

    override fun onShareAddon(name: String, id: String) {
        val baseURL = "https://celestia.mobi/resources/item"
        val uri = baseURL.toUri().buildUpon().appendQueryParameter("item", id).appendQueryParameter("lang", AppCore.getLanguage()).build()
        shareURLDirect(name, uri.toString())
    }

    private fun shareURLDirect(title: String, url: String) {
        var intent = ShareCompat.IntentBuilder(this)
            .setType("text/plain")
            .setText(url)
            .intent
        intent.putExtra(Intent.EXTRA_TITLE, title)
        intent = Intent.createChooser(intent, null)
        val ai = intent.resolveActivityInfo(packageManager, PackageManager.MATCH_DEFAULT_ONLY)
        if (ai != null && ai.exported)
            startActivity(intent)
        else
            showUnsupportedAction()
    }

    private fun onBottomControlHide() {
        lifecycleScope.launch {
            hideToolbar(true)
        }
    }

    override fun onBrowserItemSelected(item: BrowserUIItem) {
        val frag = supportFragmentManager.findFragmentById(R.id.bottom_sheet) as? BrowserRootFragment ?: return
        if (!item.isLeaf) {
            frag.pushItem(item.item)
        } else {
            val obj = item.item.`object`
            if (obj != null) {
                frag.showInfo(Selection(obj))
            } else {
                showAlert(CelestiaString("Object not found", ""))
            }
        }
    }

    override fun onBrowserAddonCategoryRequested(categoryInfo: BrowserPredefinedItem.CategoryInfo) {
        openAddonCategory(categoryInfo)
    }

    override fun onCameraActionClicked(action: CameraControlAction) {
        lifecycleScope.launch(executor.asCoroutineDispatcher()) { appCore.simulation.reverseObserverOrientation() }
    }

    override fun onCameraActionStepperTouchDown(action: CameraControlAction) {
        lifecycleScope.launch(executor.asCoroutineDispatcher()) { appCore.keyDown(action.value) }
    }

    override fun onCameraActionStepperTouchUp(action: CameraControlAction) {
        lifecycleScope.launch(executor.asCoroutineDispatcher()) { appCore.keyUp(action.value) }
    }

    override fun onCameraControlObserverModeClicked() {
        val frag = supportFragmentManager.findFragmentById(R.id.bottom_sheet) as? CameraControlContainerFragment ?: return
        frag.pushFragment(ObserverModeFragment.newInstance())
    }

    override fun onObserverModeLearnMoreClicked(link: String) {
        openURL(link)
    }

    override fun onHelpActionSelected(action: HelpAction) {
        when (action) {
            HelpAction.RunDemo -> {
                lifecycleScope.launch(executor.asCoroutineDispatcher()) { appCore.runDemo() }
            }
        }
    }

    override fun onHelpURLSelected(url: String) {
        openURL(url)
    }

    override fun addFavoriteItem(item: MutableFavoriteBaseItem) {
        val frag = supportFragmentManager.findFragmentById(R.id.bottom_sheet)
        if (frag is FavoriteFragment && item is FavoriteBookmarkItem) {
            val bookmark = appCore.currentBookmark
            if (bookmark == null) {
                showAlert(CelestiaString("Cannot add object", "Failed to add a favorite item (currently a bookmark)"))
                return
            }
            frag.add(FavoriteBookmarkItem(bookmark))
        }
    }

    override fun shareFavoriteItem(item: MutableFavoriteBaseItem) {
        if (item is FavoriteBookmarkItem && item.bookmark.isLeaf) {
            shareURLDirect(item.bookmark.name, item.bookmark.url)
        }
    }

    private fun readFavorites() {
        var favorites = arrayListOf<BookmarkNode>()
        try {
            val myType = object : TypeToken<List<BookmarkNode>>() {}.type
            val str = FileUtils.readFileToText(favoriteJsonFilePath)
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
            FileUtils.writeTextToFile(str, favoriteJsonFilePath)
        } catch (ignored: Throwable) { }
    }

    override fun onFavoriteItemSelected(item: FavoriteBaseItem) {
        if (item.isLeaf) {
            if (item is FavoriteScriptItem) {
                openCelestiaURL(item.script.filename)
            } else if (item is FavoriteBookmarkItem) {
                openCelestiaURL(item.bookmark.url)
            } else if (item is FavoriteDestinationItem) {
                val destination = item.destination
                val frag = supportFragmentManager.findFragmentById(R.id.bottom_sheet)
                if (frag is FavoriteFragment) {
                    frag.pushFragment(DestinationDetailFragment.newInstance(destination))
                }
            }
        } else {
            val frag = supportFragmentManager.findFragmentById(R.id.bottom_sheet)
            if (frag is FavoriteFragment) {
                frag.pushItem(item)
            }
        }
    }

    override fun onGoToDestination(destination: Destination) {
        lifecycleScope.launch(executor.asCoroutineDispatcher()) { appCore.simulation.goTo(destination) }
    }

    override fun deleteFavoriteItem(index: Int) {
        val frag = supportFragmentManager.findFragmentById(R.id.bottom_sheet)
        if (frag is FavoriteFragment) {
            frag.remove(index)
        }
    }

    override fun renameFavoriteItem(item: MutableFavoriteBaseItem, completion: (String) -> Unit) {
        showTextInput(CelestiaString("Rename", "Rename a favorite item (currently bookmark)"), item.title) { text ->
            item.rename(text)
            completion(text)
        }
    }

    override fun moveFavoriteItem(fromIndex: Int, toIndex: Int) {
        val frag = supportFragmentManager.findFragmentById(R.id.bottom_sheet)
        if (frag is FavoriteFragment) {
            frag.move(fromIndex, toIndex)
        }
    }

    override fun onMainSettingItemSelected(item: SettingsItem) {
        val frag = supportFragmentManager.findFragmentById(R.id.bottom_sheet)
        if (frag is SettingsFragment) {
            frag.pushMainSettingItem(item)
        }
    }

    override fun onRefreshRateChanged(frameRateOption: Int) {
        (supportFragmentManager.findFragmentById(R.id.celestia_fragment_container) as? CelestiaFragment)?.updateFrameRateOption(frameRateOption)
    }

    override fun onAboutURLSelected(url: String, localizable: Boolean) {
        var uri = url.toUri()
        if (localizable)
            uri = uri.buildUpon().appendQueryParameter("lang", AppCore.getLanguage()).build()
        openURI(uri)
    }

    override fun onSearchForEvent(objectName: String, startDate: Date, endDate: Date) {
        lifecycleScope.launch {
            val body = withContext(executor.asCoroutineDispatcher()) {
                appCore.simulation.findObject(objectName).`object` as? Body
            }
            if (body == null) {
                showAlert(CelestiaString("Object not found", ""))
                return@launch
            }
            val finder = EclipseFinder(body)
            val alert = showLoading(CelestiaString("Calculating…", "Calculating for eclipses")) {
                finder.abort()
            } ?: return@launch
            val results = withContext(Dispatchers.IO) {
                finder.search(
                    startDate.julianDay,
                    endDate.julianDay,
                    EclipseFinder.ECLIPSE_KIND_LUNAR or EclipseFinder.ECLIPSE_KIND_SOLAR
                )
            }
            EventFinderResultFragment.eclipses = results
            finder.close()
            if (alert.isShowing) alert.dismiss()
            val frag = supportFragmentManager.findFragmentById(R.id.bottom_sheet)
            if (frag is EventFinderContainerFragment) {
                frag.showResult()
            }
        }
    }

    override fun onEclipseChosen(eclipse: EclipseFinder.Eclipse) {
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            appCore.simulation.goToEclipse(eclipse)
        }
    }

    private fun showUnsupportedAction() {
        showAlert(CelestiaString("Unsupported action.", ""))
    }

    override fun celestiaFragmentDidRequestActionMenu() {
        showToolbar()
    }

    override fun celestiaFragmentDidRequestObjectInfo() {
        lifecycleScope.launch {
            val selection =
                withContext(executor.asCoroutineDispatcher()) { appCore.simulation.selection }
            if (!selection.isEmpty) {
                showInfo(selection)
            }
        }
    }

    override fun celestiaFragmentDidRequestObjectInfo(selection: Selection) {
        showInfo(selection)
    }

    override fun celestiaFragmentDidRequestSearch() {
        showSearch()
    }

    override fun celestiaFragmentDidRequestGoTo() {
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            appCore.charEnter(CelestiaAction.GoTo.value)
        }
    }

    override fun celestiaFragmentCanAcceptKeyEvents(): Boolean {
        // check drawer
        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            return false
        // check bottom sheet
        return findViewById<View>(R.id.bottom_sheet_overlay).visibility != View.VISIBLE
    }

    override fun celestiaFragmentLoadingFromFallback() {
        lifecycleScope.launch {
            showAlert(CelestiaString("Error loading data, fallback to original configuration.", ""))
        }
    }

    private fun openURL(url: String) {
        openURI(url.toUri())
    }

    private fun openURI(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val ai = intent.resolveActivityInfo(packageManager, PackageManager.MATCH_DEFAULT_ONLY)
        if (ai != null && ai.exported)
            startActivity(intent)
        else
            showUnsupportedAction()
    }

    private suspend fun hideOverlay(animated: Boolean) {
        hideMenu(animated)
        hideBottomSheet(animated)
    }

    private suspend fun hideMenu(animated: Boolean): Unit = suspendCoroutine { cont ->
        if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
            cont.resume(Unit)
            return@suspendCoroutine
        }

        val weakSelf = WeakReference(this)

        val listener = object: DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                cont.resume(Unit)
                val self = weakSelf.get() ?: return
                self.drawerLayout.removeDrawerListener(this)
            }

            override fun onDrawerStateChanged(newState: Int) {}
        }

        drawerLayout.addDrawerListener(listener)
        drawerLayout.closeDrawer(GravityCompat.END, animated)
    }

    private suspend fun hideToolbar(animated: Boolean) {
        hideViewAlpha(animated, R.id.bottom_toolbar_container)
        currentToolbarActions = listOf()
    }

    private suspend fun hideBottomSheet(animated: Boolean) {
        hideView(animated, R.id.bottom_sheet_card, false)
        findViewById<View>(R.id.bottom_sheet_overlay).visibility = View.INVISIBLE
        val fragment = supportFragmentManager.findFragmentById(R.id.bottom_sheet)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().hide(fragment).remove(fragment).setPrimaryNavigationFragment(null)
                .commitAllowingStateLoss()
        }
    }

    private suspend fun showView(animated: Boolean, viewID: Int, horizontal: Boolean) {
        val view = findViewById<View>(viewID)
        view.visibility = View.VISIBLE
        val parent = view.parent as? View ?: return

        val destination: Float = if (horizontal) {
            val ltr = resources.configuration.layoutDirection != View.LAYOUT_DIRECTION_RTL
            (if (ltr) (parent.width - view.left) else -(view.right)).toFloat()
        } else {
            (parent.height - view.top).toFloat()
        }
        showView(animated, ObjectAnimator.ofFloat(view, if (horizontal) "translationX" else "translationY", destination, 0f))
    }

    private suspend fun showViewAlpha(animated: Boolean, viewID: Int) {
        val view = findViewById<View>(viewID)
        view.visibility = View.VISIBLE
        view.alpha = 0.0f

        showView(animated, ObjectAnimator.ofFloat(view, "alpha", 1.0f))
    }

    private suspend fun showView(animated: Boolean, showAnimator: ObjectAnimator): Unit = suspendCoroutine { cont ->
        val executionBlock = {
            cont.resume(Unit)
        }
        if (!animated) {
            executionBlock()
        } else {
            showAnimator.duration = 200
            showAnimator.addListener(onStart = {
                interactionBlocked = true
            }, onEnd = {
                interactionBlocked = false
                executionBlock()
            }, onCancel = {
                interactionBlocked = false
                executionBlock()
            })
            showAnimator.start()
        }
    }

    private suspend fun hideView(animated: Boolean, viewID: Int, horizontal: Boolean) {
        val view = findViewById<View>(viewID)
        val parent = view.parent as? View ?: return

        val destination: Float = if (horizontal) {
            val ltr = resources.configuration.layoutDirection != View.LAYOUT_DIRECTION_RTL
            (if (ltr) (parent.width - view.left) else -(view.right)).toFloat()
        } else {
            (parent.height - view.top).toFloat()
        }
        hideView(animated, view, ObjectAnimator.ofFloat(view, if (horizontal) "translationX" else "translationY", 0f, destination))
    }

    private suspend fun hideViewAlpha(animated: Boolean, viewID: Int) {
        val view = findViewById<View>(viewID)
        hideView(animated, view, ObjectAnimator.ofFloat(view, "alpha", 0.0f))
    }

    private suspend fun hideView(animated: Boolean, view: View, hideAnimator: ObjectAnimator): Unit = suspendCoroutine { cont ->
        val executionBlock = {
            view.visibility = View.INVISIBLE
            cont.resume(Unit)
        }

        if (view.visibility != View.VISIBLE || !animated) {
            executionBlock()
        } else {
            hideAnimator.duration = 200
            hideAnimator.addListener(onStart = {
                interactionBlocked = true
            }, onEnd = {
                interactionBlocked = false
                executionBlock()
            }, onCancel = {
                interactionBlocked = false
                executionBlock()
            })
            hideAnimator.start()
        }
    }

    private fun  hideKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun showInfo(selection: Selection) = lifecycleScope.launch {
        showBottomSheetFragment(InfoFragment.newInstance(selection))
    }

    private fun showSendFeedback() {
        showOptions(title = "", options = arrayOf(CelestiaString("Report a Bug", ""), CelestiaString("Suggest a Feature", ""))) {
            if (it == 1) {
                suggestFeature()
            } else {
                reportBug()
            }
        }
    }

    private fun suggestFeature() = lifecycleScope.launch {
        val directory = File(cacheDir, "feedback")
        if (!directory.exists())
            directory.mkdir()

        val parentDirectory = File(directory, "${UUID.randomUUID()}")
        val purchaseToken = purchaseManager.purchaseToken()
        val purchaseTokenFile: File? = if (purchaseToken != null) {
            writeTextToFileWithName(purchaseToken, parentDirectory, "purchasetoken.txt")
        } else {
            null
        }
        if (!sendEmail(listOfNotNull(purchaseTokenFile), CelestiaString("Feature suggestion for Celestia", "Default email title for feature suggestion"), CelestiaString("Please describe the feature you want to see in Celestia.", "Default email body for feature suggestion"))) {
            reportBugSuggestFeatureFallback()
        }
    }

    private fun sendEmail(files: List<File>, subject: String, body: String): Boolean {
        val intent: Intent
        val uris: List<Uri> = files.mapNotNull { file ->
            try {
                return@mapNotNull FileProvider.getUriForFile(
                    this@MainActivity,
                    FILE_PROVIDER_AUTHORITY,
                    file
                )
            } catch (_: Throwable) {
                return@mapNotNull null
            }
        }
        if (uris.isEmpty()) {
            intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL_ADDRESS))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
        } else {
            intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL_ADDRESS))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            }
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            return true
        }
        return false
    }

    private fun reportBug() = lifecycleScope.launch {
        val directory = File(cacheDir, "feedback")
        if (!directory.exists())
            directory.mkdir()

        val parentDirectory = File(directory, "${UUID.randomUUID()}")
        if (!parentDirectory.exists())
            parentDirectory.mkdir()
        val proposedScreenshotFile = File(parentDirectory, "screenshot.png")
        val celestiaInfo = withContext(executor.asCoroutineDispatcher()) {
            val renderInfo = appCore.renderInfo
            val success = appCore.saveScreenshot(proposedScreenshotFile.absolutePath, AppCore.IMAGE_TYPE_PNG)
            val url = appCore.currentURL
            return@withContext Triple(renderInfo, url, success)
        }
        val screenshotFile = if (celestiaInfo.third) proposedScreenshotFile else null
        val renderInfoFile = writeTextToFileWithName(celestiaInfo.first, parentDirectory, "renderinfo.txt")
        val urlInfoFile = writeTextToFileWithName(celestiaInfo.second, parentDirectory, "urlinfo.txt")
        val addons =
            resourceManager.installedResourcesAsync().joinToString("\n") { "${it.name}/${it.id}" }
        val addonInfoFile = writeTextToFileWithName(addons, parentDirectory, "addoninfo.txt")
        val purchaseToken = purchaseManager.purchaseToken()
        val purchaseTokenFile: File? = if (purchaseToken != null) {
            writeTextToFileWithName(purchaseToken, parentDirectory, "purchasetoken.txt")
        } else {
            null
        }
        val systemInfo = "Application Version: ${versionName}(${versionCode})\n" +
            "Operating System: Android\n" +
            "Operating System Version: ${Build.VERSION.RELEASE}(${Build.VERSION.SDK_INT})\n" +
            "Operating System Architecture: ${Build.SUPPORTED_ABIS.joinToString(",")}\n" +
            "Device Model: ${Build.MODEL}\n" +
            "Device Manufacturer: ${Build.MANUFACTURER}"
        val systemInfoFile = writeTextToFileWithName(systemInfo, parentDirectory, "systeminfo.txt")
        val files = listOfNotNull(
            screenshotFile,
            renderInfoFile,
            urlInfoFile,
            addonInfoFile,
            systemInfoFile,
            purchaseTokenFile
        )
        if (!sendEmail(files,  CelestiaString("Bug report for Celestia", "Default email title for bug report"), CelestiaString("Please describe the issue and repro steps, if known.", "Default email body for bug report"))) {
            reportBugSuggestFeatureFallback()
        }
    }

    private fun reportBugSuggestFeatureFallback() {
        openURL(FEEDBACK_GITHUB_LINK)
    }

    private fun writeTextToFileWithName(text: String, directory: File, fileName: String): File? {
        val proposedFile = File(directory, fileName)
        return try {
            FileUtils.writeTextToFile(text, proposedFile)
            proposedFile
        } catch (ignored: Throwable) {
            null
        }
    }

    private fun showSearch() = lifecycleScope.launch {
        showBottomSheetFragment(SearchFragment.newInstance())
    }

    private fun showBrowser() = lifecycleScope.launch {
        showBottomSheetFragment(BrowserFragment.newInstance())
    }

    private fun showTimeControl() = lifecycleScope.launch {
        val timeSettingItem = CustomAction(type = CustomActionType.ShowTimeSettings, imageID = null, contentDescription = CelestiaString("Settings", ""))
        val actions: List<CelestiaAction> = if (resources.configuration.layoutDirection == LayoutDirection.RTL) {
            listOf(
                CelestiaAction.Faster,
                CelestiaAction.PlayPause,
                CelestiaAction.Slower,
                CelestiaAction.Reverse,
            )
        } else {
            listOf(
                CelestiaAction.Slower,
                CelestiaAction.PlayPause,
                CelestiaAction.Faster,
                CelestiaAction.Reverse,
            )
        }
        showToolbarActions(actions = actions.map { InstantAction(it) }, overflowItems = listOf(
            OverflowItem(title = CelestiaString("Settings", ""), action = timeSettingItem),
        ))
    }

    private fun showScriptControl() = lifecycleScope.launch {
        showToolbarActions(listOf(
            CelestiaAction.PlayPause,
            CelestiaAction.CancelScript
        ).map { InstantAction(it) })
    }

    private fun showSpeedControl() = lifecycleScope.launch {
        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        val actions: ArrayList<BottomControlAction> = if (isRTL) arrayListOf(
            ContinuousAction(CelestiaContinuosAction.TravelFaster),
            ContinuousAction(CelestiaContinuosAction.TravelSlower),
        ) else arrayListOf(
            ContinuousAction(CelestiaContinuosAction.TravelSlower),
            ContinuousAction(CelestiaContinuosAction.TravelFaster),
        )
        actions.add(InstantAction(CelestiaAction.Stop))
        actions.add(InstantAction(CelestiaAction.ReverseSpeed))
        showToolbarActions(actions = actions, overflowItems = listOf(
            OverflowItem(title = CelestiaString("1 km/s", "Speed unit"), action = ContinuousAction(CelestiaContinuosAction.F2)),
            OverflowItem(title = CelestiaString("1000 km/s", "Speed unit"), action = ContinuousAction(CelestiaContinuosAction.F3)),
            OverflowItem(title = CelestiaString("c (lightspeed)", ""), action = ContinuousAction(CelestiaContinuosAction.F4)),
            OverflowItem(title = CelestiaString("10c", "Speed unit"), action = ContinuousAction(CelestiaContinuosAction.F5)),
            OverflowItem(title = CelestiaString("1 AU/s", "Speed unit"), action = ContinuousAction(CelestiaContinuosAction.F6)),
            OverflowItem(title = CelestiaString("1 ly/s", "Speed unit"), action = ContinuousAction(CelestiaContinuosAction.F7)),
        ))
    }

    private fun showCameraControl() = lifecycleScope.launch {
        showBottomSheetFragment(CameraControlContainerFragment.newInstance())
    }

    private fun showHelp() = lifecycleScope.launch {
        showBottomSheetFragment(NewHelpFragment.newInstance())
    }

    private fun showFavorite() = lifecycleScope.launch {
        readFavorites()
        val scripts = Script.getScriptsInDirectory("scripts", true)
        for (extraScriptPath in extraScriptPaths) {
            scripts.addAll(Script.getScriptsInDirectory(extraScriptPath, true))
        }
        updateCurrentScripts(scripts)
        updateCurrentDestinations(appCore.destinations)
        showBottomSheetFragment(FavoriteFragment.newInstance())
    }

    private fun showSettings() = lifecycleScope.launch {
        showBottomSheetFragment(SettingsFragment.newInstance())
    }

    private fun showShare() {
        showOptions("", arrayOf(CelestiaString("Image", "Sharing option, image"), CelestiaString("URL", "Sharing option, URL"))) { which ->
            when (which) {
                1 -> {
                    shareURL()
                }
                0 -> {
                    shareImage()
                }
                else -> {
                    Log.e(TAG, "Unknown selection in share $which")
                }
            }
        }
    }

    private fun shareURL() = lifecycleScope.launch {
        val urlAndName = withContext(executor.asCoroutineDispatcher()) { Pair(appCore.currentURL, appCore.simulation.universe.getNameForSelection(appCore.simulation.selection)) }
        shareURLDirect(urlAndName.second, urlAndName.first)
    }

    private fun shareImage() = lifecycleScope.launch {
        val directory = File(cacheDir, "screenshots")
        if (!directory.exists())
            directory.mkdir()

        val file = File(directory, "${UUID.randomUUID()}.png")
        val success = withContext(executor.asCoroutineDispatcher()) {
            return@withContext appCore.saveScreenshot(file.absolutePath, AppCore.IMAGE_TYPE_PNG)
        }
        if (success) {
            shareFile(file, "image/png")
        } else {
            showToast(CelestiaString("Unable to generate image.", "Failed to generate an image for sharing"), Toast.LENGTH_SHORT)
        }
    }

    @Suppress("SameParameterValue")
    private fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file)
        val intent = ShareCompat.IntentBuilder(this)
            .setType(mimeType)
            .setStream(uri)
            .createChooserIntent()
        val ai = intent.resolveActivityInfo(packageManager, PackageManager.MATCH_DEFAULT_ONLY)
        if (ai != null && ai.exported)
            startActivity(intent)
        else
            showUnsupportedAction()
    }

    private fun showEventFinder() = lifecycleScope.launch {
        showBottomSheetFragment(EventFinderContainerFragment.newInstance())
    }

    // Resource
    private fun showInstalledAddons() = lifecycleScope.launch {
        showBottomSheetFragment(ResourceFragment.newInstance())
    }

    override fun onInstalledAddonSelected(addon: ResourceItem) {
        val frag = supportFragmentManager.findFragmentById(R.id.bottom_sheet)
        if (frag is ResourceFragment) {
            frag.pushItem(addon)
        }
    }

    override fun onOpenAddonDownload() {
        openAddonDownload()
    }

    private fun openAddonDownload() {
        val baseURL = "https://celestia.mobi/resources/categories"
        var builder = baseURL.toUri()
            .buildUpon()
            .appendQueryParameter("lang", AppCore.getLanguage())
            .appendQueryParameter("platform", "android")
            .appendQueryParameter("theme", "dark")
            .appendQueryParameter("transparentBackground", "true")
            .appendQueryParameter("api", "2")
        if (purchaseManager.canUseInAppPurchase())
            builder = builder.appendQueryParameter("purchaseTokenAndroid", purchaseManager.purchaseToken() ?: "")
        lifecycleScope.launch {
            showBottomSheetFragment(CommonWebNavigationFragment.newInstance(builder.build()))
        }
    }

    private fun openAddonCategory(info: BrowserPredefinedItem.CategoryInfo) {
        val baseURL = if (info.isLeaf) "https://celestia.mobi/resources/category" else "https://celestia.mobi/resources/categories"
        var builder = baseURL.toUri()
            .buildUpon()
            .appendQueryParameter("lang", language)
            .appendQueryParameter("platform", "android")
            .appendQueryParameter("theme", "dark")
            .appendQueryParameter("transparentBackground", "true")
        builder = if (info.isLeaf)
            builder.appendQueryParameter("category", info.id)
        else
            builder.appendQueryParameter("parent", info.id)
        if (purchaseManager.canUseInAppPurchase())
            builder = builder.appendQueryParameter("purchaseTokenAndroid", purchaseManager.purchaseToken() ?: "")
        lifecycleScope.launch {
            showBottomSheetFragment(CommonWebNavigationFragment.newInstance(builder.build()))
        }
    }

    private fun showGoTo() = lifecycleScope.launch {
        val inputData = GoToInputFragment.GoToData(
            objectName = "",
            0.0f,
            0.0f,
            8.0,
            GoToLocation.DistanceUnit.radii
        )
        showBottomSheetFragment(GoToContainerFragment.newInstance(inputData, Selection()))
    }

    override fun onGoToObject(goToData: GoToInputFragment.GoToData, selection: Selection) {
        if (selection.isEmpty) {
            showAlert(CelestiaString("Object not found", ""))
            return
        }

        val location = GoToLocation(
            selection,
            goToData.longitude,
            goToData.latitude,
            goToData.distance,
            goToData.distanceUnit
        )
        lifecycleScope.launch {
            withContext(executor.asCoroutineDispatcher()) { appCore.simulation.goToLocation(location) }
        }
    }

    // Utilities
    private suspend fun showBottomSheetFragment(fragment: Fragment) {
        hideOverlay(true)
        showBottomSheetFragmentDirect(fragment)
    }

    private suspend fun showBottomSheetFragmentDirect(fragment: Fragment) {
        findViewById<View>(R.id.bottom_sheet_overlay).visibility = View.VISIBLE
        supportFragmentManager
            .beginTransaction()
            .add(R.id.bottom_sheet, fragment, BOTTOM_SHEET_ROOT_FRAGMENT_TAG)
            .setPrimaryNavigationFragment(fragment)
            .commitAllowingStateLoss()
        showView(true, R.id.bottom_sheet_card, false)
    }

    private suspend fun showToolbarActions(actions: List<BottomControlAction>, overflowItems: List<OverflowItem> = listOf()) {
        hideOverlay(true)
        hideToolbar(true)
        showToolbarActionsDirect(actions, overflowItems)
        showViewAlpha(true, R.id.bottom_toolbar_container)
    }

    private fun showToolbarActionsDirect(actions: List<BottomControlAction>, overflowItems: List<OverflowItem>) {
        currentToolbarActions = actions
        val contentView = findViewById<LinearLayout>(R.id.bottom_toolbar_content)
        contentView.removeAllViews()
        val weakSelf = WeakReference(this)
        for (action in actions) {
            val button = layoutInflater.inflate(R.layout.floating_toolbar_button, contentView, false) as MaterialButton
            contentView.addView(button)
            button.icon = ContextCompat.getDrawable(this, action.imageID ?: 0)
            button.contentDescription = action.contentDescription
            button.setOnClickListener(null)
            button.setOnTouchListener(null)
            when (action) {
                is InstantAction -> {
                    button.setOnClickListener {
                        weakSelf.get()?.onInstantActionSelected(action.action)
                    }
                }

                is ContinuousAction -> {
                    @SuppressLint("ClickableViewAccessibility")
                    button.setOnTouchListener { view, event ->
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                weakSelf.get()?.onContinuousActionDown(action.action)
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                weakSelf.get()?.onContinuousActionUp(action.action)
                            }
                        }
                        return@setOnTouchListener view.onTouchEvent(event)
                    }
                }

                is CustomAction -> {
                    button.setOnClickListener {
                        weakSelf.get()?.onCustomAction(action.type)
                    }
                }
            }
        }

        val overflowButton = layoutInflater.inflate(R.layout.floating_toolbar_button, contentView, false) as MaterialButton
        overflowButton.icon = ContextCompat.getDrawable(this, R.drawable.bottom_toolbar_overflow)
        overflowButton.contentDescription = CelestiaString("More actions", "Button to show more actions to in the bottom toolbar")
        overflowButton.setOnTouchListener(null)
        overflowButton.setOnClickListener {
            val popup = PopupMenu(it.context, it)
            for (i in overflowItems.indices) {
                val action = overflowItems[i]
                popup.menu.add(Menu.NONE, i, Menu.NONE, action.title)
            }
            popup.menu.add(Menu.NONE, overflowItems.size, Menu.NONE, CelestiaString("Close", ""))
            popup.setOnMenuItemClickListener { menuItem ->
                val self = weakSelf.get() ?: return@setOnMenuItemClickListener true
                if (menuItem.itemId >= overflowItems.size) {
                    self.onBottomControlHide()
                } else {
                    when (val action = overflowItems[menuItem.itemId].action) {
                        is InstantAction -> {
                            self.onInstantActionSelected(action.action)
                        }
                        is ContinuousAction -> {
                            self.onContinuousActionDown(action.action)
                            self.onContinuousActionUp(action.action)
                        }
                        is CustomAction -> {
                            self.onCustomAction(action.type)
                        }
                    }
                }
                return@setOnMenuItemClickListener true
            }
            popup.show()
        }
        contentView.addView(overflowButton)
    }

    companion object {
        private const val CURRENT_DATA_VERSION = "117"
        // 116: 1.9.3, Localization update data update (commit e707d64080558860ea647f72f25571f3f9b05f53)
        // 114: 1.9.2, Data update (commit e612efa6adfbb1ea640f0ec2207b7d19c35f5390)
        // 111: 1.9.0, Localization update data update (commit 10bd43b0e8925f6ee9bb9687522708a95338d664)
        // 107: 1.8.8, Localization update data update (commit fe433a479a366cde21441344c00cef4b7851e124) config update
        // 101: 1.8.7, Localization update data update (commit 1586549653454b456f24680657475ba453f99689) config update
        // 98: 1.8.3, Localization update data update (commit e09de6968bda2a9d99c42ea6c064d22ea17f7373)
        // 96: 1.8.2, Localization update data update (commit 36a7474dceee5e7b83e54c4d0a44115b2953edc6)
        // 93: 1.8.0, Data update (commit 7cf89b3deace6b18c1ac6eeb5be3338c34fe470e)
        // 91: 1.7.13, Data update (commit 82f08ce3f90d9eb6339d8b9cf349afcea243f2f9)
        // 90: 1.7.12, Localization update data update (commit 81e468cf8d0bf5b0c77bfae66129561a74dbe06d)
        // 89: 1.7.11, Localization update data update (commit e804551103660af99579ad60a7275d2f89c9f214)
        // 86: 1.7.10, Data update (commit 35f14ef09ce4463143405c9459a22060c572a0fa)
        // 84: 1.7.7, Localization update data update (commit 9f85700c021c0ef084c209a6e32b176bf95524d6)
        // 80: 1.7.2, Localization update data update (commit 5fdfe4e2fdda392920bd24d8d89d08f81b6f99df)
        // 76: 1.7.1, Localization update data update (commit 4910ab33dad753673e1983a0493ef9230450391c)
        // 75: 1.7.0, Localization update data update (commit 6b9417781a6beb0ded8a1116c60c93c478830a2e)
        // 72: 1.6.8, Localization update, data update (commit 8241b5d3891df24e398ce329f7cf09a252370546)
        // 69: 1.6.6, Data update (commit 27d9de37632a384f0d6395aecbdd0c39f38e847f)
        // 67: 1.6.4, Localization update, data update (commit 06887c4ddf0c953cc493eb834cf253b1ba6d790b)
        // 63: 1.6.3, Localization update, data update (commit 321e0da37081a7e615f0698d534056f75b638f96)
        // 60: 1.6.1, Data update (commit 3a9eeb95cff1de78374643cab92fb9fe7db4f2c4)
        // 58: 1.6.0, Localization update
        // 57: 1.5.28, Localization update, data update (commit 8e28c7b83acdeb99958a3ada11badfa50b98ff9f)
        // 54: 1.5.27, Localization, data update (commit 4b07c079d31c7b174d1a9d8efbf1db92f54fa05d)
        // 52: 1.5.26 Localization
        // 51: 1.5.25 Localization, data update (commit 604e02d462a4db4b31a43f0c98e04f57d996c6d5)
        // 50: 1.5.24 Shader, localization, data update (commit a269ed58c91135991eba927bfe6c2dd60e774676)
        // 45: 1.5.23 Localization update, data update (commit 809c3fa60fb52667c4fc073328654549b253f493)
        // 44: 1.5.20 Localization update
        // 42: 1.5.17 Localization update, data update (commit d9580cd639ba583fec206b008646c2cc65782839)
        // 40: 1.5.17 Localization update, shader update, data update (commit bc8208e4474aee9a1096c6479a1e7f298aa98d02)
        // 36: 1.5.14 Localization update, data update (commit bc8208e4474aee9a1096c6479a1e7f298aa98d02)
        // 35: 1.5.13 Localization update, data update (commit 2e8cc4a4086aa1b5225d426213c6d65f011ce5d4)
        // 33: 1.5.11 Localization update, data update (commit 9b7df828cbd7205119d378fa864aabd8f3272456)
        // 32: 1.5.7 Localization update, data update (commit 7f816ccd97eeaa5d3f1364a3f17cf378946b708e)
        // 31: 1.5.5 Localization update, data update (commit 9e7a8ee18a875ae8fc202439952256a5a2378a0b)
        // 30: 1.5.2 Localization update, data update (commit 430b955920e31c84fa433bc7aaa43938c5e04ca7)
        // 29: 1.5.0 Data update
        // 28: 1.4.5 Always remove old files
        // 27: 1.4.4 Data update
        // 26: 1.4.3 Localization update
        // 25: 1.4.3 Localization update, data update
        // 24: 1.4.2 Localization update
        // 23: 1.3.3 Localization update
        // 22: 1.3.0 Localization update
        // 21: 1.2.10 Update content to commit 2a80a7695f1dea73de20d3411bfdf8eff94155e5
        // 20: 1.2.7 Privacy Policy and Service Agreement
        // 19: 1.2.2 Shader updates
        // 18: 1.2.1 ru.po/bg.po updates
        // 17: 1.2 Beta, shader updates
        // 16: 1.1 Migrate from filesDir to noBackupFilesDir
        // 15: 1.0.5
        // 14: 1.0.4
        // 13: 1.0.3
        // 12: 1.0.2
        // 11: 1.0.1
        // 10: 1.0
        // < 10: 1.0 Beta X

        private const val CELESTIA_ROOT_FOLDER_NAME = "CelestiaResources"
        private const val CELESTIA_EXTRA_FOLDER_NAME = "${CELESTIA_ROOT_FOLDER_NAME}/extras"
        private const val CELESTIA_SCRIPT_FOLDER_NAME = "${CELESTIA_ROOT_FOLDER_NAME}/scripts"

        private const val TOOLBAR_ACTIONS_TAG = "toolbar_actions"
        private const val TOOLBAR_OVERFLOW_ACTIONS_TAG = "toolbar_overflow_actions"
        private const val TOOLBAR_VISIBLE_TAG = "toolbar_visible"
        private const val BOTTOM_SHEET_VISIBLE_TAG = "bottom_sheet_visible"

        private const val BOTTOM_SHEET_ROOT_FRAGMENT_TAG = "bottom-sheet-root"
        private const val ARG_INITIAL_URL_CHECK_PERFORMED = "initial-url-check-performed"

        private const val FILE_PROVIDER_AUTHORITY = "space.celestia.mobilecelestia.fileprovider"
        private const val FEEDBACK_EMAIL_ADDRESS = "celestia.mobile@outlook.com"
        private const val FEEDBACK_GITHUB_LINK = "https://celestia.mobi/feedback"

        private const val TAG = "MainActivity"

        private var language: String = "en"
        private var addonPaths: List<String> = listOf()
        private var extraScriptPaths: List<String> = listOf()
        private var enableMultisample = false
        private var enableHiDPI = false

        var availableInstalledFonts: Map<String, Pair<CustomFont, CustomFont>> = mapOf()
        var defaultInstalledFont: Pair<CustomFont, CustomFont>? = null

        private val supportedScriptTypes = listOf("cel", "celx")

        init {
            System.loadLibrary("ziputils")
            System.loadLibrary("celestia")
            AppCore.setUpLocale()
        }
    }
}
