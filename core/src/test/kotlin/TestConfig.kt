package top.e404.status.render.test

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.decodeFromString
import org.jetbrains.skia.FontMgr
import org.junit.jupiter.api.BeforeAll
import top.e404.skiko.draw.compose.DefaultTypefaceProvider
import top.e404.status.render.Config
import top.e404.status.render.feature.Heatmap2dRender
import top.e404.status.render.feature.Heatmap3dRender
import top.e404.status.render.platform.GithubRender
import top.e404.status.render.platform.WakatimeRender
import java.io.File

object TestConfig {
    val yaml = Yaml(configuration = YamlConfiguration(strictMode = false, polymorphismStyle = PolymorphismStyle.Property))
    val config = yaml.decodeFromString<Config>(File("config.yml").readText())
    val wakatimeRender: WakatimeRender = WakatimeRender(config)
    val githubRender: GithubRender = GithubRender(config)
    val themes2d: Heatmap2dRender.Theme = config.themes2d["tokyonight"]!!
    val themes3d: Heatmap3dRender.Theme = config.themes3d["rainbow"]!!

    init {
        DefaultTypefaceProvider.default = FontMgr.default.makeFromFile("font/JetBrainsMono-Medium.ttf")!!
    }
}