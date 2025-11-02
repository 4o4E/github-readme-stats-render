package top.e404.status.render

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.InetSocketAddress
import java.net.Proxy

interface IConfig {
    val proxyConfig: ProxyConfig?
    val wakaToken: String
    val githubToken: String
    val layout: Layout
    val themes: Map<String, Theme>

    val client: HttpClient

    companion object {
        lateinit var default: IConfig
    }
}

@Serializable
open class Config : IConfig {
    @SerialName("proxy")
    override val proxyConfig: ProxyConfig? = null

    @SerialName("waka_token")
    override val wakaToken: String = "waka_xxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    @SerialName("github_token")
    override val githubToken: String = "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    override val layout: Layout = Layout()
    override val themes: Map<String, Theme> = emptyMap()

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
}

@Serializable
data class ProxyConfig(
    val type: String,
    val host: String = "localhost",
    val port: Int = 7890
) {
    fun toProxy() = Proxy(Proxy.Type.valueOf(type.uppercase()), InetSocketAddress(host, port))
}
