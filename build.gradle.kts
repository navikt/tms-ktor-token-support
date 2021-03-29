plugins {
    kotlin("jvm") version Kotlin.version
    kotlin("plugin.serialization") version (Kotlin.version)
    kotlin("plugin.allopen") version Kotlin.version
}

val libraryVersion = properties["lib_version"] ?: "latest-local"

subprojects {
    group = "no.nav"
    version = libraryVersion
}

repositories {
    mavenCentral()
    mavenLocal()
}

tasks {
    jar {
        enabled = false
    }
}
