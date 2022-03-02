package space.celestia.mobilecelestia.resource.model

import space.celestia.mobilecelestia.utils.GlideUrlCustomCacheKey
import java.io.Serializable

interface AsyncListItem: Serializable {
    val name: String
    val id: String
    val imageURL: GlideUrlCustomCacheKey?
}
