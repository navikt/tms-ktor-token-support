plugins {
    alias(libs.plugins.kotlin)
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
