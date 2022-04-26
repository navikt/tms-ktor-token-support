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
    implementation(Kotlin.reflect)
    implementation(project(":token-support-azure-validation"))
    implementation(project(":token-support-tokenx-validation"))
    implementation(project(":token-support-idporten-sidecar"))
    implementation(Ktor.auth)
    testImplementation(kotlin("test-junit5"))
    testImplementation(Jjwt.api)
    testImplementation(Jjwt.impl)
    testImplementation(Jjwt.jackson)
    testImplementation(Junit.engine)
    testImplementation(Kluent.kluent)
    testImplementation(Ktor.serverTestHost)
    testImplementation(Mockk.mockk)
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
