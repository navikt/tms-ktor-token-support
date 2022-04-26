package no.nav.tms.token.support.azure.validation.mock

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import no.nav.tms.token.support.azure.validation.AzureAuthenticator
import no.nav.tms.token.support.azure.validation.AzurePrincipal
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class AzureAuthTest {

    private val jwtOverride = JwtBuilder.generateJwt()
    private val jwtOverrideString = jwtOverride.token

    @Test
    fun `Should respond with status 401 if alwaysAuthenticated is false`() = withTestApplication<Unit>({
        testApi {
            installAzureAuthMock {
                alwaysAuthenticated = false
            }
        }
    }) {

        val response = handleRequest(HttpMethod.Get, "/test").response

        response.status() `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Should respond ok if alwaysAuthenticated is true and principal info is defined`() = withTestApplication<Unit>({
        testApi {
            installAzureAuthMock {
                alwaysAuthenticated = true
                staticJwtOverride = jwtOverrideString
            }
        }
    }) {

        val response = handleRequest(HttpMethod.Get, "/test").response

        response.status() `should be equal to` HttpStatusCode.OK
        response.content `should be equal to` jwtOverrideString
    }

    @Test
    fun `Should provide stub jwt if override was not specified`() = withTestApplication<Unit>({
        testApi {
            installAzureAuthMock {
                alwaysAuthenticated = true
                staticJwtOverride = null
            }
        }
    }) {

        val response = handleRequest(HttpMethod.Get, "/test").response

        response.status() `should be equal to` HttpStatusCode.OK
        response.content?.isNotBlank() `should be equal to` true
    }

    @Test
    fun `Should enable setting authenticator as default`() = withTestApplication<Unit>({
        testApiWithDefault {
            installAzureAuthMock {
                setAsDefault = true
                alwaysAuthenticated = false
            }
        }
    }) {

        val response = handleRequest(HttpMethod.Get, "/test").response

        response.status() `should be equal to` HttpStatusCode.Unauthorized
    }

    private fun Application.testApi(authConfig: Application.() -> Unit) {

        authConfig()

        routing {
            authenticate(AzureAuthenticator.name) {
                get("/test") {
                    val principal = call.principal<AzurePrincipal>()
                    call.respondText(principal!!.decodedJWT.token)
                }
            }
        }
    }

    private fun Application.testApiWithDefault(authConfig: Application.() -> Unit) {

        authConfig()

        routing {
            authenticate {
                get("/test") {
                    val principal = call.principal<AzurePrincipal>()
                    call.respondText(principal!!.decodedJWT.token)
                }
            }
        }
    }
}
