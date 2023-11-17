plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    implementation(Logback.classic)
    implementation(KotlinLogging.logging)
    implementation(Ktor.serverAuth)
    implementation(Ktor.serverAuthJwt)
    implementation(Ktor.clientApache)
    implementation(Ktor.clientJson)
    implementation(Ktor.serialization)
    implementation(Ktor.clientContentNegotiation)
    implementation(Ktor.serializationKotlinxJson)
    implementation(Nimbusds.joseJwt)
    testImplementation(kotlin("test-junit5"))
    testImplementation(Kluent.kluent)
    testImplementation(Mockk.mockk)
    testImplementation(Ktor.clientMock)
    testImplementation(Ktor.serverTestHost)
    testImplementation(Kotest.runnerJunit)
    testImplementation(Kotest.assertionsCore)
    testImplementation(Kotest.extensions)
}

repositories {
    mavenCentral()
    mavenLocal()
}

val libraryVersion: String = properties["lib_version"]?.toString() ?: "latest-local"

publishing {
    repositories{
        mavenLocal()
        maven {
            url = uri("https://maven.pkg.github.com/navikt/tms-ktor-token-support")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("gpr") {
            groupId = "no.nav.tms.token.support"
            artifactId = "azure-validation"
            version = libraryVersion
            from(components["java"])
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}

