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
import java.io.Serializable

interface BottomControlAction: Serializable {
    val imageID: Int?
}

class InstantAction(val action: CelestiaAction): Serializable, BottomControlAction {
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
}

class ContinuousAction(val action: CelestiaContinuosAction): Serializable, BottomControlAction {
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
}

class GroupActionItem(val title: String, val action: CelestiaContinuosAction): Serializable

class GroupAction(val actions: List<GroupActionItem>): Serializable, BottomControlAction {
    override val imageID: Int
        get() = R.drawable.common_other
}