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

    // Calls to this endpoint should be initiated by ID-porten through the user, after the user has signed out elsewhere
    get("/oauth2/logout") {
        call.invalidateCookie(context.tokenCookieName, context.contextPath)
        call.respondRedirect(context.postLogoutRedirectUri)
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
