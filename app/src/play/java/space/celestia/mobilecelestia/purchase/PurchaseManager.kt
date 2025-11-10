package space.celestia.mobilecelestia.purchase

import android.app.Activity
import android.content.Context
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import space.celestia.mobilecelestia.BuildConfig
import space.celestia.mobilecelestia.utils.PreferenceManager
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PurchaseManager(context: Context, val purchaseAPI: PurchaseAPIService) {
    internal var billingClient: BillingClient? = null
    private var connected = false

    private var dataStore: PreferenceManager = PreferenceManager(context, "celestia_plus")
    private var cachedPurchaseToken: String?

    init {
        cachedPurchaseToken = dataStore[purchaseTokenCacheKey]
    }

    fun canUseInAppPurchase(): Boolean {
        return true
    }

    fun createInAppPurchaseFragment(): Fragment? {
        return SubscriptionManagerFragment.newInstance()
    }

    fun purchaseToken(): String? {
        return cachedPurchaseToken
    }

    var subscriptionStatus: SubscriptionStatus = SubscriptionStatus.Error.NotConnected
        private set

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
            class NotVerified(val purchaseToken: String): Good()
            class Verified(val purchaseToken: String, val plan: PlanType?): Good()
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

    enum class PlanType(val level: Int) {
        Yearly(1),
        Monthly(0)
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
                self.changeSubscriptionStatus(SubscriptionStatus.Error.NotConnected)
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                val self = weakSelf.get() ?: return
                if (p0.responseCode == BillingResponseCode.OK) {
                    self.connected = true
                    self.changeSubscriptionStatus(SubscriptionStatus.Connected)
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
            changeSubscriptionStatus(SubscriptionStatus.Good.Pending(purchase.purchaseToken))
            if (handler != null)
                handler()
            return
        }
        if (purchase.purchaseState != PurchaseState.PURCHASED) {
            changeSubscriptionStatus(SubscriptionStatus.Error.Unknown)
            if (handler != null)
                handler()
            return
        }
        if (!purchase.isAcknowledged) {
            changeSubscriptionStatus(SubscriptionStatus.Good.NotAcknowledged(purchase.purchaseToken))
            val weakSelf = WeakReference(this)
            requireNotNull(billingClient).acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(), object: AcknowledgePurchaseResponseListener {
                override fun onAcknowledgePurchaseResponse(p0: BillingResult) {
                    val self = weakSelf.get() ?: return
                    if (p0.responseCode == BillingResponseCode.OK) {
                        self.changeSubscriptionStatus(SubscriptionStatus.Good.Acknowledged(purchase.purchaseToken))
                        verifyStatus(purchase.purchaseToken, handler)
                    } else {
                        self.changeSubscriptionStatus(SubscriptionStatus.Error.Billing(p0.responseCode))
                        if (handler != null)
                            handler()
                    }
                }
            })
        } else {
            changeSubscriptionStatus(SubscriptionStatus.Good.Acknowledged(purchase.purchaseToken))
            verifyStatus(purchase.purchaseToken, handler)
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
                changeSubscriptionStatus(SubscriptionStatus.Error.Billing(result.responseCode))
                if (handler != null)
                    handler()
            } else {
                val purchase = purchasesList.firstOrNull { it.products.contains(subscriptionId) }
                if (purchase != null) {
                    handlePurchase(purchase, handler)
                } else {
                    changeSubscriptionStatus(SubscriptionStatus.Good.None)
                    if (handler != null)
                        handler()
                }
            }
        }
    }

    private fun verifyStatus(purchaseToken: String, handler: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val status = purchaseAPI.subscriptionStatus(purchaseToken)
                if (status.valid) {
                    val plan = when (status.planId) {
                        yearlyPlanId -> PlanType.Yearly
                        monthlyPlanId -> PlanType.Monthly
                        else -> null
                    }
                    changeSubscriptionStatus(SubscriptionStatus.Good.Verified(purchaseToken, plan))
                } else {
                    changeSubscriptionStatus(SubscriptionStatus.Good.None)
                }
                if (handler != null)
                    handler()
            } catch (ignored: Throwable) {
                changeSubscriptionStatus(SubscriptionStatus.Good.NotVerified(purchaseToken))
                if (handler != null)
                    handler()
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

    private fun changeSubscriptionStatus(newStatus: SubscriptionStatus) {
        val isDifferent = subscriptionStatus != newStatus
        subscriptionStatus = newStatus
        if (isDifferent) {
            val (newPurchaseToken: String?, updateValue: Boolean) = when (newStatus) {
                is SubscriptionStatus.Error, SubscriptionStatus.Good.None -> {
                    Pair(null, true)
                }
                is SubscriptionStatus.Good.Verified -> {
                    Pair(newStatus.purchaseToken, true)
                }
                is SubscriptionStatus.Good.NotVerified -> {
                    Pair(newStatus.purchaseToken, true)
                }
                else -> {
                    Pair(null, false)
                }
            }

            if (updateValue) {
                if (newPurchaseToken != cachedPurchaseToken) {
                    cachedPurchaseToken = newPurchaseToken
                    dataStore[purchaseTokenCacheKey] = newPurchaseToken
                }
            }

            for (listener in listeners) {
                listener.subscriptionStatusChanged(newStatus)
            }
        }
    }

    private companion object {
        const val subscriptionId = "space.celestia.mobilecelestia.plus"
        const val monthlyPlanId = "celestia-plus-monthly"
        const val yearlyPlanId = "celestia-plus-yearly"
        val purchaseTokenCacheKey = PreferenceManager.CustomKey("purchase_token_cache")
    }
}