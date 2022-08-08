package no.nav.tms.token.support.idporten.sidecar

import com.auth0.jwt.interfaces.DecodedJWT
import io.kotest.extensions.system.withEnvironment
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import no.nav.tms.token.support.idporten.sidecar.authentication.TokenVerifier
import no.nav.tms.token.support.idporten.sidecar.authentication.TokenVerifierBuilder
import no.nav.tms.token.support.idporten.sidecar.authentication.config.HttpClientBuilder
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
        every { TokenVerifierBuilder.buildTokenVerifier(any(), any(), any()) } returns verifier
        every { HttpClientBuilder.buildHttpClient(any()) } returns mockedClient
    }

    @AfterEach
    fun cleanUp() {
        clearMocks(verifier)
        unmockkObject(HttpClientBuilder)
        unmockkObject(TokenVerifierBuilder)
    }

    @Test
    fun `Should respond unauthorized when no valid token header provided`() = testApplication {

        application {
            testApi()
        }

        val status = client.get("/test")
            .status

        status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Should respond unauthorized when no valid token headr provided and authenticator is default`() = testApplication {

        application {
            testApiWithDefault()
        }

        val status = client.get("/test").status

        status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Should return ok if token is valid`() = testApplication {

        application {
            testApiWithDefault()
        }

        every { verifier.verifyAccessToken(dummyToken) } returns dummyJwt

        val status = client.get("/test"){
            headers.append(HttpHeaders.Authorization, "Bearer $dummyToken")
        }.status

        status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Should return unauthorized if token is invalid`() = testApplication {

        application {
            testApiWithDefault()
        }

        every { verifier.verifyAccessToken(dummyToken) } throws RuntimeException()

        val status = client.get("/test"){
            headers.append(HttpHeaders.Authorization, "Bearer $dummyToken")
        }.status

        status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Should ignore fallback cookie when not enabled`() = testApplication {

        application {
            testApiWithDefault(fallbackEnabled = false)
        }

        every { verifier.verifyAccessToken(dummyToken) } returns dummyJwt

        val status = client.get("/test") {
            headers.append(HttpHeaders.Cookie, "$fallbackCookieName=$dummyToken")
        }.status

        status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Should use fallback cookie when enabled`() = testApplication {

        application {
            testApiWithDefault(fallbackEnabled = true)
        }

        every { verifier.verifyAccessToken(dummyToken) } returns dummyJwt

        val status = client.get("/test") {
            headers.append(HttpHeaders.Cookie, "$fallbackCookieName=$dummyToken")
        }.status

        status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Requesting logout should redirect to logout url`() = testApplication {

        application {
            testApiWithDefault()
        }

        val clientOverride = createClient {
            followRedirects = false
        }

        val response = clientOverride.get("/logout")

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
