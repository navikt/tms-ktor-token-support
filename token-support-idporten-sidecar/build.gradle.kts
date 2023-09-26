import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    implementation(Ktor.serverForwardedHeaders)
    implementation(Ktor.serverNetty)
    implementation(Nimbusds.oauth2OidcSdk)
    testImplementation(kotlin("test-junit5"))
    testImplementation(Kluent.kluent)
    testImplementation(Mockk.mockk)
    testImplementation(Ktor.clientMock)
    testImplementation(Ktor.serverTestHost)
    testImplementation(Kotest.runnerJunit)
    testImplementation(Kotest.assertionsCore)
    testImplementation(Kotest.extensions)
    testImplementation(Nimbusds.joseJwt)
}

repositories {
    mavenCentral()
    mavenLocal()
}

publishing {
    repositories{
        mavenLocal()
    }

    publications {
        create<MavenPublication>("local") {
            from(components["java"])
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}
