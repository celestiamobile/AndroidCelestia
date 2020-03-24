package space.celestia.mobilecelestia.toolbar.model

import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.toolbar.ToolbarAction

interface ToolbarListItem: RecyclerViewItem

class ToolbarActionItem(val action: ToolbarAction, var image: Int) : ToolbarListItem {
    val title = action.title
}
