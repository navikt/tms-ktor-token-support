package no.nav.tms.token.support.idporten.sidecar.mock

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.tms.token.support.idporten.sidecar.IdPortenCookieAuthenticator
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUserFactory
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class IdPortenAuthTest {

    private val userPid = "12345"

    @Test
    fun `Should respond with status 401 if alwaysAuthenticated is false`() = testApplication {

        application {
            testApi {
                installIdPortenAuthMock {
                    alwaysAuthenticated = false
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
                installIdPortenAuthMock {
                    alwaysAuthenticated = true
                    staticUserPid = userPid
                    staticLevelOfAssurance = LevelOfAssurance.HIGH
                }
            }
        }

        val response = client.get("/test")

        response.status `should be equal to` HttpStatusCode.OK
        response.body<String>() `should be equal to` userPid
    }

    @Test
    fun `Should enable setting authenticator as default`() = testApplication {

        application {
            testApiWithDefault {
                installIdPortenAuthMock {
                    setAsDefault = true
                    alwaysAuthenticated = false
                }
            }
        }

        val response = client.get("/test")

        response.status `should be equal to` HttpStatusCode.Unauthorized
    }

    private fun Application.testApi(authConfig: Application.() -> Unit) {

        authConfig()

        routing {
            authenticate(IdPortenCookieAuthenticator.name) {
                get("/test") {
                    val user = IdportenUserFactory.createIdportenUser(call)
                    call.respondText(user.ident)
                }
            }
        }
    }

    private fun Application.testApiWithDefault(authConfig: Application.() -> Unit) {

        authConfig()

        routing {
            authenticate {
                get("/test") {
                    val user = IdportenUserFactory.createIdportenUser(call)
                    call.respondText(user.ident)
                }
            }
        }
    }
}
