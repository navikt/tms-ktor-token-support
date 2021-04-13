package no.nav.tms.token.support.idporten

import io.kotest.extensions.system.withEnvironment
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.netty.handler.codec.http.HttpResponseStatus
import no.nav.tms.token.support.idporten.authentication.config.HttpClientBuilder
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IdportenAuthIT {

    val envVars = listOf(
            "IDPORTEN_WELL_KNOWN_URL" to "http://mocked-issuer/config",
            "IDPORTEN_CLIENT_ID" to "123456",
            "IDPORTEN_CLIENT_JWK" to JwkBuilder.generateJwk(),
            "IDPORTEN_REDIRECT_URI" to "/oath2/callback"
    ).toMap()

    @BeforeEach
    fun setupMock() {
        mockkObject(HttpClientBuilder)
        every { HttpClientBuilder.buildHttpClient() } returns mockedClient
    }

    @AfterEach
    fun cleanUp() {
        unmockkObject(HttpClientBuilder)
    }

    @Test
    fun `Should send redirect to local login on call to cookie-authenticated endpoint when no valid cookie provided`()
            = withTestApplication<Unit>({ testApi() }) {

        val redirect = handleRequest(HttpMethod.Get, "/test")
                .response.headers["Location"]

        redirect `should be equal to` "/oauth2/login"
    }

    @Test
    fun `Function installIdPortenAuth should enable oauth-authenticated endpoint 'login' which may redirect to idporten`()
            = withTestApplication<Unit>({ testApi() }) {

        val response = handleRequest(HttpMethod.Get, "/oauth2/login").response

        response.headers["Location"]!! `should contain`  idportenMetadata.authorizationEndpoint
    }

    @Test
    fun `Function installIdPortenAuth should enable oauth-authenticated endpoint 'callback' which may redirect to idporten`()
            = withTestApplication<Unit>({ testApi() }) {

        val response = handleRequest(HttpMethod.Get, "/oauth2/callback").response

        response.headers["Location"]!! `should contain`  idportenMetadata.authorizationEndpoint
    }

    @Test
    fun `Should send redirect to idporten on calling callback endpoint without credentials`()
            = withTestApplication<Unit>({ testApi() }) {

        val redirect = handleRequest(HttpMethod.Get, "/oauth2/callback")
                .response.headers["Location"]!!

        redirect `should contain` idportenMetadata.authorizationEndpoint
    }

    @Test
    fun `Can set authenticator as default and expect same result`()
            = withTestApplication<Unit>({ testApiWithDefault() }) {

        val redirect = handleRequest(HttpMethod.Get, "/test")
                .response.headers["Location"]

        redirect `should be equal to` "/oauth2/login"
    }

    @Test
    fun `Requesting single logout with no valid token present should clear token and redirect to post logout url`()
            = withTestApplication<Unit>({ testApiWithDefault() }) {

        val response = handleRequest(HttpMethod.Get, "/logout").response

        response.cookies["my_token"]!!.value `should be equal to` ""
        response.headers["Location"]!! `should be equal to` "http://dummy.io"
    }

    @Test
    fun `Handling single logout initiated elsewhere should clear token and respond ok`()
            = withTestApplication<Unit>({ testApiWithDefault() }) {

        val response = handleRequest(HttpMethod.Get, "/oauth2/logout").response

        response.cookies["my_token"]!!.value `should be equal to` ""
        response.status() `should be equal to` HttpStatusCode.OK
    }

    private fun Application.testApi() = withEnvironment(envVars) {

        installIdPortenAuth {
            tokenCookieName = "my_token"
            postLogoutRedirectUri = "http://dummy.io"
        }

        routing {
            authenticate(IdPortenCookieAuthenticator.name) {
                get("/test") {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }

    private fun Application.testApiWithDefault() = withEnvironment(envVars) {

        installIdPortenAuth {
            tokenCookieName = "my_token"
            setAsDefault = true
            postLogoutRedirectUri = "http://dummy.io"
            secureCookie = false
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
