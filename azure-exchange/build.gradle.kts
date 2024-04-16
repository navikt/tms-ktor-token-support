plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    implementation(Logback.classic)
    implementation(KotlinLogging.logging)
    implementation(Caffeine.caffeine)
    implementation(Ktor.serverAuth)
    implementation(Ktor.serverAuthJwt)
    implementation(Ktor.clientApache)
    implementation(Ktor.clientContentNegotiation)
    implementation(Ktor.jackson)
    implementation(Ktor.clientJson)
    implementation(Ktor.serialization)
    implementation(Ktor.serverNetty)
    implementation(Nimbusds.joseJwt)
    testImplementation(kotlin("test-junit5"))
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
            artifactId = "azure-exchange"
            version = libraryVersion
            from(components["java"])

            val sourcesJar by tasks.creating(Jar::class) {
                archiveClassifier.set("sources")
                from(sourceSets.main.get().allSource)
            }

            artifact(sourcesJar)
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
