plugins {
    alias(managed.plugins.kotlin)
}

subprojects {
    pluginManager.withPlugin("java-library") {
        dependencies {
            add("api", kotlin("stdlib-jdk8"))
            add("implementation", libs.ktor.client.json)
            add("implementation", managed.logback.encoder)
            add("implementation", managed.logback.classic)
            add("implementation", managed.kotlinLogging)
            add("implementation", managed.ktor.server.auth)
            add("implementation", managed.ktor.server.auth.jwt)

            add("testImplementation", managed.junit.launcher)
            add("testImplementation", managed.junit.api)
            add("testImplementation", managed.junit.engine)
            add("testImplementation", managed.ktor.test.clientMock)
            add("testImplementation", managed.ktor.test.server)
            add("testImplementation", managed.kotest.assertion)
            add("testImplementation", managed.kotest.extensions)
        }
    }
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
