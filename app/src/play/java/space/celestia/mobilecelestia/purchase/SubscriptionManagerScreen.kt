package space.celestia.mobilecelestia.purchase

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.annotation.RawRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import space.celestia.celestiaui.compose.EmptyHint
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.purchase.viewmodel.SubscriptionManagerViewModel
import kotlin.math.PI
import kotlin.math.tan
import kotlin.random.Random

@Composable
fun SubscriptionManagerScreen(preferredPlayOfferId: String?) {
    val nestedScrollInterop = rememberNestedScrollInteropConnection()
    Column(
        modifier = Modifier
        .nestedScroll(nestedScrollInterop)
        .fillMaxWidth()) {
        Content(preferredPlayOfferId = preferredPlayOfferId, modifier = Modifier.weight(1.0f))
    }
}

@Composable
private fun AppIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = space.celestia.celestiaui.R.drawable.loading_icon),
        contentDescription = null,
        modifier = modifier
            .size(dimensionResource(id = space.celestia.celestiaui.R.dimen.app_icon_dimension))
    )
}

@Composable
private fun ErrorText(text: String?, modifier: Modifier = Modifier, retry: () -> Unit) {
    EmptyHint(
        text = text ?: CelestiaString("We encountered an error.", "Error loading the subscription page"),
        modifier = modifier,
        actionText = CelestiaString("Refresh", "Button to refresh this list"),
        actionHandler = retry
    )
}

@Composable
private fun Content(preferredPlayOfferId: String?, modifier: Modifier = Modifier) {
    data class RefreshState(val needsRefreshPlans: Boolean, val needsRefreshSubscription: Boolean)

    val viewModel: SubscriptionManagerViewModel = hiltViewModel()
    var refreshState by remember {
        mutableStateOf(RefreshState(needsRefreshPlans = true, needsRefreshSubscription = true))
    }
    var subscription by remember {
        mutableStateOf<PurchaseManagerImpl.Subscription?>(null)
    }
    var errorText by remember {
        mutableStateOf<String?>(null)
    }
    val purchaseManager by remember { mutableStateOf(viewModel.purchaseManager as PurchaseManagerImpl )}
    var subscriptionStatus by remember {
        mutableStateOf(purchaseManager.subscriptionStatus)
    }

    val listener by remember {
        mutableStateOf(object: PurchaseManagerImpl.Listener {
            override fun subscriptionStatusChanged(oldStatus: PurchaseManagerImpl.SubscriptionStatus, newStatus: PurchaseManagerImpl.SubscriptionStatus) {
                subscriptionStatus = newStatus
            }

            override fun needsToFetchSubscriptionPlans() {
                refreshState = RefreshState(needsRefreshPlans = true, needsRefreshSubscription = false)
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

    val resources = LocalResources.current
    val scroll = rememberScrollState(0)

    LaunchedEffect(refreshState) {
        if (!refreshState.needsRefreshSubscription && !refreshState.needsRefreshPlans)
            return@LaunchedEffect

        if (refreshState.needsRefreshPlans) {
            val subscriptionResult = purchaseManager.getSubscriptionDetails(preferredPlayOfferId, resources)
            val subscriptionValue = subscriptionResult?.subscription
            if (subscriptionResult == null || subscriptionResult.billingResult.responseCode != BillingResponseCode.OK || subscriptionValue == null || purchaseManager.subscriptionStatus is PurchaseManagerImpl.SubscriptionStatus.Error) {
                errorText = CelestiaString("We encountered an error.", "Error loading the subscription page")
                subscription = null
            } else {
                errorText = null
                subscription = subscriptionValue
            }
        }
        if (refreshState.needsRefreshSubscription) {
            purchaseManager.getValidSubscription()
        }
        refreshState = RefreshState(needsRefreshSubscription = false, needsRefreshPlans = false)
    }

    Scaffold(contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Bottom)) { paddingValues ->
        if (refreshState.needsRefreshSubscription || refreshState.needsRefreshPlans) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorText != null || subscriptionStatus !is PurchaseManagerImpl.SubscriptionStatus.Good) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center
            ) {
                ErrorText(text = errorText) {
                    refreshState = RefreshState(
                        needsRefreshPlans = true,
                        needsRefreshSubscription = true
                    )
                }
            }
        } else {

            val direction = LocalLayoutDirection.current
            val contentPadding = PaddingValues(
                start = dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_small_margin_horizontal) + paddingValues.calculateStartPadding(
                    direction
                ),
                top = dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_small_margin_vertical) + paddingValues.calculateTopPadding(),
                end = dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_small_margin_horizontal) + paddingValues.calculateEndPadding(
                    direction
                ),
                bottom = dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_small_margin_vertical) + paddingValues.calculateBottomPadding(),
            )
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(contentPadding)
            ) {

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppIcon()
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_large_gap_vertical)))

                Text(
                    text = CelestiaString(
                        "Celestia PLUS",
                        "Name for the subscription service"
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_large_gap_vertical)))

                VideoCarousel()

                Spacer(modifier = Modifier.height(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_medium_gap_vertical)))

                val status = subscriptionStatus as? PurchaseManagerImpl.SubscriptionStatus.Good
                val plans = subscription?.plans
                val productDetails = subscription?.productDetails
                if (status != null && plans != null && productDetails != null) {
                    PlanList(
                        purchaseManager = purchaseManager,
                        productDetails = productDetails,
                        plans = plans,
                        status = status
                    )
                }
            }
        }
    }
}

