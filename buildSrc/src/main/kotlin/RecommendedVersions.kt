object Auth0 {
    private const val version = "3.11.0"
    const val javajwt = "com.auth0:java-jwt:$version"
}

object Bouncycastle {
    private const val version = "1.66"
    const val bcprovJdk15on = "org.bouncycastle:bcprov-jdk15on:$version"
}

object Brukernotifikasjon {
    private const val version = "ekstern-varsling-SNAPSHOT"
    const val schemas = "com.github.navikt:brukernotifikasjon-schemas:$version"
}

object Caffeine {
    private const val version = "3.0.0"
    private const val groupId = "com.github.ben-manes.caffeine"

    const val caffeine = "$groupId:caffeine:$version"
}

object Doknotifikasjon {
    private const val version = "1.2020.11.16-09.27-d037b30bb0ea"
    const val schemas = "com.github.navikt:doknotifikasjon-schemas:$version"
}

object Flyway {
    const val version = "6.5.7"
    const val pluginId = "org.flywaydb.flyway"
    const val core = "org.flywaydb:flyway-core:$version"
}

object H2Database {
    private const val version = "1.4.200"
    const val h2 = "com.h2database:h2:$version"
}

object Hikari {
    private const val version = "3.4.5"
    const val cp = "com.zaxxer:HikariCP:$version"
}

object Influxdb {
    private const val version = "2.20"
    const val java = "org.influxdb:influxdb-java:$version"
}

object Junit {
    private const val version = "5.4.1"
    private const val groupId = "org.junit.jupiter"

    const val api = "$groupId:junit-jupiter-api:$version"
    const val engine = "$groupId:junit-jupiter-engine:$version"
    const val params = "$groupId:junit-jupiter-params:$version"
}

object Jjwt {
    private const val version = "0.11.2"
    private const val groupId = "io.jsonwebtoken"

    const val api = "$groupId:jjwt-api:$version"
    const val impl = "$groupId:jjwt-impl:$version"
    const val jackson = "$groupId:jjwt-jackson:$version"
}

object Kluent {
    private const val version = "1.68"
    const val kluent = "org.amshove.kluent:kluent:$version"
}

object Kotlin {
    const val version = "1.6.20"
    private const val groupId = "org.jetbrains.kotlin"

    const val reflect = "$groupId:kotlin-reflect:$version"
}

object Kotest {
    private const val groupId = "io.kotest"
    private const val version = "4.3.1"

    const val runnerJunit = "$groupId:kotest-runner-junit5:$version"
    const val assertionsCore = "$groupId:kotest-assertions-core:$version"
    const val extensions = "$groupId:kotest-extensions:$version"
}

object Ktor {
    private const val version = "1.5.2"
    private const val groupId = "io.ktor"

    const val auth = "$groupId:ktor-auth:$version"
    const val authJwt = "$groupId:ktor-auth-jwt:$version"
    const val htmlBuilder = "$groupId:ktor-html-builder:$version"
    const val serialization = "$groupId:ktor-serialization:$version"
    const val serverNetty = "$groupId:ktor-server-netty:$version"
    const val clientApache = "$groupId:ktor-client-apache:$version"
    const val clientJson = "$groupId:ktor-client-json:$version"
    const val clientSerializationJvm = "$groupId:ktor-client-serialization-jvm:$version"
    const val clientJackson = "$groupId:ktor-client-jackson:$version"
    const val clientLogging = "$groupId:ktor-client-logging:$version"
    const val clientLoggingJvm = "$groupId:ktor-client-logging-jvm:$version"
    const val clientMock = "$groupId:ktor-client-mock:$version"
    const val clientMockJvm = "$groupId:ktor-client-mock-jvm:$version"
    const val clientCIO = "$groupId:ktor-client-cio:$version"
    const val serverTestHost = "$groupId:ktor-server-test-host:$version"
}

object Logback {
    private const val version = "1.2.3"
    const val classic = "ch.qos.logback:logback-classic:$version"
}

object Mockk {
    private const val version = "1.10.0"
    const val mockk = "io.mockk:mockk:$version"
}

object Nimbusds {
    private const val version = "9.19"
    private const val groupId = "com.nimbusds"

    const val joseJwt = "$groupId:nimbus-jose-jwt:$version"
    const val oauth2OidcSdk =  "$groupId:oauth2-oidc-sdk:$version"
}
