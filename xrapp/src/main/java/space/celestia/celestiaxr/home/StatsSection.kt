package space.celestia.celestiaxr.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.celestia.celestia.AppState
import space.celestia.celestiaui.utils.CelestiaString
import java.text.DateFormat
import java.text.NumberFormat

@Composable
internal fun StatsSection(state: SimulationState, appState: AppState, dateFormatter: DateFormat) {
    val isMetric = remember { usesMetricSystem() }
    val numberFormatter = remember { NumberFormat.getNumberInstance() }
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        StatRow(CelestiaString("Time", ""), formatTime(appState.time, appState.isPaused, appState.isLightTravelDelayEnabled, dateFormatter))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        StatRow(CelestiaString("Time Scale", ""), formatTimeScale(appState.timeScale, numberFormatter))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (state.selectedObjectName.isNotEmpty()) {
            StatRow(CelestiaString("Selected", ""), state.selectedObjectName)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (appState.showDistanceToSelection) {
                StatRow(CelestiaString("Distance", "Distance to the object (in Go to)"), formatLength(appState.distanceToSelectionSurface, isMetric, numberFormatter))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                if (appState.showDistanceToSelectionCenter) {
                    StatRow(CelestiaString("Distance to Center", ""), formatLength(appState.distanceToSelectionCenter, isMetric, numberFormatter))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
        StatRow(CelestiaString("Mode", ""), formatCoordinateSystem(appState.coordinateSystem, state.referenceObjectName, state.targetObjectName))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        StatRow(CelestiaString("Speed", ""), formatSpeed(appState.speed.toDouble(), isMetric, numberFormatter))
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
