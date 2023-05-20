package top.e404.status.render

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.InetSocketAddress
import java.net.Proxy

interface IConfig {
    val proxyConfig: ProxyConfig?
    val token: String
    val layout: Layout
    val themes: Map<String, Theme>

    val client: HttpClient
}

@Serializable
open class Config : IConfig {
    @SerialName("proxy")
    override val proxyConfig: ProxyConfig? = null

    @SerialName("waka_token")
    override val token: String = "waka_xxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
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
