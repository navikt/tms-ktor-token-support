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
    override val version = "3.1.8"
    override val groupId = "com.github.ben-manes.caffeine"

    val caffeine = dependency("caffeine")
}

object JacksonDatatype: DependencyGroup {
    override val version get() = "2.18.1"

    val datatypeJsr310 get() = dependency("jackson-datatype-jsr310", groupId = "com.fasterxml.jackson.datatype")
    val moduleKotlin get() = dependency("jackson-module-kotlin", groupId = "com.fasterxml.jackson.module")
}

object Kotlin {
     const val version = "2.0.21"
}

object Kotest : DependencyGroup {
    override val groupId = "io.kotest"
    override val version = "5.9.1"

    val runnerJunit = dependency("kotest-runner-junit5")
    val assertionsCore = dependency("kotest-assertions-core")
    val extensions = dependency("kotest-extensions")
}

object Ktor : DependencyGroup {
    override val version = "3.0.1"
    override val groupId = "io.ktor"

    val serverAuth = dependency("ktor-server-auth")
    val serverAuthJwt = dependency("ktor-server-auth-jwt")
    val serialization = dependency("ktor-serialization")
    val jackson = dependency("ktor-serialization-jackson")
    val serverNetty = dependency("ktor-server-netty")
    val clientApache = dependency("ktor-client-apache")
    val clientJson = dependency("ktor-client-json")
    val clientMock = dependency("ktor-client-mock")
    val serverTestHost = dependency("ktor-server-test-host")
    val clientContentNegotiation = dependency("ktor-client-content-negotiation")
    val serverForwardedHeaders = dependency("ktor-server-forwarded-header")
    val serverAuthJvm = dependency("ktor-server-auth-jvm")
    val serverCoreJvm = dependency("ktor-server-core-jvm")
    val serverAuthLdapJvm = dependency("ktor-server-auth-ldap-jvm")
}

object KotlinLogging : DependencyGroup {
    override val groupId = "io.github.oshai"
    override val version = "7.0.0"

    val logging = dependency("kotlin-logging")
}


object Logback : DependencyGroup {
    override val version = "1.5.12"
    override val groupId = "ch.qos.logback"

    val classic = dependency("logback-classic")
}

object Mockk : DependencyGroup {
    override val version = "1.13.13"
    override val groupId = "io.mockk"

    val mockk = dependency("mockk")
}

object Nimbusds : DependencyGroup {
    override val version = "9.42"
    override val groupId = "com.nimbusds"

    val joseJwt = dependency("nimbus-jose-jwt")
    val oauth2OidcSdk = dependency("oauth2-oidc-sdk")
}
