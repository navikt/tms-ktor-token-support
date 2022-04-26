import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    implementation(project(":token-support-tokenx-validation"))
    implementation(Logback.classic)
    implementation(Ktor.auth)
    implementation(Ktor.authJwt)
    implementation(Ktor.clientJson)
    implementation(Nimbusds.joseJwt)
    testImplementation(kotlin("test-junit5"))
    testImplementation(Junit.engine)
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
