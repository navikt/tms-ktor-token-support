package no.nav.tms.token.support.user.token.verification

import com.auth0.jwk.Jwk
import com.auth0.jwk.SigningKeyNotFoundException
import com.auth0.jwt.JWT
import com.nimbusds.jose.jwk.RSAKey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import no.nav.tms.token.support.user.token.verification.UserTokenBuilder.IdPortenLoa
import no.nav.tms.token.support.user.token.verification.UserTokenBuilder.TokenxLoa
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

internal class UserTokenAuthTest {

    private val testIdent = "01234567890"

    private val idPortenUrl = "http://idporten"
    private val idPortenNavAudId = UUID.randomUUID().toString()
    private val idPortenJwk = JwkBuilder.generateJwk()

    private val tokenxUrl = "http://tokenx"
    private val tokenxClientId = "test:app"
    private val tokenxJwk = JwkBuilder.generateJwk()

    private val userTokenBuilder = UserTokenBuilder(
        idPortenUrl = idPortenUrl,
        idPortenJwk = idPortenJwk,
        idPortenAud = idPortenNavAudId,
        tokenxUrl = tokenxUrl,
        tokenxJwk = tokenxJwk,
        tokenxClientId = tokenxClientId
    )

    private val idPortenEnv = listOf(
        "IDPORTEN_WELL_KNOWN_URL" to "$idPortenUrl/config",
        "IDPORTEN_AUDIENCE" to idPortenNavAudId
    ).toMap()

    private val tokenxEnv = listOf(
        "TOKEN_X_WELL_KNOWN_URL" to "$tokenxUrl/config",
        "TOKEN_X_CLIENT_ID" to tokenxClientId,
    ).toMap()

    private val testClient = testClient(
        idPortenUrl = idPortenUrl,
        tokenxUrl = tokenxUrl,
    )

    @BeforeEach
    fun setupMock() {
        mockkObject(HttpClientBuilder)
        mockkObject(JwkProviderBuilderWrapper)
        every { HttpClientBuilder.buildHttpClient(any()) } returns testClient
        every { JwkProviderBuilderWrapper.createJwkProvider("$idPortenUrl/jwks") } returns createMockedJwkProvider(idPortenJwk)
        every { JwkProviderBuilderWrapper.createJwkProvider("$tokenxUrl/jwks") } returns createMockedJwkProvider(tokenxJwk)
    }

    @AfterEach
    fun cleanUp() {
        unmockkObject(HttpClientBuilder)
        unmockkObject(JwkProviderBuilderWrapper)
        UserTokenVerificationEnvironment.reset()
    }

