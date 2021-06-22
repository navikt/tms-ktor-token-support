package no.nav.tms.token.support.idporten.authentication

import io.ktor.auth.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import org.slf4j.LoggerFactory

// As of version 1.5.0, ktor oath2 does not natively support client authentication through 'private_key_jwt'.
// To solve this, we can intercept the call towards the auth issuer and replace the form parameters with the correct ones,
// which includes a signed jwt.
internal class IdportenClientInterceptor(clientAssertionService: ClientAssertionService, private val clientId: String, private val audience: String) {

    val log = LoggerFactory.getLogger(IdportenClientInterceptor::class.java)

    val appendClientAssertion: HttpRequestBuilder.() -> Unit = {

        val requestParams = (body as TextContent).text.parseUrlEncodedParameters()

        val code = requestParams[OAuth2RequestParameters.Code] ?: throw RuntimeException("Code missing")
        val redirectUri = requestParams[OAuth2RequestParameters.RedirectUri] ?: throw RuntimeException("RedirectUri missing")
        val jwt = clientAssertionService.createClientAssertion()

        val urlParameters = ParametersBuilder().apply {
            append(OAuth2RequestParameters.ClientId, clientId)
            append(OAuth2RequestParameters.GrantType, "authorization_code")
            append(OAuth2RequestParameters.Code, code)
            append(OAuth2RequestParameters.RedirectUri, redirectUri)
            append("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
            append("client_assertion", jwt)
        }

        body = TextContent(urlParameters.build().formUrlEncode(), ContentType.Application.FormUrlEncoded)
    }
}
