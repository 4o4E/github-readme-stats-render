package top.e404.status.render.config

import com.charleskorn.kaml.Yaml
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import top.e404.status.render.*
import java.io.File

@Serializable
data class ServerConfig(
    val host: String = "localhost",
    val port: Int = 2345,
    @SerialName("proxy") override val proxyConfig: ProxyConfig? = null,
    @SerialName("waka_token") override val token: String,
    override val layout: Layout,
    override val themes: Map<String, Theme>
): IConfig {
    override val client by lazy {
        HttpClient(OkHttp) {
            engine {
                config {
                    followRedirects(true)
                }
                proxyConfig?.let {
                    proxy = it.toProxy()
                }
            }
        }
    }

    companion object {
        private val file = File("config.yml")
        lateinit var config: ServerConfig
            private set

        fun saveDefault(): ByteArray? {
            if (file.exists()) return null
            return this::class.java.classLoader
                .getResourceAsStream("config.yml")!!
                .use { it.readBytes() }
                .also { file.writeBytes(it) }
        }

        fun load() {
            val yaml = String(saveDefault() ?: file.readBytes(), Charsets.UTF_8)
            config = Yaml.default.decodeFromString(yaml)
        }
    }
}
