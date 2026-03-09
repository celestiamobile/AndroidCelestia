// ResourceItem.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiafoundation.resource.model

import android.os.Parcelable
import androidx.annotation.Keep
import java.io.Serializable
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
class ResourceItem(
    val id: String,
    val name: String,
    val type: String?,
    val description: String,
    val item: String,
    val checksum: String?,
    val image: String?,
    val authors: List<String>?,
    val objectName: String?,
    val mainScriptName: String?
): Serializable, Parcelable