package top.e404.status.render.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.LocalDateTime
import kotlin.test.Test

class TestGithubRender {

    @Test
    fun testRenderContribution2d() {
        runBlocking(Dispatchers.IO) {
            val bytes = TestConfig.githubRender.renderContribution2d(
                "4o4E",
                LocalDateTime.now(),
                TestConfig.themes2d
            )
            File("github_contribution_2d.png").writeBytes(bytes)
        }
    }

    @Test
    fun testRenderContribution3d() {
        runBlocking(Dispatchers.IO) {
            val bytes = TestConfig.githubRender.renderContribution3d(
                "4o4e",
                TestConfig.themes3d
            )
            File("github_contribution_3d.png").writeBytes(bytes)
        }
    }
}