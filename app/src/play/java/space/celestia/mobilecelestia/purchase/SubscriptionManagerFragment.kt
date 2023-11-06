package space.celestia.mobilecelestia.purchase

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.utils.CelestiaString
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionManagerFragment: Fragment() {
    @Inject
    lateinit var purchaseManager: PurchaseManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
        }
    }

    @Composable
    private fun MainScreen() {
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        Column(modifier = Modifier
            .nestedScroll(nestedScrollInterop)
            .fillMaxWidth()) {
            Content(modifier = Modifier.weight(1.0f))
        }
    }

    @Composable
    private fun AppIcon(modifier: Modifier = Modifier) {
        Image(
            painter = painterResource(id = R.drawable.loading_icon),
            contentDescription = null,
            modifier = modifier
                .size(dimensionResource(id = R.dimen.app_icon_dimension))
        )
    }

    @Composable
    private fun ErrorText(text: String?, modifier: Modifier = Modifier, retry: () -> Unit) {
        EmptyHint(
            text = text ?: CelestiaString("We encountered an error.", ""),
            modifier = modifier,
            actionText = CelestiaString("Refresh", ""),
            actionHandler = retry
        )
    }

    @Composable
    private fun Content(modifier: Modifier = Modifier) {
        var needsRefreshing by remember {
            mutableStateOf(true)
        }
        var subscription by remember {
            mutableStateOf<PurchaseManager.Subscription?>(null)
        }
        var errorText by remember {
            mutableStateOf<String?>(null)
        }
        var subscriptionStatus by remember {
            mutableStateOf(purchaseManager.subscriptionStatus)
        }

        val listener by remember {
            mutableStateOf(object: PurchaseManager.Listener {
                override fun subscriptionStatusChanged(newStatus: PurchaseManager.SubscriptionStatus) {
                    subscriptionStatus = newStatus
                }
            })
        }
        val lifeCycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifeCycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_CREATE) {
                    purchaseManager.addListener(listener)
                } else if (event == Lifecycle.Event.ON_DESTROY) {
                    purchaseManager.removeListener(listener)
                }
            }
            lifeCycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifeCycleOwner.lifecycle.removeObserver(observer)
            }
        }

        val scroll = rememberScrollState(0)
        Column(modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .systemBarsPadding()
            .padding(
                horizontal = dimensionResource(id = R.dimen.common_page_small_margin_horizontal),
                vertical = dimensionResource(id = R.dimen.common_page_small_margin_vertical)
            )) {
            if (needsRefreshing) {
                LaunchedEffect(true) {
                    val subscriptionResult = purchaseManager.getSubscriptionDetails()
                    purchaseManager.getValidSubscription()
                    val subscriptionValue = subscriptionResult?.subscription
                    needsRefreshing = false
                    if (subscriptionResult == null || subscriptionResult.billingResult.responseCode != BillingResponseCode.OK || subscriptionValue == null || purchaseManager.subscriptionStatus is PurchaseManager.SubscriptionStatus.Error) {
                        errorText = CelestiaString("We encountered an error.", "")
                    } else {
                        subscription = subscriptionValue
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().weight(1.0f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorText != null || subscriptionStatus !is PurchaseManager.SubscriptionStatus.Good) {
                Box(modifier = Modifier.fillMaxWidth().weight(1.0f), contentAlignment = Alignment.Center) {
                    ErrorText(text = errorText) {
                        needsRefreshing = true
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    AppIcon()
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.common_page_large_gap_vertical)))

                Text(
                    text = CelestiaString("Celestia PLUS", ""),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.common_page_large_gap_vertical)))

                FeatureList()

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.common_page_medium_gap_vertical)))

                val status = subscriptionStatus as? PurchaseManager.SubscriptionStatus.Good
                val plans = subscription?.plans
                val productDetails = subscription?.productDetails
                if (status != null && plans != null && productDetails != null) {
                    PlanList(productDetails = productDetails, plans = plans, status = status)
                }
            }
        }
    }

    @Composable
    private fun FeatureList() {
        val features = arrayListOf(
            Pair(R.drawable.plus_feature_latest_update, CelestiaString("Get latest add-ons, updates, and trending add-ons.", "")),
            Pair(R.drawable.plus_feature_feedback, CelestiaString("Receive timely feedback on feature requests and bug reports.", "")),
            Pair(R.drawable.plus_feature_support, CelestiaString("Support the developer community and keep the project going.", ""))
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            features.add(0, Pair(R.drawable.plus_feature_customize, CelestiaString("Customize the visual appearance of Celestia.", "")))
        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.purchase_box_corner_radius)))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(
                horizontal = dimensionResource(
                    id = R.dimen.common_page_medium_margin_horizontal
                ),
                vertical = dimensionResource(
                    id = R.dimen.common_page_medium_margin_vertical
                ),
            )) {
            for (featureIndex in features.indices) {
                val feature = features[featureIndex]
                Feature(resource = feature.first, text = feature.second)
                if (featureIndex != features.size - 1) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.common_page_medium_gap_vertical)))
                }
            }
        }
    }
    
    @Composable
    private fun Feature(@DrawableRes resource: Int, text: String) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Image(painter = painterResource(id = resource), contentDescription = null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer), modifier = Modifier
                .padding(
                    dimensionResource(id = R.dimen.purchase_feature_icon_padding)
                )
                .size(
                    dimensionResource(id = R.dimen.purchase_feature_icon_size)
                ))
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.common_page_medium_gap_horizontal)))
            Text(text, color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.bodyLarge)
        }
    }

    @Composable
    private fun PlanList(productDetails: ProductDetails, plans: List<PurchaseManager.Plan>, status: PurchaseManager.SubscriptionStatus.Good) {
        val weakSelf = WeakReference(this)
        val text: String
        when (status) {
            is PurchaseManager.SubscriptionStatus.Good.None -> {
                text = CelestiaString("Choose one of the plans below to get Celestia PLUS", "")
            }
            is PurchaseManager.SubscriptionStatus.Good.Pending -> {
                text = CelestiaString("Your purchase is pending", "")
            }
            is PurchaseManager.SubscriptionStatus.Good.NotAcknowledged -> {
                text = CelestiaString("We are processing your purchase", "")
            }
            is PurchaseManager.SubscriptionStatus.Good.Acknowledged, is PurchaseManager.SubscriptionStatus.Good.NotVerified, is PurchaseManager.SubscriptionStatus.Good.Verified -> {
                text = CelestiaString("Congratulations, you are a Celestia PLUS user", "")
            }
        }
        Text(text = text, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.common_page_large_gap_vertical)))
        for (index in plans.indices) {
            val plan = plans[index]
            val token: String?
            val canAction: Boolean
            val actionTitle: String
            val actionButtonHidden: Boolean
            when (status) {
                is PurchaseManager.SubscriptionStatus.Good.None -> {
                    token = null
                    canAction = true
                    actionButtonHidden = false
                    actionTitle = CelestiaString("Get", "")
                }
                is PurchaseManager.SubscriptionStatus.Good.Pending -> {
                    token = status.purchaseToken
                    canAction = false
                    actionButtonHidden = false
                    actionTitle = CelestiaString("Get", "")
                }
                is PurchaseManager.SubscriptionStatus.Good.NotAcknowledged -> {
                    token = status.purchaseToken
                    canAction = false
                    actionButtonHidden = false
                    actionTitle = CelestiaString("Get", "")
                }
                is PurchaseManager.SubscriptionStatus.Good.Acknowledged -> {
                    token = status.purchaseToken
                    canAction = false
                    actionButtonHidden = false
                    actionTitle = CelestiaString("Change", "")
                }
                is PurchaseManager.SubscriptionStatus.Good.NotVerified -> {
                    token = status.purchaseToken
                    canAction = true
                    actionButtonHidden = false
                    actionTitle = CelestiaString("Change", "")
                }
                is PurchaseManager.SubscriptionStatus.Good.Verified -> {
                    token = status.purchaseToken
                    val currentPlan = status.plan
                    canAction = currentPlan != plan.type
                    actionButtonHidden = !canAction
                    if (currentPlan == null) {
                        actionTitle = CelestiaString("Change", "")
                    } else if (currentPlan.level < plan.type.level) {
                        actionTitle = CelestiaString("Upgrade", "")
                    } else  if (currentPlan.level > plan.type.level) {
                        actionTitle = CelestiaString("Downgrade", "")
                    } else {
                        actionTitle = ""
                    }
                }
            }
            PlanCard(plan = plan, actionButtonText = actionTitle, actionButtonEnabled = canAction, actionButtonHidden = actionButtonHidden) {
                val self = weakSelf.get() ?: return@PlanCard
                val activity = self.activity ?: return@PlanCard
                self.purchaseManager.createSubscription(plan, productDetails, token, activity)
            }
            if (index != plans.size - 1) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.common_page_medium_gap_vertical)))
            }
        }
        if (status is PurchaseManager.SubscriptionStatus.Good.NotVerified || status is PurchaseManager.SubscriptionStatus.Good.Verified || status is PurchaseManager.SubscriptionStatus.Good.Acknowledged) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.common_page_medium_gap_vertical)))
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val self = weakSelf.get() ?: return@FilledTonalButton
                    val activity = self.activity ?: return@FilledTonalButton
                    val url = self.purchaseManager.subscriptionManagementURL()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    val ai = intent.resolveActivityInfo(activity.packageManager, PackageManager.MATCH_DEFAULT_ONLY)
                    if (ai != null && ai.exported)
                        activity.startActivity(intent)
                }) {
                Text(text = CelestiaString("Manage Subscription", ""))
            }
        }
    }

    @Composable
    private fun PlanCard(plan: PurchaseManager.Plan, actionButtonText: String, actionButtonEnabled: Boolean, actionButtonHidden: Boolean, modifier: Modifier = Modifier, action: () -> Unit) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.purchase_box_corner_radius)))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(
                horizontal = dimensionResource(
                    id = R.dimen.common_page_small_margin_horizontal
                ),
                vertical = dimensionResource(
                    id = R.dimen.common_page_small_margin_vertical
                ),
            )) {
            Column {
                Text(text = when (plan.type) {
                    PurchaseManager.PlanType.Yearly -> CelestiaString("Yearly", "")
                    PurchaseManager.PlanType.Monthly -> CelestiaString("Monthly", "")
                }, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.common_page_small_gap_vertical)))
                Text(text = plan.formattedPrice, color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium), style = MaterialTheme.typography.bodyMedium)
            }
            if (!actionButtonHidden) {
                Button(onClick = action, enabled = actionButtonEnabled) {
                    Text(text = actionButtonText)
                }
            }
        }
    }

    companion object {
        fun newInstance() = SubscriptionManagerFragment()
    }
}