package space.celestia.mobilecelestia.resource.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import space.celestia.celestiafoundation.resource.model.AddonUpdate
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.resource.model.ResourceManager

class AddonUpdateManager(val resourceManager: ResourceManager, val resourceAPI: ResourceAPIService) {
    data class PendingAddonUpdate(val update: AddonUpdate, val addon: ResourceItem)

    private var addonUpdates = mapOf<String, AddonUpdate>()
    private var didCheckOnViewAppear = false
    var pendingUpdates = mutableStateListOf<PendingAddonUpdate>()

    enum class CheckReason { Change, Refresh, ViewAppear }

    var isCheckingUpdates = mutableStateOf(false)

    suspend fun refresh(reason: CheckReason, purchaseToken: String, language: String): Boolean {
        val installedAddons = resourceManager.installedResourcesAsync()
        var success = true

        val needCheckUpdates = when (reason) {
            CheckReason.Change -> false
            CheckReason.Refresh -> true
            CheckReason.ViewAppear -> {
                val value = !didCheckOnViewAppear
                didCheckOnViewAppear = true
                value
            }
        }

        if (needCheckUpdates && !isCheckingUpdates.value) {
            isCheckingUpdates.value = true
            val installedAddonIds = installedAddons.mapNotNull { if (it.checksum != null) it.id else null }
            try {
                val result = resourceAPI.updates(UpdateRequest(lang = language, items = installedAddonIds, purchaseToken = purchaseToken))
                addonUpdates = result
            } catch (ignored: Throwable) {
                success = false
            }
            isCheckingUpdates.value = false
        }
        val updates = arrayListOf<PendingAddonUpdate>()
        for (addon in installedAddons) {
            val update = addonUpdates[addon.id]
            if (addon.checksum != null && update != null && addon.checksum != update.checksum) {
                updates.add(PendingAddonUpdate(update = update, addon = addon))
            }
        }
        pendingUpdates.clear()
        pendingUpdates.addAll(updates)
        return success
    }
}