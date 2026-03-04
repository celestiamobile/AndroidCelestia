package space.celestia.celestiaxr.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestiaui.compose.Mdc3Theme
import space.celestia.celestiaxr.loading.LoadingScreen

@AndroidEntryPoint
class HomeActivity: AppCompatActivity() {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        openedActivityCount -= 1
    }

    companion object {
        private var openedActivityCount = 0
        val hasOpenedActivity: Boolean
            get() = openedActivityCount > 0
    }
}