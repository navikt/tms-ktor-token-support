package no.nav.tms.token.support.idporten

import com.auth0.jwk.Jwk
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import java.security.interfaces.RSAPublicKey

internal fun Routing.loginApi(runtimeContext: RuntimeContext) {

    val settings = runtimeContext.oauth2ServerSettings

    authenticate(Idporten.authenticatorName) {
        get("/oauth2/callback") {
            val principal = checkNotNull(call.authentication.principal<OAuthAccessTokenResponse.OAuth2>())
            when (val decodedJWT = settings.verify(principal, runtimeContext)) {
                null -> call.respond(HttpStatusCode.InternalServerError, "Fant ikke ${Idporten.responseToken} i tokenrespons")
                else -> {
                    call.response.cookies.append(runtimeContext.tokenCookieName, decodedJWT.token, path = "/${runtimeContext.contextPath}")
                    call.response.cookies.appendExpired(Idporten.postLoginRedirectCookie, path = "/${runtimeContext.contextPath}")
                    call.respondRedirect(call.request.cookies[Idporten.postLoginRedirectCookie] ?: runtimeContext.postLoginRedirectUri)
                }
            }
        }

        get("/oauth2/login") {}
    }
}

internal fun OAuthServerSettings.OAuth2ServerSettings.verify(tokenResponse: OAuthAccessTokenResponse.OAuth2?, runtimeContext: RuntimeContext): DecodedJWT? =
tokenResponse?.idToken(Idporten.responseToken)?.let {
    runtimeContext.jwkProvider[JWT.decode(it).keyId].idTokenVerifier(
            clientId,
            runtimeContext.metadata.issuer
    ).verify(it)
}

internal fun OAuthAccessTokenResponse.OAuth2.idToken(tokenCookieName: String): String? = extraParameters[tokenCookieName]

internal fun Jwk.idTokenVerifier(clientId: String, issuer: String): JWTVerifier =
        JWT.require(this.RSA256())
                .withAudience(clientId)
                .withIssuer(issuer)
                .build()

internal fun Jwk.RSA256() = Algorithm.RSA256(publicKey as RSAPublicKey, null)
