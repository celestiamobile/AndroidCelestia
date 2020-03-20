package space.celestia.MobileCelestia.Toolbar.Model

import space.celestia.MobileCelestia.Common.RecyclerViewItem
import space.celestia.MobileCelestia.Toolbar.ToolbarAction

interface ToolbarListItem: RecyclerViewItem {
}

class ToolbarActionItem(val action: ToolbarAction, var image: Int) : ToolbarListItem {
    val title = action.title
}
