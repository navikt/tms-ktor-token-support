package no.nav.tms.token.support.azure.validation.mock

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.tms.token.support.azure.validation.AzureAuthenticator
import no.nav.tms.token.support.azure.validation.AzurePrincipal
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class AzureAuthTest {

    private val jwtOverride = JwtBuilder.generateJwt()
    private val jwtOverrideString = jwtOverride.token

    @Test
    fun `Should respond with status 401 if alwaysAuthenticated is false`() = testApplication {

        application {
            testApi {
                authentication {
                    azureMock {
                        alwaysAuthenticated = false
                        staticJwtOverride = jwtOverrideString
                    }
                }
            }
        }

        val response = client.get("/test")

        response.status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Should respond ok if alwaysAuthenticated is true and principal info is defined`() = testApplication {

        application {
            testApi {
                authentication {
                    azureMock {
                        alwaysAuthenticated = true
                        staticJwtOverride = jwtOverrideString
                    }
                }
            }
        }

        val response = client.get("/test")

        response.status `should be equal to` HttpStatusCode.OK
        response.body<String>() `should be equal to` jwtOverrideString
    }

    @Test
    fun `Should provide stub jwt if override was not specified`() = testApplication {

        application {
            testApi {
                authentication {
                    azureMock {
                        alwaysAuthenticated = true
                        staticJwtOverride = jwtOverrideString
                    }
                }
            }
        }

        val response = client.get("/test")

        response.status `should be equal to` HttpStatusCode.OK
        response.body<String>().isNotBlank() `should be equal to` true
    }

    @Test
    fun `Should enable setting authenticator as default`() = testApplication {

        application {
            testApiWithDefault {
                authentication {
                    azureMock {
                        setAsDefault = true
                        alwaysAuthenticated = false
                        staticJwtOverride = jwtOverrideString
                    }
                }
            }
        }

        val response = client.get("/test")

        response.status `should be equal to` HttpStatusCode.Unauthorized
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
