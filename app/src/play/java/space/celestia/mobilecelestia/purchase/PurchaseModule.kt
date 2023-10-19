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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import space.celestia.mobilecelestia.utils.BaseResult
import java.lang.ref.WeakReference
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PurchaseModule {
    @Singleton
    @Provides
    fun providePurchaseManager(@ApplicationContext context: Context, purchaseAPI: PurchaseAPIService): PurchaseManager {
        val purchaseManager = PurchaseManager(context, purchaseAPI)
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

    @Singleton
    @Provides
    fun providePurchaseAPI(): PurchaseAPIService {
        return Retrofit.Builder()
            .baseUrl("https://celestia.mobi/api/subscription/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PurchaseAPIService::class.java)
    }
}


interface PurchaseAPIService {
    @GET("play")
    suspend fun subscriptionStatus(
        @Query("purchaseToken") purchaseToken: String,
    ): BaseResult
}
