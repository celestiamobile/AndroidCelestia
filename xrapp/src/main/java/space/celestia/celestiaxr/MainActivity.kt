package space.celestia.celestiaxr

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import space.celestia.celestiaui.compose.Mdc3Theme
import space.celestia.celestiaxr.ui.LauncherScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mdc3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LauncherScreen(innerPadding) {
                        val immersiveIntent =
                            Intent(this, XRActivity::class.java).apply {
                                action = Intent.ACTION_MAIN
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        startActivity(immersiveIntent)
                        finishAndRemoveTask()
                    }
                }
            }
        }
    }
}