package no.nav.tms.token.support.idporten.authentication.logout

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.date.*
import no.nav.tms.token.support.idporten.authentication.LogoutPrincipal
import no.nav.tms.token.support.idporten.authentication.config.RuntimeContext
import java.net.URI

internal fun Routing.logoutApi(context: RuntimeContext) {

    authenticate(LogoutAuthenticator.name) {
        // Calling this endpoint with a bearer token will send a redirect to idporten to trigger single-logout
        get("/logout") {
            val principal = call.principal<LogoutPrincipal>()

            call.invalidateCookie(context.accessTokenCookieName, context.contextPath)
            call.invalidateCookie(context.idTokenTokenCookieName, context.contextPath)
            call.invalidateCookie(context.tokenRefreshCookieName, context.contextPath)

            if (principal == null) {
                call.respondRedirect(context.postLogoutRedirectUri)
            } else {
                call.redirectToSingleLogout(principal.idToken.token, context.metadata.logoutEndpoint, context.postLogoutRedirectUri)
            }
        }
    }

    // Calls to this endpoint should be initiated by ID-porten through the user, after the user has signed out elsewhere
    get("/oauth2/logout") {
        call.invalidateCookieForExternalLogout(context.accessTokenCookieName, context.contextPath, context.secureCookie)
        call.invalidateCookieForExternalLogout(context.idTokenTokenCookieName, context.contextPath, context.secureCookie)
        call.invalidateCookieForExternalLogout(context.tokenRefreshCookieName, context.contextPath, context.secureCookie)
        call.respond(OK)
    }
}

private fun ApplicationCall.invalidateCookie(cookieName: String, contextPath: String) {
    response.cookies.appendExpired(cookieName, path = "/$contextPath")
}

private suspend fun ApplicationCall.redirectToSingleLogout(idToken: String, signoutUrl: String, postLogoutUrl: String) {
    val urlBuilder = URLBuilder()
    urlBuilder.takeFrom(URI(signoutUrl))
    urlBuilder.parameters.apply {
        append("id_token_hint", idToken)
        append("post_logout_redirect_uri", postLogoutUrl)
    }

    val redirectUrl = urlBuilder.buildString()

    respondRedirect(redirectUrl)
}

private fun ApplicationCall.invalidateCookieForExternalLogout(cookieName: String, contextPath: String, secure: Boolean) {

    if (secure) {
        response.cookies.appendExpiredCrossSite(cookieName, contextPath)
    } else {
        response.cookies.appendExpired(cookieName, path = "/$contextPath")
    }
}

private fun ResponseCookies.appendExpiredCrossSite(cookieName: String, contextPath: String) {
    append(
            name = cookieName,
            value = "",
            path = "/$contextPath",
            expires = GMTDate.START,
            secure = true,
            extensions = mapOf("SameSite" to "None")
    )
}
