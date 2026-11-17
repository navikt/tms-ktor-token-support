plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(libs.ktor.client.core)
    api(libs.ktor.server.core)
    api(libs.ktor.utils)
    implementation(libs.apache.httpclient5)
    implementation(libs.auth0.javaJwt)
    implementation(libs.auth0.jwksRsa)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.moduleKotlin)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.ktor.client.apache5)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.http)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.jackson)
    runtimeOnly(libs.logback.classic)


    testImplementation(libs.junit.api)
    testImplementation(libs.kotest.assertionsShared)
    testImplementation(libs.kotest.assertionsCore)
    testImplementation(libs.kotest.common)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.ktor.io)
    testImplementation(libs.ktor.test.clientMock)
    testImplementation(libs.ktor.test.server)
    testImplementation(libs.mockk)
    testImplementation(libs.mockkCore)
    testImplementation(libs.mockkDsl)
    testImplementation(libs.nimbusds.oauth2OidcSdk)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.launcher)
}

repositories {
    mavenCentral()
    mavenLocal()
}

val libraryVersion: String = providers.gradleProperty("lib_version")
    .orElse("latest-local").toString()

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
            artifactId = "user-login-routes"
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
