package no.nav.tms.token.support.entraid.token.verification


import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.tms.token.support.entraid.token.verification.install.HttpClientBuilder
import no.nav.tms.token.support.entraid.token.verification.install.JwkProviderBuilderWrapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class EntraIdAuthTest {

    private val testServer = NaisApplication("test-cluster", "test-namespace", "server-app")
    private val testClient = NaisApplication("test-cluster", "test-namespace", "client-app")

    private val azureUrl = "https://azure"
    private val azureAudience = UUID.randomUUID().toString()
    private val azureJwk = JwkBuilder.generateJwk()

    private val tokenBuilder = EntraIdTokenBuilder(
        azureUrl = azureUrl,
        azureJwk = azureJwk,
        azureAudience = azureAudience,
        application = testServer
    )
    private val envVars = listOf(
        "AZURE_APP_CLIENT_ID" to azureAudience,
        "AZURE_APP_WELL_KNOWN_URL" to "$azureUrl/config"
    ).toMap()

    private val mockedClient = createMockedMockedClient(azureUrl)
    private val mockedJwkProvider = createMockedJwkProvider(azureJwk.toPublicJWK())

    @BeforeEach
    fun setupMock() {
        mockkObject(HttpClientBuilder)
        mockkObject(JwkProviderBuilderWrapper)
        every { HttpClientBuilder.build(any()) } returns mockedClient
        every { JwkProviderBuilderWrapper.createJwkProvider(any()) } returns mockedJwkProvider
    }

    @AfterEach
    fun cleanUp() {
        unmockkObject(HttpClientBuilder)
        unmockkObject(JwkProviderBuilderWrapper)
        EntraIdEnvironment.reset()
    }

    @Test
    fun `tillater autentisering av endepunkt med tokens fra entraid-azure`() = testApplication {
        EntraIdEnvironment.extend(envVars)

        application {
            authentication {
                entraId {

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

        val unauthorizedResponse = client.get("/test")

        val systemToken = tokenBuilder.azureSystemToken(testClient)
        val systemAuthorizedResponse = client.authorizedGet("/test", systemToken)

        val userToken = tokenBuilder.azureUserToken("A012345")
        val userAuthorizedResponse = client.authorizedGet("/test", userToken)

        unauthorizedResponse.status shouldBe HttpStatusCode.Unauthorized
        systemAuthorizedResponse.status shouldBe HttpStatusCode.OK
        userAuthorizedResponse.status shouldBe HttpStatusCode.OK
    }

    @Test
    fun `legger til principal i call-context med info fra system-token`() = testApplication {
        EntraIdEnvironment.extend(envVars)

        var systemPrincipal: EntraIdPrincipal? = null

        application {
            authentication {
                entraId {

                }
            }

            routing {
                authenticate {
                    get("/test") {
                        systemPrincipal = call.principal<EntraIdPrincipal>()

                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }

        val clientApplication = NaisApplication("cluster", "namespace", "the-calling-app")

        val token = tokenBuilder.azureSystemToken(client = clientApplication)

        val response = client.authorizedGet("/test", token)

        response.status shouldBe HttpStatusCode.OK

        systemPrincipal.shouldNotBeNull().let {
            it.issuedFor shouldBe clientApplication
        }
    }

    @Test
    fun `legger til principal i call-context med info fra brukers token`() = testApplication {
        EntraIdEnvironment.extend(envVars)

        var userPrincipal: EntraIdUserPrincipal? = null

        application {
            authentication {
                entraId {

                }
            }

            routing {
                authenticate {
                    get("/test") {
                        userPrincipal = call.principal<EntraIdUserPrincipal>()

                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }

        val navIdent = "U122333"
        val displayName = "Bruker Navnessen"
        val loginName = "navn@nav.no"
        val oid = UUID.randomUUID().toString()

        val token = tokenBuilder.azureUserToken(
            navIdent = navIdent,
            name = displayName,
            email = loginName,
            oid = oid,
        )

        val response = client.authorizedGet("/test", token)

        response.status shouldBe HttpStatusCode.OK

        userPrincipal.shouldNotBeNull().let {
            it.shouldNotBeNull()

            it.navIdent shouldBe navIdent
            it.displayName shouldBe displayName
            it.userName shouldBe loginName
            it.userId shouldBe oid

            it.issuedFor shouldBe testServer
        }
    }

    @Test
    fun `feiler ved oppstart hvis env ikke finnes`() = testApplication {
        shouldThrow<Exception> {
            application {
                authentication {
                    entraId {

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
    fun `feiler ved oppstart azure ikke kan nåes på well known url`() = testApplication {
        val feilaktigEnv = listOf(
            "IDPORTEN_WELL_KNOWN_URL" to "http://feil/config",
            "IDPORTEN_AUDIENCE" to azureAudience
        ).toMap()

        EntraIdEnvironment.extend(feilaktigEnv)

        shouldThrow<Exception> {
            application {
                authentication {
                    entraId {

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

    private suspend fun HttpClient.authorizedGet(path: String, token: String): HttpResponse {
        return get(path) {
            headers[HttpHeaders.Authorization] = "Bearer $token"
        }
    }
}

