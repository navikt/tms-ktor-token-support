package no.nav.tms.token.support.idporten.sidecar

import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import io.kotest.extensions.system.withEnvironment
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.*
import kotlinx.serialization.json.*
import no.nav.tms.token.support.idporten.sidecar.install.HttpClientBuilder
import no.nav.tms.token.support.idporten.sidecar.install.IdPortenLevelOfAssurance
import no.nav.tms.token.support.idporten.sidecar.install.TokenVerifier
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IdPortenPluginTest {
    private val envVars = listOf(
        "IDPORTEN_WELL_KNOWN_URL" to "http://mocked-issuer/config",
        "IDPORTEN_CLIENT_ID" to "123456",
    ).toMap()

    private val verifier: TokenVerifier = mockk()
    private val dummyJwt: DecodedJWT = mockk()

    private val dummyToken = "token"

    private val objectMapper = Json

    @BeforeEach
    fun setupMock() {
        mockkObject(TokenVerifier)
        mockkObject(HttpClientBuilder)
        every { TokenVerifier.build(any(), any(), any()) } returns verifier
        every { HttpClientBuilder.buildHttpClient(any()) } returns mockedClient
    }

    @AfterEach
    fun cleanUp() {
        clearMocks(verifier)
        unmockkObject(HttpClientBuilder)
        unmockkObject(TokenVerifier)
    }

    @Test
    fun `Enables login endpoint which redirects to callback`() = loginApiTest { client ->

        client.get("/login").let {
            it.status `should be equal to` HttpStatusCode.Found
            it.headers["location"] `should be equal to` "/oauth2/login?redirect=/login/callback"
        }
    }

    @Test
    fun `Status endpoint returns login status when unauthorized`() = loginApiTest { client ->

        client.get("/login/status") {
            accept(ContentType.Application.Json)
        }.let { response ->
            response.status `should be equal to` HttpStatusCode.OK
            response.bodyAsText()
                .let(objectMapper::parseToJsonElement)
                .let { it as JsonObject }
                .let {
                    it["authenticated"]?.jsonPrimitive?.boolean `should be equal to` false
                    it["level"] `should be instance of` JsonNull::class
                    it["levelOfAssurance"] `should be instance of` JsonNull::class
                }
        }
    }

    @Test
    fun `Status endpoint returns login status when authorized`() = loginApiTest { client ->

        every { verifier.verifyAccessToken(dummyToken) } returns dummyJwt

        val ident = "123"

        val acrClaim: Claim = mockk()
        every { acrClaim.asString() } returns IdPortenLevelOfAssurance.High.acr

        val identClaim: Claim = mockk()
        every { identClaim.asString() } returns ident

        every { dummyJwt.getClaim("acr") } returns acrClaim
        every { dummyJwt.getClaim("pid") } returns identClaim

        client.get("/login/status") {
            bearerAuth(dummyToken)
            accept(ContentType.Application.Json)
        }.let { response ->
            response.status `should be equal to` HttpStatusCode.OK
            response.bodyAsText()
                .let(objectMapper::parseToJsonElement)
                .let { it as JsonObject }
                .let {
                    it["authenticated"]?.jsonPrimitive?.boolean `should be equal to` true
                    it["level"]?.jsonPrimitive?.int `should be equal to` 4
                    it["levelOfAssurance"]?.jsonPrimitive?.content `should be equal to` IdPortenLevelOfAssurance.High.name
                }
        }
    }

    @KtorDsl
    private fun loginApiTest(block: suspend TestApplicationBuilder.(HttpClient) -> Unit) = testApplication {
        application {
            withEnvironment(envVars) {
                install(IdPortenLogin)
            }
        }

        val client = createClient {
            followRedirects = false
        }

        block(client)
    }
}
