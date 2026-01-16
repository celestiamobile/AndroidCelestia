package space.celestia.mobilecelestia.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.SwitchRow
import space.celestia.mobilecelestia.purchase.SubscriptionBackingFragment
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager

class PushNotificationSettingsFragment : SubscriptionBackingFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Notifications", "Title for settings for push notifications")
    }

    private var listener: Listener? = null

    interface Listener {
        fun pushNotificationSettingsChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement PushNotificationSettingsFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @Composable
    override fun MainView() {
        val viewModel: SettingsViewModel = hiltViewModel()

        val activity = LocalActivity.current
        val lifecycleOwner = LocalLifecycleOwner.current

        var hasPermission by remember {
            mutableStateOf(if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) true else (if (activity == null) false else ContextCompat.checkSelfPermission(activity,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED))
        }
        var shouldShowRationale by remember {
            mutableStateOf(if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) false else (activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) ?: false))
        }
        var hasAskedForNotificationPermission by remember {
            mutableStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.HasAskedForNotificationPermission] == "true")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        hasPermission =
                            (if (activity == null) false else ContextCompat.checkSelfPermission(
                                activity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED)
                        shouldShowRationale =
                            (activity?.shouldShowRequestPermissionRationale(
                                Manifest.permission.POST_NOTIFICATIONS
                            ) ?: false)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            DisposableEffect(viewModel) {
                val listener = object :
                    PreferenceManager.ChangeListener(PreferenceManager.PredefinedKey.HasAskedForNotificationPermission) {
                    override fun preferenceChanged(newValue: String?) {
                        hasAskedForNotificationPermission = newValue == "true"
                    }
                }
                viewModel.appSettings.registerOnPreferenceChangeListener(listener)
                onDispose { viewModel.appSettings.unregisterOnPreferenceChangeListener(listener) }
            }
        }

        if (!hasPermission && !shouldShowRationale && hasAskedForNotificationPermission) {
            // Never ask again
            Box(modifier = Modifier.fillMaxSize().systemBarsPadding(), contentAlignment = Alignment.Center) {
                EmptyHint(
                    text = CelestiaString("Notifications Disabled", ""),
                    message = CelestiaString("Notifications permission is denied. Please go to settings to enable it.", "")
                )
            }
        } else {
            var isPushNotificationsEnabled by remember {
                mutableStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.EnablePushNotifications] != "false")
            }
            var isPushNotificationsAddonCreationEnabled by remember {
                mutableStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.EnablePushNotificationsAddonCreation] != "false")
            }
            var isPushNotificationsAddonModificationEnabled by remember {
                mutableStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.EnablePushNotificationsAddonModification] != "false")
            }
            val nestedScrollInterop = rememberNestedScrollInteropConnection()
            Column(modifier = Modifier
                .nestedScroll(nestedScrollInterop)
                .verticalScroll(state = rememberScrollState(), enabled = true)
                .systemBarsPadding()) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                SwitchRow(primaryText = CelestiaString("Push Notifications", "Push notification settings"), checked = isPushNotificationsEnabled, onCheckedChange = {
                    isPushNotificationsEnabled = it
                    viewModel.appSettings[PreferenceManager.PredefinedKey.EnablePushNotifications] = if (it) "true" else "false"
                    listener?.pushNotificationSettingsChanged()
                })
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                SwitchRow(primaryText = CelestiaString("New Add-ons", "Push notification settings for new add-ons"), enabled = isPushNotificationsEnabled, checked = isPushNotificationsAddonCreationEnabled, onCheckedChange = {
                    isPushNotificationsAddonCreationEnabled = it
                    viewModel.appSettings[PreferenceManager.PredefinedKey.EnablePushNotificationsAddonCreation] = if (it) "true" else "false"
                    listener?.pushNotificationSettingsChanged()
                })
                SwitchRow(primaryText = CelestiaString("Add-on Updates", "Push notification settings for add-on updates"), enabled = isPushNotificationsEnabled, checked = isPushNotificationsAddonModificationEnabled, onCheckedChange = {
                    isPushNotificationsAddonModificationEnabled = it
                    viewModel.appSettings[PreferenceManager.PredefinedKey.EnablePushNotificationsAddonModification] = if (it) "true" else "false"
                    listener?.pushNotificationSettingsChanged()
                })
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): PushNotificationSettingsFragment {
            return PushNotificationSettingsFragment()
        }
    }
}