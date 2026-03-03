package space.celestia.celestiaui.settings.viewmodel

import space.celestia.celestiaui.common.CommonSectionV2
import space.celestia.celestiaui.purchase.PurchaseManager

interface SettingsEntryProvider {
    fun settings(purchaseManager: PurchaseManager): List<CommonSectionV2<SettingsItem>>
}