package no.nav.tms.token.support.idporten.validation


import io.kotest.extensions.system.withEnvironment
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.tms.token.support.idporten.validation.config.HttpClientBuilder
import no.nav.tms.token.support.idporten.validation.config.JwkProviderBuilder
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IdportenAuthIT {

    private val clientId = "cluster:namespace:appname"

    private val envVars = listOf(
            "TOKEN_X_CLIENT_ID" to clientId,
            "TOKEN_X_WELL_KNOWN_URL" to "http://tokendings-url/config"
    ).toMap()

    private val privateJwk = JwkJwtBuilder.generateJwk()
    private val publicJwk = privateJwk.toPublicJWK()

    private val mockedClient = createMockedMockedClient()
    private val mockedJwkProvider = createMockedJwkProvider(publicJwk)

    @BeforeEach
    fun setupMock() {
        mockkObject(HttpClientBuilder)
        mockkObject(JwkProviderBuilder)
        every { HttpClientBuilder.build() } returns mockedClient
        every { JwkProviderBuilder.createJwkProvider(any()) } returns mockedJwkProvider
    }

    @AfterEach
    fun cleanUp() {
        unmockkObject(HttpClientBuilder)
        unmockkObject(JwkProviderBuilder)
    }

    @Test
    fun `Should respond with status 401 if no bearer token was provided`()
            = withTestApplication<Unit>({ testApi() }) {

        val response = handleRequest(HttpMethod.Get, "/test").response

        response.status() `should be equal to` HttpStatusCode.Unauthorized
        response.content `should be equal to` "No bearer token found."
    }

    @Test
    fun `Should respond with status 401 if bearer token is malformed`()
            = withTestApplication<Unit>({ testApi() }) {

        val response = handleRequest(HttpMethod.Get, "/test") {
            addHeader(HttpHeaders.Authorization, "Bearer <dummy>")
        }.response

        response.status() `should be equal to` HttpStatusCode.Unauthorized
        response.content `should be equal to` "Invalid or expired token."
    }

    @Test
    fun `Should respond ok if valid bearer token is provided`()
            = withTestApplication<Unit>({ testApi() }) {

        val bearerToken = JwtBuilder.generateJwtString(clientId, idportenMetadata.issuer, privateJwk)

        val response = handleRequest(HttpMethod.Get, "/test") {
            addHeader(HttpHeaders.Authorization, "Bearer $bearerToken")
        }.response

        response.status() `should be equal to` HttpStatusCode.OK
    }


    @Test
    fun `Should respond ok if valid bearer token is provided in auth header`()
            = withTestApplication<Unit>({ testApi() }) {

        val bearerToken = JwtBuilder.generateJwtString(clientId, idportenMetadata.issuer, privateJwk)

        val response = handleRequest(HttpMethod.Get, "/test") {
            addHeader(HttpHeaders.Authorization, "Bearer $bearerToken")
        }.response

        response.status() `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Should prioritize well-formed idporten-auth header over regular auth header`()
            = withTestApplication<Unit>({ testApi() }) {

        val bearerToken = JwtBuilder.generateJwtString(clientId, idportenMetadata.issuer, privateJwk)

        val response = handleRequest(HttpMethod.Get, "/test") {
            addHeader(IdportenHeader.Authorization, "Bearer $bearerToken")
            addHeader(HttpHeaders.Authorization, "Bearer othertoken")
        }.response

        response.status() `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Should fall back to regular auth header if idporten-auth header is malformed`()
            = withTestApplication<Unit>({ testApi() }) {

        val bearerToken = JwtBuilder.generateJwtString(clientId, idportenMetadata.issuer, privateJwk)

        val response = handleRequest(HttpMethod.Get, "/test") {
            addHeader(IdportenHeader.Authorization, "Malformed")
            addHeader(HttpHeaders.Authorization, "Bearer $bearerToken")
        }.response

        response.status() `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Should return 401 if idporten token is welformed but invalid, even with valid auth token`()
            = withTestApplication<Unit>({ testApi() }) {

        val bearerToken = JwtBuilder.generateJwtString(clientId, idportenMetadata.issuer, privateJwk)

        val response = handleRequest(HttpMethod.Get, "/test") {
            addHeader(IdportenHeader.Authorization, "Bearer invalid")
            addHeader(HttpHeaders.Authorization, "Bearer $bearerToken")
        }.response

        response.status() `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `Should respond with status 401 if bearer belongs to different app`()
            = withTestApplication<Unit>({ testApi() }) {

        val targetApp = "cluster:namespace:otherApp"

        val bearerToken = JwtBuilder.generateJwtString(targetApp, idportenMetadata.issuer, privateJwk)

        val response = handleRequest(HttpMethod.Get, "/test") {
            addHeader(HttpHeaders.Authorization, "Bearer $bearerToken")
        }.response

        response.status() `should be equal to` HttpStatusCode.Unauthorized
        response.content `should be equal to` "Invalid or expired token."
    }

    @Test
    fun `Should respond with status 401 if bearer token has unexpected issuer`()
            = withTestApplication<Unit>({ testApi() }) {

        val differentIssuer = "http://different-issuer/provider"

        val bearerToken = JwtBuilder.generateJwtString(clientId, differentIssuer, privateJwk)

        val response = handleRequest(HttpMethod.Get, "/test") {
            addHeader(HttpHeaders.Authorization, "Bearer $bearerToken")
        }.response

        response.status() `should be equal to` HttpStatusCode.Unauthorized
        response.content `should be equal to` "Invalid or expired token."
    }

    @Test
    fun `Should respond with status 401 if bearer token is expired`()
            = withTestApplication<Unit>({ testApi() }) {

        val targetApp = "cluster:namespace:otherApp"

        val bearerToken = JwtBuilder.generateJwtString(targetApp, idportenMetadata.issuer, privateJwk)

        val response = handleRequest(HttpMethod.Get, "/test") {
            addHeader(HttpHeaders.Authorization, "Bearer $bearerToken")
        }.response

        response.status() `should be equal to` HttpStatusCode.Unauthorized
        response.content `should be equal to` "Invalid or expired token."
    }

    @Test
    fun `Should enable setting authenticator as default`()
            = withTestApplication<Unit>({ testApiWithDefault() }) {

        val response = handleRequest(HttpMethod.Get, "/test").response

        response.status() `should be equal to` HttpStatusCode.Unauthorized
        response.content `should be equal to` "No bearer token found."
    }

    private fun Application.testApi() = withEnvironment(envVars) {

        installIdportenAuth()

        routing {
            authenticate(IdportenAuthenticator.name) {
                get("/test") {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }

    private fun Application.testApiWithDefault() = withEnvironment(envVars) {

        installIdportenAuth {
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

