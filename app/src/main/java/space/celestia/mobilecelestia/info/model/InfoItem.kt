/*
 * InfoItem.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.info.model

import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.Serializable

enum class CelestiaAction(val value: Int) : Serializable {
    GoTo(103),
    Center(99),
    PlayPause(32),
    Reverse(106),
    Slower(107),
    Faster(108),
    CurrentTime(33),
    SyncOrbit(121),
    Lock(58),
    Chase(34),
    Follow(102),
    RunDemo(100),
    CancelScript(27),
    Home(104);

    val title: String
        get() {
            val orig = when (this) {
                GoTo -> "Go"
                PlayPause -> "Resume/Pause"
                CurrentTime -> "Current Time"
                SyncOrbit -> "Sync Orbit"
                RunDemo -> "Run Demo"
                CancelScript -> "Cancel Script"
                Home -> "Home (Sol)"
                Reverse -> "Reverse Time"
                else -> this.toString()
            }
            return CelestiaString(orig, "")
        }
}

interface InfoItem

interface InfoActionItem : InfoItem {
    val title: String

    companion object {
        val infoActions: List<InfoActionItem>
            get() = listOf(
                InfoSelectActionItem(),
                InfoNormalActionItem(CelestiaAction.GoTo),
                InfoNormalActionItem(CelestiaAction.Center),
                InfoNormalActionItem(CelestiaAction.Follow),
                InfoNormalActionItem(CelestiaAction.Chase),
                InfoNormalActionItem(CelestiaAction.SyncOrbit),
                InfoNormalActionItem(CelestiaAction.Lock)
            )
    }
}

class InfoNormalActionItem(val item: CelestiaAction) : InfoActionItem {
    override val title: String
        get() = item.title
}
class InfoSelectActionItem : InfoActionItem {
    override val title: String
        get() = CelestiaString("Select", "")
}
class InfoWebActionItem : InfoActionItem {
    override val title: String
        get() = CelestiaString("Web Info", "")
}
class InfoDescriptionItem(val name: String, val overview: String, val hasWebInfo: Boolean) : InfoItem, Serializable