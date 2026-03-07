package space.celestia.celestiaxr.home

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestiaui.info.model.CelestiaAction
import space.celestia.celestiaui.info.model.CelestiaContinuousAction
import space.celestia.celestiaui.info.model.perform
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaxr.viewmodel.MainViewModel

private enum class ActionGroup {
    Time, Speed;

    val title: String
        get() = when (this) {
            Time -> CelestiaString("Time", "")
            Speed -> CelestiaString("Speed", "")
        }

    sealed class Action {
        data class Transient(val action: CelestiaAction) : Action()
        data class Continuous(val action: CelestiaContinuousAction) : Action()

        val title: String
            get() = when (this) {
                is Transient -> action.title
                is Continuous -> when (action) {
                    CelestiaContinuousAction.TravelFaster -> CelestiaString("Faster", "Make time go faster")
                    CelestiaContinuousAction.TravelSlower -> CelestiaString("Slower", "Make time go more slowly")
                    else -> ""
                }
            }
    }

    val actions: List<Action>
        get() = when (this) {
            Time -> listOf(
                Action.Transient(CelestiaAction.Slower),
                Action.Transient(CelestiaAction.PlayPause),
                Action.Transient(CelestiaAction.Faster),
                Action.Transient(CelestiaAction.Reverse)
            )
            Speed -> listOf(
                Action.Continuous(CelestiaContinuousAction.TravelSlower),
                Action.Transient(CelestiaAction.Stop),
                Action.Continuous(CelestiaContinuousAction.TravelFaster),
                Action.Transient(CelestiaAction.ReverseSpeed)
            )
        }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BottomActionBar() {
    val viewModel: MainViewModel = hiltViewModel()
    var selectedGroup by remember { mutableStateOf(ActionGroup.Time) }

    Surface(color = MaterialTheme.colorScheme.surface) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SingleChoiceSegmentedButtonRow {
                ActionGroup.entries.forEachIndexed { index, group ->
                    SegmentedButton(
                        selected = selectedGroup == group,
                        onClick = { selectedGroup = group },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ActionGroup.entries.size),
                        label = { Text(group.title, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            selectedGroup.actions.forEach { action ->
                ActionButton(action, viewModel)
            }
        }
    }
}

@Composable
private fun ActionButton(action: ActionGroup.Action, viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    when (action) {
        is ActionGroup.Action.Transient -> {
            OutlinedButton(onClick = {
                scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.perform(action.action)
                }
            }) {
                Text(action.title, style = MaterialTheme.typography.bodySmall)
            }
        }
        is ActionGroup.Action.Continuous -> {
            val interactionSource = remember { MutableInteractionSource() }
            LaunchedEffect(interactionSource) {
                interactionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                            viewModel.appCore.keyDown(action.action.value)
                        }
                        is PressInteraction.Release, is PressInteraction.Cancel -> scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                            viewModel.appCore.keyUp(action.action.value)
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = {},
                interactionSource = interactionSource
            ) {
                Text(action.title, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