private sealed class CarouselItem {
    abstract val title: String
    data class Video(@param: RawRes val videoResId: Int, override val title: String) : CarouselItem()
    data class Support(override val title: String, val message: String) : CarouselItem()
}

@Composable
private fun VideoCarousel() {
    val items = listOf(
        CarouselItem.Video(R.raw.toolbar_android, CelestiaString("Toolbar Customization", "Description for toolbar customization video")),
        CarouselItem.Video(R.raw.font, CelestiaString("Custom Fonts", "Description for custom font video")),
        CarouselItem.Video(R.raw.search, CelestiaString("Search Add-ons", "Description for search add-ons video")),
        CarouselItem.Video(R.raw.addon_updates, CelestiaString("Add-on Updates", "Description for addon updates video")),
        CarouselItem.Support(
            title = CelestiaString("Support the Project", "Description for support project card"),
            message = CelestiaString("By subscribing to Celestia PLUS, you directly support the developers and keep this project alive. You'll also receive timely feedback on feature requests and bug reports!", "Message on support project card")
        )
    )

    val pagerState = rememberPagerState(pageCount = { items.count() })
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            pageSpacing = 16.dp,
        ) { i ->
            val item = items[i]
            Column(modifier = Modifier.fillMaxWidth()) {
                when (item) {
                    is CarouselItem.Video -> {
                        VideoItem(
                            videoResId = item.videoResId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(MaterialTheme.shapes.extraLarge)
                        )
                    }
                    is CarouselItem.Support -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            FloatingIcons(modifier = Modifier.fillMaxSize())
                            Text(
                                text = item.message,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_medium_margin_horizontal))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_small_gap_vertical)))
                Text(
                    text = item.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_medium_gap_vertical)))
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

private data class FloatingIconData(
    val id: Long,
    val size: Float,
    val startX: Float,
    val endX: Float,
    val duration: Int,
    val isHeart: Boolean,
    val rotationTarget: Float
)

