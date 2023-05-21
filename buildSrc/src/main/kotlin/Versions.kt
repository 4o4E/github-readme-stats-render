object Versions {
    const val group = "top.e404"
    const val version = "1.1.0"
    const val kotlin = "1.8.20"
    const val skiko = "0.7.37"
    const val ktor = "2.2.3"
    const val log4j = "2.20.0"
    const val kaml = "0.52.0"
}

fun kotlinx(id: String, version: String = Versions.kotlin) = "org.jetbrains.kotlinx:kotlinx-$id:$version"
fun skiko(module: String, version: String = Versions.skiko) = "org.jetbrains.skiko:skiko-awt-runtime-$module:$version"
fun ktor(module: String, version: String = Versions.ktor) = "io.ktor:ktor-$module:$version"
fun log4j(module: String, version: String = Versions.log4j) = "org.apache.logging.log4j:log4j-$module:$version"
const val kaml = "com.charleskorn.kaml:kaml:${Versions.kaml}"
