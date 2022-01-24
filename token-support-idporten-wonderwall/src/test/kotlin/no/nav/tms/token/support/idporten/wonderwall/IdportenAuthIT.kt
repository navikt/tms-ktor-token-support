package no.nav.tms.token.support.idporten.wonderwall

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
import no.nav.tms.token.support.idporten.wonderwall.IdPortenCookieAuthenticator
import no.nav.tms.token.support.idporten.wonderwall.installIdPortenAuth
import no.nav.tms.token.support.idporten.wonderwall.authentication.config.HttpClientBuilder
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IdportenAuthIT {

    val envVars = listOf(
            "IDPORTEN_WELL_KNOWN_URL" to "http://mocked-issuer/config",
            "IDPORTEN_CLIENT_ID" to "123456",
    ).toMap()

    @BeforeEach
    fun setupMock() {
        mockkObject(HttpClientBuilder)
        every { HttpClientBuilder.buildHttpClient(any()) } returns mockedClient
    }

    @AfterEach
    fun cleanUp() {
        unmockkObject(HttpClientBuilder)
    }

    @Test
    fun `Should respond unauthorized when no valid cookie provided and config is set to default`()
            = withTestApplication<Unit>({ testApi() }) {

        val status = handleRequest(HttpMethod.Get, "/test")
                .response.status()

        status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Can set authenticator as default and expect same result`()
            = withTestApplication<Unit>({ testApiWithDefault() }) {

        val status = handleRequest(HttpMethod.Get, "/test")
            .response.status()

        status `should be equal to` HttpStatusCode.Unauthorized
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

    private fun Application.testApiWithDefault() = withEnvironment(envVars) {

        installIdPortenAuth {
            setAsDefault = true
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
