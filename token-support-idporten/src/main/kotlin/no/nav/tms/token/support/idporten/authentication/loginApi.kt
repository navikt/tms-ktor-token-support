package no.nav.tms.token.support.idporten.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.tms.token.support.idporten.authentication.config.Idporten
import no.nav.tms.token.support.idporten.authentication.config.RuntimeContext

internal fun Routing.loginApi(runtimeContext: RuntimeContext) {

    val settings = runtimeContext.oauth2ServerSettings

    authenticate(Idporten.authenticatorName) {
        // This method is empty because the authenticator will redirect any calls to idporten, which will in turn
        // redirect to 'oath2/callback'. This method exists to differentiate between internal and external redirects
        get("/oauth2/login") {}

        // Users should arrive at this endpoint after a redirect from idporten, which will include a 'code' parameter
        // This parameter will be used to retrieve the user's token directly from idporten, and will then be provided
        // to the user as a token. The name of this token is determined when installing authentication.
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
    }
}

private fun OAuthServerSettings.OAuth2ServerSettings.verify(tokenResponse: OAuthAccessTokenResponse.OAuth2?, runtimeContext: RuntimeContext): DecodedJWT? =
tokenResponse?.idToken(Idporten.responseToken)?.let {
    runtimeContext.jwkProvider[JWT.decode(it).keyId].idTokenVerifier(
            clientId,
            runtimeContext.metadata.issuer
    ).verify(it)
}

private fun OAuthAccessTokenResponse.OAuth2.idToken(tokenCookieName: String): String? = extraParameters[tokenCookieName]
