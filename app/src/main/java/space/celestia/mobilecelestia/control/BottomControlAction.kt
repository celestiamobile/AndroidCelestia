/*
 * BottomControlAction.kt
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.control

import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.info.model.CelestiaAction
import space.celestia.mobilecelestia.info.model.CelestiaContinuosAction
import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.Serializable

sealed class BottomControlAction: Serializable {
    abstract val imageID: Int?
    abstract val contentDescription: String?
}

data class InstantAction(val action: CelestiaAction): Serializable, BottomControlAction() {
    override val imageID: Int?
        get() = when (action) {
            CelestiaAction.Faster -> {
                R.drawable.time_faster
            }
            CelestiaAction.Slower -> {
                R.drawable.time_slower
            }
            CelestiaAction.PlayPause -> {
                R.drawable.time_playpause
            }
            CelestiaAction.CancelScript -> {
                R.drawable.time_stop
            }
            CelestiaAction.Reverse -> {
                R.drawable.time_reverse
            }
            CelestiaAction.ReverseSpeed -> {
                R.drawable.time_reverse
            }
            CelestiaAction.Stop -> {
                R.drawable.time_stop
            }
            else -> {
                null
            }
        }

    override val contentDescription: String?
        get() = when (action) {
            CelestiaAction.Faster -> {
                CelestiaString("Faster", "Make time go faster")
            }
            CelestiaAction.Slower -> {
                CelestiaString("Slower", "Make time go more slowly")
            }
            CelestiaAction.PlayPause -> {
                CelestiaString("Resume or Pause", "Resume or pause time/script")
            }
            CelestiaAction.CancelScript -> {
                CelestiaString("Stop", "Interupt the process of finding eclipse/Set traveling speed to 0")
            }
            CelestiaAction.Reverse -> {
                CelestiaString("Reverse", "Reverse time or travel direction")
            }
            CelestiaAction.ReverseSpeed -> {
                CelestiaString("Reverse", "Reverse time or travel direction")
            }
            CelestiaAction.Stop -> {
                CelestiaString("Stop", "Interupt the process of finding eclipse/Set traveling speed to 0")
            }
            else -> {
                null
            }
        }
}

data class ContinuousAction(val action: CelestiaContinuosAction): Serializable, BottomControlAction() {
    override val imageID: Int?
        get() = when (action) {
            CelestiaContinuosAction.TravelFaster -> {
                R.drawable.time_faster
            }
            CelestiaContinuosAction.TravelSlower -> {
                R.drawable.time_slower
            }
            else -> {
                null
            }
        }

    override val contentDescription: String?
        get() = when (action) {
            CelestiaContinuosAction.TravelFaster -> {
                CelestiaString("Faster", "Make time go faster")
            }
            CelestiaContinuosAction.TravelSlower -> {
                CelestiaString("Slower", "Make time go more slowly")
            }
            else -> {
                null
            }
        }
}

data class GroupActionItem(val title: String, val action: CelestiaContinuosAction): Serializable

enum class CustomActionType: Serializable {
    ShowTimeSettings
}
data class CustomAction(val type: CustomActionType, override val imageID: Int?, override val contentDescription: String?): Serializable, BottomControlAction()

data class GroupAction(override val imageID: Int, override val contentDescription: String, val actions: List<GroupActionItem>): Serializable, BottomControlAction()