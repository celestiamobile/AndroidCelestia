package space.celestia.celestiaxr.home

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import space.celestia.celestiaxr.tool.Tool
import space.celestia.celestiaxr.tool.ToolActivity

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ToolsSection(onHelpOpened: (() -> Unit)? = null) {
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
                            if (tool is Tool.Page.Help) {
                                onHelpOpened?.invoke()
                            }
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
