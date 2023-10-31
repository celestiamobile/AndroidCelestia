/*
 * AboutItem.kt
 *
 * Copyright (C) 2001-2023, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

sealed class AboutItem
class VersionItem(val versionName: String) : AboutItem()
class ActionItem(val title: String, val url: String) : AboutItem()
class TitleItem(val title: String) : AboutItem()
class DetailItem(val detail: String) : AboutItem()