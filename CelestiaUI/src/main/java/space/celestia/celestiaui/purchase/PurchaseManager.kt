package space.celestia.celestiaui.purchase

import androidx.compose.runtime.Composable

enum class PurchaseType(val rawValue: String) {
    Subscription("subscription"),
    Lifetime("lifetime"),
}

interface PurchaseManager {
    fun canUseInAppPurchase(): Boolean
    fun purchaseToken(): String?
    fun purchaseType(): PurchaseType?
    @Composable
    fun ManagerScreen(preferredPlayOfferId: String?)
}