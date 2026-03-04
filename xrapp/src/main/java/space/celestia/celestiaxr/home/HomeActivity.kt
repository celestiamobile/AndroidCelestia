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
        if (openedActivityCount == 1) {
            panelState.value = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        openedActivityCount -= 1
        if (openedActivityCount == 0) {
            panelState.value = false
        }
    }

    companion object {
        private var openedActivityCount = 0
    }
}