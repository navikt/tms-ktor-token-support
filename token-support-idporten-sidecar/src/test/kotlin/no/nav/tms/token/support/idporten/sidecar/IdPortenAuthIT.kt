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
import no.nav.tms.token.support.idporten.sidecar.install.TokenVerifier
import no.nav.tms.token.support.idporten.sidecar.install.HttpClientBuilder
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IdPortenAuthIT {

    private val envVars = listOf(
            "IDPORTEN_WELL_KNOWN_URL" to "http://mocked-issuer/config",
            "IDPORTEN_CLIENT_ID" to "123456",
    ).toMap()

    val verifier: TokenVerifier = mockk()
    val dummyJwt: DecodedJWT = mockk()

    private val dummyToken = "token"

    @BeforeEach
    fun setupMock() {
        mockkObject(TokenVerifier)
        mockkObject(HttpClientBuilder)
        every { TokenVerifier.build(any(), any(), any()) } returns verifier
        every { HttpClientBuilder.buildHttpClient(any()) } returns mockedClient
    }

    @AfterEach
    fun cleanUp() {
        clearMocks(verifier)
        unmockkObject(HttpClientBuilder)
        unmockkObject(TokenVerifier)
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
    fun `Allows installing multiple authorizers in parallel`() = testApplication {

        application {
            withEnvironment(envVars) {
                authentication {
                    idPorten {
                        setAsDefault = true
                        levelOfAssurance = LevelOfAssurance.HIGH
                    }
                    idPorten {
                        setAsDefault = false
                        authenticatorName = "other"
                    }
                }
            }
            routing {
                authenticate {
                    get("/test/one") {
                        call.respond(HttpStatusCode.OK)
                    }
                }
                authenticate("other") {
                    get("test/two") {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }

        every { verifier.verifyAccessToken(dummyToken) } returns dummyJwt

        client.get("/test/one") {
            headers.append(HttpHeaders.Authorization, "Bearer $dummyToken")
        }.status `should be equal to` HttpStatusCode.OK

        client.get("/test/two") {
            headers.append(HttpHeaders.Authorization, "Bearer $dummyToken")
        }.status `should be equal to` HttpStatusCode.OK
    }

    private fun Application.testApi() = withEnvironment(envVars) {

        authentication {
            idPorten { }
        }

        routing {
            authenticate(IdPortenAuthenticator.name) {
                get("/test") {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }

    private fun Application.testApiWithDefault() = withEnvironment(envVars) {

        authentication {
            idPorten {
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
