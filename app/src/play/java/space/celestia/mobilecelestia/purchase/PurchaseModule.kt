package space.celestia.mobilecelestia.purchase

import android.content.Context
import androidx.annotation.Keep
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import space.celestia.celestiaui.purchase.PurchaseManager
import java.io.Serializable as JavaSerializable
import java.lang.ref.WeakReference
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PurchaseModule {
    @Singleton
    @Provides
    fun providePurchaseManager(@ApplicationContext context: Context, purchaseAPI: PurchaseAPIService): PurchaseManager {
        val purchaseManager = PurchaseManagerImpl(context, purchaseAPI)
        val weakManager = WeakReference(purchaseManager)
        val billingClient = BillingClient
            .newBuilder(context)
            .setListener(object: PurchasesUpdatedListener {
                override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
                    val self = weakManager.get() ?: return
                    self.onPurchasesUpdated(p0, p1)
                }
            })
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()
        purchaseManager.billingClient = billingClient
        purchaseManager.connectToService()
        return purchaseManager
    }

    @Singleton
    @Provides
    fun providePurchaseAPI(): PurchaseAPIService {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl("https://celestia.mobi/api/2/subscription/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PurchaseAPIService::class.java)
    }
}

@Keep
@Serializable
class PurchaseStatus(val valid: Boolean, val planId: String? = null): JavaSerializable

interface PurchaseAPIService {
    @GET("play")
    suspend fun subscriptionStatus(
        @Query("purchaseToken") purchaseToken: String,
        @Query("productType") productType: String
    ): PurchaseStatus
}
