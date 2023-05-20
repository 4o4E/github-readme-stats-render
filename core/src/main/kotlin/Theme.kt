package top.e404.status.render

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Theme(
    @SerialName("title_color") @Serializable(ColorSerializer::class) val titleColor: Int = 0xff2f80ed.toInt(),
    @SerialName("icon_color") @Serializable(ColorSerializer::class) val iconColor: Int = 0xff4c71f2.toInt(),
    @SerialName("text_color") @Serializable(ColorSerializer::class) val textColor: Int = 0xff434d58.toInt(),
    @SerialName("bg_color") @Serializable(ColorSerializer::class) val bgColor: Int = 0xfffffefe.toInt(),
    @SerialName("border_color") @Serializable(ColorSerializer::class) val bolderColor: Int = 0xffe4e2e2.toInt(),
) {
    companion object {
        val default = Theme()
    }
}

object ColorSerializer : KSerializer<Int> {
    override val descriptor = PrimitiveSerialDescriptor(this::class.java.name, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Int) = encoder.encodeString(value.toString(16))

    override fun deserialize(decoder: Decoder): Int = decoder.decodeString().toLong(16).toInt()
}
