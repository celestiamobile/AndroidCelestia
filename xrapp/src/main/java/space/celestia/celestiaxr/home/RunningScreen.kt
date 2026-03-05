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
import space.celestia.celestia.Utils
import space.celestia.celestiaxr.tool.Tool
import space.celestia.celestiaxr.tool.ToolActivity
import space.celestia.celestiaxr.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Locale

data class SimulationState(
    val time: Double = 0.0,
    val timeScale: Double = 1.0,
    val isPaused: Boolean = false,
    val selectedObjectName: String = "",
    val speed: Double = 0.0,
    val coordinateSystem: Int = 0,
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
                    val time = viewModel.appCore.simulation.time
                    val timeScale = viewModel.appCore.timeScale
                    val isPaused = viewModel.appCore.isPaused
                    val observer = viewModel.appCore.simulation.activeObserver
                    val speed = observer.speed
                    val coordinateSystem = observer.coordinateSystem
                    val selection = viewModel.appCore.simulation.selection
                    val selectedName = if (!selection.isEmpty) {
                        viewModel.appCore.simulation.universe.getNameForSelection(selection)
                    } else ""
                    val messageText = viewModel.appCore.messageText

                    scope.launch {
                        simulationState = SimulationState(
                            time = time,
                            timeScale = timeScale,
                            isPaused = isPaused,
                            selectedObjectName = selectedName,
                            speed = speed,
                            coordinateSystem = coordinateSystem,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tools Section
            SectionHeader("Tools")
            ToolsSection()

            // Stats Section
            SectionHeader("Stats")
            StatsSection(simulationState, dateFormatter)

            // Messages Section
            if (simulationState.messageText.isNotEmpty()) {
                SectionHeader("Messages")
                MessagesSection(simulationState.messageText)
            }
        }
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
                        is Tool.Pause -> { /* TODO: handle pause action directly */ }
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
private fun StatsSection(state: SimulationState, dateFormatter: DateFormat) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        StatRow("Time", formatJulianDate(state.time, dateFormatter))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        StatRow("Time Scale", formatTimeScale(state.timeScale, state.isPaused))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (state.selectedObjectName.isNotEmpty()) {
            StatRow("Selected", state.selectedObjectName)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
        StatRow("Speed", formatSpeed(state.speed))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        StatRow("Mode", formatCoordinateSystem(state.coordinateSystem))
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

private fun formatJulianDate(julianDate: Double, formatter: DateFormat): String {
    val date = Utils.createDateFromJulianDay(julianDate)
    return formatter.format(date)
}

private fun formatTimeScale(timeScale: Double, isPaused: Boolean): String {
    if (isPaused) return "Paused"
    return when {
        timeScale == 1.0 -> "1× (Real time)"
        timeScale > 0 && timeScale < 1 -> "%.6f×".format(timeScale)
        else -> "%.0f×".format(timeScale)
    }
}

private fun formatSpeed(speed: Double): String {
    // Speed is in microlight-years per second from Celestia engine
    // Convert to km/s: 1 light-year = 9.461e12 km, 1 microly = 9.461e6 km
    val kmPerSec = speed * 9.461e6
    return when {
        kmPerSec < 1.0 -> "%.2f m/s".format(kmPerSec * 1000.0)
        kmPerSec < 1000.0 -> "%.2f km/s".format(kmPerSec)
        kmPerSec < 299792.458 -> "%.0f km/s".format(kmPerSec)
        else -> "%.4f c".format(kmPerSec / 299792.458)
    }
}

private fun formatCoordinateSystem(coordinateSystem: Int): String {
    return when (coordinateSystem) {
        0 -> "Universal"
        1 -> "Ecliptical"
        2 -> "Body Fixed"
        3 -> "Phase Lock"
        4 -> "Chase"
        5 -> "Phase Lock (Target)"
        6 -> "Unknown"
        else -> "Unknown"
    }
}
