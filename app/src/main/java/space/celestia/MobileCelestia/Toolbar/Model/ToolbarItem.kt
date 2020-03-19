package space.celestia.MobileCelestia.Toolbar.Model

import space.celestia.MobileCelestia.Common.SeparatorRecyclerViewAdapter
import space.celestia.MobileCelestia.Toolbar.ToolbarAction

interface ToolbarListItem: SeparatorRecyclerViewAdapter.RecyclerViewItem {
}

class ToolbarActionItem(val action: ToolbarAction, var image: Int) : ToolbarListItem {
    val title = action.title
}
