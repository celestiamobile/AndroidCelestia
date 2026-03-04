package space.celestia.celestiaxr.di

import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.celestia.celestiaui.purchase.PurchaseManager
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