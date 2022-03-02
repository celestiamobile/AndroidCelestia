/*
 * ResourceCategory.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource.model

import space.celestia.mobilecelestia.utils.GlideUrlCustomCacheKey
import java.io.Serializable

class ResourceCategory(override val name: String, val description: String, override val id: String, val image: String?): AsyncListItem, Serializable {
    override val imageURL: GlideUrlCustomCacheKey?
        get() {
            val image = this.image ?: return null
            return GlideUrlCustomCacheKey(image, id)
        }
}