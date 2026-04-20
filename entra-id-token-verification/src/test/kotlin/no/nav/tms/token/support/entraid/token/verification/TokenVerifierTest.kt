package no.nav.tms.token.support.entraid.token.verification

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.nimbusds.jose.jwk.RSAKey
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tms.token.support.entraid.token.verification.install.AccessFilter
import no.nav.tms.token.support.entraid.token.verification.install.TokenVerifier
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.Date
import java.util.UUID

class TokenVerifierTest {

    private val mockIssuer = "https://azure"
    private val mockAudience = UUID.randomUUID().toString()
    private val mockJwk = JwkBuilder.generateJwk()
    private val mockJwkProvider: JwkProvider = mockk()

    private val mockNavIdent = "A000001"
    private val mockClient = NaisApplication("cluster", "namespace", "client-app")
    private val mockLocalServer = NaisApplication("cluster", "namespace", "server-app")

    private val tokenBuilder = EntraIdTokenBuilder(
        defaultIssuer = mockIssuer,
        azureJwk = mockJwk,
        defaultAudience = mockAudience,
        defaultIssuedFor = mockLocalServer
    )

    private val now = Date()

    @AfterEach
    fun cleanUp() {
        clearMocks(mockJwkProvider)
    }

    @Test
    fun `godtar gyldig system-token`() {

        val testIssuer = "http://valid-issuer"
        val testAudience = UUID.randomUUID().toString()

        val verifier = tokenVerifier(
            issuer = testIssuer,
            audience = testAudience,
        )
        val clientName = NaisApplication("cluster", "namespace", "client-app")

        val token = tokenBuilder.azureSystemToken(
            clientName,
            issuer = testIssuer,
            audience = testAudience
        )

        every { mockJwkProvider.get(any()) } returns mockJwk.toJwk()

        shouldNotThrow<Exception> {
            verifier.verify(token)
        }
    }

    @Test
    fun `godtar ikke token med ukjent issuer`() {

        val validIssuer = "http://valid-issuer"

        val verifier = tokenVerifier(
            issuer = validIssuer,
        )

        val systemToken = tokenBuilder.azureSystemToken(
            mockClient,
            issuer = "http://invalid-issuer"
        )

        val userToken = tokenBuilder.azureUserToken(
            mockNavIdent,
            issuer = "http://invalid-issuer"
        )

        every { mockJwkProvider.get(any()) } returns mockJwk.toJwk()

        shouldThrow<Exception> {
            verifier.verify(systemToken)
        }

        shouldThrow<Exception> {
            verifier.verify(userToken)
        }
    }

    @Test
    fun `kan vurdere alle bruker-tokens som ugyldige`() {

        val accessFilter = AccessFilter(
            allowUserAccess = false,
            cluster = null,
            namespace = null,
            app = null
        )

        val verifier = tokenVerifier(
            accessFilter = accessFilter
        )

        val userToken = tokenBuilder.azureUserToken(
            mockNavIdent
        )

        every { mockJwkProvider.get(any()) } returns mockJwk.toJwk()

        shouldThrow<TokenVerifier.EntraIdAccessException> {
            verifier.verify(userToken)
        }.let { cause ->
            cause.description shouldBe "Token ble utstedt på vegne av bruker, men endepunktet krever system-token"
        }
    }

    @Test
    fun `kan vurdere om token er gyldig basert på app det tilhører`() {

        val allowedCluster = "allowedCluster"
        val allowedNamespace = "allowedNamespace"
        val allowedApp = "allowedApp"

        val filter = AccessFilter(
            allowUserAccess = true,
            cluster = allowedCluster,
            namespace = allowedNamespace,
            app = allowedApp
        )

        val verifier = tokenVerifier(
            accessFilter = filter,
        )

        every { mockJwkProvider.get(any()) } returns mockJwk.toJwk()

        val allowedToken = tokenBuilder.azureSystemToken(
            NaisApplication(allowedCluster, allowedNamespace, allowedApp)
        )

        val invalidClusterToken = tokenBuilder.azureSystemToken(
            NaisApplication("invalid", allowedNamespace, allowedApp)
        )

        val invalidNamespaceToken = tokenBuilder.azureSystemToken(
            NaisApplication(allowedCluster, "invalid", allowedApp)
        )

        val invalidAppToken = tokenBuilder.azureSystemToken(
            NaisApplication(allowedCluster, allowedNamespace, "invalid")
        )

        shouldNotThrow<Exception> {
            verifier.verify(allowedToken)
        }

        shouldThrow<TokenVerifier.EntraIdAccessException> {
            verifier.verify(invalidClusterToken)
        }.let { cause ->
            cause.description shouldBe "Token ble utstedt til 'invalid:$allowedNamespace:$allowedApp', men påkrevd cluster er '$allowedCluster'"
        }

        shouldThrow<TokenVerifier.EntraIdAccessException> {
            verifier.verify(invalidNamespaceToken)
        }.let { cause ->
            cause.description shouldBe "Token ble utstedt til '$allowedCluster:invalid:$allowedApp', men påkrevd namespace er '$allowedNamespace'"
        }

        shouldThrow<TokenVerifier.EntraIdAccessException> {
            verifier.verify(invalidAppToken)
        }.let { cause ->
            cause.description shouldBe "Token ble utstedt til '$allowedCluster:$allowedNamespace:invalid', men påkrevd applikasjon er '$allowedApp'"
        }
    }

    @Test
    fun `godtar ikke token med feil audience`() {

        val riktigAudience = "riktig_audience"
        val feilAudience = "feil_audience"

        val verifier = tokenVerifier(
            audience = riktigAudience,
        )

        val systemToken = tokenBuilder.azureSystemToken(
            mockClient,
            audience = feilAudience,
        )

        val userToken = tokenBuilder.azureUserToken(
            mockNavIdent,
            audience = feilAudience
        )

        every { mockJwkProvider.get(any()) } returns mockJwk.toJwk()

        shouldThrow<Exception> {
            verifier.verify(systemToken)
        }

        shouldThrow<Exception> {
            verifier.verify(userToken)
        }
    }

    @Test
    fun `godtar ikke tokens hvor expiryTime er passert`() {

        val verifier = tokenVerifier()

        val systemToken = tokenBuilder.azureSystemToken(
            mockClient,
            issueTime = now - Duration.ofHours(2),
            expiryTime = now - Duration.ofHours(1),
        )

        val userToken = tokenBuilder.azureUserToken(
            mockNavIdent,
            issueTime = now - Duration.ofHours(2),
            expiryTime = now - Duration.ofHours(1),
        )

        every { mockJwkProvider.get(any()) } returns mockJwk.toJwk()

        shouldThrow<Exception> {
            verifier.verify(systemToken)
        }

        shouldThrow<Exception> {
            verifier.verify(userToken)
        }
    }

    private fun tokenVerifier(
        issuer: String = mockIssuer,
        jwkProvider: JwkProvider = mockJwkProvider,
        audience: String = mockAudience,
        accessFilter: AccessFilter = AccessFilter(true, null, null, null)
    ) = TokenVerifier(
        issuer = issuer,
        jwkProvider = jwkProvider,
        audience = audience,
        accessFilter = accessFilter
    )

    private fun RSAKey.toJwk() = toJSONObject()
        .run { toMap() }
        .let { Jwk.fromValues(it) }
}
