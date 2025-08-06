// ListSection.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.common
open class CommonSection<T>(val items: List<T>)
class CommonSectionV2<T>(items: List<T>, val header: String? = "", val footer: String? = null) : CommonSection<T>(items)