package space.celestia.mobilecelestia.purchase

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import space.celestia.mobilecelestia.utils.PreferenceManager
import java.lang.ref.WeakReference
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PurchaseModule {
    @Singleton
    @Provides
    fun providePurchaseManager(@ApplicationContext context: Context): PurchaseManager {
        val purchaseManager = PurchaseManager(context)
        val weakManager = WeakReference(purchaseManager)
        val billingClient = BillingClient
            .newBuilder(context)
            .setListener(object: PurchasesUpdatedListener {
                override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
                    val self = weakManager.get() ?: return
                    self.onPurchasesUpdated(p0, p1)
                }
            })
            .enablePendingPurchases()
            .build()
        purchaseManager.billingClient = billingClient
        purchaseManager.connectToService()
        return purchaseManager
    }
}