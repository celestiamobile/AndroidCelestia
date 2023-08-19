package space.celestia.mobilecelestia.purchase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.utils.CelestiaString
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
    private fun AppIcon(modifier: Modifier = Modifier) {
        Image(
            painter = painterResource(id = R.drawable.loading_icon),
            contentDescription = null,
            modifier = modifier
                .size(128.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.Black)
                .padding(14.dp)
        )
    }

    @Composable
    private fun ErrorText(text: String, retry: () -> Unit) {
        Text(text = text)
        FilledTonalButton(onClick = {
            retry()
        }) {
            Text(text = "Retry")
        }
    }

    @Composable
    private fun MainScreen() {
        var subscriptionPlans by remember {
            mutableStateOf<List<PurchaseManager.Plan>?>(null)
        }
        var errorText by remember {
            mutableStateOf<String?>(null)
        }
        val scroll = rememberScrollState(0)
        Column(modifier = Modifier
            .verticalScroll(scroll)
            .padding(
                horizontal = dimensionResource(
                    id = R.dimen.common_page_small_margin_horizontal
                ),
                vertical = dimensionResource(
                    id = R.dimen.common_page_small_margin_vertical
                ),
            )) {
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                AppIcon()
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.common_page_medium_gap_vertical)))
            if (errorText != null) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f), horizontalArrangement = Arrangement.Center) {
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        ErrorText(text = errorText ?: "") {
                            errorText = null
                        }
                    }
                }
            } else if (subscriptionPlans == null) {
                LaunchedEffect(true) {
                    val subscriptionResult = purchaseManager.getSubscriptionDetails()
                    val subscription = subscriptionResult.subscription
                    if (subscriptionResult.billingResult.responseCode != BillingResponseCode.OK || subscription == null) {
                        errorText = "We encountered an error"
                    } else {
                        subscriptionPlans = subscription.plans
                    }
                }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f), horizontalArrangement = Arrangement.Center) {
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(progress = 0.5f)
                    }
                }
            } else {
                var insertSpace = false
                val plans = subscriptionPlans ?: listOf()
                for (index in plans.indices) {
                    PlanCard(plan = plans[index])
                    if (index != plans.size - 1) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.common_page_small_gap_vertical)))
                    }
                }
            }
        }
    }

    @Composable
    private fun PlanCard(plan: PurchaseManager.Plan) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(Color.Red)
            .padding(
                horizontal = dimensionResource(
                    id = R.dimen.common_page_small_margin_horizontal
                ),
                vertical = dimensionResource(
                    id = R.dimen.common_page_small_margin_vertical
                ),
            )) {
            Text(text = when (plan.type) {
                PurchaseManager.PlanType.Yearly -> CelestiaString("Yearly", "")
                PurchaseManager.PlanType.Monthly -> CelestiaString("Monthly", "")
            }, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
            Text(text = plan.formattedPrice, color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium), style = MaterialTheme.typography.bodyMedium)
        }
    }

    companion object {
        fun newInstance() = SubscriptionManagerFragment()
    }
}