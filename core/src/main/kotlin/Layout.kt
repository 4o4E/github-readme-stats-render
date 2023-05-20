package top.e404.status.render

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.skia.Data
import org.jetbrains.skia.Font
import org.jetbrains.skia.Typeface
import java.io.File

@Serializable
data class Layout(
    @SerialName("title_font") val titleFontFile: String = "font/JetBrainsMono-Bold.ttf",
    @SerialName("title_size") val titleSize: Float = 26F,
    @SerialName("lang_font") val langFontFile: String = "font/JetBrainsMono-Bold.ttf",
    @SerialName("lang_size") val langSize: Float = 20F,
    @SerialName("text_font") val textFontFile: String = "font/JetBrainsMono-Medium.ttf",
    @SerialName("text_size") val textSize: Float = 20F,
    @SerialName("bg_radii") val bgRadii: Float = 4.5F,
    @SerialName("stroke_radii") val strokeRadii: Float = 4.5F,
    val margin: Float = 25F,
    val spacing: Float = 30F,
    @SerialName("bar_padding") val barPadding: Float = 10F,
    @SerialName("bar_height") val barHeight: Float = 10F,
    @SerialName("bar_width") val barWidth: Float = 280F,

    ) {
    val titleTypeface by lazy {
        Typeface.makeFromData(Data.makeFromBytes(File(titleFontFile).readBytes()))
    }
    val titleFont by lazy {
        Font(titleTypeface, titleSize)
    }
    val langTypeface by lazy {
        Typeface.makeFromData(Data.makeFromBytes(File(langFontFile).readBytes()))
    }
    val langFont by lazy {
        Font(langTypeface, langSize)
    }
    val textTypeface by lazy {
        Typeface.makeFromData(Data.makeFromBytes(File(textFontFile).readBytes()))
    }
    val textFont by lazy {
        Font(textTypeface, textSize)
    }
}
