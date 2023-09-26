package no.nav.tms.token.support.tokenx.validation


import io.kotest.extensions.system.withEnvironment
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.tms.token.support.tokenx.validation.LevelOfAssurance.HIGH
import no.nav.tms.token.support.tokenx.validation.LevelOfAssurance.SUBSTANTIAL
import no.nav.tms.token.support.tokenx.validation.install.HttpClientBuilder
import no.nav.tms.token.support.tokenx.validation.install.IdPortenLevelOfAssurance.*
import no.nav.tms.token.support.tokenx.validation.install.JwkProviderBuilder
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TokenXAuthIT {

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
    fun `Should respond ok if valid bearer token is provided`() = testApplication {

        application {
            testApi()
        }

        val bearerToken = JwtBuilder.generateJwtString(clientId, High, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $bearerToken")
        }

        response.status `should be equal to` HttpStatusCode.OK
    }


    @Test
    fun `Should respond ok if valid bearer token is provided in auth header`() = testApplication {

        application {
            testApi()
        }

        val bearerToken = JwtBuilder.generateJwtString(clientId, High, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $bearerToken")
        }

        response.status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Should prioritize well-formed tokenx-auth header over regular auth header`() = testApplication {

        application {
            testApi()
        }

        val bearerToken = JwtBuilder.generateJwtString(clientId, High, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(TokenXHeader.Authorization, "Bearer $bearerToken")
            headers.append(HttpHeaders.Authorization, "Bearer othertoken")
        }

        response.status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Should fall back to regular auth header if tokenx-auth header is malformed`() = testApplication {

        application {
            testApi()
        }

        val bearerToken = JwtBuilder.generateJwtString(clientId, High, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(TokenXHeader.Authorization, "Malformed")
            headers.append(HttpHeaders.Authorization, "Bearer $bearerToken")
        }

        response.status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Should return 401 if tokenx token is well formed but invalid, even with valid auth token`() = testApplication {

        application {
            testApi()
        }

        val bearerToken = JwtBuilder.generateJwtString(clientId, High, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(TokenXHeader.Authorization, "Bearer invalid")
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

        val bearerToken = JwtBuilder.generateJwtString(targetApp, High, idportenMetadata.issuer, privateJwk)

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

        val bearerToken = JwtBuilder.generateJwtString(clientId, High, differentIssuer, privateJwk)

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

        val bearerToken = JwtBuilder.generateJwtString(targetApp, High, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $bearerToken")
        }

        response.status `should be equal to` HttpStatusCode.Unauthorized
        response.body<String>() `should be equal to` "Invalid or expired token."
    }

    @Test
    fun `Should respond with status 401 if level of assurance is too low`() = testApplication {

        application {
            testApi(minLoa = HIGH)
        }

        val bearerToken = JwtBuilder.generateJwtString(clientId, Low, idportenMetadata.issuer, privateJwk)

        val response = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $bearerToken")
        }

        response.status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `acr value Level4 should be equivalent to idporten-loa-high`() = testApplication {
        application {
            testApi(minLoa = HIGH)
        }

        val loaHighToken = JwtBuilder.generateJwtString(clientId, High, idportenMetadata.issuer, privateJwk)
        val level4Token = JwtBuilder.generateJwtString(clientId, Level4, idportenMetadata.issuer, privateJwk)
        val loaLowToken = JwtBuilder.generateJwtString(clientId, Substantial, idportenMetadata.issuer, privateJwk)
        val level3Token = JwtBuilder.generateJwtString(clientId, Level3, idportenMetadata.issuer, privateJwk)

        val loaHighResponse = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $loaHighToken")
        }
        val level4Response = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $level4Token")
        }
        val loaLowResponse = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $loaLowToken")
        }
        val level3Response = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $level3Token")
        }

        loaHighResponse.status `should be equal to` HttpStatusCode.OK
        level4Response.status `should be equal to` HttpStatusCode.OK
        loaLowResponse.status `should be equal to` HttpStatusCode.Unauthorized
        level3Response.status `should be equal to` HttpStatusCode.Unauthorized
    }

    @Test
    fun `acr value Level3 should be equivalent to idporten-loa-substantial`() = testApplication {
        application {
            testApi(minLoa = SUBSTANTIAL)
        }

        val loaSubstantialToken = JwtBuilder.generateJwtString(clientId, Substantial, idportenMetadata.issuer, privateJwk)
        val level3Token = JwtBuilder.generateJwtString(clientId, Level3, idportenMetadata.issuer, privateJwk)

        val loaLowResponse = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $loaSubstantialToken")
        }
        val level3Response = client.get("/test") {
            headers.append(HttpHeaders.Authorization, "Bearer $level3Token")
        }

        loaLowResponse.status `should be equal to` HttpStatusCode.OK
        level3Response.status `should be equal to` HttpStatusCode.OK
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

    @Test
    fun `Allows verifying different apis with different configurations`() = testApplication {

        application {
            withEnvironment(envVars) {
                authentication {
                    tokenX {
                        setAsDefault = true
                        levelOfAssurance = HIGH
                    }
                    tokenX {
                        setAsDefault = false
                        authenticatorName = "substantial"
                        levelOfAssurance = SUBSTANTIAL
                    }
                }
            }
            routing {
                authenticate {
                    get("/test/one") {
                        call.respond(HttpStatusCode.OK)
                    }
                }
                authenticate("substantial") {
                    get("test/two") {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }

        val loaSubstantialToken = JwtBuilder.generateJwtString(clientId, Substantial, idportenMetadata.issuer, privateJwk)

        client.get("/test/one") {
            headers.append(HttpHeaders.Authorization, "Bearer $loaSubstantialToken")
        }.status `should be equal to` HttpStatusCode.Unauthorized

        client.get("/test/two") {
            headers.append(HttpHeaders.Authorization, "Bearer $loaSubstantialToken")
        }.status `should be equal to` HttpStatusCode.OK
    }

    private fun Application.testApi(minLoa: LevelOfAssurance? = null) = withEnvironment(envVars) {

        authentication{
            tokenX {
                if (minLoa != null) {
                    levelOfAssurance = minLoa
                }
            }
        }

        routing {
            authenticate(TokenXAuthenticator.name) {
                get("/test") {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }

    private fun Application.testApiWithDefault() = withEnvironment(envVars) {

        authentication {
            tokenX {
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

