package space.celestia.mobilecelestia.purchase

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

class PurchaseManager {
    fun canUseInAppPurchase(): Boolean {
        return false
    }

    fun createInAppPurchaseFragment(): Fragment? {
        return null
    }

    fun purchaseToken(): String? {
        return null
    }

    data object SubscriptionStatus

    interface Listener {
        fun subscriptionStatusChanged(newStatus: SubscriptionStatus)
    }

    fun addListener(listener: Listener) {}
    fun removeListener(listener: Listener) {}
}

@Module
@InstallIn(SingletonComponent::class)
class PurchaseModule {
    @Singleton
    @Provides
    fun providePurchaseManager(): PurchaseManager {
        return PurchaseManager()
    }
}