package space.celestia.celestiaui.help

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.utils.URLHelper
import space.celestia.celestiaui.help.viewmodel.HelpViewModel
import space.celestia.celestiaui.resource.WebPage

@Composable
fun NewHelpScreen(linkClicked: (String) -> Unit) {
    val viewModel: HelpViewModel = hiltViewModel()
    Scaffold { paddingValues ->
        WebPage(
            uri = URLHelper.buildInAppGuideShortURI("/help/welcome", AppCore.getLanguage(), platform = viewModel.platform, shareable = false),
            filterURL = true,
            matchingQueryKeys = listOf("guide"),
            paddingValues = paddingValues,
            fallbackContent = { modifier, paddingValues ->
                HelpScreen(modifier = modifier, paddingValues = paddingValues, linkClicked = { link ->
                    linkClicked(link)
                })
            }
        )
    }
}