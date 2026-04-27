interface DependencyGroup {
    val groupId: String? get() = null
    val version: String? get() = null

    fun dependency(name: String, groupId: String? = this.groupId, version: String? = this.version): String {
        requireNotNull(groupId)
        requireNotNull(version)

        return "$groupId:$name:$version"
    }
}

object Caffeine : DependencyGroup {
    override val version = "3.2.3"
    override val groupId = "com.github.ben-manes.caffeine"

    val caffeine = dependency("caffeine")
}

object JacksonDatatype: DependencyGroup {
    override val version get() = "2.21.2"

    val datatypeJsr310 get() = dependency("jackson-datatype-jsr310", groupId = "com.fasterxml.jackson.datatype")
    val moduleKotlin get() = dependency("jackson-module-kotlin", groupId = "com.fasterxml.jackson.module")
}

object JunitJupiter: DependencyGroup {
    override val groupId get() = "org.junit.jupiter"
    override val version get() = "6.0.3"

    val api get() = dependency("junit-jupiter-api")
    val engine get() = dependency("junit-jupiter-engine")
}

object JunitPlatform: DependencyGroup {
    override val groupId get() = "org.junit.platform"
    override val version get() = "6.0.3"

    val launcher get() = dependency("junit-platform-launcher")
}

object Kotest: DependencyGroup {
    override val groupId get() = "io.kotest"
    override val version get() = "6.1.11"

    val assertionsCore get() = dependency("kotest-assertions-core")
    val extensions get() = dependency("kotest-extensions")
}

object Kotlin: DependencyGroup {
    override val groupId get() = "org.jetbrains.kotlin"
    override val version get() = "2.3.20"
}

object KotlinLogging: DependencyGroup {
    override val groupId get() = "io.github.oshai"
    override val version get() = "8.0.01"

    val logging get() = dependency("kotlin-logging")
}

object Ktor : DependencyGroup {
    override val version = "3.4.2"
    override val groupId = "io.ktor"

    val clientApache5 get() = dependency("ktor-client-apache5")
    val clientContentNegotiation = dependency("ktor-client-content-negotiation")
    val clientJson = dependency("ktor-client-json")
    val clientMock = dependency("ktor-client-mock")
    val jackson = dependency("ktor-serialization-jackson")
    val serverAuth = dependency("ktor-server-auth")
    val serverAuthJwt = dependency("ktor-server-auth-jwt")
    val serialization = dependency("ktor-serialization")
    val serverContentNegotiation get() = dependency("ktor-server-content-negotiation")
    val serverNetty = dependency("ktor-server-netty")
    val serverTestHost = dependency("ktor-server-test-host")
    val serverForwardedHeaders = dependency("ktor-server-forwarded-header")
}

object Logback: DependencyGroup {
    override val version = "1.5.32"
    val classic = "ch.qos.logback:logback-classic:$version"
}

object Logstash: DependencyGroup {
    override val groupId get() = "net.logstash.logback"
    override val version get() = "9.0"

    val logbackEncoder get() = dependency("logstash-logback-encoder")
}

object Mockk: DependencyGroup {
    override val groupId get() = "io.mockk"
    override val version get() = "1.14.9"

    val mockk get() = dependency("mockk")
}

object Nimbusds : DependencyGroup {
    override val version = "10.9"
    override val groupId = "com.nimbusds"

    val joseJwt = dependency("nimbus-jose-jwt")
    val oauth2OidcSdk = dependency("oauth2-oidc-sdk")
}
