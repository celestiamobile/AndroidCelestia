package space.celestia.mobilecelestia.purchase

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import java.lang.ref.WeakReference

class PurchaseManager() {
    internal var billingClient: BillingClient? = null
    internal var connected = false

    enum class PlanType {
        Yearly,
        Monthly
    }

    class Plan(val type: PlanType, val formattedPrice: String)
    class Subscription(val plans: List<Plan>)
    class PlanResult(val billingResult: BillingResult, val subscription: Subscription?)

    internal fun connectToService() {
        val weakSelf = WeakReference(this)
        requireNotNull(billingClient).startConnection(object: BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                val self = weakSelf.get() ?: return
                self.connected = false
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                connected = p0.responseCode == BillingResponseCode.OK
            }
        })
    }

    internal fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {

    }

    suspend fun getSubscriptionDetails(): PlanResult {
        val subscriptionProduct = QueryProductDetailsParams.Product.newBuilder().setProductId(
            subscriptionId).setProductType(ProductType.SUBS).build()
        val queryDetailsParam = QueryProductDetailsParams.newBuilder().setProductList(listOf(subscriptionProduct)).build()
        val result = requireNotNull(billingClient).queryProductDetails(queryDetailsParam)
        val productLists = result.productDetailsList
        if (result.billingResult.responseCode != BillingResponseCode.OK || productLists == null || productLists.size != 1) {
            return PlanResult(result.billingResult, null)
        }
        val product = productLists[0]
        val plans = arrayListOf<Plan>()
        val yearlyPlan = product.subscriptionOfferDetails?.firstOrNull { it.basePlanId == yearlyPlanId }
        if (yearlyPlan != null) {
            val yearlyPlanPrice = yearlyPlan.pricingPhases.pricingPhaseList.lastOrNull()?.formattedPrice
            if (yearlyPlanPrice != null) {
                plans.add(Plan(PlanType.Yearly, yearlyPlanPrice))
            }
        }
        val monthlyPlan = product.subscriptionOfferDetails?.firstOrNull { it.basePlanId == monthlyPlanId }
        if (monthlyPlan != null) {
            val monthlyPlanPrice = monthlyPlan.pricingPhases.pricingPhaseList.lastOrNull()?.formattedPrice
            if (monthlyPlanPrice != null) {
                plans.add(Plan(PlanType.Monthly, monthlyPlanPrice))
            }
        }
        if (plans.isEmpty()) {
            return PlanResult(result.billingResult, null)
        }
        return PlanResult(result.billingResult, Subscription(plans))
    }

    private companion object {
        const val subscriptionId = "space.celestia.mobilecelestia.plus"
        const val monthlyPlanId = "celestia-plus-monthly"
        const val yearlyPlanId = "celestia-plus-yearly"
    }
}