package no.nav.tms.token.support.user.token.verification.mock

import com.auth0.jwt.JWT
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldThrow
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
import no.nav.tms.token.support.user.token.verification.Issuer
import no.nav.tms.token.support.user.token.verification.LevelOfAssurance
import no.nav.tms.token.support.user.token.verification.UserPrincipal
import no.nav.tms.token.support.user.token.verificaton.mock.IdPortenMockIssuer
import no.nav.tms.token.support.user.token.verificaton.mock.TokenxMockIssuer
import no.nav.tms.token.support.user.token.verificaton.mock.mockAuthorizedHeader
import no.nav.tms.token.support.user.token.verificaton.mock.mockUnauthorizedHeader
import no.nav.tms.token.support.user.token.verificaton.mock.userTokenMock
import org.junit.jupiter.api.Test

internal class UserTokenMockTest {

    private val userPid = "01234567890"

    @Test
    fun `svarer med 401 dersom ingen auth-info er gitt`() = testApplication {

        application {
            testApi {
                authentication {
                    userTokenMock {
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
                    userTokenMock {
                        enableDefaultAuthentication {
                            tokenIdent = userPid
                            tokenLoa = LevelOfAssurance.High
                            tokenIssuer = Issuer.IdPorten
                        }
                    }
                }
            }
        }

        val response = client.get("/test")

        response.status shouldBe HttpStatusCode.OK
        response.checkBody { body ->
            body["ident"].asText() shouldBe userPid
            body["loa"].asText() shouldBe "high"
            body["issuer"].asText() shouldBe IdPortenMockIssuer
        }
    }

    @Test
    fun `tillater å sende auth-info som mock i auth-header`() = testApplication {

        application {
            testApi {
                authentication {
                    userTokenMock {
                    }
                }
            }
        }

        val response = client.get("/test") {
            mockAuthorizedHeader(
                ident = userPid,
                levelOfAssurance = LevelOfAssurance.High,
                issuer = Issuer.IdPorten
            )
        }

        response.status shouldBe HttpStatusCode.OK
        response.checkBody { body ->
            body["ident"].asText() shouldBe userPid
            body["loa"].asText() shouldBe "high"
            body["issuer"].asText() shouldBe IdPortenMockIssuer
        }
    }

    @Test
    fun `tillater å overstyre default auth-info med mock i auth-header`() = testApplication {

        application {
            testApi {
                authentication {
                    userTokenMock {
                        levelOfAssurance = LevelOfAssurance.Substantial

                        enableDefaultAuthentication {
                            tokenIdent = userPid
                            tokenIssuer = Issuer.IdPorten
                        }
                    }
                }
            }
        }

        val identOverride = "99999999999"

        val response = client.get("/test") {
            mockAuthorizedHeader(
                ident = identOverride,
                levelOfAssurance = LevelOfAssurance.High,
                issuer = Issuer.Tokenx
            )
        }

        response.status shouldBe HttpStatusCode.OK
        response.checkBody { body ->
            body["ident"].asText() shouldBe identOverride
            body["loa"].asText() shouldBe "high"
            body["issuer"].asText() shouldBe TokenxMockIssuer
        }
    }

    @Test
    fun `validerer issuer når en sender auth-info via auth-header`() = testApplication {

        application {
            testApi {
                authentication {
                    userTokenMock {
                        levelOfAssurance = LevelOfAssurance.High
                        configureIssuers(Issuer.IdPorten)
                    }
                }
            }
        }

        val response = client.get("/test") {
            mockAuthorizedHeader(
                ident = userPid,
                levelOfAssurance = LevelOfAssurance.High,
                issuer = Issuer.Tokenx
            )
        }

        response.status shouldBe HttpStatusCode.Unauthorized
    }

    @Test
    fun `validerer level of assurance når en sender auth-info via auth-header`() = testApplication {

        application {
            testApi {
                authentication {
                    userTokenMock {
                        levelOfAssurance = LevelOfAssurance.High
                        configureIssuers(Issuer.IdPorten)
                    }
                }
            }
        }

        val response = client.get("/test") {
            mockAuthorizedHeader(
                ident = userPid,
                levelOfAssurance = LevelOfAssurance.Substantial,
                issuer = Issuer.IdPorten
            )
        }

        response.status shouldBe HttpStatusCode.Unauthorized
    }

    @Test
    fun `tillater å overstyre default auth-info med UnauthorizedMock i auth-header`() = testApplication {

        application {
            testApi {
                authentication {
                    userTokenMock {
                        enableDefaultAuthentication {
                            tokenIdent = userPid
                            tokenLoa = LevelOfAssurance.High
                            tokenIssuer = Issuer.IdPorten
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
                userTokenMock {
                    configureIssuers(Issuer.IdPorten)
                    levelOfAssurance = LevelOfAssurance.Substantial

                    enableDefaultAuthentication {
                        tokenIdent = userPid
                    }
                }

                userTokenMock("idporten_high") {
                    configureIssuers(Issuer.IdPorten)
                    levelOfAssurance = LevelOfAssurance.High

                    enableDefaultAuthentication {
                        tokenIdent = userPid
                    }
                }
            }

            routing {
                authenticate {
                    get("/test") {
                        call.respond(HttpStatusCode.OK)
                    }
                }
                authenticate("idporten_high") {
                    get("/test/extra/secure") {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }

        client.get("/test").checkBody { json ->
            json["loa"].asText() shouldBe "substantial"
        }

        client.get("/test/extra/secure").checkBody { json ->
            json["loa"].asText() shouldBe "high"
        }
    }

    @Test
    fun `krever at issuer i default auth er en del av konfigurerte issuers dersom det er spesifisert`() = testApplication {

        application {
            authentication {
                shouldThrow<Exception> {
                    userTokenMock {
                        configureIssuers(Issuer.Tokenx)

                        enableDefaultAuthentication {
                            tokenIdent = userPid
                            tokenIssuer = Issuer.IdPorten
                        }
                    }
                }
            }

            routing {
                get("/test") {
                    val principal = call.principal<UserPrincipal>()!!

                    call.respond(UserAuthInfo.fromPrincipal(principal))
                }

            }
        }

        client.get("/test")
    }

    @Test
    fun `krever at loa i default auth er høyere enn påkrevd nivå`() = testApplication {

        application {
            authentication {
                shouldThrow<Exception> {
                    userTokenMock {
                        levelOfAssurance = LevelOfAssurance.High

                        enableDefaultAuthentication {
                            tokenIdent = userPid
                            tokenIssuer = Issuer.IdPorten
                            tokenLoa = LevelOfAssurance.Substantial
                        }
                    }
                }
            }

            routing {
                get("/test") {
                    val principal = call.principal<UserPrincipal>()!!

                    call.respond(UserAuthInfo.fromPrincipal(principal))
                }

            }
        }

        client.get("/test")
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
                    val principal = call.principal<UserPrincipal>()!!

                    call.respond(UserAuthInfo.fromPrincipal(principal))
                }
            }
        }
    }

    private data class UserAuthInfo(
        val issuer: String,
        val ident: String,
        val loa: String
    ) {
        companion object {
            fun fromPrincipal(userPrincipal: UserPrincipal) = UserAuthInfo(
                issuer = issuer(userPrincipal.accessToken),
                ident = userPrincipal.ident,
                loa = userPrincipal.levelOfAssurance.name.lowercase()
            )

            private fun issuer(token: String) = JWT.decode(token).issuer
        }
    }
}
