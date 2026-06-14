package space.celestia.celestiaxr.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestiaui.compose.Mdc3Theme
import space.celestia.celestiaxr.di.PanelState
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity: AppCompatActivity() {
    @PanelState
    @Inject
    lateinit var panelState: MutableState<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mdc3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(innerPadding)
                }
            }
        }
        openedActivityCount += 1
        openInstances.add(this)
        if (openedActivityCount == 1) {
            panelState.value = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        openInstances.remove(this)
        openedActivityCount -= 1
        if (openedActivityCount == 0) {
            panelState.value = false
        }
    }

    companion object {
        private var openedActivityCount = 0
        private val openInstances = mutableListOf<HomeActivity>()

        // Called by XRActivity right before it kills the process so the Quest
        // panel host doesn't keep the last (about-to-be-stale) surface buffer
        // visible after the JVM exits.
        fun finishAll() {
            for (activity in openInstances.toList()) {
                activity.finish()
            }
        }
    }
}