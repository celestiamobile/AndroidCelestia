// HelpItem.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.help

open class HelpItem
class DescriptionItem(val description: String, val imageResourceID: Int) : HelpItem()
class ActionItem(val title: String, val action: HelpAction) : HelpItem()
class URLItem(val title: String, val url: String) : HelpItem()