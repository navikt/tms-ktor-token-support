package no.nav.tms.token.support.idporten.authentication

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import no.nav.tms.token.support.idporten.JwkBuilder
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.`should not contain`
import org.junit.jupiter.api.Test

internal class IdportenClientInterceptorTest {
    private val jwk = JwkBuilder.generateJwk()
    private val clientId = "clientId"
    private val audience = "audience"

    private val interceptor = IdportenClientInterceptor(jwk, clientId, audience)

    @Test
    fun `Should intercept token calls of 'client_secret' format and transform to 'private_key_jwt' format`() {
        val url = "http://mock-issuer/token"
        val secret = "clientSecret"
        val redirectUri = "http://localhost/oauth2/callback"
        val code = "authCode"
        val grantType = "authorization_code"

        val originalParameters = ParametersBuilder().apply {
            append("grant_type", grantType)
            append("code", code)
            append("redirect_uri", redirectUri)
            append("client_secret", secret)
        }.build()

        val request = HttpRequestBuilder(url).apply {
            method = HttpMethod.Post

            header(
                    HttpHeaders.Accept,
                    listOf(ContentType.Application.FormUrlEncoded, ContentType.Application.Json).joinToString(",")
            )

            body = TextContent(originalParameters.formUrlEncode(), ContentType.Application.FormUrlEncoded)
        }

        interceptor.appendClientAssertion(request)

        val finalParams = (request.body  as TextContent).text.parseUrlEncodedParameters()

        finalParams.names() `should not contain` "client_secret"

        finalParams["grant_type"] `should be equal to` grantType
        finalParams["code"] `should be equal to` code
        finalParams["redirect_uri"] `should be equal to` redirectUri
        finalParams["client_assertion_type"] `should be equal to` "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"

        finalParams["client_assertion"].`should not be null`()
    }
}
