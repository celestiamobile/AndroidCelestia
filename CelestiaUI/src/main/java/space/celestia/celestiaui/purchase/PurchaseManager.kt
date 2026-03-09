package space.celestia.celestiaui.purchase

import androidx.compose.runtime.Composable

interface PurchaseManager {
    fun canUseInAppPurchase(): Boolean
    fun purchaseToken(): String?
    @Composable
    fun ManagerScreen(preferredPlayOfferId: String?)
}