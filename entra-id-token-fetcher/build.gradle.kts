plugins {
    `maven-publish`
    `java-library`
    alias(managed.plugins.kotlin)
}

dependencies {
    implementation(managed.caffeine)
    implementation(managed.ktor.client.apache5)
    implementation(managed.ktor.client.contentNegotiation)
    implementation(managed.ktor.serialization.jackson)
    implementation(libs.ktor.serialization)
    implementation(managed.ktor.server.netty)
    implementation(libs.nimbusds.jose.jwt)

    testImplementation(managed.mockk)
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
            artifactId = "entra-id-token-fetcher"
            version = libraryVersion
            from(components["java"])

            val sourcesJar by tasks.registering(Jar::class) {
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
