package space.celestia.mobilecelestia.purchase

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.compose.runtime.Composable
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
import space.celestia.celestiaui.purchase.PurchaseManager
import space.celestia.celestiaui.purchase.PurchaseType
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.mobilecelestia.BuildConfig
import space.celestia.mobilecelestia.R
import java.lang.ref.WeakReference
import java.time.Period
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PurchaseManagerImpl(context: Context, val purchaseAPI: PurchaseAPIService): PurchaseManager {
    internal var billingClient: BillingClient? = null
    private var connected = false

    private var dataStore: PreferenceManager = PreferenceManager(context, "celestia_plus")
    private var cachedPurchaseToken: String?
    private var cachedPurchaseType: PurchaseType?

    init {
        cachedPurchaseToken = dataStore[purchaseTokenCacheKey]
        cachedPurchaseType = dataStore[purchaseTypeCacheKey]?.let { value ->
            PurchaseType.entries.firstOrNull { it.rawValue == value }
        } ?: cachedPurchaseToken?.let { PurchaseType.Subscription }
    }

    override fun canUseInAppPurchase(): Boolean {
        return true
    }

    @Composable
    override fun ManagerScreen(preferredPlayOfferId: String?) {
        SubscriptionManagerScreen(preferredPlayOfferId)
    }
    override fun purchaseToken(): String? {
        return cachedPurchaseToken
    }

    override fun purchaseType(): PurchaseType? {
        return if (cachedPurchaseToken != null) cachedPurchaseType else null
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
            class NotAcknowledged(val purchaseToken: String, val productType: PurchaseType): Good()
            class Acknowledged(val purchaseToken: String, val productType: PurchaseType): Good()
            class NotVerified(val purchaseToken: String, val productType: PurchaseType): Good()
            class Verified(val purchaseToken: String, val productType: PurchaseType, val plan: PlanType?): Good()
        }
    }

    interface Listener {
        fun needsToFetchSubscriptionPlans()
        fun subscriptionStatusChanged(oldStatus: SubscriptionStatus, newStatus: SubscriptionStatus)
    }

    private val listeners: MutableSet<Listener> = mutableSetOf()

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    enum class PlanType(val level: Int) {
        Yearly(level = 2),
        Monthly(level = 1),
        Weekly(level = 0);

        val displayName: String
            get() = when(this) {
                Yearly -> CelestiaString("Yearly", "Yearly subscription")
                Monthly -> CelestiaString("Monthly", "Monthly subscription")
                Weekly -> CelestiaString("Weekly", "Weekly subscription")
            }
    }

    class Plan(val type: PlanType, val offerToken: String, val formattedPriceLine1: String, val formattedPriceLine2: String?, val productId: String, val offersFreeTrial: Boolean)
    class Subscription(val productDetails: ProductDetails, val plans: List<Plan>)
    class PlanResult(val billingResult: BillingResult, val subscription: Subscription?)
    class LifetimePlan(val productDetails: ProductDetails, val formattedPrice: String)
    class LifetimePlanResult(val billingResult: BillingResult, val lifetimePlan: LifetimePlan?)

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
            val productType = if (purchase.products.contains(lifetimeProductId)) PurchaseType.Lifetime else PurchaseType.Subscription
            handlePurchase(purchase = purchase, productType = productType)
        }
    }

    private fun handlePurchase(purchase: Purchase, productType: PurchaseType, handler: (() -> Unit)? = null) {
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
            changeSubscriptionStatus(SubscriptionStatus.Good.NotAcknowledged(purchase.purchaseToken, productType))
            val weakSelf = WeakReference(this)
            requireNotNull(billingClient).acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(), object: AcknowledgePurchaseResponseListener {
                override fun onAcknowledgePurchaseResponse(p0: BillingResult) {
                    val self = weakSelf.get() ?: return
                    if (p0.responseCode == BillingResponseCode.OK) {
                        self.changeSubscriptionStatus(SubscriptionStatus.Good.Acknowledged(purchase.purchaseToken, productType))
                        verifyStatus(purchase.purchaseToken, productType, handler)
                    } else {
                        self.changeSubscriptionStatus(SubscriptionStatus.Error.Billing(p0.responseCode))
                        if (handler != null)
                            handler()
                    }
                }
            })
        } else {
            changeSubscriptionStatus(SubscriptionStatus.Good.Acknowledged(purchase.purchaseToken, productType))
            verifyStatus(purchase.purchaseToken, productType, handler)
        }
    }

    fun createSubscription(plan: Plan, productDetails: ProductDetails, currentPurchaseToken: String?, activity: Activity) {
        if (!connected) return
        val productDetailsParamsBuilder = ProductDetailsParams.newBuilder().setProductDetails(productDetails).setOfferToken(plan.offerToken)
        val billingFlowParams: BillingFlowParams = if (currentPurchaseToken != null) {
            val replacementParams = ProductDetailsParams.SubscriptionProductReplacementParams.newBuilder().setOldProductId(productDetails.productId).setReplacementMode(ReplacementMode.WITHOUT_PRORATION).build()
            val subscriptionUpdateParams = SubscriptionUpdateParams.newBuilder().setOldPurchaseToken(currentPurchaseToken).build()
            BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productDetailsParamsBuilder.setSubscriptionProductReplacementParams(replacementParams).build())).setSubscriptionUpdateParams(subscriptionUpdateParams).build()
        } else {
            BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build())).build()
        }
        requireNotNull(billingClient).launchBillingFlow(activity, billingFlowParams)
    }

    fun createLifetimePurchase(productDetails: ProductDetails, activity: Activity) {
        if (!connected) return
        val productDetailsParamsBuilder = ProductDetailsParams.newBuilder().setProductDetails(productDetails)
        val billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build())).build()
        requireNotNull(billingClient).launchBillingFlow(activity, billingFlowParams)
    }

    private fun getValidSubscriptionAsync(handler: (() -> Unit)? = null) {
        val lifetimeQueryParams = QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()
        requireNotNull(billingClient).queryPurchasesAsync(lifetimeQueryParams) { lifetimeResult, lifetimePurchases ->
            val lifetimePurchase = if (lifetimeResult.responseCode == BillingResponseCode.OK) {
                lifetimePurchases.firstOrNull { it.products.contains(lifetimeProductId) }
            } else null
            if (lifetimePurchase != null) {
                handlePurchase(lifetimePurchase, PurchaseType.Lifetime) {
                    // After lifetime verification: Good.None means the server returned valid:false,
                    // so a co-existing subscription should still be considered. NotVerified means
                    // network/throw — trust on-device, no fallback.
                    if (subscriptionStatus is SubscriptionStatus.Good.None) {
                        querySubscriptionPurchasesAsync(handler)
                    } else {
                        if (handler != null)
                            handler()
                    }
                }
            } else {
                querySubscriptionPurchasesAsync(handler)
            }
        }
    }

    private fun querySubscriptionPurchasesAsync(handler: (() -> Unit)? = null) {
        val queryPurchasesParams = QueryPurchasesParams.newBuilder().setProductType(ProductType.SUBS).build()
        requireNotNull(billingClient).queryPurchasesAsync(queryPurchasesParams) { result, purchasesList ->
            if (result.responseCode != BillingResponseCode.OK) {
                changeSubscriptionStatus(SubscriptionStatus.Error.Billing(result.responseCode))
                if (handler != null)
                    handler()
            } else {
                val purchase = purchasesList.firstOrNull { it.products.contains(subscriptionId) }
                if (purchase != null) {
                    handlePurchase(purchase, PurchaseType.Subscription, handler)
                } else {
                    changeSubscriptionStatus(SubscriptionStatus.Good.None)
                    if (handler != null)
                        handler()
                }
            }
        }
    }

    private fun verifyStatus(purchaseToken: String, productType: PurchaseType, handler: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val status = purchaseAPI.subscriptionStatus(purchaseToken, productType.rawValue)
                if (status.valid) {
                    val plan = when (productType) {
                        PurchaseType.Lifetime -> null
                        PurchaseType.Subscription -> when (status.planId) {
                            yearlyPlanId -> PlanType.Yearly
                            monthlyPlanId -> PlanType.Monthly
                            weeklyPlanId -> PlanType.Weekly
                            else -> null
                        }
                    }
                    changeSubscriptionStatus(SubscriptionStatus.Good.Verified(purchaseToken, productType, plan))
                    for (listener in listeners) {
                        listener.needsToFetchSubscriptionPlans()
                    }
                } else {
                    changeSubscriptionStatus(SubscriptionStatus.Good.None)
                }
                if (handler != null)
                    handler()
            } catch (ignored: Throwable) {
                changeSubscriptionStatus(SubscriptionStatus.Good.NotVerified(purchaseToken, productType))
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

    suspend fun getLifetimePlanDetails(): LifetimePlanResult? {
        if (!connected) return null
        val lifetimeProduct = QueryProductDetailsParams.Product.newBuilder().setProductId(lifetimeProductId).setProductType(ProductType.INAPP).build()
        val queryDetailsParam = QueryProductDetailsParams.newBuilder().setProductList(listOf(lifetimeProduct)).build()
        val result = requireNotNull(billingClient).queryProductDetails(queryDetailsParam)
        val productLists = result.productDetailsList
        if (result.billingResult.responseCode != BillingResponseCode.OK || productLists == null || productLists.size != 1) {
            return LifetimePlanResult(result.billingResult, null)
        }
        val product = productLists[0]
        val price = product.oneTimePurchaseOfferDetails?.formattedPrice ?: return LifetimePlanResult(result.billingResult, null)
        return LifetimePlanResult(result.billingResult, LifetimePlan(product, price))
    }

    suspend fun getSubscriptionDetails(preferredPlayOfferId: String?, resources: Resources): PlanResult? {
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
        val preferredYearlyPlan = if (preferredPlayOfferId != null) product.subscriptionOfferDetails?.firstOrNull { it.offerId == preferredPlayOfferId && it.basePlanId == yearlyPlanId } else null
        val yearlyPlan = preferredYearlyPlan ?: product.subscriptionOfferDetails?.firstOrNull { it.basePlanId == yearlyPlanId }
        if (yearlyPlan != null) {
            plans.add(Plan(PlanType.Yearly, yearlyPlan.offerToken, yearlyPlan.formattedPriceLine1(resources), yearlyPlan.formattedPriceLine2(resources), product.productId, yearlyPlan.offersFreeTrial()))
        }
        val preferredMonthlyPlan = if (preferredPlayOfferId != null) product.subscriptionOfferDetails?.firstOrNull { it.offerId == preferredPlayOfferId && it.basePlanId == monthlyPlanId } else null
        val monthlyPlan = preferredMonthlyPlan ?: product.subscriptionOfferDetails?.firstOrNull { it.basePlanId == monthlyPlanId }
        if (monthlyPlan != null) {
            plans.add(Plan(PlanType.Monthly, monthlyPlan.offerToken, monthlyPlan.formattedPriceLine1(resources), monthlyPlan.formattedPriceLine2(resources), product.productId, monthlyPlan.offersFreeTrial()))
        }
        val preferredWeeklyPlan = if (preferredPlayOfferId != null) product.subscriptionOfferDetails?.firstOrNull { it.offerId == preferredPlayOfferId && it.basePlanId == weeklyPlanId } else null
        val weeklyPlan = preferredWeeklyPlan ?: product.subscriptionOfferDetails?.firstOrNull { it.basePlanId == weeklyPlanId }
        if (weeklyPlan != null) {
            plans.add(Plan(PlanType.Weekly, weeklyPlan.offerToken, weeklyPlan.formattedPriceLine1(resources), weeklyPlan.formattedPriceLine2(resources), product.productId, weeklyPlan.offersFreeTrial()))
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
        val oldStatus = subscriptionStatus
        val isDifferent = subscriptionStatus != newStatus
        subscriptionStatus = newStatus
        if (isDifferent) {
            data class PendingUpdate(val token: String?, val productType: PurchaseType?)
            val pending: PendingUpdate? = when (newStatus) {
                is SubscriptionStatus.Error, SubscriptionStatus.Good.None -> {
                    PendingUpdate(null, null)
                }
                is SubscriptionStatus.Good.Verified -> {
                    PendingUpdate(newStatus.purchaseToken, newStatus.productType)
                }
                is SubscriptionStatus.Good.NotVerified -> {
                    PendingUpdate(newStatus.purchaseToken, newStatus.productType)
                }
                else -> {
                    null
                }
            }

            if (pending != null) {
                if (pending.token != cachedPurchaseToken || pending.productType != cachedPurchaseType) {
                    cachedPurchaseToken = pending.token
                    cachedPurchaseType = pending.productType
                    dataStore[purchaseTokenCacheKey] = pending.token
                    dataStore[purchaseTypeCacheKey] = pending.productType?.rawValue
                }
            }

            for (listener in listeners) {
                listener.subscriptionStatusChanged(oldStatus = oldStatus, newStatus = newStatus)
            }
        }
    }

    private companion object {
        const val subscriptionId = "space.celestia.mobilecelestia.plus"
        const val lifetimeProductId = "space.celestia.mobilecelestia.plus.lifetime"
        const val weeklyPlanId = "celestia-plus-weekly"
        const val monthlyPlanId = "celestia-plus-monthly"
        const val yearlyPlanId = "celestia-plus-yearly"
        val purchaseTokenCacheKey = PreferenceManager.CustomKey("purchase_token_cache")
        val purchaseTypeCacheKey = PreferenceManager.CustomKey("purchase_type_cache")
    }
}

private sealed interface Duration {
    val value: Int

    data class Year(override val value: Int) : Duration
    data class Month(override val value: Int) : Duration
    data class Week(override val value: Int) : Duration
    data class Day(override val value: Int) : Duration

    companion object {
        fun fromString(value: String): Duration? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val period = Period.parse(value)
                    if (period.years > 0) {
                        return Year(period.years)
                    } else if (period.months > 0) {
                        return Month(period.months)
                    } else {
                        val days = period.days
                        if (days > 0) {
                            return if (days % 7 == 0) {
                                Week(days / 7)
                            } else {
                                Day(days)
                            }
                        }
                        return null
                    }

                } catch (_: Throwable) {
                    return null
                }
            } else {
                return if (value.contains("Y")) {
                    Year(1)
                } else if (value.contains("M")) {
                    Month(1)
                } else if (value.contains("W")) {
                    Week(1)
                } else if (value.contains("D")) {
                    Day(1)
                } else {
                    null
                }
            }
        }
    }

    val singleCyclePriceTemplate: Int
        get() {
            return when (this) {
                is Year -> {
                    R.string.subscription_offer_price_template_per_year
                }
                is Month -> {
                    R.string.subscription_offer_price_template_per_month
                }
                is Week -> {
                    R.string.subscription_offer_price_template_per_week
                }
                is Day -> {
                    R.string.subscription_offer_price_template_per_day
                }
            }
        }

    val multipleCyclePriceTemplate: Int
        get() {
            return when (this) {
                is Year -> {
                    R.plurals.subscription_offer_price_template_per_year_multiple
                }
                is Month -> {
                    R.plurals.subscription_offer_price_template_per_month_multiple
                }
                is Week -> {
                    R.plurals.subscription_offer_price_template_per_week_multiple
                }
                is Day -> {
                    R.plurals.subscription_offer_price_template_per_day_multiple
                }
            }
        }

    val offerPriceTemplate: Int
        get() {
            return when (this) {
                is Year -> {
                    R.plurals.subscription_offer_price_with_phase_template_per_year
                }

                is Month -> {
                    R.plurals.subscription_offer_price_with_phase_template_per_month
                }

                is Week -> {
                    R.plurals.subscription_offer_price_with_phase_template_per_week
                }

                is Day -> {
                    R.plurals.subscription_offer_price_with_phase_template_per_day
                }
            }
        }
}

