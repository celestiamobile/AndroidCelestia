package space.celestia.mobilecelestia.settings

import android.os.Build
import space.celestia.celestiaui.common.CommonSectionV2
import space.celestia.celestiaui.purchase.PurchaseManager
import space.celestia.celestiaui.resource.model.FeatureFlags
import space.celestia.celestiaui.settings.viewmodel.SettingsEntryProvider
import space.celestia.celestiaui.settings.viewmodel.SettingsItem
import space.celestia.mobilecelestia.pushnotification.pushNotificationSettingsSection

class SettingsEntryProviderImpl(private val featureFlags: FeatureFlags) : SettingsEntryProvider {
    override fun settings(purchaseManager: PurchaseManager): List<CommonSectionV2<SettingsItem>> {
        val sections = mutableListOf<CommonSectionV2<SettingsItem>>()
        sections.addAll(mainSettingSectionsBeforePlus)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pushNotificationSettingsSection()?.let { sections.add(it) }
        }
        if (purchaseManager.canUseInAppPurchase()) {
            sections.addAll(celestiaPlusSettingSection)
        }
        sections.addAll(mainSettingSectionsAfterPlus)
        return sections
    }
}
