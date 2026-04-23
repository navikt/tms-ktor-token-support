package no.nav.tms.token.support.entraid.token.verification.mock

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.tms.token.support.entraid.token.verification.EntraIdPrincipal
import no.nav.tms.token.support.entraid.token.verification.EntraIdUserPrincipal
import no.nav.tms.token.support.entraid.token.verification.NaisApplication
import org.junit.jupiter.api.Test

internal class EntraIdTokenVerificationMockTest {

    @Test
    fun `svarer med 401 dersom ingen auth-info er gitt`() = testApplication {

        application {
            testApi {
                authentication {
                    entraIdMock {

                    }
                }
            }
        }

        val response = client.get("/test")

        response.status shouldBe HttpStatusCode.Unauthorized
    }

    @Test
    fun `bruker default auth-info dersom det er konfigurert`() = testApplication {

        application {
            testApi {
                authentication {
                    entraIdMock {
                        enableDefaultAuthentication {
                            tokenIssuedFor = NaisApplication("dev", "test", "app")
                        }
                    }
                }
            }
        }

        val response = client.get("/test")

        response.status shouldBe HttpStatusCode.OK
        response.checkBody { body ->
            body["issuedFor"].asText() shouldBe "dev:test:app"
            body["isUserToken"].asBoolean() shouldBe false
        }
    }

    @Test
    fun `simulerer autentisering med obo-token dersom userInfo er gitt`() = testApplication {

        application {
            testApi {
                authentication {
                    entraIdMock {
                        enableDefaultAuthentication {
                            tokenIssuedFor = NaisApplication("dev", "test", "app")
                            tokenUserInfo = UserInfo(
                                navIdent = "A001122",
                                userId = "111",
                                displayName = "Navn",
                                userName = "navn@nav.no"
                            )
                        }
                    }
                }
            }
        }

        val response = client.get("/test")

        response.status shouldBe HttpStatusCode.OK
        response.checkBody { body ->
            body["issuedFor"].asText() shouldBe "dev:test:app"
            body["isUserToken"].asBoolean() shouldBe true
            body["userInfo"].let { info ->
                info["navIdent"].asText() shouldBe "A001122"
                info["userId"].asText() shouldBe "111"
                info["displayName"].asText() shouldBe "Navn "
                info["userName"].asText() shouldBe "navn@nav.no "
            }
        }
    }

    @Test
    fun `tillater å sende auth-info som mock i auth-header`() = testApplication {

        application {
            testApi {
                authentication {
                    entraIdMock {

                    }
                }
            }
        }

        val response = client.get("/test") {
            mockAuthorizedHeader(
                issuedFor = NaisApplication("dev", "test", "other-app")
            )
        }

        response.status shouldBe HttpStatusCode.OK
        response.checkBody { body ->
            body["issuedFor"].asText() shouldBe "dev:test:other-app"
            body["isUserToken"].asBoolean() shouldBe false
        }
    }

    @Test
    fun `tillater å overstyre default auth-info med mock i auth-header`() = testApplication {

        application {
            testApi {
                authentication {
                    entraIdMock {
                        enableDefaultAuthentication {
                            tokenIssuedFor = NaisApplication("dev-1", "test-1", "app-1")
                        }
                    }
                }
            }
        }

        val response = client.get("/test") {
            mockAuthorizedHeader(
                issuedFor = NaisApplication("dev-2", "test-2", "app-2")
            )
        }

        response.status shouldBe HttpStatusCode.OK
        response.checkBody { body ->
            body["issuedFor"].asText() shouldBe "dev-2:test-2:app-2"
            body["isUserToken"].asBoolean() shouldBe false
        }
    }

    @Test
    fun `tillater å overstyre default auth-info med UnauthorizedMock i auth-header`() = testApplication {

        application {
            testApi {
                authentication {
                    entraIdMock {
                        enableDefaultAuthentication {
                            tokenIssuedFor = NaisApplication("dev-1", "test-1", "app-1")
                        }
                    }
                }
            }
        }

        val response = client.get("/test") {
            mockUnauthorizedHeader()
        }

        response.status shouldBe HttpStatusCode.Unauthorized
    }

    @Test
    fun `kan konfigurere ulike endepunkt med forskjellig konfigurasjon`() = testApplication {
        application {
            install(ContentNegotiation) {
                jackson {}
            }

            authentication {
                entraIdMock {
                    enableDefaultAuthentication {
                        tokenIssuedFor = NaisApplication("dev", "test", "app")
                    }
                }

                entraIdMock("other") {
                    enableDefaultAuthentication {
                        tokenIssuedFor = NaisApplication("dev", "test", "app")
                        tokenUserInfo = UserInfo(
                            navIdent = "I000000",
                            userId = "123",
                            displayName = "Navn Navnesen",
                            userName = "navn.navnesen@nav.no"
                        )
                    }
                }
            }

            routing {
                authenticate {
                    get("/test") {
                        val principal = call.principal<EntraIdPrincipal>()!!

                        call.respond(SessionAuthInfo.fromPrincipal(principal))
                    }
                }
                authenticate("other") {
                    get("/test/other") {
                        val principal = call.principal<EntraIdPrincipal>()!!

                        call.respond(SessionAuthInfo.fromPrincipal(principal))
                    }
                }
            }
        }

        client.get("/test").checkBody { body ->
            body["issuedFor"].asText() shouldBe "dev:test:app"
            body["isUserToken"].asBoolean() shouldBe false
        }

        client.get("/test/other").checkBody { body ->
            body["issuedFor"].asText() shouldBe "dev:test:app"
            body["isUserToken"].asBoolean() shouldBe true
            body["userInfo"].let { info ->
                info["navIdent"].asText() shouldBe "I000000"
                info["userId"].asText() shouldBe "123"
                info["displayName"].asText() shouldBe "Navn Navnesen "
                info["userName"].asText() shouldBe "navn.navnesen@nav.no "
            }
        }
    }

    private  fun HttpResponse.checkBody(function: (JsonNode) -> Unit) = suspend {
        jacksonObjectMapper()
            .readTree(bodyAsText())
            .let(function)
    }

    private fun Application.testApi(authConfig: Application.() -> Unit) {

        authConfig()

        install(ContentNegotiation) {
            jackson {}
        }

        routing {
            authenticate {
                get("/test") {
                    val principal = call.principal<EntraIdPrincipal>()!!

                    call.respond(SessionAuthInfo.fromPrincipal(principal))
                }
            }
        }
    }

    private data class SessionAuthInfo(
        val issuedFor: String,
        val isUserToken: Boolean,
        val userInfo: UserInfo?
    ) {
        companion object {
            fun fromPrincipal(principal: EntraIdPrincipal) = SessionAuthInfo(
                issuedFor = principal.issuedFor?.toString()!!,
                isUserToken = principal is EntraIdUserPrincipal,
                userInfo = if (principal is EntraIdUserPrincipal) {
                    UserInfo(
                        navIdent = principal.navIdent,
                        userId = principal.userId ?: "",
                        displayName = principal.displayName ?: "",
                        userName = principal.userName ?: "",
                    )
                } else {
                    null
                }
            )
        }
    }
}
