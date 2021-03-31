package no.nav.tms.token.support.idporten.authentication.logout

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.tms.token.support.idporten.authentication.IdTokenPrincipal
import no.nav.tms.token.support.idporten.authentication.config.RuntimeContext
import java.net.URI

internal fun Routing.logoutApi(context: RuntimeContext) {

    authenticate(LogoutAuthenticator.name) {
        // Calling this endpoint with a bearer token will send a redirect to idporten to trigger single-logout
        get("/logout") {
            val principal = call.principal<IdTokenPrincipal>()

            call.invalidateCookie(context.tokenCookieName, context.contextPath)

            if (principal == null) {
                call.respondRedirect(context.postLogoutRedirectUri)
            } else {
                call.redirectToSingleSignout(principal.decodedJWT.token, context.metadata.logoutEndpoint, context.postLogoutRedirectUri)
            }
        }
    }

    // This endpoint is intended to be called by idporten, and will invalidate idtokens issued for a specific session.
    // Not currently implemented due to concerns with state handling across different pods.
    get("/oauth2/logout") {
        call.respond(HttpStatusCode.OK)
    }
}

private fun ApplicationCall.invalidateCookie(cookieName: String, contextPath: String) {
    response.cookies.appendExpired(cookieName, path = "/$contextPath")

}

private suspend fun ApplicationCall.redirectToSingleSignout(idToken: String, signoutUrl: String, postLogoutUrl: String) {
    val urlBuilder = URLBuilder()
    urlBuilder.takeFrom(URI(signoutUrl))
    urlBuilder.parameters.apply {
        append("id_token_hint", idToken)
        append("post_logout_redirect_uri", postLogoutUrl)
    }

    val redirectUrl = urlBuilder.buildString()

    respondRedirect(redirectUrl)
}
