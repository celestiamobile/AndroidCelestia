package space.celestia.mobilecelestia.resource.model

import space.celestia.mobilecelestia.utils.GlideUrlCustomCacheKey
import java.io.Serializable

class GuideItem(override val id: String, val title: String): AsyncListItem, Serializable {
    override val name: String
        get() = title

    override val imageURL: GlideUrlCustomCacheKey?
        get() = null
}