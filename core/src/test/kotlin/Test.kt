package top.e404.status.render.test

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import org.junit.jupiter.api.BeforeAll
import top.e404.status.render.Config
import top.e404.status.render.Theme
import top.e404.status.render.WakatimeRender
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import kotlin.test.Test

class CommonTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun load() {
            WakatimeRender.config = Yaml(configuration = YamlConfiguration(strictMode = false)).decodeFromString<Config>(File("config.yml").readText())
        }
    }

    @Test
    fun testFetch() {
        runBlocking(Dispatchers.IO) {
            WakatimeRender.fetchUserStats("404E", WakatimeRender.FetchRange.LAST_30_DAYS)
        }
    }

    @Test
    fun testRenderLang() {
        runBlocking(Dispatchers.IO) {
            val render = WakatimeRender.renderLang("404E", WakatimeRender.FetchRange.LAST_30_DAYS, false, Theme.default)
            File("out.png").writeBytes(render)
        }
    }

    @Test
    fun testRenderEditor() {
        runBlocking(Dispatchers.IO) {
            val render = WakatimeRender.renderEditor("404E", WakatimeRender.FetchRange.LAST_6_MONTHS, Theme.default)
            File("out.png").writeBytes(render)
        }
    }

    @Test
    fun testTime() {
        runBlocking(Dispatchers.IO) {
            val l = System.currentTimeMillis()
            HttpClient(OkHttp) {
                engine {
                    config {
                        followRedirects(true)
                    }

                    proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("localhost", 7890))
                }
            }.get("https://wakatime.com/api/v1/users/404E/stats/last_6_months") {
                parameter("is_including_today", true)
                parameter("api_key", WakatimeRender.config.token)
            }.bodyAsText()
            println(System.currentTimeMillis() - l)
        }
    }
}
