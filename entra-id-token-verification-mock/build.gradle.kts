plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(libs.ktor.client.core)
    api(libs.ktor.server.auth)
    api(project(":entra-id-token-verification"))
    implementation(libs.auth0.javaJwt)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.moduleKotlin)
    implementation(libs.kotlinLogging)
    implementation(libs.ktor.client.apache5)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.http)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.utils)
    implementation(libs.nimbusds.jose)
    runtimeOnly(libs.jackson3.core)
    runtimeOnly(libs.jackson3.databind)
    runtimeOnly(libs.logback.classic)

    testImplementation(libs.junit.api)
    testImplementation(libs.kotest.assertionsCore)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.ktor.serialization)
    testImplementation(libs.ktor.server.contentNegotiation)
    testImplementation(libs.ktor.test.server)
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.launcher)
}

repositories {
    mavenCentral()
    mavenLocal()
}

val libraryVersion: String = providers.gradleProperty("lib_version")
    .orElse("latest-local")
    .get()

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
            artifactId = "entra-id-token-verification-mock"
            version = libraryVersion
            from(components["java"])

            val sourcesJar = tasks.register<Jar>("sources") {
                archiveClassifier.set("sources")
                from(sourceSets.main.get().allSource)
            }

            artifact(sourcesJar)
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
