object Caffeine {
    private const val version = "3.0.0"
    private const val groupId = "com.github.ben-manes.caffeine"

    const val caffeine = "$groupId:caffeine:$version"
}
object Kluent {
    private const val version = "1.68"
    const val kluent = "org.amshove.kluent:kluent:$version"
}

object Kotlin {
    const val version = "1.9.0"
}

object Kotest {
    private const val groupId = "io.kotest"
    private const val version = "4.3.1"

    const val runnerJunit = "$groupId:kotest-runner-junit5:$version"
    const val assertionsCore = "$groupId:kotest-assertions-core:$version"
    const val extensions = "$groupId:kotest-extensions:$version"
}

object Ktor {
    private const val version = "2.3.7"
    private const val groupId = "io.ktor"

    const val serverAuth = "$groupId:ktor-server-auth:$version"
    const val serverAuthJwt = "$groupId:ktor-server-auth-jwt:$version"
    const val serialization = "$groupId:ktor-serialization:$version"
    const val serializationKotlinxJson = "$groupId:ktor-serialization-kotlinx-json:$version"
    const val serverNetty = "$groupId:ktor-server-netty:$version"
    const val clientApache = "$groupId:ktor-client-apache:$version"
    const val clientJson = "$groupId:ktor-client-json:$version"
    const val clientMock = "$groupId:ktor-client-mock:$version"
    const val serverTestHost = "$groupId:ktor-server-test-host:$version"
    const val clientContentNegotiation = "$groupId:ktor-client-content-negotiation:$version"
    const val serverForwardedHeaders = "$groupId:ktor-server-forwarded-header:$version"
}

object KotlinLogging {
    private const val groupId = "io.github.oshai"
    private const val version = "6.0.3"

    const val logging = "$groupId:kotlin-logging:$version"
}


object Logback {
    private const val version = "1.4.14"
    const val classic = "ch.qos.logback:logback-classic:$version"
}

object Mockk {
    private const val version = "1.12.3"
    const val mockk = "io.mockk:mockk:$version"
}

object Nimbusds {
    private const val version = "9.37.3"
    private const val groupId = "com.nimbusds"

    const val joseJwt = "$groupId:nimbus-jose-jwt:$version"
    const val oauth2OidcSdk =  "$groupId:oauth2-oidc-sdk:$version"
}
