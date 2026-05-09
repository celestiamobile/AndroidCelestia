package space.celestia.celestiafoundation.resource.model

import androidx.annotation.Keep
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Date

private object DateAsEpochSecondsSerializer : KSerializer<Date> {
    override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.DOUBLE)
    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeDouble(value.time / 1000.0)
    override fun deserialize(decoder: Decoder): Date = Date((decoder.decodeDouble() * 1000.0).toLong())
}

@Keep
@Serializable
data class AddonUpdate(
    val checksum: String,
    val size: Int,
    @Serializable(with = DateAsEpochSecondsSerializer::class)
    val modificationDate: Date
)
