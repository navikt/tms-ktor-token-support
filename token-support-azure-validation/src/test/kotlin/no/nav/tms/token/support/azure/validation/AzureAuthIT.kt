package no.nav.tms.token.support.azure.validation


import io.kotest.extensions.system.withEnvironment
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.tms.token.support.azure.validation.config.HttpClientBuilder
import no.nav.tms.token.support.azure.validation.config.JwkProviderBuilder
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AzureAuthIT {

    private val clientId = "cluster:namespace:appname"

    private val envVars = listOf(
            "AZURE_APP_CLIENT_ID" to clientId,
            "AZURE_APP_WELL_KNOWN_URL" to "http://tokendings-url/config"
    ).toMap()

    private val privateJwk = JwkJwtBuilder.generateJwk()
    private val publicJwk = privateJwk.toPublicJWK()

    private val mockedClient = createMockedMockedClient()
    private val mockedJwkProvider = createMockedJwkProvider(publicJwk)

    @BeforeEach
    fun setupMock() {
        mockkObject(HttpClientBuilder)
        mockkObject(JwkProviderBuilder)
        every { HttpClientBuilder.build(any()) } returns mockedClient
        every { JwkProviderBuilder.createJwkProvider(any()) } returns mockedJwkProvider
    }

    @AfterEach
    fun cleanUp() {
        unmockkObject(HttpClientBuilder)
        unmockkObject(JwkProviderBuilder)
    }

    @Test
    fun `Should respond with status 401 if no bearer token was provided`() = testApplication {

        application {
            testApi()
        }

        val response = client.get("/test")

        response.status `should be equal to` HttpStatusCode.Unauthorized
        response.body<String>() `should be equal to` "No bearer token found."
    }

    @Test
    fun `Should respond with status 401 if bearer token is malformed`() = testApplication {

        application {
            testApi()
        }

        val response = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer <dummy>")
        }

        response.status `should be equal to` HttpStatusCode.Unauthorized
        response.body<String>() `should be equal to` "Invalid or expired token."
    }

    @Test
    fun `Should respond ok if valid bearer token is provided in azure-auth header`() = testApplication {

        application {
            testApi()
        }

        val bearerToken = JwtBuilder.generateJwtString(clientId, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(AzureHeader.Authorization, "Bearer $bearerToken")
        }

        response.status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Should respond ok if valid bearer token is provided in auth header`() = testApplication {

        application {
            testApi()
        }

        val bearerToken = JwtBuilder.generateJwtString(clientId, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $bearerToken")
        }

        response.status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Should prioritize well-formed azure-auth header over regular auth header`() = testApplication {

        application {
            testApi()
        }

        val bearerToken = JwtBuilder.generateJwtString(clientId, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(AzureHeader.Authorization, "Bearer $bearerToken")
            headers.append(HttpHeaders.Authorization, "Bearer othertoken")
        }

        response.status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Should fall back to regular auth header if azure-auth header is malformed`() = testApplication {

        application {
            testApi()
        }

        val bearerToken = JwtBuilder.generateJwtString(clientId, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(AzureHeader.Authorization, "Malformed")
            headers.append(HttpHeaders.Authorization, "Bearer $bearerToken")
        }

        response.status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Should return 401 if azure token is welformed but invalid, even with valid auth token`() = testApplication {

        application {
            testApi()
        }

        val bearerToken = JwtBuilder.generateJwtString(clientId, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(AzureHeader.Authorization, "Bearer invalid")
            headers.append(HttpHeaders.Authorization, "Bearer $bearerToken")
        }

        response.status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Should respond with status 401 if bearer belongs to different app`() = testApplication {

        application {
            testApi()
        }

        val targetApp = "cluster:namespace:otherApp"

        val bearerToken = JwtBuilder.generateJwtString(targetApp, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $bearerToken")
        }

        response.status `should be equal to` HttpStatusCode.Unauthorized
        response.body<String>() `should be equal to` "Invalid or expired token."
    }

    @Test
    fun `Should respond with status 401 if bearer token has unexpected issuer`() = testApplication {

        application {
            testApi()
        }

        val differentIssuer = "http://different-issuer/provider"

        val bearerToken = JwtBuilder.generateJwtString(clientId, differentIssuer, privateJwk)

        val response = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $bearerToken")
        }

        response.status `should be equal to` HttpStatusCode.Unauthorized
        response.body<String>() `should be equal to` "Invalid or expired token."
    }

    @Test
    fun `Should respond with status 401 if bearer token is expired`() = testApplication {

        application {
            testApi()
        }

        val targetApp = "cluster:namespace:otherApp"

        val bearerToken = JwtBuilder.generateJwtString(targetApp, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $bearerToken")
        }

        response.status `should be equal to` HttpStatusCode.Unauthorized
        response.body<String>() `should be equal to` "Invalid or expired token."
    }

    @Test
    fun `Should enable setting authenticator as default`() = testApplication {

        application {
            testApiWithDefault()
        }

        val response = client.get("/test")

        response.status `should be equal to` HttpStatusCode.Unauthorized
        response.body<String>() `should be equal to` "No bearer token found."
    }

    private fun Application.testApi() = withEnvironment(envVars) {

        installAzureAuth()

        routing {
            authenticate(AzureAuthenticator.name) {
                get("/test") {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }

    private fun Application.testApiWithDefault() = withEnvironment(envVars) {

        installAzureAuth {
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

