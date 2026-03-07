package space.celestia.celestiaxr.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import space.celestia.celestia.AppState
import space.celestia.celestia.Observer
import space.celestia.celestia.Utils
import space.celestia.celestiaxr.tool.Tool
import space.celestia.celestiaxr.tool.ToolActivity
import space.celestia.celestiaxr.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Locale

data class SimulationState(
    val appState: AppState? = null,
    val selectedObjectName: String = "",
    val referenceObjectName: String = "",
    val targetObjectName: String = "",
    val messageText: String = ""
)

@Composable
fun RunningScreen() {
    val viewModel: MainViewModel = hiltViewModel()
    var simulationState by remember { mutableStateOf(SimulationState()) }
    val dateFormatter = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault()) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val job = scope.launch {
            while (true) {
                viewModel.xrRenderer.enqueueTask {
                    val appState = viewModel.appCore.state
                    val universe = viewModel.appCore.simulation.universe
                    val selectedObjectName = if (!appState.selectedObject.isEmpty) {
                        universe.getNameForSelection(appState.selectedObject)
                    } else ""
                    val referenceObjectName = if (!appState.referenceObject.isEmpty) {
                        universe.getNameForSelection(appState.referenceObject)
                    } else ""
                    val targetObjectName = if (!appState.targetObject.isEmpty) {
                        universe.getNameForSelection(appState.targetObject)
                    } else ""
                    val messageText = viewModel.appCore.messageText

                    scope.launch {
                        simulationState = SimulationState(
                            appState = appState,
                            selectedObjectName = selectedObjectName,
                            referenceObjectName = referenceObjectName,
                            targetObjectName = targetObjectName,
                            messageText = messageText
                        )
                    }
                }
                delay(1000)
            }
        }
        onDispose {
            job.cancel()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader("Tools")
            ToolsSection()

            val appState = simulationState.appState
            if (appState != null) {
                SectionHeader("Stats")
                StatsSection(simulationState, appState, dateFormatter)
            }

            if (simulationState.messageText.isNotEmpty()) {
                SectionHeader("Messages")
                MessagesSection(simulationState.messageText)
            }
        }

        BottomActionBar()
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ToolsSection() {
    val context = LocalContext.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Tool.all.forEach { tool ->
            OutlinedButton(
                onClick = {
                    when (tool) {
                        is Tool.Page -> {
                            val intent = Intent(context, ToolActivity::class.java)
                            intent.putExtra(ToolActivity.EXTRA_TOOL, tool)
                            context.startActivity(intent)
                        }
                    }
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = tool.title, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StatsSection(state: SimulationState, appState: AppState, dateFormatter: DateFormat) {
    val isMetric = remember { usesMetricSystem() }
    val numberFormatter = remember { NumberFormat.getNumberInstance() }
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        StatRow("Time", formatTime(appState.time, appState.isPaused, appState.isLightTravelDelayEnabled, dateFormatter))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        StatRow("Time Scale", formatTimeScale(appState.timeScale, numberFormatter))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (state.selectedObjectName.isNotEmpty()) {
            StatRow("Selected", state.selectedObjectName)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (appState.showDistanceToSelection) {
                StatRow("Distance", formatLength(appState.distanceToSelectionSurface, isMetric, numberFormatter))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                if (appState.showDistanceToSelectionCenter) {
                    StatRow("Distance to Center", formatLength(appState.distanceToSelectionCenter, isMetric, numberFormatter))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
        StatRow("Mode", formatCoordinateSystem(appState.coordinateSystem, state.referenceObjectName, state.targetObjectName))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        StatRow("Speed", formatSpeed(appState.speed.toDouble(), isMetric, numberFormatter))
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun MessagesSection(messageText: String) {
    Text(
        text = messageText,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

private fun formatTime(julianDate: Double, isPaused: Boolean, isLightTravelDelayEnabled: Boolean, formatter: DateFormat): String {
    val time = formatter.format(Utils.createDateFromJulianDay(julianDate))
    return when {
        isPaused && isLightTravelDelayEnabled -> "$time LT (Paused)"
        isPaused -> "$time (Paused)"
        isLightTravelDelayEnabled -> "$time LT"
        else -> time
    }
}

private fun formatTimeScale(timeScale: Double, formatter: NumberFormat): String {
    return when {
        timeScale == 1.0 -> "Real Time"
        timeScale == -1.0 -> "-Real Time"
        kotlin.math.abs(timeScale) < 1e-15 -> "Time Stopped"
        else -> "${formatNumber(timeScale, formatter)}× Real Time"
    }
}

private const val oneMiInKm = 1.60934
private const val oneFtInKm = 0.0003048

private fun usesMetricSystem(): Boolean {
    return Locale.getDefault().country !in setOf("US", "LR", "MM")
}

private fun formatNumber(number: Double, formatter: NumberFormat): String {
    val abs = kotlin.math.abs(number)
    val fractionDigits = when {
        abs >= 1000.0 -> 0
        abs >= 10.0 -> 1
        abs >= 1.0 -> 2
        else -> 4
    }
    formatter.minimumFractionDigits = fractionDigits
    formatter.maximumFractionDigits = fractionDigits
    return formatter.format(number)
}

private fun formatLength(km: Double, isMetric: Boolean, formatter: NumberFormat): String {
    val ly = Utils.kilometersToLightYears(km)
    val mpcThreshold = Utils.parsecsToLightYears(1e6)
    val kpcThreshold = 0.5 * Utils.parsecsToLightYears(1e3)
    val number: Double
    val unit: String
    if (kotlin.math.abs(ly) >= mpcThreshold) {
        unit = "Mpc"
        number = Utils.lightYearsToParsecs(ly) / 1e6
    } else if (kotlin.math.abs(ly) >= kpcThreshold) {
        unit = "kpc"
        number = Utils.lightYearsToParsecs(ly) / 1e3
    } else {
        val au = Utils.kilometersToAU(km)
        if (kotlin.math.abs(au) >= 1000.0) {
            unit = "ly"
            number = ly
        } else if (km >= 10000000.0) {
            unit = "au"
            number = au
        } else if (!isMetric) {
            if (km >= oneMiInKm) {
                unit = "mi"
                number = km / oneMiInKm
            } else {
                unit = "ft"
                number = km / oneFtInKm
            }
        } else {
            if (km >= 1.0) {
                unit = "km"
                number = km
            } else {
                unit = "m"
                number = km * 1000.0
            }
        }
    }
    return "${formatNumber(number, formatter)} $unit"
}

private fun formatSpeed(speed: Double, isMetric: Boolean, formatter: NumberFormat): String {
    val au = Utils.kilometersToAU(speed)
    val number: Double
    val unit: String
    if (kotlin.math.abs(au) >= 1000.0) {
        unit = "ly/s"
        number = Utils.kilometersToLightYears(speed)
    } else if (speed >= 10000000.0) {
        unit = "au/s"
        number = au
    } else if (!isMetric) {
        if (speed >= oneMiInKm) {
            unit = "mi/s"
            number = speed / oneMiInKm
        } else {
            unit = "ft/s"
            number = speed / oneFtInKm
        }
    } else {
        if (speed >= 1.0) {
            unit = "km/s"
            number = speed
        } else {
            unit = "m/s"
            number = speed * 1000.0
        }
    }
    return "${formatNumber(number, formatter)} $unit"
}

private fun formatCoordinateSystem(coordinateSystem: Int, referenceName: String, targetName: String): String {
    return when (coordinateSystem) {
        Observer.COORDINATE_SYSTEM_ECLIPTICAL -> "Follow $referenceName"
        Observer.COORDINATE_SYSTEM_BODY_FIXED -> "Sync Orbit $referenceName"
        Observer.COORDINATE_SYSTEM_PHASE_LOCK -> "Lock $referenceName → $targetName"
        Observer.COORDINATE_SYSTEM_CHASE -> "Chase $referenceName"
        Observer.COORDINATE_SYSTEM_UNIVERSAL -> "Freeflight"
        else -> "Unknown"
    }
}
