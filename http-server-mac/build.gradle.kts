plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

application {
    mainClass.set("top.e404.status.render.App")
    applicationDefaultJvmArgs = listOf(
        "-Dio.netty.tryReflectionSetAccessible=true",
        "--add-opens",
        "java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.util=ALL-UNNAMED"
    )
}

dependencies {
    implementation(project(":http-server-win")) {
        exclude("org.jetbrains.skiko")
    }

    // skiko
    implementation(skiko("macos-x64"))
}

tasks {
    runShadow {
        workingDir = rootProject.projectDir.resolve("run")
        doFirst {
            if (workingDir.isFile) workingDir.delete()
            workingDir.mkdirs()
        }

        jvmArgs = mutableListOf(
            "-Xmx8g",
            "-Xms8g",
        )
    }

    shadowJar {
        archiveFileName.set("${project.name}.jar")
        val jar = project.rootDir.resolve("jar")
        doLast {
            jar.mkdir()
            project.buildDir
                .resolve("libs/${project.name}.jar")
                .copyTo(jar.resolve("${project.name}.jar"), true)
        }
    }
}
