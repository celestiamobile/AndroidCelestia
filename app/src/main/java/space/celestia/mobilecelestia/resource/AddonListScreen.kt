package space.celestia.mobilecelestia.resource

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.resource.viewmodel.AddonViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

class AddonListState {
    var needsRefresh by mutableStateOf(false)
        internal set
}

@Composable
fun AddonListScreen(
    state: AddonListState = remember { AddonListState() },
    openInstalledAddon: (ResourceItem) -> Unit,
    openAddonDownload: () -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    val viewModel: AddonViewModel = hiltViewModel()
    val installedAddons = remember { mutableStateListOf<ResourceItem>() }

    if (state.needsRefresh) {
        LaunchedEffect(Unit) {
            val addons = viewModel.resourceManager.installedResourcesAsync()
            installedAddons.clear()
            installedAddons.addAll(addons)
            state.needsRefresh = false
        }
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        if (installedAddons.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), contentAlignment = Alignment.Center) {
                EmptyHint(text = CelestiaString("Enhance Celestia with online add-ons", ""), actionText = CelestiaString("Get Add-ons", "Open webpage for downloading add-ons"), actionHandler = {
                    openAddonDownload()
                })
            }
        } else {
            val direction = LocalLayoutDirection.current
            val contentPadding = PaddingValues(
                start = paddingValues.calculateStartPadding(direction),
                top = dimensionResource(id = R.dimen.list_spacing_short) + paddingValues.calculateTopPadding(),
                end = paddingValues.calculateEndPadding(direction),
                bottom = dimensionResource(id = R.dimen.list_spacing_tall) + paddingValues.calculateBottomPadding(),
            )
            LazyColumn(
                contentPadding = contentPadding,
                modifier = modifier
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                items(installedAddons) {
                    TextRow(primaryText = it.name, accessoryResource = R.drawable.accessory_full_disclosure, modifier = Modifier.clickable {
                        openInstalledAddon(it)
                    })
                }
            }
        }
    }
}