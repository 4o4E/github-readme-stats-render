plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    // slf4j
    implementation("org.slf4j:slf4j-api:2.0.7")
    // log4j2
    implementation(log4j("core"))
    implementation(log4j("slf4j2-impl")) {
        exclude("org.slf4j")
    }
    // 异步
    implementation("com.lmax:disruptor:3.4.4")

    api(ktor("client-core-jvm"))
    api(ktor("client-okhttp-jvm"))

    // serialization
    api(kotlinx("serialization-core-jvm", "1.5.0"))
    api(kotlinx("serialization-json", "1.5.0"))

    // skiko
    implementation(skiko("windows-x64"))

    // test
    testImplementation(kotlin("test", Versions.kotlin))
    // kaml
    testImplementation(kaml)
}

tasks {
    test {
        useJUnitPlatform()
        workingDir = rootProject.projectDir.resolve("run")
        workingDir.mkdir()
    }
}