private fun ProductDetails.SubscriptionOfferDetails.formattedPriceLine1(resources: Resources): String {
    val phases = this.pricingPhases.pricingPhaseList
    if (phases.isEmpty()) return resources.getString(R.string.subscription_offer_price_unavailable)
    val phase = phases[0]
    if (phases.size < 2)
        return phase.cyclePrice(resources)

    val duration = Duration.fromString(phase.billingPeriod) ?: return resources.getString(R.string.subscription_offer_price_unavailable)
    val count = duration.value * phase.billingCycleCount
    return resources.getQuantityString(
        duration.offerPriceTemplate,
        count,
        phase.cyclePrice(resources),
        count
    )
}

private fun ProductDetails.SubscriptionOfferDetails.formattedPriceLine2(resources: Resources): String? {
    val phases = this.pricingPhases.pricingPhaseList
    if (phases.size < 2) return null
    val phase = phases[1]
    return resources.getString(R.string.subscription_offer_price_template_thereafter, phase.cyclePrice(resources))
}

private fun ProductDetails.SubscriptionOfferDetails.offersFreeTrial(): Boolean {
    val phases = this.pricingPhases.pricingPhaseList
    if (phases.size < 2) return false
    val phase = phases[0]
    return phase.priceAmountMicros == 0L
}

private fun ProductDetails.PricingPhase.cyclePrice(resources: Resources): String {
    if (priceAmountMicros == 0L)
        return CelestiaString("Free", "Subscription price indicating the subscription is free")

    val duration = Duration.fromString(billingPeriod) ?: return resources.getString(R.string.subscription_offer_price_unavailable)
    if (duration.value == 1) {
        return resources.getString(duration.singleCyclePriceTemplate, formattedPrice)
    }
    return resources.getQuantityString(duration.multipleCyclePriceTemplate, duration.value, formattedPrice, duration.value)
}