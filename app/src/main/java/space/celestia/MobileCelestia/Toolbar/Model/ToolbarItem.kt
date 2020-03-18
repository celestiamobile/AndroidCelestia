package space.celestia.MobileCelestia.Toolbar.Model

import space.celestia.MobileCelestia.Toolbar.ToolbarAction

interface ToolbarListItem {
}

class ToolbarSeparatorItem: ToolbarListItem {
}

class ToolbarActionItem(val action: ToolbarAction, var image: Int) : ToolbarListItem {
    val title = action.title
}
