package top.e404.status.render

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import org.jetbrains.skia.*
import kotlin.math.max

object WakatimeRender {
    var config: IConfig = Config()
    private val layout inline get() = config.layout
    private val titleFont inline get() = layout.titleFont
    private val langFont inline get() = layout.langFont
    private val textFont inline get() = layout.textFont

    private val bgRadii inline get() = layout.bgRadii
    private val strokeRadii inline get() = layout.strokeRadii

    private val margin inline get() = layout.margin
    private val spacing inline get() = layout.spacing
    private val barPadding inline get() = layout.barPadding
    private val barHeight inline get() = layout.barHeight
    private val barWidth inline get() = layout.barWidth

    private val client get() = config.client

    private val radii = floatArrayOf(barHeight / 2, barHeight / 2, barHeight / 2, barHeight / 2)

    suspend fun renderLang(user: String, range: FetchRange, allLang: Boolean, theme: Theme): ByteArray {
        val stats = fetchUserStats(user, range)
        val langs = stats["languages"]!!.jsonArray.map {
            it as JsonObject
            Lang(it["name"]!!.jsonPrimitive.content, it["percent"]!!.jsonPrimitive.float, it["text"]!!.jsonPrimitive.content)
        }.let { langs ->
            if (allLang) langs
            else langs.filter { it.duration != "0 secs" }
        }

        val maxLangName = langs.maxOf { it.nameWidth }.toInt()
        val maxLangText = langs.maxOf { it.textWidth }.toInt()
        val titleLine = TextLine.make("$user's wakatime status in ${range.display}", titleFont)
//        val barWidth = titleLine.width - maxLangName - 40 - maxLangText
        val width = maxLangName + barWidth.toInt() + barPadding.toInt() * 2 + maxLangText + margin.toInt() * 2
        val height = langs.size * spacing.toInt() + margin.toInt() * 2 + titleFont.metrics.run { descent - ascent }.toInt()

        val surface = Surface.makeRasterN32Premul(width, height)
        val canvas = surface.canvas
        // bg
        canvas.drawRRect(
            r = RRect.makeComplexXYWH(
                l = 0F,
                t = 0F,
                w = width.toFloat(),
                h = height.toFloat(),
                radii = floatArrayOf(bgRadii, bgRadii, bgRadii, bgRadii)
            ),
            paint = Paint().apply {
                color = theme.bgColor
            }
        )
        // stroke
        canvas.drawRRect(
            r = RRect.makeComplexXYWH(
                l = 0F,
                t = 0F,
                w = width.toFloat(),
                h = height.toFloat(),
                radii = floatArrayOf(strokeRadii, strokeRadii, strokeRadii, strokeRadii)
            ),
            paint = Paint().apply {
                mode = PaintMode.STROKE
                strokeWidth = .5F
                color = theme.bolderColor
            }
        )
        val x = margin
        var y = margin - titleFont.metrics.ascent
        canvas.drawTextLine(titleLine, x, y, Paint().apply { color = theme.titleColor })
        y += spacing + titleFont.metrics.descent
        val paint = Paint().apply {
            color = theme.textColor
        }
        for (lang in langs) {
            // lang name
            canvas.drawTextLine(lang.nameLine, x, y, paint)
            // lang bar
            canvas.drawRRect(
                RRect.makeComplexXYWH(x + maxLangName + barPadding, y - barHeight, barWidth, barHeight, radii),
                Paint().apply { color = theme.textColor }
            )
            canvas.drawRRect(
                RRect.makeComplexXYWH(x + maxLangName + barPadding, y - barHeight, max(barWidth * lang.percent / 100, barHeight), barHeight, radii),
                Paint().apply { color = theme.titleColor }
            )
            // lang percent
            canvas.drawTextLine(lang.textLine, x + maxLangName + barWidth + barPadding * 2, y, paint)
            y += spacing
        }
        return surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)!!.bytes
    }

    data class Lang(
        val langName: String,
        val percent: Float,
        val duration: String
    ) {
        val nameLine = TextLine.make(langName, langFont)
        val nameWidth get() = nameLine.width
        val textLine = TextLine.make(duration, textFont)
        val textWidth get() = textLine.width
    }

    suspend fun fetchUserStats(user: String, range: FetchRange): JsonObject {
        val jo = client.get("https://wakatime.com/api/v1/users/${user}/stats/${range.path}") {
            parameter("is_including_today", true)
            parameter("api_key", config.token)
        }.bodyAsText().let { Json.parseToJsonElement(it).jsonObject }
        if (jo["message"]?.jsonPrimitive?.content?.contains("Calculating") == true) {
            delay(500)
            return fetchUserStats(user, range)
        }
        val error = jo["error"]?.jsonPrimitive?.content
        if (error != null) throw Exception("unknown error $error")
        return jo["data"]!!.jsonObject
    }

    @Suppress("UNUSED")
    enum class FetchRange(val path: String, val display: String, regex: String) {
        LAST_7_DAYS("last_7_days", "last 7 days", "(?i)7d"),
        LAST_30_DAYS("last_30_days", "last 30 days", "(?i)30d"),
        LAST_6_MONTHS("last_6_months", "last 6 months", "(?i)6m"),
        LAST_YEAR("last_year", "last year", "(?i)year|1?y"),
        ALL_TIME("all_time", "all time", "(?i)all|a");

        val regex = Regex(regex)

        companion object {
            fun byName(name: String) = FetchRange.values().firstOrNull { it.regex.matches(name) }
        }
    }
}
