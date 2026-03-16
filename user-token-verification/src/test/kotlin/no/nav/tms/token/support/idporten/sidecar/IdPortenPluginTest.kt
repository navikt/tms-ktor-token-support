package no.nav.tms.token.support.idporten.sidecar

import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.utils.io.*
import io.mockk.*
import no.nav.tms.token.support.verification.IdPortenLogin
import no.nav.tms.token.support.user.token.verification.UserTokenVerificationEnvironment
import no.nav.tms.token.support.user.token.verification.HttpClientBuilder
import no.nav.tms.token.support.user.token.verification.idporten.IdPortenLevelOfAssurance
import no.nav.tms.token.support.user.token.verification.idporten.IdPortenTokenVerifier
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IdPortenPluginTest {
    private val envVars = listOf(
        "IDPORTEN_WELL_KNOWN_URL" to "http://mocked-issuer/config",
        "IDPORTEN_CLIENT_ID" to "123456",
    ).toMap()

    private val verifier: IdPortenTokenVerifier = mockk()
    private val dummyJwt: DecodedJWT = mockk()

    private val dummyToken = "token"

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setupMock() {
        mockkObject(IdPortenTokenVerifier)
        mockkObject(HttpClientBuilder)
        every { IdPortenTokenVerifier.build(any(), any(), any()) } returns verifier
        every { HttpClientBuilder.buildHttpClient(any()) } returns mockedClient
    }

    @AfterEach
    fun cleanUp() {
        UserTokenVerificationEnvironment.reset()
        clearMocks(verifier)
        unmockkObject(HttpClientBuilder)
        unmockkObject(IdPortenTokenVerifier)
    }

    @Test
    fun `Enables login endpoint which redirects to callback`() = loginApiTest { client ->

        client.get("/login").let {
            it.status shouldBe HttpStatusCode.Found
            it.headers["location"] shouldBe "/oauth2/login?redirect=/login/callback"
        }
    }

    @Test
    fun `Status endpoint returns login status when unauthorized`() = loginApiTest { client ->

        client.get("/login/status") {
            accept(ContentType.Application.Json)
        }.let { response ->
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText()
                .let(objectMapper::readTree)
                .let {
                    it["authenticated"]?.asBoolean() shouldBe false
                    it["level"].isNull shouldBe true
                    it["levelOfAssurance"].isNull shouldBe true
                }
        }
    }

    @Test
    fun `Status endpoint returns login status when authorized`() = loginApiTest { client ->

        every { verifier.verify(dummyToken) } returns dummyJwt

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
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText()
                .let(objectMapper::readTree)
                .let {
                    it["authenticated"]?.asBoolean() shouldBe true
                    it["levelOfAssurance"]?.asText() shouldBe IdPortenLevelOfAssurance.High.name
                }
        }
    }

    @KtorDsl
    private fun loginApiTest(block: suspend TestApplicationBuilder.(HttpClient) -> Unit) = testApplication {
        UserTokenVerificationEnvironment.extend(envVars)

        application {
            install(IdPortenLogin)
        }

        val client = createClient {
            followRedirects = false
        }

        block(client)
    }
}
