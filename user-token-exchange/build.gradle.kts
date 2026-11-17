plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(libs.jackson.annotations)
    implementation(libs.auth0.javaJwt)
    implementation(libs.jackson.databind)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.ktor.client.apache5)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.http)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.utils)
    implementation(libs.caffeine)
    implementation(libs.nimbusds.jose)

    testImplementation(libs.junit.api)
    testImplementation(libs.kotest.assertionsCore)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.mockk)
    testImplementation(libs.mockkCore)
    testImplementation(libs.mockkDsl)
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
            artifactId = "user-token-exchange"
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
