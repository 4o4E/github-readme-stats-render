package top.e404.status.render.plugin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import top.e404.status.render.platform.WakatimeRender
import top.e404.status.render.config.ServerConfig
import top.e404.status.render.feature.Heatmap2dRender
import top.e404.status.render.platform.GithubRender
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun Application.routing(wakatimeRender: WakatimeRender, githubRender: GithubRender) = routing {
    get("/wakatime/themes") {
        call.respondText(Json.encodeToString(ServerConfig.config.themes2d.keys), ContentType.Application.Json)
    }

    get("/wakatime/{type}/{username}/{range}") {
        val username = call.parameters["username"]!!
        val range = WakatimeRender.FetchRange.byName(call.parameters["range"]!!)
        if (range == null) {
            call.respond(HttpStatusCode.BadRequest, "invalid range")
            return@get
        }
        val theme = call.request.queryParameters["theme"]?.let { ServerConfig.config.themes2d[it] } ?: Heatmap2dRender.Theme.default
        try {
            when (val type = call.parameters["type"]!!.lowercase()) {
                "lang" -> {
                    val allLang = call.request.queryParameters["all"]?.toBoolean() == true
                    call.respondBytes(wakatimeRender.renderLang(username, range, allLang, theme), ContentType.Image.PNG)
                }

                "editor" -> call.respondBytes(wakatimeRender.renderEditor(username, range, theme), ContentType.Image.PNG)

                else -> call.respondText("unknown request type: $type", ContentType.Text.Plain, HttpStatusCode.BadRequest)
            }
        } catch (e: Exception) {
            call.respondText(e.message ?: "", ContentType.Text.Plain, HttpStatusCode.BadRequest)
        }
    }

    get("/github/contribution/{username}/{end}") {
        val username = call.parameters["username"]!!
        val end = try {
            call.parameters["end"]!!.let { LocalDate.parse(it) }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "invalid end date: ${e.message}")
            return@get
        }
        val theme = call.request.queryParameters["theme"]?.let {
            ServerConfig.config.themes2d[it]
        } ?: Heatmap2dRender.Theme.default
        try {
            call.respondBytes(githubRender.renderContribution2d(username, LocalDateTime.of(end, LocalTime.MIN), theme), ContentType.Image.PNG)
        } catch (e: Exception) {
            call.respondText(e.message ?: "", ContentType.Text.Plain, HttpStatusCode.BadRequest)
        }
    }

    get("/github/contribution3d/{username}") {
        val username = call.parameters["username"]!!
        try {
            val theme3d = call.request.queryParameters["theme"]?.let { theme ->
                ServerConfig.config.themes3d[theme]
            } ?: ServerConfig.config.themes3d["rainbow"]!!
            call.respondBytes(githubRender.renderContribution3d(username, theme3d), ContentType.Image.PNG)
        } catch (e: Exception) {
            call.respondText(e.message ?: "", ContentType.Text.Plain, HttpStatusCode.BadRequest)
        }
    }
}
