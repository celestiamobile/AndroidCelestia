package space.celestia.celestiafoundation.resource.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
class GuideItem(val id: String, val title: String)