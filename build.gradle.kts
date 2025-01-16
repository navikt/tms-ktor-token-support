plugins {
    kotlin("jvm") version Kotlin.version
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

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
