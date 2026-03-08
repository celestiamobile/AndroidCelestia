package space.celestia.celestiaxr.home

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestiaxr.tool.Tool
import space.celestia.celestiaxr.tool.ToolActivity
import space.celestia.celestiaxr.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ToolsSection(onHelpOpened: (() -> Unit)? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = hiltViewModel()
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Tool.all.forEach { tool ->
            OutlinedButton(
                onClick = {
                    when (tool) {
                        is Tool.Info -> {
                            scope.launch {
                                val selection = withContext(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.simulation.selection }
                                val intent = Intent(context, ToolActivity::class.java)
                                intent.putExtra(ToolActivity.EXTRA_TOOL, Tool.Page.ObjectInfo(selection))
                                context.startActivity(intent)
                            }
                        }
                        is Tool.Page -> {
                            if (tool is Tool.Page.Help) {
                                onHelpOpened?.invoke()
                            } else if (tool is Tool.Page.Favorites) {

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
