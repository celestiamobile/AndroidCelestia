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
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Keep
@Serializable
class ResourceItem(
    val id: String,
    val name: String,
    val type: String? = null,
    val item: String,
    val checksum: String? = null,
    val objectName: String? = null,
    val mainScriptName: String? = null
): Parcelable