package space.celestia.MobileCelestia.Info.Model

import java.io.Serializable

enum class CelestiaAction(val value: Int) : Serializable {
    GoTo(103),
    Center(99),
    PlayPause(32),
    Backward(107),
    Forward(108),
    CurrentTime(33),
    SyncOrbit(121),
    Lock(58),
    Chase(34),
    Follow(102),
    RunDemo(100),
    CancelScript(27);

    val title: String
        get() {
            when (this) {
                GoTo -> {
                    return "Go"
                }
                Center -> {
                    return "Center"
                }
                PlayPause -> {
                    return "Resume/Pause"
                }
                Backward -> {
                    return "Backward"
                }
                Forward -> {
                    return "Forward"
                }
                CurrentTime -> {
                    return "Current Time"
                }
                SyncOrbit -> {
                    return "Sync Orbit"
                }
                Lock -> {
                    return "Lock"
                }
                Chase -> {
                    return "Chase"
                }
                Follow -> {
                    return "Follow"
                }
                RunDemo -> {
                    return "Run Demo"
                }
                CancelScript ->
                    return "Cancel Script"
                }
            }
}

interface InfoItem {
}

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
        get() = "Select"
}
class InfoWebActionItem : InfoActionItem {
    override val title: String
        get() = "Web Info"
}
class InfoDescriptionItem(val name: String, val overview: String) : InfoItem, Serializable {}