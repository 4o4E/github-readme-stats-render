@file:JvmName("App")

package top.e404.status.render

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import sun.misc.Signal
import sun.misc.SignalHandler
import top.e404.status.render.config.ServerConfig
import top.e404.status.render.plugin.logging
import top.e404.status.render.plugin.negotiation
import top.e404.status.render.plugin.routing
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import kotlin.system.exitProcess


val appLog = LoggerFactory.getLogger("App")!!

private lateinit var engine: NettyApplicationEngine
private lateinit var channel: FileChannel
private lateinit var lock: FileLock

suspend fun main() {
    try {
        withContext(Dispatchers.IO) {
            channel = FileOutputStream(".lock").channel
            lock = channel.tryLock()
        }
    } catch (t: Throwable) {
        appLog.error("请勿打开多个实例")
        exitProcess(1)
    }

    SignalHandler { stop() }.let {
        // ctrl + c
        Signal.handle(Signal("INT"), it)
        // alt + f4
        Signal.handle(Signal("TERM"), it)
    }

    ServerConfig.load()
    WakatimeRender.config = ServerConfig.config

    engine = embeddedServer(
        factory = Netty,
        host = ServerConfig.config.host,
        port = ServerConfig.config.port,
        module = Application::module
    )
    engine.start(true)
}

private fun stop() {
    appLog.info("正在关闭")
    engine.stop()
}

fun Application.module() {
    logging()
    routing()
    negotiation()
}
