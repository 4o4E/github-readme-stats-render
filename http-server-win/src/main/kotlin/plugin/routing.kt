package top.e404.status.render.plugin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import top.e404.status.render.Theme
import top.e404.status.render.WakatimeRender
import top.e404.status.render.config.ServerConfig

fun Application.routing() = routing {
    get("/wakatime/{username}/{range}") {
        val username = call.parameters["username"]!!
        val range = WakatimeRender.FetchRange.byName(call.parameters["range"]!!)
        if (range == null) {
            call.respond(HttpStatusCode.BadRequest, "invalid range")
            return@get
        }
        val theme = call.request.queryParameters["theme"]?.let { ServerConfig.config.themes[it] } ?: Theme.default
        val allLang = call.request.queryParameters["all"]?.toBoolean() == true
        val bytes = WakatimeRender.renderLang(username, range, allLang, theme)
        call.respondBytes(bytes, ContentType.Image.PNG)
    }
}
