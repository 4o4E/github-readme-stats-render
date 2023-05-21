package top.e404.status.render.plugin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import top.e404.status.render.Theme
import top.e404.status.render.WakatimeRender
import top.e404.status.render.config.ServerConfig

fun Application.routing() = routing {
    get("/wakatime/themes") {
        call.respondText(Json.encodeToString(ServerConfig.config.themes.keys), ContentType.Application.Json)
    }

    get("/wakatime/{type}/{username}/{range}") {
        val username = call.parameters["username"]!!
        val range = WakatimeRender.FetchRange.byName(call.parameters["range"]!!)
        if (range == null) {
            call.respond(HttpStatusCode.BadRequest, "invalid range")
            return@get
        }
        val theme = call.request.queryParameters["theme"]?.let { ServerConfig.config.themes[it] } ?: Theme.default
        try {
            when (val type = call.parameters["type"]!!.lowercase()) {
                "lang" -> {
                    val allLang = call.request.queryParameters["all"]?.toBoolean() == true
                    call.respondBytes(WakatimeRender.renderLang(username, range, allLang, theme), ContentType.Image.PNG)
                }

                "editor" -> call.respondBytes(WakatimeRender.renderEditor(username, range, theme), ContentType.Image.PNG)

                else -> call.respondText("unknown request type: $type", ContentType.Text.Plain, HttpStatusCode.BadRequest)
            }
        } catch (e: Exception) {
            call.respondText(e.message ?: "", ContentType.Text.Plain, HttpStatusCode.BadRequest)
        }
    }
}
