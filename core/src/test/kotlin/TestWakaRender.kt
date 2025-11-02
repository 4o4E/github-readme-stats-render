package top.e404.status.render.test

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import org.junit.jupiter.api.BeforeAll
import top.e404.status.render.Config
import top.e404.status.render.Theme
import top.e404.status.render.platform.WakatimeRender
import java.io.File
import kotlin.test.Test

class TestWakaRender {
    companion object {
        lateinit var config: Config
        lateinit var wakatimeRender: WakatimeRender
        lateinit var defaultTheme: Theme
        @BeforeAll
        @JvmStatic
        fun load() {
            config = Yaml(configuration = YamlConfiguration(strictMode = false))
                .decodeFromString<Config>(File("config.yml").readText())
            wakatimeRender = WakatimeRender(config)
            defaultTheme = config.themes["tokyonight"]!!
        }
    }

    @Test
    fun testRenderLang() {
        runBlocking(Dispatchers.IO) {
            val bytes = wakatimeRender.renderLang("404E", WakatimeRender.FetchRange.LAST_6_MONTHS, false, defaultTheme)
            File("waka_lang.png").writeBytes(bytes)
        }
    }

    @Test
    fun testRenderEditor() {
        runBlocking(Dispatchers.IO) {
            val bytes = wakatimeRender.renderEditor("404E", WakatimeRender.FetchRange.LAST_6_MONTHS, defaultTheme)
            File("waka_editor.png").writeBytes(bytes)
        }
    }
}
