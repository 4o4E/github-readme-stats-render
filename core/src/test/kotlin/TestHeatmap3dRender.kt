package top.e404.status.render.test

import org.junit.jupiter.api.Test
import top.e404.status.render.feature.Heatmap3dRender
import java.io.File
import java.time.LocalDate
import kotlin.random.Random

class TestHeatmap3dRender {
    val maxValue = 6

    @Test
    fun test_gen_rainbow() {
        val now = LocalDate.now()
        val data: MutableList<Pair<LocalDate, Int?>> = (0..365).map {
            val value = if (Random.nextInt(3) > 1) Random.nextInt(0, maxValue) else 0
            now.minusDays(365L - it) to value
        }.toMutableList()
        Heatmap3dRender.render(
            data, TestConfig.config.layout3d, TestConfig.themes3d
        ).encodeToData()!!.let { data ->
            File("heatmap3d.png").writeBytes(data.bytes)
        }
    }
}