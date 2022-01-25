package no.nav.tms.token.support.idporten.wonderwall

import com.auth0.jwt.interfaces.DecodedJWT
import io.kotest.extensions.system.withEnvironment
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import no.nav.tms.token.support.idporten.wonderwall.authentication.TokenVerifier
import no.nav.tms.token.support.idporten.wonderwall.authentication.TokenVerifierBuilder
import no.nav.tms.token.support.idporten.wonderwall.authentication.config.HttpClientBuilder
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IdportenAuthIT {

    private val envVars = listOf(
            "IDPORTEN_WELL_KNOWN_URL" to "http://mocked-issuer/config",
            "IDPORTEN_CLIENT_ID" to "123456",
    ).toMap()

    val verifier: TokenVerifier = mockk()
    val dummyJwt: DecodedJWT = mockk()

    private val dummyToken = "token"

    private val fallbackCookieName = "fallback"

    @BeforeEach
    fun setupMock() {
        mockkObject(TokenVerifierBuilder)
        mockkObject(HttpClientBuilder)
        every { TokenVerifierBuilder.buildTokenVerifier(any(), any(), any(), any()) } returns verifier
        every { HttpClientBuilder.buildHttpClient(any()) } returns mockedClient
    }

    @AfterEach
    fun cleanUp() {
        clearMocks(verifier)
        unmockkObject(HttpClientBuilder)
        unmockkObject(TokenVerifierBuilder)
    }

    @Test
    fun `Should respond unauthorized when no valid token header provided`()
            = withTestApplication<Unit>({ testApi() }) {

        val status = handleRequest(HttpMethod.Get, "/test")
                .response.status()

        status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Should respond unauthorized when no valid token headr provided and authenticator is default`()
            = withTestApplication<Unit>({ testApiWithDefault() }) {

        val status = handleRequest(HttpMethod.Get, "/test")
            .response.status()

        status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Should return ok if token is valid`()
            = withTestApplication<Unit>({ testApiWithDefault() }) {

        every { verifier.verifyAccessToken(dummyToken) } returns dummyJwt

        val status = handleRequest(HttpMethod.Get, "/test"){
            addHeader(HttpHeaders.Authorization, "Bearer $dummyToken")
        }.response.status()

        status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Should return unauthorized if token is invalid`()
            = withTestApplication<Unit>({ testApiWithDefault() }) {

        every { verifier.verifyAccessToken(dummyToken) } throws RuntimeException()

        val status = handleRequest(HttpMethod.Get, "/test"){
            addHeader(HttpHeaders.Authorization, "Bearer $dummyToken")
        }.response.status()

        status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Should ignore fallback cookie when not enabled`()
            = withTestApplication<Unit>({ testApiWithDefault(fallbackEnabled = false) }) {

        every { verifier.verifyAccessToken(dummyToken) } returns dummyJwt

        val status = handleRequest(HttpMethod.Get, "/test") {
            addHeader(HttpHeaders.Cookie, "$fallbackCookieName=$dummyToken")
        }.response.status()

        status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Should use fallback cookie when enabled`()
            = withTestApplication<Unit>({ testApiWithDefault(fallbackEnabled = true) }) {

        every { verifier.verifyAccessToken(dummyToken) } returns dummyJwt

        val status = handleRequest(HttpMethod.Get, "/test") {
            addHeader(HttpHeaders.Cookie, "$fallbackCookieName=$dummyToken")
        }.response.status()

        status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Requesting logout should redirect to logout url`()
            = withTestApplication<Unit>({ testApiWithDefault() }) {

        val response = handleRequest(HttpMethod.Get, "/logout").response

        response.headers["Location"]!! `should be equal to` "/oauth2/logout"
    }

    private fun Application.testApi() = withEnvironment(envVars) {

        installIdPortenAuth {
        }

        routing {
            authenticate(IdPortenCookieAuthenticator.name) {
                get("/test") {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }

    private fun Application.testApiWithDefault(fallbackEnabled: Boolean = false) = withEnvironment(envVars) {

        installIdPortenAuth {
            setAsDefault = true

            if (fallbackEnabled) {
                fallbackCookieEnabled = true
                fallbackTokenCookieName = fallbackCookieName
            }
        }

        routing {
            authenticate {
                get("/test") {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}
