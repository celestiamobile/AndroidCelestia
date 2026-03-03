package space.celestia.celestiaui.purchase

import androidx.fragment.app.Fragment

interface PurchaseManager {
    fun canUseInAppPurchase(): Boolean
    fun createInAppPurchaseFragment(preferredPlayOfferId: String?): Fragment?
    fun purchaseToken(): String?
}