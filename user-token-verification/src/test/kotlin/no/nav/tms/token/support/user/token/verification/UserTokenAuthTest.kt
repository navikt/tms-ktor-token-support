package no.nav.tms.token.support.user.token.verification

import com.auth0.jwk.Jwk
import com.auth0.jwk.SigningKeyNotFoundException
import com.nimbusds.jose.jwk.RSAKey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

internal class UserTokenAuthenticationFlowTest {

    private val testIdent = "01234567890"

    private val idPortenUrl = "http://idporten"
    private val idPortenAud = UUID.randomUUID().toString()
    private val idPortenJwk = JwkBuilder.generateJwk()

    private val tokenxUrl = "http://tokenx"
    private val tokenxClientId = "test:app"
    private val tokenxJwk = JwkBuilder.generateJwk()

    private val userTokenBuilder = UserTokenBuilder(
        idPortenUrl = idPortenUrl,
        idPortenJwk = idPortenJwk,
        idPortenAud = idPortenAud,
        tokenxUrl = tokenxUrl,
        tokenxJwk = tokenxJwk
    )

    private val idPortenEnv = listOf(
        "IDPORTEN_WELL_KNOWN_URL" to "$idPortenUrl/config",
        "IDPORTEN_AUDIENCE" to idPortenAud
    ).toMap()

    private val tokenxEnv = listOf(
        "TOKEN_X_WELL_KNOWN_URL" to "$tokenxUrl/config",
        "TOKEN_X_CLIENT_ID" to tokenxClientId,
    ).toMap()

    private val securedEndpoint = "/test"

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
    }

    @Nested
    inner class IdPortenTests {

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
                        get(securedEndpoint) {
                            call.respondText(call.principal<UserPrincipal>()?.ident ?: "null")
                        }
                    }
                }
            }

            val token = userTokenBuilder.idportenToken(testIdent)

            val response = client.get(securedEndpoint) {
                headers[HttpHeaders.Authorization] = "Bearer $token"
            }

            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText() shouldBe testIdent
        }

        @Test
        fun `feiler hvis idporten er påkrevd som issuer og env ikke finnes`() = testApplication {
            UserTokenVerificationEnvironment.extend(tokenxEnv)

            shouldThrow<Exception> {
                application {
                    authentication {
                        userToken {
                            requireIssuer(Issuer.IdPorten)
                        }
                    }

                    routing {
                        authenticate {
                            get(securedEndpoint) {
                                call.respondText(call.principal<UserPrincipal>()?.ident ?: "null")
                            }
                        }
                    }
                }
                client.get(securedEndpoint)
            }
        }
    }

//    @Test
//    fun `Should respond unauthorized when no valid token header provided`() = testApplication {
//
//        every { HttpClientBuilder.buildHttpClient(any()) } returns client
//
//        idpo
//
//        application {
//            testApi()
//        }
//
//        val status = client.get("/test")
//            .status
//
//        status shouldBe HttpStatusCode.Unauthorized
//    }

//    @Test
//    fun `Should respond unauthorized when no valid token headr provided and authenticator is default`() = testApplication {
//
//        application {
//            testApiWithDefault()
//        }
//
//        val status = client.get("/test").status
//
//        status shouldBe HttpStatusCode.Unauthorized
//    }
//
//    @Test
//    fun `Should return ok if token is valid`() = testApplication {
//
//        application {
//            testApiWithDefault()
//        }
//
//        every { verifier.verify(dummyToken) } returns dummyJwt
//
//        val status = client.get("/test"){
//            headers.append(HttpHeaders.Authorization, "Bearer $dummyToken")
//        }.status
//
//        status shouldBe HttpStatusCode.OK
//    }
//
//    @Test
//    fun `Should return unauthorized if token is invalid`() = testApplication {
//
//        application {
//            testApiWithDefault()
//        }
//
//        every { verifier.verify(dummyToken) } throws RuntimeException()
//
//        val status = client.get("/test"){
//            headers.append(HttpHeaders.Authorization, "Bearer $dummyToken")
//        }.status
//
//        status shouldBe HttpStatusCode.Unauthorized
//    }
//
//    @Test
//    fun `Allows installing multiple authorizers in parallel`() = testApplication {
//
//        UserTokenVerificationEnvironment.extend(idPortenEnv)
//
//        application {
//            authentication {
//                userToken {
//                    setAsDefault = true
//                    levelOfAssurance = LevelOfAssurance.HIGH
//                }
//                userToken {
//                    setAsDefault = false
//                    authenticatorName = "other"
//                }
//            }
//            routing {
//                authenticate {
//                    get("/test/one") {
//                        call.respond(HttpStatusCode.OK)
//                    }
//                }
//                authenticate("other") {
//                    get("test/two") {
//                        call.respond(HttpStatusCode.OK)
//                    }
//                }
//            }
//        }
//
//        every { verifier.verify(dummyToken) } returns dummyJwt
//
//        client.get("/test/one") {
//            headers.append(HttpHeaders.Authorization, "Bearer $dummyToken")
//        }.status shouldBe HttpStatusCode.OK
//
//        client.get("/test/two") {
//            headers.append(HttpHeaders.Authorization, "Bearer $dummyToken")
//        }.status shouldBe HttpStatusCode.OK
//    }

    internal fun createMockedJwkProvider(publicJwk: RSAKey) = { kid: String ->
        if (publicJwk.keyID == kid) {
            publicJwk.toJSONObject()
                .run { toMap() }
                .let { Jwk.fromValues(it) }
        } else {
            throw SigningKeyNotFoundException("", Exception())
        }
    }

    private fun Application.testApiWithDefault() {

        UserTokenVerificationEnvironment.extend(idPortenEnv)

        authentication {
            userToken {
                setAsDefault = true
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
