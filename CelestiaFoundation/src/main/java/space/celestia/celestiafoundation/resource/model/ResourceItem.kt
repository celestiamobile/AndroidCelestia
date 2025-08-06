// ResourceItem.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiafoundation.resource.model

import androidx.annotation.Keep
import java.io.Serializable
import java.util.*

@Keep
class ResourceItem(
    val id: String,
    val name: String,
    val type: String?,
    val description: String,
    val item: String,
    val image: String?,
    val authors: List<String>?,
    val publishTime: Date?,
    val objectName: String?,
    val mainScriptName: String?
): Serializable