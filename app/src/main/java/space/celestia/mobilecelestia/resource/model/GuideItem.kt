package space.celestia.mobilecelestia.resource.model

import java.io.Serializable

class GuideItem(override val id: String, val title: String): AsyncListItem, Serializable {
    override val name: String
        get() = title
}