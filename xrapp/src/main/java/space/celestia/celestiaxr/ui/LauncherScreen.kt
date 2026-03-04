package space.celestia.celestiaxr.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.celestia.celestiaxr.ui.theme.CelestiaTheme

@Composable
fun LauncherScreen(paddingValues: PaddingValues, onEnterVR: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000510),
                        Color(0xFF020A1E),
                        Color(0xFF040D2A),
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(horizontal = 40.dp),
        ) {

            // ── Decorative star row ──────────────────────────────────────────
            Text(
                text = "✦  ✧  ✦",
                color = Color(0xFF7B9FFF),
                fontSize = 18.sp,
                letterSpacing = 10.sp,
            )

            // ── App title ────────────────────────────────────────────────────
            Text(
                text = "CELESTIA",
                color = Color.White,
                fontSize = 52.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 14.sp,
            )

            // ── Tagline ──────────────────────────────────────────────────────
            Text(
                text = "A journey through the cosmos",
                color = Color(0xFF9BB8FF),
                fontSize = 15.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 3.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Description ──────────────────────────────────────────────────
            Text(
                text = "Explore the infinite universe in immersive VR.\n" +
                        "Navigate stars, nebulae, and celestial wonders.",
                color = Color(0xFF6A85B5),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Primary CTA ──────────────────────────────────────────────────
            Button(
                onClick = onEnterVR,
                modifier = Modifier
                    .width(240.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A4A8F),
                    contentColor   = Color.White,
                ),
            ) {
                Text(
                    text          = "ENTER THE STARS",
                    fontSize      = 13.sp,
                    fontWeight    = FontWeight.Medium,
                    letterSpacing = 3.sp,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Build info ───────────────────────────────────────────────────
            Text(
                text  = "v1.0  •  OpenXR  •  Meta Quest",
                color = Color(0xFF3D5580),
                fontSize = 10.sp,
                letterSpacing = 2.sp,
            )
        }
    }
}
