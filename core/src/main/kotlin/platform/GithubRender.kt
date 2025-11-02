package top.e404.status.render.platform

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.jetbrains.skia.Color
import top.e404.skiko.draw.compose.*
import top.e404.skiko.util.argb
import top.e404.status.render.IConfig
import top.e404.status.render.Theme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class GithubRender(val config: IConfig) {
    private companion object {
        const val URL = "https://api.github.com/graphql"
    }

    private val layout inline get() = config.layout
    private val client inline get() = config.client
    private suspend fun fetchCommitCount(username: String, end: LocalDateTime): JsonObject {
        val query = $$"""
            query userContributions($username: String!, $from: DateTime!, $to: DateTime!) {
                user(login: $username) {
                    contributionsCollection(from: $from, to: $to) {
                        contributionCalendar {
                            weeks {
                                contributionDays {
                                    contributionCount
                                    date
                                }
                            }
                        }
                    }
                }
            }
        """.replace(Regex("\\s{2,}"), " ")
        return client.post(URL) {
            header("Authorization", "bearer ${config.githubToken}")
            setBody(Json.encodeToString(buildJsonObject {
                put("query", JsonPrimitive(query))
                put("variables", buildJsonObject {
                    put("username", username)
                    put("from", end.minusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    put("to", end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                })
            }))
        }.bodyAsText().let { Json.parseToJsonElement(it) }.jsonObject
    }

    suspend fun renderCommit(username: String, end: LocalDateTime, theme: Theme): ByteArray {
        val commitInfo = fetchCommitCount(username, end = end)
        if (commitInfo["errors"]?.jsonArray?.isNotEmpty() == true) {
            val errors = commitInfo["errors"]!!.jsonArray.joinToString("\n") {
                it.jsonObject["message"]!!.jsonPrimitive.content
            }
            throw IllegalArgumentException("Failed to fetch commit info:\n${errors}")
        }
        val weeks = commitInfo.byPath(
            "data",
            "user",
            "contributionsCollection",
            "contributionCalendar",
            "weeks"
        )!!.jsonArray

        val days: MutableList<Pair<LocalDate, Int?>> = weeks.flatMap { week ->
            week.jsonObject["contributionDays"]!!.jsonArray.map { day ->
                day.jsonObject.let {
                    LocalDate.parse(it["date"]!!.jsonPrimitive.content) to it["contributionCount"]!!.jsonPrimitive.int
                }
            }
        }.toMutableList()
        run {
            // 拆分出到第一个周一之前的零散天数
            val first = days.first().first
            val dayOfWeek = first.dayOfWeek.value // 1-7
            repeat(dayOfWeek - 1) {
                days.add(it, first.minusDays((dayOfWeek - 1 - it).toLong()) to null)
            }
        }

        // 需要处理跨年的情况
        val first = days.first().first
        val weekList = days.groupBy {
            // 计算和第一天的差的天数
            ChronoUnit.DAYS.between(first, it.first) / 7
        }
        val byMonth: Map<Pair<Int, Int>, List<List<Pair<LocalDate, Int?>>>> = weekList.values.groupBy {
            it.first().first.run { year to monthValue }
        }.toSortedMap { (y1, m1), (y2, m2) ->
            if (y1 != y2) y1 - y2 else m1 - m2
        }

        val max = days.maxOf { it.second ?: 0 }
        val (_, sr, sg, sb) = theme.bgColor.argb()
        val (_, er, eg, eb) = theme.titleColor.argb()
        fun getColor(count: Int): Int {
            val ratio = count.toFloat() / max
            val r = (sr + (er - sr) * ratio).toInt()
            val g = (sg + (eg - sg) * ratio).toInt()
            val b = (sb + (eb - sb) * ratio).toInt()
            return argb(255, r, g, b)
        }

        @UiDsl
        fun Row.week(week: List<Pair<LocalDate, Int?>>) = column {
            for ((_, count) in week) {
                val color = count?.let { getColor(it) } ?: Color.TRANSPARENT
                box(
                    Modifier.size(15f)
                        .background(color)
                        .border(.5f, if (count == null) Color.TRANSPARENT else theme.textColor)
                        .clip(Shape.RoundedRect(3f))
                        .margin(3f)
                )
            }
        }

        /**
         * 一个月的块, 包含了左上角的年月和下方的几周热力图
         */
        @UiDsl
        fun UiElement.months(
            year: Int,
            month: Int,
            byWeek: List<List<Pair<LocalDate, Int?>>>,
            index: Int
        ) {
            // 处理第一个月不满的情况
            column(horizontalAlignment = if (index == 0) HorizontalAlignment.Right else HorizontalAlignment.Left) {
                // 小于三周的情况下不显示年月
                text(
                    if (byWeek.size >= 3) "${year.toString().substring(2)}.${month.toString().padStart(2, '0')}"
                    else " ", Modifier
                        .textColor(theme.textColor)
                        .fontFamily(layout.textTypeface)
                        .fontSize(layout.textSize)
                        .margin(3f, 0f)
                )
                row {
                    for (week in byWeek) week(week)
                }
            }
        }

        return render {
            column(Modifier.background(theme.bgColor).padding(20f)) {
                text(
                    "$username's GitHub Commit from ${end.toLocalDate()} to ${end.minusYears(1).toLocalDate()}",
                    Modifier.textColor(theme.titleColor).fontFamily(layout.titleTypeface).fontSize(layout.titleSize)
                )
                row(Modifier.margin(top = 20f)) {
                    // 最左侧星期
                    val textModifier = Modifier
                        .textColor(theme.textColor)
                        .fontFamily(layout.textTypeface)
                        .fontSize(layout.textSize)
                    val boxModifier = Modifier.height(16f)
                    column(Modifier.margin(right = 10f)) {
                        // 一行字的高度
                        text(
                            " ", Modifier
                                .fontFamily(layout.textTypeface)
                                .fontSize(layout.textSize)
                        )
                        text("Mon", textModifier)
                        box(boxModifier)
                        text("Wed", textModifier)
                        box(boxModifier)
                        text("Fri", textModifier)
                        box(boxModifier)
                        text("Sat", textModifier)
                    }
                    // 每月数据
                    for ((index, e) in byMonth.entries.withIndex()) {
                        val (yearMonth, byWeek) = e
                        months(yearMonth.first, yearMonth.second, byWeek, index)
                    }
                }
            }
        }.encodeToData()!!.bytes
    }
}

private fun JsonObject.byPath(vararg path: String): JsonElement? {
    var current: JsonElement = this
    for (p in path) {
        current = current.jsonObject[p] ?: return null
    }
    return current
}
