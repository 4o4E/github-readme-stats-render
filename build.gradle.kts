import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin apply false
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    group = Versions.group
    version = Versions.version

    repositories {
        mavenCentral()
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "11"
        }
    }

    kotlin {
        jvmToolchain(11)
    }
}
