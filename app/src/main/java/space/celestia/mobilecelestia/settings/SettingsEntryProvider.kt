package space.celestia.mobilecelestia.settings

import space.celestia.celestiaui.common.CommonSectionV2
import space.celestia.celestiaui.purchase.PurchaseManager
import space.celestia.celestiaui.settings.viewmodel.SettingsEntryProvider
import space.celestia.celestiaui.settings.viewmodel.SettingsItem

class SettingsEntryProviderImpl: SettingsEntryProvider {
    override fun settings(purchaseManager: PurchaseManager): List<CommonSectionV2<SettingsItem>> {
        return if (purchaseManager.canUseInAppPurchase()) mainSettingSectionsBeforePlus + celestiaPlusSettingSection + mainSettingSectionsAfterPlus else mainSettingSectionsBeforePlus + mainSettingSectionsAfterPlus
    }
}