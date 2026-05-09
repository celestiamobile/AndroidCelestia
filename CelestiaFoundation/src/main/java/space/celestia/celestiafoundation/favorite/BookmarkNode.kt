// BookmarkNode.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiafoundation.favorite

import androidx.annotation.Keep
import java.io.Serializable

@Keep
@kotlinx.serialization.Serializable
class BookmarkNode(var name: String, var url: String, var children: ArrayList<BookmarkNode>? = null) : Serializable {
    val isLeaf: Boolean
        get() = children == null
}