    @Nested
    inner class IdPortenTests {

        @Test
        fun `tillater autentisering av endepunkt med tokens fra idporten`() = testApplication {
            UserTokenVerificationEnvironment.extend(idPortenEnv)

            application {
                authentication {
                    userToken {
                        configureIssuers(Issuer.IdPorten)
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

            val unauthorizedResponse =  client.get("/test")

            val token = userTokenBuilder.idPortenToken(testIdent)

            val authorizedResponse = client.authorizedGet("/test", token)

            unauthorizedResponse.status shouldBe HttpStatusCode.Unauthorized
            authorizedResponse.status shouldBe HttpStatusCode.OK
        }

        @Test
        fun `legger til principal i call-context med info fra brukers token`() = testApplication {
            UserTokenVerificationEnvironment.extend(idPortenEnv)

            var userPrincipal: UserPrincipal? = null

            application {
                authentication {
                    userToken {
                        configureIssuers(Issuer.IdPorten)

                        levelOfAssurance = LevelOfAssurance.Substantial
                    }
                }

                routing {
                    authenticate {
                        get("/test") {
                            userPrincipal = call.principal<UserPrincipal>()

                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
            }

            val token = userTokenBuilder.idPortenToken(
                testIdent,
                acrClaim = IdPortenLoa.Substantial.acrValue
            )

            val response = client.authorizedGet("/test", token)

            response.status shouldBe HttpStatusCode.OK

            userPrincipal.shouldNotBeNull().let {
                it.ident shouldBe testIdent
                it.levelOfAssurance shouldBe LevelOfAssurance.Substantial
            }
        }

        @Test
        fun `konfigurerer idporten som issuer automatisk hvis env finnes`() = testApplication {
            UserTokenVerificationEnvironment.extend(idPortenEnv)

            application {
                authentication {
                    userToken {

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

            val token = userTokenBuilder.idPortenToken(testIdent)

            val response = client.authorizedGet("/test", token)

            response.status shouldBe HttpStatusCode.OK
        }

        @Test
        fun `feiler ved oppstart hvis idporten er valgt som issuer og env ikke finnes`() = testApplication {
            shouldThrow<Exception> {
                application {
                    authentication {
                        userToken {
                            configureIssuers(Issuer.IdPorten)
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
                client.get("/test")
            }
        }

        @Test
        fun `feiler ved oppstart idporten ikke kan nåes på well known url`() = testApplication {
            val feilaktigEnv = listOf(
                "IDPORTEN_WELL_KNOWN_URL" to "http://feil/config",
                "IDPORTEN_AUDIENCE" to idPortenNavAudId
            ).toMap()

            UserTokenVerificationEnvironment.extend(feilaktigEnv)

            shouldThrow<Exception> {
                application {
                    authentication {
                        userToken {
                            configureIssuers(Issuer.IdPorten)
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
                client.get("/test")
            }
        }

        @Test
        fun `setter godtatt audience til navs audience-id fra env`() = testApplication {
            val audience = UUID.randomUUID().toString()

            val environment = mapOf(
                "IDPORTEN_AUDIENCE" to audience
            )

            UserTokenVerificationEnvironment.extend(idPortenEnv)
            UserTokenVerificationEnvironment.extend(environment)

            application {
                authentication {
                    userToken {

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

            val tokenForNav = userTokenBuilder.idPortenToken(
                testIdent,
                audience = audience
            )

            val tokenForAnnetDomene = userTokenBuilder.idPortenToken(
                testIdent,
                audience = UUID.randomUUID().toString()
            )

            client.authorizedGet("/test", tokenForNav).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test", tokenForAnnetDomene).status shouldBe HttpStatusCode.Unauthorized
        }

        @Test
        fun `godtar kun kjente acr-verdier fra idporten og mapper om til LevelOfAssurance`() = testApplication {
            UserTokenVerificationEnvironment.extend(idPortenEnv)

            application {
                authentication {
                    userToken("substantial") {
                        levelOfAssurance = LevelOfAssurance.Substantial
                    }
                    userToken("high") {
                        levelOfAssurance = LevelOfAssurance.High
                    }
                }

                routing {
                    authenticate("substantial") {
                        get("/test/substantial") {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                    authenticate("high") {
                        get("/test/high") {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
            }

            val lowToken = userTokenBuilder.idPortenToken(
                testIdent,
                acrClaim = "idporten-loa-low"
            )

            val substantialToken = userTokenBuilder.idPortenToken(
                testIdent,
                acrClaim = "idporten-loa-substantial"
            )

            val highToken = userTokenBuilder.idPortenToken(
                testIdent,
                acrClaim = "idporten-loa-high"
            )

            val ukjentAcrToken = userTokenBuilder.idPortenToken(
                testIdent,
                acrClaim = "ukjent"
            )

            client.authorizedGet("/test/substantial", lowToken).status shouldBe HttpStatusCode.Unauthorized
            client.authorizedGet("/test/high", lowToken).status shouldBe HttpStatusCode.Unauthorized

            client.authorizedGet("/test/substantial", substantialToken).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/high", substantialToken).status shouldBe HttpStatusCode.Unauthorized

            client.authorizedGet("/test/substantial", highToken).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/high", highToken).status shouldBe HttpStatusCode.OK

            client.authorizedGet("/test/substantial", ukjentAcrToken).status shouldBe HttpStatusCode.Unauthorized
            client.authorizedGet("/test/high", ukjentAcrToken).status shouldBe HttpStatusCode.Unauthorized
        }
    }

    @Nested
    inner class TokenxTests {

        @Test
        fun `tillater autentisering av endepunkt med tokens fra tokenx`() = testApplication {
            UserTokenVerificationEnvironment.extend(tokenxEnv)

            application {
                authentication {
                    userToken {
                        configureIssuers(Issuer.Tokenx)
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

            val unauthorizedResponse =  client.get("/test")

            val token = userTokenBuilder.tokenxToken(testIdent, tokenxClientId)

            val authorizedResponse = client.authorizedGet("/test", token)

            unauthorizedResponse.status shouldBe HttpStatusCode.Unauthorized
            authorizedResponse.status shouldBe HttpStatusCode.OK
        }

        @Test
        fun `legger til principal i call-context med info fra brukers token`() = testApplication {
            UserTokenVerificationEnvironment.extend(tokenxEnv)

            var userPrincipal: UserPrincipal? = null

            application {
                authentication {
                    userToken {
                        configureIssuers(Issuer.Tokenx)

                        levelOfAssurance = LevelOfAssurance.Substantial
                    }
                }

                routing {
                    authenticate {
                        get("/test") {
                            userPrincipal = call.principal<UserPrincipal>()

                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
            }

            val token = userTokenBuilder.tokenxToken(
                testIdent,
                acrClaim = TokenxLoa.Substantial.acrValue
            )

            val response = client.authorizedGet("/test", token)

            response.status shouldBe HttpStatusCode.OK

            userPrincipal.shouldNotBeNull().let {
                it.ident shouldBe testIdent
                it.levelOfAssurance shouldBe LevelOfAssurance.Substantial
            }
        }

        @Test
        fun `konfigurerer tokenx som issuer automatisk hvis env finnes`() = testApplication {
            UserTokenVerificationEnvironment.extend(tokenxEnv)

            application {
                authentication {
                    userToken {

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

            val token = userTokenBuilder.tokenxToken(testIdent)

            val response = client.authorizedGet("/test", token)

            response.status shouldBe HttpStatusCode.OK
        }

        @Test
        fun `feiler ved oppstart hvis tokenx er valgt som issuer og env ikke finnes`() = testApplication {
            shouldThrow<Exception> {
                application {
                    authentication {
                        userToken {
                            configureIssuers(Issuer.Tokenx)
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
                client.get("/test")
            }
        }

        @Test
        fun `feiler ved oppstart tokenx ikke kan nåes på well known url`() = testApplication {
            val feilaktigEnv = listOf(
                "TOKENX_WELL_KNOWN_URL" to "http://feil/config",
                "TOKENX_AUDIENCE" to tokenxClientId
            ).toMap()

            UserTokenVerificationEnvironment.extend(feilaktigEnv)

            shouldThrow<Exception> {
                application {
                    authentication {
                        userToken {
                            configureIssuers(Issuer.Tokenx)
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
                client.get("/test")
            }
        }

        @Test
        fun `setter godtatt audience til applikasjonens clientId fra env`() = testApplication {
            val clientId = "denne:appens:clientid"

            val environment = mapOf(
                "TOKEN_X_CLIENT_ID" to clientId
            )

            UserTokenVerificationEnvironment.extend(tokenxEnv)
            UserTokenVerificationEnvironment.extend(environment)

            application {
                authentication {
                    userToken {

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

            val tokenForDenneAppen = userTokenBuilder.tokenxToken(
                testIdent,
                target = clientId
            )

            val tokenForAnnenApp = userTokenBuilder.tokenxToken(
                testIdent,
                target = "annen:app"
            )

            client.authorizedGet("/test", tokenForDenneAppen).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test", tokenForAnnenApp).status shouldBe HttpStatusCode.Unauthorized
        }

        @Test
        fun `godtar kun kjente acr-verdier fra tokenx og mapper om til LevelOfAssurance`() = testApplication {
            UserTokenVerificationEnvironment.extend(tokenxEnv)

            application {
                authentication {
                    userToken("substantial") {
                        levelOfAssurance = LevelOfAssurance.Substantial
                    }
                    userToken("high") {
                        levelOfAssurance = LevelOfAssurance.High
                    }
                }

                routing {
                    authenticate("substantial") {
                        get("/test/substantial") {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                    authenticate("high") {
                        get("/test/high") {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
            }

            val level3Token = userTokenBuilder.tokenxToken(
                testIdent,
                acrClaim = "Level3"
            )

            val level4Token = userTokenBuilder.tokenxToken(
                testIdent,
                acrClaim = "Level4"
            )

            val ukjentAcrToken = userTokenBuilder.tokenxToken(
                testIdent,
                acrClaim = "ukjent"
            )

            client.authorizedGet("/test/substantial", level3Token).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/high", level3Token).status shouldBe HttpStatusCode.Unauthorized

            client.authorizedGet("/test/substantial", level4Token).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/high", level4Token).status shouldBe HttpStatusCode.OK

            client.authorizedGet("/test/substantial", ukjentAcrToken).status shouldBe HttpStatusCode.Unauthorized
            client.authorizedGet("/test/high", ukjentAcrToken).status shouldBe HttpStatusCode.Unauthorized
        }
    }

    @Nested
    inner class UserTokenAuthenticatorTests {

        @Test
        fun `tillater autentisering av endepunkt med tokens fra både idporten og tokenx`() = testApplication {
            UserTokenVerificationEnvironment.extend(idPortenEnv)
            UserTokenVerificationEnvironment.extend(tokenxEnv)

            application {
                authentication {
                    userToken {
                        configureIssuers(Issuer.IdPorten, Issuer.Tokenx)
                    }
                }

                routing {
                    authenticate {
                        get("/test") {
                            val issuer = call.principal<UserPrincipal>()
                                ?.accessToken
                                ?.let(JWT::decode)
                                ?.issuer

                            call.respondText(issuer ?: "null")
                        }
                    }
                }
            }

            val unauthorizedResponse =  client.get("/test")
            val tokenxResponse = client.authorizedGet("/test", userTokenBuilder.tokenxToken(testIdent))
            val idPortenResponse = client.authorizedGet("/test", userTokenBuilder.idPortenToken(testIdent))

            unauthorizedResponse.status shouldBe HttpStatusCode.Unauthorized

            tokenxResponse.status shouldBe HttpStatusCode.OK
            tokenxResponse.bodyAsText() shouldBe tokenxUrl

            idPortenResponse.status shouldBe HttpStatusCode.OK
            idPortenResponse.bodyAsText() shouldBe idPortenUrl
        }

        @Test
        fun `konfigurer både idPorten og tokenx automatisk hvis env finnes`() = testApplication {
            UserTokenVerificationEnvironment.extend(idPortenEnv)
            UserTokenVerificationEnvironment.extend(tokenxEnv)

            application {
                authentication {
                    userToken {

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

            val tokenxResponse = client.authorizedGet("/test", userTokenBuilder.tokenxToken(testIdent))
            val idPortenResponse = client.authorizedGet("/test", userTokenBuilder.idPortenToken(testIdent))

            tokenxResponse.status shouldBe HttpStatusCode.OK
            idPortenResponse.status shouldBe HttpStatusCode.OK
        }

        @Test
        fun `tillater å konfigurere ulike endepunkt for forskjellige issuers`() = testApplication {
            UserTokenVerificationEnvironment.extend(idPortenEnv)
            UserTokenVerificationEnvironment.extend(tokenxEnv)

            application {
                authentication {
                    userToken {

                    }

                    userToken("bare_tokenx") {
                        configureIssuers(Issuer.Tokenx)
                    }

                    userToken("bare_idporten") {
                        configureIssuers(Issuer.IdPorten)
                    }
                }

                routing {
                    authenticate {
                        get("/test/begge") {
                            call.respond(HttpStatusCode.OK)
                        }
                    }

                    authenticate("bare_tokenx") {
                        get("/test/tokenx") {
                            call.respond(HttpStatusCode.OK)
                        }
                    }

                    authenticate("bare_idporten") {
                        get("/test/idporten") {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
            }

            client.get("/test/begge").status shouldBe HttpStatusCode.Unauthorized
            client.get("/test/tokenx").status shouldBe HttpStatusCode.Unauthorized
            client.get("/test/idporten").status shouldBe HttpStatusCode.Unauthorized

            val idportenToken = userTokenBuilder.idPortenToken(testIdent)

            client.authorizedGet("/test/begge", idportenToken).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/tokenx", idportenToken).status shouldBe HttpStatusCode.Unauthorized
            client.authorizedGet("/test/idporten", idportenToken).status shouldBe HttpStatusCode.OK

            val tokenxToken = userTokenBuilder.tokenxToken(testIdent)

            client.authorizedGet("/test/begge", tokenxToken).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/tokenx", tokenxToken).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/idporten", tokenxToken).status shouldBe HttpStatusCode.Unauthorized
        }

        @Test
        fun `tillater å konfigurere ulike endepunkt med ulik level of assurance`() = testApplication {
            UserTokenVerificationEnvironment.extend(idPortenEnv)
            UserTokenVerificationEnvironment.extend(tokenxEnv)

            application {
                authentication {
                    userToken {
                        levelOfAssurance = LevelOfAssurance.Substantial
                    }

                    userToken("loa_high") {
                        levelOfAssurance = LevelOfAssurance.High
                    }
                }

                routing {
                    get("/test/open") {
                        call.respond(HttpStatusCode.OK)
                    }

                    authenticate {
                        get("/test/substantial") {
                            call.respond(HttpStatusCode.OK)
                        }
                    }

                    authenticate("loa_high") {
                        get("/test/high") {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
            }

            val tokenxTokenSubstantial = userTokenBuilder.tokenxToken(testIdent, acrClaim = TokenxLoa.Substantial.acrValue)
            val tokenxTokenHigh = userTokenBuilder.tokenxToken(testIdent, acrClaim = TokenxLoa.High.acrValue)
            val idPortenTokenSubstantial = userTokenBuilder.idPortenToken(testIdent, acrClaim = IdPortenLoa.Substantial.acrValue)
            val idPortenTokenHigh = userTokenBuilder.idPortenToken(testIdent, acrClaim = IdPortenLoa.High.acrValue)

            client.get("/test/open").status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/open", idPortenTokenSubstantial).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/open", idPortenTokenHigh).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/open", tokenxTokenSubstantial).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/open", tokenxTokenHigh).status shouldBe HttpStatusCode.OK

            client.get("/test/substantial").status shouldBe HttpStatusCode.Unauthorized
            client.authorizedGet("/test/substantial", idPortenTokenSubstantial).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/substantial", idPortenTokenHigh).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/substantial", tokenxTokenSubstantial).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/substantial", tokenxTokenHigh).status shouldBe HttpStatusCode.OK

            client.get("/test/high").status shouldBe HttpStatusCode.Unauthorized
            client.authorizedGet("/test/high", idPortenTokenSubstantial).status shouldBe HttpStatusCode.Unauthorized
            client.authorizedGet("/test/high", idPortenTokenHigh).status shouldBe HttpStatusCode.OK
            client.authorizedGet("/test/high", tokenxTokenSubstantial).status shouldBe HttpStatusCode.Unauthorized
            client.authorizedGet("/test/high", tokenxTokenHigh).status shouldBe HttpStatusCode.OK
        }

        @Test
        fun `feiler ved oppstart hvis ingen nødcendige env-variabler finnes`() = testApplication {
            shouldThrow<Exception> {
                application {
                    authentication {
                        userToken {

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
                client.get("/test")
            }
        }
    }

    internal fun createMockedJwkProvider(publicJwk: RSAKey) = { kid: String ->
        if (publicJwk.keyID == kid) {
            publicJwk.toJSONObject()
                .run { toMap() }
                .let { Jwk.fromValues(it) }
        } else {
            throw SigningKeyNotFoundException("", Exception())
        }
    }

    private suspend fun HttpClient.authorizedGet(path: String, token: String): HttpResponse {
        return get(path) {
            headers[HttpHeaders.Authorization] = "Bearer $token"
        }
    }

}
