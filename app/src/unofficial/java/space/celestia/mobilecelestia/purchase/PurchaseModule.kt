package space.celestia.mobilecelestia.purchase

import androidx.compose.runtime.Composable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.celestia.celestiaui.purchase.PurchaseManager
import space.celestia.celestiaui.purchase.PurchaseType
import javax.inject.Singleton

class PurchaseManagerImpl: PurchaseManager {
    override fun canUseInAppPurchase(): Boolean {
        return false
    }

    @Composable
    override fun ManagerScreen(preferredPlayOfferId: String?) {}

    override fun purchaseToken(): String? {
        return null
    }

    override fun purchaseType(): PurchaseType? {
        return null
    }
}

@Module
@InstallIn(SingletonComponent::class)
class PurchaseModule {
    @Singleton
    @Provides
    fun providePurchaseManager(): PurchaseManager {
        return PurchaseManagerImpl()
    }
}