@Composable
private fun FloatingIcons(modifier: Modifier = Modifier) {
    val icons = remember { mutableStateListOf<FloatingIconData>() }

    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val width = maxWidth.value
        val height = maxHeight.value

        LaunchedEffect(width, height) {
            if (width == 0f || height == 0f) return@LaunchedEffect
            var nextId = 0L
            while (true) {
                delay((300..700).random().toLong())

                val size = (24..36).random().toFloat()
                val startX = Random.nextFloat() * width
                val angleRad = Random.nextDouble(-30.0, 30.0) * PI / 180.0
                val endX = startX + ((height + 50f) * tan(angleRad)).toFloat()
                val duration = (8000..12000).random()
                val isHeart = Random.nextBoolean()
                val angularSpeed = Random.nextFloat() * 20f + 20f
                val rotTarget = angularSpeed * (duration / 1000f) * if (Random.nextBoolean()) 1f else -1f

                icons.add(FloatingIconData(
                    id = nextId++,
                    size = size,
                    startX = startX,
                    endX = endX,
                    duration = duration,
                    isHeart = isHeart,
                    rotationTarget = rotTarget
                ))
            }
        }

        for (icon in icons) {
            key(icon.id) {
                val animY = remember { Animatable(height) }
                val animX = remember { Animatable(icon.startX) }
                val animRot = remember { Animatable(0f) }

                LaunchedEffect(icon) {
                    launch {
                        animY.animateTo(
                            targetValue = -50f,
                            animationSpec = tween(durationMillis = icon.duration, easing = LinearEasing)
                        )
                        icons.remove(icon)
                    }
                    launch {
                        animX.animateTo(
                            targetValue = icon.endX,
                            animationSpec = tween(durationMillis = icon.duration, easing = LinearEasing)
                        )
                    }
                    launch {
                        animRot.animateTo(
                            targetValue = icon.rotationTarget,
                            animationSpec = tween(durationMillis = icon.duration, easing = LinearEasing)
                        )
                    }
                }

                val alphaProgression = (animY.value / height).coerceIn(0f, 1f)
                val alpha = 0.1f + alphaProgression * 0.3f

                if (icon.isHeart) {
                    Text(
                        text = "❤️",
                        modifier = Modifier
                            .offset(x = animX.value.dp, y = animY.value.dp)
                            .rotate(animRot.value)
                            .alpha(alpha),
                        fontSize = icon.size.sp,
                    )
                } else {
                    Image(
                        painter = painterResource(id = space.celestia.celestiaui.R.drawable.loading_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(x = animX.value.dp, y = animY.value.dp)
                            .rotate(animRot.value)
                            .size(icon.size.dp)
                            .alpha(alpha)
                    )
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoItem(@RawRes videoResId: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember(videoResId) {
        ExoPlayer.Builder(context).build().apply {
            val rawUri = "android.resource://${context.packageName}/$videoResId"
            setMediaItem(MediaItem.fromUri(rawUri))
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(videoResId) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            val view = android.view.LayoutInflater.from(ctx).inflate(R.layout.view_carousel_video, null, false) as PlayerView
            view.apply {
                player = exoPlayer
                useController = false
                setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun PlanList(purchaseManager: PurchaseManagerImpl, productDetails: ProductDetails, plans: List<PurchaseManagerImpl.Plan>, status: PurchaseManagerImpl.SubscriptionStatus.Good) {
    val activity = LocalActivity.current
    val text: String = when (status) {
        is PurchaseManagerImpl.SubscriptionStatus.Good.None -> {
            CelestiaString("Choose one of the plans below to get access to all the features.", "")
        }

        is PurchaseManagerImpl.SubscriptionStatus.Good.Pending -> {
            CelestiaString("Your purchase is pending", "")
        }

        is PurchaseManagerImpl.SubscriptionStatus.Good.NotAcknowledged -> {
            CelestiaString("We are processing your purchase", "")
        }

        is PurchaseManagerImpl.SubscriptionStatus.Good.Acknowledged, is PurchaseManagerImpl.SubscriptionStatus.Good.NotVerified, is PurchaseManagerImpl.SubscriptionStatus.Good.Verified -> {
            CelestiaString("Congratulations, you are a Celestia PLUS user", "")
        }
    }
    Text(text = text, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
    Spacer(modifier = Modifier.height(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_large_gap_vertical)))

    val preferredPlans: List<PurchaseManagerImpl.Plan>
    val lastPlan = plans.lastOrNull()

    if (lastPlan != null && lastPlan.type == PurchaseManagerImpl.PlanType.Weekly) {
        val orderedPlans = ArrayList(plans.subList(0, plans.size - 1))
        val currentPlan = if (status is PurchaseManagerImpl.SubscriptionStatus.Good.Verified) status.plan else null
        if ((lastPlan.offersFreeTrial && currentPlan == null) || currentPlan == PurchaseManagerImpl.PlanType.Weekly) {
            orderedPlans.add(0, lastPlan)
        }
        preferredPlans = orderedPlans
    } else {
        preferredPlans = plans
    }

    for (index in preferredPlans.indices) {
        val plan = preferredPlans[index]
        val token: String?
        val canAction: Boolean
        val actionTitle: String
        val actionButtonHidden: Boolean
        var currentPlan: PurchaseManagerImpl.PlanType? = null
        when (status) {
            is PurchaseManagerImpl.SubscriptionStatus.Good.None -> {
                token = null
                canAction = true
                actionButtonHidden = false
                actionTitle = if (plan.offersFreeTrial) CelestiaString("Try for Free", "Free trial on subscription service") else CelestiaString("Get", "Purchase subscription service")
            }
            is PurchaseManagerImpl.SubscriptionStatus.Good.Pending -> {
                token = status.purchaseToken
                canAction = false
                actionButtonHidden = false
                actionTitle = if (plan.offersFreeTrial) CelestiaString("Try for Free", "Free trial on subscription service") else CelestiaString("Get", "Purchase subscription service")
            }
            is PurchaseManagerImpl.SubscriptionStatus.Good.NotAcknowledged -> {
                token = status.purchaseToken
                canAction = false
                actionButtonHidden = false
                actionTitle = if (plan.offersFreeTrial) CelestiaString("Try for Free", "Free trial on subscription service") else CelestiaString("Get", "Purchase subscription service")
            }
            is PurchaseManagerImpl.SubscriptionStatus.Good.Acknowledged -> {
                token = status.purchaseToken
                canAction = false
                actionButtonHidden = false
                actionTitle = CelestiaString("Change", "Change subscription service")
            }
            is PurchaseManagerImpl.SubscriptionStatus.Good.NotVerified -> {
                token = status.purchaseToken
                canAction = true
                actionButtonHidden = false
                actionTitle = CelestiaString("Change", "Change subscription service")
            }
            is PurchaseManagerImpl.SubscriptionStatus.Good.Verified -> {
                token = status.purchaseToken
                currentPlan = status.plan
                canAction = currentPlan != plan.type
                actionButtonHidden = !canAction
                actionTitle = if (currentPlan == null) {
                    CelestiaString("Change", "Change subscription service")
                } else if (currentPlan.level < plan.type.level) {
                    CelestiaString("Upgrade", "Upgrade subscription service")
                } else  if (currentPlan.level > plan.type.level) {
                    CelestiaString("Downgrade", "Downgrade subscription service")
                } else {
                    ""
                }
            }
        }
        PlanCard(plan = plan, actionButtonText = actionTitle, actionButtonEnabled = canAction, actionButtonHidden = actionButtonHidden, isCurrent = currentPlan == plan.type) {
            activity?.let {
                purchaseManager.createSubscription(plan, productDetails, token, it)
            }
        }
        if (index != preferredPlans.size - 1) {
            Spacer(modifier = Modifier.height(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_medium_gap_vertical)))
        }
    }
    if (status is PurchaseManagerImpl.SubscriptionStatus.Good.NotVerified || status is PurchaseManagerImpl.SubscriptionStatus.Good.Verified || status is PurchaseManagerImpl.SubscriptionStatus.Good.Acknowledged) {
        Spacer(modifier = Modifier.height(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_medium_gap_vertical)))
        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                activity?.let {
                    val url = purchaseManager.subscriptionManagementURL()
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    val ai = intent.resolveActivityInfo(it.packageManager, PackageManager.MATCH_DEFAULT_ONLY)
                    if (ai != null && ai.exported)
                        it.startActivity(intent)
                }
            }) {
            Text(text = CelestiaString("Manage Subscription", ""))
        }
    } else {
        Spacer(modifier = Modifier.height(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_medium_gap_vertical)))
        Text(text = CelestiaString("Purchase will be available immediately. Subscription will be automatically renewed and you will be charged the price listed here each year (for Yearly), each month (for Monthly), or each week (for Weekly). Cancel at anytime in Subscriptions on Google Play.", ""), color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PlanCard(plan: PurchaseManagerImpl.Plan, actionButtonText: String, actionButtonEnabled: Boolean, actionButtonHidden: Boolean, isCurrent: Boolean, modifier: Modifier = Modifier, action: () -> Unit) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = modifier
        .defaultMinSize(minHeight = dimensionResource(space.celestia.celestiaui.R.dimen.list_item_one_line_min_height))
        .fillMaxWidth()
        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.purchase_box_corner_radius)))
        .background(MaterialTheme.colorScheme.primaryContainer)
        .padding(
            horizontal = dimensionResource(
                id = space.celestia.celestiaui.R.dimen.common_page_small_margin_horizontal
            ),
            vertical = dimensionResource(
                id = if (isCurrent) space.celestia.celestiaui.R.dimen.common_page_medium_margin_vertical else space.celestia.celestiaui.R.dimen.common_page_small_margin_vertical
            ),
        )) {
        Column {
            Text(text = if (isCurrent) CelestiaString("%s (Current)", "Subscription plan name when the plan is the current plan user owns").format(plan.type.displayName) else plan.type.displayName, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)

            if (!isCurrent) {
                Spacer(modifier = Modifier.height(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_small_gap_vertical)))
                Text(
                    text = plan.formattedPriceLine1,
                    color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (plan.formattedPriceLine2 != null) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_small_gap_vertical)))
                    Text(
                        text = plan.formattedPriceLine2,
                        color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        if (!actionButtonHidden) {
            Spacer(modifier = Modifier.width(dimensionResource(id = space.celestia.celestiaui.R.dimen.common_page_small_gap_horizontal)))
            Button(onClick = action, enabled = actionButtonEnabled) {
                Text(text = actionButtonText)
            }
        }
    }
}
