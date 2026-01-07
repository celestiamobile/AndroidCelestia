package space.celestia.celestiafoundation.resource.model

import androidx.annotation.Keep
import java.util.Date

@Keep
data class AddonUpdate(val checksum: String, val size: Int, val modificationDate: Date)