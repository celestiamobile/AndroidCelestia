package space.celestia.mobilecelestia.purchase

import android.app.Activity
import androidx.fragment.app.Fragment
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams.ReplacementMode
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import space.celestia.mobilecelestia.BuildConfig
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PurchaseManager {
    internal var billingClient: BillingClient? = null
    private var connected = false

    fun canUseInAppPurchase(): Boolean {
        return true
    }

    fun createInAppPurchaseFragment(): Fragment? {
        return SubscriptionManagerFragment.newInstance()
    }

    fun purchaseToken(): String? {
        val status = subscriptionStatus
        if (status is SubscriptionStatus.Good.Acknowledged) {
            return status.purchaseToken
        }
        return null
    }

    var subscriptionStatus: SubscriptionStatus = SubscriptionStatus.Error.NotConnected
        private set(value) {
            val isDifferent = field != value
            field = value
            if (isDifferent) {
                for (listener in listeners) {
                    listener.subscriptionStatusChanged(value)
                }
            }
        }

    sealed class SubscriptionStatus {
        sealed class Error: SubscriptionStatus() {
            data object NotConnected: Error()
            @Suppress("UNUSED_PARAMETER")
            class Billing(responseCode: Int): Error()
            data object Unknown: Error()
        }

        data object Connected: SubscriptionStatus()

        sealed class Good: SubscriptionStatus() {
            data object None: Good()

            class Pending(val purchaseToken: String) : Good()
            class NotAcknowledged(val purchaseToken: String): Good()
            class Acknowledged(val purchaseToken: String): Good()
        }
    }

    interface Listener {
        fun subscriptionStatusChanged(newStatus: SubscriptionStatus)
    }

    private val listeners: MutableSet<Listener> = mutableSetOf()

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    enum class PlanType {
        Yearly,
        Monthly
    }

    class Plan(val type: PlanType, val offerToken: String, val formattedPrice: String)
    class Subscription(val productDetails: ProductDetails, val plans: List<Plan>)
    class PlanResult(val billingResult: BillingResult, val subscription: Subscription?)

    internal fun connectToService() {
        val weakSelf = WeakReference(this)
        requireNotNull(billingClient).startConnection(object: BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                val self = weakSelf.get() ?: return
                self.connected = false
                self.subscriptionStatus = SubscriptionStatus.Error.NotConnected
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                val self = weakSelf.get() ?: return
                if (p0.responseCode == BillingResponseCode.OK) {
                    self.connected = true
                    self.subscriptionStatus = SubscriptionStatus.Connected
                    self.getValidSubscriptionAsync()
                }
            }
        })
    }

    internal fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode != BillingResponseCode.OK) return
        if (purchases == null) return
        for (purchase in purchases) {
            handlePurchase(purchase = purchase)
        }
    }

    private fun handlePurchase(purchase: Purchase, handler: (() -> Unit)? = null) {
        if (purchase.purchaseState == PurchaseState.PENDING) {
            subscriptionStatus = SubscriptionStatus.Good.Pending(purchase.purchaseToken)
            if (handler != null)
                handler()
            return
        }
        if (purchase.purchaseState != PurchaseState.PURCHASED) {
            subscriptionStatus = SubscriptionStatus.Error.Unknown
            if (handler != null)
                handler()
            return
        }
        if (!purchase.isAcknowledged) {
            subscriptionStatus = SubscriptionStatus.Good.NotAcknowledged(purchase.purchaseToken)
            val weakSelf = WeakReference(this)
            requireNotNull(billingClient).acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(), object: AcknowledgePurchaseResponseListener {
                override fun onAcknowledgePurchaseResponse(p0: BillingResult) {
                    val self = weakSelf.get() ?: return
                    if (p0.responseCode == BillingResponseCode.OK) {
                        self.subscriptionStatus = SubscriptionStatus.Good.Acknowledged(purchase.purchaseToken)
                        if (handler != null)
                            handler()
                    } else {
                        self.subscriptionStatus = SubscriptionStatus.Error.Billing(p0.responseCode)
                        if (handler != null)
                            handler()
                    }
                }
            })
        } else {
            subscriptionStatus = SubscriptionStatus.Good.Acknowledged(purchase.purchaseToken)
            if (handler != null)
                handler()
        }
    }

    fun createSubscription(plan: Plan, productDetails: ProductDetails, currentPurchaseToken: String?, activity: Activity) {
        if (!connected) return
        val productDetailsParams = ProductDetailsParams.newBuilder().setProductDetails(productDetails).setOfferToken(plan.offerToken).build()
        val billingFlowParams: BillingFlowParams = if (currentPurchaseToken != null) {
            val subscriptionUpdateParams = SubscriptionUpdateParams.newBuilder().setOldPurchaseToken(currentPurchaseToken).setSubscriptionReplacementMode(ReplacementMode.WITHOUT_PRORATION).build()
            BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productDetailsParams)).setSubscriptionUpdateParams(subscriptionUpdateParams).build()
        } else {
            BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productDetailsParams)).build()
        }
        requireNotNull(billingClient).launchBillingFlow(activity, billingFlowParams)
    }

    private fun getValidSubscriptionAsync(handler: (() -> Unit)? = null) {
        val queryPurchasesParams = QueryPurchasesParams.newBuilder().setProductType(ProductType.SUBS).build()
        requireNotNull(billingClient).queryPurchasesAsync(queryPurchasesParams) { result, purchasesList ->
            if (result.responseCode != BillingResponseCode.OK) {
                subscriptionStatus = SubscriptionStatus.Error.Billing(result.responseCode)
                if (handler != null)
                    handler()
            } else {
                val purchase = purchasesList.firstOrNull { it.products.contains(subscriptionId) }
                if (purchase != null) {
                    handlePurchase(purchase, handler)
                } else {
                    subscriptionStatus = SubscriptionStatus.Good.None
                    if (handler != null)
                        handler()
                }
            }
        }
    }
    
    suspend fun getValidSubscription(): Unit = suspendCoroutine { cont ->
        if (!connected) {
            cont.resume(Unit)
            return@suspendCoroutine
        }
       getValidSubscriptionAsync {
           cont.resume(Unit)
       }
    }

    suspend fun getSubscriptionDetails(): PlanResult? {
        if (!connected)
            return null
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
                plans.add(Plan(PlanType.Yearly, yearlyPlan.offerToken, yearlyPlanPrice))
            }
        }
        val monthlyPlan = product.subscriptionOfferDetails?.firstOrNull { it.basePlanId == monthlyPlanId }
        if (monthlyPlan != null) {
            val monthlyPlanPrice = monthlyPlan.pricingPhases.pricingPhaseList.lastOrNull()?.formattedPrice
            if (monthlyPlanPrice != null) {
                plans.add(Plan(PlanType.Monthly, monthlyPlan.offerToken, monthlyPlanPrice))
            }
        }
        if (plans.isEmpty()) {
            return PlanResult(result.billingResult, null)
        }
        return PlanResult(result.billingResult, Subscription(product, plans))
    }

    fun subscriptionManagementURL(): String {
        // https://developer.android.com/google/play/billing/subscriptions#deep-link
        return "https://play.google.com/store/account/subscriptions?sku=${subscriptionId}&package=${BuildConfig.APPLICATION_ID}"
    }

    private companion object {
        const val subscriptionId = "space.celestia.mobilecelestia.plus"
        const val monthlyPlanId = "celestia-plus-monthly"
        const val yearlyPlanId = "celestia-plus-yearly"
    }
}