package no.nav.tms.token.support.idporten.authentication

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.auth.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

// As of version 1.5.0, ktor oath2 does not natively support client authentication through 'private_key_jwt'.
// To solve this, we can intercept the call towards the auth issuer and replace the form parameters with the correct ones,
// which includes a signed jwt.
internal class IdportenClientInterceptor(privateJwk: String, private val clientId: String, private val audience: String) {

    val log = LoggerFactory.getLogger(IdportenClientInterceptor::class.java)

    private val privateRsaKey = RSAKey.parse(privateJwk)

    val appendClientAssertion: HttpRequestBuilder.() -> Unit = {

        val requestParams = (body as TextContent).text.parseUrlEncodedParameters()

        val code = requestParams[OAuth2RequestParameters.Code] ?: throw RuntimeException("Code missing")
        val redirectUri = requestParams[OAuth2RequestParameters.RedirectUri] ?: throw RuntimeException("RedirectUri missing")
        val jwt = clientAssertion(clientId, audience, privateRsaKey)

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

    private fun clientAssertion(clientId: String, audience: String, rsaKey: RSAKey): String {
        val now = Date.from(Instant.now())
        return JWTClaimsSet.Builder()
                .issuer(clientId)
                .audience(audience)
                .issueTime(now)
                .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                .jwtID(UUID.randomUUID().toString())
                .build()
                .sign(rsaKey)
                .serialize()
    }

    private fun JWTClaimsSet.sign(rsaKey: RSAKey): SignedJWT =
            SignedJWT(
                    JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(rsaKey.keyID)
                            .type(JOSEObjectType.JWT).build(),
                    this
            ).apply {
                sign(RSASSASigner(rsaKey.toPrivateKey()))
            }
}
