// InfoItem.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.info.model

import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.ui.linkpreview.LPLinkMetadata
import space.celestia.ui.linkpreview.LPLinkViewData
import java.io.Serializable

enum class CelestiaAction(val value: Int) : Serializable {
    GoTo(103),
    GoToSurface(7),
    Center(99),
    PlayPause(32),
    Reverse(106),
    Slower(107),
    Faster(108),
    CurrentTime(33),
    SyncOrbit(121),
    Lock(58),
    Chase(34),
    Track(116),
    Follow(102),
    CancelScript(27),
    Home(104),
    Stop(115),
    ReverseSpeed(113);

    val title: String
        get() {
            return when (this) {
                GoTo -> CelestiaString("Go", "Go to an object")
                GoToSurface -> CelestiaString("Land", "Go to surface of an object")
                PlayPause -> CelestiaString("Resume/Pause", "")
                CurrentTime -> CelestiaString("Current Time", "")
                SyncOrbit -> CelestiaString("Sync Orbit", "")
                CancelScript -> CelestiaString("Cancel Script", "")
                Home -> CelestiaString("Home (Sol)", "Home object, sun.")
                Reverse -> CelestiaString("Reverse Time", "")
                Center -> CelestiaString("Center", "Center an object")
                Slower -> CelestiaString("Slower", "Make time go more slowly")
                Faster -> CelestiaString("Faster", "Make time go faster")
                Lock -> CelestiaString("Lock", "")
                Chase -> CelestiaString("Chase", "")
                Track -> CelestiaString("Track", "Track an object")
                Follow -> CelestiaString("Follow", "")
                Stop -> CelestiaString("Stop", "Interupt the process of finding eclipse/Set traveling speed to 0")
                ReverseSpeed -> CelestiaString("Reverse Direction", "Reverse camera direction, reverse travel direction")
            }
        }

    companion object {
        val allActions: List<CelestiaAction>
            get() = listOf(
                GoTo, Center,Follow, Chase, Track, SyncOrbit, Lock, GoToSurface
            )
    }
}

enum class CelestiaContinuosAction(val value: Int) {
    TravelFaster(97),
    TravelSlower(122),
    F1(11), // KeyEvent.KEYCODE_F1
    F2(12),
    F3(13),
    F4(14),
    F5(15),
    F6(16),
    F7(17);
}

interface InfoItem

interface InfoActionItem : InfoItem {
    val title: String

    companion object {
        val infoActions: List<InfoActionItem>
            get() = listOf(InfoSelectActionItem()) + CelestiaAction.allActions.map { return@map InfoNormalActionItem(it) }
    }
}

class InfoNormalActionItem(val item: CelestiaAction) : InfoActionItem {
    override val title: String
        get() = item.title
}
class InfoSelectActionItem : InfoActionItem {
    override val title: String
        get() = CelestiaString("Select", "Select an object")
}
class InfoWebActionItem : InfoActionItem {
    override val title: String
        get() = CelestiaString("Web Info", "Web info for an object")
}
class SubsystemActionItem : InfoActionItem {
    override val title: String
        get() = CelestiaString("Subsystem", "Subsystem of an object (e.g. planetarium system)")
}
class AlternateSurfacesItem : InfoActionItem {
    override val title: String
        get() = CelestiaString("Alternate Surfaces", "Alternative textures to display")
}
class MarkItem : InfoActionItem {
    override val title: String
        get() = CelestiaString("Mark", "Mark an object")
}