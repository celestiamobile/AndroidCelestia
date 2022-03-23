package space.celestia.mobilecelestia.resource.model

import java.io.Serializable

interface AsyncListItem: Serializable {
    val name: String
    val id: String
}
