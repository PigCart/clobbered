pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.parchmentmc.org")
    }
}
plugins {
    id("dev.kikugie.stonecutter") version "0.9"
}

stonecutter {
    create(rootProject) {
        fun mc(version: String, vararg loaders: String) = loaders
            .forEach {
                if (it == "fabric") {
                    version("$version-$it", version).buildscript = "build.fabric.gradle.kts"
                } else {
                    version("$version-$it", version).buildscript = "build.$it.gradle.kts"
                }
            }

        mc("26.1", "fabric", "neoforge")
        mc("26.2", "fabric", "neoforge")

        vcsVersion = "26.1-fabric"
    }
}
