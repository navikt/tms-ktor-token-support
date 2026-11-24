plugins {
    `maven-publish`
    `java-library`
    alias(managed.plugins.kotlin)
}

dependencies {
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.nimbusds.oauth)

    implementation(managed.ktor.client.apache5)
    implementation(managed.ktor.client.contentNegotiation)
    implementation(managed.ktor.serialization.jackson)
    implementation(managed.ktor.server.netty)

    testImplementation(managed.mockk)
    testImplementation(libs.nimbusds.jose.jwt)
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
            artifactId = "user-login-routes"
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
