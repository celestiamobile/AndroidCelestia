package space.celestia.celestiaxr.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaxr.viewmodel.MainViewModel
import java.text.DateFormat
import java.util.Locale

@Composable
fun RunningScreen() {
    val viewModel: MainViewModel = hiltViewModel()
    var simulationState by remember { mutableStateOf(SimulationState()) }
    val dateFormatter = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault()) }
    val scope = rememberCoroutineScope()
    var showOnboarding by remember {
        mutableStateOf(viewModel.appSettingsNoBackup[onboardingShownKey] != "true")
    }

    DisposableEffect(Unit) {
        val job = scope.launch {
            while (true) {
                simulationState = withContext(viewModel.executor.asCoroutineDispatcher()) {
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
                    return@withContext SimulationState(
                        appState = appState,
                        selectedObjectName = selectedObjectName,
                        referenceObjectName = referenceObjectName,
                        targetObjectName = targetObjectName,
                        messageText = viewModel.appCore.messageText
                    )
                }
                delay(200)
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
            if (showOnboarding) {
                OnboardingTooltip(onDismiss = {
                    showOnboarding = false
                    viewModel.appSettingsNoBackup[onboardingShownKey] = "true"
                })
            }

            SectionHeader(CelestiaString("Tools", ""))
            ToolsSection(onHelpOpened = {
                if (showOnboarding) {
                    showOnboarding = false
                    viewModel.appSettingsNoBackup[onboardingShownKey] = "true"
                }
            })

            val appState = simulationState.appState
            if (appState != null) {
                SectionHeader(CelestiaString("Stats", ""))
                StatsSection(simulationState, appState, dateFormatter)
            }

            if (simulationState.messageText.isNotEmpty()) {
                SectionHeader(CelestiaString("Messages", ""))
                MessagesSection(simulationState.messageText)
            }

            SectionHeader(CelestiaString("Time", ""))
            TimeControlsSection()

            SectionHeader(CelestiaString("Speed", ""))
            SpeedControlsSection()
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

@Composable
private fun MessagesSection(messageText: String) {
    Text(
        text = messageText,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}
