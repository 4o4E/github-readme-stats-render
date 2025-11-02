package top.e404.status.render.test

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import org.junit.jupiter.api.BeforeAll
import top.e404.status.render.Config
import top.e404.status.render.Theme
import top.e404.status.render.platform.GithubRender
import java.io.File
import java.time.LocalDateTime
import kotlin.test.Test

class TestGithubRender {
    companion object {
        lateinit var config: Config
        lateinit var githubRender: GithubRender
        lateinit var defaultTheme: Theme
        @BeforeAll
        @JvmStatic
        fun load() {
            config = Yaml(configuration = YamlConfiguration(strictMode = false))
                .decodeFromString<Config>(File("config.yml").readText())
            githubRender = GithubRender(config)
            defaultTheme = config.themes["tokyonight"]!!
        }
    }

    @Test
    fun testRenderCommit() {
        runBlocking(Dispatchers.IO) {
            val bytes = githubRender.renderCommit(
                "4o4E",
                LocalDateTime.now(),
                defaultTheme
            )
            File("github_commit.png").writeBytes(bytes)
        }
    }
}