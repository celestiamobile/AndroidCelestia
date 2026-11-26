package space.celestia.celestiaui.help

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.URLHelper
import space.celestia.celestiaui.help.viewmodel.HelpViewModel
import space.celestia.celestiaui.resource.GuideScreen
import java.io.File

@Composable
fun NewHelpScreen(
    linkClicked: (String) -> Unit,
    runScript: ((String, String, String?, String?, File?) -> Unit),
    shareURL: ((String, String) -> Unit),
    receivedACK: ((String) -> Unit),
    runDemo: (() -> Unit),
    openSubscriptionPage: ((String?) -> Unit),
    externalLinkClicked: ((String) -> Unit),
) {
    val viewModel: HelpViewModel = hiltViewModel()
    GuideScreen(
        uri = URLHelper.buildInAppGuideShortURI("/help/welcome", AppCore.getLanguage(), platform = viewModel.platform, shareable = false),
        initialTitle = CelestiaString("Welcome to Celestia", "Welcome message"),
        fallbackContent = { modifier, paddingValues ->
            HelpScreen(modifier = modifier, paddingValues = paddingValues, linkClicked = linkClicked)
        },
        runScript = runScript,
        shareURL = shareURL,
        receivedACK = receivedACK,
        runDemo = runDemo,
        openSubscriptionPage = openSubscriptionPage,
        externalLinkClicked = externalLinkClicked,
    )
}