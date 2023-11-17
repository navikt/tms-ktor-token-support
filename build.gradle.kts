plugins {
    kotlin("jvm") version Kotlin.version
    kotlin("plugin.serialization") version (Kotlin.version)
    kotlin("plugin.allopen") version Kotlin.version
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
