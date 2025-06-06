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
    override val version = "3.2.0"
    override val groupId = "com.github.ben-manes.caffeine"

    val caffeine = dependency("caffeine")
}

object JacksonDatatype: DependencyGroup {
    override val version get() = "2.19.0"

    val datatypeJsr310 get() = dependency("jackson-datatype-jsr310", groupId = "com.fasterxml.jackson.datatype")
    val moduleKotlin get() = dependency("jackson-module-kotlin", groupId = "com.fasterxml.jackson.module")
}

object JunitJupiter: DependencyGroup {
    override val groupId get() = "org.junit.jupiter"
    override val version get() = "5.13.0"

    val api get() = dependency("junit-jupiter-api")
    val params get() = dependency("junit-jupiter-params")
}

object JunitPlatform: DependencyGroup {
    override val groupId get() = "org.junit.platform"
    override val version get() = "1.13.0"

    val launcher get() = dependency("junit-platform-launcher")
}

object Kotest: DependencyGroup {
    override val groupId get() = "io.kotest"
    override val version get() = "5.9.1"

    val assertionsCore get() = dependency("kotest-assertions-core")
    val extensions get() = dependency("kotest-extensions")
}

object Kotlin: DependencyGroup {
    override val groupId get() = "org.jetbrains.kotlin"
    override val version get() = "2.1.21"
}

object KotlinLogging: DependencyGroup {
    override val groupId get() = "io.github.oshai"
    override val version get() = "7.0.7"

    val logging get() = dependency("kotlin-logging")
}

object Ktor : DependencyGroup {
    override val version = "3.1.3"
    override val groupId = "io.ktor"

    val clientApache = dependency("ktor-client-apache")
    val clientContentNegotiation = dependency("ktor-client-content-negotiation")
    val clientJson = dependency("ktor-client-json")
    val clientMock = dependency("ktor-client-mock")
    val jackson = dependency("ktor-serialization-jackson")
    val serverAuth = dependency("ktor-server-auth")
    val serverAuthJwt = dependency("ktor-server-auth-jwt")
    val serialization = dependency("ktor-serialization")
    val serverNetty = dependency("ktor-server-netty")
    val serverTestHost = dependency("ktor-server-test-host")
    val serverForwardedHeaders = dependency("ktor-server-forwarded-header")
    val serverAuthJvm = dependency("ktor-server-auth-jvm")
    val serverCoreJvm = dependency("ktor-server-core-jvm")
    val serverAuthLdapJvm = dependency("ktor-server-auth-ldap-jvm")
}

object Logstash: DependencyGroup {
    override val groupId get() = "net.logstash.logback"
    override val version get() = "8.1"

    val logbackEncoder get() = dependency("logstash-logback-encoder")
}

object Mockk: DependencyGroup {
    override val groupId get() = "io.mockk"
    override val version get() = "1.14.2"

    val mockk get() = dependency("mockk")
}


object Nimbusds : DependencyGroup {
    override val version = "10.3"
    override val groupId = "com.nimbusds"

    val joseJwt = dependency("nimbus-jose-jwt")
    val oauth2OidcSdk = dependency("oauth2-oidc-sdk")
}
